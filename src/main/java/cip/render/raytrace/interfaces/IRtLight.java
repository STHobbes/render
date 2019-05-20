/*
 * IRtLight.java
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

import cip.render.raytrace.LightInfo;
import cip.render.raytrace.RayIntersection;
import cip.render.util2d.Point2f;
import cip.render.util3d.Point3f;

/**
 * This is the interface for lights that can be used in ray tracing or other illumination tasks.  The interface allows
 * us to query the light for its illumination contribution to a point on a surface in the absence of a shadowing
 * occlusion between the light and the surface.
 *
 * @author royster.hall@gmail.com
 * @version 1.0
 * @since 1.0
 */
public interface IRtLight {
    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Specifies the sampling arrays that should be used for distributed ray-tracing.  Lights that do not participate
     * in distributed ray-tracing will normally ignore this function, or, pass the arrays down to child lights which
     * may make use of the sampling arrays.  Normally, these are used for motion-blur in animated lights or for distributing
     * the light in area sources.
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
     * Set a dimmer value for the light, which is a scalar multiplier for the light intensity.
     *
     * @param fDimmer The dimming factor - usually in the range 0 to 1
     */
    void setDimmer(float fDimmer);

    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Get the lighting information describing how this light illuminates the ray intersection.  The light
     * should check the ray intersection to make sure the intersection actually faces the light, and return
     * <tt>false</tt> if the intersection cannot be illuminated by the light.
     *
     * @param lightInfo    (LightInfo, modified) TThe description of the illumination of the intersection by this light.
     * @param intersection TRayIntersection, constant) The description of the ray intersection.
     * @param nSample      The pixel sub-sample index.  This is used in distributed ray-tracing to make sure
     *                     the correct sample displacement is used for samples that are distributed.
     * @param nRandom      The jitter array index.
     * @return Returns <tt>true</tt> if this light illuminates the ray intersection and the <tt>lightInfo</tt>
     * has been filled in with lighting information, otherwise <tt>false</tt> is returned and the <tt>lightInfo</tt>
     * is meaningless (probably unchanged).
     */
    boolean getLight(LightInfo lightInfo, RayIntersection intersection, int nSample, int nRandom);
}
