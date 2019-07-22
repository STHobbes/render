/*
 * RenXmlHierOSJ.java
 *
 * Created on October 26, 2002, 12:16 AM
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
package cip.render.raytrace;

import cip.render.util2d.Point2f;
import cip.render.util3d.Point3f;

import java.util.Random;

/**
 * This implementation of the {@link cip.render.IRenderScene} interface extends {@link RenderXmlOS}
 * to include a 'jitter' array for preturbing samples in either 1, 2, or 3 dimensions.  The addition
 * of 'jitter' adds noise in exchange for a reduction in aliasing artifacts.
 *
 * @author royster.hall@gmail.com
 * @version 1.0
 * @see cip.render.FrameLoader
 * @since fall 2002
 */
public class RenderXmlOSJ extends RenderXmlOS {
    static final int RANDOM_ARRAY_LENGTH = 73;
    static final boolean USE_GAUSSIAN_DISTRIBUTION = false;

    //-------------------------------------------------------------------------------------------------------------------------
    final Random m_random = new Random(0x0f0f0f0f);
    // 1x1 sampling
    public float[] m_random1_1d = new float[RANDOM_ARRAY_LENGTH];
    public Point2f[] m_random1_2d = new Point2f[RANDOM_ARRAY_LENGTH];
    public Point3f[] m_random1_3d = new Point3f[RANDOM_ARRAY_LENGTH];
    // 2x2 sampling
    public float[] m_random2_1d = new float[RANDOM_ARRAY_LENGTH];
    public Point2f[] m_random2_2d = new Point2f[RANDOM_ARRAY_LENGTH];
    public Point3f[] m_random2_3d = new Point3f[RANDOM_ARRAY_LENGTH];
    // 3x3 sampling
    public float[] m_random3_1d = new float[RANDOM_ARRAY_LENGTH];
    public Point2f[] m_random3_2d = new Point2f[RANDOM_ARRAY_LENGTH];
    public Point3f[] m_random3_3d = new Point3f[RANDOM_ARRAY_LENGTH];
    // 4x4 sampling
    public float[] m_random4_1d = new float[RANDOM_ARRAY_LENGTH];
    public Point2f[] m_random4_2d = new Point2f[RANDOM_ARRAY_LENGTH];
    public Point3f[] m_random4_3d = new Point3f[RANDOM_ARRAY_LENGTH];

    /**
     * Creates a new instance of RenXmlHierOSJ
     */
    public RenderXmlOSJ() {
        // generate pseudo-random arrays for jitter sampling
        for (int ii = 0; ii < RANDOM_ARRAY_LENGTH; ii++) {
            // initialize the 1x1 sample with normalized value from -0.5 to 0.5
            if (USE_GAUSSIAN_DISTRIBUTION) {
                m_random1_1d[ii] = (float) (m_random.nextGaussian() * 0.25);
                m_random1_2d[ii] = new Point2f((float) (m_random.nextGaussian() * 0.25), (float) (m_random.nextGaussian() * 0.25));
                m_random1_3d[ii] = new Point3f((float) (m_random.nextGaussian() * 0.25), (float) (m_random.nextGaussian() * 0.25), (float) (m_random.nextGaussian() * 0.25));
            } else {
                m_random1_1d[ii] = m_random.nextFloat() - 0.5f;
                m_random1_2d[ii] = new Point2f(m_random.nextFloat() - 0.5f, m_random.nextFloat() - 0.5f);
                m_random1_3d[ii] = new Point3f(m_random.nextFloat() - 0.5f, m_random.nextFloat() - 0.5f, m_random.nextFloat() - 0.5f);
            }
            // now initialize and scale the other sampling rate arrays accordingly
            m_random2_1d[ii] = m_random1_1d[ii] / 4.0f;
            m_random3_1d[ii] = m_random1_1d[ii] / 9.0f;
            m_random4_1d[ii] = m_random1_1d[ii] / 16.0f;

            m_random2_2d[ii] = new Point2f(m_random1_2d[ii].x / 2.0f, m_random1_2d[ii].y / 2.0f);
            m_random3_2d[ii] = new Point2f(m_random1_2d[ii].x / 3.0f, m_random1_2d[ii].y / 3.0f);
            m_random4_2d[ii] = new Point2f(m_random1_2d[ii].x / 4.0f, m_random1_2d[ii].y / 4.0f);

            m_random2_3d[ii] = new Point3f(m_random1_3d[ii].x / 2.0f, m_random1_3d[ii].y / 2.0f, m_random1_3d[ii].z / 2.0f);
            m_random3_3d[ii] = new Point3f(m_random1_3d[ii].x * 0.4f, m_random1_3d[ii].y * 0.4f, m_random1_3d[ii].z * 0.4f);
            m_random4_3d[ii] = new Point3f(m_random1_3d[ii].x * 0.3f, m_random1_3d[ii].y * 0.3f, m_random1_3d[ii].z * 0.3f);
        }
        // now set the globals to these new sampling arrays
        s_random1_1d = m_random1_1d;
        s_random2_1d = m_random2_1d;
        s_random3_1d = m_random3_1d;
        s_random4_1d = m_random4_1d;

        s_random1_2d = m_random1_2d;
        s_random2_2d = m_random2_2d;
        s_random3_2d = m_random3_2d;
        s_random4_2d = m_random4_2d;

        s_random1_3d = m_random1_3d;
        s_random2_3d = m_random2_3d;
        s_random3_3d = m_random3_3d;
        s_random4_3d = m_random4_3d;
    }

    void lclIncrementSampling() {
        super.lclIncrementSampling();
        if (++m_nRandCur >= m_fRandom.length) {
            // randomly pick a start in the random arrays to prevent moire patterns.
            m_nRandCur = m_random.nextInt(RANDOM_ARRAY_LENGTH);
        }
    }

}
