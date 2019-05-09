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

import cip.render.IDynXmlObject;
import cip.render.raytrace.interfaces.IRtMaterial;
import cip.render.DynXmlObjParseException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

import java.util.LinkedList;

/**
 * The abstract class for a texture that combines 2 materials.
 *
 * @author royster.hall@gmail.com
 * @version 1.0
 * @since 1.0
 */
@SuppressWarnings("WeakerAccess")
public abstract class ADualMaterialTexture extends ATexture {
    protected IRtMaterial m_mtl1 = null;
    protected IRtMaterial m_mtl2 = null;

    protected boolean lclProcessXmlElement(final @NotNull String elementTag, final @NotNull Element xmlElement,
                                           final @Nullable LinkedList refObjectList) throws DynXmlObjParseException {
        return lclParseMaterials(elementTag, xmlElement, refObjectList, "material1", "material2");
    }

    protected final boolean lclParseMaterials(final @NotNull String elementTag, final @NotNull Element xmlElement,
                                              final @Nullable LinkedList refObjectList, final @NotNull String mtl1Name,
                                              final @NotNull String mtl2Name) throws DynXmlObjParseException {
        if (elementTag.equals(mtl1Name)) {
            m_mtl1 = parseMaterial(xmlElement, refObjectList);
            return true;
        } else if (elementTag.equals(mtl2Name)) {
            m_mtl2 = parseMaterial(xmlElement, refObjectList);
            return true;
        }
        return false;
    }

    protected void lclAppendChildElements(final @NotNull Element element) {
        lclAppendMaterials(element, "material1", "material2" );
    }

    protected final void lclAppendMaterials(final @NotNull Element element, final @NotNull String mtl1Name,
                                            final @NotNull String mtl2Name) {

        if ((null != m_mtl1) && (m_mtl1 instanceof IDynXmlObject)) {
            final Element elMtl1 = element.getOwnerDocument().createElement(mtl1Name);
            ((IDynXmlObject) m_mtl1).toChildXmlElement(elMtl1);
            element.appendChild(elMtl1);
        }
        if ((null != m_mtl2) && (m_mtl2 instanceof IDynXmlObject)) {
            final Element elMtl2 = element.getOwnerDocument().createElement(mtl2Name);
            ((IDynXmlObject) m_mtl2).toChildXmlElement(elMtl2);
            element.appendChild(elMtl2);
        }
    }
}
