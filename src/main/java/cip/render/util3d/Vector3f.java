/*
 * Vector3f.java
 *
 * Created on September 11, 2002, 6:35 PM
 * Copyright(c) 1993-2019 Crisis in Perspective, Inc.
 *                        PO Box 1949
 *                        Hood River, OR 97031
 *                        www.crisisinperspecive.com
 */
package cip.render.util3d;

/**
 * A class representing a vector in 3D of single precision (the components are represented by <tt>float</tt> values),
 * hence the name <tt>Vector3f</tt>.
 * <p>
 * This class implements the basic functionality for a 3D vector required for rendering and 3D graphics use.  This class is
 * patterned after and most code adapted from the <tt>CUnitV3f</tt> class of the <b><i>JOEY</i></b> toolkit written and
 * distributed by Crisis in Perspective, Inc.
 *
 * @author royster.hall@gmail.com
 * @version 1.0
 * @since 1.0
 */
public class Vector3f {
    /**
     * A pointer to tne next <tt>Vector3f</tt> in a cache if this object is cached.
     */
    public Vector3f m_next = null;

    /**
     * The I component of the vector.
     */
    public float i;
    /**
     * The J component of the vector.
     */
    public float j;
    /**
     * The K component of the vector.
     */
    public float k;

    /**
     * Creates a new instance of <tt>Vector3f</tt>.  This uses the default
     * initialization of the fields setting the vector to 0,0,0.
     */
    public Vector3f() {
    }

    /**
     * Creates a new instance of <tt>Vector3f</tt> with the I, J, and K components
     * set as specified.  No normalization is performed, the values are set exactly as
     * specified.
     *
     * @param i The I component of the vector.
     * @param j The J component of the vector.
     * @param k The K component of the vector.
     */
    public Vector3f(final float i, final float j, final float k) {
        setValue(i, j, k);
    }

    /**
     * Creates a new instance of <tt>Vector3f</tt> set equal to another vector.
     *
     * @param v The vector to make this new vector equal to.
     */
    @SuppressWarnings("CopyConstructorMissesField")
    public Vector3f(final Vector3f v) {
        setValue(v);
    }

    /**
     * Creates a new instance of <tt>Vector3f</tt> initialized to be a vector from <tt>ptOrg</tt> to
     * <tt>ptThru</tt>.  This length of this vector is equal to the distance from <tt>ptOrg</tt> to
     * <tt>ptThru</tt>.  The vector should be normalized if you need a direction vector.
     *
     * @param ptOrg  The start of the vector.
     * @param ptThru The end of the vector.
     */
    public Vector3f(final Point3f ptOrg, final Point3f ptThru) {
        setValue(ptThru.x - ptOrg.x, ptThru.y - ptOrg.y, ptThru.z - ptOrg.z);
    }

    /**
     * Sets the I, J, and K components as specified.  No normalization is performed, the values are set exactly as specified.
     *
     * @param i The I component of the vector.
     * @param j The J component of the vector.
     * @param k The K component of the vector.
     * @return Returns this vector with the value set as specified.
     */
    public Vector3f setValue(final float i, final float j, final float k) {
        this.i = i;
        this.j = j;
        this.k = k;
        return this;
    }

    /**
     * Sets the value of the vector to be a vector from <tt>ptOrg</tt> to <tt>ptThru</tt>.  This
     * length of this vector is equal to the distance from <tt>ptOrg</tt> to <tt>ptThru</tt>.  You should
     * normalized this vector if you need a direction vector.
     *
     * @param ptOrg  The start of the vector.
     * @param ptThru The end of the vector.
     * @return Returns this vector with the value set as specified.
     */
    public Vector3f setValue(final Point3f ptOrg, final Point3f ptThru) {
        return setValue(ptThru.x - ptOrg.x, ptThru.y - ptOrg.y, ptThru.z - ptOrg.z);
    }

    /**
     * Sets the value of this vector (each of the components) to be equal to some other vector, <tt>v</tt>.
     *
     * @param v The vector to make this vector equal to.  The value of this vector is unchanged.
     * @return Returns this vector with its components set equal to vector <tt>v</tt>.
     */
    public Vector3f setValue(final Vector3f v) {
        i = v.i;
        j = v.j;
        k = v.k;
        return this;
    }

    /**
     * Reverse the direction of this vector.  This is the same as scaling the vector by -1.
     *
     * @return Returns this vector after it has been reversed.
     */
    public Vector3f reverse() {
        i = -i;
        j = -j;
        k = -k;
        return this;
    }

    /**
     * Gets the dot product between this vector and another vector, <tt>v</tt>.
     *
     * @param v The vector against which we take the dot product.  The value of this vector is unchanged.
     * @return Returns the dot product between this vector and vector <tt>v</tt>.
     */
    public float dot(final Vector3f v) {
        return (i * v.i) + (j * v.j) + (k * v.k);
    }

    /**
     * Adds a vector, <tt>v</tt>, to this vector.
     *
     * @param v The vector to be added to this vector.  The value of this vector is unchanged.
     * @return Returns this vector after vector <tt>v</tt> has been added.
     */
    public Vector3f add(final Vector3f v) {
        i += v.i;
        j += v.j;
        k += v.k;
        return this;
    }

    /**
     * Subtracts a vector, <tt>v</tt>, from this vector.
     *
     * @param v The vector to be subtracted from this vector.  The value of this vector is unchanged.
     * @return Returns this vector after vector <tt>v</tt> has been subtracted.
     */
    public Vector3f subtract(final Vector3f v) {
        i -= v.i;
        j -= v.j;
        k -= v.k;
        return this;
    }

    /**
     * Cross this vector with another vector, <tt>v</tt>.  The cross product produces a vector perpendicular to the 2 vectors
     * crossed.  Specifically, the X axis crossed with the Y axis will produce the Z axis, while the cross of the Y axis with the
     * X axis will produce the -Z axis.  NOTE: i cross j = k; j cross k = i; and k cross i = j regardless of the handedness of
     * the coordinate system being used.
     *
     * @param v The vector to be crossed with this vector.  The value of this vector is unchanged.
     * @return Returns this vector after vector <tt>v</tt> has been subtracted.
     */
    public Vector3f cross(final Vector3f v) {
        setValue((j * v.k) - (k * v.j),
                (k * v.i) - (i * v.k),
                (i * v.j) - (j * v.i));
        return this;
    }

    /**
     * Sets this vector to the the reflection of the <tt>vFrom</tt> vector around the surface normal,
     * <tt>vNormal</tt>.  NOTE: it does not matter which side of the surface is the
     * inside or the outside (i.e. the dot product between the normal and the vector
     * from the surface may be either positive or negative.
     * <p>
     * This computation is performed as described in <i>Illumination and Color in
     * Computer Generated Imagery</i> by Roy Hall, Appendix III.1 Geometry utilities.
     * <p>
     * Although the math should produce a normalized vector if the input vectors are normalized, the
     * reflection vector is re-normalized after computation to minimize the buildup of round off
     * errors.
     *
     * @param vNormal The surface normal.  The value of this vector is unchanged.
     * @param vFrom   The vector from the surface to be reflected about the normal.  The value of this vector is unchanged.
     * @return Returns this vector set to the reflection of the <tt>vFrom</tt> vector around the surface normal,
     * <tt>vNormal</tt>.
     */
    public Vector3f setToReflection(final Vector3f vNormal, final Vector3f vFrom) throws ZeroLengthVectorException {
        // get the vector from the surface that is the reflection of the vFrom vector around the vNormal
        final float fNdotV = vNormal.dot(vFrom);
        return setValue(vNormal).scale(2.0f * fNdotV).subtract(vFrom).normalize();
    }

    /**
     * Sets this vector to the the refraction of the <tt>vFrom</tt> vector at the surface
     * given by the normal <tt>vNormal</tt>.  NOTE: it does not matter which side of the surface is the
     * inside or the outside (i.e. the dot product between the normal and the vector
     * from the surface may be either positive or negative.
     * <p>
     * This computation is  performed as described in <i>Illumination and Color in
     * Computer Generated Imagery</i> by Roy Hall, Appendix III.1 Geometry utilities.
     *
     * @param vNormal The surface normal.  The value of this vector is unchanged.
     * @param vFrom   The vector from the surface for which we want the refraction (by convention, all
     *                vectors are from the surface when the computations are made).  The value of this vector is unchanged.
     * @param fN_in   The index of refraction of the material the <tt>vFrom</tt> vector is
     *                traveling through.
     * @param fN_out  The index of refraction of the material the refracted vector will be traveling
     *                through.
     * @return Returns <tt>false</tt> if there is no refracted vector (total internal
     * reflection occurs), and <tt>true</tt> if a refracted vector was computed.
     * If <tt>false</tt> is returned, the value of this vector is arbitrary and
     * it should be considered uninitialized.
     */
    public boolean setToRefraction(final Vector3f vNormal, final Vector3f vFrom, final float fN_in, final float fN_out) {
        final float fNdotV = vNormal.dot(vFrom);
        setValue(vNormal).scale(fNdotV).subtract(vFrom).scale(fN_in / fN_out);
        final float fLenNt = dot(this);
        boolean bRet = false;
        if (fLenNt < 1.0f) {
            // if fLenNt >= 1.0 then there is total internal reflection.  Ths above condition must be met for a refracted
            //  ray to be spawned from this intersection.
            float fNormalScale = (float) Math.sqrt(1.0f - fLenNt);
            if (fNdotV > 0.0f) fNormalScale = -fNormalScale;
            i += (vNormal.i * fNormalScale);
            j += (vNormal.j * fNormalScale);
            k += (vNormal.k * fNormalScale);
            bRet = true;
        }
        return bRet;
    }

    /**
     * Compute the microfacet surface normal, <b>Ht</b>, that would be required to refract light from the <b>L</b> direction
     * in the <b>T</b> direction.
     *
     * @param vL The light vector, L
     * @param vT The transmitted direction, T
     * @param ni The index of refraction for L
     * @param nt The index of refraction for T
     * @return Returns <tt>true</tt> if Ht was computed amd <tt>false</tt> if a valid Ht does not exist for this geometry
     */
    public boolean setToHt(final Vector3f vL, final Vector3f vT, final float ni, final float nt) {
        final float LdotT = -(vL.dot(vT));
        final float divisor;

        // check for special cases
        if (ni == nt) {
            // only exists if N and L are perpendicular to the surface
            if (LdotT == 1.0f) {
                setValue(vL);
                return true;
            } else {
                return false;
            }
        }
        try {
            if (ni < nt) {
                divisor = (nt / ni) - 1.0f;
                setValue(vL).add(vT).scale(1.0f / divisor).add(vT).reverse().normalize();
            } else {
                divisor = (ni / nt) - 1.0f;
                setValue(vL).add(vT).scale(1.0f / divisor).add(vL).normalize();
            }
        } catch (final ZeroLengthVectorException e) {
            return false;
        }
        return true;
    }

    /**
     * Get the length of this vector.
     *
     * @return Returns the length of this vector.  The length is always &gt;= 0.
     */
    public float getLength() {
        return ((float) (Math.sqrt((double) ((i * i) + (j * j) + (k * k)))));
    }

    /**
     * Normalize this vector - in other words, scale the vector so its length is equal to 1 and it can
     * be used as a direction vector.
     *
     * @return This vector is returned after normalization.
     * @throws ZeroLengthVectorException The exception is thrown when the length of the vector is eoo close to zero that
     *                                   normalizing the vector returns a meaningless result.
     */
    public Vector3f normalize() throws ZeroLengthVectorException {
        final float fLength = getLength();
        if (PackageConstants.isZero(fLength)) {
            throw new ZeroLengthVectorException();
        }
        return scale(1.0f / fLength);
    }

    /**
     * Scale a vector - multiply each of its components by a scaling factor.
     *
     * @param fScale The scaling factor.
     * @return Returns this vector after it has been scaled.
     */
    public Vector3f scale(final float fScale) {
        i *= fScale;
        j *= fScale;
        k *= fScale;
        return this;
    }

    /**
     * Tests another vector, <tt>v</tt>, for equality with this vector.
     *
     * @param v The vector to be tested.  This vector is unchanged.
     * @return Returns <tt>true</tt> if <tt>v</tt> is equal to this vector (identical
     * in all components), and <tt>false</tt> otherwise.
     */
    public boolean equals(final Vector3f v) {
        if (this == v) {
            return true;
        }
        return (null != v) && (i == v.i) && (j == v.j) && (k == v.k);
    }

    /**
     * Tests another object, <tt>obj</tt>, for equality with this vector.
     *
     * @param obj The object to be tested.  This object is unchanged.
     * @return Returns <tt>true</tt> if <tt>obj</tt> is equal to this vector (also a <tt>Vector3f</tt> and
     * identical in all components), and <tt>false</tt> otherwise.
     */
    public boolean equals(final Object obj) {
        if ((null == obj) ||
                (getClass() != obj.getClass())) {
            return false;
        }
        return equals((Vector3f) obj);
    }

    /**
     * Clone this vector.
     *
     * @return Returns a clone of the vector.  The clone is NOT obtained from the object cache.
     */
    public Object clone() {
        return cloneVector3f();
    }

    /**
     * Clone this vector.
     *
     * @return Returns a clone of this vector.  The clone is NOT obtained from the object cache.
     */
    public Vector3f cloneVector3f() {
        return new Vector3f(i, j, k);
    }
}
