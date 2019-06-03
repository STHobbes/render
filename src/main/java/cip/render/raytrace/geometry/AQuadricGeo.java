package cip.render.raytrace.geometry;

import cip.render.raytrace.RayIntersection;
import cip.render.raytrace.interfaces.IRtLight;
import cip.render.util3d.*;
import org.jetbrains.annotations.NotNull;

/**
 * This is an abstract class for a quadric object geometry. Quadric geometries all wrap a {@link cip.render.util3d.Quadric3f}
 * object with XML load and ray-tracing capabilities. The ray-tracing capabilities are pretty much the same for all quadrics, so
 * most of the code is here.
 */
abstract class AQuadricGeo extends AGeometry {
    protected static final String XML_TAG_RADIUS = "radius";

    // The instance definition
    final Quadric3f m_quadric = new Quadric3f();

    AQuadricGeo() {
        super();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IRtGeometry interface implementation                                                                                       //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean isConvex() {
        return m_quadric.isConvex();
    }

    @Override
    public boolean isInside(Point3f pt) {
        return m_quadric.isInside(pt);
    }

    /**
     * Tests a ray for an intersection with a sphere.  See {@link cip.render.raytrace.interfaces.IRtGeometry}
     * description of getRayIntersection().
     */
    @Override
    public boolean getRayIntersection(@NotNull final RayIntersection intersection, @NotNull final Line3f ray,
                                      final boolean bStartsInside, final int nSample, final int nRandom) {
        final Quadric3fIntersection qInt = intersection.borrowQuadricInt();
        try {
            m_quadric.getIntersection(qInt, ray, bStartsInside);
            if (qInt.m_nCode == Quadric3fIntersection.NONE_OUTSIDE || qInt.m_nCode == Quadric3fIntersection.NONE_INSIDE) {
                return false;
            }
            // We got here if the ray intersects the object.  Test the intersection distance - if
            //  this intersection is behind the eye, or, is not closer than a previously computed intersection, return.
            float fDistTmp;
            if (qInt.m_nCode == Quadric3fIntersection.GOING_INTO) {
                // the normal in-out for convex and certain orientations of concave quadrics
                fDistTmp = bStartsInside ? qInt.m_fDist2 : qInt.m_fDist1;
            } else {
                // this is a concave with and out-in ordering of distances
                fDistTmp = bStartsInside ? qInt.m_fDist1 : qInt.m_fDist2;

            }
            if (!bStartsInside && ((fDistTmp < 0.0f) || (fDistTmp > intersection.m_fDist))) {
                return false;
            }

            // Update the intersection structure with information for this intersection
            intersection.m_fDist = fDistTmp;
            ray.pointAtDistance(intersection.m_pt, fDistTmp);
            m_quadric.getNormal(intersection.m_vNormal, intersection.m_pt);
            intersection.m_bNatural = false;
            intersection.m_ptObject.setValue(intersection.m_pt);
            intersection.m_vObjNormal.setValue(intersection.m_vNormal);
            intersection.m_xfmObjToWorldNormal.identity();
            intersection.m_mtl = m_mtl;
            intersection.m_rtObj = this;
            return true;
        } finally {
            intersection.returnQuadricInt(qInt);
        }
    }

    @Override
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
            //  this intersection is behind the eye, or, is not closer than a previously computed intersection, return.
            float fDistTmp = (qInt.m_nCode == Quadric3fIntersection.GOING_INTO) ? qInt.m_fDist1 : qInt.m_fDist2;
            return (!(fDistTmp < 0.0f)) && (!(fDistTmp > fDistLight));
        } finally {
            intersection.returnQuadricInt(qInt);
        }
    }
}
