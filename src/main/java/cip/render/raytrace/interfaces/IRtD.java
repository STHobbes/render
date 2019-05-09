/*
 * IRtD.java
 *
 * Created on October 21, 2002, 10:13 AM
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

import cip.render.util.AngleF;
import cip.render.util3d.Vector3f;

/**
 * This is the interface to the slope distribution functions used in illumination models.  The slope
 * distribution function is a model of the roughness of the surface that predicts how much of the light
 * received from one direction will be reflected in another direction as a result of surface roughness
 * only.
 *
 * @author royster.hall@gmail.com
 * @version 1.0
 * @since 1.0
 */
public interface IRtD {
    /**
     * The slope distibution is initialized from the angle beta that represents the angle between <b>N</b>
     * and <b>H</b> at which the function drops to half the value of when <b>N</b> = <b>H</b>.
     *
     * @param aBeta The angle at which the function drops to half its maximum value.
     */
    void initialize(AngleF aBeta);

    /**
     * Evaluate the slope-distribution function.  Most functions use only <b>N</b> and <b>H</b> for the
     * evaluation, however <b>V</b> and <b>L</b> are required for the historically maintained, but seldom
     * used Phong model.  I've not found that passing everything represents a neglibible performance
     * penalty, so I use this most general set of arguments.
     *
     * @param N The surface normal.
     * @param H The normalized bisector between <b>V</b> and <b>L</b>.
     * @param V The view or eye vector.
     * @param L The light vector.
     * @return The value of the slope distribution function.
     */
    float evaluate(Vector3f N, Vector3f H, Vector3f V, Vector3f L);
}
