/*
 * Wavy.java
 *
 */
package cip.render.raytrace.material.texture;

import cip.render.raytrace.RayIntersection;
import cip.render.raytrace.interfaces.IRtBackground;
import cip.render.raytrace.interfaces.IRtGeometry;
import cip.render.raytrace.interfaces.IRtLight;
import cip.render.util3d.Point3f;
import cip.render.utilColour.RGBf;
import org.jetbrains.annotations.NotNull;

/**
 * Steve Titus's wavy functional texture.
 *
 * This texture is a sine curve that divides a square into 2 materials. This sine wave is reflected in the 3 surrounding
 * squares so the pattern is continuous. Render the <tt>test/textures/test_wavy.xml</tt> environment to see this texture
 * as a naturally mapped 2D texture, and an object projection 3D texture.
 */
public class Wavy extends ADualMaterialTexture {

    /**
     * Creates a new instance of <tt>Wavy</tt>
     */
    public Wavy() {
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IRtMaterial interface implementation                                                                                       //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void getColor(final @NotNull RGBf rgb, final @NotNull RayIntersection intersection, final @NotNull IRtLight[] lights,
                         final @NotNull IRtGeometry[] rtObjects, final @NotNull IRtBackground rtBkg, final int nMaxRecursions,
                         final int nSample, final int nRandom) {
        final Point3f ptTexture = intersection.m_ptTexture;
        if (!intersection.m_bTexture) {
            throw new IllegalStateException("Wavy: texture coordinates have not been set");
        }

        ptTexture.x -= (float) Math.floor(ptTexture.x);
        ptTexture.y -= (float) Math.floor(ptTexture.y);
        intersection.m_mtl = m_mtl2;
        float curve1;
        final boolean is_2d = Float.isNaN(ptTexture.z);
        if (is_2d) {
            curve1 = 0.25f * (1.0f + (float) Math.cos(2.0 * Math.PI * ptTexture.x));
        } else {
            ptTexture.z -= (float) Math.floor(ptTexture.z);
            curve1 = 0.25f *
                    ((0.5f * (1.0f + (float) Math.cos(2.0 * Math.PI * ptTexture.x))) *
                            (0.5f * (1.0f + (float) Math.cos(2.0 * Math.PI * ptTexture.z))));
        }
        if (curve1 < 0.0f) {
            curve1 = 0.0f;
        }
        if (curve1 > 1.0f) {
            curve1 = 1.0f;
        }
        double curve2 = 1.0f - curve1;
        if ((ptTexture.y > curve1) && (ptTexture.y < curve2)) {
            intersection.m_mtl = m_mtl1;
        }

        intersection.m_mtl.getColor(rgb, intersection, lights, rtObjects, rtBkg, nMaxRecursions, nSample, nRandom);
    }
}
