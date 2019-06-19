/*
 * IRtGeometry.java
 *
 * Created on October 3, 2002, 3:06 AM
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
package cip.render.raytrace.interfaces;

import cip.render.raytrace.RayIntersection;
import cip.render.util2d.Point2f;
import cip.render.util3d.Bv3fIntersection;
import cip.render.util3d.Line3f;
import cip.render.util3d.Point3f;
import cip.render.util3d.Vector3f;
import org.jetbrains.annotations.NotNull;

/**
 * This is the interface for geometry that can be ray-traced.  The assumptions are that any object that can
 * be ray-traced is a valid closed non-self-intersecting solid.  This means that if a ray goes into the object,
 * it can always be traced out of the object, and it won't report the ray going into itself again before it
 * goes out.
 *
 * @author royster.hall@gmail.com
 * @version 1.0
 * @since 1.0
 */
public interface IRtGeometry {

    /**
     * Get the geometry type. Useful in error logging or messaging.
     *
     * @return (not null)
     */
    @NotNull String getType();

    /**
     * Specifies the sampling arrays that should be used for distributed ray-tracing.  Geometries that do not participate
     * in distributed ray-tracing will normally ignore this function, or, pass the arrays down to child geometries which
     * may make use of the sampling arrays.  Normally, these are used for motion-blurr in animated objects.
     *
     * @param nSample    The number of subsamples (oversamples) per pixel.
     * @param f1dSample  The 1d sample displacement array.  The length of this array will be equal to the number of subsamples
     *                   per pixel.  This array is for oversampling linear phenomena.
     * @param f1dRandom  The 1d 'jitter' array.  No assumptions should be made about the length of this array.
     * @param pt2dSample The 2d sample displacement array.  The length of this array will be equal to the number of subsamples
     *                   per pixel.  This array is for oversampling area phenomena.
     * @param pt2dRandom The 2d 'jitter' array.  No assumptions should be made about the length of this array.
     * @param pt3dSample The 3d sample displacement array.  The length of this array will be equal to the number of subsamples
     *                   per pixel.  This array is for oversampling volume phenomena.
     * @param pt3dRandom The 3d 'jitter' array.  No assumptions should be made about the length of this array.
     */
    void initSampling(int nSample, float[] f1dSample, float[] f1dRandom, Point2f[] pt2dSample, Point2f[] pt2dRandom,
                      Point3f[] pt3dSample, Point3f[] pt3dRandom);

    /**
     * Reports whether the solid is convex or not.  If there is any question about convexity, the object
     * should return <tt>false</tt> to the query.
     * <p>
     * The convexity query is used to determine whether
     * an object needs to tested for self-shadowing or self-reflection, and possibly for othe optimizations
     * in the ray tracing process.  If an object reports it is not convex, it will always render correctly though
     * there may be unnecessary processing if the object actually is convex.  If an object incorrectly reports
     * that it is convex, ther will likely be errors in shadow and reflection computations for that object.
     *
     * @return Returns <tt>true</tt> if the object is convex and <tt>false</tt> if the object is not convex
     * or the convexity cannot be verified.
     */
    boolean isConvex();

    /**
     * Tests whether a point is inside ot outside the geometry. This might be interesting, for example, for a travelling
     * camera that
     * ystal ball.
     *
     * @param pt (readonly) The point to be tested.
     * @return <tt>true</tt> if the point is inside the geometry, <tt>false</tt> otherwise.
     */
    boolean isInside(final Point3f pt);

    /**
     * Returns the vertices of the convex hull for the object.  There are no limits to the number of vertices that
     * define the convex hull for the object.  A tighter convex hull generally means a better fitting bounding volume
     * after transformation.
     *
     * @return Returns the array of convex hull vertices,  Returns <tt>null</tt> if a convex hull cannot be
     * fit to the object.  Being unable to fit a convex hull implies that the object must be explicitly
     * queried for intersection on every ray.  The returned array should be a copy of anything stored locally
     * in the object and it must be OK if this array is subsequently modified by the caller.
     */
    Point3f[] getConvexHullVertices();

    /**
     * Tests the bounding volume of the object for intersection.  If the object does not have a bounding volume it should
     * <tt>true</tt> and set the bounding volume intersection to indicate there was an intersection with the ray
     * originating inside the bounding volume, entering the bounding volume at Float.NEGATIVE_INFINITY and leaving the
     * bounding volume at Float.POSITIVE_INFINITY.
     *
     * @param bvInt The bounding volume intersection.
     * @param ray   The ray being tested for intersection with the bounding volume.
     * @return <tt>true</tt> if the ray intersects the bounding volume and may intersect the object, <tt>false</tt> if
     * the ray does not intersect the bounding volume and cannot intersect the object.
     */
    boolean getBvIntersection(Bv3fIntersection bvInt, Line3f ray);

    /**
     * Test for an intersection with the ray.  Return <tt>true</tt> and update the
     * {@link cip.render.raytrace.RayIntersection} if this is a closer intersection than what is currently in  the
     * {@link cip.render.raytrace.RayIntersection}, otherwise return <tt>false</tt> and leave the intersection
     * unchanged.  When an object reports that it has a closer intersection, the object <b>MUST</b> fill
     * in the {@link cip.render.raytrace.RayIntersection#m_fDist}, {@link cip.render.raytrace.RayIntersection#m_pt},
     * {@link cip.render.raytrace.RayIntersection#m_vNormal}, {@link cip.render.raytrace.RayIntersection#m_ptObject},
     * {@link cip.render.raytrace.RayIntersection#m_vObjNormal}, {@link cip.render.raytrace.RayIntersection#m_xfmObjToWorldNormal},
     * {@link cip.render.raytrace.RayIntersection#m_mtl}, {@link cip.render.raytrace.RayIntersection#m_rtObj}.  The object
     * may choose to fill in the natural coordinates of the intersection, in which case
     * {@link cip.render.raytrace.RayIntersection#m_bNatural} should be set to <tt>true</tt> though it is most
     * common for objects to set this to <tt>false</tt> and not go through the expense of computing
     * natural coordinates unless they are specifically requested later in the rendering process.
     *
     * @param intersection  (RayIntersection, not null, modified) The intersection of the ray and the surface.  The intersection
     *                      will be either freshly initialized or will contain information about the closest intersection
     *                      discovered thus far. It will be updated for this intersection if it is a closer intersection.
     * @param ray           (Line3f, not null, readonly) The ray being tested for intersection.
     * @param bStartsInside <tt>false</tt> if this is an outside ray, <tt>true</tt> if this is a ray
     *                      spawned from an intersection with this object and on the inside of the object.
     * @param nSample       The pixel sub-sample index.  This is used in distributed ray-tracing to make sure
     *                      the correct sample displacement is used for samples that are distributed.
     * @param nRandom       The jitter array index.
     * @return Returns <tt>true</tt> if there is an intersection with this object that is closer than the
     * current intersection, and <tt>false</tt> otherwise.
     */
    boolean getRayIntersection(@NotNull RayIntersection intersection, @NotNull Line3f ray,
                               boolean bStartsInside, int nSample, int nRandom);

    /**
     * Get the natural coordinates that are associated with the object-space intersection point.  When called, an object
     * should use the object coordinates intersection set in the <tt>intersection</tt> to generate the natural coordinates
     * which are filled into the {@link cip.render.raytrace.RayIntersection#m_ptNatural},
     * and {@link cip.render.raytrace.RayIntersection#m_vNatural} fields of the intersection.  Additionally, the
     * {@link cip.render.raytrace.RayIntersection#m_bNatural} flag should be set to <tt>true</tt> to prevent redundant computation
     * of natural coordinates.  If the {@link cip.render.raytrace.RayIntersection#m_bNatural} flag is already <tt>true</tt>
     * then the natural coordinates for the intersection have already been computed and the function can immediately return.
     *
     * NOTE: in many cases the natural coordinates are 2D (expressed as only x and y) and represent the bast wrapping of an image
     * around an object. In this case
     *
     * @param intersection The ray intersection.
     */
    void getNaturalCoordinates(@NotNull RayIntersection intersection);

    /**
     * Test whether there is an intersection with the ray from the intersection in the <tt>vLight</tt> direction
     * that is closer than the distance to the light, <tt>fDistLight</tt>.  Return true of there is an intersection-
     * which means that there is something between the intersection and the light and this object casts a shadow
     * on the intersection.
     *
     * @param intersection The intersection being tested for shadow.
     * @param vLight       The direction from the intersection to the light.
     * @param fDistLight   The disance from the intersection to the light.
     * @param light        The light.  This is supplied as an argument in the event an object is both a light source
     *                     and a geometry and the geometry of the light should not be tested for casting a shadow from that light.
     * @param nSample      The pixel sub-sample index.  This is used in distributed ray-tracing to make sure
     *                     the correct sample displacement is used for samples that are distributed.
     * @param nRandom      The jitter array index.
     * @return Returns <tt>true</tt> if this object casts a shadow from the light and <tt>false</tt>
     * otherwise.
     */
    boolean testShadow(RayIntersection intersection, Vector3f vLight, float fDistLight, IRtLight light, int nSample, int nRandom);

}
