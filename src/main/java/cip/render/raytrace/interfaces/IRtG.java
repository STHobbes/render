/*
 * IRtG.java
 *
 * Created on October 25, 2002, 8:27 PM
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
 * This is the interface to the geometric slope distribution functions used in illumination models.  The
 * geometric attenuation function predicts the amount of self shading that happens due to surface roughness
 * and is generally a function of roughness and the geometry of reflection, coming into effect primarily
 * near grazing.
 *
 * @author royster.hall@gmail.com
 * @version 1.0
 * @since 1.0
 */
public interface IRtG {
    /**
     * Some geometric attenuation functions are initialized from the angle beta that represents the
     * angle between <b>N</b> and <b>H</b> at which slope distribution function drops to half the value
     * of when <b>N</b> = <b>H</b>.  Beta is an indicator of surface roughness.
     *
     * @param aBeta The angle at which the slope distribution function drops to half its maximum value.
     */
    void initialize(AngleF aBeta);

    /**
     * Evaluate the geometric attenuation function.
     *
     * @param N The surface normal.
     * @param H The normalized bisector between <b>V</b> and <b>L</b>.
     * @param V The view or eye vector.
     * @param L The light vector.
     * @return The value of the geometric attenuation function.
     */
    float evaluate(Vector3f N, Vector3f H, Vector3f V, Vector3f L);

}
