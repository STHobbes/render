/*
 * Cone.java
 *
 * Created on November 2, 2002, 3:11 PM
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
import cip.render.IDynXmlObject;
import cip.render.INamedObject;
import cip.render.raytrace.RayIntersection;
import cip.render.util.AngleF;
import cip.render.util3d.PackageConstants;
import cip.render.util3d.Point3f;
import cip.render.util3d.Vector3f;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.LinkedList;
import java.util.StringTokenizer;

/**
 * This is an implementation of an elliptical cone whose apex is at 0,0,0 and extends infinitely along the Z axis.
 * <p>
 * The elliptical cone is specified as a node in an XML file as:
 * <pre>
 *     <font style="color:blue">&lt;<b>DynamicallyLoadedObject</b> class="cip.raytrace.geometry.Cone" name="<font style="color:magenta"><i>coneName</i></font>"&gt;</font>
 *         <font style="color:blue">&lt;<b>radius</b>&gt;<font style="color:magenta"><i>radius</i></font>&lt;/<b>radius</b>&gt;</font>
 *         <font style="color:blue">&lt;<b>radius</b>&gt;<font style="color:magenta"><i>Xradius,Yradius</i></font>&lt;/<b>radius</b>&gt;</font>
 *         <font style="color:blue">&lt;<b>height</b>&gt;<font style="color:magenta"><i>height</i></font>&lt;/<b>height</b>&gt;</font>
 *         <font style="color:blue">&lt;<b>MaterialByRef</b> name="<font style="color:magenta"><i>materialName</i></font>"/&gt;</font>
 *         <font style="color:blue">&lt;<b>DynamicallyLoadedObject</b>class="<font style="color:magenta"><i>materialClass</i></font>"&gt;</font>
 *               <font style="color:gray"><b>.</b>
 *             <i>material specific nodes and attributes</i>
 *               <b>.</b></font>
 *         <font style="color:blue">&lt;/<b>DynamicallyLoadedObject</b>&gt;</font>
 *         <font style="color:blue">&lt;<b>face</b> plane="<font style="color:magenta"><i>A,B,C,D</i></font>"/&gt;</font>
 *           <font style="color:gray"><b>.</b>
 *           <b>.</b></font>
 *         <font style="color:blue">&lt;<b>face</b> plane="<font style="color:magenta"><i>A,B,C,D</i></font>"&gt;</font>
 *           <font style="color:blue">&lt;<b>MaterialByRef</b>&gt;<font style="color:magenta"><i>materialName</i></font>&lt;/<b>MaterialByRef</b>&gt;</font>
 *           <font style="color:blue">&lt;<b>DynamicallyLoadedObject</b> class="<font style="color:magenta"><i>materialClass</i></font>"&gt;</font>
 *                 <font style="color:gray"><b>.</b>
 *               <i>material specific nodes and attributes</i>
 *                 <b>.</b></font>
 *           <font style="color:blue">&lt;/<b>DynamicallyLoadedObject</b>&gt;</font>
 *         <font style="color:blue">&lt;/<b>face</b>&gt;</font>
 *     <font style="color:blue">&lt;/<b>DynamicallyLoadedObject</b>&gt;</font>
 * </pre>
 * <table border="0" width="90%">
 * <caption style="text-align:left">where:</caption> <tr>
 * <td style="width:5%"></td>
 * <td><table border="1" summary="">
 * <tr>
 * <td><tt>radius</tt></td>
 * <td>The radii of the cone.  This is specified either as a single value which is applied to i and j
 * resulting in a cone (the special case of the elliptical cone); or as 2 values that will be applied as X radius and
 * Y radius.  The default is a cone of radiii 1,2 if not specified.
 * </td>
 * </tr>
 * <tr>
 * <td><tt>height</tt></td>
 * <td>The Z distance from 0,0,0 (both positive and negative) where the cone has the specified radii.
 * </td>
 * </tr>
 * <tr>
 * <td><tt>MaterialByRef</tt></td>
 * <td>A material specified by reference to the name of a previously loaded material. <tt>MaterialByRef</tt> is
 * mutually exclusive with the <tt>DynamicallyLoadedObject</tt> specification of a material.  If no material
 * is specified, the material defaults to matte green material.
 * </td>
 * </tr>
 * <tr>
 * <td><tt>DynamicallyLoadedObject</tt></td>
 * <td>The specification for a material for the cone.  <tt>MaterialByRef</tt> is
 * mutually exclusive with the <tt>DynamicallyLoadedObject</tt> specification of a material.  The dynamically
 * loaded object must implement the  {@link cip.render.raytrace.interfaces.IRtMaterial} interface.  If no material
 * is specified, the material defaults to matte green material.
 * </td>
 * </tr>
 * <tr>
 * <td><tt>face</tt></td>
 * <td><i>Optional, none for a true cone.</i> The plane equation of a clipping face.  There are as many <tt>face</tt> entries
 * as there are clipping planes on the cone.
 * The plane equation is normalized during object load. Within the face description these elements may optionally appear:
 * <ul>
 *   <li><tt>MaterialByRef</tt> - A material for the face specified by reference to the name of a previously loaded
 *   material.  <tt>MaterialByRef</tt> is mutually exclusive with the <tt>DynamicallyLoadedObject</tt> specification
 *   of a material. </li>
 *   <li><tt>DynamicallyLoadedObject</tt> - The specification for a material for the face.  <tt>MaterialByRef</tt> is
 *   mutually exclusive with the <tt>DynamicallyLoadedObject</tt> specification of a material.  The dynamically
 *   loaded object must implement the  {@link cip.render.raytrace.interfaces.IRtMaterial} interface.</li>
 * </ul>
 *  If no face material is specified, the cone material is used. Face materials are best used with opaque
 *  objects. Specifying different transparent materials for the cone and clipping faces should be avoided unless the
 *  materials are the same except for surface roughness, i.e. smooth glass and frosted (sandblasted) glass.
 * </td>
 * </tr>
 * </table>
 * </td>
 * </tr>
 * </table><br>
 * <p>
 * <b>Example of XML Specification</b>
 * <p>
 * The following specifies a cone with base radii 1, 2 and height 5:
 * <pre>
 *     <font style="color:blue">&lt;<b>DynamicallyLoadedObject</b> class="cip.raytrace.geometry.Cone" name="<font style="color:magenta"><i>cone 1</i></font>"&gt;</font>
 *         <font style="color:blue">&lt;<b>radius</b>&gt;<font style="color:magenta"><i>1.0f, 2.0f</i></font>&lt;/<b>radius</b>&gt;</font>
 *         <font style="color:blue">&lt;<b>height</b>&gt;<font style="color:magenta"><i>5.0f</i></font>&lt;/<b>height</b>&gt;</font>
 *         <font style="color:blue">&lt;<b>MaterialByRef</b> name="<font style="color:magenta"><i>blue</i></font>"/&gt;</font>
 *     <font style="color:blue">&lt;/<b>DynamicallyLoadedObject</b>&gt;</font>
 * </pre>
 *
 * @author rnesius - original student author
 */
public class Cone extends AQuadricGeo {

    private static final String XML_TAG_HEIGHT = "height";

    /**
     * Creates a new instance of <tt>Cone</tt>.
     */
    public Cone() {
        super();
        m_quadric.setEllipticalCone(1.0f, 2.0f, 5.0f);
        m_strType = m_quadric.getQuadricType();
        m_strName = "Cone";
    }

    //------------------------------------------------------------------------------------------------------------------------------
    // Accessor/Mutator functions
    public float getRadiusX() {
        return (float) Math.sqrt(1.0f / m_quadric.getQ(1));
    }

    public float getRadiusY() {
        return (float) Math.sqrt(1.0f / m_quadric.getQ(2));
    }

    public float getHeight() {
        return (float) Math.sqrt(-1.0f / m_quadric.getQ(3));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IDynXmlObject interface implementation                                                                                     //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    boolean pkgParseElement(@NotNull Element element, final LinkedList<INamedObject> refObjectList)
            throws DynXmlObjParseException {
        if (element.getTagName().equalsIgnoreCase(XML_TAG_RADIUS) || element.getTagName().equalsIgnoreCase(XML_TAG_HEIGHT)) {
            float radiusX = getRadiusX();
            float radiusY = getRadiusY();
            float height = getHeight();
            boolean bRefresh = false;
            if (element.getTagName().equalsIgnoreCase(XML_TAG_RADIUS)) {
                Node textNode = element.getFirstChild();
                while (null != textNode) {
                    if (textNode.getNodeType() == Node.TEXT_NODE) {
                        final StringTokenizer tokens = new StringTokenizer(textNode.getNodeValue(), ",");
                        if (tokens.countTokens() == 1) {
                            radiusX = radiusY = Float.parseFloat(textNode.getNodeValue().trim());
                            bRefresh = true;
                        } else if (tokens.countTokens() == 2) {
                            radiusX = Float.parseFloat(tokens.nextToken().trim());
                            radiusY = Float.parseFloat(tokens.nextToken().trim());
                            bRefresh = true;
                        } else {
                            throw new IllegalArgumentException(String.format(
                                    "\"%s\" specification must be in the form \"radius\" or \"Xradius,Yradius\"",
                                    XML_TAG_RADIUS));
                        }
                        break;
                    }
                    textNode = textNode.getNextSibling();
                }
            } else {
                Node textNode = element.getFirstChild();
                while (null != textNode) {
                    if (textNode.getNodeType() == Node.TEXT_NODE) {
                        final StringTokenizer tokens = new StringTokenizer(textNode.getNodeValue(), ",");
                        if (tokens.countTokens() == 1) {
                            height = Float.parseFloat(tokens.nextToken().trim());
                            bRefresh = true;
                        } else {
                            throw new IllegalArgumentException(String.format(
                                    "\"%s\" specification must be in the form \"height\"",
                                    XML_TAG_HEIGHT));
                        }
                        break;
                    }
                    textNode = textNode.getNextSibling();
                }
            }
            if (bRefresh) {
                m_quadric.setEllipticalCone(radiusX, radiusY, height);
                m_strType = m_quadric.getQuadricType();
            }
            return true;
        }
        return super.pkgParseElement(element, refObjectList);
    }

    @Override
    protected void pkgToXml(@NotNull final Element element) {
        // The radii
        final Element elRadius = element.getOwnerDocument().createElement(XML_TAG_RADIUS);
        element.appendChild(elRadius);
        if (getRadiusX() == getRadiusY()) {
            elRadius.appendChild(element.getOwnerDocument().createTextNode(String.format("%f", getRadiusX())));
        } else {
            elRadius.appendChild(element.getOwnerDocument().createTextNode(String.format("%f,%f",
                    getRadiusX(), getRadiusY())));
        }
        // The Height
        final Element elHeight = element.getOwnerDocument().createElement(XML_TAG_HEIGHT);
        element.appendChild(elHeight);
        elHeight.appendChild(element.getOwnerDocument().createTextNode(Float.toString(getHeight())));
        // The material
        if ((m_mtl != DEFAULT_MATERIAL) && (m_mtl instanceof IDynXmlObject)) {
            ((IDynXmlObject) m_mtl).toChildXmlElement(element);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IRtGeometry interface implementation                                                                                       //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns an array of 24 points that are the vertices of a convex hull described by a min-max box with the corners
     * trimmed by planes that are perpendicular to the diagonals of the 8 quadrants of a 3D axis system.
     */
    @Override
    public Point3f[] getConvexHullVertices() {
        // yeah, we need to sort this out - the cylinder is infinite extent, so until it is clipped, the bounds are infinite...

//        final float fEdge = (float) (2.0 - Math.sqrt(3.0)) * getRadius();
//        final Point3f[] ptHull = new Point3f[24];
//        int ii;
//
//        // do the points for one quadrant
//        ptHull[0] = new Point3f().setValue(getRadius(), getRadius(), fEdge);
//        ptHull[1] = new Point3f().setValue(fEdge, getRadius(), getRadius());
//        ptHull[2] = new Point3f().setValue(getRadius(), fEdge, getRadius());
//        // flip on i
//        for (ii = 0; ii < 3; ii++) {
//            ptHull[ii + 3] = new Point3f().setValue(-ptHull[ii].x, ptHull[ii].y, ptHull[ii].z);
//        }
//        // flip on j
//        for (ii = 0; ii < 6; ii++) {
//            ptHull[ii + 6] = new Point3f().setValue(ptHull[ii].x, -ptHull[ii].y, ptHull[ii].z);
//        }
//        // flip on k
//        for (ii = 0; ii < 12; ii++) {
//            ptHull[ii + 12] = new Point3f().setValue(ptHull[ii].x, ptHull[ii].y, -ptHull[ii].z);
//        }
//        return ptHull;
        return null;
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
