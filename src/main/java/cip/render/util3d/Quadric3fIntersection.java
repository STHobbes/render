/*
 * Quadric3fIntersection.java
 *
 * Created on November 13, 2002, 10:11 PM
 * Copyright(c) 1993-2019 Crisis in Perspective, Inc.
 *                        PO Box 1949
 *                        Hood River, OR 97031
 *                        www.crisisinperspecive.com
 */
package cip.render.util3d;

/**
 * The quadric intersection describes the results of a quadric intersection test.  For any quadric there are either two,
 * one, or no intersections for a given ray.  The intersection test provides a code that characterizes the
 * ray as either a non-intersecting ray or an intersecting ray.  For a non-intersecting ray, the ray is either fully
 * outside the quadric or fully inside the quadric.  For an intersecting ray, the two intersection distances are given
 * and the first intersection (the one with the least distance) is classified as a going in or a going out.  In the
 * case of a single intersection, it is reported as the first intersection distance and the second intersection distance
 * is set to Float.POSITIVE_INFINITY.
 * <p>
 * Quadric objects have a {@link Quadric3f#isInside(Point3f)} Method that can be used to determine whether a point is inside
 * or outside the object. This would typically be used only for the camera to decide whether it has been moved into an object.
 * Otherwise, the logic of your call to the {@link Quadric3f#getIntersection(Quadric3fIntersection, Line3f, boolean)} should be
 * used to interpret the intersection:
 *
 * </p>
 * <p>
 * From this information we can deduce the whether the point is inside (first intersection ray going in, dist 1
 * negative, dist 2 positive; or first intersection ray going out, dist 1 is positive or dist 2 is negative) or
 * outside (first intersection ray going out, dist 1 negative, dist 2 positive; or first intersection ray going in
 * and, dist 1 is positive or dist 2 is negative).
 *
 * @author royster.hall@gmail.com
 * @version 1.0
 * @since 1.0
 */
public class Quadric3fIntersection {
    /**
     * A pointer to tne next <tt>Quadric3fIntersection</tt> in a cache if this object is cached.
     */
    public Quadric3fIntersection m_next = null;
    /**
     * The code for no intersection with the quadric and the ray being outside the quadric.
     */
    public static final int NONE_OUTSIDE = 1;

    /**
     * The code for no intersection with the quadric and the ray being inside the quadric.
     */
    public static final int NONE_INSIDE = 2;

    /**
     * The first intersection (at distance <tt>m_fDist1</tt>) is going into the quadric.
     */
    public static final int GOING_INTO = 3;

    /**
     * The second intersection (at distance <tt>m_fDist2</tt>) is going out of the quadric.
     */
    public static final int GOING_OUT_OF = 4;


    /**
     * The smallest distance to the intersection (the first intersection for an infinite directed line) from the origin
     * of the ray if the code is
     * {@link cip.render.util3d.Quadric3fIntersection#GOING_INTO} (the only possibility for a convex quadric); or
     * {@link cip.render.util3d.Quadric3fIntersection#GOING_OUT_OF} (which happens with non-convex quadrics).  Otherwise,
     * this field is meaningless.
     * <p>
     * NOTE: if the distance is negative, it implies the intersection is effectively behind the start
     * of the ray. HOWEVER, you should observe the logic of whether the ray starts inside or outside the object as numerical
     * inconsistencies (round off) make it arbitrary whether the start of reflections or refractions from the current object
     * numerically start inside or outside the quadric.
     * </p>
     */
    public float m_fDist1;

    /**
     *
     */
    public Line3f m_int1 = new Line3f();

    /**
     * The largest distance to the intersection (the second intersection for an infinite directed line) from the origin
     * of the ray if the code is
     * {@link cip.render.util3d.Quadric3fIntersection#GOING_INTO} or
     * {@link cip.render.util3d.Quadric3fIntersection#GOING_OUT_OF}.  Otherwise, this field is
     * meaningless.
     * <p>
     * NOTE: if the distance is negative, it implies the intersection is effectively behind the start
     * of the ray. HOWEVER, you should observe the logic of whether the ray starts inside or outside the object as numerical
     * inconsistencies (round off) make it arbitrary whether the start of reflections or refractions from the current object
     * numerically start inside or outside the quadric.
     * </p>
     */
    public float m_fDist2;

    /**
     * The code describing the type of intersection.
     */
    public int m_nCode;

    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Creates a new uninitialized instance of <tt>Quadric3fIntersection</tt>.
     */
    public Quadric3fIntersection() {
    }

}
