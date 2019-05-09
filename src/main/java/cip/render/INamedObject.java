/*
 * INamedObject.java
 *
 * Created on October 3, 2002, 1:20 AM
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

package cip.render;

import org.jetbrains.annotations.NotNull;

/**
 * This is the interface for a named object.
 *
 * @author royster.hall@gmail.com
 * @version 1.0
 * @since 1.0
 */
public interface INamedObject {
    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Get the name of this object.
     *
     * @return The name of this object.
     */
    @NotNull
    String getName();

    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Set the name of the object.
     *
     * @param strName The name of the object.
     */
    void setName(@NotNull String strName);
}
