/*
 * Outlined.java
 *
 * Created on November 11, 2002, 1:01 AM
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
package cip.render.raytrace.material.texture;

import cip.render.DynXmlObjParseException;
import cip.render.INamedObject;
import cip.render.raytrace.RayIntersection;
import cip.render.raytrace.interfaces.IRtBackground;
import cip.render.raytrace.interfaces.IRtGeometry;
import cip.render.raytrace.interfaces.IRtLight;
import cip.render.utilColour.RGBf;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Maps every unit square (in 2D) or cube (in 3D) into either an outline material or an infill material.  Within each
 * mapped square or cube, the textures coordinates are normalized to be in the 0,0,0 to 1,1,1 range.
 * <p>
 * The outlined mapping is specified as:<br><br>
 * <pre>
 *     <font style="color:blue">&lt;<b>DynamicallyLoadedObject</b> class="cip.raytrace.material.texture.Checkerboard"
 *                              name="<font style="color:magenta"><i>myCheckerboard</i></font>"&gt;</font>
 *         <font style="color:blue">&lt;<b>width</b>&gt;<font style="color:magenta"><i>outlineWidth</i></font>&lt;/<b>width</b>&gt;</font>
 *         <font style="color:blue">&lt;<b>width</b>&gt;<font style="color:magenta"><i>outlineWidthX,outlineWidthY,outlineWidthZ</i></font>&lt;/<b>width</b>&gt;</font>
 *         <font style="color:blue">&lt;<b>outlineMtl</b>&gt;</font>
 *             <font style="color:blue">&lt;<b>MaterialByRef</b> name="<font style="color:magenta"><i>materialName</i></font>"/&gt;</font>
 *             <font style="color:blue">&lt;<b>DynamicallyLoadedObject</b> class="<font style="color:magenta"><i>materialClass</i></font>"&gt;</font>
 *                   <font style="color:gray"><b>.</b>
 *                 <i>material specific nodes and attributes</i>
 *                   <b>.</b></font>
 *             <font style="color:blue">&lt;/<b>DynamicallyLoadedObject</b>&gt;</font>
 *         <font style="color:blue">&lt;/<b>outlineMtl</b>&gt;</font>
 *         <font style="color:blue">&lt;<b>infillMtl</b>&gt;</font>
 *             <font style="color:blue">&lt;<b>MaterialByRef</b> name="<font style="color:magenta"><i>materialName</i></font>"/&gt;</font>
 *             <font style="color:blue">&lt;<b>DynamicallyLoadedObject</b> class="<font style="color:magenta"><i>materialClass</i></font>"&gt;</font>
 *                   <font style="color:gray"><b>.</b>
 *                 <i>material specific nodes and attributes</i>
 *                   <b>.</b></font>
 *             <font style="color:blue">&lt;/<b>DynamicallyLoadedObject</b>&gt;</font>
 *         <font style="color:blue">&lt;/<b>infillMtl</b>&gt;</font>
 *     <font style="color:blue">&lt;/<b>DynamicallyLoadedObject</b>&gt;</font>
 * </pre>
 * <table border="0" width="90%">
 * <caption style="text-align:left">where:</caption>
 * <tr>
 * <td style="width:5%"></td>
 * <td><table border="1" summary="">
 * <tr>
 * <td><tt>width</tt></td>
 * <td>The width of the outline.  This is specified either as a single value which is applied to i, j, and k; or
 * as 3 values that will be applied to i, j, and k individually. The default width in i, j, and i is 0.1f if not specified.
 * </td>
 * </tr>
 * <tr>
 * <td><tt>outlineMtl</tt></td>
 * <td>The outline material.
 * </td>
 * </tr>
 * <tr>
 * <td><tt>infillMtl</tt></td>
 * <td>The in-fill material.
 * </td>
 * </tr>
 * <tr>
 * <td><tt>MaterialByRef</tt></td>
 * <td>A material specified by reference to the name of a previously loaded material.  <tt>MaterialByRef</tt> is
 * mutually exclusive with the <tt>DynamicallyLoadedObject</tt> specification of a material.  If no material
 * is specified the mapping will generate an exception during rendering.
 * </td>
 * </tr>
 * <tr>
 * <td><tt>DynamicallyLoadedObject</tt></td>
 * <td>The specification for one of the materials of the outline texture.  <tt>MaterialByRef</tt> is
 * mutually exclusive with the <tt>DynamicallyLoadedObject</tt> specification of a material.  The dynamically
 * loaded object must implement the  {@link cip.render.raytrace.interfaces.IRtMaterial} interface.  If no material
 * is specified the mapping will generate an exception during rendering.
 * </td>
 * </tr>
 * </table>
 * </td>
 * </tr>
 * </table><br>
 *
 * @author royster.hall@gmail.com
 * @version 1.0
 * @since 1.0
 */
public class Outlined extends ADualMaterialTexture {
    private static final Logger logger = Logger.getLogger(Outlined.class.getName());
    private static boolean loggingFinest = logger.isLoggable(Level.FINEST);

    private static final String XML_TAG_WIDTH = "width";

    // The instance definition
    private float m_fOutlineWidthX = 0.1f;
    private float m_fOutlineWidthY = 0.1f;
    private float m_fOutlineWidthZ = 0.1f;


    /**
     * Creates a new instance of <tt>Outlined </tt>
     */
    public Outlined() {
        super("outlineMtl", "infillMtl");
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IDynXmlObject interface implementation                                                                                //
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected boolean lclProcessXmlElement(final @NotNull String elementTag, final @NotNull Element xmlElement,
                                           final LinkedList<INamedObject> refObjectList) throws DynXmlObjParseException {
        return super.lclProcessXmlElement(elementTag, xmlElement, refObjectList) ||
                lclParseWidth(elementTag, xmlElement);
    }

    private boolean lclParseWidth(final @NotNull String elementTag,
                                  final @NotNull Element xmlElement) throws DynXmlObjParseException {
        if (elementTag.equals(XML_TAG_WIDTH)) {
            Node txtNode = xmlElement.getFirstChild();
            while (null != txtNode) {
                if (txtNode.getNodeType() == Node.TEXT_NODE) {
                    final StringTokenizer tokens = new StringTokenizer(txtNode.getNodeValue(), ",");
                    if (tokens.countTokens() == 1) {
                        if ((m_fOutlineWidthX = Float.parseFloat(txtNode.getNodeValue().trim())) < 0.01f) {
                            m_fOutlineWidthX = 0.01f;
                        } else if (m_fOutlineWidthX > 0.49f) {
                            m_fOutlineWidthX = 0.49f;
                        }
                        m_fOutlineWidthZ = m_fOutlineWidthY = m_fOutlineWidthX;
                    } else if (tokens.countTokens() == 3) {
                        if ((m_fOutlineWidthX = Float.parseFloat(tokens.nextToken().trim())) > 0.49f) {
                            m_fOutlineWidthX = 0.49f;
                        }
                        if ((m_fOutlineWidthY = Float.parseFloat(tokens.nextToken().trim())) > 0.49f) {
                            m_fOutlineWidthY = 0.49f;
                        }
                        if ((m_fOutlineWidthZ = Float.parseFloat(tokens.nextToken().trim())) > 0.49f) {
                            m_fOutlineWidthZ = 0.49f;
                        }
                    } else {
                        throw new DynXmlObjParseException(
                                "'width' specification must be in the form 'width' or 'Xwidth,Ywidth,Zwidth'");
                    }
                    break;
                }
                txtNode = txtNode.getNextSibling();
            }
            return true;
        }
        return false;
    }

    protected void lclAppendChildElements(final @NotNull Element element) {
        super.lclAppendChildElements(element);
        // outline width
        final Element elWidth = element.getOwnerDocument().createElement(XML_TAG_WIDTH);
        element.appendChild(elWidth);
        if ((m_fOutlineWidthX == m_fOutlineWidthY) && (m_fOutlineWidthX == m_fOutlineWidthZ)) {
            elWidth.appendChild(element.getOwnerDocument().createTextNode(Float.toString(m_fOutlineWidthX)));
        } else {
            elWidth.appendChild(element.getOwnerDocument().createTextNode(m_fOutlineWidthX +
                    "," + m_fOutlineWidthY + "," + m_fOutlineWidthZ));
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IRtMaterial interface implementation                                                                                  //
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void getColor(final @NotNull RGBf rgb, final @NotNull RayIntersection intersection,
                         final @NotNull IRtLight[] lights, final @NotNull IRtGeometry[] rtObjects,
                         final @NotNull IRtBackground rtBkg, final int nMaxRecursions, final int nSample, final int nRandom) {
        if (!intersection.m_bTexture) {
            throw new IllegalStateException("Outlined: texture coordinates have not been set");
        }
        // the checkerboard is really simple, we just take every 1 unit square and check
        //  which quadrant of the square we are in and assign a material
        intersection.m_ptTexture.x -= (float) Math.floor(intersection.m_ptTexture.x);
        intersection.m_ptTexture.y -= (float) Math.floor(intersection.m_ptTexture.y);
        final boolean is_2d = Float.isNaN(intersection.m_ptTexture.z);
        if (!is_2d) {
            intersection.m_ptTexture.z -= (float) Math.floor(intersection.m_ptTexture.z);
        }
        intersection.m_mtl = m_mtl2;

        // now check for the outline material
        if ((intersection.m_ptTexture.x < m_fOutlineWidthX) ||
                (intersection.m_ptTexture.x > (1.0f - m_fOutlineWidthX)) ||
                (intersection.m_ptTexture.y < m_fOutlineWidthY) ||
                (intersection.m_ptTexture.y > (1.0f - m_fOutlineWidthY)) ||
                (!is_2d && (intersection.m_ptTexture.z < m_fOutlineWidthZ) ||
                        (intersection.m_ptTexture.z > (1.0f - m_fOutlineWidthZ)))) {
            if (loggingFinest) {
                logger.finest(String.format("%f, %f mapped to outline material",
                        intersection.m_ptTexture.x, intersection.m_ptTexture.y));
            }
            intersection.m_mtl = m_mtl1;
        } else {
            if (loggingFinest) {
                logger.finest(String.format("%f, %f mapped to infill material",
                        intersection.m_ptTexture.x, intersection.m_ptTexture.y));
            }
        }

        intersection.m_mtl.getColor(rgb, intersection, lights, rtObjects, rtBkg, nMaxRecursions, nSample, nRandom);
    }
}
