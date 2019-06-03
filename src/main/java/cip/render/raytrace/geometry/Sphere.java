/*
 * Sphere.java
 *
 * Created on October 3, 2002, 10:46 AM
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
import cip.render.raytrace.RayIntersection;
import cip.render.raytrace.interfaces.IRtMaterial;
import cip.render.util.AngleF;
import cip.render.util3d.PackageConstants;
import cip.render.util3d.Point3f;
import cip.render.util3d.Vector3f;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.LinkedList;

/**
 * This is the implementation of a sphere of some radius centered at 0,0,0 in the object coordinate system.
 * <p>
 * There are methods to programmatically initialize a sphere, but, it is most common to load the sphere from a
 * scene description file. The sphere is specified as a node in an XML scene description file as:
 * <pre>
 *     <font style="color:blue">&lt;<b>DynamicallyLoadedObject</b> class="cip.raytrace.geometry.Sphere" name="<font style="color:magenta"><i>sphereName</i></font>"&gt;</font>
 *         <font style="color:blue">&lt;<b>radius</b>&gt;<font style="color:magenta"><i>radius</i></font>&lt;/<b>radius</b>&gt;</font>
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
 * <td>The radius of the sphere.  The default radius is 1 if not specified.
 * </td>
 * </tr>
 * <tr>
 * <td><tt>MaterialByRef</tt></td>
 * <td>A material specified by reference to the name of a previously loaded material.  <tt>MaterialByRef</tt> is
 * mutually exclusive with the <tt>DynamicallyLoadedObject</tt> specification of a material.  If no material
 * is specified, the material defaults to matte green material.
 * </td>
 * </tr>
 * <tr>
 * <td><tt>DynamicallyLoadedObject</tt></td>
 * <td>The specification fof a material for the sphere.  <tt>MaterialByRef</tt> is
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
 * The following specifies a sphere of radius 5:<br><br>
 * <pre>
 *     <font style="color:blue">&lt;<b>DynamicallyLoadedObject</b> class="cip.raytrace.geometry.Sphere" name="<font style="color:magenta">sphere</font>"&gt;</font>
 *         <font style="color:blue">&lt;<b>radius</b>&gt;<font style="color:magenta">5</font>&lt;/<b>radius</b>&gt;</font>
 *         <font style="color:blue">&lt;<b>MaterialByRef</b> name="<font style="color:magenta"><i>blue</i></font>"/&gt;</font>
 *     <font style="color:blue">&lt;/<b>DynamicallyLoadedObject</b>&gt;</font>
 * </pre>
 *
 * @author royster.hall@gmail.com
 * @version 1.0
 * @since 1.0
 */
public class Sphere extends AQuadricGeo {
    /**
     * Creates a new instance of <tt>Sphere</tt>.
     */
    public Sphere() {
        super();
        m_quadric.setEllipsoid(1.0f, 1.0f, 1.0f);
        m_strType = m_quadric.getQuadricType();
        m_strName = "sphere";
    }

    //------------------------------------------------------------------------------------------------------------------------------
    // Accessor/Mutator functions
    public float getRadius() {
        return (float) Math.sqrt(1.0f / m_quadric.getQ(1));
    }

    public void setRadius(final float fRadius) {
        m_quadric.setEllipsoid(fRadius, fRadius, fRadius);
        m_strType = m_quadric.getQuadricType();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IDynXmlObject interface implementation                                                                                     //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void loadFromXml(final @NotNull Element xmlElement, final @Nullable LinkedList refObjectList)
            throws DynXmlObjParseException {
        try {
            Node domNode = xmlElement.getFirstChild();
            while (null != domNode) {
                if (domNode instanceof Element) {
                    final Element element = (Element) domNode;
                    IRtMaterial mtl;
                    if (element.getTagName().equalsIgnoreCase(XML_TAG_RADIUS)) {
                        Node textNode = element.getFirstChild();
                        while (null != textNode) {
                            if (textNode.getNodeType() == Node.TEXT_NODE) {
                                setRadius(Float.parseFloat(textNode.getNodeValue().trim()));
                                break;
                            }
                            textNode = textNode.getNextSibling();
                        }
                    } else if (null != (mtl = FrameLoader.tryParseMaterial(element, refObjectList, m_strType, m_strName))) {
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
        elRadius.appendChild(element.getOwnerDocument().createTextNode(Float.toString(getRadius())));
        // The material
        if ((m_mtl != DEFAULT_MATERIAL) && (m_mtl instanceof IDynXmlObject)) {
            ((IDynXmlObject) m_mtl).toChildXmlElement(element);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IRtGeometry interface implementation                                                                                  //
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Returns an array of 24 points that are the vertices of a convex hull described by a min-max box with the corners
     * trimmed by planes that are perpendicular to the diagonals of the 8 quadrants of a 3D axis system.
     */
    public Point3f[] getConvexHullVertices() {
        final float fEdge = (float) (2.0 - Math.sqrt(3.0)) * getRadius();
        final Point3f[] ptHull = new Point3f[24];
        int ii;

        // do the points for one quadrant
        ptHull[0] = new Point3f().setValue(getRadius(), getRadius(), fEdge);
        ptHull[1] = new Point3f().setValue(fEdge, getRadius(), getRadius());
        ptHull[2] = new Point3f().setValue(getRadius(), fEdge, getRadius());
        // flip on i
        for (ii = 0; ii < 3; ii++) {
            ptHull[ii + 3] = new Point3f().setValue(-ptHull[ii].x, ptHull[ii].y, ptHull[ii].z);
        }
        // flip on j
        for (ii = 0; ii < 6; ii++) {
            ptHull[ii + 6] = new Point3f().setValue(ptHull[ii].x, -ptHull[ii].y, ptHull[ii].z);
        }
        // flip on k
        for (ii = 0; ii < 12; ii++) {
            ptHull[ii + 12] = new Point3f().setValue(ptHull[ii].x, ptHull[ii].y, -ptHull[ii].z);
        }
        return ptHull;
    }
    /**
     * The natural mapping for a sphere is U equal to degrees longitude measured counter-clockwise (looking down) starting at
     * the +i axis being 0 degree (+j = 90 degree, -i = 180 degree, and -j = 270 degree).  And V being latitude in degrees
     * starting at -90 degrees for -k, and +90 degree for +k.  The <tt>Sphere</tt> does not compute natural coordinates
     * unless they are specifically requested using this function.
     */
    public void getNaturalCoordinates(final RayIntersection intersection) {
        if (intersection.m_bNatural) return;

        final Vector3f vN = intersection.borrowVector().setValue(intersection.m_vObjNormal);
        // the natural coordinates are longitude and latitude.
        final float xyLen = (float) Math.sqrt((double) ((vN.i * vN.i) + (vN.j * vN.j)));
        if (xyLen < PackageConstants.ZERO_TOLERANCE_MAX_FLOAT) {
            // the normal is essentially straight up, or straight down, the geometric computations fall apart here
            intersection.m_ptNatural.x = 0.0f;
            if (vN.k > 0.0) {   // straight up
                intersection.m_ptNatural.y = 90.0f;
            } else {            // straight down
                intersection.m_ptNatural.y = -90.0f;
            }
            intersection.m_vNatural[0].setValue(0.0f, 0.0f, 0.0f);
            intersection.m_vNatural[1].setValue(0.0f, 0.0f, 0.0f);
        } else {
            final AngleF ang = intersection.borrowAngle();
            intersection.m_ptNatural.x = ang.atan2(vN.j, vN.i).getDegrees();
            intersection.m_ptNatural.y = ang.atan2(vN.k, xyLen).getDegrees();
            intersection.returnAngle(ang);

            intersection.m_vNatural[0].setValue(-vN.j / xyLen, vN.i / xyLen, 0.0f);
            intersection.m_vNatural[1].setValue(-(vN.k * vN.i) / xyLen, -(vN.k * vN.j) / xyLen, xyLen);
        }
        intersection.m_bNatural = true;
        intersection.returnVector(vN);
    }

}
