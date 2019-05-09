/*
 * XfmGeometry.java
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
import cip.render.raytrace.interfaces.IRtGeometry;
import cip.render.raytrace.interfaces.IRtLight;
import cip.render.raytrace.RayIntersection;
import cip.render.DynXmlObjLoader;
import cip.render.DynXmlObjParseException;
import cip.render.util3d.Bv3fIntersection;
import cip.render.util3d.Line3f;
import cip.render.util3d.Point3f;
import cip.render.util3d.Vector3f;
import cip.render.util3d.Xfm4x4f;

import java.util.LinkedList;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This is a transformed geometry implementation.  It applies a transform to a geometry to reposition the
 * local object coordinate system within the parent coordinate system (the coordinate system containing this
 * node).  The components of the transformed geometry are the positioning transform and the geometry to be
 * transformed.  Before rendering starts, the associated transformations for normals, and the backtransform
 * from object to world are precomputed.
 * <p>
 * This object is implemented primarily as a pass-through implementation where the normals, vectors, and
 * points for any intersection or shadow query are transformed into object space, the object operator is
 * called, and the result is back-transformed to world space if required.
 * <p>
 * The transformed geometry is specified as a node in an XML file as:<br><br>
 * <tt>
 * &nbsp;&nbsp;&nbsp; <font style="color:blue">&lt;D<b>ynamicallyLoadedObject</b>
 * class="cip.raytrace.geometry.XfmGeometry" name="<font style="color:magenta"><i>xfmGeomName</i></font>"&gt;</font><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <font style="color:blue">&lt;<b>position</b> <font style="color:magenta"><i>Xfm4x4f_attributes</i></font>/&gt;</font><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <font style="color:blue">&lt;<b>GeometryByRef</b> name="<font style="color:magenta"><i>geomName</i></font>"/&gt;</font><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <font style="color:blue">&lt;<b>DynamicallyLoadedObject</b>
 * class="<font style="color:magenta"><i>transformedGeomClass</i></font>"&gt;</font><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <font style="color:gray"><b>.</b><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <b>.</b><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<i>transformed geometry specific node content</i><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <b>.</b><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <b>.</b></font><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <font style="color:blue">&lt;/<b>DynamicallyLoadedObject</b>&gt;</font><br>
 * &nbsp;&nbsp;&nbsp; <font style="color:blue">&lt;/<b>DynamicallyLoadedObject</b>&gt;</font><br><br>
 * </tt>
 * where:<br>
 * <table border="0" width="90%">
 * <tr>
 * <td style="width:5%"></td>
 * <td><table border="1">
 * <tr>
 * <td><tt>position</tt></td>
 * <td>The geometry position as specified by the <tt><i>Xfm4x4f_attributes</i></tt> which are described in
 * {@link Xfm4x4f#setValue(org.w3c.dom.Element, boolean, boolean)}.  This object supports only
 * rigid-body transformation.  Specifically, translation and rotation are supported, scale and shear are
 * NOT supported.<br>
 * </td>
 * </tr>
 * <tr>
 * <td><tt>GeometryByRef</tt></td>
 * <td>A geometry specfied by reference to the name of a perviously loaded geometry.  <tt>GeometryByRef</tt> is
 * mutually exclusive with the <tt>DynamicallyLoadedObject</tt> specification of a geometry.  If no geometry
 * is specified this results in a parse exception.
 * </td>
 * </tr>
 * <tr>
 * <td><tt>DynamicallyLoadedObject</tt></td>
 * <td>The specification for a geometry to be transformed.  <tt>GeometryByRef</tt> is
 * mutually exclusive with the <tt>DynamicallyLoadedObject</tt> specification of a geometry.  If no geometry
 * is specified this results in a parse exception.  The dynamically
 * loaded object must implement the  {@link cip.render.raytrace.interfaces.IRtGeometry} interface.
 * </td>
 * </tr>
 * </table>
 * </td>
 * </tr>
 * </table><br>
 * <p>
 * <b>Example of XML Specification</b>
 * <p>
 * The following specifies a {@link cip.render.raytrace.geometry.Sphere} geometry of radius 6 centered at (10,20,30):<br><br>
 * <tt>
 * &nbsp;&nbsp;&nbsp; <font style="color:blue">&lt;D<b>ynamicallyLoadedObject</b>
 * class="cip.raytrace.geometry.XfmGeometry" name="<font style="color:magenta">sphere1</font>"&gt;</font><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <font style="color:blue">&lt;<b>position</b> originAt="<font style="color:magenta">10,20,30</font>"/&gt;</font><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <font style="color:blue">&lt;<b>DynamicallyLoadedObject</b>
 * class="<font style="color:magenta">cip.raytrace.geometry.Sphere</font>"&gt;</font><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <font style="color:blue">&lt;<b>radius</b>&gt;<font style="color:magenta">6</font>&lt/<b>radius</b>&gt;</font><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <font style="color:blue">&lt;/<b>DynamicallyLoadedObject</b>&gt;</font><br>
 * &nbsp;&nbsp;&nbsp; <font style="color:blue">&lt;/<b>DynamicallyLoadedObject</b>&gt;</font><br><br>
 * </tt>
 *
 * @author royster.hall@gmail.com
 * @version 1.0
 * @since 1.0
 */
public class XfmGeometry extends AGeometry {
    private static final String XML_TAG_POSITION = "position";
    // The instance definition
    private final Xfm4x4f m_xfm = new Xfm4x4f().identity();   // the positioning transform for the geometry (obj->world)
    //  This transforms both position and direction vectors
    private final Xfm4x4f m_xfmNormal = new Xfm4x4f();        // the obj->world normal transform (transpose of m_xfxWldObj)
    private final Xfm4x4f m_xfmWldObj = new Xfm4x4f();        // the world->obj transform (inverse of m_xfm)
    private final Xfm4x4f m_xfmWldObjNormal = new Xfm4x4f();  // the world->obj normal transform (transpose of m_xfm)
    private IRtGeometry m_obj = null;                       // the transformed geometry

    /**
     * Creates a new instance of a <tt>XfmGeometry</tt> transformed geometry object.
     */
    public XfmGeometry() {
    }

    //-------------------------------------------------------------------------------------------------------------------------
    public IRtGeometry getGeometry() {
        return m_obj;
    }

    public void setGeometry(final IRtGeometry obj) {
        m_obj = obj;
    }

    public void getXfm(final Xfm4x4f xfm) {
        xfm.setValue(m_xfm);
    }

    public void setXfm(final Xfm4x4f xfm) {
        m_xfm.setValue(xfm);
        initForRender();
    }

    //-------------------------------------------------------------------------------------------------------------------------
    //  This initialization for rendering is the computation of the back transfrom (world->obj) and forward
    //  transform (obj->world) for normals.  Note that if we do not allow scale and shear, the forward transfor for normals is
    //  equal to the forward transform for points and directions.  Otherwise the back forward for normals is the transpose
    //  of the inverse of the  forward transform for points.  NOTE: the back transform is already the inverse of the forward
    //  transform -- so we only need to transpose that to get the forward transform for normals.
    protected final void initForRender() {
        m_xfmWldObj.setValue(m_xfm).invert();
        m_xfmNormal.setValue(m_xfmWldObj).transpose();
        m_xfmWldObjNormal.setValue(m_xfm).transpose();
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
                    if (element.getTagName().equalsIgnoreCase(DynXmlObjLoader.XML_TAG)) {
                        // if this should be the contained geometry - note, we can load only one, and the last
                        //  one we encounter is the one that is saved.
                        final Object obj = DynXmlObjLoader.LoadObject(element, refObjectList);
                        if (obj instanceof IRtGeometry) {
                            m_obj = (IRtGeometry) obj;
                        } else {
                            throw new DynXmlObjParseException("Transformed geometry " + m_strName + ": contained geometry could not be parsed");
                        }
                    } else if (element.getTagName().equalsIgnoreCase(XML_TAG_POSITION)) {
                        // pass the position on to the transformation for parsing
                        m_xfm.setValue(element, false, false);
                    } else if (element.getTagName().equalsIgnoreCase(XML_TAG_GEOMETRY_REF)) {
                        final String strName = element.getAttribute(XML_TAG_REF_NAME_ATTR);
                        m_obj = resolveGeometryRef(strName, refObjectList);
                    } else {
                        throw new DynXmlObjParseException("Unrecognized Transformed Geometry element <" +
                                element.getTagName() + ">");
                    }
                }
                domNode = domNode.getNextSibling();
            }
            if (null == m_obj) {
                throw new DynXmlObjParseException("Transformed geometry " + m_strName + ": no contained geometry specified.");
            }
            initForRender();
        } catch (final Throwable t) {
            if (t instanceof DynXmlObjParseException) {
                throw (DynXmlObjParseException) t;
            } else {
                throw new DynXmlObjParseException("Transformed geometry parse exception", t);
            }
        }
    }

    //-------------------------------------------------------------------------------------------------------------------------
    protected void internalToXml(final Element element) {
        // The position transform
        try {
            // the transformed position
            final Element elXfm = element.getOwnerDocument().createElement(XML_TAG_POSITION);
            m_xfm.toXmlAttr(elXfm, false, false);
            element.appendChild(elXfm);
        } catch (final Throwable t) {
            // there is a singularity in the transform, so we can't decompose it -- oops (means we couldn't render it either)
        }
        // the object
        if (null != m_obj) {
            ((IDynXmlObject) m_obj).toChildXmlElement(element);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IRtGeometry interface implementation                                                                                  //
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Passes the query to the child (transformed) geometry.  Returns <tt>false</tt> if there is no child geometry.
     *
     * @return Returns <tt>true</tt> if the object is convex and <tt>false</tt> if the object is not convex
     * or the convexity cannot be verified.
     */
    public boolean IsConvex() {
        if (null == m_obj) {
            return true;
        }
        return m_obj.IsConvex();
    }

    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Gets the convex hull vertices of the child object, transforms them, and returns them.  <tt>null</tt> is returned
     * if there is no child object, or, if the child object returns no convex null vertices.
     *
     * @return Returns the array of convex hull vertices,  Returns <tt>null</tt> if a convex hull cannot be
     * fit to the object.  Being unable to fit a convex hull implies that the object must be explicitly
     * queried for intersection on every ray.
     */
    public Point3f[] getConvexHullVertices() {
        if (null == m_obj) {
            return null;
        }
        final Point3f[] pts = m_obj.getConvexHullVertices();
        if (null != pts) {
            m_xfm.transform(pts);
        }
        return pts;
    }

    //-------------------------------------------------------------------------------------------------------------------------
    public boolean getBvIntersection(final Bv3fIntersection bvInt, final Line3f ray) {
        if (null == m_obj) {
            return false;
        }
        final float fX;
        final float fY;
        final float fZ;
        final float fI;
        final float fJ;
        final float fK;
        fX = ray.m_ptOrg.x;
        fY = ray.m_ptOrg.y;
        fZ = ray.m_ptOrg.z;
        fI = ray.m_vDir.i;
        fJ = ray.m_vDir.j;
        fK = ray.m_vDir.k;
        m_xfmWldObj.transform(ray.m_ptOrg);
        m_xfmWldObj.transform(ray.m_vDir);
        final boolean bRet = m_obj.getBvIntersection(bvInt, ray);
        ray.m_ptOrg.x = fX;
        ray.m_ptOrg.y = fY;
        ray.m_ptOrg.z = fZ;
        ray.m_vDir.i = fI;
        ray.m_vDir.j = fJ;
        ray.m_vDir.k = fK;
        if (bRet) {
            bvInt.m_obj = this;
        }
        return bRet;
    }

    //-------------------------------------------------------------------------------------------------------------------------
    public void getNaturalCoordinates(final RayIntersection intersection) {
        if (null == m_obj) {
            return;
        }
        m_obj.getNaturalCoordinates(intersection);
    }

    //-------------------------------------------------------------------------------------------------------------------------
    public boolean getRayIntersection(final RayIntersection intersection, final Line3f ray, final boolean bStartsInside, final int nSample, final int nRandom) {
        if (null == m_obj) {
            return false;
        }
        // transform the ray to object space.  Do the intersection - if this is a closer intersection,
        //  then transform the relevant intersection info back to world space.
        final Line3f rayObj = intersection.borrowLine();
        m_xfmWldObj.transform(ray.m_ptOrg, rayObj.m_ptOrg);
        m_xfmWldObjNormal.transform(ray.m_vDir, rayObj.m_vDir);
        final boolean bRet = m_obj.getRayIntersection(intersection, rayObj, bStartsInside, nSample, nRandom);
        if (bRet) {
            m_xfm.transform(intersection.m_pt);
            m_xfmNormal.transform(intersection.m_vNormal);
            intersection.m_rtObj = this;
        }
        intersection.returnLine(rayObj);
        return bRet;
    }

    //-------------------------------------------------------------------------------------------------------------------------
    public boolean testShadow(final RayIntersection intersection, final Vector3f vLight, final float fDistLight,
                              final IRtLight light, final int nSample, final int nRandom) {
        if (null == m_obj) {
            return false;
        }
        // transform the intersection and light vector into object space and do the intersection calculation
        final RayIntersection intersectionObj = intersection.borrowIntersection();
        final Vector3f vLightObj = intersection.borrowVector().setValue(vLight);
        m_xfmWldObj.transform(intersection.m_pt, intersectionObj.m_pt);
        m_xfmWldObjNormal.transform(intersection.m_vNormal, intersectionObj.m_vNormal);
        m_xfmWldObj.transform(vLightObj);
        final boolean bRet = m_obj.testShadow(intersectionObj, vLightObj, fDistLight, light, nSample, nRandom);
        intersection.returnIntersection(intersectionObj);
        intersection.returnVector(vLightObj);
        return bRet;
    }
}
