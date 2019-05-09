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

import cip.render.IDynXmlObject;
import cip.render.raytrace.interfaces.IRtMaterial;
import cip.render.raytrace.interfaces.IRtLight;
import cip.render.raytrace.RayIntersection;
import cip.render.util3d.PackageConstants;
import cip.render.DynXmlObjLoader;
import cip.render.DynXmlObjParseException;
import cip.render.util.AngleF;
import cip.render.util3d.Line3f;
import cip.render.util3d.Point3f;
import cip.render.util3d.Vector3f;

import java.util.LinkedList;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This is the implementation of a sphere of some radius centered at 0,0,0 in the object coordinate system.
 * <p>
 * The sphere is specified as a node in an XML file as:<br><br>
 * <tt>
 * &nbsp;&nbsp;&nbsp; <font style="color:blue">&lt;<b>DynamicallyLoadedObject</b>
 * class="cip.raytrace.geometry.Sphere" name="<font style="color:magenta"><i>sphereName</i></font>"&gt;</font><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <font style="color:blue">&lt;<b>radius</b>&gt;<font style="color:magenta"><i>radius</i></font>&lt;/<b>radius</b>&gt;</font><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <font style="color:blue">&lt;<b>MaterialByRef</b> name="<font style="color:magenta"><i>materialName</i></font>"/&gt;</font><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <font style="color:blue">&lt;<b>DynamicallyLoadedObject</b>
 * class="<font style="color:magenta"><i>materialClass</i></font>"&gt;</font><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <font style="color:gray"><b>.</b><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <i>material specific nodes and attributes</i><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <b>.</b></font><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <font style="color:blue">&lt;/<b>DynamicallyLoadedObject</b>&gt;</font><br>
 * &nbsp;&nbsp;&nbsp; <font style="color:blue">&lt;/<b>DynamicallyLoadedObject</b>&gt;</font><br><br>
 * </tt>
 * where:<br>
 * <table border="0" width="90%">
 * <tr>
 * <td style="width:5%"></td>
 * <td><table border="1">
 * <tr>
 * <td><tt>radius</tt></td>
 * <td>The radius of the sphere.  The default radius is 1 if not specified.
 * </td>
 * </tr>
 * <tr>
 * <td><tt>MaterialByRef</tt></td>
 * <td>A material specfied by reference to the name of a previously loaded material.  <tt>MaterialByRef</tt> is
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
 * <tt>
 * &nbsp;&nbsp;&nbsp; <font style="color:blue">&lt;<b>DynamicallyLoadedObject</b>
 * class="cip.raytrace.geometry.Sphere" name="<font style="color:magenta">sphere</font>"&gt;</font><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <font style="color:blue">&lt;<b>radius</b>&gt;<font style="color:magenta">5</font>&lt;/<b>radius</b>&gt;</font><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <font style="color:blue">&lt;<b>MaterialByRef</b> name="<font style="color:magenta"><i>blue</i></font>"/&gt;</font><br>
 * &nbsp;&nbsp;&nbsp; <font style="color:blue">&lt;/<b>DynamicallyLoadedObject</b>&gt;</font><br><br>
 * </tt>
 *
 * @author royster.hall@gmail.com
 * @version 1.0
 * @since 1.0
 */
public class Sphere extends AGeometry {
    private static final String XML_TAG_RADIUS = "radius";

    // The instance definition
    private IRtMaterial m_mtl = DEFAULT_MATERIAL;  // the sphere material
    private float m_fRadius = 1.0f;                           // the radius of the sphere

    /**
     * Creates a new instance of <tt>Sphere</tt>.
     */
    public Sphere() {
    }

    //-------------------------------------------------------------------------------------------------------------------------
    public IRtMaterial getMaterial() {
        return m_mtl;
    }

    public void setMaterial(final IRtMaterial mtl) {
        m_mtl = mtl;
    }

    public float getRadius() {
        return m_fRadius;
    }

    public void setRadius(final float fRadius) {
        m_fRadius = fRadius;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IDynXmlObject interface implementation                                                                                //
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void loadFromXml(final @NotNull Element xmlElement, final @Nullable LinkedList refObjectList) throws DynXmlObjParseException {
        try {
            Node domNode = xmlElement.getFirstChild();
            while (null != domNode) {
                if (domNode instanceof Element) {
                    final Element element = (Element) domNode;
                    if (element.getTagName().equalsIgnoreCase(XML_TAG_RADIUS)) {
                        Node textNode = element.getFirstChild();
                        while (null != textNode) {
                            if (textNode.getNodeType() == Node.TEXT_NODE) {
                                m_fRadius = Float.parseFloat(textNode.getNodeValue().trim());
                                break;
                            }
                            textNode = textNode.getNextSibling();
                        }
                    } else if (element.getTagName().equalsIgnoreCase(DynXmlObjLoader.XML_TAG)) {
                        // Should be a material - that is the only dynamically loaded object that can be used
                        final Object obj = DynXmlObjLoader.LoadObject(element, refObjectList);
                        if (obj instanceof IRtMaterial) {
                            m_mtl = (IRtMaterial) obj;
                        } else {
                            throw new DynXmlObjParseException("Sphere " + m_strName + " material could not be parsed");
                        }
                    } else if (element.getTagName().equalsIgnoreCase(XML_TAG_MATERIAL_REF)) {
                        // a material reference
                        final String strName = element.getAttribute(XML_TAG_REF_NAME_ATTR);
                        m_mtl = resolveMaterialRef(strName, refObjectList);
                    } else {
                        throw new DynXmlObjParseException("Unrecognized sphere XML description element <" +
                                element.getTagName() + ">.");
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

    //-------------------------------------------------------------------------------------------------------------------------
    protected void internalToXml(final Element element) {
        // The radius
        final Element elRadius = element.getOwnerDocument().createElement(XML_TAG_RADIUS);
        element.appendChild(elRadius);
        elRadius.appendChild(element.getOwnerDocument().createTextNode(Float.toString(m_fRadius)));
        // The material
        if ((m_mtl != DEFAULT_MATERIAL) && (m_mtl instanceof IDynXmlObject)) {
            ((IDynXmlObject) m_mtl).toChildXmlElement(element);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IRtGeometry interface implementation                                                                                  //
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Returns <tt>true</tt> since a sphere is convex.
     */
    public boolean IsConvex() {
        return true;
    }

    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Returns an array of 24 points that are the vertices of a convex hull described by a min-max box with the corners
     * trimmed by planes that are perpendicular to the diagonals of the 8 quadrants of a 3D axis system.
     */
    public Point3f[] getConvexHullVertices() {
        final float fEdge = (float) (2.0 - Math.sqrt(3.0)) * m_fRadius;
        final Point3f[] ptHull = new Point3f[24];
        int ii;

        // do the points for one quadrant
        ptHull[0] = new Point3f().setValue(m_fRadius, m_fRadius, fEdge);
        ptHull[1] = new Point3f().setValue(fEdge, m_fRadius, m_fRadius);
        ptHull[2] = new Point3f().setValue(m_fRadius, fEdge, m_fRadius);
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
    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Tests a ray for an intersection with a sphere.  See {@link cip.render.raytrace.interfaces.IRtGeometry}
     * description of getRayIntersection().
     */
    public boolean getRayIntersection(final RayIntersection intersection, final Line3f ray, final boolean bStartsInside, final int nSample, final int nRandom) {
        // see if the ray intersects the sphere from the outside - this uses the sphere intersection formula from Watt, p18, with
        //  a=1 since the ray is normalized, and the computation of B and C simplified because the center of the sphere as at
        //  0,0,0
        final float fB = 2.0f * ((ray.m_vDir.i * ray.m_ptOrg.x) + (ray.m_vDir.j * ray.m_ptOrg.y) + (ray.m_vDir.k * ray.m_ptOrg.z));
        final float fC = (ray.m_ptOrg.x * ray.m_ptOrg.x) + (ray.m_ptOrg.y * ray.m_ptOrg.y) + (ray.m_ptOrg.z * ray.m_ptOrg.z) -
                (m_fRadius * m_fRadius);

        // solve for the intersection distance using the quadratic formula.  First check the determinant to make sure
        // it is greater than 0 - otherwise, there is no intersection.
        final float fDet = (fB * fB) - (4.0f * fC);

        final float fDistTmp;
        if (bStartsInside) {
            if (fDet < 0.0f) return false;  // no intersection - no solution to the quadratic equation
            // the ray started inside this sphere -- get the far intersection which should be where the ray leaves the sphere
            fDistTmp = 0.5f * (-fB + (float) Math.sqrt((double) fDet));
        } else {
            if (fDet < PackageConstants.ZERO_TOLERANCE_MAX_FLOAT)
                return false;  // no intersection - no solution to the quadratic equation
            // the ray started outside the sphere - get the closest intersection which should be where the ray entered the sphere
            fDistTmp = 0.5f * (-fB - (float) Math.sqrt((double) fDet));
            // We got here if the ray intersects the object.  Test the intersection distance - if
            //  this intersection is behind the eye, or, is not closer than a previously computed intersection, return.
            if ((fDistTmp < 0.0f) || (fDistTmp > intersection.m_fDist)) {
                return false;
            }
        }

        // Update the intersection structure with information for this intersection
        intersection.m_fDist = fDistTmp;
        ray.pointAtDistance(intersection.m_pt, fDistTmp);
        intersection.m_vNormal.i = intersection.m_pt.x / m_fRadius;
        intersection.m_vNormal.j = intersection.m_pt.y / m_fRadius;
        intersection.m_vNormal.k = intersection.m_pt.z / m_fRadius;
        intersection.m_ptObject.setValue(intersection.m_pt);
        intersection.m_vObjNormal.setValue(intersection.m_vNormal);
        intersection.m_bNatural = false;
        intersection.m_xfmObjToWorldNormal.identity();
        intersection.m_mtl = m_mtl;
        intersection.m_rtObj = this;
        return true;
    }
    //-------------------------------------------------------------------------------------------------------------------------

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

    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Tests a ray for a shadow intersection with a sphere.  See {@link cip.render.raytrace.interfaces.IRtGeometry}
     * description of testShadow().
     */
    public boolean testShadow(final RayIntersection intersection, final Vector3f vLight, final float fDistLight, final IRtLight light,
                              final int nSample, final int nRandom) {
        final float fB = 2.0f * ((vLight.i * intersection.m_pt.x) + (vLight.j * intersection.m_pt.y) +
                (vLight.k * intersection.m_pt.z));
        final float fC = (intersection.m_pt.x * intersection.m_pt.x) + (intersection.m_pt.y * intersection.m_pt.y) +
                (intersection.m_pt.z * intersection.m_pt.z) - (m_fRadius * m_fRadius);

        // solve for the intersection distance using the quadratic formula.  First check the determinant to make sure
        // it is greater than 0 - otherwise, there is no intersection.
        final float fDet = (fB * fB) - (4.0f * fC);
        if (fDet < PackageConstants.ZERO_TOLERANCE_MAX_FLOAT)
            return false;  // no intersection - no solution to the quadratic equation

        final float fDistTmp = 0.5f * (-fB - (float) Math.sqrt((double) fDet));
        // We got here if the ray intersects the object.  Test the intersection distance - if
        //  this intersection is behind the eye, or, is not closer than a previously computed intersection, return.
        if ((fDistTmp < 0.0f) || (fDistTmp > fDistLight)) {
            return false;
        }
        return true;
    }
}
