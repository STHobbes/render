/*
 * FresnelDielectric.java
 *
 * Created on October 28, 2002, 1:55 PM
 * Copyright(c) 1993-2019 Crisis in Perspective, Inc.
 *                        PO Box 1949
 *                        Hood River, OR 97031
 *                        www.crisisinperspecive.com
 */
package cip.render.utilColour;

import cip.render.util3d.Vector3f;
import org.jetbrains.annotations.NotNull;

/**
 * @author royster.hall@gmail.com
 * @version 1.0
 * @since 1.0
 */
public class FresnelDielectric extends Fresnel {
    protected RGBf m_rgb;      // the material RGB
    protected float m_Ro;       // the average reflectance
    protected float m_n;        // the index of refraction

    /**
     * Creates a new instance of FresnelDielectric
     *
     * @param rgb (RGB, readonly) The reflectance of the material
     */
    public FresnelDielectric(final RGBf rgb) {
        init(rgb, approxN(rgb));
    }

    public FresnelDielectric(final RGBf rgb, final float n) {
        init(rgb, n);
    }

    private void init(final RGBf rgb, final float n) {
        m_rgb = rgb;
        m_Ro = (rgb.r + rgb.g + rgb.b) / 3.0f;
        m_n = n;
    }

    public float getAveReflectance() {
        return m_Ro;
    }


    private float dielectric_ave(final Vector3f N, final Vector3f L, final Vector3f T, final boolean bIn, final float n) {
        final float NdotL = N.dot(L);
        final float NdotT = N.dot(T);
        final float ni = bIn ? n : m_n;
        final float nt = bIn ? m_n : n;
        final float fAmplParallel = ((nt * NdotL) + (ni * NdotT)) / ((nt * NdotL) - (ni * NdotT));
        final float fAmplPerpendicular = ((ni * NdotL) + (nt * NdotT)) / ((ni * NdotL) - (nt * NdotT));
        // the energy is proportional to the square of amplitude
        return 0.5f * ((fAmplParallel * fAmplParallel) + (fAmplPerpendicular * fAmplPerpendicular));
    }

    public RGBf approxFrFt(@NotNull final RGBf Fr, @NotNull final RGBf Ft, @NotNull final Vector3f N, @NotNull final Vector3f L,
                           @NotNull final Vector3f T, final boolean bIn, final float n) {
        final float fRtheta = dielectric_ave(N, L, T, bIn, n);   // average reflectance at incident angle
        final float fTmp = (fRtheta - m_Ro) / (1.0f - m_Ro);
        if ((Fr.r = m_rgb.r + ((1.0f - m_rgb.r) * fTmp)) < 0.0f) {
            Fr.r = 0.0f;
        }
        if ((Fr.g = m_rgb.g + ((1.0f - m_rgb.g) * fTmp)) < 0.0f) {
            Fr.g = 0.0f;
        }
        if ((Fr.b = m_rgb.b + ((1.0f - m_rgb.b) * fTmp)) < 0.0f) {
            Fr.b = 0.0f;
        }
        if (null != Ft) {
            Ft.r = 1.0f - Fr.r;
            Ft.g = 1.0f - Fr.g;
            Ft.b = 1.0f - Fr.b;
        }
        return Fr;
    }

}
