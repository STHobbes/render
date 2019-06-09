/*
 * Fresnel.java
 *
 * Created on October 28, 2002, 11:06 AM
 * Copyright(c) 1993-2019 Crisis in Perspective, Inc.
 *                        PO Box 1949
 *                        Hood River, OR 97031
 *                        www.crisisinperspecive.com
 */
package cip.render.utilColour;

import cip.render.util3d.Vector3f;
import org.jetbrains.annotations.NotNull;

/**
 * An abstract class encapsulating the Fresnel reflectance/transmittance computations for
 * a surface.
 *
 * @author royster.hall@gmail.com
 * @version 1.0
 * @since 1.0
 */
public abstract class Fresnel {
    /**
     * Creates a new instance of <tt>Fresnel</tt>
     */
    public Fresnel() {
    }

    /**
     * Approximates the index of refraction for a material from the reflectance for incident
     * light parallel to the normal.  In this approximation, the coefficient of extinction is
     * assumed to be 0.
     *
     * @param rgb The reflectance for incident light parallel to the normal.
     * @return Returns the approximated index of refraction that would produce the reflectance
     * assuming the coefficient of extinction is 0.
     */
    public static float approxN(@NotNull final RGBf rgb) {
        final float fSqrtRo = (float) Math.sqrt((rgb.r + rgb.g + rgb.b) / 3.0);
        return (1.0f + fSqrtRo) / (1.0f - fSqrtRo);
    }

    /**
     * Approximates the coefficient of extinction for a material from the reflectance for incident
     * light parallel to the normal.  In this approximation, the index of refraction is
     * assumed to be 1.
     *
     * @param rgb The reflectance for incident light parallel to the normal.
     * @return Returns the approximated coefficient of extinction that would produce the reflectance
     * assuming the index of refraction is 1.
     */
    public static float approxK(@NotNull final RGBf rgb) {
        final double dRo = (rgb.r + rgb.g + rgb.b) / 3.0;
        return 2.0f * (float) Math.sqrt(dRo / (1.0 - dRo));
    }

    /**
     * Returns the average reflectance for an incident ray parallel to the normal.
     *
     * @return Returns the average reflectance.
     */
    public abstract float getAveReflectance();

    /**
     * Approximate the Fresnel reflectance of the surface for light incident from a specific direction.
     *
     * @param Fr (RGBf, modified) The reflectance, which will be set to Fresnel reflectance and be returned.
     * @param N  (vector3f, readonly) The normal.
     * @param L  (vector3f, readonly) The direction of the light.  This vector is directed from the surface to the light.
     * @return (RGBf) Returns the approximated Fresnel reflectance (the Fr argument after the Fresnel reflectance is set).
     */
    public RGBf approxFr(@NotNull final RGBf Fr, @NotNull final Vector3f N, @NotNull final Vector3f L) {
        throw new IllegalStateException(getClass().getName() + ".approxFr(Fr,N,L) is not implemented.");
    }

    /**
     * Approximate the Fresnel reflectance and transmittance of the surface for light incident from a specific direction.
     *
     * @param Fr (RGBf, modified) The reflectance, which will be set to Fresnel reflectance and be returned.
     * @param Ft The transmittance.
     * @param N  (vector3f, readonly) The normal.
     * @param L  (vector3f, readonly) The direction of the light.  This vector is directed from the surface to the light.
     * @param T  (vector3f, readonly) The direction of the transmitted light.
     * @param bIn <tt>true</tt> if the ray is going into the material and <tt>false</tt> if
     *            the ray is going out of the material.
     * @param n   The index of refraction on the transmitted side of the material boundary (normally 1.0 for air)
     * @return (RGBf) Returns the approximated Fresnel reflectance (the Fr argument after the Fresnel reflectance is set).
     */
    public RGBf approxFrFt(@NotNull final RGBf Fr, @NotNull final RGBf Ft, @NotNull final Vector3f N, @NotNull final Vector3f L,
                           @NotNull final Vector3f T, final boolean bIn, final float n) {
        throw new IllegalStateException(getClass().getName() + ".approxFr(Fr,Ft,N,L,T,bIn,n) is not implemented.");
    }

}
