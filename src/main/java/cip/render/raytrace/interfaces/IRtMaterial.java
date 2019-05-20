/*
 * IRtMaterial.java
 *
 * Created on October 3, 2002, 3:07 AM
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
import cip.render.util3d.Point3f;
import cip.render.utilColour.RGBf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


/**
 * The interface to a material that can participate well in a ray-traced renderer.
 *
 * @author royster.hall@gmail.com
 * @version 1.0
 * @since 1.0
 */
public interface IRtMaterial {
    /**
     * Specifies the sampling arrays that should be used for distributed ray-tracing.  Materials that do not participate
     * in distributed ray-tracing will normally ignore this function, or, pass the arrays down to child materials which
     * may make use of the sampling arrays.  Normally, these are used for normal perturbations for rough surfaces.
     *
     * @param nSample    The number of sub-samples (over-samples) per pixel.
     * @param f1dSample  (readonly) The 1d sample displacement array.  The length of this array will be equal to the number
     *                   of sub-samples per pixel.  This array is for oversampling linear phenomena.
     * @param f1dRandom  (readonly) The 1d 'jitter' array.  No assumptions should be made about the length of this array.
     * @param pt2dSample (readonly) The 2d sample displacement array.  The length of this array will be equal to the number
     *                   of sub-samples per pixel.  This array is for oversampling area phenomena.
     * @param pt2dRandom (readonly) The 2d 'jitter' array.  No assumptions should be made about the length of this array.
     * @param pt3dSample (readonly) The 3d sample displacement array.  The length of this array will be equal to the number
     *                   of sub-samples per pixel.  This array is for oversampling volume phenomena.
     * @param pt3dRandom (readonly) The 3d 'jitter' array.  No assumptions should be made about the length of this array.
     */
    void initSampling(int nSample, @NotNull float[] f1dSample, @Nullable float[] f1dRandom,
                      @NotNull Point2f[] pt2dSample, @Nullable Point2f[] pt2dRandom,
                      @NotNull Point3f[] pt3dSample, @Nullable Point3f[] pt3dRandom);

    /**
     * Compute the colour of a surface as seen from a specific direction.
     *
     * @param rgb            (modified) The computed colour at the surface.
     * @param intersection   (readonly) The description of the surface - location, orientation, material, etc.
     * @param lights         (readonly) The light in the scene that may affect the intersection.
     * @param rtObjects      (readonly) The objects in the scene.
     * @param rtBkg          (readonly) The background description.
     * @param nMaxRecursions The maximum reflections to be followed in computing this colour.
     * @param nSample        The pixel sub-sample index.  This is used in distributed ray-tracing to make sure
     *                       the correct sample displacement is used for samples that are distributed.
     * @param nRandom        The jitter array index..
     */
    void getColor(@NotNull RGBf rgb, @NotNull RayIntersection intersection, @NotNull IRtLight[] lights,
                  @NotNull IRtGeometry[] rtObjects, @NotNull IRtBackground rtBkg, int nMaxRecursions,
                  int nSample, int nRandom);
}
