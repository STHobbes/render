/*
 * IRtCamera.java
 *
 * Created on October 14, 2002, 10:51 AM
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
import cip.render.util3d.Line3f;
import cip.render.util3d.Point3f;

/**
 * This is the interface to a camera used in a ray-traced scene.  The basic function of the camera in the context
 * of ray tracing is to map a pixel or pixel sub-sample from a location on the picture plane to a ray that can be
 * traced through the environment.
 *
 * @author royster.hall@gmail.com
 * @version 1.0
 * @since 1.0
 */
public interface IRtCamera {
    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Specifies the sampling arrays that should be used for distributed ray-tracing.  Cameras that do not participate
     * in distributed ray-tracing will normally ignore this function, or, pass the arrays down to child cameras which
     * may make use of the sampling arrays.  Normally, these are used for depth of field in cameras with a non-point
     * aperture.
     *
     * @param nSample    The number of sub-samples (over-samples) per pixel.
     * @param f1dSample  The 1d sample displacement array.  The length of this array will be equal to the number of sub-samples
     *                   per pixel.  This array is for oversampling linear phenomena.
     * @param f1dRandom  The 1d 'jitter' array.  No assumptions should be made about the length of this array.
     * @param pt2dSample The 2d sample displacement array.  The length of this array will be equal to the number of sub-samples
     *                   per pixel.  This array is for oversampling area phenomena.
     * @param pt2dRandom The 2d 'jitter' array.  No assumptions should be made about the length of this array.
     * @param pt3dSample The 3d sample displacement array.  The length of this array will be equal to the number of sub-samples
     *                   per pixel.  This array is for oversampling volume phenomena.
     * @param pt3dRandom The 3d 'jitter' array.  No assumptions should be made about the length of this array.
     */
    void initSampling(int nSample, float[] f1dSample, float[] f1dRandom, Point2f[] pt2dSample, Point2f[] pt2dRandom,
                      Point3f[] pt3dSample, Point3f[] pt3dRandom);

    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Specify the picture plane that will be used in mapping pixels to rays.
     *
     * @param nXres        The X resolution (width) of the picture plane.
     * @param nYres        The Y resolution (height) of the picture plane.
     * @param fPixelAspect The aspect ratio of a pixel, width/height.
     */
    void initPicturePlane(int nXres, int nYres, float fPixelAspect);

    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Performs the mapping from a pixel and pixel sub-sample to a ray.  The upper left corner of the picture plane is
     * taken as 0,0.  If the rayIntersection is non-null, then the view vector and sampling information (sample start offset
     * and sample solid angle) will be set in the ray intersection
     *
     * @param ray             The ray that will be computed as the mapping from the specified pixel and subsample.
     * @param rayIntersection The ray intersection to be initialized (if non-<tt>null</tt>).
     * @param nXpixel         The pixel X location.
     * @param nYpixel         The pixel Y location.
     * @param nSample         The pixel sub-sample.
     * @param nRandom         The random value for this sub-sample..
     */
    void getRay(Line3f ray, RayIntersection rayIntersection, int nXpixel, int nYpixel, int nSample, int nRandom);

}
