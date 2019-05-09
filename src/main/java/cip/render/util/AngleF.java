/*
 * AngleF.java
 *
 * Created on September 12, 2002, 9:30 PM
 * Copyright(c) 1993-2019 Crisis in Perspective, Inc.
 *                        PO Box 1949
 *                        Hood River, OR 97031
 *                        www.crisisinperspecive.com
 */
package cip.render.util;

/**
 * @author royster.hall@gmail.com
 * @version 1.0
 * @since 1.0
 */
public class AngleF {
    /**
     * The degrees type.  Used to indicate the angle is specified in degrees when a type is specified.
     */
    public static final int DEGREES = 0;

    /**
     * The radians type.  Used to indicate the angle is specified in radians when a type is specified.
     */
    public static final int RADIANS = 1;

    /**
     * The conversion factor for radians to degrees. Normally <tt>AngledF</tt> handles any conversions you need
     * so there is probably no reason to ever use this.
     */
    private static final float RADIANS_TO_DEGREES = 180.f / (float) Math.PI;

    /**
     * The conversion factor for degrees to radians. Normally <tt>AngledF</tt> handles any conversions you need
     * so there is probably no reason to ever use this.
     */
    public static final float DEGREES_TO_RADIANS = (float) Math.PI / 180.f;

    private float m_fDegrees;   // the internal representation is always degrees because degrees are
    //  rational numbers for common increments of a circle.  This means that
    //  there is a somewhat better chance that the value saved/reported will
    //  be closer to the intended value.

    /**
     * A pointer to tne next <tt>AngleF</tt> in a cache if this object is cached.
     */
    public AngleF m_next = null;

    //-------------------------------------------------------------------------------------------------------------------------
    /**
     * Creates a new instance of <tt>AngleF</tt>
     */
    public AngleF() {
    }

    public AngleF(final int type, final float fAngle) {
        setValue(type, fAngle);
    }

    @SuppressWarnings("CopyConstructorMissesField")
    public AngleF(final AngleF aInit) {
        setValue(aInit);
    }

    //-------------------------------------------------------------------------------------------------------------------------
    public AngleF setValue(final int type, final float fAngle) {
        if (DEGREES == type) {
            m_fDegrees = fAngle;
        } else if (RADIANS == type) {
            m_fDegrees = fAngle * RADIANS_TO_DEGREES;
        } else {
            throw new IllegalArgumentException("Unrecognized angle type");
        }
        return this;
    }

    public AngleF setValue(final AngleF aInit) {
        m_fDegrees = aInit.m_fDegrees;
        return this;
    }

    //-------------------------------------------------------------------------------------------------------------------------
    public AngleF mult(final float f) {
        m_fDegrees *= f;
        return this;
    }

    public AngleF add(final AngleF aAng) {
        m_fDegrees += aAng.m_fDegrees;
        return this;
    }

    public AngleF subtract(final AngleF aAng) {
        m_fDegrees -= aAng.m_fDegrees;
        return this;
    }

    //-------------------------------------------------------------------------------------------------------------------------
    public AngleF atan(final float fTan) {
        m_fDegrees = (float) Math.atan(fTan) * RADIANS_TO_DEGREES;
        return this;
    }

    public AngleF atan2(final float fSin, final float fCos) {
        m_fDegrees = (float) Math.atan2(fSin, fCos) * RADIANS_TO_DEGREES;
        return this;
    }

    public AngleF asin(final float fSin) {
        m_fDegrees = (float) Math.asin(fSin) * RADIANS_TO_DEGREES;
        return this;
    }

    public AngleF acos(final float fCos) {
        m_fDegrees = (float) Math.acos(fCos) * RADIANS_TO_DEGREES;
        return this;
    }

    //-------------------------------------------------------------------------------------------------------------------------
    public float getDegrees() {
        return m_fDegrees;
    }

    public void setDegrees(final float fDegrees) {
        m_fDegrees = fDegrees;
    }

    public float getRadians() {
        return m_fDegrees * DEGREES_TO_RADIANS;
    }

    public void setRadians(final float fRadians) {
        m_fDegrees = fRadians * RADIANS_TO_DEGREES;
    }

    //-------------------------------------------------------------------------------------------------------------------------
    public float cos() {
        return (float) Math.cos((double) (m_fDegrees * DEGREES_TO_RADIANS));
    }

    public float sin() {
        return (float) Math.sin((double) (m_fDegrees * DEGREES_TO_RADIANS));
    }

    public float tan() {
        return (float) Math.tan((double) (m_fDegrees * DEGREES_TO_RADIANS));
    }

    //-------------------------------------------------------------------------------------------------------------------------
    public boolean equals(final AngleF angle) {
        if (this == angle) {
            return true;
        }
        return (null != angle) && (m_fDegrees == angle.m_fDegrees);
    }

    //-------------------------------------------------------------------------------------------------------------------------
    @Override
    public boolean equals(final Object obj) {
        if ((null == obj) ||
                (getClass() != obj.getClass())) {
            return false;
        }
        return equals((AngleF) obj);
    }

    //-------------------------------------------------------------------------------------------------------------------------
    @Override
    public Object clone() throws CloneNotSupportedException {
        super.clone();
        return cloneAngleF();
    }

    //-------------------------------------------------------------------------------------------------------------------------
    public AngleF cloneAngleF() {
        return new AngleF(DEGREES, m_fDegrees);
    }
}
