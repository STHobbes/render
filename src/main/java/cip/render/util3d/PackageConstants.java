/*
 * PackageConstants.java
 *
 * Created on October 4, 2002, 10:28 AM
 * Copyright(c) 1993-2019 Crisis in Perspective, Inc.
 *                        PO Box 1949
 *                        Hood River, OR 97031
 *                        www.crisisinperspecive.com
 */
package cip.render.util3d;

/**
 * Geometric computations for graphics are fraught with corner cases, particularly around vector normalization, matrix inversion
 * singularities, and ray intersection when the intersection is near the origin of the ray. These are rooted in numerical issues
 * such as round-off errors in computing intersections, and numerical limitations in the precision of the digital mathematics
 * when near 0.
 *
 * @author royster.hall@gmail.com
 * @version 1.0
 * @since 1.0
 */
public class PackageConstants {
    /**
     * The maximum positive <tt>float</tt> value that is considered to be 0.0f for graphics.
     */
    public static final float ZERO_TOLERANCE_MAX_FLOAT = 1.0e-7f;
    /**
     * The minimum negative <tt>float</tt> value that is considered to be 0.0f for graphics.
     */
    public static final float ZERO_TOLERANCE_MIN_FLOAT = -ZERO_TOLERANCE_MAX_FLOAT;

    /**
     * Test whether a <tt>float</tt> value should be considered to be 0.0f for graphics.
     *
     * @param fVal (float) The value to be tested.
     * @return <tt>true</tt> if the value should be considered to be 0.0f, <tt>false</tt> otherwise.
     */
    public static boolean isZero(final float fVal) {
        return (fVal < ZERO_TOLERANCE_MAX_FLOAT) && (fVal > ZERO_TOLERANCE_MIN_FLOAT);
    }

    /**
     * The maximum positive <tt>double</tt> value that is considered to be 0.0 for graphics.
     */
    public static final double ZERO_TOLERANCE_MAX_DOUBLE = 1.0e-35;
    /**
     * The minimum negative <tt>double</tt> value that is considered to be 0.0 for graphics.
     */
    public static final double ZERO_TOLERANCE_MIN_DOUBLE = -ZERO_TOLERANCE_MAX_DOUBLE;

    /**
     * Test whether a <tt>double</tt> value should be considered to be 0.0 for graphics.
     *
     * @param dVal (double) The value to be tested.
     * @return <tt>true</tt> if the value should be considered to be 0.0, <tt>false</tt> otherwise.
     */
    public static boolean isZero(final double dVal) {
        return (dVal < ZERO_TOLERANCE_MAX_DOUBLE) && (dVal > ZERO_TOLERANCE_MIN_DOUBLE);
    }

}
