/*
 * Paraboloid.java
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
import cip.render.raytrace.RayIntersection;
import cip.render.raytrace.interfaces.IRtMaterial;
import cip.render.util3d.Point3f;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.LinkedList;
import java.util.StringTokenizer;

/**
 * This is the implementation of an elliptical paraboloid with tip at 0,0,0 in the object coordinate system.
 * The paraboloid opens upward (toward +k), and is specified by 3 scalars: 'height' is the height of the object
 * from tip to "cup rim", 'Xradius' is the width of the cup opening along the i axis, and 'Yradius' is the
 * width of the cup opening along the j axis. NOTE: the paraboloid is of infinite extent unless clipping is specified.
 * <p>
 * The paraboloid is specified as a node in an XML file as:
 * <pre>
 *     <font style="color:blue">&lt;<b>DynamicallyLoadedObject</b> class="cip.raytrace.geometry.Paraboloid" name="<font style="color:magenta"><i>coneName</i></font>"&gt;</font>
 *         <font style="color:blue">&lt;<b>radius</b>&gt;<font style="color:magenta"><i>radius</i></font>&lt;/<b>radius</b>&gt;</font>
 *         <font style="color:blue">&lt;<b>radius</b>&gt;<font style="color:magenta"><i>Xradius,Yradius</i></font>&lt;/<b>radius</b>&gt;</font>
 *         <font style="color:blue">&lt;<b>height</b>&gt;<font style="color:magenta"><i>height</i></font>&lt;/<b>height</b>&gt;</font>
 *         <font style="color:blue">&lt;<b>MaterialByRef</b> name="<font style="color:magenta"><i>materialName</i></font>"/&gt;</font>
 *         <font style="color:blue">&lt;<b>DynamicallyLoadedObject</b>class="<font style="color:magenta"><i>materialClass</i></font>"&gt;</font>
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
 * <td>The radii of the paraboloid. This is specified either as a single value which is applied to i and j
 * resulting in a paraboloid (the special case of the elliptical paraboloid); or as 2 values that will be applied as X radius and
 * Y radius.  The default is a paraboloid of radii 1,2 if not specified.
 * </td>
 * </tr>
 * <tr>
 * <td><tt>height</tt></td>
 * <td>The positive Z distance from 0,0,0 where the paraboloid has the specified radii.
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
 * <td>The specification fof a material for the paraboloid.  <tt>MaterialByRef</tt> is
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
 * The following specifies a paraboloid with base radii 1, 2 and height 5:
 * <pre>
 *     <font style="color:blue">&lt;<b>DynamicallyLoadedObject</b> class="cip.raytrace.geometry.Paraboloid" name="<font style="color:magenta"><i>paraboloid 1</i></font>"&gt;</font>
 *         <font style="color:blue">&lt;<b>radius</b>&gt;<font style="color:magenta"><i>1.0f, 2.0f</i></font>&lt;/<b>radius</b>&gt;</font>
 *         <font style="color:blue">&lt;<b>height</b>&gt;<font style="color:magenta"><i>5.0f</i></font>&lt;/<b>height</b>&gt;</font>
 *         <font style="color:blue">&lt;<b>MaterialByRef</b> name="<font style="color:magenta"><i>blue</i></font>"/&gt;</font>
 *     <font style="color:blue">&lt;/<b>DynamicallyLoadedObject</b>&gt;</font>
 * </pre>
 *
 * @author sjtitus@alumni.duke.edu - original student author
 * @version 1.0
 * @since 1.0
 */
public class Paraboloid extends AQuadricGeo {
    private static final String XML_TAG_HEIGHT = "height";

    public Paraboloid() {
        super();
        m_quadric.setEllipticalParaboloid(1.0f, 2.0f, 5.0f);
        m_strType = m_quadric.getQuadricType();
        m_strName = "Parabaloid";
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
        return -1.0f / m_quadric.getQ(3);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IDynXmlObject interface implementation                                                                                     //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void loadFromXml(final @NotNull Element xmlElement, final LinkedList<INamedObject> refObjectList)
            throws DynXmlObjParseException {
        try {
            Node domNode = xmlElement.getFirstChild();
            float radiusX = getRadiusX();
            float radiusY = getRadiusY();
            float height = getHeight();
            boolean bRefresh = false;
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
                                    radiusX = radiusY = Float.parseFloat(txtNode.getNodeValue().trim());
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
                            txtNode = txtNode.getNextSibling();
                        }
                    } else if (element.getTagName().equalsIgnoreCase(XML_TAG_HEIGHT)) {
                        Node txtNode = element.getFirstChild();
                        while (null != txtNode) {
                            if (txtNode.getNodeType() == Node.TEXT_NODE) {
                                final StringTokenizer tokens = new StringTokenizer(txtNode.getNodeValue(), ",");
                                height = Float.parseFloat(tokens.nextToken().trim());
                                bRefresh = true;
                            } else {
                                throw new IllegalArgumentException(String.format(
                                        "\"%s\" specification must be in the form \"height\"",
                                        XML_TAG_HEIGHT));
                            }
                            break;
                        }
                    } else if (null != (mtl = FrameLoader.tryParseMaterial(element, refObjectList, getType(), m_strName))) {
                        m_mtl = mtl;
                    } else {
                        pkgThrowUnrecognizedXml(element);
                    }
                }
                domNode = domNode.getNextSibling();
            }
            if (bRefresh) {
                m_quadric.setEllipticalParaboloid(radiusX, radiusY, height);
                m_strType = m_quadric.getQuadricType();
            }
        } catch (final Throwable t) {
            if (t instanceof DynXmlObjParseException) {
                throw (DynXmlObjParseException) t;
            } else {
                throw new DynXmlObjParseException(getClass().getName() + " parse exception", t);
            }
        }
    }

    // Write out this object as XML
    protected void internalToXml(final Element element) {
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
     * The natural mapping for a paraboloid is U equal to degrees longitude measured counter-clockwise
     * (looking down) starting at the +i axis being 0 degree (+j = 90 degree, -i = 180 degree,
     * and -j = 270 degree).  And V being latitude in degrees *  starting at -90 degrees for -k,
     * and +90 degree for +k.  The <tt>Paraboloid</tt> does not compute natural coordinates
     * unless they are specifically requested using this function.
     *
     * @param intersection The ray intersection.
     */
    public void getNaturalCoordinates(final RayIntersection intersection) {
        if (intersection.m_bNatural) return;

//        // PARABOLA BODY, USE SPHERICAL
//        if (intersection.m_ptObject.z < getHeight() - 0.001) {
//            final Vector3f vN = intersection.borrowVector().setValue(intersection.m_vObjNormal);
//            final float xyLen = (float) Math.sqrt((double) ((vN.i * vN.i) + (vN.j * vN.j)));
//            if (xyLen < GlobalConstants.ZERO_TOLERANCE_MAX_FLOAT) {
//                // normal is straight up / straight down
//                intersection.m_ptNatural.x = 0.0f;
//                if (vN.k > 0.0) {   // straight up
//                    intersection.m_ptNatural.y = 90.0f;
//                } else {            // straight down
//                    intersection.m_ptNatural.y = -90.0f;
//                }
//                intersection.m_vNatural[0].setValue(0.0f, 0.0f, 0.0f);
//                intersection.m_vNatural[1].setValue(0.0f, 0.0f, 0.0f);
//            } else {
//                final AngleF ang = intersection.borrowAngle();
//                intersection.m_ptNatural.x = ang.atan2(vN.j, vN.i).getDegrees();
//                intersection.m_ptNatural.y = ang.atan2(vN.k, xyLen).getDegrees();
//                intersection.returnAngle(ang);
//
//                intersection.m_vNatural[0].setValue(-vN.j / xyLen, vN.i / xyLen, 0.0f);
//                intersection.m_vNatural[1].setValue(-(vN.k * vN.i) / xyLen, -(vN.k * vN.j) / xyLen, xyLen);
//            }
//            intersection.returnVector(vN);
//        }
//        // CAP
//        else {
//            try {
//                final Vector3f vN = intersection.borrowVector().setValue(
//                        intersection.m_ptObject.x,
//                        intersection.m_ptObject.y,
//                        intersection.m_ptObject.z).normalize();
//                final float xyLen = (float) Math.sqrt((double) ((vN.i * vN.i) + (vN.j * vN.j)));
//                if (xyLen < GlobalConstants.ZERO_TOLERANCE_MAX_FLOAT) {
//                    // normal is straight up / straight down
//                    intersection.m_ptNatural.x = 0.0f;
//                    if (vN.k > 0.0) {   // straight up
//                        intersection.m_ptNatural.y = 90.0f;
//                    } else {            // straight down
//                        intersection.m_ptNatural.y = -90.0f;
//                    }
//                    intersection.m_vNatural[0].setValue(0.0f, 0.0f, 0.0f);
//                    intersection.m_vNatural[1].setValue(0.0f, 0.0f, 0.0f);
//                } else {
//                    final AngleF ang = intersection.borrowAngle();
//                    intersection.m_ptNatural.x = ang.atan2(vN.j, vN.i).getDegrees();
//                    intersection.m_ptNatural.y = ang.atan2(vN.k, xyLen).getDegrees();
//                    //intersection.m_ptNatural.j = 0;
//                    intersection.returnAngle(ang);
//
//                    intersection.m_vNatural[0].setValue(-vN.j / xyLen, vN.i / xyLen, 0.0f);
//                    intersection.m_vNatural[1].setValue(-(vN.k * vN.i) / xyLen, -(vN.k * vN.j) / xyLen, xyLen);
//                }
//                intersection.returnVector(vN);
//            } catch (final Throwable t) {
//                System.out.println("EXCEPTION IN GETNATURAL FOR PARAB");
//            }
//        }
//        intersection.m_bNatural = true;
    }

}
