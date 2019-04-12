package cip.CSE581.assign3;

import cip.render.IRenderScene;
import cip.render.util.AngleF;
import cip.render.util3d.*;
import cip.render.utilColour.RGBf;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

/**
 * This is the first part of assignment 3 - Adding reflection. The intent is
 * to to modify the code from Assignment2c to include inter-object reflection.
 * <p>
 * <b>Usage:</b>
 * <pre>
 *     RenderWindow -r cip.CSE581.assign3.Assignment3a
 * </pre>
 * <p>
 * As in other assignment 1 and 2 solutions, the code base is small enough that it is in a single file.
 *
 * @author royster.hall@gmail.com
 * @version 1.0
 * @since fall 2002
 */
public class Assignment3a implements IRenderScene {
    // -----------------------------------------------------------------------------------------------------------------------------
    // the camera in world space
    // -----------------------------------------------------------------------------------------------------------------------------
    private final Point3f m_ptEye = new Point3f(0.0f, -6.0f, 0.0f);
    private final Point3f m_ptTarget = new Point3f(0.0f, 0.0f, 0.0f);
    private final Vector3f m_vUp = new Vector3f(0.0f, 0.0f, 1.0f);
    private final Vector3f m_vSide = new Vector3f(1.0f, 0.0f, 0.0f);
    private final float m_fTargetWidth = 4.0f;

    // -----------------------------------------------------------------------------------------------------------------------------
    // the environment
    // -----------------------------------------------------------------------------------------------------------------------------
    // background colour
    final RGBf m_rgbBkg = new RGBf(0.25f, 0.25f, 0.25f);

    // the base block
    private final Material m_mtlBase = new Material(new RGBf(0.0f,1.0f,0.0f), false, new AngleF(AngleF.DEGREES,45.0f));
    private final Plane3f[] m_plnBase = {new Plane3f(0.0f, 0.0f, 1.0f, 1.0f),      // top
            new Plane3f (0.0f, -1.0f,  0.0f, -2.0f),      // front
            new Plane3f( 1.0f,  0.0f,  0.0f, -2.0f),      // right
            new Plane3f( 0.0f,  1.0f,  0.0f, -2.0f),      // back
            new Plane3f(-1.0f,  0.0f,  0.0f, -2.0f),      // left
            new Plane3f( 0.0f,  0.0f, -1.0f, -1.5f)       // bottom
    };
    final ImplicitPolyhedra m_basePolyhedra = new ImplicitPolyhedra(m_plnBase, m_mtlBase);

    // the gem block
    private final Material m_mtlGem = new Material(new RGBf(1.0f,0.0f,0.0f), true, new AngleF(AngleF.DEGREES,10.0f));
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
    private final ImplicitPolyhedra m_gemPolyhedra = new ImplicitPolyhedra(m_plnGem, m_mtlGem);

    // center sphere
    private final Material m_mtlSphere1 = new Material(new RGBf(0.0f,0.0f,1.0f), true, new AngleF(AngleF.DEGREES,9.0f));
    private final Sphere3f m_sphere1 = new Sphere3f(new Point3f(0.0f, 0.0f, 0.0f), 1.0f, m_mtlSphere1);

    // little sphere
    private final Material m_mtlSphere2 = new Material(new RGBf(1.0f,0.0f,1.0f), false, new AngleF(AngleF.DEGREES,5.0f));
    private final Sphere3f m_sphere2 = new Sphere3f(new Point3f(-0.6f, -1.25f, 0.6f), 0.25f, m_mtlSphere2);

    // the global scene lighting dimmer
    private final float m_fDimmer = 0.85f;

    // the ambient light
    private final AmbientLight m_lgtAmbient = new AmbientLight(new RGBf(0.1f,0.1f,0.1f));

    // a local light
    private final PointLight m_lgtPoint = new PointLight(new RGBf(0.8f,0.8f,0.8f), new Point3f(-12.0f,-24.0f, 12.0f));

    // a spot light
    private final SpotLight m_lgtSpot1 = new SpotLight(new RGBf(0.7f,0.7f,0.7f), new Point3f( 12.0f,-10.0f, 48.0f),
            new Vector3f(new Point3f(12.0f,-10.0f,48.0f), new Point3f(0.0f,0.0f,0.0f)).normalize(),
            new AngleF(AngleF.DEGREES,5.0f) );

    // another spot light
    private final SpotLight m_lgtSpot2 = new SpotLight(new RGBf(0.7f,0.7f,0.7f), new Point3f( 20.0f,-10.0f,10.0f),
            new Vector3f(new Point3f(20.0f,-10.0f,10.0f), new Point3f(0.0f,0.0f,0.0f)).normalize(),
            new AngleF(AngleF.DEGREES,5.0f) );

    // Put together the object and light lists
    private final IRtGeometry[] m_geometry = {m_basePolyhedra, m_gemPolyhedra, m_sphere1, m_sphere2};
    private final IRtLight[] m_lights = {m_lgtAmbient, m_lgtPoint, m_lgtSpot1, m_lgtSpot2};

    //------------------------------------------------------------------------------------------------------------------------------
    /**
     * Get the colour for a pixel (really, get the color for a view ray).
     * @param ray The ray we want the colour for.
     * @param intersection   (RayIntersection, readonly) The description of the surface - location, orientation, material, etc.
     * @param lights         (IRtLight[], readonly) The light in the scene that may affect the intersection.
     * @param rtObjects      (IRtGeometry[], readonly) The objects in the scene.
     * @param bkg            (RGBf, readonly) The background color.
     * @return Returns the colour seen by this ray.
     */
    static RGBf getPixelColor(Line3f ray, RayIntersection intersection, @NotNull IRtLight[] lights,
                               @NotNull IRtGeometry[] rtObjects, IRtGeometry lastHit, @NotNull RGBf bkg, int nMaxBounce) {
        boolean bIntersectObj = false;
        intersection.m_vToEye.setValue(ray.m_vDir).reverse();
        for (IRtGeometry geometry : rtObjects) {
            if ((geometry != lastHit) && geometry.rayIntersection(ray, intersection)) {
                bIntersectObj = true;
            }
        }
        final RGBf rgb = new RGBf(bkg);
        if (bIntersectObj) {
            intersection.m_mtl.getColor(rgb, intersection, lights,
                    rtObjects, bkg, nMaxBounce);
        }
        return rgb.clamp();

    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IRenderScene interface implementation                                                                                      //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void loadScene(String strSceneDesc) throws Exception {
        // The scene is statically defined, the only loading is applying the dimmer to the lights.
        for (IRtLight light : m_lights) {
            light.setDimmer(m_fDimmer);
        }
    }

    @Override
    public String getTitle() {
        return "CSE581 - assignment 3a";
    }

    @Override
    public void renderImage(Image image) {

    }

    @Override
    public void renderScene(Component component, Graphics gc) {
        final Dimension dimScreen = component.getSize();
        // get the bounds of the hither plane (pixel plane)
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
                    final RayIntersection intersection = new RayIntersection();
                    RGBf rgb = getPixelColor(ray, intersection, m_lights, m_geometry, null, m_rgbBkg, 5);
                    gc.setColor(new Color(rgb.r, rgb.g, rgb.b));
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

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Geometry and Light data-structures and interfaces.                                                                             //
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
interface IRtMaterial {
    /**
     * Compute the colour of a surface as seen from a specific direction.
     * @param rgb            (RGBf, modified) The computed colour at the surface.
     * @param intersection   (RayIntersection, readonly) The description of the surface - location, orientation, material, etc.
     * @param lights         (IRtLight[], readonly) The light in the scene that may affect the intersection.
     * @param rtObjects      (IRtGeometry[], readonly) The objects in the scene.
     * @param bkg            (RGBf, readonly) The background color.
     * @param nMaxBounce     (int, readonly) The maximum reflective bounces.
     */
    void getColor(@NotNull RGBf rgb, @NotNull RayIntersection intersection, @NotNull IRtLight[] lights,
                  @NotNull IRtGeometry[] rtObjects, @NotNull RGBf bkg, int nMaxBounce);
}

class Material implements IRtMaterial {
    public final RGBf m_clr;
    private final float m_Kd;
    private final float m_Ks;
    private final float m_Ns;
    private final boolean m_conductor;

    Material(RGBf color, boolean conductor, AngleF beta) {
        m_clr = color;
        m_Ns = -(float)(Math.log(2.0) / Math.log(beta.cos()));
        float fR = (m_Ns - 5.0f) / 100.0f;
        if (fR < 0.0f) { fR = 0.0f; }
        if (fR > 1.0f) { fR = 1.0f; }
        if (fR > 0.0f ) {
            // If there is recursive reflection/refraction then that is the primary source of illumination for
            //  all surfaces.  For the diffuse surfaces to look correct, some sort of reflected ray distribution or
            //  reflection filtering needs to be used to soften the reflection.
            m_Kd = 0.40f - (0.20f * (float) Math.sqrt(fR));
            m_Ks = 0.20f + (0.65f * (float) Math.sqrt(fR));
        } else {
            // If there is no recursive reflection, then the primary lighting is the only illumination.
            //  The diffuse and specular coefficients are higher in this case than they are when there is
            //  recursive reflection/refraction
            m_Kd = 0.65f - (0.30f * (float) Math.sqrt(fR));
            m_Ks = 0.05f + (0.90f * (float) Math.sqrt(fR));
        }
        m_conductor = conductor;
    }

    @Override
    public void getColor(@NotNull RGBf rgb, @NotNull RayIntersection intersection, @NotNull IRtLight[] lights,
                         @NotNull IRtGeometry[] rtObjects, @NotNull RGBf bkg, int nMaxBounce) {

        final LightInfo lightInfo = new LightInfo();
        rgb.setValue(0.0f,0.0f,0.0f);
        // Compute the light contributions to the color
        for (IRtLight light : lights) {
            if (light.getLight(lightInfo, intersection)) {
                if (lightInfo.m_nType == LightInfo.AMBIENT) {
                    rgb.add(intersection.m_mtl.m_clr).mult(lightInfo.m_rgb);
                } else if (lightInfo.m_nType == LightInfo.LOCAL) {
                    boolean bInShadow = false;
                    final Line3f rayLight = new Line3f(intersection.m_ptLocation,lightInfo.m_ptFrom);
                    for (IRtGeometry geometry : rtObjects) {
                        if (geometry.testShadow(rayLight, lightInfo.m_fDist)) {
                            bInShadow = true;
                            break;
                        }
                    }
                    if (!bInShadow) {
                        final float NdotL = rayLight.m_vDir.dot(intersection.m_vNormal);
                        if (NdotL > 0.0f) {
                            // get the diffuse part
                            final RGBf rgbDiffuse = new RGBf(intersection.m_mtl.m_clr).mult(lightInfo.m_rgb).
                                    scale(NdotL).scale(m_Kd);
                            rgb.add(rgbDiffuse);
                            // get the specular part
                            final Vector3f vH = new Vector3f(rayLight.m_vDir).add(intersection.m_vToEye).normalize();
                            final float D = (float) Math.pow((double) intersection.m_vNormal.dot(vH), m_Ns);
                            if (m_conductor) {
                                final RGBf rgbSpecular = new RGBf(intersection.m_mtl.m_clr).mult(lightInfo.m_rgb).
                                        scale(D).scale(m_Ks);
                                rgb.add(rgbSpecular);
                            } else {
                                final RGBf rgbSpecular = new RGBf(1.0f, 1.0f, 1.0f).mult(lightInfo.m_rgb).
                                        scale(D).scale(m_Ks);
                                rgb.add(rgbSpecular);
                            }
                        }
                    }
                }
            }
        }
        // If the material is reflective, add the reflected component
        if ((nMaxBounce > 0) && (m_Ks > 0.2f)) {
            Vector3f vRflRfr = new Vector3f().setToReflection(intersection.m_vNormal, intersection.m_vToEye);
            if (vRflRfr.dot(intersection.m_vNormal) <= 0.0f) {
                System.out.println("cip.raytrace.material.Whitted(): unexpected negative R.N");
            }
            Line3f reflectedRay = new Line3f().setValue(intersection.m_ptLocation, vRflRfr);
            RayIntersection intRflRfr = new RayIntersection();
            rgb.add(Assignment3a.getPixelColor(reflectedRay, intRflRfr, lights, rtObjects, intersection.m_obj,
                    bkg, nMaxBounce-1).scale(m_Ks));
        }
    }
}

class RayIntersection {
    public float        m_fDistance;    // the distance to the intersection
    public Point3f      m_ptLocation;   // the location of the intersection
    public Vector3f     m_vNormal;      // the surface normal at the intersection
    public Vector3f     m_vToEye;       // the direction to the eye or the previous intersection.
    public Material     m_mtl;          // the material of the surface
    public IRtGeometry  m_obj;          // the object at this intersection

    // All of the objects used by the intersection are created at construction
    public RayIntersection() {
        m_fDistance = Float.MAX_VALUE;
        m_ptLocation = new Point3f();
        m_vNormal = new Vector3f();
        m_vToEye = new Vector3f();
        m_mtl = null;
        m_obj = null;
    }
}

interface IRtGeometry {
    /**
     * Test for a ray intersection closer than <tt>intersection.m_fDistance</tt> and if there is a closer intersection
     * save in information about that intersection in the <tt>intersection</tt>
     * @param ray The ray being intersected.
     * @param intersection The intersection.
     * @return <tt>true</tt> if there is a closer intersection, <tt>false</tt> otherwise.
     */
    boolean rayIntersection(Line3f ray, RayIntersection intersection);

    /**
     * Test for this object blocking the light to <tt>intersection</tt>
     * @param rayToLight The ray to the light.
     * @param fDistLight The distance to the light.
     * @return <tt>true</tt> if this object casts a shadow, <tt>false</tt> otherwise.
     */
    boolean testShadow(Line3f rayToLight, float fDistLight);
}

class LightInfo {
    static public final int             AMBIENT = 0;
    static public final int             DIRECTIONAL = 1;
    static public final int             LOCAL = 2;
    public int      m_nType;                    // type from the constants
    public RGBf     m_rgb = new RGBf();         // colour (intensity)
    public float    m_fDist;                    // distance (LOCAL lights only)
    public Vector3f m_vDir = new Vector3f();    // direction (DIRECTIONAL and LOCAL)
    public Point3f  m_ptFrom = new Point3f();   // pt the light comes from (LOCAL only)
}

interface IRtLight
{
    /**
     * Set a dimmer value for the light, which is a scalar multiplier for the light intensity.
     * @param fDimmer (float) The dimming factor - usually in the range 0 to 1
     */
    void setDimmer(float fDimmer);

    /**
     * Get the lighting information describing how this light illuminates the ray intersection.  The light
     * should check the ray intersection to make sure the intersection actually faces the light, and return
     * <tt>false</tt> if the intersection cannot be illuminated by the light.
     * @param lightInfo    (LightInfo, modified) The description of the illumination of the intersection by this light.
     * @param intersection (RayIntersection, constant) The description of the ray intersection.
     * @return Returns <tt>true</tt> if this light illuminates the ray intersection and the <tt>lightInfo</tt>
     *     has been filled in with lighting information, otherwise <tt>false</tt> is returned and the <tt>lightInfo</tt>
     *     is meaningless (probably unchanged).
     */
    boolean getLight(LightInfo lightInfo, RayIntersection intersection);
}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Geometric Objects                                                                                                              //
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
class Sphere3f implements IRtGeometry {
    private final Point3f m_ptCtr;
    private final float m_fRad;
    private final Material m_mtl;

    public Sphere3f(Point3f ptCtr, float fRad, Material mtl) {
        m_ptCtr = ptCtr;
        m_fRad = fRad;
        m_mtl = mtl;
    }

    @Override
    public boolean rayIntersection(Line3f ray, RayIntersection intersection) {
        // see if the ray intersects the sphere from the outside - this uses the sphere intersection formula from Watt, p18, with
        //  a=0 since the ray is normalized.
        float fDistTmp = intersect(ray);
        if ((fDistTmp < 0.0f) || (fDistTmp > intersection.m_fDistance)) {
            return false;
        }

        // Update the intersection structure with information for this intersection
        intersection.m_fDistance = fDistTmp;
        ray.pointAtDistace(intersection.m_ptLocation, fDistTmp);
        intersection.m_vNormal.i = (intersection.m_ptLocation.x - m_ptCtr.x) / m_fRad;
        intersection.m_vNormal.j = (intersection.m_ptLocation.y - m_ptCtr.y) / m_fRad;
        intersection.m_vNormal.k = (intersection.m_ptLocation.z - m_ptCtr.z) / m_fRad;
        intersection.m_mtl = m_mtl;
        intersection.m_obj = this;
        return true;
    }

    @Override
    public boolean testShadow(Line3f rayToLight, float fDistLight) {
        float fDistTmp = intersect(rayToLight);
        return (fDistTmp > 0.0f) && (fDistTmp < fDistLight);
    }


    private float intersect(Line3f ray) {
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
        if (fDet < 0.0f) return -1.0f;  // no intersection - no solution to the quadratic equation

        return 0.5f * (-fB - (float) Math.sqrt((double)fDet));
    }
}

// ---------------------------------------------------------------------------------------------------------------------------------
class ImplicitPolyhedra implements IRtGeometry {
    private final Plane3f[] m_planes;
    private final Material m_mtl;

    ImplicitPolyhedra(final Plane3f[] planes, Material mtl) {
        m_planes = planes;
        m_mtl = mtl;
    }

    @Override
    public boolean rayIntersection(final Line3f ray, final RayIntersection intersection) {
        Plane3fIntersection plnInt = intersect(ray);
        // We got here if the ray intersects the object.  Test the intersection distance - if
        //  this intersection is behind the eye, or, is not closer than a previously computed intersection, return.
        if ((null == plnInt) || (plnInt.m_fDist < 0.0f) || (plnInt.m_fDist > intersection.m_fDistance)) {
            return false;
        }

        // Update the intersection structure with information for this intersection
        intersection.m_fDistance = plnInt.m_fDist;
        ray.pointAtDistace(intersection.m_ptLocation, plnInt.m_fDist);
        plnInt.m_plane.getNormal(intersection.m_vNormal);
        intersection.m_mtl = m_mtl;
        intersection.m_obj = this;
        return true;
    }

    @Override
    public boolean testShadow(Line3f rayToLight, float fDistLight) {
        Plane3fIntersection plnInt = intersect(rayToLight);
        return (null != plnInt) && (plnInt.m_fDist > 0.0f) && (plnInt.m_fDist < fDistLight);
    }


    private Plane3fIntersection intersect(Line3f ray) {
        //  This is the convex polyhedra test where we compute the distance to intersections
        //  into the planes of the polyhedra, and out of the planes of the polyhedra.  If
        //  the furthest in-to is closer than the furthest out-of, then the ray is is
        //  intersecting the polyhedra.
        float fDistIn = -Float.MAX_VALUE;
        float fDistOut = Float.MAX_VALUE;
        final Plane3fIntersection plnIntTmp = new Plane3fIntersection();
        Plane3fIntersection plnInt = null;
        for (int ix = 0; ix < m_planes.length; ix++) {
            m_planes[ix].getIntersection(plnIntTmp, ray);
            if (plnIntTmp.m_nCode == Plane3fIntersection.NONE_OUTSIDE) {
                // This ray is parallel to and outside one of the planes of the polyhedra.
                //  An intersection is not possible - we don't need to do anymore testing
                return null;
            } else if (plnIntTmp.m_nCode == Plane3fIntersection.NONE_INSIDE) {
                // do nothing - parallel and inside the plane, other geometry will be
                //  the determining factor
            } else if (plnIntTmp.m_nCode == Plane3fIntersection.GOING_OUT_OF) {
                // going out of the plane - this is important if it is the closest 'goes out of' we've
                //  encountered so far.
                if (plnIntTmp.m_fDist < fDistOut) {
                    fDistOut = plnIntTmp.m_fDist;
                    if ((fDistOut < 0.0f) || (fDistIn > fDistOut)) {
                        // if this test is true, an intersection is not possible - we don't
                        //  need to do anymore testing.
                        return null;
                    }
                }
            } else {
                // going into the plane - this is important if it is the furthest 'goes into' we've
                //  encountered so far.
                if (plnIntTmp.m_fDist > fDistIn) {
                    fDistIn = plnIntTmp.m_fDist;
                    if (fDistIn > fDistOut) {
                        // if this test is true, an intersection is not possible - we don't
                        //  need to do anymore testing.
                        return null;
                    }
                    if (null == plnInt) {
                        plnInt = plnIntTmp.clonePlane3fIntersection();
                    } else {
                        plnInt.setValue(plnIntTmp);
                    }
                }
            }
        }
        return plnInt;
    }

}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Lights                                                                                                                         //
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

class AmbientLight implements IRtLight {

    private final RGBf m_rgb;

    AmbientLight(@NotNull RGBf rgb) {
        m_rgb = rgb;
    }

    @Override
    public void setDimmer(float fDimmer) {
        // ambient does not dim
    }

    @Override
    public boolean getLight(LightInfo lightInfo, RayIntersection intersection) {
        lightInfo.m_nType = LightInfo.AMBIENT;
        lightInfo.m_rgb.setValue(m_rgb);
        return true;
    }
}

// ---------------------------------------------------------------------------------------------------------------------------------
class PointLight implements IRtLight {

    private final RGBf m_rgb;                       // the point light intensity
    private float m_fDimmer = 1.0f;                 // the dimmer value
    private final RGBf m_rgbDimmed = new RGBf();    // the dimmed intensity used in the scene
    private final Point3f m_location;

    PointLight(RGBf rgb, Point3f location) {
        m_rgb = rgb;
        m_location = location;
        m_rgbDimmed.setValue(rgb).scale(m_fDimmer);
    }

    @Override
    public void setDimmer(float fDimmer) {
        m_fDimmer = fDimmer;
        m_rgbDimmed.setValue(m_rgb).scale(m_fDimmer);
    }

    @Override
    public boolean getLight(LightInfo lightInfo, RayIntersection intersection) {
        final Line3f rayLight = new Line3f(intersection.m_ptLocation, m_location);
        final float NdotL = rayLight.m_vDir.dot(intersection.m_vNormal);
        if (NdotL <= 0.0f) {
            // The light is behind the surface, no illumination.
            return false;
        }
        lightInfo.m_nType = LightInfo.LOCAL;
        lightInfo.m_vDir.setValue(rayLight.m_vDir).reverse();
        lightInfo.m_rgb.setValue(m_rgbDimmed);
        lightInfo.m_ptFrom.setValue(m_location);
        lightInfo.m_fDist = intersection.m_ptLocation.getDistanceTo(m_location);
        return true;
    }
}

// ---------------------------------------------------------------------------------------------------------------------------------
class SpotLight implements IRtLight {

    private final RGBf m_rgb;                       // the spot light intensity
    private float m_fDimmer = 1.0f;                 // the dimmer value
    private final Point3f m_location;               // The location of the light
    private final Vector3f m_directiuon;            // The axis of the light
    private final AngleF m_beta;                    // The focus of the light (a small value is tightly focused).
    // derived values
    private double m_dExp = 5.0;
    private final RGBf m_rgbDimmed = new RGBf();    // the dimmed intensity used in the scene

    SpotLight(RGBf rgb, Point3f location, Vector3f direction, AngleF beta) {
        m_rgb = rgb;
        m_location = location;
        m_directiuon = direction;
        m_beta = beta;
        m_rgbDimmed.setValue(rgb).scale(m_fDimmer);
        if (m_beta.getDegrees() >= 85.0f) {
            m_dExp = 0.0f;
        } else {
            double dAngle = (double) m_beta.getDegrees();
            if (dAngle < 2.0) dAngle = 2.0;
            m_dExp = -(Math.log(2.0) / Math.log(Math.cos(dAngle * AngleF.DEGREES_TO_RADIANS)));
        }
    }

    @Override
    public void setDimmer(float fDimmer) {
        m_fDimmer = fDimmer;
        m_rgbDimmed.setValue(m_rgb).scale(m_fDimmer);
    }

    @Override
    public boolean getLight(LightInfo lightInfo, RayIntersection intersection) {
        final Line3f rayLight = new Line3f(intersection.m_ptLocation, m_location);
        final float NdotL = rayLight.m_vDir.dot(intersection.m_vNormal);
        if (NdotL <= 0.0f) {
            // The light is behind the surface, no illumination.
            return false;
        }
        // check the intersection is in the half-space illuminated by the spot
        if (intersection.m_ptLocation.y > (-PackageConstants.ZERO_TOLERANCE_MAX_FLOAT)) {
            return false;
        }

        lightInfo.m_nType = LightInfo.LOCAL;
        lightInfo.m_vDir.setValue(rayLight.m_vDir).reverse();
        lightInfo.m_rgb.setValue(m_rgbDimmed).
                scale((float) (Math.pow((double) (lightInfo.m_vDir.dot(m_directiuon)), m_dExp)));
        lightInfo.m_ptFrom.setValue(m_location);
        lightInfo.m_fDist = intersection.m_ptLocation.getDistanceTo(m_location);
        return true;
    }
}
