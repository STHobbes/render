/*
 * Plane3fIntersection.java
 *
 * Created on September 11, 2002, 6:38 PM
 * Copyright(c) 1993-2019 Crisis in Perspective, Inc.
 *                        PO Box 1949
 *                        Hood River, OR 97031
 *                        www.crisisinperspecive.com
 */
package cip.render.util3d;

/**
 * A class representing a the intersection of a ray with a plane in 3D of single precision (the components are represented by
 * <tt>float</tt> values), hence the name <tt>Plane3fIntersection</tt>.  This object can be used as both a typical object
 * instantiated using the <tt>new</tt> and reclaimed by the garbage collector when the are no longer references to the object,
 * and/or a cached object which is borrowed from a dynamically growing cache of <tt>Plane3f</tt> objects.
 *
 * @author royster.hall@gmail.com
 * @version 1.0
 * @since 1.0
 */
public final class Plane3fIntersection {
    /**
     * The code for no intersection with the plane and the ray being outside the plane.
     * This means that the ray is essentially parallel to the plane and the origin of
     * the ray is on the outside of (a positive distance from) the plane.  Essentially
     * parallel means that the intersection distance is essentially infinite (within
     * floating point tolerances) and, therefore, meaningless.
     */
    public static final int NONE_OUTSIDE = 1;

    /**
     * This means that the ray is essentially parallel to the plane and the origin of
     * the ray is on the inside of (a negative distance from) the plane.  Essentially
     * parallel means that the intersection distance is essentially infinite (within
     * floating point tolerances) and, therefore, meaningless.
     */
    public static final int NONE_INSIDE = 2;

    /**
     * The origin of the ray is outside of (a positive distance from) the plane, and
     * the intersection is for the ray going into the plane.
     */
    public static final int GOING_INTO = 3;

    /**
     * The origin of the ray is inside of (a negative distance from) the plane, and
     * the intersection is for the ray going out of the plane.
     */
    public static final int GOING_OUT_OF = 4;

    public Plane3fIntersection m_next = null;

    //------------------------------------------------------------------------------------------------------------------------------

    /**
     * The distance to the intersection from the origin of the ray if the code is
     * {@link cip.render.util3d.Plane3fIntersection#GOING_INTO} or
     * {@link cip.render.util3d.Plane3fIntersection#GOING_OUT_OF}.  Otherwise, this field is
     * meaningless.  NOTE: if the distance is negative, it means the intersection is effectively behind the start
     * of the ray.
     */
    public float m_fDist;

    /**
     * The code describing the type of intersection.
     */
    public int m_nCode;

    /**
     * The plane being intersected - this is always a reference and the plane is not owned by this intersection.
     */
    public Plane3f m_plane;

    //------------------------------------------------------------------------------------------------------------------------------

    /**
     * Creates a new uninitialized instance of <tt>Plane3fIntersection</tt>.
     */
    public Plane3fIntersection() {
    }

    //------------------------------------------------------------------------------------------------------------------------------
    public Plane3fIntersection setValue(Plane3fIntersection plnInt) {
        m_fDist = plnInt.m_fDist;
        m_nCode = plnInt.m_nCode;
        m_plane = plnInt.m_plane;
        return this;
    }
    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Clone this plane.
     *
     * @return Returns a clone of the plane.  The clone is NOT obtained from the object cache.
     */
    public Object clone() {
        return clonePlane3fIntersection();
    }

    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Clone this plane.
     *
     * @return Returns a clone of the plane.  The clone is NOT obtained from the object cache.
     */
    public Plane3fIntersection clonePlane3fIntersection() {
        return new Plane3fIntersection().setValue(this);
    }
}
