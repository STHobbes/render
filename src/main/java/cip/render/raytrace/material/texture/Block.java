/*
 * Block.java
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

package cip.render.raytrace.material.texture;

import cip.render.raytrace.RayIntersection;
import cip.render.raytrace.interfaces.IRtBackground;
import cip.render.raytrace.interfaces.IRtGeometry;
import cip.render.raytrace.interfaces.IRtLight;
import cip.render.utilColour.RGBf;
import org.jetbrains.annotations.NotNull;

/**
 * This is a student contributed texture - it seems to create a randomized block texture (like a stone wall). It seems to have
 * some numerical issues, and has not been extended to 3D. I will revise this documentation and code when I figure out what the
 * intent of this texture was, and who wrote it.
 */
public class Block extends ADualMaterialTexture {

    private final float[] rand_numbers;
    private final int rand_size = 256;

    /**
     * Creates a new instance of <tt>Block</tt>
     */
    public Block() {
        int i;

        rand_numbers = new float[rand_size];
        for (i = 0; i < rand_size; i++)
            rand_numbers[i] = (float) Math.random();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IRtMaterial interface implementation                                                                                       //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // this needs to always return the same random number when given the same 2 ints
    private float rand2d(final int a, final int b) {
        return (rand_numbers[(a + b) % rand_size] * 0.6f) + 0.2f;
    }

    public void getColor(final @NotNull RGBf rgb, final @NotNull RayIntersection intersection, final @NotNull IRtLight[] lights,
                         final @NotNull IRtGeometry[] rtObjects, final @NotNull IRtBackground rtBkg, final int nMaxRecursions,
                         final int nSample, final int nRandom) {
        final float xf;
        final float yf;
        final float mortar_width = 0.1f;
        final int xi;
        final int yi;
        boolean vert_square = false;
        final float this_offset;
        final float neighbor_offset;

        if (!intersection.m_bTexture) {
            throw new IllegalStateException("Block: texture coordinates have not been set");
        }

        intersection.m_mtl = m_mtl1;

        xf = Math.abs((intersection.m_ptTexture.x + 100000) % 1.0f);
        yf = Math.abs((intersection.m_ptTexture.y + 100000) % 1.0f);

        xi = (int) intersection.m_ptTexture.x + 100000;
        yi = (int) intersection.m_ptTexture.y + 100000;

        this_offset = rand2d(xi, yi);

        if ((xi + yi) % 2 == 0) {
            vert_square = true;

            if (Math.abs(this_offset - xf) < mortar_width)
                intersection.m_mtl = m_mtl2;
            else {
                if (xf > this_offset)  // to the right of the partition
                    neighbor_offset = rand2d(xi + 1, yi);
                else // left of partition
                    neighbor_offset = rand2d(xi - 1, yi);

                if (Math.abs(neighbor_offset - yf) < mortar_width)
                    intersection.m_mtl = m_mtl2;
            }

        } else {
            if (Math.abs(this_offset - yf) < mortar_width)
                intersection.m_mtl = m_mtl2;
            else {
                if (yf > this_offset) // above partition
                    neighbor_offset = rand2d(xi, yi + 1);
                else
                    neighbor_offset = rand2d(xi, yi - 1);

                if (Math.abs(neighbor_offset - xf) < mortar_width)
                    intersection.m_mtl = m_mtl2;
            }

        }

        // System.out.println(Float.toString(xf) + " " + Float.toString(yf));

	/* what's this for ?
    // before we go on, assign a local texture coordinate within the square
	intersection.m_ptTexture.i *= 2.0;
        intersection.m_ptTexture.i -= (float)Math.floor(intersection.m_ptTexture.i);
	intersection.m_ptTexture.j *= 2.0;
        intersection.m_ptTexture.j -= (float)Math.floor(intersection.m_ptTexture.j);
	intersection.m_ptTexture.k *= 2.0;
        intersection.m_ptTexture.k -= (float)Math.floor(intersection.m_ptTexture.k);
	*/

        intersection.m_mtl.getColor(rgb, intersection, lights, rtObjects, rtBkg, nMaxRecursions, nSample, nRandom);
    }
}
