/*
 * Hall.java
 *
 * Created on October 28, 2002, 11:07 AM
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
import cip.render.util3d.Line3f;
import cip.render.util3d.Point3f;
import cip.render.util3d.Vector3f;
import cip.render.utilColour.Fresnel;
import cip.render.utilColour.FresnelConductor;
import cip.render.utilColour.FresnelDielectric;
import cip.render.utilColour.RGBf;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is an implementation of the Hall illumination model as described in
 * Hall, Roy A and D P Greenberg (1983), "A Testbed for Realistic Image Synthesis", IEEE Computer
 * Graphics and Applications, Nov 1983; and later in Hall, R A, <i>Color and Illumination in Computer Generated
 * Imagery</i>, Springer-Verlag, New York 1988. In this model uses the recursive model of Whitted, enhanced
 * with the Fresnel reflection approximations from Cook, and adding transmitted illumination from primary
 * light sources.  This implementation supports various implementations
 * of D and G, and sets F to 1.0.
 * <p>
 * This implementation supports a combination of a constant recursion (bounce) depth cutoff and adaptive recusion
 * depth cutoff based upon the contribution that the ray can make to the colour of the pixel.  Generally, depth is
 * adaptively terminated before the absolute cutoff ts reached and the absolute cutoff is a guard against pathologic
 * environments that could reflect rays forever (i.e. 100% reflective).
 * <p>
 * The Hall illuminated material is specified as a node in an XML file as:<br><br>
 * <pre>
 *     <font style="color:blue">&lt;<b>DynamicallyLoadedObject</b> class="cip.render.raytrace.material.Hall" name="<font style="color:magenta"><i>materialName</i></font>"&gt;</font>
 *       <font style="color:blue">&lt;<b>colour</b> <font style="color:magenta"><i>RGBf_attributes</i></font>/&gt;</font>
 *       <font style="color:blue">&lt;<b>beta</b>&gt;<font style="color:magenta"><i>betaDegrees</i></font>&lt;/<b>beta</b>&gt;</font>
 *       <font style="color:blue">&lt;<b>conductor</b>/&gt;</font>
 *       <font style="color:blue">&lt;<b>dielectric</b>/&gt;</font>
 *       <font style="color:blue">&lt;<b>transparent</b>/&gt;</font>
 *       <font style="color:blue">&lt;<b>opaque</b>/&gt;</font>
 *       <font style="color:blue">&lt;<b>indexOfRefraction</b>&gt;<font style="color:magenta"><i>indexOfRefraction</i></font>&lt;/<b>indexOfRefraction</b>&gt;</font>
 *       <font style="color:blue">&lt;<b>coefficientOfExtinction</b>&gt;<font style="color:magenta"><i>coefficientOfExtinction</i></font>&lt;/<b>coefficientOfExtinction</b>&gt;</font>
 *       <font style="color:blue">&lt;<b>DynamicallyLoadedObject</b> class="<font style="color:magenta"><i>slopeDistributionFunction</i></font>"/&gt;</font>
 *       <font style="color:blue">&lt;<b>DynamicallyLoadedObject</b> class="<font style="color:magenta"><i>geometricAttenuationFunction</i></font>"/&gt;</font>
 *     <font style="color:blue">&lt;/<b>DynamicallyLoadedObject</b>&gt;</font>
 * </pre>
 * <table border="0" width="90%">
 * <caption style="text-align:left">where:</caption>
 * <tr>
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
 * <td>The index of refraction of the material.  This is only applicable for both a transparent material or opaque
 * material.  If not specified explicitly or within the sprctral file description of the <tt>colour</tt>,
 * the index of refraction defaults is approximated using the material <tt>colour</tt> and the Fresnel
 * relationships.
 * </td>
 * </tr>
 * <tr>
 * <td><tt>coefficientOfExtinction</tt></td>
 * <td>The coefficient of extinction applies to conductive opaque materials only..  If not
 * specified explicitly or within the sprctral file description of the <tt>colour</tt>, the coefficient
 * of extinction is approximated using the Fresnel relationships with either the specified index-of-refraction
 * or an index of refraction of 1.
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
 * The following specifies a glass material using the Blinn slope distibution function:<br><br>
 * <pre>
 *     <font style="color:blue">&lt;<b>DynamicallyLoadedObject</b> class="cip.render.raytrace.material.Hall" name="<font style="color:magenta">glass</font>"&gt;</font>
 *       <font style="color:blue">&lt;<b>colour</b> rgb="<font style="color:magenta">0.15f,0.15f,0.15f</font>"/&gt;</font>
 *       <font style="color:blue">&lt;<b>dielectric</b>/&gt;</font>
 *       <font style="color:blue">&lt;<b>beta</b>&gt;<font style="color:magenta">2</font>&lt;/<b>beta</b>&gt;</font>
 *       <font style="color:blue">&lt;<b>transparent</b>/&gt;</font>
 *       <font style="color:blue">&lt;<b>indexOfRefraction</b>&gt;<font style="color:magenta">1.5</font>&lt;/<b>indexOfRefraction</b>&gt;</font>
 *     <font style="color:blue">&lt;/<b>DynamicallyLoadedObject</b>&gt;</font>
 * </pre>
 *
 * @author royster.hall@gmail.com
 * @version 1.0
 * @since 1.0
 */
public class Hall implements IDynXmlObject, INamedObject, IRtMaterial {
    static final Logger logger = Logger.getLogger(Hall.class.getName());
    static boolean loggingFine = logger.isLoggable(Level.FINE);

    private static final String XML_TAG_EMISSIVITY = "emissivity";
    private static final String XML_TAG_COLOR = "color";
    private static final String XML_TAG_COLOUR = "colour";
    private static final String XML_TAG_BETA = "beta";
    private static final String XML_TAG_CONDUCTOR = "conductor";
    private static final String XML_TAG_DIELECTRIC = "dielectric";
    private static final String XML_TAG_TRANSPARENT = "transparent";
    private static final String XML_TAG_OPAQUE = "opaque";
    private static final String XML_TAG_INDEX_OF_REF = "indexOfRefraction";
    private static final String XML_TAG_COEFFICIENT_OF_EXTINCTION = "coefficientOfExtinction";

    private static final float KD_REFLECTIVE_BASE = 0.40f;
    private static final float KD_REFLECTIVE_FACTOR = 0.20f;
    private static final float KS_REFLECTIVE_BASE = 0.20f;
    private static final float KS_REFLECTIVE_FACTOR = 0.75f;


    // The instance definition
    protected String m_strName = PackageConstants.DEFAULT_NAME;      // the material name
    // the specified components of the illumination model.
    protected RGBf m_rgbEmissive = new RGBf(0.0f, 0.0f, 0.0f);       // the spectral emissivity of the material
    protected RGBf m_rgbDiffuse = new RGBf(1.0f, 0.0f, 0.0f);        // the spectral diffuse colour (material colour)
    protected boolean m_bConductor = false;                           // is the material a conductor
    protected boolean m_bTransparent = false;                         // opaque by default
    protected float m_fIndexOfRefraction = -1.0f;                   // the index of refraction.
    protected float m_fCoefficientOfExtinction = -1.0f;             // the index of refraction.
    protected AngleF m_aBeta = new AngleF(AngleF.DEGREES, 45.0f);     // roughness - the angle at which the reflection
    //  drops to half of the value at the perfect
    //  reflection angle.
    protected Fresnel m_F = null;
    protected IRtD m_D = new cip.render.raytrace.material.D.Blinn();      // the default slope distribution function
    protected IRtG m_G = null;                                     // no geometric attenuation is the default
    // the derived components of the illumination model.
    protected float m_fKd;                                          // diffuse coefficient
    protected float m_fKs;                                          // specular coefficient
    protected double m_dNs;                                          // specular exponent
    protected RGBf m_rgbSpecular = new RGBf();                     // the spectral specular colour

    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Creates a new instance of the <tt>Hall</tt> illumination model.
     */
    public Hall() {
    }


    /**
     * Creates a new instance of the <tt>Hall</tt> illumination model.
     *
     * @param strName    The illumination model name.
     * @param rgbMtl     The RGB diffuse color of the material.
     * @param bConductor <tt>true</tt> if the material is a conductor (metal), <tt>false</tt> otherwise.
     * @param aBeta      The angle at which reflection drops to 0.5 (roughness).
     */
    public Hall(final String strName, final RGBf rgbMtl, final boolean bConductor, final AngleF aBeta) {
        m_strName = strName;
        m_rgbDiffuse.setValue(rgbMtl);
        m_bConductor = bConductor;
        m_aBeta.setValue(aBeta);
        initForRender();
    }

    //-------------------------------------------------------------------------------------------------------------------------
    public final void getMaterialColour(final RGBf rgb) {
        rgb.setValue(m_rgbDiffuse);
    }

    public final void setMaterialColour(final RGBf rgb) {
        m_rgbDiffuse.setValue(rgb);
        initForRender();
    }

    public final boolean getConductior() {
        return m_bConductor;
    }

    public final void setConductior(final boolean bConductor) {
        m_bConductor = bConductor;
        initForRender();
    }

    public final void getBeta(final AngleF aBeta) {
        aBeta.setValue(m_aBeta);
    }

    public final void setBeta(final AngleF aBeta) {
        m_aBeta.setValue(aBeta);
        initForRender();
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
        } else if (m_bTransparent) {
            m_rgbSpecular.setValue(m_rgbDiffuse);
        } else {
            m_rgbSpecular.setValue(0.5f, 0.5f, 0.5f);
        }

        // initialize the slope distribution function
        m_D.initialize(m_aBeta);

        if (m_bConductor) {
            if ((m_fIndexOfRefraction != -1.0f) && (m_fCoefficientOfExtinction != -1.0f)) {
                // the index of refraction and coefficient of exteniction are both set, use them in the computation
                m_F = new FresnelConductor(m_rgbDiffuse, m_fIndexOfRefraction, m_fCoefficientOfExtinction);
            } else {
                // the index of refraction and coefficient of exteniction are not both set, use the the initialization
                //  that approximates them based on the reflectivity of the material.
                m_F = new FresnelConductor(m_rgbDiffuse);
            }
            m_bTransparent = false;
        } else {
            if (m_fIndexOfRefraction == -1.0f) {
                // the index of refraction was not set -- use 1.5 as a pretty reliable value for a glass material or
                //  plastic substrate
                m_fIndexOfRefraction = 1.5f;
            }
            m_F = new FresnelDielectric(m_rgbSpecular, m_fIndexOfRefraction);
            if (m_bTransparent) {
                // roughness adjustment: OK, there is a real problem when we take something like
                //	glass with a reflectance of 5% and rough it up. The Ks factor goes down a lot
                //	and it is multiplied by the transparency --- so reflection and refraction go
                //	way down, but the diffuse doesn't come up enough to look right.
                final float fKdMin = KD_REFLECTIVE_BASE - KD_REFLECTIVE_FACTOR;
                final float fKsMax = KS_REFLECTIVE_BASE + KS_REFLECTIVE_FACTOR;
                m_fKd = fKdMin + (0.75f * (fKsMax - m_fKs)) / m_F.getAveReflectance();
            }
        }
    }

    //-------------------------------------------------------------------------------------------------------------------------
    protected final void ApproximateKdAndKs() {
        // In the JOEY days we developed an empirical relationship between Ns, Ks, and Ks.  We'll derive what NS would be if
        //  we used the Blinn cosine distribution function for specular reflection, then get Kd and Ks fom that.  The development of
        //  this empirical relationship comes from Hall, "Illumination and Color in Computer Generated Ilagery," Appendix II,
        //  Controlling illumination.
        m_dNs = -(float) (Math.log(2.0) / Math.log((double) m_aBeta.cos()));
        double dR = (m_dNs - 5.0) / 100.0;
        if (dR < 0.0) dR = 0.0;
        if (dR > 1.0) dR = 1.0;
        // always reflective - use the reflective coefficients with an adjustment for the Kd
        m_fKd = KD_REFLECTIVE_BASE - (KD_REFLECTIVE_FACTOR * (float) Math.sqrt(dR));
        m_fKs = KS_REFLECTIVE_BASE + (KS_REFLECTIVE_FACTOR * (float) Math.sqrt(dR));
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IDynXmlObject interface implementation                                                                                //
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void loadFromXml(final @NotNull Element xmlElement, final LinkedList<INamedObject> refObjectList) throws DynXmlObjParseException {
        m_fIndexOfRefraction = -1.0f;
        m_fCoefficientOfExtinction = -1.0f;
        try {
            // Read the specified components for the material
            Node domNode = xmlElement.getFirstChild();
            while (null != domNode) {
                if (domNode instanceof Element) {
                    final Element element = (Element) domNode;
                    boolean bParsed = false;
                    if (element.getTagName().equalsIgnoreCase(XML_TAG_EMISSIVITY)) {
                        // this is the material emissivity - the colour regardless of illumination
                        m_rgbEmissive.setValue(element, true);
                        bParsed = true;
                    } else if (element.getTagName().equalsIgnoreCase(XML_TAG_COLOR) ||
                            element.getTagName().equalsIgnoreCase(XML_TAG_COLOUR)) {
                        // this is the material colour.  it will be specified either as RGB values
                        //  or a spectral curve
                        m_rgbDiffuse.setValue(element, false);
                        bParsed = true;
                    } else if (element.getTagName().equalsIgnoreCase(XML_TAG_BETA)) {
                        Node textNode = element.getFirstChild();
                        while (null != textNode) {
                            if (textNode.getNodeType() == Node.TEXT_NODE) {
                                m_aBeta.setDegrees(Float.parseFloat(textNode.getNodeValue().trim()));
                                bParsed = true;
                                break;
                            }
                            textNode = textNode.getNextSibling();
                        }
                    } else if (element.getTagName().equalsIgnoreCase(XML_TAG_CONDUCTOR)) {
                        m_bConductor = true;
                        bParsed = true;
                    } else if (element.getTagName().equalsIgnoreCase(XML_TAG_DIELECTRIC)) {
                        m_bConductor = false;
                        bParsed = true;
                    } else if (element.getTagName().equalsIgnoreCase(DynXmlObjLoader.XML_TAG)) {
                        // this is a dynamically loaded object -- can we use it?
                        final Object obj = DynXmlObjLoader.LoadObject(element, null);
                        // now that we have loaded it, lets find out what it is
                        if (obj instanceof IRtD) {
                            m_D = (IRtD) obj;
                            bParsed = true;
                        } else if (obj instanceof IRtG) {
                            m_G = (IRtG) obj;
                            bParsed = true;
                        }
                    } else if (element.getTagName().equalsIgnoreCase(XML_TAG_TRANSPARENT)) {
                        m_bTransparent = true;
                    } else if (element.getTagName().equalsIgnoreCase(XML_TAG_OPAQUE)) {
                        m_bTransparent = false;
                    } else if (element.getTagName().equalsIgnoreCase(XML_TAG_INDEX_OF_REF)) {
                        Node textNode = element.getFirstChild();
                        while (null != textNode) {
                            if (textNode.getNodeType() == Node.TEXT_NODE) {
                                m_fIndexOfRefraction = Float.parseFloat(textNode.getNodeValue().trim());
                                bParsed = true;
                                break;
                            }
                            textNode = textNode.getNextSibling();
                        }
                    } else if (element.getTagName().equalsIgnoreCase(XML_TAG_COEFFICIENT_OF_EXTINCTION)) {
                        Node textNode = element.getFirstChild();
                        while (null != textNode) {
                            if (textNode.getNodeType() == Node.TEXT_NODE) {
                                m_fCoefficientOfExtinction = Float.parseFloat(textNode.getNodeValue().trim());
                                bParsed = true;
                                break;
                            }
                            textNode = textNode.getNextSibling();
                        }
                    } else if (!bParsed) {
                        throw new DynXmlObjParseException("Unrecognized Hall illumination model element <" +
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

    //-------------------------------------------------------------------------------------------------------------------------
    public void toChildXmlElement(final @NotNull Element parentEl) {
        // the node for the Hall material
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
    public void initSampling(final int nSample, final @NotNull float[] f1dSample, final float[] f1dRandom, final @NotNull Point2f[] pt2dSample, final Point2f[] pt2dRandom,
                             final @NotNull Point3f[] pt3dSample, final Point3f[] pt3dRandom) {
    }

    //-------------------------------------------------------------------------------------------------------------------------
    public void getColor(final @NotNull RGBf rgb, final @NotNull RayIntersection intersection, final @NotNull IRtLight[] lights, final @NotNull IRtGeometry[] rtObjects, final @NotNull IRtBackground rtBkg, final int nMaxRecursions,
                         final int nSample, final int nRandom) {
        rgb.setValue(m_rgbEmissive);
        final LightInfo lightInfo = intersection.borrowLightInfo();
        final Vector3f vL = intersection.borrowVector();
        final Vector3f vH = intersection.borrowVector();
        final Vector3f vR = intersection.borrowVector();
        final Vector3f vT = intersection.borrowVector();
        final Line3f lnRflRfr = intersection.borrowLine();
        final RGBf rgbTemp = intersection.borrowRGB();
        final RGBf rgbRflRfr = intersection.borrowRGB();
        final RGBf rgbFt = intersection.borrowRGB();
        final RayIntersection intRflRfr = intersection.borrowIntersection();
        // loop through the lights and sum the color contribution from each light
        try {
            //----------------------------------------------------------------------------------------------------------------
            // Step 1: add in the contribution of direct light sources
            //
            if (null != lights) {
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
                                            if (m_bConductor) {
                                                rgb.add(m_F.approxFr(rgbTemp, vH, vL).
                                                        mult(lightInfo.m_rgb).scale(fScale));
                                            } else if (vT.setToRefraction(vH, vL, 1.0f, m_fIndexOfRefraction)) {
                                                rgb.add(m_F.approxFrFt(rgbTemp, null, vH, vL, vT, true, 1.0f).
                                                        mult(lightInfo.m_rgb).scale(fScale));
                                            } else {
                                                // this can't really happen unless light is going faster than the speed of light inside the object - complete
                                                //  internalFresnel reflection.
                                                rgb.add(rgbTemp.setValue(lightInfo.m_rgb).scale(fScale));
                                            }
                                        } else {
                                            System.out.println("unexpected negative N.H");
                                        }
                                    } else {
                                        System.out.println("unexpected negative N.L (" + fNdotL + ") on light " + light);
                                    }
                                }
                                break;
                        }
                    }
                }
            }
            //----------------------------------------------------------------------------------------------------------------
            // Step 2: add in the contribution from the reflected direction
            //
            boolean bIntersectObj = false;
            boolean bTransmitted = true;
            float fAveAtten;

            // precompute the contribution from Fresnel
            if (m_bConductor) {
                m_F.approxFr(rgbTemp, intersection.m_vNormal, intersection.m_vToEye).scale(m_fKs);
            } else if (vT.setToRefraction(intersection.m_vNormal, intersection.m_vToEye, 1.0f, m_fIndexOfRefraction)) {
                bTransmitted = true;
                m_F.approxFrFt(rgbTemp, rgbFt, intersection.m_vNormal, intersection.m_vToEye, vT, true, 1.0f).scale(m_fKs);
            } else {
                // this can't really happen unless light is going faster than the speed of light inside the object - complete
                //  internalFresnel reflection.
                rgbTemp.setValue(m_fKs, m_fKs, m_fKs);
            }
            fAveAtten = (rgbTemp.r + rgbTemp.g + rgbTemp.b) / 3.0f;
            // test to see if there are any more recursions left for the material - and get the reflection if there are
            if ((nMaxRecursions > 0) && ((intersection.m_fMaxContribution * fAveAtten) > PackageConstants.CUTOFF_CONTRIBUTION)) {
                vR.setToReflection(intersection.m_vNormal, intersection.m_vToEye);
                if (PackageConstants.VALIDITY_CHECKING && vR.dot(intersection.m_vNormal) <= 0.0f) {
                    System.out.println("cip.raytrace.material.Hall(): unexpected negative R.N");
                }
                // get the reflected vector and do a get colour pass on that
                lnRflRfr.setValue(intersection.m_pt, vR);
                intRflRfr.initialize(vR);
                intRflRfr.m_fMaxContribution = intersection.m_fMaxContribution * fAveAtten;
                for (IRtGeometry rtObject : rtObjects) {
                    if (((intersection.m_rtObj != rtObject) || (!rtObject.isConvex())) &&
                            rtObject.getRayIntersection(intRflRfr, lnRflRfr, false, nSample, nRandom)) {
                        bIntersectObj = true;
                    }
                }
            }
            if (bIntersectObj) {
                // get the intersected object colour
                intRflRfr.m_mtl.getColor(rgbRflRfr, intRflRfr, lights, rtObjects, rtBkg, nMaxRecursions - 1, nSample, nRandom);
            } else {
                // if there was no intersection with the reflection vector, set the colour to the background
                rtBkg.getColor(rgbRflRfr, lnRflRfr, null);
            }
            rgb.add(rgbTemp.mult(rgbRflRfr));

            //----------------------------------------------------------------------------------------------------------------
            // Step 3: add in the contribution from the transmitted direction
            //
            if (bTransmitted && m_bTransparent) {
                bIntersectObj = false;
                rgbFt.scale(m_fKs);
                fAveAtten = (rgbFt.r + rgbFt.g + rgbFt.b) / 3.0f;
                if ((nMaxRecursions > 0) && ((intersection.m_fMaxContribution * fAveAtten) > PackageConstants.CUTOFF_CONTRIBUTION)) {
                    lnRflRfr.setValue(intersection.m_pt, vT);
                    intRflRfr.initialize(vT);
                    intRflRfr.m_fMaxContribution = intersection.m_fMaxContribution * fAveAtten;
                    bIntersectObj = intersection.m_rtObj.getRayIntersection(intRflRfr, lnRflRfr, true, nSample, nRandom);
                }

                if (bIntersectObj) {
                    getInternalColor(rgbRflRfr, intRflRfr, lights, rtObjects, nMaxRecursions, 0, rtBkg, nSample, nRandom);
                } else {
                    // this can't really happen unless we are just done recursing.  Being inside the object here makes
                    //  it difficult do decide what to do.  Adding the full background colour feels too generous, so
                    //  I'm arbitrarily scaling the background colour by 0.5
                    rtBkg.getColor(rgbRflRfr, null, null);
                    rgbRflRfr.scale(0.5f);
                }
                rgb.add(rgbRflRfr.mult(rgbFt));
            }

        } catch (final Throwable t) {
            rgb.setValue(1.0f, 1.0f, 0.0f);
        } finally {
            intersection.returnIntersection(intRflRfr);
            intersection.returnRGB(rgbFt);
            intersection.returnRGB(rgbRflRfr);
            intersection.returnRGB(rgbTemp);
            intersection.returnLine(lnRflRfr);
            intersection.returnVector(vT);
            intersection.returnVector(vR);
            intersection.returnVector(vH);
            intersection.returnVector(vL);
            intersection.returnLightInfo(lightInfo);
        }
    }

    //-------------------------------------------------------------------------------------------------------------------------
    public void getInternalColor(final RGBf rgb, final RayIntersection intersection, final IRtLight[] lights, final IRtGeometry[] rtObjects,
                                 final int nMaxRecursions, final int nInternalReflections, final IRtBackground rtBkg, final int nSample, final int nRandom) {
        // the is the contribution from a refracted ray.  The ray is intersecting the surface from the inside and the
        //  contributions are from:
        //  > direct lighting being transmitted through the object surface from the outside;
        //  > the tramsmitted direction;
        //  > the internally reflected direction.
        final LightInfo lightInfo = intersection.borrowLightInfo();
        final Vector3f vL = intersection.borrowVector();
        final Vector3f vT = intersection.borrowVector();
        final Vector3f vR = intersection.borrowVector();
        final Vector3f vHt = intersection.borrowVector();
        final RGBf rgbFr = intersection.borrowRGB();
        final RGBf rgbFt = intersection.borrowRGB();
        final RGBf rgbRflRfr = intersection.borrowRGB();
        final Line3f lnRflRfr = intersection.borrowLine();
        final RayIntersection intRflRfr = intersection.borrowIntersection();

        rgb.setValue(0.0f, 0.0f, 0.0f);

        try {
            //----------------------------------------------------------------------------------------------------------------
            // Step 1: add in the contribution of direct light sources
            //
            if (null != lights) {
                for (IRtLight light : lights) {
                    if (light.getLight(lightInfo, intersection, nSample, nRandom)) {
                        switch (lightInfo.m_nType) {
                            case LightInfo.AMBIENT:
                                rgb.add(lightInfo.m_rgb.mult(m_rgbDiffuse).scale(m_fKd * (1.0f - m_F.getAveReflectance())));
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
                                        rgb.add(rgbFt.setValue(lightInfo.m_rgb).mult(m_rgbDiffuse).
                                                scale(fNdotL * m_fKd * (1.0f - (m_F.getAveReflectance() * m_fKs))));
                                        // add the specular contribution
                                        if (vHt.setToHt(vL, intersection.m_vToEye, 1.0f, m_fIndexOfRefraction) &&
                                                (vHt.dot(intersection.m_vNormal) > 0.0f)) {
                                            final float fScale = m_D.evaluate(intersection.m_vNormal, vHt, null, vL) * m_fKs;
                                            m_F.approxFrFt(rgbFr, rgbFt, vHt, vL, intersection.m_vToEye, true, 1.0f);
                                            rgb.add(rgbFt.mult(lightInfo.m_rgb).scale(fScale));
//                                        } else {
//                                            System.out.println("unexpected negative N.Ht");
                                        }
                                    } else {
                                        System.out.println("unexpected negative N.L (" + fNdotL + ") on light " + light);
                                    }
                                }
                                break;
                        }
                    }
                }
            }

            //----------------------------------------------------------------------------------------------------------------
            // Step 2: add in the contribution of the transmitted ray (the ray to the outside)
            //
            boolean bIntersectObj = false;
            intersection.m_vNormal.reverse();       // reversed because the Fresnel formulation assumes the normal is
            // faced towards the side the V vector comes from.
            if (vT.setToRefraction(intersection.m_vNormal, intersection.m_vToEye, m_fIndexOfRefraction, 1.0f)) {
                intRflRfr.initialize(vT);
                lnRflRfr.setValue(intersection.m_pt, vT);
                for (IRtGeometry rtObject : rtObjects) {
                    if (((intersection.m_rtObj != rtObject) || (!rtObject.isConvex())) &&
                            rtObject.getRayIntersection(intRflRfr, lnRflRfr, false, nSample, nRandom)) {
                        bIntersectObj = true;
                    }
                }
                if (bIntersectObj) {
                    intRflRfr.m_mtl.getColor(rgbRflRfr, intRflRfr, lights, rtObjects, rtBkg, nMaxRecursions - 1, nSample, nRandom);
                } else {
                    rtBkg.getColor(rgbRflRfr, lnRflRfr, null);
                }
                // The colour from the outside is scaled by the transmittance (the rest is reflected to the outside)
                m_F.approxFrFt(rgbFr, rgbFt, intersection.m_vNormal, intersection.m_vToEye, vT, false, 1.0f);
                rgb.add(rgbFt.mult(rgbRflRfr).scale(m_fKs));
                rgbFr.scale(m_fKs);
            } else {
                rgbFr.setValue(m_fKs, m_fKs, m_fKs);
            }

            //----------------------------------------------------------------------------------------------------------------
            // Step 3: add in the contribution of the reflected ray (the ray internally reflected)
            //
            final float fAveAtten = (rgbFr.r + rgbFr.g + rgbFr.b) / 3.0f;
            if ((nInternalReflections < PackageConstants.MAX_INTERNAL_REFLECTION) &&
                    ((intersection.m_fMaxContribution * fAveAtten) > PackageConstants.CUTOFF_CONTRIBUTION)) {
                // we continue to recurse the internal refraction into the current object
                vR.setToReflection(intersection.m_vNormal, intersection.m_vToEye);
                lnRflRfr.setValue(intersection.m_pt, vR);
                intRflRfr.initialize(vR);
                intRflRfr.m_fMaxContribution = intersection.m_fMaxContribution * fAveAtten;
                if (intersection.m_rtObj.getRayIntersection(intRflRfr, lnRflRfr, true, nSample, nRandom)) {
                    getInternalColor(rgbRflRfr, intRflRfr, lights, rtObjects, nMaxRecursions, nInternalReflections + 1, rtBkg, nSample, nRandom);
                } else {
                    rgbRflRfr.setValue(1.0f, 0.5f, 0.0f);
                }
            } else {
                // this can't really happen unless we are just done recursing.  Being inside the object here makes
                //  it difficult do decide what to do.  Adding the full background colour feels too generous, so
                //  I'm arbitrarily scaling the background colour by 0.5
                rtBkg.getColor(rgbRflRfr, null, null);
                rgbRflRfr.scale(0.5f);
            }
            rgb.add(rgbFr.mult(rgbRflRfr));

        } catch (final Throwable t) {
            rgb.setValue(1.0f, 1.0f, 0.0f);
        } finally {
            intersection.returnIntersection(intRflRfr);
            intersection.returnLine(lnRflRfr);
            intersection.returnRGB(rgbRflRfr);
            intersection.returnRGB(rgbFt);
            intersection.returnRGB(rgbFr);
            intersection.returnVector(vHt);
            intersection.returnVector(vR);
            intersection.returnVector(vT);
            intersection.returnVector(vL);
            intersection.returnLightInfo(lightInfo);
        }
    }
}
