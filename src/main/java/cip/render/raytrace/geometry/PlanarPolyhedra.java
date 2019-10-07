/*
 * PlanarPolyhedra.java
 *
 * Created on October 10, 2002, 3:18 PM
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
import cip.render.raytrace.interfaces.IRtLight;
import cip.render.raytrace.interfaces.IRtMaterial;
import cip.render.util3d.*;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Element;

import java.util.LinkedList;

/**
 * A convex planar polyhedra described by the plane equations of the faces.
 * <p>
 * There are methods to programmatically initialize a planar polyhedra, but, it is most common to load the  planar polyhedra
 * from a scene description file. The planar polyhedra is specified as a node in an XML scene description file as:
 * <pre>
 *     <font style="color:blue">&lt;<b>DynamicallyLoadedObject</b> class="cip.raytrace.geometry.PlanarPolyhedra" name="<font style="color:magenta"><i>polyhedraName</i></font>"&gt;</font>
 *         <font style="color:blue">&lt;<b>face</b> plane="<font style="color:magenta"><i>A,B,C,D</i></font>"/&gt;</font>
 *           <font style="color:gray"><b>.</b>
 *           <b>.</b></font>
 *         <font style="color:blue">&lt;<b>face</b> plane="<font style="color:magenta"><i>A,B,C,D</i></font>"/&gt;</font>
 *         <font style="color:blue">&lt;<b>MaterialByRef</b>&gt;<font style="color:magenta"><i>materialName</i></font>&lt;/<b>MaterialByRef</b>&gt;</font>
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
 * <td><tt>face</tt></td>
 * <td>The plane equation of a face.  There are as many <tt>face</tt> entries as there are faces on the object.
 * The plane equation is normalized during object load.
 * </td>
 * </tr>
 * <tr>
 * <td><tt>MaterialByRef</tt></td>
 * <td>A material specfied by reference to the name of a perviously loaded material.  <tt>MaterialByRef</tt> is
 * mutually exclusive with the <tt>DynamicallyLoadedObject</tt> specification of a material.  If no material
 * is specified, the material defaults to matte green material.
 * </td>
 * </tr>
 * <tr>
 * <td><tt>DynamicallyLoadedObject</tt></td>
 * <td>The specification for a material for the polyhedra.  <tt>MaterialByRef</tt> is
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
 * The following specifies a unit cube of material <i>red</i> centered on the origin:
 * <pre>
 *     <font style="color:blue">&lt;<b>DynamicallyLoadedObject</b> class="cip.raytrace.geometry.PlanarPolyhedra" name="<font style="color:magenta">unitCube</font>"&gt;</font>
 *         <font style="color:blue">&lt;<b>face</b> plane="<font style="color:magenta"> 0.0f, 0.0f, 1.0f,-0.5f</font>"/&gt;</font>
 *         <font style="color:blue">&lt;<b>face</b> plane="<font style="color:magenta"> 0.0f,-1.0f, 0.0f,-0.5f</font>"/&gt;</font>
 *         <font style="color:blue">&lt;<b>face</b> plane="<font style="color:magenta"> 1.0f, 0.0f, 0.0f,-0.5f</font>"/&gt;</font>
 *         <font style="color:blue">&lt;<b>face</b> plane="<font style="color:magenta"> 0.0f, 1.0f, 0.0f,-0.5f</font>"/&gt;</font>
 *         <font style="color:blue">&lt;<b>face</b> plane="<font style="color:magenta">-1.0f, 0.0f, 0.0f,-0.5f</font>"/&gt;</font>
 *         <font style="color:blue">&lt;<b>face</b> plane="<font style="color:magenta"> 0.0f, 0.0f,-1.0f,-0.5f</font>"/&gt;</font>
 *         <font style="color:blue">&lt;<b>MaterialByRef</b>&gt;<font style="color:magenta">red</font>&lt;/<b>MaterialByRef</b>&gt;</font>
 *     <font style="color:blue">&lt;/<b>DynamicallyLoadedObject</b>&gt;</font>
 * </pre>
 *
 * @author royster.hall@gmail.com
 * @version 1.0
 * @since 1.0
 */
public class PlanarPolyhedra extends AGeometry {
    // These are the planes describing the geometry
    protected Face[] m_faces = null;
    protected LinkedList<Face> m_faceListTmp = new LinkedList<Face>();

    /**
     * Creates a new instance of <tt>PlanarPolyhedra</tt>
     */
    public PlanarPolyhedra() {
        m_strType = "planar polyhedra";
        m_strName = "PlanarPolyhedra";
    }

    //------------------------------------------------------------------------------------------------------------------------------
    public void clear() {
        m_mtl = DEFAULT_MATERIAL;
        m_faces = null;
    }

    public IRtMaterial getMaterial() {
        return m_mtl;
    }

    public void setMaterial(final IRtMaterial mtl) {
        m_mtl = mtl;
    }

    public void addFace(final Plane3f pln) throws ZeroLengthVectorException {
        addFace(pln.m_fA, pln.m_fB, pln.m_fC, pln.m_fD, null);
    }

    public void addFace(final Plane3f pln, final IRtMaterial mtl) throws ZeroLengthVectorException {
        addFace(pln.m_fA, pln.m_fB, pln.m_fC, pln.m_fD, mtl);
    }

    public void addFace(final float fA, final float fB, final float fC, final float fD, final IRtMaterial mtl)
            throws ZeroLengthVectorException {
        // The faces are kept in a fixed length array for fastest traversal during ray tracing.  This means we need to allocate
        //  a new vector and copy face references from the old vector.
        final Face tmpFace = new Face(fA, fB, fC, fD, mtl);    // in case it throws an exception;
        if (null != m_faces) {
            final Face[] oldFaces = m_faces;
            final int oldLen = m_faces.length;
            m_faces = new Face[oldLen + 1];
            System.arraycopy(oldFaces, 0, m_faces, 0, oldLen);
            m_faces[oldLen] = tmpFace;
        } else {
            m_faces = new Face[1];
            m_faces[0] = tmpFace;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IDynXmlObject interface implementation                                                                                     //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected boolean internalParseElement(@NotNull Element element, final LinkedList<INamedObject> refObjectList)
            throws DynXmlObjParseException {
        Face face = tryParseFace(element, refObjectList);
        if (null != face) {
            m_faceListTmp.add(face);
            return true;
        }
        return false;
    }

    //------------------------------------------------------------------------------------------------------------------------------
    @Override
    protected void internalFinishLoad() {
        // create the face array from the linked list -- we move the faces into an array for best performance during
        // intersection testing. There is a physical/philosophical issue here - what constitutes a valid planar polyhedra?
        // For example, if you want a surface representing ground, and the camera is above the ground, then a single plane
        // is valid. If you want a glass/diamond gem, then you need a closed polyhedra - this requires a minimum of 4
        // planes (a tetrahedron-like volume) that are configured to create a closed solid. This validation is non-trivial
        m_faces = new Face[m_faceListTmp.size()];
        for (int iFace = 0; iFace < m_faceListTmp.size(); iFace++) {
            m_faces[iFace] = m_faceListTmp.get(iFace);
        }
        m_faceListTmp = null;
    }


    //------------------------------------------------------------------------------------------------------------------------------
    @Override
    protected void internalToXml(@NotNull final Element element) {
        final StringBuilder strBuff = new StringBuilder(64);
        // The faces
        if (null != m_faces) {
            for (Face m_face : m_faces) {
                final Element elFace = element.getOwnerDocument().createElement(XML_TAG_FACE);
                element.appendChild(elFace);
                strBuff.delete(0, strBuff.length()).append(m_face.m_pln.m_fA).append(',').
                        append(m_face.m_pln.m_fB).append(',').append(m_face.m_pln.m_fC).append(',').
                        append(m_face.m_pln.m_fD);
                elFace.setAttribute(XML_ATTR_FACE_PLANE, strBuff.substring(0));
                if (null != m_face.m_mtl) {
                    ((IDynXmlObject) m_face.m_mtl).toChildXmlElement(elFace);
                }
            }
        }
        // The material
        if ((m_mtl != DEFAULT_MATERIAL) && (m_mtl instanceof IDynXmlObject)) {
            ((IDynXmlObject) m_mtl).toChildXmlElement(element);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IRtGeometry interface implementation                                                                                       //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean isConvex() {
        return true;
    }

    @Override
    public boolean isInside(Point3f pt) {
        return false;
    }

    //------------------------------------------------------------------------------------------------------------------------------
    @Override
    public boolean getRayIntersection(@NotNull final RayIntersection intersection, @NotNull final Line3f ray,
                                      final boolean bStartsInside, final int nSample, final int nRandom) {
        final Plane3fIntersection plnInt = intersection.borrowPlaneInt();
        try {
            //  This is the convex polyhedra test where we compute the distence to intersections
            //  into the planes of the polyhedra, and out of the planes of the polyhedra.  If
            //  the furthest in-to is closer than the furthest out-of, then the ray is is
            //  intersecting the polyhedra.
            float fDistIn = Float.NEGATIVE_INFINITY;
            float fDistOut = Float.POSITIVE_INFINITY;
            int nIn = -1;
            int nOut = -1;
            for (int ix = 0; ix < m_faces.length; ix++) {
                m_faces[ix].m_pln.getIntersection(plnInt, ray);
                if (plnInt.m_nCode == Plane3fIntersection.NONE_OUTSIDE) {
                    // This ray is parallel to and outside one of the planes of the polyhedra
                    if (!bStartsInside) return false;
                } else if (plnInt.m_nCode == Plane3fIntersection.NONE_INSIDE) {
                    // do nothing - parallel and inside the plane, other geometry will be
                    //  the determining factor
                } else if (plnInt.m_nCode == Plane3fIntersection.GOING_OUT_OF) {
                    // going out of the plane
                    if (plnInt.m_fDist < fDistOut) {
                        fDistOut = plnInt.m_fDist;
                        nOut = ix;
                        if (((fDistOut < 0.0f) || (fDistIn > fDistOut)) && !bStartsInside) return false;
                    }
                } else {
                    // going into the plane - and if this is greater than the current distance in, reset that.
                    if (plnInt.m_fDist > fDistIn) {
                        fDistIn = plnInt.m_fDist;
                        nIn = ix;
                        if (fDistIn > fDistOut) {
                            // ooh - the furthest distance in is greater than the closest distance out - so no intersection.
                            return false;
                        }
                    }
                }
            }
            // We got here if the ray intersects the object.
            if (bStartsInside) {
                intersection.m_fDist = fDistOut;
                m_faces[nOut].m_pln.getNormal(intersection.m_vNormal);
            } else {
                //  Test the intersection distance - if
                //  this intersection is behind the eye, or, is not closer than a previously computed intersection, return.
                if ((fDistIn < 0.0f) || (fDistIn > intersection.m_fDist)) {
                    return false;
                }
                intersection.m_fDist = fDistIn;
                m_faces[nIn].m_pln.getNormal(intersection.m_vNormal);
            }

            // Update the intersection structure with information for this intersection
            ray.pointAtDistance(intersection.m_pt, intersection.m_fDist);
            intersection.m_ptObject.setValue(intersection.m_pt);
            intersection.m_vObjNormal.setValue(intersection.m_vNormal);
            intersection.m_bNatural = false;
            intersection.m_xfmObjToWorldNormal.identity();
            intersection.m_mtl = (null != m_faces[nIn].m_mtl) ? m_faces[nIn].m_mtl : m_mtl;
            intersection.m_rtObj = this;
            return true;
        } catch (final Throwable t) {
            return false;
        } finally {
            intersection.returnPlaneInt(plnInt);
        }
    }

    //------------------------------------------------------------------------------------------------------------------------------
    @Override
    public boolean testShadow(@NotNull final RayIntersection intersection, final Vector3f vLight, final float fDistLight,
                              final IRtLight light, final int nSample, final int nRandom) {
        final Plane3fIntersection plnInt = intersection.borrowPlaneInt();

        try {
            //  This is the convex polyhedra test where we compute the distance to intersections
            //  into the planes of the polyhedra, and out of the planes of the polyhedra.  If
            //  the furthest in-to is closer than the furthest out-of, then the ray is is
            //  intersecting the polyhedra.
            float fDistIn = Float.NEGATIVE_INFINITY;
            float fDistOut = Float.POSITIVE_INFINITY;
            for (int ix = 0; ix < m_faces.length; ix++) {
                m_faces[ix].m_pln.getIntersection(plnInt, intersection.m_pt, vLight);
                if (plnInt.m_nCode == Plane3fIntersection.NONE_OUTSIDE) {
                    // This ray is parallel to and outside one of the planes of the polyhedra.
                    //  An intersection is not possible - we don't need to do anymore testing
                    return false;
                } else if (plnInt.m_nCode == Plane3fIntersection.NONE_INSIDE) {
                    // do nothing - parallel and inside the plane, other geometry will be
                    //  the determining factor
                } else if (plnInt.m_nCode == Plane3fIntersection.GOING_OUT_OF) {
                    // going out of the plane - this is important if it is the closest 'goes out of' we've
                    //  encountered so far.
                    if (plnInt.m_fDist < fDistOut) {
                        fDistOut = plnInt.m_fDist;
                        if ((fDistOut < 0.0f) || (fDistIn > fDistOut)) {
                            // if this test is true, an intersection is not possible - we don't
                            //  need to do anymore testing.
                            return false;
                        }
                    }
                } else {
                    // going into the plane - this is important if it is the furthest 'goes into' we've
                    //  encountered so far.
                    if (plnInt.m_fDist > fDistIn) {
                        fDistIn = plnInt.m_fDist;
                        if (fDistIn > fDistOut) {
                            // if this test is true, an intersection is not possible - we don't
                            //  need to do anymore testing.
                            return false;
                        }
                    }
                }
            }
            // We got here if the ray intersects the object.  Test the intersection distance - if
            //  this intersection is behind the eye, or, is not closer than a previously computed intersection, return.
            return (!(fDistIn < 0.0f)) && (!(fDistIn > fDistLight));
        } catch (final Throwable t) {
            return false;
        } finally {
            intersection.returnPlaneInt(plnInt);
        }
    }
}
