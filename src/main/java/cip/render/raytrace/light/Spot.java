/*
 * Spot.java
 *
 * Created on October 8, 2002, 7:37 PM
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
package cip.render.raytrace.light;

import cip.render.DynXmlObjParseException;
import cip.render.raytrace.LightInfo;
import cip.render.raytrace.RayIntersection;
import cip.render.util.AngleF;
import cip.render.util3d.PackageConstants;
import cip.render.utilColour.RGBf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.LinkedList;

/**
 * A focused spotlight source.  The spotlight source is a source located in the scene and is focused along the -Y axis.
 * <p>
 * The spotlight source is specified as a node in an XML file as:<br><br>
 * <pre>
 *     <font style="color:blue">&lt;<b>DynamicallyLoadedObject</b> class="cip.raytrace.light.Spot"
 *                              name="<font style="color:magenta"><i>pointLightName</i></font>"&gt;</font>
 *         <font style="color:blue">&lt;<b>intensity</b> <font style="color:magenta"><i>RGBf_attributes</i></font>/&gt;</font>
 *         <font style="color:blue">&lt;<b>beta</b>&gt;<font style="color:magenta"><i>betaDegrees</i></font>&lt;/<b>beta</b>&gt;</font>
 *     <font style="color:blue">&lt;/<b>DynamicallyLoadedObject</b>&gt;</font>
 * </pre>
 * <table border="0" width="90%">
 * <caption style="text-align:left">where:</caption>
 * <tr>
 * <td style="width:5%"></td>
 * <td><table border="1" summary="">
 * <tr>
 * <td><tt>intensity</tt></td>
 * <td>The intensity of the spotlight source as specified by the <tt><i>RGBf_attributes</i></tt> which are described in
 * {@link RGBf#setValue(Element, boolean)}<br>
 * </td>
 * </tr>
 * <tr>
 * <td><tt>beta</tt></td>
 * <td>The <i>beta</i> angle, or angle of deviation between <b>L</b> and the light source axis when the intensity
 * is half the value as when <b>L =</b> the light source axis.  Valid values are 2 degree to 85 degree.
 * The default is 10 degree if not specified.
 * </td>
 * </tr>
 * </table>
 * </td>
 * </tr>
 * </table><br>
 * <p>
 * <b>Example of XML Specification</b>
 * <p>
 * The following specifies blue spotlight source:<br><br>
 * <pre>
 *     <font style="color:blue">&lt;<b>DynamicallyLoadedObject</b> class="cip.raytrace.light.Spot"
 *                              name="<font style="color:magenta">spotLight</font>"&gt;</font>
 *         <font style="color:blue">&lt;<b>intensity</b> rgb="<font style="color:magenta">0,0,1</font>"/&gt;</font>
 *         <font style="color:blue">&lt;<b>beta</b>&gt;<font style="color:magenta">10</font>&lt;/<b>beta</b>&gt;</font>
 *     <font style="color:blue">&lt;/<b>DynamicallyLoadedObject</b>&gt;</font>
 * </pre>
 *
 * @author royster.hall@gmail.com
 * @version 1.0
 * @since 1.0
 */
public class Spot extends ALight {

    private static final String XML_TAG_INTENSITY = "intensity";
    private static final String XML_TAG_BETA = "beta";

    // The instance definition
    // base specification
    private final RGBf m_rgb = new RGBf();                             // the specified intensity
    private final AngleF m_aBeta = new AngleF(AngleF.DEGREES, 10.0f);    // the angle at which intensity drops to half
    private float m_fDimmer = 1.0f;                               // the dimmer value
    // derived values
    private double m_dExp = 5.0;
    private final RGBf m_rgbDimmed = new RGBf();   // the intensity after dimming

    /**
     * Creates a new instance of a <tt>Spot</tt> light source.
     */
    public Spot() {
    }

    protected void initForRender() {
        if (m_aBeta.getDegrees() >= 85.0f) {
            m_dExp = 0.0f;
        } else {
            double dAngle = (double) m_aBeta.getDegrees();
            if (dAngle < 2.0) dAngle = 2.0;
            m_dExp = -(Math.log(2.0) / Math.log(Math.cos(dAngle * AngleF.DEGREES_TO_RADIANS)));
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IDynXmlObject interface implementation                                                                                //
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void loadFromXml(final @NotNull Element xmlElement, final @Nullable LinkedList refObjectList) throws DynXmlObjParseException {
        try {
            // Read the specified components for the material
            Node domNode = xmlElement.getFirstChild();
            while (null != domNode) {
                if (domNode instanceof Element) {
                    final Element element = (Element) domNode;
                    if (element.getTagName().equalsIgnoreCase(XML_TAG_INTENSITY)) {
                        // this is the intensity.  it will be specified either as RGB values
                        //  or a spectral curve
                        m_rgb.setValue(element, true);
                        m_rgbDimmed.setValue(m_rgb);
                    } else if (element.getTagName().equalsIgnoreCase(XML_TAG_BETA)) {
                        Node elBeta = element.getFirstChild();
                        while (null != elBeta) {
                            if (elBeta.getNodeType() == Node.TEXT_NODE) {
                                m_aBeta.setDegrees(Float.parseFloat(elBeta.getNodeValue().trim()));
                                break;
                            }
                            elBeta = elBeta.getNextSibling();
                        }
                    } else {
                        throw new DynXmlObjParseException("Unrecognized Spot Light element <" +
                                element.getTagName() + ">, terminating model parsing.");
                    }
                }
                domNode = domNode.getNextSibling();
            }
            initForRender();
        } catch (final Throwable t) {
            if (t instanceof DynXmlObjParseException) {
                throw (DynXmlObjParseException) t;
            } else {
                throw new DynXmlObjParseException("Spot Light parse exception", t);
            }
        }
    }

    //-------------------------------------------------------------------------------------------------------------------------
    @Override
    protected void internalToXml(final Element element) {
        // the point light intensity
        final Element elRGB = element.getOwnerDocument().createElement(XML_TAG_INTENSITY);
        element.appendChild(elRGB);
        m_rgb.toXmlAttr(elRGB);
        // beta
        final Element elBeta = element.getOwnerDocument().createElement(XML_TAG_BETA);
        element.appendChild(elBeta);
        elBeta.appendChild(element.getOwnerDocument().createTextNode(Float.toString(m_aBeta.getDegrees())));
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IRtLight interface implementation                                                                                     //
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void setDimmer(final float fDimmer) {
        m_fDimmer = fDimmer;
        m_rgbDimmed.setValue(m_rgb).scale(m_fDimmer);
    }

    //-------------------------------------------------------------------------------------------------------------------------
    @Override
    public boolean getLight(final LightInfo lightInfo, final RayIntersection intersection, final int nSample, final int nRandom) {
        // check whether the light is behind the intersection surface
        final float fPerpDist = -((intersection.m_vNormal.i * intersection.m_pt.x) +
                (intersection.m_vNormal.j * intersection.m_pt.y) +
                (intersection.m_vNormal.k * intersection.m_pt.z));
        if (fPerpDist < PackageConstants.ZERO_TOLERANCE_MAX_FLOAT) return false;
        // check the intersection is in the half-space illuminated by the spot
        if (intersection.m_pt.y > (-PackageConstants.ZERO_TOLERANCE_MAX_FLOAT)) return false;
        // check surface faces the light
        if (intersection.m_vNormal.j <= 0.0f) return false;
        // fill in the light info - remember, the intersection has been transformed into the
        //  coordinate syetem of the light.  The light is at 0,0,0 - the position of the
        //  intersection is the light vector to the intersection
        lightInfo.m_nType = LightInfo.LOCAL;
        lightInfo.m_ptFrom.setValue(0.0f, 0.0f, 0.0f);
        lightInfo.m_vDir.setValue(intersection.m_pt.x, intersection.m_pt.y, intersection.m_pt.z);
        lightInfo.m_fDist = lightInfo.m_vDir.getLength();
        lightInfo.m_vDir.scale(1.0f / lightInfo.m_fDist);
        lightInfo.m_rgb.setValue(m_rgbDimmed).scale((float) (Math.pow((double) (-lightInfo.m_vDir.j), m_dExp)));
        lightInfo.m_fPerpDist = fPerpDist;
        return true;
    }

}
