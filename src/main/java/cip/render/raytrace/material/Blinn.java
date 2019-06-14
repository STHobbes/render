/*
 * Blinn.java
 *
 * Created on October 3, 2002, 9:46 PM
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

import cip.render.DynXmlObjLoader;
import cip.render.DynXmlObjParseException;
import cip.render.IDynXmlObject;
import cip.render.INamedObject;
import cip.render.raytrace.LightInfo;
import cip.render.raytrace.RayIntersection;
import cip.render.raytrace.interfaces.*;
import cip.render.util.AngleF;
import cip.render.util2d.Point2f;
import cip.render.util3d.Point3f;
import cip.render.util3d.Vector3f;
import cip.render.utilColour.RGBf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.LinkedList;

/**
 * This is an implementation of the Blinn illumination model as described in
 * Blinn, James F (1977), "Models of Light Reflection for Computer Synthesized Images", ACM Computer Graphics,
 * (SIGGRAPH 77), vol. 11, no. 2, pp.192-198.  In this paper, Blinn describes a specular reflection DGF
 * where: D is the slope-distribution function; G is the geometric attenuation function; and F is the
 * Fresnel reflectance.  Though Blinn mentions Fresnel reflectance, it is unclear whether it was used
 * in his images.  This implementation of the Blinn illumination model supports various implementations
 * of D and G, and sets F to 1.0.
 * <p>
 * The Blinn illuminated material is specified as a node in an XML file as:
 * <pre>
 *     <font style="color:blue">&lt;<b>DynamicallyLoadedObject</b> class="cip.raytrace.material.Blinn" name="<font style="color:magenta"><i>materialName</i></font>"&gt;</font>
 *       <font style="color:blue">&lt;<b>colour</b> <font style="color:magenta"><i>RGBf_attributes</i></font>/&gt;</font>
 *       <font style="color:blue">&lt;<b>beta</b>&gt;<font style="color:magenta"><i>betaDegrees</i></font>&lt;/<b>beta</b>&gt;</font>
 *       <font style="color:blue">&lt;<b>conductor</b>/&gt;</font>
 *       <font style="color:blue">&lt;<b>dielectric</b>/&gt;</font>
 *       <font style="color:blue">&lt;<b>DynamicallyLoadedObject</b> class="<font style="color:magenta"><i>slopeDistributionFunction</i></font>"/&gt;</font>
 *       <font style="color:blue">&lt;<b>DynamicallyLoadedObject</b> class="<font style="color:magenta"><i>geometricAttenuationFunction</i></font>"/&gt;</font>
 *     <font style="color:blue">&lt;/<b>DynamicallyLoadedObject</b>&gt;</font>
 * </pre>
 * <table border="0" width="90%">
 * <caption style="text-align:left">where:</caption> <tr>
 * <td style="width:5%"></td>
 * <td><table border="1" summary="">
 * <tr>
 * <td><tt>colour</tt> or <tt>colour</tt></td>
 * <td>The material colour as specified by the <tt><i>RGBf_attributes</i></tt> which are described in
 * {@link RGBf#setValue(Element, boolean)}.
 * </td>
 * </tr>
 * <tr>
 * <td><tt>emissivity</tt></td>
 * <td>The base material colour in the absense of any lighting as specified by the
 * <tt><i>RGBf_attributes</i></tt> which are described in
 * {@link RGBf#setValue(Element, boolean)}.
 * Emissivity can be used when an object needs to be a constant colour for chroma-key compositing, or
 * when a material is a light source.
 * </td>
 * </tr>
 * <tr>
 * <td><tt>beta</tt></td>
 * <td>The <i>beta</i> angle, or angle of deviation between <b>N</b> and <b>H</b> when the slope distribution
 * is half the value as when <b>N = H</b>.  Valid values are 2 degree to 45 degree.  The default is 45 degree
 * if not specified.
 * </td>
 * </tr>
 * <tr>
 * <td><tt>conductor</tt></td>
 * <td>If specified, the material is a conductor and the specular colour is set equal to the material colour.
 * <tt>conductor</tt> and <tt>dielectric</tt> are mutually exclusive.  If not specified, the material
 * defaults to a <tt>dielectric</tt>.
 * </td>
 * </tr>
 * <tr>
 * <td><tt>dielectric</tt></td>
 * <td>If specified, the material is a dielectric and the specular colour is 1,1,1.
 * <tt>conductor</tt> and <tt>dielectric</tt> are mutually exclusive.
 * </td>
 * </tr>
 * <tr>
 * <td><tt>DynamicallyLoadedObject</tt></td>
 * <td>This is the specification of either the a slope distribution function (the object implements the
 * {@link cip.render.raytrace.interfaces.IRtD} interface) or the geometric attenuation function (the object
 * implements the {@link cip.render.raytrace.interfaces.IRtG} interface).  If not specified, the default
 * slope distribution function is the {@link cip.render.raytrace.material.D.Blinn} function, and the
 * geometric attenuation defaults to a constant value of 1.
 * </td>
 * </tr>
 * </table>
 * </td>
 * </tr>
 * </table><br>
 * <p>
 * <b>Example of XML Specification</b>
 * <p>
 * The following specifies a matte green material using the Phong slope distibution function:
 * <pre>
 *     <font style="color:blue">&lt;<b>DynamicallyLoadedObject</b> class="cip.raytrace.material.Blinn" name="<font style="color:magenta">green</font>"&gt;</font>
 *       <font style="color:blue">&lt;<b>colour</b> rgb="<font style="color:magenta">0,1,0</font>"/&gt;</font>
 *       <font style="color:blue">&lt;<b>dielectric</b>/&gt;</font>
 *       <font style="color:blue">&lt;<b>beta</b>&gt;<font style="color:magenta">45</font>&lt;/<b>beta</b>&gt;</font>
 *       <font style="color:blue">&lt;<b>DynamicallyLoadedObject</b> class="<font style="color:magenta">cip.raytrace.material.D.Phong</font>"/&gt;</font>
 *     <font style="color:blue">&lt;/<b>DynamicallyLoadedObject</b>&gt;</font>
 * </pre>
 *
 * @author royster.hall@gmail.com
 * @version 1.0
 * @since 1.0
 */
public class Blinn implements IDynXmlObject, INamedObject, IRtMaterial {
    private static final String XML_TAG_EMISSIVITY = "emissivity";
    private static final String XML_TAG_COLOR = "color";
    private static final String XML_TAG_COLOUR = "colour";
    private static final String XML_TAG_BETA = "beta";
    private static final String XML_TAG_CONDUCTOR = "conductor";
    private static final String XML_TAG_DIELECTRIC = "dielectric";

    // The instance definition
    protected String m_strName = PackageConstants.DEFAULT_NAME;      // the material name
    protected boolean m_bReflective = false;
    // the specified components of the illumination model.
    protected RGBf m_rgbEmissive = new RGBf(0.0f, 0.0f, 0.0f);       // the spectral emissivity of the material
    protected RGBf m_rgbDiffuse = new RGBf(1.0f, 0.0f, 0.0f);        // the spectral diffuse colour (material colour)
    protected boolean m_bConductor = false;                           // is the material a conductor
    protected AngleF m_aBeta = new AngleF(AngleF.DEGREES, 45.0f);     // roughness - the angle at which the reflection
    //  drops to half of the value at the perfect
    //  reflection angle.
    protected IRtD m_D = new cip.render.raytrace.material.D.Blinn();      // the default slope distribution function
    protected IRtG m_G = null;                                     // no geometric attenuation is the default
    // the derived components of the illumination model.
    protected float m_fKd;                                          // diffuse coefficient
    protected float m_fKs;                                          // specular coefficient
    protected double m_dNs;                                          // specular exponent
    protected RGBf m_rgbSpecular = new RGBf();                     // the spectral specular colour

    /**
     * Creates a new instance of the <tt>Blinn</tt> illumination model.
     */
    public Blinn() {
    }

    /**
     * Creates a new instance of the <tt>Blinn</tt> illumination model.
     *
     * @param strName    (String) The material name.
     * @param rgbMtl     (RGBf, readonly) The (reflective) material color.
     * @param bConductor (boolean) <tt>true</tt> if the material is a coinductor (metallic), <tt>false</tt> if the material
     *                   is dielectric (plastic, non-metal))
     * @param aBeta      (AngleF, readonly) The <i>beta</i> angle, or angle of deviation between <b>N</b> and <b>H</b> when the
     *                   slope distribution is half the value as when <b>N = H</b>.  Valid values are 2 degree to 45 degree.
     */
    public Blinn(final String strName, final RGBf rgbMtl, final boolean bConductor, final AngleF aBeta) {
        m_strName = strName;
        m_rgbDiffuse.setValue(rgbMtl);
        m_bConductor = bConductor;
        m_aBeta.setValue(aBeta);
        initForRender();
    }

    /**
     * Get the emissive color for the material.
     *
     * @param rgb (RGBf, modified) The RGBf to be set to the emissive color.
     */
    public final void getEmissiveColour(@NotNull final RGBf rgb) {
        rgb.setValue(m_rgbEmissive);
    }

    /**
     * Set the emissive color for the material.
     *
     * @param rgb (RGBf, readonly) The emissive color.
     */
    public final void setEmissiveColour(final RGBf rgb) {
        m_rgbEmissive.setValue(rgb);
    }

    /**
     * Get the (reflective) color for the material.
     *
     * @param rgb (RGBf. modified) The RGBf to be set to the (reflective) color.
     */
    public final void getMaterialColour(@NotNull final RGBf rgb) {
        rgb.setValue(m_rgbDiffuse);
    }

    /**
     * Set the (reflective) color for the material.
     *
     * @param rgb (RGBf, readonly) The (reflective) color.
     */
    public final void setMaterialColour(final RGBf rgb) {
        m_rgbDiffuse.setValue(rgb);
        initForRender();
    }

    /**
     * Get whether this material is a conductor or dielectric.
     *
     * @return Returns <tt>true</tt> if this material is a conductor, and <tt>false</tt> ifm this material is a dielectric.
     */
    public final boolean getConductor() {
        return m_bConductor;
    }

    /**
     * Set whether this material is a conductor or dielectric.
     *
     * @param bConductor (boolean) <tt>true</tt> if this material is a conductor, and <tt>false</tt> ifm this material
     *                   is a dielectric.
     */
    public final void setConductor(final boolean bConductor) {
        m_bConductor = bConductor;
        initForRender();
    }

    /**
     * Set the specular beta for the material.
     *
     * @param aBeta (AngleF,modified) Set to the <i>beta</i> angle, or angle of deviation between <b>N</b> and <b>H</b> when the
     *              slope distribution is half the value as when <b>N = H</b>.  Valid values are 2 degree to 45 degree.
     */
    public final void getBeta(@NotNull final AngleF aBeta) {
        aBeta.setValue(m_aBeta);
    }

    /**
     * Set the specular beta for the material.
     *
     * @param aBeta (AngleF, readonly) The <i>beta</i> angle, or angle of deviation between <b>N</b> and <b>H</b> when the
     *              slope distribution is half the value as when <b>N = H</b>.  Valid values are 2 degree to 45 degree.
     */
    public final void setBeta(final AngleF aBeta) {
        m_aBeta.setValue(aBeta);
        initForRender();
    }


    /**
     * Initialize this material to be identical to an existing material.
     *
     * @param b The existing material this material will be made identical to
     * @return Returns this material.
     */
    public Blinn setValue(final Blinn b) {
        m_strName = b.m_strName;
        m_bReflective = b.m_bReflective;
        m_rgbEmissive.setValue(b.m_rgbEmissive);
        m_rgbDiffuse.setValue(b.m_rgbDiffuse);
        m_bConductor = b.m_bConductor;
        m_aBeta.setValue(b.m_aBeta);
        m_D = b.m_D;
        m_G = b.m_G;
        m_fKd = b.m_fKd;
        m_fKs = b.m_fKs;
        m_dNs = b.m_dNs;
        m_rgbSpecular.setValue(b.m_rgbSpecular);
        return this;
    }

    /**
     * Resets the diffuse RGB and the associated specular RGB based on whether the material is conductive.  It is
     * assumed everything else about the material is fine, so no re-initialization for rendering is performed.
     *
     * @param newRGB (RGBf, readonly) Sets the new reflectance for this material.
     * @return Returns this material.
     */
    public Blinn setRGB(final RGBf newRGB) {
        m_rgbDiffuse.setValue(newRGB);
        if (m_bConductor) {
            m_rgbSpecular.setValue(newRGB);
        }
        return this;
    }

    //-------------------------------------------------------------------------------------------------------------------------
    // This is a local initialization for rendering.  It is called with recursive rendering being false for a non-reflecting
    //  model (the standard blinn model), and true when used as the base for a reflective model (as in the base for the
    //  Whitted material).
    protected void initForRender() {
        // clamp beta into a reasonable range
        float fBeta = m_aBeta.getDegrees();
        if (fBeta < 2.0f) {
            fBeta = 2.0f;
        }         // reasonable minimum beta
        else if (fBeta > 45.0f) {
            fBeta = 45.0f;
        }  // reasonable maximum beta
        m_aBeta.setDegrees(fBeta);

        // initialize all of the coefficients
        ApproximateKdAndKs();
        if (m_bConductor) {
            m_rgbSpecular.setValue(m_rgbDiffuse);
        } else {
            m_rgbSpecular.setValue(1.0f, 1.0f, 1.0f);
        }

        // initialize the slope distribution function
        m_D.initialize(m_aBeta);
    }

    protected final void ApproximateKdAndKs() {
        // In the JOEY days we developed an empirical relationship between Ns, Ks, and Ks.  We'll derive what NS would be if
        //  we used the Blinn cosine distribution function for specular reflection, then get Kd and Ks fom that.  The development of
        //  this empirical relationship comes from Hall, "Illumination and Color in Computer Generated Ilagery," Appendix II,
        //  Controlling illumination.
        m_dNs = -(float) (Math.log(2.0) / Math.log((double) m_aBeta.cos()));
        double dR = (m_dNs - 5.0) / 100.0;
        if (dR < 0.0) dR = 0.0;
        if (dR > 1.0) dR = 1.0;
        if (m_bReflective) {
            // If there is recursive reflection/refraction then that is the primary source of illumination for
            //  all surfaces.  For the diffuse surfaces to look correct, some sort of reflected ray distribution or
            //  reflection filtering needs to be used to soften the reflection.
            m_fKd = 0.40f - (0.20f * (float) Math.sqrt(dR));
            m_fKs = 0.20f + (0.65f * (float) Math.sqrt(dR));
        } else {
            // If there is no recursive reflection, then the primary lighting is the only illumination.
            //  The diffuse and specular coefficients are higher in this case than they are when there is
            //  recursive reflection/refraction
            m_fKd = 0.65f - (0.30f * (float) Math.sqrt(dR));
            m_fKs = 0.05f + (0.90f * (float) Math.sqrt(dR));
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IDynXmlObject interface implementation                                                                                //
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void loadFromXml(final @NotNull Element xmlElement,
                            final @Nullable LinkedList refObjectList) throws DynXmlObjParseException {
        try {
            // Read the specified components for the material
            Node domNode = xmlElement.getFirstChild();
            while (null != domNode) {
                if (domNode instanceof Element) {
                    final Element element = (Element) domNode;
                    if (!parseBlinnTag(element)) {
                        throw new DynXmlObjParseException("Unrecognized Blinn illumination model element <" +
                                element.getTagName() + ">, terminating model parsing.");
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
                throw new DynXmlObjParseException("Blinn illumination model parse exception", t);
            }
        }
    }

    // the parsing of Blinn components of the illumination model.  Returns true if this element was recognized and parsed, false
    //  otherwise.
    protected boolean parseBlinnTag(final Element element) throws DynXmlObjParseException {
        if (element.getTagName().equalsIgnoreCase(XML_TAG_EMISSIVITY)) {
            // this is the material emissivity - the colour regardless of illumination
            m_rgbEmissive.setValue(element, true);
        } else if (element.getTagName().equalsIgnoreCase(XML_TAG_COLOR) ||
                element.getTagName().equalsIgnoreCase(XML_TAG_COLOUR)) {
            // this is the material colour.  it will be specified either as RGB values
            //  or a spectral curve
            m_rgbDiffuse.setValue(element, false);
        } else if (element.getTagName().equalsIgnoreCase(XML_TAG_BETA)) {
            Node domNode = element.getFirstChild();
            while (null != domNode) {
                if (domNode.getNodeType() == Node.TEXT_NODE) {
                    m_aBeta.setDegrees(Float.parseFloat(domNode.getNodeValue().trim()));
                    return true;
                }
                domNode = domNode.getNextSibling();
            }
        } else if (element.getTagName().equalsIgnoreCase(XML_TAG_CONDUCTOR)) {
            m_bConductor = true;
        } else if (element.getTagName().equalsIgnoreCase(XML_TAG_DIELECTRIC)) {
            m_bConductor = false;
        } else if (element.getTagName().equalsIgnoreCase(DynXmlObjLoader.XML_TAG)) {
            // this is a dynamically loaded object -- can we use it?
            final Object obj = DynXmlObjLoader.LoadObject(element, null);
            // now that we have loaded it, lets find out what it is
            if (obj instanceof IRtD) {
                m_D = (IRtD) obj;
            } else if (obj instanceof IRtG) {
                m_G = (IRtG) obj;
            } else {
                return false;
            }
        } else {
            return false;
        }
        return true;
    }

    public void toChildXmlElement(final @NotNull Element parentEl) {
        // the node for the Blinn material
        final Element element = parentEl.getOwnerDocument().createElement(DynXmlObjLoader.XML_TAG);
        parentEl.appendChild(element);
        element.setAttribute(DynXmlObjLoader.XML_ATTR_CLASS, this.getClass().getName());
        element.setAttribute(DynXmlObjLoader.XML_ATTR_NAME, m_strName);
        // the material emissivity
        if ((m_rgbEmissive.getRed() != 0.0f) || (m_rgbEmissive.getGreen() != 0.0f) || (m_rgbEmissive.getBlue() != 0.0f)) {
            final Element elRGBe = element.getOwnerDocument().createElement(XML_TAG_EMISSIVITY);
            element.appendChild(elRGBe);
            m_rgbEmissive.toXmlAttr(elRGBe);
        }
        // the material colour
        final Element elRGB = element.getOwnerDocument().createElement(XML_TAG_COLOUR);
        element.appendChild(elRGB);
        m_rgbDiffuse.toXmlAttr(elRGB);
        // beta
        final Element elBeta = element.getOwnerDocument().createElement(XML_TAG_BETA);
        element.appendChild(elBeta);
        elBeta.appendChild(parentEl.getOwnerDocument().createTextNode(Float.toString(m_aBeta.getDegrees())));
        // conductor/dielectric
        final Element elConductor = element.getOwnerDocument().createElement(m_bConductor ? XML_TAG_CONDUCTOR : XML_TAG_DIELECTRIC);
        element.appendChild(elConductor);
        // slope distribution function
        ((IDynXmlObject) m_D).toChildXmlElement(element);
        // anything else that might need to be written out
        internalToXmlElement(element);
    }

    protected void internalToXmlElement(final Element element) {
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // INamedObject interface implementation                                                                                 //
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public @NotNull String getName() {
        return m_strName;
    }

    public void setName(final @NotNull String strName) {
        m_strName = strName;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IRtMaterial interface implementation                                                                                  //
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void initSampling(final int nSample, final @NotNull float[] f1dSample, final float[] f1dRandom,
                             final @NotNull Point2f[] pt2dSample, final Point2f[] pt2dRandom,
                             final @NotNull Point3f[] pt3dSample, final Point3f[] pt3dRandom) {
    }

    //-------------------------------------------------------------------------------------------------------------------------
    public void getColor(final @NotNull RGBf rgb, final @NotNull RayIntersection intersection,
                         final @NotNull IRtLight[] lights, final @NotNull IRtGeometry[] rtObjects,
                         final @NotNull IRtBackground rtBkg, final int nMaxRecursions,
                         final int nSample, final int nRandom) {
        rgb.setValue(m_rgbEmissive);
        if (null == lights) return;
        final LightInfo lightInfo = intersection.borrowLightInfo();
        final Vector3f vL = intersection.borrowVector();
        final Vector3f vH = intersection.borrowVector();
        final RGBf rgbTemp = intersection.borrowRGB();
        // loop through the lights and sum the color contribution from each light
        try {
            for (IRtLight light : lights) {
                if (light.getLight(lightInfo, intersection, nSample, nRandom)) {
                    switch (lightInfo.m_nType) {
                        case LightInfo.AMBIENT:
                            rgb.add(lightInfo.m_rgb.mult(m_rgbDiffuse).scale(m_fKd));
                            break;
                        case LightInfo.DIRECTIONAL:
                        case LightInfo.LOCAL:
                            // The light should return true ONLY if it actually illuminates the surface (neglecting shadows)
                            vL.setValue(lightInfo.m_vDir).reverse(); // direct from the intersection to the light
                            boolean bInShadow = false;
                            for (IRtGeometry rtObject : rtObjects) {
                                // break on the first object that casts a shadow, otherwise they all need testing.  Note,
                                //  we do not need to test the object this intersection is on if it is a convex object.
                                if (((intersection.m_rtObj != rtObject) || (!rtObject.isConvex())) &&
                                        rtObject.testShadow(intersection, vL, lightInfo.m_fDist, light, nSample, nRandom)) {
                                    bInShadow = true;
                                    break;
                                }
                            }
                            if (!bInShadow) {
                                final float fNdotL = intersection.m_vNormal.dot(vL);
                                if (!PackageConstants.VALIDITY_CHECKING || (fNdotL > 0.0f)) {
                                    // add the diffuse contribution
                                    rgb.add(rgbTemp.setValue(lightInfo.m_rgb).mult(
                                            m_rgbDiffuse).scale(fNdotL * m_fKd));
                                    // add the specular contribution
                                    vH.setValue(vL).add(intersection.m_vToEye);
                                    if (!PackageConstants.VALIDITY_CHECKING || (vH.dot(intersection.m_vNormal) > 0.0f)) {
                                        // although it shouldn't be possible for N.H to be negative since both the eye and the light
                                        //  ray are on the outside of the surface. We occasionally encounter a negative N.L presumably
                                        //  as a result of numerical error.  Most D and G functions do not respond will to negative
                                        //  values of N.H
                                        vH.normalize();
                                        float fScale = m_D.evaluate(intersection.m_vNormal, vH, intersection.m_vToEye, vL) * m_fKs;
                                        if (null != m_G) {
                                            fScale *= m_G.evaluate(intersection.m_vNormal, vH, intersection.m_vToEye, vL);
                                        }
                                        rgb.add(rgbTemp.setValue(lightInfo.m_rgb).mult(m_rgbSpecular).scale(fScale));
                                    } else {
                                        System.out.println("unexpected negative N.H");
                                    }
                                } else {
                                    final float fD = -((intersection.m_vNormal.i * intersection.m_pt.x) +
                                            (intersection.m_vNormal.j * intersection.m_pt.y) +
                                            (intersection.m_vNormal.k * intersection.m_pt.z));
                                    final float fPerpDist = (intersection.m_vNormal.i * lightInfo.m_ptFrom.x) +
                                            (intersection.m_vNormal.j * lightInfo.m_ptFrom.y) +
                                            (intersection.m_vNormal.k * lightInfo.m_ptFrom.z) + fD;
                                    System.out.println("unexpected negative N.L (" + fNdotL + ") on light " + light);
                                }
                            }
                            break;
                    }
                }
            }
        } catch (final Throwable t) {
            rgb.setValue(1.0f, 1.0f, 0.0f);
        } finally {
            intersection.returnLightInfo(lightInfo);
            intersection.returnVector(vH);
            intersection.returnVector(vL);
            intersection.returnRGB(rgbTemp);
        }
    }

}
