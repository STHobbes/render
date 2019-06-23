/*
 * ADualMaterialTexture.java
 *
 * Created on January 9, 2017
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

import cip.render.ADynamicNamedObject;
import cip.render.DynXmlObjLoader;
import cip.render.DynXmlObjParseException;
import cip.render.INamedObject;
import cip.render.raytrace.interfaces.IRtMaterial;
import cip.render.util2d.Point2f;
import cip.render.util3d.Point3f;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.LinkedList;

/**
 * The abstract class for a texture.
 *
 * @author royster.hall@gmail.com
 * @version 1.0
 * @since 1.0
 */
public abstract class ATexture extends ADynamicNamedObject implements IRtMaterial {
    protected static final String XML_TAG_REF_NAME_ATTR = "name";
    protected static final String XML_TAG_MATERIAL_REF = "MaterialByRef";
    protected static final String XML_TAG_MATERIAL_REF_LOWER = XML_TAG_MATERIAL_REF.toLowerCase();

    /**
     * Parse a material from the <tt>xmlElement</tt>, the material may either be locally parsed or referenced from the
     * object library.
     *
     * @param xmlElement ({@link Element}, not null) The element that is expected to contain a material.
     * @param refObjectList ({@link LinkedList}&lt;{@link INamedObject}&gt;, nullable) The list of library objects.
     * @return (IRtMaterial, nullable) EReturns the material parsed from this element, returns <tt>null</tt> if a material was
     * not parsed from this eelment.
     * @throws DynXmlObjParseException thrown if there was a problem in the XML description and it could not be parsed.
     */
    final protected IRtMaterial parseMaterial(final @NotNull Element xmlElement,
                                              final @Nullable LinkedList refObjectList) throws DynXmlObjParseException {
        try {
            Node domNode = xmlElement.getFirstChild();
            while (null != domNode) {
                if (domNode instanceof Element) {
                    final Element element = (Element) domNode;
                    final String elementTag = element.getTagName().toLowerCase();
                    if (elementTag.equals(DynXmlObjLoader.XML_TAG_LOWER)) {
                        // Should be a material - that is the only dynamically loaded object that can be used
                        final Object obj = DynXmlObjLoader.LoadObject(element, refObjectList);
                        if (obj instanceof IRtMaterial) {
                            return (IRtMaterial) obj;
                        } else {
                            throw new DynXmlObjParseException(String.format("%s *s  material could not be parsed",
                                    getClass().getSimpleName(), m_strName));
                        }
                    } else if (element.getTagName().equalsIgnoreCase(XML_TAG_MATERIAL_REF_LOWER)) {
                        // a material reference
                        final String strName = element.getAttribute(XML_TAG_REF_NAME_ATTR);
                        return resolveMaterialRef(strName, refObjectList);
                    } else {
                        throw new DynXmlObjParseException("Unrecognized " + getClass().getSimpleName() + " XML description element <" +
                                element.getTagName() + ">.");
                    }
                }
                domNode = domNode.getNextSibling();
            }
        } catch (final Throwable t) {
            if (t instanceof DynXmlObjParseException) {
                throw (DynXmlObjParseException) t;
            } else {
                throw new DynXmlObjParseException(getClass().getSimpleName() + " parse exception", t);
            }
        }
        return null;
    }

    static IRtMaterial resolveMaterialRef(final @NotNull String strName,
                                          final @Nullable LinkedList refObjectList) throws DynXmlObjParseException {
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
    // IRtMaterial interface implementation                                                                                  //
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void initSampling(final int nSample, final @NotNull float[] f1dSample, final float[] f1dRandom,
                             final @NotNull Point2f[] pt2dSample, final Point2f[] pt2dRandom,
                             final @NotNull Point3f[] pt3dSample, final Point3f[] pt3dRandom) {
        // Ignored: there is no additional initialization required for this texture
    }

}
