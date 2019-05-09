/*
 * ConstantColour.java
 *
 * Created on October 11, 2002, 12:08 AM
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
package cip.render.raytrace.background;

import cip.render.IDynXmlObject;
import cip.render.INamedObject;
import cip.render.raytrace.interfaces.IRtBackground;
import cip.render.DynXmlObjLoader;
import cip.render.DynXmlObjParseException;
import cip.render.util.AngleF;
import cip.render.util3d.Line3f;
import cip.render.utilColour.RGBf;

import java.util.LinkedList;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * A constant colour background - the same colour in all directions.
 * <p>
 * The constant colour background is specified as a node in an XML file as:
 * <pre>
 *     <font style="color:blue">&lt;<b>DynamicallyLoadedObject</b> class="cip.raytrace.background.ConstantColour" name="<font style="color:magenta"><i>backgroundName</i></font>"&gt;</font>
 *       <font style="color:blue">&lt;<b>colour</b> <font style="color:magenta"><i>RGBf_attributes</i></font>/&gt;</font>
 *     <font style="color:blue">&lt;/<b>DynamicallyLoadedObject</b>&gt;</font>
 * </pre>
 * <table style="width:90%">
 * <caption style="text-align:left">where:</caption>
 * <tr>
 * <td style="width:5%"></td>
 * <td><table border="1" summary="">
 * <tr>
 * <td><tt>colour</tt> or <tt>color</tt></td>
 * <td>The colour as specified by the <tt><i>RGBf_attributes</i></tt> which are described in
 * {@link RGBf#setValue(Element, boolean)}
 * </td>
 * </tr>
 * </table>
 * </td>
 * </tr>
 * </table><br>
 * <p>
 * <b>Example of XML Specification</b>
 * <p>
 * The following specifies a gray constant colour background:
 * <pre>
 *     <font style="color:blue">&lt;<b>DynamicallyLoadedObject</b> class="cip.raytrace.background.ConstantColour" name="<font style="color:magenta">background</font>"&gt;</font>
 *       <font style="color:blue">&lt;<b>colour</b> rgb="<font style="color:magenta">0.25,0.25,0.25</font>"/&gt;</font>
 *     <font style="color:blue">&lt;/<b>DynamicallyLoadedObject</b>&gt;</font>
 * </pre>
 *
 * @author royster.hall@gmail.com
 * @version 1.0
 * @since 1.0
 */
public class ConstantColour implements IDynXmlObject, INamedObject, IRtBackground {

    private static final String DEFAULT_NAME = "<unspecified>";
    private static final String XML_TAG_COLOR = "color";
    private static final String XML_TAG_COLOUR = "colour";

    // The instance definition
    private String m_strName = DEFAULT_NAME;
    private final RGBf m_rgb = new RGBf(0.1f, 0.1f, 0.1f);

    /**
     * Creates a new instance of a <tt>ConstantColour</tt> background.
     */
    public ConstantColour() {
    }

    /**
     * Creates a new instance of a <tt>ConstantColour</tt> background initialized to a color.
     *
     * @param rgb (RGBf, readonly) The background colour.
     */
    public ConstantColour(final RGBf rgb) {
        m_rgb.setValue(rgb);
    }

    //-------------------------------------------------------------------------------------------------------------------------
    public final void getColour(final RGBf rgbIntensity) {
        rgbIntensity.setValue(m_rgb);
    }

    public final void setColour(final RGBf rgbIntensity) {
        m_rgb.setValue(rgbIntensity);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IDynXmlObject interface implementation                                                                                //
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void loadFromXml(final @NotNull Element xmlElement, final @Nullable LinkedList refObjectList)
            throws DynXmlObjParseException {
        try {
            // Read the specified components for the material
            Node domNode = xmlElement.getFirstChild();
            while (null != domNode) {
                if (domNode instanceof Element) {
                    final Element element = (Element) domNode;
                    if ((element.getTagName().equalsIgnoreCase(XML_TAG_COLOR)) ||
                            (element.getTagName().equalsIgnoreCase(XML_TAG_COLOUR))) {
                        // this is the background colour
                        m_rgb.setValue(element, false);
                    } else {
                        throw new DynXmlObjParseException("Unrecognized ConstantColour background XML description element <" +
                                element.getTagName() + ">.");
                    }
                }
                domNode = domNode.getNextSibling();
            }
        } catch (final Throwable t) {
            if (t instanceof DynXmlObjParseException) {
                throw (DynXmlObjParseException) t;
            } else {
                throw new DynXmlObjParseException("ConstantColour background parse exception", t);
            }
        }
    }

    //-------------------------------------------------------------------------------------------------------------------------
    public void toChildXmlElement(final @NotNull Element parentEl) {
        // the node for the background color
        final Element element = parentEl.getOwnerDocument().createElement(DynXmlObjLoader.XML_TAG);
        parentEl.appendChild(element);
        element.setAttribute(DynXmlObjLoader.XML_ATTR_CLASS, this.getClass().getName());
        element.setAttribute(DynXmlObjLoader.XML_ATTR_NAME, m_strName);
        // the background color
        final Element elRGB = parentEl.getOwnerDocument().createElement(XML_TAG_COLOUR);
        element.appendChild(elRGB);
        m_rgb.toXmlAttr(elRGB);
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
    // IRtBackground interface implementation                                                                                //
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void getColor(final RGBf rgb, final Line3f ray, final AngleF aSample) {
        rgb.setValue(m_rgb);
    }

}
