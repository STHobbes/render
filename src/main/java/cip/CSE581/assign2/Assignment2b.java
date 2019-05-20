/*
 * Assignment1.java
 *
 * Created on September 9, 2002, 12:05 PM
 * Copyright(c) 1993-2019 Crisis in Perspective, Inc.
 *                        618 NW 12th Avenue
 *                        Portland, OR 97209
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
package cip.CSE581.assign2;

import cip.render.util3d.Line3f;
import cip.render.utilColour.RGBf;

import java.awt.*;

/**
 * This takes the simple ray renderer from {@link cip.CSE581.assign2.Assignment2a} and adds ambient and diffuse shading. You may
 * want to difference it with {@link cip.CSE581.assign2.Assignment2a} to see what was added.
 * <p>
 * <b>Usage:</b>
 * <pre>
 *     RenderWindow -r cip.CSE581.assign2.Assignment2b
 * </pre>
 * <p>
 *
 * @author royster.hall@gmail.com
 * @version 1.0
 * @since fall 2002
 */
public class Assignment2b extends Assignment2a {
    /**
     * Creates a new instance of <tt>Assignment2a</tt>.
     */
    public Assignment2b() {
    }

    @Override
    public String getTitle() {
        return "CSE581 - assignment 2b";
    }

    //------------------------------------------------------------------------------------------------------------------------------

    /**
     * Get the colour for a pixel (really, get the color for a view ray).
     *
     * @param ray The ray we want the colour for.
     * @return Returns the colour seen by this ray.
     */
    @Override
    Color getPixelColor(final Line3f ray) {
        boolean bIntersectObj = false;
        Color clr;
        final RayIntersection intersection = new RayIntersection(m_rgbBkg);

        try {
            if (m_sphere1.rayIntersection(ray, intersection)) {
                bIntersectObj = true;
            }
            if (m_sphere2.rayIntersection(ray, intersection)) {
                bIntersectObj = true;
            }
            if (m_basePolyhedra.rayIntersection(ray, intersection)) {
                bIntersectObj = true;
            }
            if (m_gemPolyhedra.rayIntersection(ray, intersection)) {
                bIntersectObj = true;
            }
            if (bIntersectObj) {
                // We have a surface intersection, lets determine a color for it
                // compute the ambient
                final RGBf rgb = new RGBf(intersection.m_rgb).mult(m_rgbAmbientLgt);
                // add the diffuse
                final Line3f rayLight = new Line3f(intersection.m_pt, m_ptPointLgt);
                final RayIntersection lightIntersection = new RayIntersection(m_rgbBkg);
                // If the light can get to the intersection then add the diffuse
                lightIntersection.m_fDist = intersection.m_pt.getDistanceTo(m_ptPointLgt);
                if (!m_sphere1.rayIntersection(rayLight, lightIntersection) &&
                        !m_sphere2.rayIntersection(rayLight, lightIntersection) &&
                        !m_basePolyhedra.rayIntersection(rayLight, lightIntersection) &&
                        !m_gemPolyhedra.rayIntersection(rayLight, lightIntersection)) {
                    final float NdotL = rayLight.m_vDir.dot(intersection.m_vNormal);
                    if (NdotL > 0.0f) {
                        final RGBf rgbDiffuse = new RGBf(intersection.m_rgb).mult(m_rgbPointLgt).scale(NdotL);
                        rgb.add(rgbDiffuse);
                    }
                }
                intersection.m_rgb.setValue(rgb);
            }
            clr = new Color(intersection.m_rgb.r, intersection.m_rgb.g, intersection.m_rgb.b);
        } catch (Throwable t) {
            // something bad happened - color code this pixel yellow
            t.printStackTrace();
            clr = Color.YELLOW;
        }

        return clr;

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IRenderScene interface implementation - inherited from Assignment2a                                                        //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}
