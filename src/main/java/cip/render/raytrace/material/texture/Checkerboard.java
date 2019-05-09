/*
 * Checkerboard.java
 *
 * Created on November 7, 2002, 2:08 PM
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
package cip.render.raytrace.material.texture;

import cip.render.raytrace.RayIntersection;
import cip.render.raytrace.interfaces.IRtBackground;
import cip.render.raytrace.interfaces.IRtGeometry;
import cip.render.raytrace.interfaces.IRtLight;
import cip.render.utilColour.RGBf;
import org.jetbrains.annotations.NotNull;

/**
 * Maps every unit square (in 2D) or cube (in 3D) into 4 unit squares (2D) or 8 unit cubes (3D) with alternating materials in the
 * squares or cubes.  For a 2D mapping (such as natural mappings) this gives a checkerboard pattern of <tt>material1</tt> and
 * <tt>material2</tt>.  For a 3D mapping (such as a linear mapping in object space), this gives the effect of the object having
 * been sculpted out of a 3D <i>checkerboard</i> of <tt>material1</tt> and <tt>material2</tt>.  Within each mapped square
 * or cube, the textures coordinates are normalized to be in the 0,0,0 to 1,1,1 range.
 * <p>
 * The checkerboard mapping is specified as:
 * <pre>
 *     <font style="color:blue">&lt;<b>DynamicallyLoadedObject</b> class="cip.raytrace.material.texture.Checkerboard"
 *                              name="<font style="color:magenta"><i>checkerboardName</i></font>"&gt;</font>
 *         <font style="color:blue">&lt;<b>material1</b>&gt;</font>
 *             <font style="color:blue">&lt;<b>MaterialByRef</b> name="<font style="color:magenta"><i>materialName</i></font>"/&gt;</font>
 *             <font style="color:blue">&lt;<b>DynamicallyLoadedObject</b> class="<font style="color:magenta"><i>materialClass</i></font>"&gt;</font>
 *                   <font style="color:gray"><b>.</b>
 *                 <i>material specific nodes and attributes</i>
 *                   <b>.</b></font>
 *             <font style="color:blue">&lt;/<b>DynamicallyLoadedObject</b>&gt;</font>
 *         <font style="color:blue">&lt;/<b>material1</b>&gt;</font>
 *         <font style="color:blue">&lt;<b>material2</b>&gt;</font>
 *             <font style="color:blue">&lt;<b>MaterialByRef</b> name="<font style="color:magenta"><i>materialName</i></font>"/&gt;</font>
 *             <font style="color:blue">&lt;<b>DynamicallyLoadedObject</b> class="<font style="color:magenta"><i>materialClass</i></font>"&gt;</font>
 *                   <font style="color:gray"><b>.</b>
 *                 <i>material specific nodes and attributes</i>
 *                   <b>.</b></font><br>
 *             <font style="color:blue">&lt;/<b>DynamicallyLoadedObject</b>&gt;</font>
 *         <font style="color:blue">&lt;/<b>material2</b>&gt;</font>
 *     <font style="color:blue">&lt;/<b>DynamicallyLoadedObject</b>&gt;</font>
 * </pre>
 * <table border="0" width="90%">
 * <caption style="text-align:left">where:</caption>
 * <tr>
 * <td style="width:5%"></td>
 * <td><table border="1" summary="">
 * <tr>
 * <td><tt>material1</tt></td>
 * <td>The material in the ((i &lt; .5) and (j &lt; .5)) or ((i &gt; .5) and (j &gt; .5))
 * quadrants of a 2D mapping (texture Z = 0) of a unit square,
 * and in the ((i &lt; .5) and (j &lt; .5) and (k &lt; .5)) or ((i &gt; .5) and (j &gt; .5) and (k &lt; .5)) or
 * ((i &lt; .5) and (j &gt; .5) and (k &gt; .5)) or ((i &gt; .5) and (j &lt; .5) and (k &gt; .5)).
 * </td>
 * </tr>
 * <tr>
 * <td><tt>material2</tt></td>
 * <td>The material in the quadrants that aren't <tt>material1</tt>.
 * </td>
 * </tr>
 * <tr>
 * <td><tt>MaterialByRef</tt></td>
 * <td>A material specfied by reference to the name of a previously loaded material.  <tt>MaterialByRef</tt> is
 * mutually exclusive with the <tt>DynamicallyLoadedObject</tt> specification of a material.  If no material
 * is specified the mapping will generate an exception during rendering.
 * </td>
 * </tr>
 * <tr>
 * <td><tt>DynamicallyLoadedObject</tt></td>
 * <td>The specification fof a material for the sphere.  <tt>MaterialByRef</tt> is
 * mutually exclusive with the <tt>DynamicallyLoadedObject</tt> specification of a material.  The dynamically
 * loaded object must implement the  {@link cip.render.raytrace.interfaces.IRtMaterial} interface.  If no material
 * is specified the mapping will generate an exception during rendering.
 * </td>
 * </tr>
 * </table>
 * </td>
 * </tr>
 * </table>
 *
 * @author royster.hall@gmail.com
 * @version 1.0
 * @since 1.0
 */
public class Checkerboard extends ADualMaterialTexture {

    /**
     * Creates a new instance of <tt>Checkerboard</tt>
     */
    public Checkerboard() {
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IRtMaterial interface implementation                                                                                  //
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void getColor(final @NotNull RGBf rgb, final @NotNull RayIntersection intersection,
                         final @NotNull IRtLight[] lights, final @NotNull IRtGeometry[] rtObjects,
                         final @NotNull IRtBackground rtBkg, final int nMaxRecursions, final int nSample, final int nRandom) {
        if (!intersection.m_bTexture) {
            throw new IllegalStateException("Checkerboard: texture coordinates have not been set");
        }
        // the checkerboard is really simple, we just take every 1 unit square and check
        //  which quadrant of the square we are in and assign a material
        intersection.m_ptTexture.x -= (float) Math.floor(intersection.m_ptTexture.x);
        intersection.m_ptTexture.y -= (float) Math.floor(intersection.m_ptTexture.y);
        intersection.m_ptTexture.z -= (float) Math.floor(intersection.m_ptTexture.z);
        intersection.m_mtl = m_mtl2;

        if (((intersection.m_ptTexture.x < 0.5f) && (intersection.m_ptTexture.y < 0.5f) && (intersection.m_ptTexture.z < 0.5f)) ||
                ((intersection.m_ptTexture.x > 0.5f) && (intersection.m_ptTexture.y > 0.5f) && (intersection.m_ptTexture.z < 0.5f)) ||
                ((intersection.m_ptTexture.x < 0.5f) && (intersection.m_ptTexture.y > 0.5f) && (intersection.m_ptTexture.z > 0.5f)) ||
                ((intersection.m_ptTexture.x > 0.5f) && (intersection.m_ptTexture.y < 0.5f) && (intersection.m_ptTexture.z > 0.5f))) {
            intersection.m_mtl = m_mtl1;
        }

        // before we go on, assign a local texture coordinate within the square
        intersection.m_ptTexture.x *= 2.0;
        intersection.m_ptTexture.x -= (float) Math.floor(intersection.m_ptTexture.x);
        intersection.m_ptTexture.y *= 2.0;
        intersection.m_ptTexture.y -= (float) Math.floor(intersection.m_ptTexture.y);
        intersection.m_ptTexture.z *= 2.0;
        intersection.m_ptTexture.z -= (float) Math.floor(intersection.m_ptTexture.z);

        intersection.m_mtl.getColor(rgb, intersection, lights, rtObjects, rtBkg, nMaxRecursions, nSample, nRandom);
    }
}
