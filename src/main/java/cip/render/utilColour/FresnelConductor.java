/*
 * FresnelConductor.java
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
public class FresnelConductor extends Fresnel {
    protected RGBf m_rgb;          // the material RGB
    protected float m_Ro;           // the average reflectance
    protected float m_n;            // the index of refraction
    protected float m_k;            // the coefficient of extinction
    protected float m_n2_k2;        // precomputed n squared + k squared

    /**
     * Creates a new instance of FresnelConductor
     *
     * @param rgb (RGB, readonly) The reflectance of the material
     */
    public FresnelConductor(final RGBf rgb) {
        init(rgb, 1.0f, approxK(rgb));
    }

    public FresnelConductor(final RGBf rgb, final float n, final float k) {
        init(rgb, n, k);
    }

    private void init(final RGBf rgb, final float n, final float k) {
        m_rgb = rgb;
        m_Ro = (rgb.r + rgb.g + rgb.b) / 3.0f;
        m_n = n;
        m_k = k;
        m_n2_k2 = (n * n) + (k * k);
    }

    public float getAveReflectance() {
        return m_Ro;
    }
    
    /*
     * The implementation below works for a while, but then trashes the JVM when running
     *  on a 4 processor box with 4 rendering threads (i.e. this set of functions is being hit
     *  simultaneously by 4 threads - through calls to the approxFr) with an error that reports
     *  itself as:
     *
     *
    private float conductor_parallel(float NdotL, float NdotL2, float fNLn2)
    {
        return ((m_n2_k2 * NdotL2) - fNLn2 + 1.0f) / ((m_n2_k2 * NdotL2) + fNLn2 + 1.0f);
    }
    private float conductor_perpendicular(float NdotL, float NdotL2, float fNLn2)
    {
        return (m_n2_k2 - fNLn2 + NdotL2) / (m_n2_k2 + fNLn2 + NdotL2);
    }
    private float conductor_ave(Vector3f N, Vector3f L)
    {
        float   NdotL = N.dot(L);
        float   NdotL2 = NdotL * NdotL;
        float   fNLn2 = 2.0f * m_n * NdotL;
        return 0.5f * ( conductor_parallel(NdotL, NdotL2, fNLn2) + 
                        conductor_perpendicular(NdotL, NdotL2, fNLn2) );
    }
     */

    private float conductor_ave(final Vector3f N, final Vector3f L) {
        final float NdotL = N.dot(L);
        final float NdotL2 = NdotL * NdotL;
        final float fNLn2 = 2.0f * m_n * NdotL;
        final float a2Parallel = ((m_n2_k2 * NdotL2) - fNLn2 + 1.0f) / ((m_n2_k2 * NdotL2) + fNLn2 + 1.0f);
        final float a2Perpendicular = (m_n2_k2 - fNLn2 + NdotL2) / (m_n2_k2 + fNLn2 + NdotL2);
        return 0.5f * (a2Parallel + a2Perpendicular);
    }

    public RGBf approxFr(@NotNull final RGBf Fr, @NotNull final Vector3f N, @NotNull final Vector3f L) {
        final float fRtheta = conductor_ave(N, L);   // average reflectance at incident angle
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
        Fr.setValue(m_rgb);
        return Fr;
    }

}
