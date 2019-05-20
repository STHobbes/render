/*
 * Bv3fIntersection.java
 *
 * Created on November 17, 2002, 8:50 PM
 * Copyright(c) 1993-2019 Crisis in Perspective, Inc.
 *                        PO Box 1949
 *                        Hood River, OR 97031
 *                        www.crisisinperspecive.com
 */
package cip.render.util3d;

/**
 * The bounding volume intersection describes the results of a bounding volume intersection test.  For a bounding
 * volume there a 3 possibilities:
 * <ul>
 * <li><tt>{@link #NO_INTERSECTION}</tt> - the ray starts outside the volume and does not intersect it;</li>
 * <li><tt>{@link #INTERSECTS_INSIDE}</tt> - the ray starts
 * inside the bounding volume and, obviously, goes out somewhere;</li>
 * <li><tt>{@link #INTERSECTS_OUTSIDE}</tt> - or the ray starts outside the bounding volume and intersects it.</li>
 * </ul>
 * The intersection test provides a code that characterizes the
 * ray as either a non-intersecting ray or an intersecting ray.  For an intersecting ray, the two intersection
 * distances are given and the code clarifies whether the ray starts inside the bounding volume (the distance in
 * is negative if the ray starts inside).
 *
 * @author royster.hall@gmail.com
 * @version 1.0
 * @since 1.0
 */
public class Bv3fIntersection {

    /**
     * The ray starts outside the volume and does not intersect it.
     */
    public static final int NO_INTERSECTION = 1;
    /**
     * The ray starts inside the bounding volume and, obviously, goes out somewhere.
     */
    public static final int INTERSECTS_INSIDE = 2;
    /**
     * The ray starts outside the bounding volume and intersects it.
     */
    public static final int INTERSECTS_OUTSIDE = 3;

    /**
     * A pointer to tne next <tt>Bv3fIntersection</tt> in a cache if this object is cached.
     */
    public Bv3fIntersection m_next = null;

    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * The distance until the ray enters the bounding volume.
     */
    public float m_fDistIn;

    /**
     * The distance until the ray leaves the bounding volume.
     */
    public float m_fDistOut;

    /**
     * The code describing the type of intersection.
     */
    public int m_nCode;

    /**
     * The object that this is the min-max intersection of.
     */
    public Object m_obj;

    /**
     * Creates a new instance of <tt>Bv3fIntersection</tt>
     */
    public Bv3fIntersection() {
    }

}
