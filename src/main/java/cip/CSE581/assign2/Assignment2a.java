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

import cip.render.IRenderScene;
import cip.render.util3d.*;
import cip.render.utilColour.RGBf;

import java.awt.*;

/**
 * This takes the simple ray renderer from {@link cip.CSE581.assign1.Assignment1} and adds ambient and diffuse shading. You may
 * want to difference it with {@link cip.CSE581.assign1.Assignment1} to see what was added.
 * <p>
 * <b>Usage:</b>
 * <pre>
 *     RenderWindow -r cip.CSE581.assign2.Assignment2a
 * </pre>
 * <p>
 *
 * @author royster.hall@gmail.com
 * @version 1.0
 * @since fall 2002
 */
public class Assignment2a implements IRenderScene {
    // the camera in world space
    private final Point3f m_ptEye = new Point3f(0.0f, -6.0f, 0.0f);
    private final Point3f m_ptTarget = new Point3f(0.0f, 0.0f, 0.0f);
    private final Vector3f m_vUp = new Vector3f(0.0f, 0.0f, 1.0f);
    private final Vector3f m_vSide = new Vector3f(1.0f, 0.0f, 0.0f);
    private final float m_fTargetWidth = 4.0f;
    // the environment
    // background colour
    final RGBf m_rgbBkg = new RGBf(0.25f, 0.25f, 0.25f);
    // ambient light
    final RGBf m_rgbAmbientLgt = new RGBf(0.1f, 0.1f, 0.1f);
    // point light
    final Point3f m_ptPointLgt = new Point3f(-12.0f, -24.0f, 12.0f);
    final RGBf m_rgbPointLgt = new RGBf(0.8f, 0.8f, 0.8f);

    // the base block
    private final Plane3f[] m_plnBase = {new Plane3f(0.0f, 0.0f, 1.0f, 1.0f),      // top
            new Plane3f(0.0f, -1.0f, 0.0f, -2.0f),      // front
            new Plane3f(1.0f, 0.0f, 0.0f, -2.0f),      // right
            new Plane3f(0.0f, 1.0f, 0.0f, -2.0f),      // back
            new Plane3f(-1.0f, 0.0f, 0.0f, -2.0f),      // left
            new Plane3f(0.0f, 0.0f, -1.0f, -1.5f)       // bottom
    };
    final ImplicitPolyhedra m_basePolyhedra = new ImplicitPolyhedra(m_plnBase, new RGBf(0.0f, 1.0f, 0.0f));

    // the gem block
    private final float m_fRoot3 = 1.0f / (float) Math.sqrt(3.0);
    private final Plane3f[] m_plnGem = {new Plane3f(m_fRoot3, m_fRoot3, m_fRoot3, 0.0f),
            new Plane3f(m_fRoot3, -m_fRoot3, m_fRoot3, -(2.0f * m_fRoot3)),
            new Plane3f(-m_fRoot3, m_fRoot3, m_fRoot3, (2.0f * m_fRoot3)),
            new Plane3f(-m_fRoot3, -m_fRoot3, m_fRoot3, 0.0f),
            new Plane3f(m_fRoot3, m_fRoot3, -m_fRoot3, -m_fRoot3),
            new Plane3f(m_fRoot3, -m_fRoot3, -m_fRoot3, -(3.0f * m_fRoot3)),
            new Plane3f(-m_fRoot3, m_fRoot3, -m_fRoot3, m_fRoot3),
            new Plane3f(-m_fRoot3, -m_fRoot3, -m_fRoot3, -m_fRoot3)
    };
    final ImplicitPolyhedra m_gemPolyhedra = new ImplicitPolyhedra(m_plnGem, new RGBf(1.0f, 0.0f, 0.0f));

    // center sphere
    final Sphere3f m_sphere1 = new Sphere3f(new Point3f(0.0f, 0.0f, 0.0f), 1.0f, new RGBf(0.0f, 0.0f, 1.0f));

    // little sphere
    final Sphere3f m_sphere2 = new Sphere3f(new Point3f(-0.6f, -1.25f, 0.6f), 0.25f, new RGBf(1.0f, 0.0f, 1.0f));

    /**
     * Creates a new instance of <tt>Assignment2a</tt>.
     */
    public Assignment2a() {
    }

    //------------------------------------------------------------------------------------------------------------------------------
    /**
     * Get the colour for a pixel (really, get the color for a view ray).
     * @param ray The ray we want the colour for.
     * @return Returns the colour seen by this ray.
     */
    Color getPixelColor(Line3f ray) {
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
                final Vector3f vLight = new Vector3f(intersection.m_pt, m_ptPointLgt).normalize();
                final float NdotL = vLight.dot(intersection.m_vNormal);
                if (NdotL > 0.0f) {
                    final RGBf rgbDiffuse = new RGBf(intersection.m_rgb).mult(m_rgbPointLgt).scale(NdotL);
                    rgb.add(rgbDiffuse);
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
    // IRenderScene interface implementation                                                                                      //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void loadScene(final String strSceneDesc) {
        // there is no loading, this method has nothing to do.
    }

    @Override
    public String getTitle() {
        return "CSE581 - assignment 2a";
    }

    //------------------------------------------------------------------------------------------------------------------------------
    @Override
    public void renderImage(final Image image) {
    }

    //------------------------------------------------------------------------------------------------------------------------------
    @Override
    public void renderScene(final Component component, final Graphics gc) {
        final Dimension dimScreen = component.getSize();
        // get the bounds of the hither plane (picture plane)
        final Rectangle rectRender = gc.getClipBounds();
        // setup the comutation of spatial locations (in XYZ) of points on the pixel plane
        final float fTargetHgt = (m_fTargetWidth * (float) dimScreen.height) / (float) dimScreen.width;
        final Point3f ptTargetUL = m_ptTarget.clonePoint3f().
                addVector(m_vSide.cloneVector3f().scale(-0.5f * m_fTargetWidth)).
                addVector(m_vUp.cloneVector3f().scale(0.5f * fTargetHgt));
        final Point3f ptPixel = new Point3f();
        final float fIncX = m_fTargetWidth / (float) dimScreen.width;
        final float fIncY = -fTargetHgt / (float) dimScreen.height;
        // create the objects to represent the ray from the eye through the pixel on the pixel plane
        //  and to represent the intersection at each pixel.  These objects are created outside the
        //  pixel loop for increased performance.
        final Line3f ray = new Line3f();
        // the loop through the horizontal lines of the display
        for (int iy = rectRender.y; iy < rectRender.y + rectRender.height; iy++) {
            // the loop through the pixels in a line
            for (int ix = rectRender.x; ix < rectRender.x + rectRender.width; ix++) {
                try {
                    // setup the ray and intersection for this pixel
                    ptPixel.setValue(ptTargetUL).
                            addVector(m_vSide.cloneVector3f().scale((float) (ix) * fIncX)).
                            addVector(m_vUp.cloneVector3f().scale((float) (iy) * fIncY));
                    ray.setValue(m_ptEye, ptPixel);
                    // get the colour of the pixel
                    gc.setColor(getPixelColor(ray));
                } catch (final Throwable t) {
                    // something bad happened - color code this pixel yellow
                    t.printStackTrace();
                    gc.setColor(Color.YELLOW);
                }
                // draw the pixel - using the rectangle fill here is less than ideal, but, there
                //  is no pixel draw for the Graphics drawing context.
                gc.drawRect(ix, iy, 1, 1);
            }
        }
    }
}


class RayIntersection {
    float m_fDist;
    Point3f m_pt;
    Vector3f m_vNormal;
    RGBf m_rgb;

    // All of the objects used by the intersection are created at construction
    public RayIntersection(RGBf rgbBkg) {
        m_fDist = Float.MAX_VALUE;
        m_pt = new Point3f();
        m_vNormal = new Vector3f();
        m_rgb = new RGBf().setValue(rgbBkg);
        ;
    }

}

class Sphere3f {
    private final Point3f m_ptCtr;
    private final float m_fRad;
    private final RGBf m_rgb;

    public Sphere3f(Point3f ptCtr, float fRad, RGBf rgb) {
        m_ptCtr = ptCtr;
        m_fRad = fRad;
        m_rgb = rgb;
    }

    public boolean rayIntersection(Line3f ray, RayIntersection intersection) {
        // see if the ray intersects the sphere from the outside - this uses the sphere intersection formula from Watt, p18, with
        //  a=0 since the ray is normalized.
        float fB = 2.0f * ((ray.m_vDir.i * (ray.m_ptOrg.x - m_ptCtr.x)) +
                (ray.m_vDir.j * (ray.m_ptOrg.y - m_ptCtr.y)) +
                (ray.m_vDir.k * (ray.m_ptOrg.z - m_ptCtr.z)));
        float fC = (m_ptCtr.x * m_ptCtr.x) + (m_ptCtr.y * m_ptCtr.y) + (m_ptCtr.z * m_ptCtr.z) +
                (ray.m_ptOrg.x * ray.m_ptOrg.x) + (ray.m_ptOrg.y * ray.m_ptOrg.y) + (ray.m_ptOrg.z * ray.m_ptOrg.z) -
                (2.0f * ((m_ptCtr.x * ray.m_ptOrg.x) + (m_ptCtr.y * ray.m_ptOrg.y) + (m_ptCtr.z * ray.m_ptOrg.z))) -
                (m_fRad * m_fRad);

        // solve for the intersection distance using the quadratic formula.  First check the determinant to make sure
        // it is greater than 0 - otherwise, there is no intersection.
        float fDet = (fB * fB) - (4.0f * fC);
        if (fDet < 0.0f) return false;  // no intersection - no solution to the quadratic equation

        float fDistTmp = 0.5f * (-fB - (float) Math.sqrt((double) fDet));
        // We got here if the ray intersects the object.  Test the intersection distance - if
        //  this intersection is behind the eye, or, is not closer than a previously computed intersection, return.
        if ((fDistTmp < 0.0f) || (fDistTmp > intersection.m_fDist)) {
            return false;
        }

        // Update the intersection structure with information for this intersection
        intersection.m_fDist = fDistTmp;
        ray.pointAtDistace(intersection.m_pt, fDistTmp);
        intersection.m_vNormal.i = (intersection.m_pt.x - m_ptCtr.x) / m_fRad;
        intersection.m_vNormal.j = (intersection.m_pt.y - m_ptCtr.y) / m_fRad;
        intersection.m_vNormal.k = (intersection.m_pt.z - m_ptCtr.z) / m_fRad;
        intersection.m_rgb.setValue(m_rgb);
        return true;
    }
}

class ImplicitPolyhedra {
    private final Plane3f[] m_planes;
    private final RGBf m_rgb;
    private final Plane3fIntersection m_plnInt = new Plane3fIntersection();

    ImplicitPolyhedra(final Plane3f[] planes, final RGBf rgb) {
        m_planes = planes;
        m_rgb = rgb;
    }

    boolean rayIntersection(final Line3f ray, final RayIntersection intersection) {
        //  This is the convex polyhedra test where we compute the distance to intersections
        //  into the planes of the polyhedra, and out of the planes of the polyhedra.  If
        //  the furthest in-to is closer than the furthest out-of, then the ray is is
        //  intersecting the polyhedra.
        float fDistIn = -Float.MAX_VALUE;
        float fDistOut = Float.MAX_VALUE;
        int nIn = -1;
        for (int ix = 0; ix < m_planes.length; ix++) {
            m_planes[ix].getIntersection(m_plnInt, ray);
            if (m_plnInt.m_nCode == Plane3fIntersection.NONE_OUTSIDE) {
                // This ray is parallel to and outside one of the planes of the polyhedra.
                //  An intersection is not possible - we don't need to do anymore testing
                return false;
            } else if (m_plnInt.m_nCode == Plane3fIntersection.NONE_INSIDE) {
                // do nothing - parallel and inside the plane, other geometry will be
                //  the determining factor
            } else if (m_plnInt.m_nCode == Plane3fIntersection.GOING_OUT_OF) {
                // going out of the plane - this is important if it is the closest 'goes out of' we've
                //  encountered so far.
                if (m_plnInt.m_fDist < fDistOut) {
                    fDistOut = m_plnInt.m_fDist;
                    if ((fDistOut < 0.0f) || (fDistIn > fDistOut)) {
                        // if this test is true, an intersection is not possible - we don't
                        //  need to do anymore testing.
                        return false;
                    }
                }
            } else {
                // going into the plane - this is important if it is the furthest 'goes into' we've
                //  encountered so far.
                if (m_plnInt.m_fDist > fDistIn) {
                    fDistIn = m_plnInt.m_fDist;
                    nIn = ix;
                    if (fDistIn > fDistOut) {
                        // if this test is true, an intersection is not possible - we don't
                        //  need to do anymore testing.
                        return false;
                    }
                }
            }
        }
        // We got here if the ray intersects the object.  Test the intersection distance - if
        //  this intersection is behind the eye, or, is not closer than a previously computed intersection, return.
        if ((fDistIn < 0.0f) || (fDistIn > intersection.m_fDist)) {
            return false;
        }

        // Update the intersection structure with information for this intersection
        intersection.m_fDist = fDistIn;
        ray.pointAtDistace(intersection.m_pt, fDistIn);
        m_planes[nIn].getNormal(intersection.m_vNormal);
        intersection.m_rgb.setValue(m_rgb);
        return true;
    }
}
