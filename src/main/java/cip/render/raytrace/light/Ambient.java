/*
 * Ambient.java
 *
 * Created on October 5, 2002, 11:29 PM
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
import cip.render.utilColour.RGBf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.LinkedList;

/**
 * A uniform ambient light source.  The ambient light source is uniform global non-directional illumination.
 * <p>
 * The uniform ambient light source is specified as a node in an XML file as:<br><br>
 * <pre>
 *     <font style="color:blue">&lt;<b>DynamicallyLoadedObject</b> class="cip.raytrace.light.Ambient"
 *                              name="<font style="color:magenta"><i>ambientName</i></font>"&gt;</font>
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
 * <td>The ambient intensity as specified by the <tt><i>RGBf_attributes</i></tt> which are described in
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
 * The following specifies low level white ambient:<br><br>
 * <pre>
 *     <font style="color:blue">&lt;<b>DynamicallyLoadedObject</b> class="cip.raytrace.light.Ambient"
 *                              name="<font style="color:magenta">ambient</font>"&gt;</font>
 *         <font style="color:blue">&lt;<b>intensity</b> rgb="<font style="color:magenta">0.1,0.1,0.1</font>"/&gt;</font>
 *     <font style="color:blue">&lt;/<b>DynamicallyLoadedObject</b>&gt;</font>
 * </pre>
 *
 * @author royster.hall@gmail.com
 * @version 1.0
 * @since 1.0
 */
public class Ambient extends ALight {
    private static final String XML_TAG_INTENSITY = "intensity";

    // The instance definition
    private final RGBf m_rgb = new RGBf(0.1f, 0.1f, 0.1f);

    /**
     * Creates a new instance of an <tt>Ambient</tt> light source.
     */
    public Ambient() {
    }

    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Get the intensity of the ambient light.
     *
     * @param rgbIntensity Set to the intensity of the ambient light.
     */
    public final void getIntensity(final RGBf rgbIntensity) {
        rgbIntensity.setValue(m_rgb);
    }

    /**
     * Set the intensity of the ambient light'
     *
     * @param rgbIntensity The ambeint light is set to this intensity.
     */
    public final void setIntensity(final RGBf rgbIntensity) {
        m_rgb.setValue(rgbIntensity);
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
                        throw new DynXmlObjParseException("Unrecognized Ambient light  XML description element <" +
                                element.getTagName() + ">.");
                    }
                }
                domNode = domNode.getNextSibling();
            }
        } catch (final Throwable t) {
            if (t instanceof DynXmlObjParseException) {
                throw (DynXmlObjParseException) t;
            } else {
                throw new DynXmlObjParseException("Ambient light model parse exception", t);
            }
        }
    }

    //-------------------------------------------------------------------------------------------------------------------------
    @Override
    protected void internalToXml(final Element element) {
        // the ambient light intensity
        final Element elRGB = element.getOwnerDocument().createElement(XML_TAG_INTENSITY);
        element.appendChild(elRGB);
        m_rgb.toXmlAttr(elRGB);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IRtLight interface implementation                                                                                     //
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean getLight(final LightInfo lightInfo, final RayIntersection intersection, final int nSample, final int nRandom) {
        lightInfo.m_nType = LightInfo.AMBIENT;
        lightInfo.m_rgb.setValue(m_rgb);
        return true;
    }
}
