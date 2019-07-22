/*
 * RenXmlHierOS.java
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

import cip.render.FrameLoader;
import cip.render.raytrace.interfaces.IRtGeometry;
import cip.render.util2d.Point2f;
import cip.render.util3d.Line3f;
import cip.render.util3d.Point3f;
import cip.render.utilColour.RGBf;

import java.awt.*;
import java.util.logging.Logger;

/**
 * <p>
 * This is the {@link RenderXml} implementation
 * with the infrastructure added for oversampling and filtering.  The idea is that some quantity that we have
 * been sampling once-per-pixel will be sampled at a greater frequency, that is, it will be oversampled.
 * The oversampling comes in the form of a set of offsets from the original sample point.  For example,
 * if we use an oversampling of 2, this is a 2x2 sampling grid within each pixel.  The offsets of those
 * samples from the single pixel sample (which is the center of the pixel) is given in an over-sampling
 * offset array.  The offsets typically go from -.5 to +.5 assuming the thing you are sampling can be
 * thought of in a 'normalized' way and the sampling array expanded to fit whatever phenomena you
 * are oversampling.
 * </p>
 * <p>
 * As part of the initialization of all objects in the rendering environment, the <tt>initSampling</tt>
 * functions of all of the objects is called to tell them how many over-samples will be used and to
 * provide sampling arrays in 1, 2, and 3 dimensions.  This implementation defines references to the sampling
 * and a default set of sampling arrays.  It also provides references to sample 'jitter' arrays which are
 * set to <tt>null</tt> by default in this implementation (see {@link RenderXmlOSJ} for a jittered extension of
 * oversampling).  If you want to experiment further with jitter or alternate sampling
 * locations, the easiest path is to derive an implementation of <tt>IRenderScene</tt> from this
 * class and reset the references to the sampling and jitter arrays in your constructor (see
 * {@link RenderXmlOSJ} for an example of this).
 * <p>
 * The second part of this implementation is filtering, which reduces aliasing artifacts. The basic idea for filtering is that
 * we have a fixed number of pixels and samples per pixel and if the spatial frequency of changes in the environment being sampled
 * is greater than the sampling rate, it will create bad visual artifacts - particularly with animation (moving camera and
 * objects). In general you need to low-pass filter to eliminate the high frequency noisem and typically a filter that is twice
 * the width (half the frequency) of the display frequency does a good job. So this class implements a roughly 2x2 pixel gaussian
 * filter over the sampling grid to determine the pixel colour.
 * </p>
 * <p>
 * The XML scene description file is loaded by {@link FrameLoader}.  Refer to the
 * {@link FrameLoader} documentation for XML scene description file format and
 * conventions.
 * </p>
 *
 * @author royster.hall@gmail.com
 * @version 1.0
 * @see FrameLoader
 * @since fall 2002
 */
public class RenderXmlOS extends RenderXml {

    private static final Logger logger = Logger.getLogger(RenderXmlOS.class.getName());

    public static final int PIXEL_SAMPLES = 0;
    public static final int KERNEL_SAMPLES = 1;
    public static final int PIXEL_CONTRIBUTIONS = 2;
    public static final int[][] s_nPixelSamples = {
            {1, 9, 9},    // 1x1 - 1 sample per pixel.  For a 2 pixel kernel: contributing to this and 8 adjacent pixels; pixel collects 9 samples
            {4, 4, 16},   // 2x2 - 4 sample per pixel.  For a 2 pixel kernel: contributing to this and 8 adjacent pixels; pixel collects 16 samples
            {9, 4, 25},   // 3x3 - 9 sample per pixel.  For a 2 pixel kernel: contributing to this and 8 adjacent pixels; pixel collects 25 samples
            {16, 4, 36}}; // 4x4 - 16 sample per pixel. For a 2 pixel kernel: contributing to this and 8 adjacent pixels; pixel collects 36 samples


    //-------------------------------------------------------------------------------------------------------------------------
    // Sampling - sample point arrays, pixel filter kernels
    //-------------------------------------------------------------------------------------------------------------------------
    // each kernel is the collection of contributions for each of the samples to current and surrounding pixels

    //-------------------------------------------------------------------------------------------------------------------------
    // One sample per pixel.  The sample point is taken in the center of the pixel.  For a 1 pixel kernel, the
    //  colour of the sample is the colour of the pixel.  For a 2 pixel kernel, the sample contributes a small amount to
    //  the surrounding pixels.  The 2 pixel filter is setup so the sample in the pixel contributes 50% of the pixel
    //  colour.

    /**
     * The linear sampling array for a single sample per pixel.  It has a single sample
     * at 0. If you want to specify a different sampling array, it is best to create a
     * class derived from this and set the sampling in the constructor.
     */
    public static float[] s_sample1_1d = {0.0f};

    /**
     * The 'jitter' array for 1 dimensional sampling.  This is set to <tt>null</tt>
     * in this implementation (There is no jitter). If you want to specify a different
     * jitter array, it is best to create a
     * class derived from this and set the sampling in the constructor.
     */
    public static float[] s_random1_1d = null;

    /**
     * The 2D sampling array for a single sample per pixel.  It has a single sample
     * at 0,0. If you want to specify a different sampling array, it is best to create a
     * class derived from this and set the sampling in the constructor.
     */
    public static Point2f[] s_sample1_2d = {new Point2f(0.0f, 0.0f)};

    /**
     * The 'jitter' array for 2 dimensional sampling.  This is set to <tt>null</tt>
     * in this implementation (There is no jitter). If you want to specify a different
     * jitter array, it is best to create a
     * class derived from this and set the sampling in the constructor.
     */
    public static Point2f[] s_random1_2d = null;
    public static Point3f[] s_sample1_3d = {new Point3f(0.0f, 0.0f, 0.0f)};
    public static Point3f[] s_random1_3d = null;
    // a single sample per pixel - no filter kernel
    static final KernelSample[][] s_kernel1_1 = {{new KernelSample(0, 0, 1.0000f)}};
    // a single sample per pixel, filtering over all of the adjacent pixels
    static final KernelSample[][] s_kernel1_2 = {{new KernelSample(-1, -1, 0.025f),
            new KernelSample(0, -1, 0.100f),
            new KernelSample(1, -1, 0.025f),
            new KernelSample(-1, 0, 0.100f),
            new KernelSample(0, 0, 0.500f),
            new KernelSample(1, 0, 0.100f),
            new KernelSample(-1, 1, 0.025f),
            new KernelSample(0, 1, 0.100f),
            new KernelSample(1, 1, 0.025f)}};

    //-------------------------------------------------------------------------------------------------------------------------
    // 2x2 or 4 samples per pixel.  The sample points are taken in the center of the four quadrants of the pixel.  For a 1 pixel
    //  kernel, the colour of the pixel is the average of the 4 samples in the pixel.  For a 2 pixel kernel, each sample contributes
    //  a small amount to the 3 pixels adjacent to the quadrant of the sample pixels.  The 2 pixel filter is setup so the samples in
    //  the pixel contributes 50% of the pixel colour.
    public static float[] s_sample2_1d = {-0.375f, -0.125f, 0.125f, 0.375f};
    public static float[] s_random2_1d = null;
    public static Point2f[] s_sample2_2d = {new Point2f(-0.25f, -0.25f),
            new Point2f(0.25f, -0.25f),
            new Point2f(-0.25f, 0.25f),
            new Point2f(0.25f, 0.25f)};
    public static Point2f[] s_random2_2d = null;
    public static Point3f[] s_sample2_3d = {new Point3f(0.25f, 0.25f, 0.25f),
            new Point3f(-0.25f, 0.25f, -0.25f),
            new Point3f(0.25f, -0.25f, -0.25f),
            new Point3f(-0.25f, -0.25f, 0.25f)};
    public static Point3f[] s_random2_3d = null;
    // 2x2 oversampling per pixel - no filter kernel                                                     
    static final KernelSample[][] s_kernel2_1 = {
            {new KernelSample(0, 0, 0.2500f)},
            {new KernelSample(0, 0, 0.2500f)},
            {new KernelSample(0, 0, 0.2500f)},
            {new KernelSample(0, 0, 0.2500f)}};

    static final KernelSample[][] s_kernel2_2 = {
            {new KernelSample(-1, -1, 0.025f),
                    new KernelSample(0, -1, 0.050f),
                    new KernelSample(-1, 0, 0.050f),
                    new KernelSample(0, 0, 0.125f)},
            {new KernelSample(1, -1, 0.025f),
                    new KernelSample(0, -1, 0.050f),
                    new KernelSample(1, 0, 0.050f),
                    new KernelSample(0, 0, 0.125f)},
            {new KernelSample(-1, 1, 0.025f),
                    new KernelSample(0, 1, 0.050f),
                    new KernelSample(-1, 0, 0.050f),
                    new KernelSample(0, 0, 0.125f)},
            {new KernelSample(1, 1, 0.025f),
                    new KernelSample(0, 1, 0.050f),
                    new KernelSample(1, 0, 0.050f),
                    new KernelSample(0, 0, 0.125f)}};

    // 3x3 or 9 samples per pixel.  The pixel is divided into a 3x3 grid and the sample taken in the middle of each grid square.
    //  For a 1 pixel kernel, the colour of the pixel is the average of the 9 samples in the pixel.  For a 2 pixel kernel, each
    //  sample along an edge contributes either to it's single adjacent pixel (the mid-side samples) or the 3 adjacent pixels
    //  (the corner samples).  The samples within the pixel contribute 49/81 (slightly over half) of the pixel colour.
    public static float[] s_sample3_1d = {-8.0f / 18.0f, -6.0f / 18.0f, -4.0f / 18.0f, -2.0f / 18.0f, 0.0f,
            2.0f / 18.0f, 4.0f / 18.0f, 6.0f / 18.0f, 8.0f / 18.0f};
    public static float[] s_random3_1d = null;
    public static Point2f[] s_sample3_2d = {new Point2f(-0.3333333f, -0.3333333f),
            new Point2f(0.0000000f, -0.3333333f),
            new Point2f(0.3333333f, -0.3333333f),
            new Point2f(-0.3333333f, 0.0000000f),
            new Point2f(0.0000000f, 0.0000000f),
            new Point2f(0.3333333f, 0.0000000f),
            new Point2f(-0.3333333f, 0.3333333f),
            new Point2f(0.0000000f, 0.3333333f),
            new Point2f(0.3333333f, 0.3333333f)};
    public static Point2f[] s_random3_2d = null;
    public static Point3f[] s_sample3_3d = {new Point3f(0.0f, 0.0f, 0.0f),
            new Point3f(-0.3f, -0.3f, -0.3f),
            new Point3f(0.3f, -0.3f, -0.3f),
            new Point3f(-0.3f, 0.3f, -0.3f),
            new Point3f(0.3f, 0.3f, -0.3f),
            new Point3f(-0.3f, -0.3f, 0.3f),
            new Point3f(0.3f, -0.3f, 0.3f),
            new Point3f(-0.3f, 0.3f, 0.3f),
            new Point3f(0.3f, 0.3f, 0.3f)};
    public static Point3f[] s_random3_3d = null;
    // 3x3 oversampling per pixel - no filter kernel                                                     
    static final KernelSample[][] s_kernel3_1 = {
            {new KernelSample(0, 0, 1.0f / 9.0f)},
            {new KernelSample(0, 0, 1.0f / 9.0f)},
            {new KernelSample(0, 0, 1.0f / 9.0f)},
            {new KernelSample(0, 0, 1.0f / 9.0f)},
            {new KernelSample(0, 0, 1.0f / 9.0f)},
            {new KernelSample(0, 0, 1.0f / 9.0f)},
            {new KernelSample(0, 0, 1.0f / 9.0f)},
            {new KernelSample(0, 0, 1.0f / 9.0f)},
            {new KernelSample(0, 0, 1.0f / 9.0f)}};

    static final KernelSample[][] s_kernel3_2 = {
            {new KernelSample(-1, -1, 1.0f / 81.0f),     // UL
                    new KernelSample(0, -1, 2.0f / 81.0f),
                    new KernelSample(-1, 0, 2.0f / 81.0f),
                    new KernelSample(0, 0, 4.0f / 81.0f)},
            {new KernelSample(0, -1, 3.0f / 81.0f),     // UM
                    new KernelSample(0, 0, 6.0f / 81.0f),
                    new KernelSample(0, 0, 0.0f),
                    new KernelSample(0, 0, 0.0f)},
            {new KernelSample(1, -1, 1.0f / 81.0f),     // UR
                    new KernelSample(0, -1, 2.0f / 81.0f),
                    new KernelSample(1, 0, 2.0f / 81.0f),
                    new KernelSample(0, 0, 4.0f / 81.0f)},
            {new KernelSample(-1, 0, 3.0f / 81.0f),     // ML
                    new KernelSample(0, 0, 6.0f / 81.0f),
                    new KernelSample(0, 0, 0.0f),
                    new KernelSample(0, 0, 0.0f)},
            {new KernelSample(0, 0, 9.0f / 81.0f),     // MM
                    new KernelSample(0, 0, 0.0f),
                    new KernelSample(0, 0, 0.0f),
                    new KernelSample(0, 0, 0.0f)},
            {new KernelSample(1, 0, 3.0f / 81.0f),     // MR
                    new KernelSample(0, 0, 6.0f / 81.0f),
                    new KernelSample(0, 0, 0.0f),
                    new KernelSample(0, 0, 0.0f)},
            {new KernelSample(-1, 1, 1.0f / 81.0f),     // LL
                    new KernelSample(0, 1, 2.0f / 81.0f),
                    new KernelSample(-1, 0, 2.0f / 81.0f),
                    new KernelSample(0, 0, 4.0f / 81.0f)},
            {new KernelSample(0, 1, 3.0f / 81.0f),     // LM
                    new KernelSample(0, 0, 6.0f / 81.0f),
                    new KernelSample(0, 0, 0.0f),
                    new KernelSample(0, 0, 0.0f)},
            {new KernelSample(1, 1, 1.0f / 81.0f),     // LR
                    new KernelSample(0, 1, 2.0f / 81.0f),
                    new KernelSample(1, 0, 2.0f / 81.0f),
                    new KernelSample(0, 0, 4.0f / 81.0f)}};


    // 4x4 or 16 samples per pixel.  The pixel is divided into a 4x4 grid and the sample taken in the middle of each grid square.
    //  For a 1 pixel kernel, the colour of the pixel is the average of the 16 samples in the pixel.  For a 2 pixel kernel, each
    //  sample along an edge contributes either to it's single adjacent pixel (the mid-side samples) or the 3 adjacent pixels
    //  (the corner samples).  The samples within the pixel contribute 100/144 (slightly over 2/3) of the pixel colour.
    public static float[] s_sample4_1d = {-15.0f / 32.0f, -13.0f / 32.0f, -11.0f / 32.0f, -9.0f / 32.0f,
            -7.0f / 32.0f, -5.0f / 32.0f, -3.0f / 32.0f, -1.0f / 32.0f,
            1.0f / 32.0f, 3.0f / 32.0f, 5.0f / 32.0f, 7.0f / 32.0f,
            9.0f / 32.0f, 11.0f / 32.0f, 13.0f / 32.0f, 15.0f / 32.0f};
    public static float[] s_random4_1d = null;
    public static Point2f[] s_sample4_2d = {new Point2f(-0.375f, -0.375f),
            new Point2f(-0.125f, -0.375f),
            new Point2f(0.125f, -0.375f),
            new Point2f(0.375f, -0.375f),
            new Point2f(-0.375f, -0.125f),
            new Point2f(-0.125f, -0.125f),
            new Point2f(0.125f, -0.125f),
            new Point2f(0.375f, -0.125f),
            new Point2f(-0.375f, 0.125f),
            new Point2f(-0.125f, 0.125f),
            new Point2f(0.125f, 0.125f),
            new Point2f(0.375f, 0.125f),
            new Point2f(-0.375f, 0.375f),
            new Point2f(-0.125f, 0.375f),
            new Point2f(0.125f, 0.375f),
            new Point2f(0.375f, 0.375f),};
    public static Point2f[] s_random4_2d = null;
    public static Point3f[] s_sample4_3d = {new Point3f(0.2f, 0.2f, 0.2f),
            new Point3f(-0.2f, -0.2f, -0.2f),
            new Point3f(-0.3f, -0.3f, -0.3f),
            new Point3f(0.3f, -0.3f, -0.3f),
            new Point3f(-0.3f, 0.3f, -0.3f),
            new Point3f(0.3f, 0.3f, -0.3f),
            new Point3f(-0.3f, -0.3f, 0.3f),
            new Point3f(0.3f, -0.3f, 0.3f),
            new Point3f(-0.3f, 0.3f, 0.3f),
            new Point3f(0.3f, 0.3f, 0.3f),
            new Point3f(0.35f, 0.0f, 0.0f),
            new Point3f(-0.35f, 0.0f, 0.0f),
            new Point3f(0.0f, 0.35f, 0.0f),
            new Point3f(0.0f, -0.35f, 0.0f),
            new Point3f(0.0f, 0.0f, 0.35f),
            new Point3f(0.0f, 0.0f, -0.35f)};
    public static Point3f[] s_random4_3d = null;
    // 3x3 oversampling per pixel - no filter kernel                                                     
    static final KernelSample[][] s_kernel4_1 = {{new KernelSample(0, 0, 0.0625f)},
            {new KernelSample(0, 0, 0.0625f)},
            {new KernelSample(0, 0, 0.0625f)},
            {new KernelSample(0, 0, 0.0625f)},
            {new KernelSample(0, 0, 0.0625f)},
            {new KernelSample(0, 0, 0.0625f)},
            {new KernelSample(0, 0, 0.0625f)},
            {new KernelSample(0, 0, 0.0625f)},
            {new KernelSample(0, 0, 0.0625f)},
            {new KernelSample(0, 0, 0.0625f)},
            {new KernelSample(0, 0, 0.0625f)},
            {new KernelSample(0, 0, 0.0625f)},
            {new KernelSample(0, 0, 0.0625f)},
            {new KernelSample(0, 0, 0.0625f)},
            {new KernelSample(0, 0, 0.0625f)},
            {new KernelSample(0, 0, 0.0625f)}};

    static final KernelSample[][] s_kernel4_2 = {
            {new KernelSample(-1, -1, 1.0f / 144.0f),     // UL
                    new KernelSample(0, -1, 2.0f / 144.0f),
                    new KernelSample(-1, 0, 2.0f / 144.0f),
                    new KernelSample(0, 0, 4.0f / 144.0f)},
            {new KernelSample(0, -1, 3.0f / 144.0f),     // UM
                    new KernelSample(0, 0, 6.0f / 144.0f),
                    new KernelSample(0, 0, 0.0f),
                    new KernelSample(0, 0, 0.0f)},
            {new KernelSample(0, -1, 3.0f / 144.0f),     // UM
                    new KernelSample(0, 0, 6.0f / 144.0f),
                    new KernelSample(0, 0, 0.0f),
                    new KernelSample(0, 0, 0.0f)},
            {new KernelSample(1, -1, 1.0f / 144.0f),     // UR
                    new KernelSample(0, -1, 2.0f / 144.0f),
                    new KernelSample(1, 0, 2.0f / 144.0f),
                    new KernelSample(0, 0, 4.0f / 144.0f)},
            {new KernelSample(-1, 0, 3.0f / 144.0f),     // ML
                    new KernelSample(0, 0, 6.0f / 144.0f),
                    new KernelSample(0, 0, 0.0f),
                    new KernelSample(0, 0, 0.0f)},
            {new KernelSample(0, 0, 9.0f / 144.0f),     // MM
                    new KernelSample(0, 0, 0.0f),
                    new KernelSample(0, 0, 0.0f),
                    new KernelSample(0, 0, 0.0f)},
            {new KernelSample(0, 0, 9.0f / 144.0f),     // MM
                    new KernelSample(0, 0, 0.0f),
                    new KernelSample(0, 0, 0.0f),
                    new KernelSample(0, 0, 0.0f)},
            {new KernelSample(1, 0, 3.0f / 144.0f),     // MR
                    new KernelSample(0, 0, 6.0f / 144.0f),
                    new KernelSample(0, 0, 0.0f),
                    new KernelSample(0, 0, 0.0f)},
            {new KernelSample(-1, 0, 3.0f / 144.0f),     // ML
                    new KernelSample(0, 0, 6.0f / 144.0f),
                    new KernelSample(0, 0, 0.0f),
                    new KernelSample(0, 0, 0.0f)},
            {new KernelSample(0, 0, 9.0f / 144.0f),     // MM
                    new KernelSample(0, 0, 0.0f),
                    new KernelSample(0, 0, 0.0f),
                    new KernelSample(0, 0, 0.0f)},
            {new KernelSample(0, 0, 9.0f / 144.0f),     // MM
                    new KernelSample(0, 0, 0.0f),
                    new KernelSample(0, 0, 0.0f),
                    new KernelSample(0, 0, 0.0f)},
            {new KernelSample(1, 0, 3.0f / 144.0f),     // MR
                    new KernelSample(0, 0, 6.0f / 144.0f),
                    new KernelSample(0, 0, 0.0f),
                    new KernelSample(0, 0, 0.0f)},
            {new KernelSample(-1, 1, 1.0f / 144.0f),     // LL
                    new KernelSample(0, 1, 2.0f / 144.0f),
                    new KernelSample(-1, 0, 2.0f / 144.0f),
                    new KernelSample(0, 0, 4.0f / 144.0f)},
            {new KernelSample(0, 1, 3.0f / 144.0f),     // LM
                    new KernelSample(0, 0, 6.0f / 144.0f),
                    new KernelSample(0, 0, 0.0f),
                    new KernelSample(0, 0, 0.0f)},
            {new KernelSample(0, 1, 3.0f / 144.0f),     // LM
                    new KernelSample(0, 0, 6.0f / 144.0f),
                    new KernelSample(0, 0, 0.0f),
                    new KernelSample(0, 0, 0.0f)},
            {new KernelSample(1, 1, 1.0f / 144.0f),     // LR
                    new KernelSample(0, 1, 2.0f / 144.0f),
                    new KernelSample(1, 0, 2.0f / 144.0f),
                    new KernelSample(0, 0, 4.0f / 144.0f)}};

    //-------------------------------------------------------------------------------------------------------------------------
    // RenderSample
    //-------------------------------------------------------------------------------------------------------------------------
    // This is the implementation of a rendering thread.  The rendering thread asks the pixel dispatcher (really a sample
    //  dispatcher) for a pixel (sample), gets its colour, and gives it to the colour collector.  Rendering threads are
    //  created when the rendering of an image starts, and finish when there are no more pixels left to render.
    class RenderSample extends RenderXml.RenderPixel {
        public RenderXmlOS m_parent;

        RenderSample(final RenderXmlOS parent) {
            super(parent);
            m_parent = parent;
        }

        @Override
        public void run() {
            // this is the actual rendering part.
            m_intersection = new RayIntersection();
            final RGBf rgb = m_intersection.borrowRGB();
            final RGBf rgbTmp = m_intersection.borrowRGB();
            // render pixels ahile there are pixels to render
            while (m_parent.dispatchPixel(this)) {
                m_parent.getSampleColor(rgb, m_ray, m_intersection, m_nSamp, m_nRandom);
                m_parent.setSampleColor(m_nX, m_nY, m_nSamp, rgb, rgbTmp);
            }
            m_intersection.returnRGB(rgbTmp);
            m_intersection.returnRGB(rgb);

            // let the main thread know we are done
            synchronized (m_parent.m_threadLock) {
                m_parent.m_threadCt--;
                if (m_parent.m_threadCt <= 0) {
                    // this is the last thread still running - the image is
                    //  done - release the main thread
                    m_parent.m_threadLock.notify();
                }
            }
        }

    }

    //-------------------------------------------------------------------------------------------------------------------------
    // RenderXmlHierarchy
    //-------------------------------------------------------------------------------------------------------------------------
    // the sampling setup - initialized to a default 1 sample per pixel and a 1 pixel filter kernel
    int m_nSampMax = 1;
    public float[] m_fSample = s_sample1_1d;
    public float[] m_fRandom = s_random1_1d;
    public Point2f[] m_pt2Sample = s_sample1_2d;
    public Point2f[] m_pt2Random = s_random1_2d;
    public Point3f[] m_pt3Sample = s_sample1_3d;
    public Point3f[] m_pt3Random = s_random1_3d;
    int m_nKernelMax = 1;
    KernelSample[][] m_pixelKernel = s_kernel1_1;
    int m_nPixelContributions = 1;

    // these are the fields that manage the pixel accumulation buffer.  The buffer is an array of pixel colours.  This
    //  array is generated on demand when the image resizes to larger than the currently allocated array.  The actual
    //  pixel accumulator is instantiated when the first sample is added in, and released when all samples have been
    //  accumulated and the pixel written to the screen.
    PixelColour m_pixColourCache = null;
    PixelColour[][] m_pixArray;

    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Creates a new instance of <tt>RenderXmlHierarchy</tt>
     */
    public RenderXmlOS() {
    }

    //-------------------------------------------------------------------------------------------------------------------------
    //  Here we do the work of getting the pixel colour.  The RayIntersection object is a cached object so that
    //  we could use this funtion in a multi-threaded environment without the need to create a new intersection
    //  object at every invocation.
    void getSampleColor(final RGBf rgb, final Line3f ray, final RayIntersection intersection, final int nSamp, final int nRandom) {
        boolean bIntersectObj = false;

        try {
            for (IRtGeometry m_rtObject : m_rtObjects) {
                if (m_rtObject.getRayIntersection(intersection, ray, false, nSamp, nRandom)) {
                    bIntersectObj = true;
                }
            }
            if (bIntersectObj) {
                intersection.m_mtl.getColor(rgb, intersection, m_rtLights, m_rtObjects, m_bkg, m_nMaxRecursions, nSamp, nRandom);
            } else {
                m_bkg.getColor(rgb, ray, null);
            }
        } catch (final Throwable t) {
            // something bad happened - color code this pixel yellow
            t.printStackTrace();
            rgb.setValue(1.0f, 1.0f, 0.0f);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IRenderScene interface implementation                                                                                      //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    protected void lclConditionLoadedEnvironment(FrameLoader frameLoader) {
        // setup the sampling
        final int nKernel = frameLoader.getSampleKernel();
        final int nSamp = frameLoader.getSamplesPerPixel();
        if (nSamp <= 1) {
            m_nSampMax = s_nPixelSamples[0][PIXEL_SAMPLES];
            m_fSample = s_sample1_1d;
            m_fRandom = s_random1_1d;
            m_pt2Sample = s_sample1_2d;
            m_pt2Random = s_random1_2d;
            m_pt3Sample = s_sample1_3d;
            m_pt3Random = s_random1_3d;
            if (nKernel <= 1) {
                m_nPixelContributions = m_nSampMax;
                m_nKernelMax = 1;
                m_pixelKernel = s_kernel1_1;
            } else {
                m_nPixelContributions = s_nPixelSamples[0][PIXEL_CONTRIBUTIONS];
                m_nKernelMax = s_nPixelSamples[0][KERNEL_SAMPLES];
                m_pixelKernel = s_kernel1_2;
            }
        } else if (nSamp == 2) {
            m_nSampMax = s_nPixelSamples[1][PIXEL_SAMPLES];
            m_fSample = s_sample2_1d;
            m_fRandom = s_random2_1d;
            m_pt2Sample = s_sample2_2d;
            m_pt2Random = s_random2_2d;
            m_pt3Sample = s_sample2_3d;
            m_pt3Random = s_random2_3d;
            if (nKernel <= 1) {
                m_nPixelContributions = m_nSampMax;
                m_nKernelMax = 1;
                m_pixelKernel = s_kernel2_1;
            } else {
                m_nPixelContributions = s_nPixelSamples[1][PIXEL_CONTRIBUTIONS];
                m_nKernelMax = s_nPixelSamples[1][KERNEL_SAMPLES];
                m_pixelKernel = s_kernel2_2;
            }
        } else if (nSamp == 3) {
            m_nSampMax = s_nPixelSamples[2][PIXEL_SAMPLES];
            m_fSample = s_sample3_1d;
            m_fRandom = s_random3_1d;
            m_pt2Sample = s_sample3_2d;
            m_pt2Random = s_random3_2d;
            m_pt3Sample = s_sample3_3d;
            m_pt3Random = s_random3_3d;
            if (nKernel <= 1) {
                m_nPixelContributions = m_nSampMax;
                m_nKernelMax = 1;
                m_pixelKernel = s_kernel3_1;
            } else {
                m_nPixelContributions = s_nPixelSamples[2][PIXEL_CONTRIBUTIONS];
                m_nKernelMax = s_nPixelSamples[2][KERNEL_SAMPLES];
                m_pixelKernel = s_kernel3_2;
            }
        } else if (nSamp >= 4) {
            m_nSampMax = s_nPixelSamples[3][PIXEL_SAMPLES];
            m_fSample = s_sample4_1d;
            m_fRandom = s_random4_1d;
            m_pt2Sample = s_sample4_2d;
            m_pt2Random = s_random4_2d;
            m_pt3Sample = s_sample4_3d;
            m_pt3Random = s_random4_3d;
            if (nKernel <= 1) {
                m_nPixelContributions = m_nSampMax;
                m_nKernelMax = 1;
                m_pixelKernel = s_kernel4_1;
            } else {
                m_nPixelContributions = s_nPixelSamples[3][PIXEL_CONTRIBUTIONS];
                m_nKernelMax = s_nPixelSamples[3][KERNEL_SAMPLES];
                m_pixelKernel = s_kernel4_2;
            }
        }

        frameLoader.initSampling(m_nSampMax, m_fSample, m_fRandom, m_pt2Sample, m_pt2Random, m_pt3Sample, m_pt3Random);

    }

    //-------------------------------------------------------------------------------------------------------------------------
    protected void lclAllocateKernelSamplingBuffer() {
        m_pixArray = new PixelColour[m_pixArrayWidth][m_pixArrayHeight];
        for (int iy = 0; iy < m_pixArrayHeight; iy++) {
            for (int ix = 0; ix < m_pixArrayWidth; ix++) {
                m_pixArray[ix][iy] = null;
            }
        }
    }

    protected void lclSetKernelDispatchBounds() {
        if (m_nPixelContributions > m_nSampMax) {
            m_nXDmin -= 1;
            m_nYDmin -= 1;
            m_nXDmax += 1;
            m_nYDmax += 1;
        }
    }

    protected void lclCreateRenderingThread() {
        new Thread(new RenderSample(this)).start();
    }

    @Override
    void lclIncrementSampling() {
        m_nSampCur++;
        if (m_nSampCur >= m_nSampMax) {
            m_nSampCur = 0;
            m_nXcur++;
            if (m_nXcur >= m_nXDmax) {
                m_nXcur = m_nXDmin;
                m_nYcur++;
            }
        }
    }


    void setSampleColor(final int nX, final int nY, final int nSamp, final RGBf rgb, final RGBf rgbTmp) {
        // this function is synchronized on m_pixArray so access to the gc is thread safe
        synchronized (m_pixArray) {
            // loop through the kernel - factor and add this colour to each of the affected pixels.
            //  if a pixel has received all of the expected contributions, write it to the screen
            for (int iK = 0; iK < m_nKernelMax; iK++) {
                final KernelSample ks = m_pixelKernel[nSamp][iK];
                if (ks.m_fWeight > 0.0f) {
                    final int nPixX = nX + ks.m_nXoff;
                    if ((nPixX >= m_nXmin) && (nPixX < m_nXmax)) {
                        final int nPixY = nY + ks.m_nYoff;
                        if ((nPixY >= m_nYmin) && (nPixY < m_nYmax)) {
                            // this is a sample that really exists (this takes care of the edge pixel problem)
                            PixelColour pc = m_pixArray[nPixX][nPixY];
                            if (null == pc) {
                                // a colour accumulator hasn't been allocated to this pixel yet - get one - from
                                //  the cache if possible.
                                pc = m_pixArray[nPixX][nPixY] = m_pixColourCache;
                                if (null != pc) {
                                    pc.m_rgb.setValue(0.0f, 0.0f, 0.0f);
                                    pc.m_nSamples = 0;
                                    m_pixColourCache = pc.m_next;
                                } else {
                                    pc = m_pixArray[nPixX][nPixY] = new PixelColour();
                                }
                            }
                            pc.m_rgb.add(rgbTmp.setValue(rgb).scale(ks.m_fWeight));
                            if (++pc.m_nSamples >= m_nPixelContributions) {
                                pc.m_rgb.clamp();
                                final Color clr = new Color(pc.m_rgb.r, pc.m_rgb.g, pc.m_rgb.b);
                                m_bi.setRGB(nPixX, nPixY, clr.getRGB());
                                m_gc.setColor(clr);
                                m_gc.drawRect(nPixX, nPixY, 0, 0);
                                // done with this accumulator - return it to the cache
                                pc.m_next = m_pixColourCache;
                                m_pixColourCache = pc;
                                m_pixArray[nPixX][nPixY] = null;

                            }
                        }
                    }
                }
            }
        }
    }
}

//----------------------------------------------------------------------------------------------------------------------------------
// KernelSample
//----------------------------------------------------------------------------------------------------------------------------------

/**
 * A class representing how a sample contributes to the pixel containing the sample, and, in the case of a multi-pixel
 * sampling kernel there will be an array of kernel samples for the contributions to surrounding pixels.
 */
class KernelSample {
    final int m_nXoff;     // the x-offset to the pixel the sample should be added to
    final int m_nYoff;     // the y-offset to the pixel the sample should be added to
    final float m_fWeight; // the weight of this sample when added to the pixel

    /**
     * Instantiate the kernel sample.
     *
     * @param nXoff   (int) The x-offset to the pixel the sample should be added to. An offset of 0 means add it to the current pixel.
     * @param nYoff   (int) The y-offset to the pixel the sample should be added to. An offset of 0 means add it to the current pixel.
     * @param fWeight (float) The weight of this sample when added to the pixel. NOTE: all samples should provide the same, total
     *                contribution to the final image. For example, if a 2x2 sampling is used, then
     *                there will be 4 samples in a pixel,  Since there are 4 samples in a pixel, each should contribute a total
     *                of 1/4 the computed color to the final image. With a 2 pixel filter kernel, each sample contributes to its
     *                pixel and the three closest pixels, i.e. for the upper-right samples, it contributes to the pixels above
     *                (y-offset=-1), to the upper right  (x-offset=1; y-offset=-1), and to the right (x-offset=1) of thea
     *                current pixel. The sum of those 4 contributions should contribute be 1/4 the computed color.
     */
    KernelSample(final int nXoff, final int nYoff, final float fWeight) {
        m_nXoff = nXoff;
        m_nYoff = nYoff;
        m_fWeight = fWeight;
    }
}

//----------------------------------------------------------------------------------------------------------------------------------
// PixelColour
//----------------------------------------------------------------------------------------------------------------------------------

/**
 *
 */
class PixelColour {
    int m_nSamples = 0;             // the number of samples that have contributed
    final RGBf m_rgb = new RGBf();  // the color at this sample
    PixelColour m_next = null;      // the next pixel colour (for object caching).
}
 

