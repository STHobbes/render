/*
 * Quadric3f.java
 *
 * Created on November 15, 2002, 9:00 PM
 * Copyright(c) 1993-2019 Crisis in Perspective, Inc.
 *                        PO Box 1949
 *                        Hood River, OR 97031
 *                        www.crisisinperspecive.com
 */
package cip.render.util3d;

/**
 * A class representing a a family of quadrics centered at 0,0,0 in object space and with the Z axis being the primary axis
 * of the shape.  The quadric is described in 3D of single precision (the components are represented by <tt>float</tt> values),
 * hence the name <tt>Quadric3f</tt>.
 * <p>
 * A quadric is implicitly represented by the formula:
 * <p style="text-align:center">
 * q<sub>1</sub>i<sup>2</sup> + q<sub>2</sub>j<sup>2</sup> + q<sub>3</sub>k<sup>2</sup>
 * + q<sub>4</sub>xy + q<sub>5</sub>yz + q<sub>6</sub>zx + q<sub>7</sub>i + q<sub>8</sub>j
 * + q<sub>9</sub>k +q<sub>0</sub> = 0
 * <p>
 * This representation allows arbitrary orientation and positioning of quadric shapes.  If we limit our consideration to quadric
 * shapes that are centered at the origin and have the Z axis as the major axis of the quadric, the families of quadric shapes are
 * represented as:
 * <blockquote><p>
 * Elliptical Cylinder about k:&nbsp;&nbsp; q<sub>1</sub> = 1/r<sub>i</sub><sup>2</sup>
 * ;&nbsp;&nbsp; q<sub>2</sub> = 1/r<sub>j</sub><sup>2</sup> ;&nbsp;&nbsp; q<sub>0</sub>  = -1
 * <p>
 * Ellipsoid:&nbsp; q<sub>1</sub> = 1/r<sub>i</sub><sup>2</sup> ;&nbsp;&nbsp; q<sub>2</sub> = 1/r<sub>j</sub><sup>2</sup> ;&nbsp;&nbsp;
 * q<sub>3</sub> = 1/r<sub>k</sub><sup>2</sup>  ;&nbsp;&nbsp; q<sub>0</sub> = -1
 * <p>
 * Elliptic Paraboloid about k:&nbsp;&nbsp;&nbsp; q<sub>1</sub> = 1/r<sub>i</sub><sup>2</sup>
 * ;&nbsp;&nbsp; q<sub>2</sub> = 1/r<sub>j</sub><sup>2</sup> ;&nbsp;&nbsp; q<sub>9</sub>  = -1/height ;
 * <p>
 * Elliptic Cone about k:&nbsp;&nbsp;&nbsp; q<sub>1</sub> = 1/r<sub>i</sub><sup>2</sup>
 * ;&nbsp;&nbsp; q<sub>2</sub> = 1/r<sub>j</sub><sup>2</sup> ;&nbsp;&nbsp; q<sub>3</sub> = -1/height<sup>2</sup> ;
 * <p>
 * Hyperboloid about k:&nbsp;&nbsp;&nbsp; q<sub>1</sub> = 1/r<sub>i</sub><sup>2</sup> ;&nbsp;&nbsp;
 * q<sub>2</sub> = 1/r<sub>j</sub><sup>2</sup> ;&nbsp;&nbsp; q<sub>3</sub> = -1/r<sub>k</sub><sup>2</sup> ;&nbsp;&nbsp; q<sub>0</sub> = -1
 * <p>
 * Hyperbolic Paraboloid about j:&nbsp;&nbsp;&nbsp; q<sub>1</sub> = 1/r<sub>i</sub><sup>2</sup> ;&nbsp;&nbsp;
 * q<sub>2</sub> = -1/r<sub>j</sub><sup>2</sup> ;&nbsp;&nbsp;&nbsp; q<sub>9</sub> = -1/r<sub>k</sub>
 * </blockquote><p>
 * We intersect a ray starting at point (i<sub>o</sub>,j<sub>o</sub>,k<sub>o</sub>) and extending in direction (i,j,k),
 * the points along which are expressed as (i<sub>t</sub>,j<sub>t</sub>,i<sub>t</sub>) = (i<sub>o</sub>,j<sub>o</sub>,k<sub>o</sub>) +
 * t(i,j,k) where t is the distance from the origin of the ray (our standard parametric representation of a ray) by
 * doing a bunch of algebraic gymnastics until we can express the combined equations as:
 * <blockquote><p>
 * t<sup>2</sup>(q<sub>1</sub>i<sup>2</sup> + q<sub>2</sub>j<sup>2</sup> + q<sub>3</sub>k<sup>2</sup> +
 * q<sub>4</sub> ij + q<sub>5</sub>jk + q<sub>6</sub>ki) +
 * <p>
 * t(2(q<sub>1</sub>i<sub>o</sub>i + q<sub>2</sub>j<sub>o</sub>j + q<sub>3</sub>k<sub>o</sub>k)+
 * q<sub>4</sub>(i<sub>o</sub>j + j<sub>o</sub>i) + q<sub>5</sub>(j<sub>o</sub>k
 * + k<sub>o</sub>j) + q<sub>6</sub>(k<sub>o</sub>i + i<sub>o</sub>k) + q<sub>7</sub>i
 * + q<sub>8</sub>j + q<sub>9</sub>k) +
 * <p>
 * (q<sub>1</sub>i<sub>o</sub><sup>2</sup> + q<sub>2</sub>j<sub>o</sub><sup>2</sup>
 * + q<sub>3</sub>k<sub>o</sub><sup>2</sup> + q<sub>4</sub> i<sub>o</sub>j<sub>o</sub>+
 * q<sub>5</sub>j<sub>o</sub>k<sub>o</sub> + q<sub>6</sub>k<sub>o</sub>i<sub>o</sub>
 * + q<sub>7</sub>i<sub>o</sub> + q<sub>8</sub>j<sub>o</sub> + q<sub>9</sub>k<sub>o</sub>
 * + q<sub>0</sub>)&nbsp; =&nbsp; 0
 * </blockquote><p>
 * or, put another way:
 * <blockquote><p>
 * at2 + bt + c = 0
 * </blockquote><p>
 * where
 * <blockquote><p>
 * a = q<sub>1</sub>i<sup>2</sup> + q<sub>2</sub>j<sup>2</sup> + q<sub>3</sub>k<sup>2</sup>
 * + q<sub>4</sub> ij + q<sub>5</sub>jk + q<sub>6</sub>ki
 * <p>
 * b = 2(q<sub>1</sub>i<sub>o</sub>i + q<sub>2</sub>j<sub>o</sub>j + q<sub>3</sub>k<sub>o</sub>k)+
 * q<sub>4</sub>(i<sub>o</sub>j + j<sub>o</sub>i) + q<sub>5</sub>(j<sub>o</sub>k
 * + k<sub>o</sub>j) + q<sub>6</sub>(k<sub>o</sub>i + i<sub>o</sub>k) + q<sub>7</sub>i
 * + q<sub>8</sub>j + q<sub>9</sub>k
 * <p>
 * c = q<sub>1</sub>i<sub>o</sub><sup>2</sup> + q<sub>2</sub>j<sub>o</sub><sup>2</sup>
 * + q<sub>3</sub>k<sub>o</sub><sup>2</sup> + q<sub>4</sub> i<sub>o</sub>j<sub>o</sub>+
 * q<sub>5</sub>j<sub>o</sub>k<sub>o</sub> + q<sub>6</sub>k<sub>o</sub>i<sub>o</sub>
 * + q<sub>7</sub>i<sub>o</sub> + q<sub>8</sub>j<sub>o</sub> + q<sub>9</sub>k<sub>o</sub>
 * + q<sub>0</sub>
 * </blockquote><p>
 * and we can use the quadratic formula to find the roots for t. Note that for our special case of
 * location and orientation, these simlify considerably to:
 * <blockquote><p>
 * a = q<sub>1</sub>i<sup>2</sup> + q<sub>2</sub>j<sup>2</sup> + q<sub>3</sub>k<sup>2</sup>
 * <p>
 * b = 2(q<sub>1</sub>i<sub>o</sub>i + q<sub>2</sub>j<sub>o</sub>j + q<sub>3</sub>k<sub>o</sub>k)
 * + q<sub>9</sub>k
 * <p>
 * c = q<sub>1</sub>i<sub>o</sub><sup>2</sup> + q<sub>2</sub>j<sub>o</sub><sup>2</sup>
 * + q<sub>3</sub>k<sub>o</sub><sup>2</sup> + q<sub>9</sub>k<sub>o</sub>
 * + q<sub>0</sub>
 * </blockquote><p>
 * To find the normal at a point on the surface, we can take the partials of the
 * formula and express the normal (i<sub>n</sub>,,j<sub>n</sub>,k<sub>n</sub>) as:
 * <blockquote><p>
 * u = 2q<sub>1</sub>i + q<sub>4</sub>j + q<sub>6</sub>k + q<sub>7</sub>
 * <p>
 * v = 2q<sub>2</sub>j + q<sub>4</sub>i + q<sub>5</sub>k + q<sub>8</sub>
 * <P>
 * w = 2q<sub>3</sub>k + q<sub>5</sub>j + q<sub>6</sub>i + q<sub>9</sub>
 * <p>
 * i<sub>n</sub> = u / sqrt(u<sup>2</sup> + v<sup>2</sup> + w<sup>2</sup>)
 * ;&nbsp; j<sub>n</sub> = v / sqrt(u<sup>2</sup> + v<sup>2</sup> + w<sup>2</sup>)
 * ;&nbsp; k<sub>n</sub> = w / sqrt(u<sup>2</sup> + v<sup>2</sup> + w<sup>2</sup>)
 * </blockquote><p>
 * As in the case of the coefficients of the quadratic form, the U, V, and W for normal computation also
 * simplify substantially to:
 * <blockquote><p>
 * u = 2q<sub>1</sub>i
 * <p>
 * v = 2q<sub>2</sub>j
 * <P>
 * w = 2q<sub>3</sub>k + q<sub>9</sub>
 * </blockquote>
 *
 *
 * @author royster.hall@gmail.com
 * @version 1.0
 * @since 1.0
 */
public class Quadric3f {
    /**
     * A pointer to tne next <tt>Quadric3f</tt> in a cache if this object is cached.
     */
    public Quadric3f m_next = null;
    /**
     * The q<sub>0</sub> coefficient of the quadric formulation.
     */
    public float m_q0 = -1.0f;

    /**
     * The q<sub>1</sub> coefficient of the quadric formulation.
     */
    public float m_q1 = 1.0f;

    /**
     * The q<sub>2</sub> coefficient of the quadric formulation.
     */
    public float m_q2 = 1.0f;

    /**
     * The q<sub>3</sub> coefficient of the quadric formulation.
     */
    public float m_q3 = 1.0f;

    /**
     * The q<sub>9</sub> coefficient of the quadric formulation.
     */
    public float m_q9 = 0.0f;

    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Creates a new instance of <tt>Quadric3f</tt> initialized to a unit sphere.
     */
    public Quadric3f() {
    }

    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Initializes the coefficients of the quadric formula for an
     * elliptical cylinder about k as:&nbsp;&nbsp; q<sub>1</sub> = 1/r<sub>i</sub><sup>2</sup>
     * ;&nbsp;&nbsp; q<sub>2</sub> = 1/r<sub>j</sub><sup>2</sup> ;&nbsp;&nbsp; q<sub>0</sub>  = -1
     * <p>
     * The eliptical cone is an ellipse of radius <tt>fRx</tt>, <tt>fRy</tt> centered at 0,0 where it cuts through
     * the XY plane.  It extends infinitely in Z.
     *
     * @param fRx The i radius.
     * @param fRy The j radius.
     * @return Returns this quadric after initialization.
     */
    public Quadric3f setEllipticalCylinder(final float fRx, final float fRy) {
        if (PackageConstants.isZero(fRx) || PackageConstants.isZero(fRy)) {
            throw new IllegalArgumentException();
        }
        m_q0 = -1.0f;
        m_q1 = 1.0f / (fRx * fRx);
        m_q2 = 1.0f / (fRy * fRy);
        m_q3 = 0.0f;
        m_q9 = 0.0f;
        return this;
    }
    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Initializes the coefficients of the quadric formula for an
     * ellipsoid as:&nbsp; q<sub>1</sub> = 1/r<sub>i</sub><sup>2</sup> ;&nbsp;&nbsp; q<sub>2</sub> = 1/r<sub>j</sub><sup>2</sup> ;&nbsp;&nbsp;
     * q<sub>3</sub> = 1/r<sub>k</sub><sup>2</sup>  ;&nbsp;&nbsp; q<sub>0</sub> = -1
     * <p>
     * The ellipsoid is centered at 0,0,0.
     *
     * @param fRx The i radius of the ellipsoid.
     * @param fRy The j radius of the ellipsoid.
     * @param fRz The k radius of the ellipsoid.
     * @return Returns this quadric after initialization.
     */
    public Quadric3f setEllipsoid(final float fRx, final float fRy, final float fRz) {
        if (PackageConstants.isZero(fRx) || PackageConstants.isZero(fRy) || PackageConstants.isZero(fRz)) {
            throw new IllegalArgumentException();
        }
        m_q0 = -1.0f;
        m_q1 = 1.0f / (fRx * fRx);
        m_q2 = 1.0f / (fRy * fRy);
        m_q3 = 1.0f / (fRz * fRz);
        m_q9 = 0.0f;
        return this;
    }
    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Initializes the coefficients of the quadric formula for an
     * elliptic paraboloid about k:&nbsp;&nbsp;&nbsp; q<sub>1</sub> = 1/r<sub>i</sub><sup>2</sup>
     * ;&nbsp;&nbsp; q<sub>2</sub> = 1/r<sub>j</sub><sup>2</sup> ;&nbsp;&nbsp; q<sub>9</sub>  = -1/height
     * <p>
     * The elliptical paraboloid has an elliptical cross section centered on the Z axis.  The elliptic cross section has i and j
     * radii of <tt>fRx</tt>,<tt>fRy</tt> where k = <tt>fHeight</tt> and k = -<tt>fHeight</tt>.
     *
     * @param fRx     The i radius of the cross section where k = <tt>fHeight</tt> and k = -<tt>fHeight</tt>.
     * @param fRy     The j radius of the cross section where k = <tt>fHeight</tt> and k = -<tt>fHeight</tt>.
     * @param fHeight The k distance from the origin at which the elliptic cross section has i and j
     *                radii of <tt>fRx</tt>,<tt>fRy</tt>
     * @return Returns this quadric after initialization.
     */
    public Quadric3f setEllipticalParaboloid(final float fRx, final float fRy, final float fHeight) {
        if (PackageConstants.isZero(fRx) || PackageConstants.isZero(fRy) || PackageConstants.isZero(fHeight)) {
            throw new IllegalArgumentException();
        }
        m_q0 = 0.0f;
        m_q1 = 1.0f / (fRx * fRx);
        m_q2 = 1.0f / (fRy * fRy);
        m_q3 = 0.0f;
        m_q9 = -1.0f / fHeight;
        return this;
    }
    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Initializes the coefficients of the quadric formula for an
     * elliptic cone about k as:&nbsp;&nbsp;&nbsp; q<sub>1</sub> = 1/r<sub>i</sub><sup>2</sup>
     * ;&nbsp;&nbsp; q<sub>2</sub> = 1/r<sub>j</sub><sup>2</sup> ;&nbsp;&nbsp; q<sub>3</sub> = -1/height<sup>2</sup>
     * <p>
     * The elliptical cone has an elliptical cross section centered on the Z axis.  The elliptic cross section has i and j
     * radii of <tt>fRx</tt>,<tt>fRy</tt> where k = <tt>fHeight</tt> and k = -<tt>fHeight</tt>.
     *
     * @param fRx     The i radius of the cross section where k = <tt>fHeight</tt> and k = -<tt>fHeight</tt>.
     * @param fRy     The j radius of the cross section where k = <tt>fHeight</tt> and k = -<tt>fHeight</tt>.
     * @param fHeight The k distance from the origin at which the elliptic cross section has i and j
     *                radii of <tt>fRx</tt>,<tt>fRy</tt>
     * @return Returns this quadric after initialization.
     */
    public Quadric3f setEllipticalCone(final float fRx, final float fRy, final float fHeight) {
        if (PackageConstants.isZero(fRx) || PackageConstants.isZero(fRy) || PackageConstants.isZero(fHeight)) {
            throw new IllegalArgumentException();
        }
        m_q0 = 0.0f;
        m_q1 = 1.0f / (fRx * fRx);
        m_q2 = 1.0f / (fRy * fRy);
        m_q3 = -1.0f / (fHeight * fHeight);
        m_q9 = 0.0f;
        return this;
    }
    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Initializes the coefficients of the quadric formula for a
     * hyperboloid about k as:&nbsp;&nbsp;&nbsp; q<sub>1</sub> = 1/r<sub>i</sub><sup>2</sup> ;&nbsp;&nbsp;
     * q<sub>2</sub> = 1/r<sub>j</sub><sup>2</sup> ;&nbsp;&nbsp; q<sub>3</sub> = -1/r<sub>k</sub><sup>2</sup> ;&nbsp;&nbsp; q<sub>0</sub> = -1
     * <p>
     * The hyperboloid has an elliptical cross section of radius <tt>fRx</tt>, <tt>fRy</tt> centered at 0,0 where it cuts through
     * the XY plane.  It extends infinitely in Z and flares out (gets larger) as it gets further from k.
     *
     * @param fRx The i radius at k = 0.
     * @param fRy The j radius at k = 0.
     * @param fRz The k 'radius'.  This is not really a radius because the hyperboloid extends infinitely.  A small radius means the
     *            shape flares rapidly, a large radius means the shape flares gradually.  Note that as <tt>fRz</tt> approaches infinity the
     *            q<sub>3</sub> term approaches 0 and the hyperboloid becomes an elliptical cylinder.
     * @return Returns this quadric after initialization.
     */
    public Quadric3f setHyperboloid(final float fRx, final float fRy, final float fRz) {
        if (PackageConstants.isZero(fRx) || PackageConstants.isZero(fRy) || PackageConstants.isZero(fRz)) {
            throw new IllegalArgumentException();
        }
        m_q0 = -1.0f;
        m_q1 = 1.0f / (fRx * fRx);
        m_q2 = 1.0f / (fRy * fRy);
        m_q3 = -1.0f / (fRz * fRz);
        m_q9 = 0.0f;
        return this;
    }

    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Initializes the coefficients of the quadric formula for a
     * hyperbolic paraboloid about j as:&nbsp;&nbsp;&nbsp; q<sub>1</sub> = 1/r<sub>i</sub><sup>2</sup> ;&nbsp;&nbsp;
     * q<sub>2</sub> = -1/r<sub>j</sub><sup>2</sup> ;&nbsp;&nbsp;&nbsp; q<sub>9</sub> = -1/r<sub>k</sub>
     *
     * @param fRx The i 'radius'.
     * @param fRy The j 'radius'.
     * @param fRz The k 'radius'.
     * @return Returns this quadric after initialization.
     */
    public Quadric3f setHyperbolicParaboloid(final float fRx, final float fRy, final float fRz) {
        if (PackageConstants.isZero(fRx) || PackageConstants.isZero(fRy) || PackageConstants.isZero(fRz)) {
            throw new IllegalArgumentException();
        }
        m_q0 = 0.0f;
        m_q1 = 1.0f / (fRx * fRx);
        m_q2 = -1.0f / (fRy * fRy);
        m_q3 = 0.0f;
        m_q9 = -1.0f / fRz;
        return this;
    }

    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Get the intersection of a ray starting at point <tt>pt</tt> in the direction <tt>v</tt> with this quadric.
     * The ray direction should be normalized for the correct intersection distance to be reported.
     *
     * @param quadInt The intersection.
     * @param pt      The origin of the ray.
     * @param v       The direction of the ray.
     * @return Returns <tt>quadInt</tt> with the intersection information set.
     */
    public Quadric3fIntersection getIntersection(final Quadric3fIntersection quadInt, final Point3f pt, final Vector3f v) {
        // compute the coefficients of the quadratic equation describing the intersections.  NOTE: c is also
        //  the .'distance' from the surface: >0 outside; <= 0 inside.
        final float fA = (m_q1 * v.i * v.i) + (m_q2 * v.j * v.j) + (m_q3 * v.k * v.k);
        final float fB = (2.0f * ((m_q1 * pt.x * v.i) + (m_q2 * pt.y * v.j) + (m_q3 * pt.z * v.k))) + (m_q9 * v.k);
        final float fC = (m_q1 * pt.x * pt.x) + (m_q2 * pt.y * pt.y) + (m_q3 * pt.z * pt.z) + (m_q9 * pt.z) + m_q0;

        // Recall, the quadratic equations is that the roots of:
        //      a t^2 + b t + c = 0
        // are:
        //      t = -b +- sqrt(b^2 - 4ac) / 2a 

        if (PackageConstants.isZero(fA)) {
            // This is the case where there is only one root and the quadratic equation breaks down due to
            //  divide by zero.  The qudratic reduces to bt + c = 0 and the root is t = -c/b
            if (PackageConstants.isZero(fB)) {
                // this can only happen at 0,0,0
                quadInt.m_nCode = Quadric3fIntersection.NONE_INSIDE;
            } else {
                quadInt.m_fDist1 = -fC / fB;
                quadInt.m_fDist2 = Float.POSITIVE_INFINITY;
                if (fC > 0.0f) { // start is outside
                    quadInt.m_nCode = (quadInt.m_fDist1 > 0.0f) ? Quadric3fIntersection.GOING_INTO : Quadric3fIntersection.GOING_OUT_OF;
                } else { // start is inside
                    quadInt.m_nCode = (quadInt.m_fDist1 > 0.0f) ? Quadric3fIntersection.GOING_OUT_OF : Quadric3fIntersection.GOING_INTO;
                }
            }
        } else {
            // solve for the intersection distance using the quadratic formula.  First check the determinant to make sure
            // it is greater than 0 - otherwise, there is no intersection.
            float fDet = (fB * fB) - (4.0f * fC * fA);
            if (fDet < 0.0f) {  // no intersection - no solution to the quadratic equation - no intersection
                if (fC > 0.0f) {
                    quadInt.m_nCode = Quadric3fIntersection.NONE_OUTSIDE;
                } else {
                    quadInt.m_nCode = Quadric3fIntersection.NONE_INSIDE;
                }
            } else {
                fDet = (float) Math.sqrt(fDet);
                final float fDist1 = ((-fB) - fDet) / (2.0f * fA);
                final float fDist2 = ((-fB) + fDet) / (2.0f * fA);
                quadInt.m_fDist1 = (fDist1 <= fDist2) ? fDist1 : fDist2;
                quadInt.m_fDist2 = (fDist1 < fDist2) ? fDist2 : fDist1;
                if (fC > 0.0f) {  // start is outside
                    if ((quadInt.m_fDist1 > 0.0f) || (quadInt.m_fDist2 <= 0.0f)) {
                        quadInt.m_nCode = Quadric3fIntersection.GOING_INTO;
                    } else if (quadInt.m_fDist2 <= 0.0f) {
                        quadInt.m_nCode = Quadric3fIntersection.GOING_OUT_OF;
                    }
                } else { // start is inside
                    if ((quadInt.m_fDist1 > 0.0f) || (quadInt.m_fDist2 <= 0.0f)) {
                        quadInt.m_nCode = Quadric3fIntersection.GOING_OUT_OF;
                    } else if (quadInt.m_fDist2 <= 0.0f) {
                        quadInt.m_nCode = Quadric3fIntersection.GOING_INTO;
                    }
                }
            }
        }
        return quadInt;
    }

    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Get the intersection of a ray, <tt>ray</tt>, with this quadric.
     * The ray direction should be normalized for the correct intersection distance to be reported.
     *
     * @param quadInt The intersection.
     * @param ray     The ray.
     * @return Returns <tt>quadInt</tt> with the intersection information set.
     */
    public Quadric3fIntersection getIntersection(final Quadric3fIntersection quadInt, final Line3f ray) {
        return getIntersection(quadInt, ray.m_ptOrg, ray.m_vDir);
    }

    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Computes the normal for a point on the surface of the quadric.
     *
     * @param v  The normal.
     * @param pt The point on the surface of the quadric.
     * @return Returns <tt>v</tt> set to the normal corresponding to the intersection point.
     */
    public Vector3f getNormal(final Vector3f v, final Point3f pt) {
        final double U = 2.0 * m_q1 * pt.x;
        final double V = 2.0 * m_q2 * pt.y;
        final double W = (2.0 * m_q3 * pt.z) + m_q9;
        final double len = Math.sqrt((U * U) + (V * V) + (W * W));
        v.i = (float) (U / len);
        v.j = (float) (V / len);
        v.k = (float) (W / len);
        return v;
    }

}