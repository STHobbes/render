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

import org.jetbrains.annotations.NotNull;

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
 * ;&nbsp;&nbsp; q<sub>2</sub> = 1/r<sub>j</sub><sup>2</sup> ;&nbsp;&nbsp; q<sub>9</sub>  = -1/height
 * <p>
 * Elliptic Cone about k:&nbsp;&nbsp;&nbsp; q<sub>1</sub> = 1/r<sub>i</sub><sup>2</sup>
 * ;&nbsp;&nbsp; q<sub>2</sub> = 1/r<sub>j</sub><sup>2</sup> ;&nbsp;&nbsp; q<sub>3</sub> = -1/height<sup>2</sup>
 * <p>
 * Hyperboloid about k:&nbsp;&nbsp;&nbsp; q<sub>1</sub> = 1/r<sub>i</sub><sup>2</sup> ;&nbsp;&nbsp;
 * q<sub>2</sub> = 1/r<sub>j</sub><sup>2</sup> ;&nbsp;&nbsp; q<sub>3</sub> = -1/r<sub>k</sub><sup>2</sup> ;&nbsp;&nbsp; q<sub>0</sub> = -1
 * <p>
 * Hyperbolic Paraboloid about k:&nbsp;&nbsp;&nbsp; q<sub>1</sub> = 1/r<sub>i</sub><sup>2</sup> ;&nbsp;&nbsp;
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
 * at<sup>2</sup> + bt + c = 0
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
 * location and orientation, these simplify considerably to:
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
 * <p>
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
 * <p>
 * w = 2q<sub>3</sub>k + q<sub>9</sub>
 * </blockquote>
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
    private float m_q0 = -1.0f;

    /**
     * The q<sub>1</sub> coefficient of the quadric formulation.
     */
    private float m_q1 = 1.0f;

    /**
     * The q<sub>2</sub> coefficient of the quadric formulation.
     */
    private float m_q2 = 1.0f;

    /**
     * The q<sub>3</sub> coefficient of the quadric formulation.
     */
    private float m_q3 = 1.0f;

    /**
     * The q<sub>9</sub> coefficient of the quadric formulation.
     */
    private float m_q9 = 0.0f;

    /*
     * <tt>true</tt> if this representation is convex, <tt>false</tt> otherwise.
     */
    private boolean m_isConvex = true;

    /**
     * The name of the type of surface generated by this quadric.
     */
    private String m_quadricType = "sphere";

    /**
     * Creates a new instance of <tt>Quadric3f</tt> initialized to a unit sphere.
     */
    public Quadric3f() {
    }

    /**
     * Initializes the coefficients of the quadric formula for an
     * elliptical cylinder about k as:&nbsp;&nbsp; q<sub>1</sub> = 1/r<sub>i</sub><sup>2</sup>
     * ;&nbsp;&nbsp; q<sub>2</sub> = 1/r<sub>j</sub><sup>2</sup> ;&nbsp;&nbsp; q<sub>3</sub> = 0 ;&nbsp;&nbsp; q<sub>0</sub>  = -1
     * <p>
     * The elliptical cone is an ellipse of radius <tt>fRx</tt>, <tt>fRy</tt> centered at 0,0 where it cuts through
     * the XY plane.  It extends infinitely in Z.
     *
     * @param fRx (float, > 0.0f) The i radius of the elliptical-cylinder.
     * @param fRy (float, > 0.0f) The j radius of the elliptical-cylinder.
     * @return Returns this quadric after initialization.
     */
    public Quadric3f setEllipticalCylinder(final float fRx, final float fRy) {
        m_quadricType = (fRx == fRy) ? "cylinder" : "elliptical-cylinder";
        lclCheckGreaterThanZeroRxRyRy(fRx, fRy);
        m_q0 = -1.0f;
        m_q1 = 1.0f / (fRx * fRx);
        m_q2 = 1.0f / (fRy * fRy);
        m_q3 = 0.0f;
        m_isConvex = true;
        return this;
    }

    /**
     * Initializes the coefficients of the quadric formula for an
     * ellipsoid as:&nbsp; q<sub>1</sub> = 1/r<sub>i</sub><sup>2</sup> ;&nbsp;&nbsp;
     * q<sub>2</sub> = 1/r<sub>j</sub><sup>2</sup> ;&nbsp;&nbsp;
     * q<sub>3</sub> = 1/r<sub>k</sub><sup>2</sup> ;&nbsp;&nbsp; q<sub>0</sub> = -1
     * <p>
     * The ellipsoid is centered at 0,0,0.
     *
     * @param fRx (float, > 0.0f) The i radius of the ellipsoid.
     * @param fRy (float, > 0.0f) The j radius of the ellipsoid.
     * @param fRz (float, > 0.0f) The k radius of the ellipsoid.
     * @return Returns this quadric after initialization.
     */
    public Quadric3f setEllipsoid(final float fRx, final float fRy, final float fRz) {
        m_quadricType = (fRx == fRy && fRy == fRz) ? "sphere" : "ellipsoid";
        lclCheckGreaterThanZeroRadii(fRx, fRy, fRz);
        m_q0 = -1.0f;
        m_q1 = 1.0f / (fRx * fRx);
        m_q2 = 1.0f / (fRy * fRy);
        m_q3 = 1.0f / (fRz * fRz);
        m_q9 = 0.0f;
        m_isConvex = true;
        return this;
    }

    /**
     * Initializes the coefficients of the quadric formula for an
     * elliptic paraboloid about k:&nbsp;&nbsp;&nbsp; q<sub>1</sub> = 1/r<sub>i</sub><sup>2</sup>
     * ;&nbsp;&nbsp; q<sub>2</sub> = 1/r<sub>j</sub><sup>2</sup> ;&nbsp;&nbsp; q<sub>9</sub>  = -1/height
     * <p>
     * The elliptical paraboloid has an elliptical cross section centered on the Z axis.  The elliptic cross section has i and j
     * radii of <tt>fRx</tt>,<tt>fRy</tt> where k = <tt>fHeight</tt> and k = -<tt>fHeight</tt>.
     *
     * @param fRx     The i radius of the cross section where k = <tt>fHeight</tt>.
     * @param fRy     The j radius of the cross section where k = <tt>fHeight</tt>.
     * @param fHeight The k distance from the origin at which the elliptic cross section has i and j
     *                radii of <tt>fRx</tt>,<tt>fRy</tt>
     * @return Returns this quadric after initialization.
     */
    public Quadric3f setEllipticalParaboloid(final float fRx, final float fRy, final float fHeight) {
        m_quadricType = "elliptical-parabaloid";
        lclCheckGreaterThanZeroRxRyRy(fRx, fRy);
        m_q0 = 0.0f;
        m_q1 = 1.0f / (fRx * fRx);
        m_q2 = 1.0f / (fRy * fRy);
        m_q3 = 0.0f;
        m_q9 = -1.0f / fHeight;
        m_isConvex = true;
        return this;
    }

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
        m_quadricType = (fRx == fRy) ? "cone" : "elliptical-cone";
        lclCheckGreaterThanZeroRxRyRy(fRx, fRy);
        if (PackageConstants.isZero(fHeight)) {
            throw new IllegalArgumentException();
        }
        m_q0 = 0.0f;
        m_q1 = 1.0f / (fRx * fRx);
        m_q2 = 1.0f / (fRy * fRy);
        m_q3 = -1.0f / (fHeight * fHeight);
        m_q9 = 0.0f;
        m_isConvex = false;
        return this;
    }

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
        m_quadricType = "hyperbaloid";
        lclCheckGreaterThanZeroRadii(fRx, fRy, fRz);
        m_q0 = -1.0f;
        m_q1 = 1.0f / (fRx * fRx);
        m_q2 = 1.0f / (fRy * fRy);
        m_q3 = -1.0f / (fRz * fRz);
        m_q9 = 0.0f;
        m_isConvex = false;
        return this;
    }

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
        m_quadricType = "hyperbolic-paraboloid";
        lclCheckGreaterThanZeroRadii(fRx, fRy, fRz);
        m_q0 = 0.0f;
        m_q1 = 1.0f / (fRx * fRx);
        m_q2 = -1.0f / (fRy * fRy);
        m_q3 = 0.0f;
        m_q9 = -1.0f / fRz;
        m_isConvex = false;
        return this;
    }

    private void lclCheckGreaterThanZeroRadii(final float fRx, final float fRy, final float fRz) {
        if (!PackageConstants.isGreaterThanZero(fRx) || !PackageConstants.isGreaterThanZero(fRy) ||
                !PackageConstants.isGreaterThanZero(fRz)) {
            throw new IllegalArgumentException(String.format("The X, Y, and Z radii must all be positive for a %s.", m_quadricType));
        }
    }

    private void lclCheckGreaterThanZeroRxRyRy(final float fRx, final float fRy) {
        if (!PackageConstants.isGreaterThanZero(fRx) || !PackageConstants.isGreaterThanZero(fRy)) {
            throw new IllegalArgumentException(String.format("The X and Y radii must both be positive for a %s.", m_quadricType));
        }
    }

    /**
     * Get the quadric coefficient at the specified index.
     *
     * @param index The index, from 0 to 9 as described in the class documentation.
     * @return Returns the value of the rquested quadric coefficient.`
     */
    public float getQ(int index) {
        switch (index) {
            case 0:
                return m_q0;
            case 1:
                return m_q1;
            case 2:
                return m_q2;
            case 3:
                return m_q3;
            case 9:
                return m_q9;
            default:
                return 0.0f;
        }
    }

    @NotNull
    public String getQuadricType() {
        return m_quadricType;
    }

    /**
     * Inquire whether this quadric is convex.
     *
     * @return Returns <tt>true</tt> if the object is quadric and <tt>false</tt> if the quadric is not convex
     * or the convexity cannot be verified.
     */
    public boolean isConvex() {
        return m_isConvex;
    }

    /**
     * Tests whether a point is inside ot outside a quadric. This might be interesting, for example, for a travelling camera that
     * passes into a crystal ball.
     *
     * @param pt (readonly) The point to be tested.
     * @return <tt>true</tt> if the point is inside the quadric, <tt>false</tt> otherwise.
     */
    public boolean isInside(final Point3f pt) {
        final float fC = (m_q1 * pt.x * pt.x) + (m_q2 * pt.y * pt.y) + (m_q3 * pt.z * pt.z) + (m_q9 * pt.z) + m_q0;
        return fC < 0.0f;
    }

    /**
     * Get the intersection of a ray starting at point <tt>pt</tt> in the direction <tt>v</tt> with this quadric.
     * The ray direction should be normalized for the correct intersection distance to be reported.
     *
     * @param quadInt       (modified) The intersection.
     * @param pt            {readonly} The origin of the ray.
     * @param v             {readnoly} The direction of the ray.
     * @param bStartsInside <tt>false</tt> if this is an outside ray, <tt>true</tt> if this is a ray
     *                      spawned from an intersection with this object and on the inside of the object. NOTE: this is a logic
     *                      decision and must be respected - specifically, if the ray starts inside it was because a previous call
     *                      to this method reported the intersection from the outside. Thus, the only possible next intersection
     *                      is going out.
     * @return Returns <tt>quadInt</tt> with the intersection information set.
     */
    public Quadric3fIntersection getIntersection(final Quadric3fIntersection quadInt, final Point3f pt, final Vector3f v,
                                                 final boolean bStartsInside) {
        // compute the coefficients of the quadratic equation describing the intersections.  NOTE: c is also
        //  the 'distance' from the surface: >0 outside; <= 0 inside.
        final float fA = (m_q1 * v.i * v.i) + (m_q2 * v.j * v.j) + (m_q3 * v.k * v.k);
        final float fB = (2.0f * ((m_q1 * pt.x * v.i) + (m_q2 * pt.y * v.j) + (m_q3 * pt.z * v.k))) + (m_q9 * v.k);
        final float fC = (m_q1 * pt.x * pt.x) + (m_q2 * pt.y * pt.y) + (m_q3 * pt.z * pt.z) + (m_q9 * pt.z) + m_q0;

        // Recall, the in quadratic solution that the roots of:
        //      a t^2 + b t + c = 0
        // are:
        //      t = -b +- sqrt(b^2 - 4ac) / 2a 
        if (PackageConstants.isZero(fA)) {
            // This is the case where there is only one root and the quadratic equation breaks down due to
            //  divide by zero.  The quadratic reduces to bt + c = 0 and the root is t = -c/b
            if (PackageConstants.isZero(fB)) {
                // this can only happen at 0,0,0
                quadInt.m_nCode = Quadric3fIntersection.NONE_INSIDE;
            } else {
                // OK there is only one, it is either going in or out as specified by the bStartsInside logic. This would be the
                // case for something like a cone (which has upper and lower halves) when the major axis of the ray is k and it
                // is pareallel to the surface of the cone - this is a rare case.
                if (bStartsInside) {
                    quadInt.m_fDist1 = Float.POSITIVE_INFINITY;
                    quadInt.m_fDist2 = -fC / fB;
                } else {
                    quadInt.m_fDist1 = -fC / fB;
                    quadInt.m_fDist2 = Float.POSITIVE_INFINITY;
                    quadInt.m_nCode = Quadric3fIntersection.GOING_INTO;
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
                quadInt.m_fDist2 = (fDist1 <= fDist2) ? fDist2 : fDist1;
                if (m_isConvex) {
                    // for a convex object the first intersection is always the going into
                    quadInt.m_nCode = Quadric3fIntersection.GOING_INTO;
                } else {
                    // OK - this is a concave object, which means we don't know whether the least distance intersection is
                    // going in or going out - so we need the normal for the first intersection, and taking the dot product
                    // with the  ray will tell us whether the first intersection is going in or out.
                    quadInt.m_int1.m_ptOrg.x = pt.x + (quadInt.m_fDist1 * v.i);
                    quadInt.m_int1.m_ptOrg.y = pt.y + (quadInt.m_fDist1 * v.j);
                    quadInt.m_int1.m_ptOrg.z = pt.z + (quadInt.m_fDist1 * v.k);
                    getNormal(quadInt.m_int1.m_vDir, quadInt.m_int1.m_ptOrg);
                    if (v.dot(quadInt.m_int1.m_vDir) < 0.0f) {
                        quadInt.m_nCode = Quadric3fIntersection.GOING_INTO;
                    } else {
                        quadInt.m_nCode = Quadric3fIntersection.GOING_OUT_OF;
                    }
                }
            }
        }
        return quadInt;
    }

    /**
     * Get the intersection of a ray, <tt>ray</tt>, with this quadric.
     * The ray direction should be normalized for the correct intersection distance to be reported.
     *
     * @param quadInt       The intersection.
     * @param ray           The ray.
     * @param bStartsInside <tt>false</tt> if this is an outside ray, <tt>true</tt> if this is a ray
     *                      spawned from an intersection with this object and on the inside of the object.
     * @return Returns <tt>quadInt</tt> with the intersection information set.
     */
    public Quadric3fIntersection getIntersection(final Quadric3fIntersection quadInt, final Line3f ray,
                                                 final boolean bStartsInside) {
        return getIntersection(quadInt, ray.m_ptOrg, ray.m_vDir, bStartsInside);
    }

    /**
     * Computes the normal for a point on the surface of the quadric.
     *
     * @param v  (Vector3f, modified) The normal.
     * @param pt (Point3f, readonly) The point on the surface of the quadric.
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
