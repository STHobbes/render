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
 * Steve's wavy functional texture
 */
public class Wavy extends ADualMaterialTexture {

    /**
     * Creates a new instance of <tt>Wavy</tt>
     */
    public Wavy() {
    }

    /////////////////////////////////////////////////////////////////////////////////////
    // IRtMaterial interface implementation                          
    /////////////////////////////////////////////////////////////////////////////////////
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
        final boolean is_2d = Float.isNaN(ptTexture.z);
        if (is_2d) {
            double curve1 = 0.25f * (1.0f + (float) Math.cos(2.0 * Math.PI * ptTexture.x));
            double curve2 = 1.0f - curve1;
            if ((ptTexture.y > curve1) && (ptTexture.y < curve2)) {
                intersection.m_mtl = m_mtl1;
            }
        } else {
            ptTexture.z -= (float) Math.floor(ptTexture.z);
            double curve1 = 0.25f *
                    ((0.5f * (1.0f + (float) Math.cos(2.0 * Math.PI * ptTexture.x))) *
                            (0.5f * (1.0f + (float) Math.cos(2.0 * Math.PI * ptTexture.z))));
            double curve2 = 1.0f - curve1;
            if ((ptTexture.y > curve1) && (ptTexture.y < curve2)) {
                intersection.m_mtl = m_mtl1;
            }
        }

        intersection.m_mtl.getColor(rgb, intersection, lights, rtObjects, rtBkg, nMaxRecursions, nSample, nRandom);
    }
}
