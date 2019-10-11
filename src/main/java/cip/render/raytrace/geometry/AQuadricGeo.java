package cip.render.raytrace.geometry;

import cip.render.DynXmlObjParseException;
import cip.render.INamedObject;
import cip.render.raytrace.RayIntersection;
import cip.render.raytrace.interfaces.IRtLight;
import cip.render.util3d.*;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Element;

import java.util.LinkedList;

/**
 * This is an abstract class for a quadric object geometry. Quadric geometries all wrap a {@link cip.render.util3d.Quadric3f}
 * object with XML load and ray-tracing capabilities. The ray-tracing capabilities are pretty much the same for all quadrics, so
 * most of the code is here.
 */
abstract class AQuadricGeo extends AGeometry {
    static final String XML_TAG_RADIUS = "radius";

    // This is the definition of the quadric
    final Quadric3f m_quadric = new Quadric3f();
    // These are the clipping planes for the quadric geometery
    private Face[] m_clipPlanes = null;
    private LinkedList<Face> m__tmpClipPlanes = new LinkedList<Face>();

    private static final int CLIP_INTERSECT = 1;
    private static final int NO_CLIP_INTERSECT = 0;
    private static final int CLIP_NEGATES_INTERSECT = -1;

    //------------------------------------------------------------------------------------------------------------------------------
    AQuadricGeo() {
        super();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IRtGeometry interface implementation                                                                                       //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    boolean pkgParseElement(@NotNull Element element, final LinkedList<INamedObject> refObjectList)
            throws DynXmlObjParseException {
        Face face = tryParseFace(element, refObjectList);
        if (null != face) {
            m__tmpClipPlanes.add(face);
            return true;
        }
        return false;
    }

    //------------------------------------------------------------------------------------------------------------------------------
    @Override
    void pkgFinishLoad() {
        // create the face array from the linked list -- we move the faces into an array for best performance during
        // intersection testing. There is a physical/philosophical issue here - what constitutes a valid planar polyhedra?
        // For example, if you want a surface representing ground, and the camera is above the ground, then a single plane
        // is valid. If you want a glass/diamond gem, then you need a closed polyhedra - this requires a minimum of 4
        // planes (a tetrahedron-like volume) that are configured to create a closed solid. This validation is non-trivial
        m_clipPlanes = new Face[m__tmpClipPlanes.size()];
        for (int iFace = 0; iFace < m__tmpClipPlanes.size(); iFace++) {
            m_clipPlanes[iFace] = m__tmpClipPlanes.get(iFace);
        }
        m__tmpClipPlanes = null;
    }

    @Override
    public boolean isConvex() {
        return m_quadric.isConvex();
    }

    @Override
    public boolean isInside(Point3f pt) {
        return m_quadric.isInside(pt);
    }

    //------------------------------------------------------------------------------------------------------------------------------
    /**
     * Tests a ray for an intersection with a quadric.  See {@link cip.render.raytrace.interfaces.IRtGeometry}
     * description of getRayIntersection().
     */
    @Override
    public boolean getRayIntersection(@NotNull final RayIntersection intersection, @NotNull final Line3f ray,
                                      final boolean bStartsInside, final int nSample, final int nRandom) {
        final Quadric3fIntersection qInt = intersection.borrowQuadricInt();
        try {
            m_quadric.getIntersection(qInt, ray, bStartsInside);
            if (qInt.m_nCode == Quadric3fIntersection.NONE_OUTSIDE) {
                // Nothing more to test, this starts ray is outside the quadric and does not intersect it, clipping
                // planes are irrelevant.
                return false;
            }
            float fDistIn = Float.NEGATIVE_INFINITY;
            float fDistOut = Float.POSITIVE_INFINITY;
            if (qInt.m_nCode == Quadric3fIntersection.NONE_INSIDE) {
                // The ray starts inside the quadric and does not intersect it - however, it could intersect a
                // clipping plane either into or out of the quadric.
                return CLIP_INTERSECT == lclTestClippingPlanes(intersection, ray, bStartsInside,
                        fDistIn, fDistOut);
            } else {
                // We got here if the ray intersects this quadric object. In a convex quadric the least distance intersection
                // is always the intersection where the ray goes into the quadric. In a concave quadric, the least distance
                // intersection may be going out of the object. If the first intersection is not the 'going in' intersection
                // then reverse the intersection of interest as dictated by whether we think we started inside or outside the
                // object and whether we think the the ray started inside or outside the object.
                if (qInt.m_nCode == Quadric3fIntersection.GOING_INTO) {
                    // the normal in-out for convex and certain orientations of concave quadrics, where the closest intersection
                    // is the going in.
                    fDistIn = qInt.m_fDist1;
                    fDistOut = qInt.m_fDist2;
                } else {
                    // this is a concave with and out-in ordering of distances
                    fDistOut = qInt.m_fDist1;
                    fDistIn = qInt.m_fDist2;
                    // Logically: if bStartsInside is not set the ray start is not inside the object, and if fDistOut is
                    // positive, the ray start is inside the quadric, but there should be a clipping plane between us and
                    // going out. If the fDistOut is negative then we are really only concerned about going in

                    // BUT - if the clipping plane is behind the goes out, then the goes out is outside the bounds of the
                    // geometry. In this case, the goes in is visible. and potentially an object intersection of interest.
                    if (!bStartsInside) {
                        if (fDistOut > 0.0f) {
                            fDistIn = Float.NEGATIVE_INFINITY;
                        } else {
                            fDistOut = Float.POSITIVE_INFINITY;
                        }
                    }
                }
                int clip_status = lclTestClippingPlanes(intersection, ray, bStartsInside, fDistIn, fDistOut);
                if (clip_status == CLIP_NEGATES_INTERSECT) {
                    if (!bStartsInside && (qInt.m_nCode == Quadric3fIntersection.GOING_OUT_OF)) {
                        // And this is the case where the ray starts inside the quadric, but the clipping planes negate that
                        // that intersection as one of interest, so we look at the goes in
                        fDistIn = qInt.m_fDist2;
                        fDistOut = Float.POSITIVE_INFINITY;
                        clip_status = lclTestClippingPlanes(intersection, ray, bStartsInside, fDistIn, fDistOut);
                        if (clip_status == CLIP_NEGATES_INTERSECT) {
                            return false;
                        } else if (clip_status == CLIP_INTERSECT) {
                            return true;
                        }
                    } else {
                        return false;
                    }
                } else if (clip_status == CLIP_INTERSECT) {
                    // The clipping plane intersection supersedes the quadric intersection
                    return true;
                }
            }


            // Test the intersection distance - if this intersection is behind the eye( behind the start of the ray), or, is
            // not closer than a previously computed intersection, return.
            if (bStartsInside) {
                intersection.m_fDist = fDistOut;
            } else if ((fDistIn < 0.0f) || (fDistIn > intersection.m_fDist)) {
                return false;
            } else {
                intersection.m_fDist = fDistIn;
            }

            // Update the intersection structure with information for this intersection
            ray.pointAtDistance(intersection.m_pt, intersection.m_fDist);
            m_quadric.getNormal(intersection.m_vNormal, intersection.m_pt);
            intersection.m_bNatural = false;
            intersection.m_ptObject.setValue(intersection.m_pt);
            intersection.m_vObjNormal.setValue(intersection.m_vNormal);
            intersection.m_xfmObjToWorldNormal.identity();
            intersection.m_mtl = m_mtl;

            return true;
        } finally {
            intersection.returnQuadricInt(qInt);
        }
    }

    //------------------------------------------------------------------------------------------------------------------------------
    private int lclTestClippingPlanes(@NotNull final RayIntersection intersection, @NotNull final Line3f ray,
                                      boolean bStartsInside, float fDistIn, float fDistOut) {
        if ((null == m_clipPlanes) || (0 == m_clipPlanes.length)) {
            return NO_CLIP_INTERSECT;
        }
        final Plane3fIntersection plnInt = intersection.borrowPlaneInt();
        try {
            //  We are testing spatial clipping planes after testing for a quadric intersection
            int nIn = -1;
            int nOut = -1;
            for (int ix = 0; ix < m_clipPlanes.length; ix++) {
                m_clipPlanes[ix].m_pln.getIntersection(plnInt, ray);
                if (plnInt.m_nCode == Plane3fIntersection.NONE_OUTSIDE) {
                    // This ray is parallel to and outside one of the planes of the polyhedra
                    if (!bStartsInside) return CLIP_NEGATES_INTERSECT;
                } else if (plnInt.m_nCode == Plane3fIntersection.NONE_INSIDE) {
                    // do nothing - parallel and inside the plane, other geometry will be
                    //  the determining factor
                } else {
                    if (plnInt.m_nCode == Plane3fIntersection.GOING_OUT_OF) {
                        // going out of the plane - OK we need to think about this in the context of concave quadrics
                        if (plnInt.m_fDist < fDistOut) {
                            fDistOut = plnInt.m_fDist;
                            if ((fDistIn > fDistOut) && !bStartsInside) {
                                return CLIP_NEGATES_INTERSECT;
                            }
                            nOut = ix;
                        }
                    } else {
                        // going into the plane - and if this is greater than the current distance in, reset that.
                        if (plnInt.m_fDist > fDistIn) {
                            fDistIn = plnInt.m_fDist;
                            if (fDistIn > fDistOut) {
                                // ooh - the furthest distance in is greater than the closest distance out - so no intersection.
                                return CLIP_NEGATES_INTERSECT;
                            }
                            nIn = ix;
                        }
                    }
                }
            }
            // We got here if the ray intersects the object.
            if (bStartsInside) {
                if (-1 == nOut) {
                    // There was no clipping plane intersection going out
                    return NO_CLIP_INTERSECT;
                }
                intersection.m_fDist = fDistOut;
                m_clipPlanes[nOut].m_pln.getNormal(intersection.m_vNormal);
                intersection.m_mtl = (null != m_clipPlanes[nOut].m_mtl) ? m_clipPlanes[nOut].m_mtl : m_mtl;
            } else {
                //  Test the intersection distance - if
                //  this intersection is behind the eye, or, is not closer than a previously computed intersection.
                if ((-1 == nIn) || (fDistIn < 0.0f) || (fDistIn > intersection.m_fDist)) {
                    return NO_CLIP_INTERSECT;
                }
                intersection.m_fDist = fDistIn;
                m_clipPlanes[nIn].m_pln.getNormal(intersection.m_vNormal);
                intersection.m_mtl = (null != m_clipPlanes[nIn].m_mtl) ? m_clipPlanes[nIn].m_mtl : m_mtl;
            }

            // Update the intersection structure with information for this intersection
            ray.pointAtDistance(intersection.m_pt, intersection.m_fDist);
            intersection.m_ptObject.setValue(intersection.m_pt);
            intersection.m_vObjNormal.setValue(intersection.m_vNormal);
            intersection.m_bNatural = false;
            intersection.m_xfmObjToWorldNormal.identity();
            intersection.m_rtObj = this;
            return CLIP_INTERSECT;
        } catch (final Throwable t) {
            return NO_CLIP_INTERSECT;
        } finally {
            intersection.returnPlaneInt(plnInt);
        }
    }

    //------------------------------------------------------------------------------------------------------------------------------
    public boolean testShadow(final RayIntersection intersection, final Vector3f vLight, final float fDistLight,
                              final IRtLight light, final int nSample, final int nRandom) {
        final Quadric3fIntersection qInt = intersection.borrowQuadricInt();
        try {
            m_quadric.getIntersection(qInt, intersection.m_pt, vLight, false);
            if (qInt.m_nCode == Quadric3fIntersection.NONE_OUTSIDE || qInt.m_nCode == Quadric3fIntersection.NONE_INSIDE) {
                return false;
            }
            // We got here if the ray intersects the object.  Test the intersection distance - if
            //  this intersection is behind the eye, or, is not closer than a previously computed intersection, return.
            float fDistIn = Float.NEGATIVE_INFINITY;
            float fDistOut = Float.POSITIVE_INFINITY;
            if (qInt.m_nCode == Quadric3fIntersection.GOING_INTO) {
                // the normal in-out for convex and certain orientations of concave quadrics, where the closest intersection
                // is the going in.
                fDistIn = qInt.m_fDist1;
                fDistOut = qInt.m_fDist2;
            } else {
                // this is a concave with and out-in ordering of distances
                if (fDistOut > 0.0f) {
                    fDistIn = Float.NEGATIVE_INFINITY;
                    fDistOut = qInt.m_fDist1;
                } else {
                    fDistIn = qInt.m_fDist2;
                    fDistOut = Float.POSITIVE_INFINITY;
                }
            }
            int clip_status = lclTestClippingPlaneShadow(intersection, vLight, fDistLight, fDistIn, fDistOut);
            if (clip_status == CLIP_NEGATES_INTERSECT) {
                if (qInt.m_nCode == Quadric3fIntersection.GOING_OUT_OF) {
                    // And this is the case where the ray starts inside the quadric, but the clipping planes negate that
                    // that intersection as one of interest, so we look at the goes in
                    fDistIn = qInt.m_fDist2;
                    fDistOut = Float.POSITIVE_INFINITY;
                    clip_status = lclTestClippingPlaneShadow(intersection, vLight, fDistLight, fDistIn, fDistOut);
                    if (clip_status == CLIP_NEGATES_INTERSECT) {
                        return false;
                    } else if (clip_status == CLIP_INTERSECT) {
                        return true;
                    }
                } else {
                    return false;
                }
            } else if (clip_status == CLIP_INTERSECT) {
                // The clipping plane intersection supersedes the
                return true;
            }
            return (!(fDistIn < 0.0f)) && (!(fDistIn > fDistLight));
        } finally {
            intersection.returnQuadricInt(qInt);
        }
    }

    //------------------------------------------------------------------------------------------------------------------------------
    public int lclTestClippingPlaneShadow(@NotNull final RayIntersection intersection,
                                          final Vector3f vLight, final float fDistLight,
                                          float fDistIn, float fDistOut) {
        if ((null == m_clipPlanes) || (0 == m_clipPlanes.length)) {
            return NO_CLIP_INTERSECT;
        }
        final Plane3fIntersection plnInt = intersection.borrowPlaneInt();

        try {
            //  This is the convex polyhedra test where we compute the distance to intersections
            //  into the planes of the polyhedra, and out of the planes of the polyhedra.  If
            //  the furthest in-to is closer than the furthest out-of, then the ray is is
            //  intersecting the polyhedra.
            for (int ix = 0; ix < m_clipPlanes.length; ix++) {
                m_clipPlanes[ix].m_pln.getIntersection(plnInt, intersection.m_pt, vLight);
                if (plnInt.m_nCode == Plane3fIntersection.NONE_OUTSIDE) {
                    // This ray is parallel to and outside one of the planes of the polyhedra.
                    //  An intersection is not possible - we don't need to do anymore testing
                    return CLIP_NEGATES_INTERSECT;
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
                            return CLIP_NEGATES_INTERSECT;
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
                            return CLIP_NEGATES_INTERSECT;
                        }
                    }
                }
            }
            // We got here if the ray intersects the object.  Test the intersection distance - if
            //  this intersection is behind the eye, or, is not closer than a previously computed intersection, return.
            return ((!(fDistIn < 0.0f)) && (!(fDistIn > fDistLight))) ? CLIP_INTERSECT : NO_CLIP_INTERSECT;
        } catch (final Throwable t) {
            return NO_CLIP_INTERSECT;
        } finally {
            intersection.returnPlaneInt(plnInt);
        }
    }
}
