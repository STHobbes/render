/*
 * IRtBackground.java
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

import cip.render.util.AngleF;
import cip.render.util3d.Line3f;
import cip.render.utilColour.RGBf;

/**
 * A background for ray tracing.  In the simplest form the background may be simply a constant colour
 * while in more complex cases the background could be a reflection map, a procedurally generated
 * background, or even a background geometry that is ray traced.
 *
 * @author royster.hall@gmail.com
 * @version 1.0
 * @since 1.0
 */
public interface IRtBackground {
    /**
     * Gets the colour for a ray directed into the background.
     *
     * @param rgb     The rgb of the background.
     * @param ray     The ray directed into the background.  This may be <tt>null</tt> in which
     *                case a non-directional "average" background will be returned.
     * @param aSample The field-of-view describing the area of the background we are interested in.
     *                The field-of-view is the angle from the ray describing a cone within which we are interested
     *                in a area sample of the background.  This is useful for getting the reflection from a rough
     *                surface.
     */
    void getColor(RGBf rgb, Line3f ray, AngleF aSample);
}
