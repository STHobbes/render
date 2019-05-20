/*
 * IRtGroup.java
 *
 * Created on November 21, 2002, 10:36 PM
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

import java.util.List;

/**
 * The interface for a group.  Objects and lights can be added to a group so that they can be
 * positioned and treated as a single unit.
 *
 * @author royster.hall@gmail.com
 * @version 1.0
 * @since 1.0
 */
public interface IRtGroup extends IRtGeometry {
    /**
     * Adds a geometry to the group.  NOTE: because this is a rendering interface as opposed to an
     * editing interfaces, it is assumed that the object is fully defined when it is added to the object
     * and will not be altered once it has been added to the object.
     *
     * @param geo a geometry,
     */
    void addGeometry(IRtGeometry geo);

    /**
     * Get a list of the geometries in this group.  This list preserves the hierarchy of the group.
     * Specifically, groups that are contained within this geometry are returned as groups.
     *
     * @return Returns the list of geometries contained in this group.
     */
    List getGeometryList();

    /**
     * Get a flattened list of the geometires in this group.  Any group hierarchy is flattened and the
     * is a list of all the transformed objects as they are positioned relative to the object axis
     * system of this geoup.
     *
     * @return Returns the flattened list of objects.
     */
    List getFlatGeometryList();

    /**
     * Add a light to the group (not yet implemented in any group implementation).
     *
     * @param lgt a light.
     */
    void addLight(IRtLight lgt);

    /**
     * Get a flattend list of lights in the group (not yet implemented in any group implementation).
     *
     * @return Returns the flattened list of lights.
     */
    List getFlatLightList();


}
