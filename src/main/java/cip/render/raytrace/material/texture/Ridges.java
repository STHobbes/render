/*
 * Ridges.java
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

import cip.render.DynXmlObjLoader;
import cip.render.DynXmlObjParseException;
import cip.render.IDynXmlObject;
import cip.render.INamedObject;
import cip.render.raytrace.RayIntersection;
import cip.render.raytrace.interfaces.IRtBackground;
import cip.render.raytrace.interfaces.IRtGeometry;
import cip.render.raytrace.interfaces.IRtLight;
import cip.render.raytrace.interfaces.IRtMaterial;
import cip.render.util2d.Point2f;
import cip.render.util3d.Point3f;
import cip.render.utilColour.RGBf;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.LinkedList;

/**
 * @author royster.hall@gmail.com
 * @version 1.0
 * @since 1.0
 */
public class Ridges implements IDynXmlObject, INamedObject, IRtMaterial {

    protected static final String XML_TAG_REF_NAME_ATTR = "name";
    protected static final String XML_TAG_MATERIAL_REF = "MaterialByRef";

    // The instance definition
    protected String m_strName = cip.render.raytrace.material.PackageConstants.DEFAULT_NAME;      // this material name
    protected IRtMaterial m_mtl = null;


    /**
     * Creates a new instance of <tt>Checkerboard</tt>
     */
    public Ridges() {
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IDynXmlObject interface implementation                                                                                //
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void loadFromXml(final @NotNull Element xmlElement, final LinkedList<INamedObject> refObjectList) throws DynXmlObjParseException {
        try {
            Node domNode = xmlElement.getFirstChild();
            while (null != domNode) {
                if (domNode instanceof Element) {
                    final Element element = (Element) domNode;
                    if (element.getTagName().equalsIgnoreCase(DynXmlObjLoader.XML_TAG)) {
                        // Should be a material - that is the only dynamically loaded object that can be used
                        final Object obj = DynXmlObjLoader.LoadObject(element, refObjectList);
                        if (obj instanceof IRtMaterial) {
                            m_mtl = (IRtMaterial) obj;
                        } else {
                            throw new DynXmlObjParseException("PositionTexture " + m_strName + " material could not be parsed");
                        }
                    } else if (element.getTagName().equalsIgnoreCase(XML_TAG_MATERIAL_REF)) {
                        // a material reference
                        final String strName = element.getAttribute(XML_TAG_REF_NAME_ATTR);
                        m_mtl = resolveMaterialRef(strName, refObjectList);
                    } else {
                        throw new DynXmlObjParseException("Unrecognized PositionTexture XML description element <" +
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

        // this is the MapNatural specific stuff
        if ((null != m_mtl) && (m_mtl instanceof IDynXmlObject)) {
            ((IDynXmlObject) m_mtl).toChildXmlElement(element);
        }
    }

    protected IRtMaterial resolveMaterialRef(final String strName, final LinkedList refObjectList) throws DynXmlObjParseException {
        if (!strName.equals("") && (null != refObjectList)) {
            for (final Object obj : refObjectList) {
                if ((obj instanceof IRtMaterial) && ((INamedObject) obj).getName().equals(strName)) {
                    return (IRtMaterial) obj;
                }
            }
        }
        throw new DynXmlObjParseException("Referenced material \"" + strName + "\" was not found.");
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // INamedObject interface implementation                                                                                 //
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public @NotNull String getName() {
        return m_strName;
    }

    public void setName(final @NotNull String strName) {
        m_strName = strName;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IRtMaterial interface implementation                                                                                  //
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * IGNORED, not applicable to <tt>Checkerboard</tt>.
     */
    public void initSampling(final int nSample, final @NotNull float[] f1dSample, final float[] f1dRandom, final @NotNull Point2f[] pt2dSample, final Point2f[] pt2dRandom,
                             final @NotNull Point3f[] pt3dSample, final Point3f[] pt3dRandom) {
    }

    public void getColor(final @NotNull RGBf rgb, final @NotNull RayIntersection intersection, final @NotNull IRtLight[] lights, final @NotNull IRtGeometry[] rtObjects, final @NotNull IRtBackground rtBkg, final int nMaxRecursions,
                         final int nSample, final int nRandom) {
        if (!intersection.m_bTexture) {
            throw new IllegalStateException("My Texture: texture coordinates have not been set");
        }
        // the checkerboard is really simple, we just take every 1 unit square and check
        //  which quadrant of the square we are in and assign a material
        intersection.m_ptTexture.x -= (float) Math.floor(intersection.m_ptTexture.x);
        intersection.m_ptTexture.y -= (float) Math.floor(intersection.m_ptTexture.y);
        intersection.m_ptTexture.z -= (float) Math.floor(intersection.m_ptTexture.z);
        intersection.m_mtl = m_mtl;

        try {

            intersection.m_vNormal.scale(((float) Math.sin((double) intersection.m_ptTexture.x)) / 2 + 0.5f);

        } catch (final Throwable T) {
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
