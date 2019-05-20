/*
 * RayIntersection.java
 *
 * Created on October 3, 2002, 10:50 AM
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
package cip.render.raytrace;

import cip.render.raytrace.interfaces.IRtGeometry;
import cip.render.raytrace.interfaces.IRtMaterial;
import cip.render.util.AngleF;
import cip.render.util2d.Point2f;
import cip.render.util3d.*;
import cip.render.utilColour.RGBf;

/**
 * This is the description of the surface at the intersection of a ray with a geometric object.  Because ray intersections
 * are frequently used temporary objects that contain a description of the complete intersection tree beneath them they are also
 * a good place to hold the object cache for temporary geometric objects they use in the computation
 * of the intersection tree.
 * <p>
 * The <tt>RayIntersection</tt> maintains a non-thread-safe object cache.
 * If a given <tt>RayIntersection</tt> is only accessed by a single thread, then the object cache maintained
 * by that intersection may be used to borrow/return objects of most 3d utility types with no synchronization overhead.
 * This is useful in the context of either a single or multi-threading because
 * the <tt>new</tt> operator is thread-safe which imposes high synchronization overhead, and garbage collecting when the object
 * goes out-of-scope also imposes high overhead. In tests, the local
 * cache is substantially faster than either a global cache or <tt>new</tt> operator.  The performance of a
 * local cache scales as would be expected with multiple threads on a multi-processor environment, while global cache and
 * <tt>new</tt> implementations may actually slow operation when multiple threads are started on a multi-processor
 * machine, presumably because of synchronization collisions and subsequent blocking.
 *
 * @author royster.hall@gmail.com
 * @version 1.0
 * @since 1.0
 */
public class RayIntersection {
    private RayIntersection m_next = null;

    //-------------------------------------------------------------------------------------------------------------------------
    // the properties of a ray intersection with a surface - these are properties of the intersection that are normally
    //  transformed into world space so illumination, reflection, and refraction can be computed.
    /**
     * The distance from the ray origin to the intersection .
     */
    public float m_fDist;

    /**
     * The solid angle of the sample.  This is the solid angles as seen from the effective start of the sampling ray
     * and can be used to compute the area represented by the intersection sample.  The solid angle is measured from the
     * true start of the ray which is typically the eye.  The cross-sectional area of the sample (area perpendicular to
     * the view vector) at the sample is equal to <tt>m_fSampleSolidAngle * ((m_fDist + m_fSampleStartOffset) ^ 2) </tt>.
     * When the ray starts at the eye, <tt>m_fSampleStartOffset = 0.0</tt>.  This offset is used to account for this
     * intersection being for a refracted or reflected ray that is continuing to spread along some solid angle from the
     * location where it originally started.  This will be 0.0 if it has not been set (for backwards compatibility).
     */
    public float m_fSampleSolidAngle;

    /**
     * The distance to add to m_fDist to get the effective distance from the start of the sample to which the solid angle
     * applies.  For example, if a ray from the eye is reflected from a plane, the reflected ray continues with the same
     * solid angle as the ray from the eye and <tt>(m_fDist + m_fSampleStartOffset) </tt> is the cumulative distance
     * the ray has traveled.  In a really good implementation, the curvature of the surface is considered which may either
     * spread of focus the solid angle after reflection and/or refraction.  NOTE: if <tt>m_fSampleStartOffset =
     * Float.POSITIVE_INFINITY</tt> it means this is a parallel projection and <tt>m_fSampleSolidAngle</tt> is the
     * actual cross-sectional area of the sample.
     */
    public float m_fSampleStartOffset;

    /**
     * The surface area represented by the sample. This is normally needed only for texturing, so it is not set until
     * required.  It is set to -1.0 during intersection initialization so you can easily test to see whether it has
     * been computed
     */
    private float m_fArea;

    /**
     * The point at which the ray intersects the surface
     */
    public Point3f m_pt = new Point3f();

    /**
     * The normal of the surface at the intersection point
     */
    public Vector3f m_vNormal = new Vector3f();

    /**
     * The direction vector from the surface intersection to the eye (ray origin). NOTE: ray origin = <tt>m_pt</tt> +
     * <tt>m_fDist</tt>(<tt>m_vToEye</tt>).  Also note that the vector is technically the vector back to where
     * the intersection was computed from, which may be the eye, but may also be any other point it space such as a
     * reflective or transparent surface which is spawning the recursive reflection and refraction rays, or, testing
     * for visibility from a light source.
     */
    public Vector3f m_vToEye = new Vector3f();

    //------------------------------------------------------------------
    // These are the 'object space' properties of the intersection.  They
    //  are maintained as part of the intersection description so that texture
    //  mappings with respect to object space can be applied, or so that
    //  natural coordinate mappings can be computed at a later time
    /**
     * The object space location of the intersection
     */
    public Point3f m_ptObject = new Point3f();

    /**
     * The object space normal of the intersection
     */
    public Vector3f m_vObjNormal = new Vector3f();

    /**
     * The object space normal of the intersection
     */
    public Xfm4x4f m_xfmObjToWorldNormal = new Xfm4x4f();

    /**
     * <tt>true</tt> if natural coordinates are set, <tt>false</tt> otherwise.
     */
    public boolean m_bNatural;
    /**
     * The natural coordinates of the ray intersection on the object
     */
    public Point2f m_ptNatural = new Point2f();
    /**
     * The natural coordinates UV vectors on the object
     */
    public Vector3f[] m_vNatural = {new Vector3f(), new Vector3f()};

    //------------------------------------------------------------------
    // These are the texture coordinates once a mapping function has been applied
    /**
     * <tt>true</tt> if texture coordinates are set, <tt>false</tt> otherwise.
     */
    public boolean m_bTexture;
    /**
     * The texture coordinate
     */
    public Point3f m_ptTexture = new Point3f();
    /**
     * The texture vectors.  How these vectors are interpreted is specific to the mapping function that generated them.
     */
    public Vector3f[] m_vTexture = {new Vector3f(), new Vector3f(), new Vector3f()};
    //------------------------------------------------------------------
    /**
     * The material of the surface at the ray intersection
     */
    public IRtMaterial m_mtl;
    /**
     * The geometry intersected by the ray
     */
    public IRtGeometry m_rtObj;

    //------------------------------------------------------------------
    /**
     * The maximum contribution this intersection can make to the final colour at a pixel.  This value is normalized to the
     * range 0 to 1.  When an intersection is queried for colour, the first query carries a contribution of 1.  As rays are
     * subsequently spawned from the intersection, the color contributed by those rays decreases based on the surface
     * properties where the rays were spawned.
     */
    public float m_fMaxContribution;

    //------------------------------------------------------------------
    // this are the heads of the local cache lists.  The local cache assumes that only one thread is working
    //  with this ray intersection so tha cache attached to the intersection can be used without synchronization
    //  since it is only accessed by a single thread.
    private RayIntersection m_cacheIntersection;
    private LightInfo m_cacheLgtInfo;
    private Point3f m_cachePoint;
    private Line3f m_cacheLine;
    private Vector3f m_cacheVector;
    private Plane3f m_cachePlane;
    private Plane3fIntersection m_cachePlnInt;
    private Bv3fIntersection m_cacheBvInt;
    private Quadric3fIntersection m_cacheQuadInt;
    private Xfm4x4f m_cacheXfm;
    private RGBf m_cacheRGB;
    private AngleF m_cacheAngle;

    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Creates a new uninitialized instance of <tt>RayIntersection</tt>
     */
    public RayIntersection() {
    }

    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Initialize a <tt>RayIntersection</tt> for the start of intersection testing.  This sets the view vector for the
     * intersection, sets the distance to the maximum representable distance in single precision, and <tt>nulls</tt> the
     * object and material references.
     *
     * @param vFromEye The <i>normalized</i> direction vector for the ray being tested.  The reverse of this will be set as
     *                 the <b>V</b> vector leaving the intersection.
     * @return Returns this <tt>RayIntersection</tt>.
     */
    public RayIntersection initialize(final Vector3f vFromEye) {
        m_fDist = Float.MAX_VALUE;
        m_fSampleSolidAngle = 0.0f;
        m_fSampleStartOffset = Float.POSITIVE_INFINITY;
        m_fArea = -1.0f;
        m_vToEye.setValue(vFromEye).reverse();
        m_bNatural = false;
        m_bTexture = false;
        m_mtl = null;
        m_rtObj = null;
        m_fMaxContribution = 1.0f;
        return this;
    }

    //-------------------------------------------------------------------------------------------------------------------------
    public RayIntersection setValue(final RayIntersection intInit) {
        m_fDist = intInit.m_fDist;
        m_fArea = intInit.m_fArea;
        m_pt.setValue(intInit.m_pt);
        m_vNormal.setValue(intInit.m_vNormal);
        m_vToEye.setValue(intInit.m_vToEye);
        m_ptObject.setValue(intInit.m_ptObject);
        m_vObjNormal.setValue(intInit.m_vObjNormal);
        m_xfmObjToWorldNormal.setValue(intInit.m_xfmObjToWorldNormal);
        if (m_bNatural = intInit.m_bNatural) {
            m_ptNatural.setValue(intInit.m_ptNatural);
            m_vNatural[0].setValue(intInit.m_vNatural[0]);
            m_vNatural[1].setValue(intInit.m_vNatural[1]);
        }
        if (m_bTexture = intInit.m_bTexture) {
            m_ptTexture.setValue(intInit.m_ptTexture);
            m_vTexture[0].setValue(intInit.m_vTexture[0]);
            m_vTexture[1].setValue(intInit.m_vTexture[1]);
            m_vTexture[2].setValue(intInit.m_vTexture[2]);
        }
        m_mtl = intInit.m_mtl;
        m_rtObj = intInit.m_rtObj;
        return this;
    }
    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Borrow a ray intersection object.  The borrowed ray intersection object should be returned to the
     * <tt>RayIntersection</tt> from which it was borrowed.  This function <b>is not</b> thread-safe.
     *
     * @return Returns an un-initialized ray intersection object.
     */
    public RayIntersection borrowIntersection() {
        final RayIntersection intersection = m_cacheIntersection;
        if (null == intersection) {
            return new RayIntersection();
        }
        m_cacheIntersection = intersection.m_next;
        return intersection;
    }

    /**
     * Returns a borrowed ray intersection object.  This function <b>is not</b> thread-safe.
     *
     * @param intersection The ray intersection object being returned.
     */
    public void returnIntersection(final RayIntersection intersection) {
        intersection.m_next = m_cacheIntersection;
        m_cacheIntersection = intersection;
    }

    /**
     * Borrow a light information object.  The borrowed light information object should be returned to the
     * <tt>RayIntersection</tt> from which it was borrowed.  This function <b>is not</b> thread-safe.
     *
     * @return Returns an un-initialized borrowed light information object.
     */
    public LightInfo borrowLightInfo() {
        final LightInfo lgtInfo = m_cacheLgtInfo;
        if (null == lgtInfo) {
            return new LightInfo();
        }
        m_cacheLgtInfo = lgtInfo.m_next;
        return lgtInfo;
    }

    /**
     * Returns a borrowed light information object.  This function <b>is not</b> thread-safe.
     *
     * @param lgtInfo The light information object being returned.
     */
    public void returnLightInfo(final LightInfo lgtInfo) {
        lgtInfo.m_next = m_cacheLgtInfo;
        m_cacheLgtInfo = lgtInfo;
    }

    /**
     * Borrow a point.  The borrowed point should be returned to the <tt>RayIntersection</tt> from which it
     * was borrowed.  This function <b>is not</b> thread-safe.
     *
     * @return Returns an un-initialized borrowed point.
     */
    public Point3f borrowPoint() {
        final Point3f pt = m_cachePoint;
        if (null == pt) {
            return new Point3f();
        }
        m_cachePoint = pt.m_next;
        return pt;
    }

    /**
     * Returns a borrowed pt.  This function <b>is not</b> thread-safe.
     *
     * @param pt The point being returned.
     */
    public void returnPoint(final Point3f pt) {
        pt.m_next = m_cachePoint;
        m_cachePoint = pt;
    }

    /**
     * Borrow a line.  The borrowed line should be returned to the <tt>RayIntersection</tt> from which it
     * was borrowed.  This function <b>is not</b> thread-safe.
     *
     * @return Returns an un-initialized borrowed line.
     */
    public Line3f borrowLine() {
        final Line3f line = m_cacheLine;
        if (null == line) {
            return new Line3f();
        }
        m_cacheLine = line.m_next;
        return line;
    }

    /**
     * Returns a borrowed line.  This function <b>is not</b> thread-safe.
     *
     * @param line The line being returned.
     */
    public void returnLine(final Line3f line) {
        line.m_next = m_cacheLine;
        m_cacheLine = line;
    }

    /**
     * Borrow a vector.  The borrowed vector should be returned to the <tt>RayIntersection</tt> from which it
     * was borrowed.  This function <b>is not</b> thread-safe.
     *
     * @return Returns an un-initialized borrowed vector.
     */
    public Vector3f borrowVector() {
        final Vector3f v = m_cacheVector;
        if (null == v) {
            return new Vector3f();
        }
        m_cacheVector = v.m_next;
        return v;
    }

    /**
     * Returns a borrowed vector.  This function <b>is not</b> thread-safe.
     *
     * @param v The vector being returned.
     */
    public void returnVector(final Vector3f v) {
        v.m_next = m_cacheVector;
        m_cacheVector = v;
    }

    /**
     * Borrow a plane.  The borrowed plane should be returned to the <tt>RayIntersection</tt> from which it
     * was borrowed.  This function <b>is not</b> thread-safe.
     *
     * @return Returns an un-initialized borrowed plane.
     */
    public Plane3f borrowPlane() {
        final Plane3f pln = m_cachePlane;
        if (null == pln) {
            return new Plane3f();
        }
        m_cachePlane = pln.m_next;
        return pln;
    }

    /**
     * Returns a borrowed plane.  This function <b>is not</b> thread-safe.
     *
     * @param pln The plane being returned.
     */
    public void returnPlane(final Plane3f pln) {
        pln.m_next = m_cachePlane;
        m_cachePlane = pln;
    }

    /**
     * Borrow a plane intersection information object.  The borrowed plane intersection information object should be returned to the
     * <tt>RayIntersection</tt> from which it was borrowed.  This function <b>is not</b> thread-safe.
     *
     * @return Returns an un-initialized borrowed plane intersection information object.
     */
    public Plane3fIntersection borrowPlaneInt() {
        final Plane3fIntersection plnInt = m_cachePlnInt;
        if (null == plnInt) {
            return new Plane3fIntersection();
        }
        m_cachePlnInt = plnInt.m_next;
        return plnInt;
    }

    /**
     * Returns a borrowed plane intersection.  This function <b>is not</b> thread-safe.
     *
     * @param plnInt The plane intersection information object being returned.
     */
    public void returnPlaneInt(final Plane3fIntersection plnInt) {
        plnInt.m_next = m_cachePlnInt;
        m_cachePlnInt = plnInt;
    }

    /**
     * Borrow a bounding volume intersection information object.  The borrowed bounding volumen intersection
     * information object should be returned to the  <tt>RayIntersection</tt> from which it was borrowed.
     * This function <b>is not</b> thread-safe.
     *
     * @return Returns an un-initialized borrowed bounding volume intersection information object.
     */
    public Bv3fIntersection borrowBvInt() {
        final Bv3fIntersection bvInt = m_cacheBvInt;
        if (null == bvInt) {
            return new Bv3fIntersection();
        }
        m_cacheBvInt = bvInt.m_next;
        return bvInt;
    }

    /**
     * Returns a borrowed bounding volume intersection.  This function <b>is not</b> thread-safe.
     *
     * @param bvInt The bounding volume intersection information object being returned.
     */
    public void returnBvInt(final Bv3fIntersection bvInt) {
        bvInt.m_next = m_cacheBvInt;
        m_cacheBvInt = bvInt;
    }

    /**
     * Borrow a quadric intersection information object.  The borrowed quadric intersection information object should be
     * returned to the <tt>RayIntersection</tt> from which it was borrowed.  This function <b>is not</b> thread-safe.
     *
     * @return Returns an un-initialized borrowed quadric intersection information object.
     */
    public Quadric3fIntersection borrowQuadricInt() {
        final Quadric3fIntersection quadInt = m_cacheQuadInt;
        if (null == quadInt) {
            return new Quadric3fIntersection();
        }
        m_cacheQuadInt = quadInt.m_next;
        return quadInt;
    }

    /**
     * Returns a borrowed quadric intersection.  This function <b>is not</b> thread-safe.
     *
     * @param quadInt The quadric intersection information object being returned.
     */
    public void returnQuadricInt(final Quadric3fIntersection quadInt) {
        quadInt.m_next = m_cacheQuadInt;
        m_cacheQuadInt = quadInt;
    }

    /**
     * Borrow a transformation.  The borrowed transformation should be returned to the
     * <tt>RayIntersection</tt> from which it was borrowed.  This function <b>is not</b> thread-safe.
     *
     * @return Returns an un-initialized borrowed transformation.
     */
    public Xfm4x4f borrowXfm() {
        final Xfm4x4f xfm = m_cacheXfm;
        if (null == xfm) {
            return new Xfm4x4f();
        }
        m_cacheXfm = xfm.m_next;
        return xfm;
    }

    /**
     * Returns a borrowed transformation.  This function <b>is not</b> thread-safe.
     *
     * @param xfm The transformation being returned.
     */
    public void returnXfm(final Xfm4x4f xfm) {
        xfm.m_next = m_cacheXfm;
        m_cacheXfm = xfm;
    }

    /**
     * Borrow an RGB colour.  The borrowed colour should be returned to the <tt>RayIntersection</tt> from which it
     * was borrowed.  This function <b>is not</b> thread-safe.
     *
     * @return Returns an un-initialized borrowed colour.
     */
    public RGBf borrowRGB() {
        final RGBf rgb = m_cacheRGB;
        if (null == rgb) {
            return new RGBf();
        }
        m_cacheRGB = rgb.m_next;
        return rgb;
    }

    /**
     * Returns a borrowed colour.  This function <b>is not</b> thread-safe.
     *
     * @param rgb The colour being returned.
     */
    public void returnRGB(final RGBf rgb) {
        rgb.m_next = m_cacheRGB;
        m_cacheRGB = rgb;
    }

    /**
     * Borrow an angle.  The borrowed angle should be returned to the <tt>RayIntersection</tt> from which it
     * was borrowed.  This function <b>is not</b> thread-safe.
     *
     * @return Returns an un-initialized borrowed angle.
     */
    public AngleF borrowAngle() {
        final AngleF angle = m_cacheAngle;
        if (null == angle) {
            return new AngleF();
        }
        m_cacheAngle = angle.m_next;
        return angle;
    }

    /**
     * Returns a borrowed angle.  This function <b>is not</b> thread-safe.
     *
     * @param angle The angle being returned.
     */
    public void returnAngle(final AngleF angle) {
        angle.m_next = m_cacheAngle;
        m_cacheAngle = angle;
    }
}
