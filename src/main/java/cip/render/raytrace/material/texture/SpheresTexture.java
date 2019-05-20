package cip.render.raytrace.material.texture;

import cip.render.raytrace.RayIntersection;
import cip.render.raytrace.interfaces.IRtBackground;
import cip.render.raytrace.interfaces.IRtGeometry;
import cip.render.raytrace.interfaces.IRtLight;
import cip.render.util3d.Point3f;
import cip.render.util3d.Vector3f;
import cip.render.utilColour.RGBf;
import org.jetbrains.annotations.NotNull;

public class SpheresTexture extends Checkerboard {

    public void getColor(final @NotNull RGBf rgb, final @NotNull RayIntersection intersection, final @NotNull IRtLight[] lights, final @NotNull IRtGeometry[] rtObjects, final @NotNull IRtBackground rtBkg, final int nMaxRecursions,
                         final int nSample, final int nRandom) {
        if (!intersection.m_bTexture) {
            throw new IllegalStateException("SpheresTexture: texture coordinates have not been set");
        }

        intersection.m_ptTexture.x -= (float) Math.floor(intersection.m_ptTexture.x);
        intersection.m_ptTexture.y -= (float) Math.floor(intersection.m_ptTexture.y);
        intersection.m_ptTexture.z -= (float) Math.floor(intersection.m_ptTexture.z);
        intersection.m_mtl = m_mtl2;

        // see if pt is within sphere part
        final Point3f inter = new Point3f(intersection.m_ptTexture.x, intersection.m_ptTexture.y, intersection.m_ptTexture.z);
        final Vector3f centerToIntersection = new Vector3f(inter, new Point3f(0.5f, 0.5f, 0.5f));
        if (centerToIntersection.getLength() > 0.5f) {
            intersection.m_mtl = m_mtl1;
        }

        intersection.m_mtl.getColor(rgb, intersection, lights, rtObjects, rtBkg, nMaxRecursions, nSample, nRandom);
    }
}
