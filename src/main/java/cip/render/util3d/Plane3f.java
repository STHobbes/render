/*
 * Plane3f.java
 *
 * Created on September 11, 2002, 6:36 PM
 * Copyright(c) 1993-2019 Crisis in Perspective, Inc.
 *                        PO Box 1949
 *                        Hood River, OR 97031
 *                        www.crisisinperspecive.com
 */
package cip.render.util3d;

/**
 * A class representing a plane in 3D of single precision (the components are represented by <tt>float</tt> values),
 * hence the name <tt>Plane3f</tt>.
 * <p>
 * The plane is implicitly represented by the formula: <b>A</b><i>i</i> + <b>B</b><i>j</i> + <b>C</b><i>k</i> + <b>D</b> = <i>distance</i>.
 * Where <i>distance</i> is the perpendicular distance of the point <i>i</i>,<i>j</i>,<i>k</i> from the plane if the plane is normalized, and
 * the normal of the plane is <b>A</b>,<b>B</b>,<b>C</b>.  A point is on the plane when <i>distance</i> = 0.
 * <p>
 * This class implements the basic functionality for a 3D plane required for rendering and 3D graphics use.  This class is
 * patterned after and most code adapted from the <tt>CPlane3f</tt> class of the <b><i>JOEY</i></b> toolkit written and
 * distributed by Crisis in Perspective, Inc.
 *
 * @author royster.hall@gmail.com
 * @version 1.0
 * @since 1.0
 */
public final class Plane3f {
    /**
     * The <b>A</b> coefficient of the plane equation.
     */
    public float m_fA;

    /**
     * The <b>B</b> coefficient of the plane equation.
     */
    public float m_fB;

    /**
     * The <b>C</b> coefficient of the plane equation.
     */
    public float m_fC;

    /**
     * The <b>D</b> coefficient of the plane equation.
     */
    public float m_fD;

    /**
     * A pointer to tne next <tt>Plane3f</tt> in a cache if this object is cached.
     */
    public Plane3f m_next = null;

    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Creates a new uninitialized instance of <tt>Plane3f</tt>.
     */
    public Plane3f() {
    }

    /**
     * Instantiates an initialized instance of <tt>Plane3f</tt>.
     *
     * @param A The <b>A</b> coefficient of the plane equation.
     * @param B The <b>B</b> coefficient of the plane equation.
     * @param C The <b>C</b> coefficient of the plane equation.
     * @param D The <b>D</b> coefficient of the plane equation.
     */
    public Plane3f(final float A, final float B, final float C, final float D) {
        setValue(A, B, C, D);
    }

    /**
     * Instantiates an initialized instance of <tt>Plane3f</tt>.
     *
     * @param normal The normal of the plane. NOTE: the normal is not re-normalized as part of the initialization.  If there is
     *               any question about whether the normal is a unit vector, then the plane should be normalized after instantiation.
     * @param pt     A point on the plane
     */
    public Plane3f(final Vector3f normal, final Point3f pt) {
        setValue(normal, pt);
    }

    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Sets the coefficients of the plane equation..
     *
     * @param A The <b>A</b> coefficient of the plane equation.
     * @param B The <b>B</b> coefficient of the plane equation.
     * @param C The <b>C</b> coefficient of the plane equation.
     * @param D The <b>D</b> coefficient of the plane equation.
     * @return Returns this plane after the coefficients of the plane equation have been set.
     */
    public Plane3f setValue(final float A, final float B, final float C, final float D) {
        m_fA = A;
        m_fB = B;
        m_fC = C;
        m_fD = D;
        return this;
    }

    /**
     * Sets the coefficients of the plane equation for a plane that has the specified normal and passes through the specified point.
     *
     * @param normal The normal of the plane. NOTE: the normal is not re-normalized as part of the initialization.  If there is
     *               any question about whether the normal is a unit vector, then the plane should be normalized after instantiation.
     * @param pt     A point on the plane
     * @return Returns this plane after the coefficients of the plane equation have been set.
     */
    public Plane3f setValue(final Vector3f normal, final Point3f pt) {
        m_fA = normal.i;
        m_fB = normal.j;
        m_fC = normal.k;
        m_fD = -((m_fA * pt.x) + (m_fB * pt.y) + (m_fC * pt.z));
        return this;
    }

    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Normalizes the plane equation coefficients so that the normal, <b>A</b>,<b>B</b>,<b>C</b>, is a unit vector.
     *
     * @return Returns this plane after normalization.
     * @throws ZeroLengthVectorException If the current length of the normal is so close to zero that a meaningful
     *                                   normal cannot be generated.
     */
    public Plane3f normalize() throws ZeroLengthVectorException {
        final float fLength = (float) (Math.sqrt((double) ((m_fA * m_fA) + (m_fB * m_fB) + (m_fC * m_fC))));
        if (PackageConstants.isZero(fLength)) {
            throw new ZeroLengthVectorException();
        }
        final float fScale = 1.0f / fLength;
        m_fA *= fScale;
        m_fB *= fScale;
        m_fC *= fScale;
        m_fD *= fScale;
        return this;
    }

    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Gets the normal of the vector,<b>A</b>,<b>B</b>,<b>C</b>.  The normal will be a unit vector only if the plane
     * is normalized.
     *
     * @param normal The vector to be set to the normal.
     * @return The vector <tt>normal</tt> is returned.
     */
    public Vector3f getNormal(final Vector3f normal) {
        normal.i = m_fA;
        normal.j = m_fB;
        normal.k = m_fC;
        return normal;
    }

    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Tests another plane, <tt>pln</tt>, for equality with this plane.
     *
     * @param pln The plane to be tested.  This plane is unchanged.
     * @return Returns <tt>true</tt> if <tt>pln</tt> is equal to this plane (identical
     * in all coefficients), and <tt>false</tt> otherwise. NOTE: it is possible that the planes represent the same
     * physical plane but have unequal coefficients if one or both are not normalized.
     */
    public boolean equals(final Plane3f pln) {
        if (this == pln) {
            return true;
        }
        return (null != pln) && (m_fA == pln.m_fA) &&  (m_fB == pln.m_fB) && (m_fC == pln.m_fC) && (m_fD == pln.m_fD);
    }

    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Tests another object, <tt>obj</tt>, for equality with this plane.
     *
     * @param obj The object to be tested.  This object is unchanged.
     * @return Returns <tt>true</tt> if <tt>obj</tt> is both a <tt>Plane3f</tt> and is equal to this plane (identical
     * in all coefficients), and <tt>false</tt> otherwise. NOTE: it is possible that the planes represent the same
     * physical plane but have unequal coefficients if one or both are not normalized.
     */
    public boolean equals(final Object obj) {
        if ((null == obj) ||
                (getClass() != obj.getClass())) {
            return false;
        }
        return equals((Plane3f) obj);
    }

    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Get the intersection of a ray starting at point <tt>pt</tt> in the direction <tt>v</tt> with this plane.
     * The plane and the ray direction should be normalized for the correct intersection distance to be reported.
     *
     * @param plnInt The intersection.
     * @param pt     The origin of the ray.
     * @param v      The direction of the ray.
     * @return Returns the intersection, <tt>plnInt</tt>.
     */
    public Plane3fIntersection getIntersection(final Plane3fIntersection plnInt, final Point3f pt, final Vector3f v) {
        plnInt.m_plane = this;
        final float fDot = (v.i * m_fA) + (v.j * m_fB) + (v.k * m_fC);
        if (PackageConstants.isZero(fDot)) {
            plnInt.m_nCode = (pt.getDistanceTo(this) > 0.0f) ?
                    Plane3fIntersection.NONE_OUTSIDE :
                    Plane3fIntersection.NONE_INSIDE;
        } else {
            plnInt.m_fDist = -(pt.getDistanceTo(this) / fDot);
            plnInt.m_nCode = (fDot < 0.0f) ? Plane3fIntersection.GOING_INTO : Plane3fIntersection.GOING_OUT_OF;
        }
        return plnInt;
    }

    /**
     * Get the intersection of a ray, <tt>ray</tt>, with this plane.
     * The plane and the ray should be normalized for the correct intersection distance to be reported.
     *
     * @param plnInt The intersection instance to be populated with the intersection.
     * @param ray    The ray.
     * @return Returns the intersection, <tt>plnInt</tt>.
     */
    public Plane3fIntersection getIntersection(final Plane3fIntersection plnInt, final Line3f ray) {
        return getIntersection(plnInt, ray.m_ptOrg, ray.m_vDir);
    }

    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Clone this plane.
     *
     * @return Returns a clone of the plane.  The clone is NOT obtained from the object cache.
     */
    public Object clone() {
        return clonePlane3f();
    }

    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Clone this plane.
     *
     * @return Returns a clone of the plane.  The clone is NOT obtained from the object cache.
     */
    public Plane3f clonePlane3f() {
        return new Plane3f(m_fA, m_fB, m_fC, m_fD);
    }

}
