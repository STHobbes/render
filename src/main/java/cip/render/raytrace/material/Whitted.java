/*
 * Whitted.java
 *
 * Created on October 10, 2002, 11:44 PM
 * Copyright(c) 1993-2019 Crisis in Perspective, Inc.
 *                        PO Box 1949
 *                        Hood River, OR 97031
 *                        www.crisisinperspecive.com
 *
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * The GNU General Public License is available at:
 *      http://www.opensource.org/licenses/gpl-license.php
 */
package cip.render.raytrace.material;

import cip.render.DynXmlObjParseException;
import cip.render.raytrace.RayIntersection;
import cip.render.raytrace.interfaces.IRtBackground;
import cip.render.raytrace.interfaces.IRtGeometry;
import cip.render.raytrace.interfaces.IRtLight;
import cip.render.util.AngleF;
import cip.render.util3d.Line3f;
import cip.render.util3d.Vector3f;
import cip.render.utilColour.RGBf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.LinkedList;

/**
 * An implementation of the Whitted illumination model as described in Whitted, Turner (1980), "An Improved Illumination
 * Model for Shaded Display", Communications of the ACM, vol 23, no 6, pp 343-349.  The Whitted model is implemented on top of
 * the {@link cip.render.raytrace.material.Blinn} illumination model - the Blinn model provides the basic surface illumination, this model
 * adds the reflected and transmitted global illumination components.
 * <p>
 * This implementation supports a combination of a constant recursion (bounce) depth cutoff and adaptive recusion
 * depth cutoff based upon the contribution that the ray can make to the colour of the pixel.  Generally, depth is
 * adaptively terminated before the absolute cutoff ts reached and the absolute cutoff is a guard against pathologic
 * environments that could reflect rays forever (i.e. 100% reflective).
 * <p>
 * The Whitted illuminated material is specified as a node in an XML file as:
 * <pre>
 *     <font style="color:blue">&lt;<b>DynamicallyLoadedObject</b>class="cip.render.raytrace.material.Whitted" name="<font style="color:magenta"><i>materialName</i></font>"&gt;</font>
 *         <font style="color:blue">&lt;<b>colour</b> <font style="color:magenta"><i>RGBf_attributes</i></font>/&gt;</font>
 *         <font style="color:blue">&lt;<b>beta</b>&gt;<font style="color:magenta"><i>betaDegrees</i></font>&lt;/<b>beta</b>&gt;</font>
 *         <font style="color:blue">&lt;<b>conductor</b>/&gt;</font>
 *         <font style="color:blue">&lt;<b>dielectric</b>/&gt;</font>
 *         <font style="color:blue">&lt;<b>transparent</b>/&gt;</font>
 *         <font style="color:blue">&lt;<b>opaque</b>/&gt;</font>
 *         <font style="color:blue">&lt;<b>indexOfRefraction</b>&gt;<font style="color:magenta"><i>n</i></font>&lt;/<b>indexOfRefraction</b>&gt;</font>
 *         <font style="color:blue">&lt;<b>transmittance</b>&gt;<font style="color:magenta"><i>transmittance</i></font>&lt;/<b>transmittance</b>&gt;</font>
 *         <font style="color:blue">&lt;<b>DynamicallyLoadedObject</b> class="<font style="color:magenta"><i>slopeDistributionFunction</i></font>"/&gt;</font>
 *         <font style="color:blue">&lt;<b>DynamicallyLoadedObject</b> class="<font style="color:magenta"><i>geometricAttenuationFunction</i></font>"/&gt;</font>
 *     <font style="color:blue">&lt;/<b>DynamicallyLoadedObject</b>&gt;</font>
 * </pre>
 * <table border="0" width="90%">
 * <caption style="text-align:left">where:</caption> <tr>
 * <td style="width:5%"></td>
 * <td><table border="1" summary="">
 * <tr>
 * <td><tt>colour</tt> or <tt>colour</tt></td>
 * <td>The material colour as specified by the <tt><i>RGBf_attributes</i></tt> which are described in
 * {@link RGBf#setValue(Element, boolean)}
 * </td>
 * </tr>
 * <tr>
 * <td><tt>beta</tt></td>
 * <td>The <i>beta</i> angle, or angle of deviation between <b>N</b> and <b>H</b> when the slopes distribution
 * is half the value as when <b>N = H</b>.  Valid values are 2 degree to 45 degree.  The default is 45 degree
 * if not specified.
 * </td>
 * </tr>
 * <tr>
 * <td><tt>conductor</tt></td>
 * <td>If specified, the material is a conductor and the specular colour is set equal to the material colour.
 * <tt>conductor</tt> and <tt>dielectric</tt> are mutually exclusive.  If not specified, the material
 * defualts to a <tt>dielectric</tt>.
 * </td>
 * </tr>
 * <tr>
 * <td><tt>dielectric</tt></td>
 * <td>If specified, the material is a dielectric and the specular colour is 1,1,1.
 * <tt>conductor</tt> and <tt>dielectric</tt> are mutually exclusive.
 * </td>
 * </tr>
 * <tr>
 * <td><tt>opaque</tt></td>
 * <td>Ihis is an opaque, reflective material.
 * <tt>opaque</tt> and <tt>transparent</tt> are mutually exclusive.  If not specified, the material
 * defaults to a <tt>opaque</tt>.
 * </td>
 * </tr>
 * <tr>
 * <td><tt>transparent</tt></td>
 * <td>This is a transparent, reflective material.
 * <tt>opaque</tt> and <tt>transparent</tt> are mutually exclusive.  If not specified, the material
 * defaults to a <tt>opaque</tt>.
 * </td>
 * </tr>
 * <tr>
 * <td><tt>indexOfRefraction</tt></td>
 * <td>The index of refraction of the material.  This is only applicable for a transparent material.  If not
 * specified, the index of refraction defaults to 1.5 (the equivalent of common window glass).
 * </td>
 * </tr>
 * <tr>
 * <td><tt>transmittance</tt></td>
 * <td>This is the transmittance of the material.  This is only applicable for a transparent material.  If not
 * specified, the transmittance defaults to 0.95 (the equivalent of common window glass).
 * </td>
 * </tr>
 * <tr>
 * <td><tt>DynamicallyLoadedObject</tt></td>
 * <td>This is the specification of either the a slope distribution function (the object implements the
 * {@link cip.render.raytrace.interfaces.IRtD} interface) and/or the geometric attenuation function (the object
 * implements the {@link cip.render.raytrace.interfaces.IRtG} interface).  If not specified, the default
 * slope distribution function is the {@link cip.render.raytrace.material.D.Blinn} function, and the
 * geometric attenuation defaults to 1.
 * </td>
 * </tr>
 * </table>
 * </td>
 * </tr>
 * </table><br>
 * <p>
 * <b>Example of XML Specification</b>
 * <p>
 * The following specifies a glass material using the Blinn slope distibution function:
 * <pre>
 *     <font style="color:blue">&lt;<b>DynamicallyLoadedObject</b> class="cip.render.raytrace.material.Whitted" name="<font style="color:magenta">green</font>"&gt;</font>
 *         <font style="color:blue">&lt;<b>colour</b> rgb="<font style="color:magenta">0,1,0</font>"/&gt;</font>
 *         <font style="color:blue">&lt;<b>dielectric</b>/&gt;</font>
 *         <font style="color:blue">&lt;<b>beta</b>&gt;<font style="color:magenta">5</font>&lt;/<b>beta</b>&gt;</font>
 *         <font style="color:blue">&lt;<b>transparent</b>/&gt;</font>
 *         <font style="color:blue">&lt;<b>indexOfRefraction</b>&gt;<font style="color:magenta">1.5</font>&lt;/<b>indexOfRefraction</b>&gt;</font>
 *         <font style="color:blue">&lt;<b>transmittance</b>&gt;<font style="color:magenta">0.8</font>&lt;/<b>transmittance</b>&gt;</font>
 *         <font style="color:blue">&lt;<b>DynamicallyLoadedObject</b> class="<font style="color:magenta">cip.render.raytrace.material.D.Blinn</font>"/&gt;</font>
 *     <font style="color:blue">&lt;/<b>DynamicallyLoadedObject</b>&gt;</font>
 * </pre>
 *
 * @author royster.hall@gmail.com
 * @version 1.0
 * @since 1.0
 */
public class Whitted extends Blinn {
    private static final String XML_TAG_TRANSPARENT = "transparent";
    private static final String XML_TAG_OPAQUE = "opaque";
    private static final String XML_TAG_INDEX_OF_REF = "indexOfRefraction";
    private static final String XML_TAG_TRANSPARENCY = "transmittance";

    // instance fields
    protected boolean m_bIsTransparent = false;       // opaque by default
    protected float m_fIndexOfRefraction = 1.5f;      // index of refraction for glass
    protected float m_fKt = 0.95f;                    // transmission for glass

    /**
     * Creates a new instance of the <tt>Whitted</tt> illumination model.
     */
    public Whitted() {
        m_bReflective = true;
    }

    public Whitted(final String strName, final RGBf rgbMtl, final boolean bConductor, final AngleF aBeta) {
        // opaque reflective material
        super(strName, rgbMtl, bConductor, aBeta);
        m_bReflective = true;
        initForRender();
    }

    public Whitted(final String strName, final RGBf rgbMtl, final boolean bConductor, final AngleF aBeta,
                   final boolean bIsTransparent, final float fIndexOfRefraction, final float fKt) {
        // transparent matereial
        super(strName, rgbMtl, bConductor, aBeta);
        m_bReflective = true;
        m_bIsTransparent = bIsTransparent;
        m_fIndexOfRefraction = fIndexOfRefraction;
        m_fKt = fKt;
        initForRender();
    }

    /**
     * Query whether this is a transparent material.
     *
     * @return (boolean) <tt>true</tt> if this material is transparent, <tt>false</tt> if this material is opaque.
     */
    public final boolean getTransparent() {
        return m_bIsTransparent;
    }

    /**
     * Set whether this is a transparent material.
     *
     * @param bIsTransparent <tt>true</tt> if this material is transparent, <tt>false</tt> if this material is opaque.
     */
    public final void setTransparent(final boolean bIsTransparent) {
        m_bIsTransparent = bIsTransparent;
        initForRender();
    }

    /**
     *  Query the index of refraction for the material. Note: the index of refraction is only meaningful if
     *  the material is transparent.
     *
     * @return (float) The index of refraction for the material.
     */
    public final float getIndexOfRefraction() {
        return m_fIndexOfRefraction;
    }

    /**
     * Set the index of refraction for the material. Note: the index of refraction is only meaningful if
     * the material is transparent.
     *
     * @param fIndexOfRefraction (float) The index of refraction for the material.
     */
    public final void setIndexOfRefraction(final float fIndexOfRefraction) {
        m_fIndexOfRefraction = fIndexOfRefraction;
    }

    /**
     * Get the transmittance (the fraction of incident light that passes into a transparent material).  Note: the transmissivity
     * is only meaningful if the material is transparent.
     *
     * @return (float) Returns the transmissivity.
     */
    public final float getTransmissivity() {
        return m_fKt;
    }

    /**
     * Set the transmittance (the fraction of incident light that passes into a transparent material).  Note: the transmissivity
     * is only meaningful if the material is transparent.
     *
     * @param fKt (float) The transmissivity.
     */
    public final void setTransmissivity(final float fKt) {
        m_fKt = fKt;
        initForRender();
    }

    public Whitted setValue(final Whitted whittedMtl) {
        super.setValue(whittedMtl);
        m_bIsTransparent = whittedMtl.m_bIsTransparent;
        m_fIndexOfRefraction = whittedMtl.m_fIndexOfRefraction;
        m_fKt = whittedMtl.m_fKt;
        return this;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IDynXmlObject interface implementation                                                                                //
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void loadFromXml(final @NotNull Element xmlElement, final @Nullable LinkedList refObjectList) throws DynXmlObjParseException {
        try {
            // Read the specified components for the material
            Node domNode = xmlElement.getFirstChild();
            while (null != domNode) {
                if (domNode instanceof Element) {
                    final Element element = (Element) domNode;
                    // let the blinn model try to parse this element first
                    if (!parseBlinnTag(element)) {
                        // Wasn't part of the Blinn model, see if it is part of the Whitted model.
                        if (element.getTagName().equalsIgnoreCase(XML_TAG_TRANSPARENT)) {
                            m_bIsTransparent = true;
                        } else if (element.getTagName().equalsIgnoreCase(XML_TAG_OPAQUE)) {
                            m_bIsTransparent = false;
                        } else if (element.getTagName().equalsIgnoreCase(XML_TAG_INDEX_OF_REF)) {
                            Node textNode = element.getFirstChild();
                            while (null != textNode) {
                                if (textNode.getNodeType() == Node.TEXT_NODE) {
                                    m_fIndexOfRefraction = Float.parseFloat(textNode.getNodeValue().trim());
                                    break;
                                }
                                textNode = textNode.getNextSibling();
                            }
                        } else if (element.getTagName().equalsIgnoreCase(XML_TAG_TRANSPARENCY)) {
                            Node textNode = element.getFirstChild();
                            while (null != textNode) {
                                if (textNode.getNodeType() == Node.TEXT_NODE) {
                                    m_fKt = Float.parseFloat(textNode.getNodeValue().trim());
                                    if (m_fKt < 0.0f) {
                                        m_fKt = 0.0f;
                                    }         // effectively opaque
                                    else if (m_fKt > 1.0f) {
                                        m_fKt = 1.0f;
                                    }    // effectively clear
                                    break;
                                }
                                textNode = textNode.getNextSibling();
                            }
                        } else {
                            throw new DynXmlObjParseException("Unrecognized Whitted illumination model element <" +
                                    element.getTagName() + ">, terminating model parsing.");
                        }
                    }
                }
                domNode = domNode.getNextSibling();
            }
            // The Blinn material node has been read.  Derive the other components of the illumination model.
            initForRender();
        } catch (final Throwable t) {
            if (t instanceof DynXmlObjParseException) {
                throw (DynXmlObjParseException) t;
            } else {
                throw new DynXmlObjParseException("Whitted illumination model parse exception", t);
            }
        }
    }

    //-------------------------------------------------------------------------------------------------------------------------
    // This is a local initialization for rendering.  It is called with recursive rendering being false for a non-reflecting
    //  model (the standard blinn model), and true when used as the base for a reflective model (as in the base for the
    //  Whitted material).  If this is a transparent material, the Ks and Kd are scaled to fit in what is left after
    //  transmittance.
    protected void initForRender() {
        super.initForRender();
        if (m_bIsTransparent) {
            m_fKd *= (1.0f - m_fKt);
            m_fKs *= (1.0f - m_fKt);
        }
    }

    //-------------------------------------------------------------------------------------------------------------------------
    protected void internalToXmlElement(final Element element) {
        // conductor/dielectric
        final Element elTransparent = element.getOwnerDocument().createElement(m_bIsTransparent ? XML_TAG_TRANSPARENT : XML_TAG_OPAQUE);
        element.appendChild(elTransparent);
        // see if we need to write out transparent specific parameters
        if (m_bIsTransparent) {
            // transparency
            final Element elTransparency = element.getOwnerDocument().createElement(XML_TAG_TRANSPARENCY);
            element.appendChild(elTransparency);
            elTransparency.appendChild(element.getOwnerDocument().createTextNode(Float.toString(m_fKt)));
            // index of refraction
            final Element elN = element.getOwnerDocument().createElement(XML_TAG_INDEX_OF_REF);
            element.appendChild(elN);
            elN.appendChild(element.getOwnerDocument().createTextNode(Float.toString(m_fIndexOfRefraction)));
        }

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IRtMaterial interface implementation                                                                                       //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void getColor(final @NotNull RGBf rgb, final @NotNull RayIntersection intersection, final @NotNull IRtLight[] lights,
                         final @NotNull IRtGeometry[] rtObjects, final @NotNull IRtBackground rtBkg, final int nMaxRecursions,
                         final int nSample, final int nRandom) {
        boolean bIntersectObj = false;
        final RayIntersection intRflRfr = intersection.borrowIntersection();
        final RGBf rgbRflRfr = intersection.borrowRGB();
        final Vector3f vRflRfr = intersection.borrowVector();
        final Line3f lnRflRfr = intersection.borrowLine();
        try {
            //-----------------------------------------------------------------------
            // The base material colour from primary lights
            super.getColor(rgb, intersection, lights, rtObjects, rtBkg, 0, nSample, nRandom);
            //-----------------------------------------------------------------------
            // The reflected colour
            // test to see if there are any more recursions left for the material
            if ((nMaxRecursions > 0) && ((intersection.m_fMaxContribution * m_fKs) > PackageConstants.CUTOFF_CONTRIBUTION)) {
                // get the reflected vector
                vRflRfr.setToReflection(intersection.m_vNormal, intersection.m_vToEye);
                if (PackageConstants.VALIDITY_CHECKING && vRflRfr.dot(intersection.m_vNormal) <= 0.0f) {
                    System.out.println("cip.raytrace.material.Whitted(): unexpected negative R.N");
                }
                // initialize the reflected ray and an intersection, then use them in getting the colour of the reflected ray.
                lnRflRfr.setValue(intersection.m_pt, vRflRfr);
                intRflRfr.initialize(vRflRfr);
                intRflRfr.m_fMaxContribution = intersection.m_fMaxContribution * m_fKs;
                for (IRtGeometry rtObject : rtObjects) {
                    if (((intersection.m_rtObj != rtObject) || (!rtObject.isConvex())) &&
                            rtObject.getRayIntersection(intRflRfr, lnRflRfr, false, nSample, nRandom)) {
                        bIntersectObj = true;
                    }
                }
            }
            if (bIntersectObj) {
                intRflRfr.m_mtl.getColor(rgbRflRfr, intRflRfr, lights, rtObjects, rtBkg, nMaxRecursions - 1, nSample, nRandom);
            } else {
                rtBkg.getColor(rgbRflRfr, lnRflRfr, null);
            }
            rgb.add(rgbRflRfr.scale(m_fKs));

            //-----------------------------------------------------------------------
            // The refracted colour - this is a ray inside the object
            if (m_bIsTransparent) {
                boolean bSetRefracted = false;
                // test to see if there are any more recursions left for the material
                if ((nMaxRecursions > 0) && ((intersection.m_fMaxContribution * m_fKt) > PackageConstants.CUTOFF_CONTRIBUTION) &&
                        vRflRfr.setToRefraction(intersection.m_vNormal, intersection.m_vToEye, 1.0f, m_fIndexOfRefraction)) {
                    // the material is transparent, we can still recurse, and there is a refraction vector - the first step
                    // is to get the intersection going out of the current object
                    lnRflRfr.setValue(intersection.m_pt, vRflRfr);
                    intRflRfr.initialize(vRflRfr);
                    intRflRfr.m_fMaxContribution = intersection.m_fMaxContribution * m_fKt;
                    if (intersection.m_rtObj.getRayIntersection(intRflRfr, lnRflRfr, true, nSample, nRandom)) {
                        getInternalColor(rgbRflRfr, intRflRfr, lights, rtObjects, nMaxRecursions, 0, rtBkg, nSample, nRandom);
                        bSetRefracted = true;
                    }
                }
                if (!bSetRefracted) {
                    rtBkg.getColor(rgbRflRfr, null, null);
                }
                // add the refracted colour
                rgb.add(rgbRflRfr.scale(m_fKt));
            }
        } catch (final Throwable t) {
            t.printStackTrace();
            rgb.setValue(1.0f, 1.0f, 0.0f);
        } finally {
            intersection.returnIntersection(intRflRfr);
            intersection.returnRGB(rgbRflRfr);
            intersection.returnVector(vRflRfr);
            intersection.returnLine(lnRflRfr);
        }
    }

    //-------------------------------------------------------------------------------------------------------------------------
    public void getInternalColor(final RGBf rgb, final RayIntersection intersection, final IRtLight[] lights,
                                 final IRtGeometry[] rtObjects, final int nMaxRecursions, final int nInternalReflections,
                                 final IRtBackground rtBkg, final int nSample, final int nRandom) {
        // The intersection is an internal intersection.  The refracted ray goes outside the object, the reflected ray stays
        //  inside the object
        boolean bIntersectObj = false;
        final RayIntersection intRflRfr = intersection.borrowIntersection();
        final RGBf rgbRflRfr = intersection.borrowRGB();
        final Vector3f vRflRfr = intersection.borrowVector();
        final Line3f lnRflRfr = intersection.borrowLine();
        float fAtten = m_fKs;
        try {
            // get the color from the outside
            if (vRflRfr.setToRefraction(intersection.m_vNormal, intersection.m_vToEye, m_fIndexOfRefraction, 1.0f)) {
                lnRflRfr.setValue(intersection.m_pt, vRflRfr);
                intRflRfr.initialize(vRflRfr);
                intRflRfr.m_fMaxContribution = intersection.m_fMaxContribution * m_fKt;
                for (IRtGeometry rtObject : rtObjects) {
                    if (((intersection.m_rtObj != rtObject) || (!rtObject.isConvex())) &&
                            rtObject.getRayIntersection(intRflRfr, lnRflRfr, false, nSample, nRandom)) {
                        bIntersectObj = true;
                    }
                }
                if (bIntersectObj) {
                    intRflRfr.m_mtl.getColor(rgb, intRflRfr, lights, rtObjects, rtBkg, nMaxRecursions - 1, nSample, nRandom);
                } else {
                    rtBkg.getColor(rgb, lnRflRfr, null);
                }
                // The colour from the outside is scaled by the transmittance (the rest is reflected to the outside)
                rgb.scale(m_fKt);
            } else {
                // total internal reflection
                fAtten /= (1.0f - m_fKt);
                rgb.setValue(0.0f, 0.0f, 0.0f);
            }
            // now do the internally reflecting color
            if ((nInternalReflections < PackageConstants.MAX_INTERNAL_REFLECTION) && ((intersection.m_fMaxContribution * fAtten) > PackageConstants.CUTOFF_CONTRIBUTION)) {
                // we continue to recurse the internal refraction into the current object
                vRflRfr.setToReflection(intersection.m_vNormal, intersection.m_vToEye);
                lnRflRfr.setValue(intersection.m_pt, vRflRfr);
                intRflRfr.initialize(vRflRfr);
                intRflRfr.m_fMaxContribution = intersection.m_fMaxContribution * fAtten;
                if (intersection.m_rtObj.getRayIntersection(intRflRfr, lnRflRfr, true, nSample, nRandom)) {
                    getInternalColor(rgbRflRfr, intRflRfr, lights, rtObjects, nMaxRecursions, nInternalReflections + 1, rtBkg, nSample, nRandom);
                } else {
                    rgbRflRfr.setValue(1.0f, 0.5f, 0.0f);
                }
            } else {
                rtBkg.getColor(rgbRflRfr, null, null);
            }
            // add the internally reflected colour
            rgb.add(rgbRflRfr.scale(fAtten));
        } catch (final Throwable t) {
            t.printStackTrace();
            rgb.setValue(1.0f, 1.0f, 0.0f);
        } finally {
            intersection.returnIntersection(intRflRfr);
            intersection.returnRGB(rgbRflRfr);
            intersection.returnVector(vRflRfr);
            intersection.returnLine(lnRflRfr);
        }
    }
}
