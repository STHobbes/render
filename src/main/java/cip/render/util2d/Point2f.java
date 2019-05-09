/*
 * Point2f.java
 *
 * Created on October 13, 2002, 12:25 AM
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
package cip.render.util2d;

/**
 * @author royster.hall@gmail.com
 * @version 1.0
 * @since 1.0
 */
public class Point2f {

    public float x;
    public float y;

    /**
     * Creates a new instance of <tt>Point2f</tt> initialized to the origin.
     */
    public Point2f() {
    }

    /**
     * Creates a new instance of <tt>Point2f</tt> initialized to a specified location.
     *
     * @param x The X coordinate of the point.
     * @param y The Y coordinate of the point.
     */
    public Point2f(final float x, final float y) {
        setValue(x, y);
    }

    /**
     * Creates a new instance of <tt>Point2f</tt> initialized to a specified location.
     *
     * @param ptInit The location of the instantiated point.
     */
    public Point2f(final Point2f ptInit) {
        setValue(ptInit);
    }

    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Sets the point to a specified location.
     *
     * @param x The X coordinate of the point.
     * @param y The Y coordinate of the point.
     * @return Returns the point after setting the specified location.
     */
    public Point2f setValue(final float x, final float y) {
        this.x = x;
        this.y = y;
        return this;
    }

    /**
     * Sets the point to a specified location.
     *
     * @param ptInit The new location of the point.
     * @return Returns the point after setting the specified location.
     */
    public Point2f setValue(final Point2f ptInit) {
        x = ptInit.x;
        y = ptInit.y;
        return this;
    }
}
