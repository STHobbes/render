/*
 * PackageConstants.java
 *
 * Created on October 3, 2002, 9:45 PM
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
package cip.render.raytrace.material;

/**
 * These are constants that control reflection and transmission recursion depth so ray-tracing does not get into
 * infinitely recursive loops.
 *
 * @author royster.hall@gmail.com
 * @version 1.0
 * @since 1.0
 */
public class PackageConstants {
    public static final String DEFAULT_NAME = "<unspecified>";

    /**
     * The maximum number of internal reflections on a ray that will be traced before the ray is terminated.  This
     * limit guards against situations where a ray never seems to escape an object (possible within our limits of
     * roundoff error, and for particular geometry configuations).
     */
    public static int MAX_INTERNAL_REFLECTION = 10;

    /**
     * The contribution below which continuing the ray intersection tree will be assumed to have
     * no meaningful effect and the tree will be adaptively terminated if adaptive depth termination is supported
     * by the illumination model.
     */
    public static float CUTOFF_CONTRIBUTION = 0.002f;

    // turn on/off validity checks that would normally be unnecessary if all the lights and objects
    //  were behaving correctly.
    public static final boolean VALIDITY_CHECKING = true;

}
