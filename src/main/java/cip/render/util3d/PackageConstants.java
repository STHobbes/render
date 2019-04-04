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
 * @author royster.hall@gmail.com
 * @version 1.0
 * @since 1.0
 */
public class PackageConstants {
    // the range for testing closeness to zero - single precision
    public static final float ZERO_TOLERANCE_MAX_FLOAT = 1.0e-15f;
    public static final float ZERO_TOLERANCE_MIN_FLOAT = -1.0e-15f;

    public static final boolean isZero(final float fVal) {
        if ((fVal < ZERO_TOLERANCE_MAX_FLOAT) && (fVal > ZERO_TOLERANCE_MIN_FLOAT)) {
            return true;
        }
        return false;
    }

    // the range for testing closeness to zero - double precision
    public static final double ZERO_TOLERANCE_MAX_DOUBLE = 1.0e-140;
    public static final double ZERO_TOLERANCE_MIN_DOUBLE = -1.0e-140;

    public static final boolean isZero(final double dVal) {
        if ((dVal < ZERO_TOLERANCE_MAX_DOUBLE) && (dVal > ZERO_TOLERANCE_MIN_DOUBLE)) {
            return true;
        }
        return false;
    }

}
