/*
 * Point.java
 *
 * Created on October 5, 2002, 11:30 PM
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
import cip.render.util3d.PackageConstants;
import cip.render.utilColour.RGBf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.LinkedList;

/**
 * A point light source.  The point light source is a source located in the scene and has a uniform intensity in all directions.
 * <p>
 * The point light source is specified as a node in an XML file as:<br><br>
 * <pre>
 *     <font style="color:blue">&lt;<b>DynamicallyLoadedObject</b> class="cip.raytrace.light.Point"
 *                              name="<font style="color:magenta"><i>pointLightName</i></font>"&gt;</font>
 *         <font style="color:blue">&lt;<b>intensity</b> <font style="color:magenta"><i>RGBf_attributes</i></font>/&gt;</font>
 *     <font style="color:blue">&lt;/<b>DynamicallyLoadedObject</b>&gt;</font>
 * </pre>
 * <table border="0" width="90%">
 * <caption style="text-align:left">where:</caption>
 * <tr>
 * <td style="width:5%"></td>
 * <td><table border="1" summary="">
 * <tr>
 * <td><tt>intensity</tt></td>
 * <td>The intensity of the point light source as specified by the <tt><i>RGBf_attributes</i></tt> which are described in
 * {@link RGBf#setValue(Element, boolean)}<br>
 * </td>
 * </tr>
 * </table>
 * </td>
 * </tr>
 * </table><br>
 * <p>
 * <b>Example of XML Specification</b>
 * <p>
 * The following specifies red point light source:<br><br>
 * <pre>
 *     <font style="color:blue">&lt;<b>DynamicallyLoadedObject</b> class="cip.raytrace.light.Point"
 *                              name="<font style="color:magenta">pointLight</font>"&gt;</font>
 *         <font style="color:blue">&lt;<b>intensity</b> rgb="<font style="color:magenta">1,0,0</font>"/&gt;</font>
 *     <font style="color:blue">&lt;/<b>DynamicallyLoadedObject</b>&gt;</font>
 * </pre>
 *
 * @author royster.hall@gmail.com
 * @version 1.0
 * @since 1.0
 */
public class Point extends ALight {
    private static final String XML_TAG_INTENSITY = "intensity";

    // The instance definition
    private final RGBf m_rgb = new RGBf(1.0f, 1.0f, 1.0f);           // the point light intensity
    private float m_fDimmer = 1.0f;                           // the dimmer value
    private final RGBf m_rgbDimmed = new RGBf(m_rgb);              // the dimmed intensity used in the scene

    /**
     * Creates a new instance of a <tt>Point</tt> light source.
     */
    public Point() {
    }

    //-------------------------------------------------------------------------------------------------------------------------
    public final void getIntensity(final RGBf rgbIntensity) {
        rgbIntensity.setValue(m_rgb);
    }

    public final void setIntensity(final RGBf rgbIntensity) {
        m_rgb.setValue(rgbIntensity);
        m_rgbDimmed.setValue(rgbIntensity).scale(m_fDimmer);
    }

    public final float getDimmer() {
        return m_fDimmer;
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
                        // this is the ambient intesnity
                        m_rgb.setValue(element, false);
                    } else {
                        throw new DynXmlObjParseException("Unrecognized point light  XML description element <" +
                                element.getTagName() + ">.");
                    }
                }
                domNode = domNode.getNextSibling();
            }
        } catch (final Throwable t) {
            if (t instanceof DynXmlObjParseException) {
                throw (DynXmlObjParseException) t;
            } else {
                throw new DynXmlObjParseException("Point light source parse exception", t);
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
        // fill in the light info - remember, the intersection has been transformed into the
        //  coordinate syetem of the light.  The light is at 0,0,0 - the position of the
        //  intersection is the light vector to the intersection.
        lightInfo.m_nType = LightInfo.LOCAL;
        lightInfo.m_rgb.setValue(m_rgbDimmed);
        lightInfo.m_ptFrom.setValue(0.0f, 0.0f, 0.0f);
        lightInfo.m_vDir.setValue(intersection.m_pt.x, intersection.m_pt.y, intersection.m_pt.z);
        lightInfo.m_fDist = lightInfo.m_vDir.getLength();
        lightInfo.m_vDir.scale(1.0f / lightInfo.m_fDist);
        return true;
    }


}
