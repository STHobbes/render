/*
 * Wavy.java
 *
 */
package cip.render.raytrace.material.texture;

import cip.render.DynXmlObjLoader;
import cip.render.DynXmlObjParseException;
import cip.render.IDynXmlObject;
import cip.render.raytrace.RayIntersection;
import cip.render.raytrace.interfaces.IRtBackground;
import cip.render.raytrace.interfaces.IRtGeometry;
import cip.render.raytrace.interfaces.IRtLight;
import cip.render.raytrace.interfaces.IRtMaterial;
import cip.render.util2d.Point2f;
import cip.render.util3d.Point3f;
import cip.render.utilColour.RGBf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.LinkedList;

//
// Steve's wavy functional texture
//
public class Wavy extends ADualMaterialTexture {
    protected static final String XML_TAG_MATERIAL1 = "material1";
    protected static final String XML_TAG_MATERIAL2 = "material2";
    protected static final String XML_TAG_REF_NAME_ATTR = "name";
    protected static final String XML_TAG_MATERIAL_REF = "MaterialByRef";

    // The instance definition
    protected String m_strName = cip.render.raytrace.material.PackageConstants.DEFAULT_NAME;      // this material name
//    protected IRtMaterial m_mtl1 = null;
//    protected IRtMaterial m_mtl2 = null;


    /**
     * Creates a new instance of <tt>Wavy</tt>
     */
    public Wavy() {
    }

    //////////////////////////////////////////////////////////////////////////////////
    // IDynXmlObject interface implementation                          
    //////////////////////////////////////////////////////////////////////////////////
    public void loadFromXml(final @NotNull Element xmlElement, final @Nullable LinkedList refObjectList) throws DynXmlObjParseException {
        try {
            Node domNode = xmlElement.getFirstChild();
            while (null != domNode) {
                if (domNode instanceof Element) {
                    final Element element = (Element) domNode;
                    if (element.getTagName().equalsIgnoreCase(XML_TAG_MATERIAL1)) {
                        m_mtl1 = parseMaterial(element, refObjectList);
                    } else if (element.getTagName().equalsIgnoreCase(XML_TAG_MATERIAL2)) {
                        m_mtl2 = parseMaterial(element, refObjectList);
                    } else {
                        throw new DynXmlObjParseException("Unrecognized Wavy XML description element <" +
                                element.getTagName() + ">.");
                    }
                }
                domNode = domNode.getNextSibling();
            }
        } catch (final Throwable t) {
            if (t instanceof DynXmlObjParseException) {
                throw (DynXmlObjParseException) t;
            } else {
                throw new DynXmlObjParseException(getClass().getName() + " parse exception", t);
            }
        }
    }

    public void toChildXmlElement(final @NotNull Element parentEl) {
        // This is the dynamically loaded object boilerplate
        final Element element = parentEl.getOwnerDocument().createElement(DynXmlObjLoader.XML_TAG);
        parentEl.appendChild(element);
        element.setAttribute(DynXmlObjLoader.XML_ATTR_CLASS, this.getClass().getName());
        element.setAttribute(DynXmlObjLoader.XML_ATTR_NAME, m_strName);
        // this is the Wavy specific stuff
        if ((null != m_mtl1) && (m_mtl1 instanceof IDynXmlObject)) {
            final Element elMtl1 = parentEl.getOwnerDocument().createElement(XML_TAG_MATERIAL1);
            ((IDynXmlObject) m_mtl1).toChildXmlElement(elMtl1);
            element.appendChild(elMtl1);
        }
        if ((null != m_mtl2) && (m_mtl2 instanceof IDynXmlObject)) {
            final Element elMtl2 = parentEl.getOwnerDocument().createElement(XML_TAG_MATERIAL2);
            ((IDynXmlObject) m_mtl2).toChildXmlElement(elMtl2);
            element.appendChild(elMtl2);
        }
    }

    protected IRtMaterial parseMaterial(final Element xmlElement, final LinkedList refObjectList) throws DynXmlObjParseException {
        final IRtMaterial mtl = null;
        try {
            Node domNode = xmlElement.getFirstChild();
            while (null != domNode) {
                if (domNode instanceof Element) {
                    final Element element = (Element) domNode;
                    if (element.getTagName().equalsIgnoreCase(DynXmlObjLoader.XML_TAG)) {
                        // Should be a material - that is the only dynamically loaded object that can be used
                        final Object obj = DynXmlObjLoader.LoadObject(element, refObjectList);
                        if (obj instanceof IRtMaterial) {
                            return (IRtMaterial) obj;
                        } else {
                            throw new DynXmlObjParseException("Wavy " + m_strName + " material could not be parsed");
                        }
                    } else if (element.getTagName().equalsIgnoreCase(XML_TAG_MATERIAL_REF)) {
                        // a material reference
                        final String strName = element.getAttribute(XML_TAG_REF_NAME_ATTR);
                        return resolveMaterialRef(strName, refObjectList);
                    } else {
                        throw new DynXmlObjParseException("Unrecognized Wavy XML description element <" +
                                element.getTagName() + ">.");
                    }
                }
                domNode = domNode.getNextSibling();
            }
        } catch (final Throwable t) {
            if (t instanceof DynXmlObjParseException) {
                throw (DynXmlObjParseException) t;
            } else {
                throw new DynXmlObjParseException(getClass().getName() + " parse exception", t);
            }
        }
        return null;
    }

//    protected IRtMaterial resolveMaterialRef(final String strName, final LinkedList refObjectList) throws DynXmlObjParseException {
//        if (!strName.equals("") && (null != refObjectList)) {
//            for (final Object obj : refObjectList) {
//                if ((obj instanceof IRtMaterial) && ((INamedObject) obj).getName().equals(strName)) {
//                    return (IRtMaterial) obj;
//                }
//            }
//        }
//        throw new DynXmlObjParseException("Referenced material \"" + strName + "\" was not found.");
//    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // INamedObject interface implementation                                             //
    ///////////////////////////////////////////////////////////////////////////////////////
    public @NotNull String getName() {
        return m_strName;
    }

    public void setName(final @NotNull String strName) {
        m_strName = strName;
    }

    /////////////////////////////////////////////////////////////////////////////////////
    // IRtMaterial interface implementation                          
    /////////////////////////////////////////////////////////////////////////////////////

    /**
     * IGNORED, not applicable to <tt>Wavy</tt>.
     */
    public void initSampling(final int nSample, final @NotNull float[] f1dSample, final float[] f1dRandom, final @NotNull Point2f[] pt2dSample, final Point2f[] pt2dRandom,
                             final @NotNull Point3f[] pt3dSample, final Point3f[] pt3dRandom) {
    }


    public void getColor(final @NotNull RGBf rgb, final @NotNull RayIntersection intersection, final @NotNull IRtLight[] lights, final @NotNull IRtGeometry[] rtObjects, final @NotNull IRtBackground rtBkg, final int nMaxRecursions,
                         final int nSample, final int nRandom) {
        if (!intersection.m_bTexture) {
            throw new IllegalStateException("Wavy: texture coordinates have not been set");
        }

        final int xq = ((int) Math.abs(Math.floor(intersection.m_ptTexture.x)) % 2);
        final int yq = ((int) Math.abs(Math.floor(intersection.m_ptTexture.y)) % 2);
        intersection.m_ptTexture.x -= (float) Math.floor(intersection.m_ptTexture.x);
        intersection.m_ptTexture.y -= (float) Math.floor(intersection.m_ptTexture.y);
        intersection.m_mtl = m_mtl2;

        final double pi = 3.1415926;

        // curve in a single quadrant
        double curve = 0.5 * (1.0 + Math.cos(pi * intersection.m_ptTexture.x));
        if (curve < 0) curve = 0;
        if (curve > 1) curve = 1;

        if (
                ((xq == 0) && (yq == 0) && (intersection.m_ptTexture.y > (1.0 - curve))) ||
                        ((xq == 0) && (yq == 1) && (intersection.m_ptTexture.y < curve)) ||
                        ((xq == 1) && (yq == 0) && (intersection.m_ptTexture.y > curve)) ||
                        ((xq == 1) && (yq == 1) && (intersection.m_ptTexture.y < (1.0 - curve)))
        )
            intersection.m_mtl = m_mtl1;

        intersection.m_mtl.getColor(rgb, intersection, lights, rtObjects, rtBkg, nMaxRecursions, nSample, nRandom);
    }
}
