/*
 * Ellipsoid.java
 *
 * Created on November 15, 2002, 11:46 PM
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

package cip.render.raytrace.geometry;


import cip.render.DynXmlObjParseException;
import cip.render.FrameLoader;
import cip.render.IDynXmlObject;
import cip.render.INamedObject;
import cip.render.raytrace.interfaces.IRtMaterial;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.LinkedList;
import java.util.StringTokenizer;

/**
 * This is the implementation of an ellipsoid of some radii centered at 0,0,0 in the object coordinate system.
 * <p>
 * The ellipsoid is specified as a node in an XML file as:
 * <pre>
 *     <font style="color:blue">&lt;<b>DynamicallyLoadedObject</b> class="cip.raytrace.geometry.Ellipsoid" name="<font style="color:magenta"><i>ellipsoidName</i></font>"&gt;</font>
 *         <font style="color:blue">&lt;<b>radius</b>&gt;<font style="color:magenta"><i>radius</i></font>&lt;/<b>radius</b>&gt;</font>
 *         <font style="color:blue">&lt;<b>radius</b>&gt;<font style="color:magenta"><i>Xradius,Yradius,Zradius</i></font>&lt;/<b>radius</b>&gt;</font>
 *         <font style="color:blue">&lt;<b>MaterialByRef</b> name="<font style="color:magenta"><i>materialName</i></font>"/&gt;</font>
 *         <font style="color:blue">&lt;<b>DynamicallyLoadedObject</b> class="<font style="color:magenta"><i>materialClass</i></font>"&gt;</font>
 *               <font style="color:gray"><b>.</b>
 *             <i>material specific nodes and attributes</i>
 *               <b>.</b></font>
 *         <font style="color:blue">&lt;/<b>DynamicallyLoadedObject</b>&gt;</font>
 *     <font style="color:blue">&lt;/<b>DynamicallyLoadedObject</b>&gt;</font>
 * </pre>
 * <table border="0" width="90%">
 * <caption style="text-align:left">where:</caption> <tr>
 * <td style="width:5%"></td>
 * <td><table border="1" summary="">
 * <tr>
 * <td><tt>radius</tt></td>
 * <td>The radii of the ellipsoid.  This is specified either as a single value which is applied to i, j, and k
 * resulting in a sphere (the degenerate case of the ellipsoid); or  as 3 values that will be applied as X radius,
 * Y radius, and Z radius individually.  The default is a ellipsoid with X, Y, and Z radii of 1.0, 2.0, and 3.0 if not specified.
 * </td>
 * </tr>
 * <tr>
 * <td><tt>MaterialByRef</tt></td>
 * <td>The specification fof a material for the ellipsoid.  <tt>MaterialByRef</tt> is
 * mutually exclusive with the <tt>DynamicallyLoadedObject</tt> specification of a material.  The dynamically
 * loaded object must implement the  {@link cip.render.raytrace.interfaces.IRtMaterial} interface.  If no material
 * is specified, the material defaults to matte green material.
 * </td>
 * </tr>
 * <tr>
 * <td><tt>DynamicallyLoadedObject</tt></td>
 * <td>The specification for a material for the ellipsoid.  <tt>MaterialByRef</tt> is
 * mutually exclusive with the <tt>DynamicallyLoadedObject</tt> specification of a material.  The dynamically
 * loaded object must implement the  {@link cip.render.raytrace.interfaces.IRtMaterial} interface.  If no material
 * is specified, the material defaults to matte green material.
 * </td>
 * </tr>
 * </table>
 * </td>
 * </tr>
 * </table><br>
 * <p>
 * <b>Example of XML Specification</b>
 * <p>
 * The following specifies a sphere of radii 5, 3, and 2:<br><br>
 * <pre>
 *     <font style="color:blue">&lt;<b>DynamicallyLoadedObject</b> class="cip.raytrace.geometry.Ellipsoid" name="<font style="color:magenta">ellipsoid</font>"&gt;</font>
 *         <font style="color:blue">&lt;<b>radius</b>&gt;<font style="color:magenta">5,3,2</font>&lt;/<b>radius</b>&gt;</font>
 *         <font style="color:blue">&lt;<b>MaterialByRef</b> name="<font style="color:magenta"><i>blue</i></font>"/&gt;</font>
 *     <font style="color:blue">&lt;/<b>DynamicallyLoadedObject</b>&gt;</font>
 * </pre>
 *
 * @author royster.hall@gmail.com
 * @version 1.0
 * @since 1.0
 */
public class Ellipsoid extends Sphere {

    /**
     * Creates a new instance of Ellipsoid
     */
    public Ellipsoid() {
        super();
        m_quadric.setEllipsoid(1.0f, 2.0f, 3.0f);
        m_strType = m_quadric.getQuadricType();
        m_strName = "ellipsoid";
    }

    public float getRadiusX() {
        return (float) Math.sqrt(1.0f / m_quadric.getQ(1));
    }

    public float getRadiusY() {
        return (float) Math.sqrt(1.0f / m_quadric.getQ(2));
    }

    public float getRadiusZ() {
        return (float) Math.sqrt(1.0f / m_quadric.getQ(3));
    }

    public void setRadius(final float fRx, final float fRy, final float fRz) {
        m_quadric.setEllipsoid(fRx, fRy, fRz);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IDynXmlObject interface implementation                                                                                     //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void loadFromXml(final @NotNull Element xmlElement, final LinkedList<INamedObject> refObjectList)
            throws DynXmlObjParseException {
        try {
            Node domNode = xmlElement.getFirstChild();
            while (null != domNode) {
                if (domNode instanceof Element) {
                    final Element element = (Element) domNode;
                    IRtMaterial mtl;
                    if (element.getTagName().equalsIgnoreCase(XML_TAG_RADIUS)) {
                        Node txtNode = element.getFirstChild();
                        while (null != txtNode) {
                            if (txtNode.getNodeType() == Node.TEXT_NODE) {
                                final StringTokenizer tokens = new StringTokenizer(txtNode.getNodeValue(), ",");
                                if (tokens.countTokens() == 1) {
                                    final float fRx = Float.parseFloat(txtNode.getNodeValue().trim());
                                    setRadius(fRx, fRx, fRx);
                                } else if (tokens.countTokens() == 3) {
                                    final float fRx = Float.parseFloat(tokens.nextToken().trim());
                                    final float fRy = Float.parseFloat(tokens.nextToken().trim());
                                    final float fRz = Float.parseFloat(tokens.nextToken().trim());
                                    setRadius(fRx, fRy, fRz);
                                    m_strType = m_quadric.getQuadricType();
                                } else {
                                    throw new IllegalArgumentException(String.format(
                                            "\"%s\" specification must be in the form \"radius\" or \"Xradius,Yradius,Zradius\"",
                                            XML_TAG_RADIUS));
                                }
                                break;
                            }
                            txtNode = txtNode.getNextSibling();
                        }
                    } else if (null != (mtl = FrameLoader.tryParseMaterial(element, refObjectList, getType(), m_strName))) {
                        m_mtl = mtl;
                    } else {
                        pkgThrowUnrecognizedXml(element);
                    }
                }
                domNode = domNode.getNextSibling();
            }
        } catch (final Throwable t) {
            if (t instanceof DynXmlObjParseException) {
                throw (DynXmlObjParseException) t;
            } else {
                throw new DynXmlObjParseException(getClass().getName() + " parse exception", t);
            }
        }
    }

    protected void internalToXml(final Element element) {
        // The radius
        final Element elRadius = element.getOwnerDocument().createElement(XML_TAG_RADIUS);
        element.appendChild(elRadius);
        if ((getRadiusX() == getRadiusY()) && (getRadiusX() == getRadiusZ())) {
            elRadius.appendChild(element.getOwnerDocument().createTextNode(String.format("%f", getRadiusX())));
        } else {
            elRadius.appendChild(element.getOwnerDocument().createTextNode(String.format("%f,%f,%f",
                    getRadiusX(),getRadiusY(),getRadiusZ())));
        }
        // The material
        if ((m_mtl != DEFAULT_MATERIAL) && (m_mtl instanceof IDynXmlObject)) {
            ((IDynXmlObject) m_mtl).toChildXmlElement(element);
        }
    }
}
