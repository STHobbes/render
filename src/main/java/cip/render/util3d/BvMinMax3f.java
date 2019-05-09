/*
 * BvMinMax3f.java
 *
 * Created on November 17, 2002, 8:59 PM
 * Copyright(c) 1993-2019 Crisis in Perspective, Inc.
 *                        PO Box 1949
 *                        Hood River, OR 97031
 *                        www.crisisinperspecive.com
 */
package cip.render.util3d;

/**
 * The description of a 3D min-max volume.  The min max box is a highly optimized 6 plane convex polyhedra where
 * each of the planes is axis aligned.  This allows us to easily: construct; union points or other min-max volumes;
 * and test for intersection with this bounding volume.
 *
 * @author royster.hall@gmail.com
 * @version 1.0
 * @since 1.0
 */
public class BvMinMax3f {
    /**
     * A pointer to tne next <tt>Bv3fIntersection</tt> in a cache if this object is cached.
     */
    public BvMinMax3f m_next = null;
    //-------------------------------------------------------------------------------------------------------------------------
    float m_xMin = Float.POSITIVE_INFINITY;
    float m_xMax = Float.NEGATIVE_INFINITY;
    float m_yMin = Float.POSITIVE_INFINITY;
    float m_yMax = Float.NEGATIVE_INFINITY;
    float m_zMin = Float.POSITIVE_INFINITY;
    float m_zMax = Float.NEGATIVE_INFINITY;

    /**
     * Creates a new instance of <tt>BvMinMax3f</tt> initialized to empty.
     */
    public BvMinMax3f() {
    }

    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Set the bounding volume to an empty volume.
     *
     * @return Returns this bounding volume after it has been set to empty.
     */
    public BvMinMax3f setEmpty() {
        m_xMin = Float.POSITIVE_INFINITY;
        m_xMax = Float.NEGATIVE_INFINITY;
        m_yMin = Float.POSITIVE_INFINITY;
        m_yMax = Float.NEGATIVE_INFINITY;
        m_zMin = Float.POSITIVE_INFINITY;
        m_zMax = Float.NEGATIVE_INFINITY;
        return this;
    }

    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Set the bounding volume to an infinite extent volume.
     *
     * @return Returns this bounding volume after it has been set to an infinite extent volume.
     */
    public BvMinMax3f setInfinite() {
        m_xMin = Float.NEGATIVE_INFINITY;
        m_xMax = Float.POSITIVE_INFINITY;
        m_yMin = Float.NEGATIVE_INFINITY;
        m_yMax = Float.POSITIVE_INFINITY;
        m_zMin = Float.NEGATIVE_INFINITY;
        m_zMax = Float.POSITIVE_INFINITY;
        return this;
    }

    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Tests whether the volume is empty.
     *
     * @return Returns <tt>true</tt> if the volume is empty and <tt>false</tt> if the volume is not empty.
     */
    public boolean isEmpty() {
        return (m_xMin > m_xMax);
    }

    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Union another min-max volume with this one.
     *
     * @param mm The volume to be unioned with this one.
     * @return This volume after the union has been performed.
     */
    public BvMinMax3f union(final BvMinMax3f mm) {
        // expand this volume to include any of the other volume that is outside this one.
        if (mm.m_xMin < m_xMin) {
            m_xMin = mm.m_xMin;
        }
        if (mm.m_xMax > m_xMax) {
            m_xMax = mm.m_xMax;
        }
        if (mm.m_yMin < m_yMin) {
            m_yMin = mm.m_yMin;
        }
        if (mm.m_yMax > m_yMax) {
            m_yMax = mm.m_yMax;
        }
        if (mm.m_zMin < m_zMin) {
            m_zMin = mm.m_zMin;
        }
        if (mm.m_zMax > m_zMax) {
            m_zMax = mm.m_zMax;
        }
        return this;
    }

    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Union a point with this bounding volume.
     *
     * @param pt The point to be unioned with this bounding volume.
     * @return This volume after the union has been performed.
     */
    public BvMinMax3f union(final Point3f pt) {
        // expand this volume to include the point
        if (pt.x < m_xMin) {
            m_xMin = pt.x;
        }
        if (pt.x > m_xMax) {
            m_xMax = pt.x;
        }
        if (pt.y < m_yMin) {
            m_yMin = pt.y;
        }
        if (pt.y > m_yMax) {
            m_yMax = pt.y;
        }
        if (pt.z < m_zMin) {
            m_zMin = pt.z;
        }
        if (pt.z > m_zMax) {
            m_zMax = pt.z;
        }
        return this;
    }

    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Union an array of points with this bounding volume.
     *
     * @param pts The array of points to be unioned with this bounding volume.
     * @return This volume after the union has been performed.
     */
    public BvMinMax3f union(final Point3f[] pts) {
        for (Point3f pt : pts) {
            union(pt);
        }
        return this;
    }

    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Get the intersection of the ray with the bounding volume.  In this case, the relevant information is whether the
     * ray hits the bounding volume and the distances to entry and exit.  The distances allow for bounding volume sorting
     * before object intersections are processed.  Intersection points and normals are unimportant because the volume
     * is never actually rendered.
     *
     * @param bvInt The bounding volume intersection that will be filled with intersection information.
     * @param ray   The ray to be intersected with the bounding volume
     * @return Returns <tt>true</tt> if the ray intersects or starts inside the bounding volume, and <tt>false</tt>
     * if the ray does not intersect the bounding volume.
     */
    public boolean getIntersection(final Bv3fIntersection bvInt, final Line3f ray) {
        // This is the standard convex polyhedra test highly optimized for 6 axis-aligned planes.  We
        //  expect most rays to miss most objects so the test is designed to eliminate the BV as
        //  quickly as possible and to drop out after minimal computation.
        bvInt.m_nCode = Bv3fIntersection.NO_INTERSECTION;
        bvInt.m_fDistIn = Float.NEGATIVE_INFINITY;
        bvInt.m_fDistOut = Float.POSITIVE_INFINITY;
        float fTmp;
        if (isEmpty()) return false;
        // test on X
        if (PackageConstants.isZero(ray.m_vDir.i)) {    // parallel to YZ
            if ((ray.m_ptOrg.x < m_xMin) || (ray.m_ptOrg.x > m_xMax)) {
                return false;
            }
        } else if (ray.m_vDir.i < 0.0f) {
            if (ray.m_ptOrg.x < m_xMin) {
                return false;
            }   // goes out behind the start
            bvInt.m_fDistIn = (m_xMax - ray.m_ptOrg.x) / ray.m_vDir.i;
            bvInt.m_fDistOut = (m_xMin - ray.m_ptOrg.x) / ray.m_vDir.i;
        } else {
            if (ray.m_ptOrg.x > m_xMax) {
                return false;
            }   // goes out behind the start
            bvInt.m_fDistIn = (m_xMin - ray.m_ptOrg.x) / ray.m_vDir.i;
            bvInt.m_fDistOut = (m_xMax - ray.m_ptOrg.x) / ray.m_vDir.i;
        }
        // test on Y
        if (PackageConstants.isZero(ray.m_vDir.j)) {    // parallel to XZ
            if ((ray.m_ptOrg.y < m_yMin) || (ray.m_ptOrg.y > m_yMax)) {
                return false;
            }
        } else if (ray.m_vDir.j < 0.0f) {
            if (ray.m_ptOrg.y < m_yMin) {
                return false;
            }   // goes out behind the start
            if ((fTmp = (m_yMax - ray.m_ptOrg.y) / ray.m_vDir.j) > bvInt.m_fDistIn) {
                bvInt.m_fDistIn = fTmp;
            }
            if ((fTmp = (m_yMin - ray.m_ptOrg.y) / ray.m_vDir.j) < bvInt.m_fDistOut) {
                bvInt.m_fDistOut = fTmp;
            }
        } else {
            if (ray.m_ptOrg.y > m_yMax) {
                return false;
            }   // goes out behind the start
            if ((fTmp = (m_yMin - ray.m_ptOrg.y) / ray.m_vDir.j) > bvInt.m_fDistIn) {
                bvInt.m_fDistIn = fTmp;
            }
            if ((fTmp = (m_yMax - ray.m_ptOrg.y) / ray.m_vDir.j) < bvInt.m_fDistOut) {
                bvInt.m_fDistOut = fTmp;
            }
        }
        if (bvInt.m_fDistIn > bvInt.m_fDistOut) {
            return false;
        }
        // test on Z
        if (PackageConstants.isZero(ray.m_vDir.k)) {    // parallel to XY
            if ((ray.m_ptOrg.z < m_zMin) || (ray.m_ptOrg.z > m_zMax)) {
                return false;
            }
        } else if (ray.m_vDir.k < 0.0f) {
            if (ray.m_ptOrg.z < m_zMin) {
                return false;
            }   // goes out behind the start
            if ((fTmp = (m_zMax - ray.m_ptOrg.z) / ray.m_vDir.k) > bvInt.m_fDistIn) {
                bvInt.m_fDistIn = fTmp;
            }
            if ((fTmp = (m_zMin - ray.m_ptOrg.z) / ray.m_vDir.k) < bvInt.m_fDistOut) {
                bvInt.m_fDistOut = fTmp;
            }
        } else {
            if (ray.m_ptOrg.z > m_zMax) {
                return false;
            }   // goes out behind the start
            if ((fTmp = (m_zMin - ray.m_ptOrg.z) / ray.m_vDir.k) > bvInt.m_fDistIn) {
                bvInt.m_fDistIn = fTmp;
            }
            if ((fTmp = (m_zMax - ray.m_ptOrg.z) / ray.m_vDir.k) < bvInt.m_fDistOut) {
                bvInt.m_fDistOut = fTmp;
            }
        }
        if (bvInt.m_fDistIn > bvInt.m_fDistOut) {
            return false;
        }
        // This is an intersection, check starts inside or starts outside.
        bvInt.m_nCode = (bvInt.m_fDistIn < 0.0f) ? Bv3fIntersection.INTERSECTS_INSIDE : Bv3fIntersection.INTERSECTS_OUTSIDE;
        return true;
    }

}
