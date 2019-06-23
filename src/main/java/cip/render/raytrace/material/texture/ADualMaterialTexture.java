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

import cip.render.DynXmlObjParseException;
import cip.render.IDynXmlObject;
import cip.render.INamedObject;
import cip.render.raytrace.interfaces.IRtMaterial;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Element;

import java.util.LinkedList;

/**
 * The abstract base class for a texture that combines 2 materials. This class implements the methods for the 2 materials parse
 * from and write to the XML scene description file. By default the materials are named <tt><"material1"</tt> and
 * <tt>"material2"</tt>. Functions are provided that let your texture use material names that are more relevant to your texture.
 *
 * @author royster.hall@gmail.com
 * @version 1.0
 * @since 1.0
 */
@SuppressWarnings("WeakerAccess")
public abstract class ADualMaterialTexture extends ATexture {
    private static final String DEFAULT_MATERIAL1_NAME = "material1";
    private static final String DEFAULT_MATERIAL2_NAME = "material2";
    protected IRtMaterial m_mtl1 = null;
    protected String m_mtl1_name = DEFAULT_MATERIAL1_NAME;
    protected IRtMaterial m_mtl2 = null;
    protected String m_mtl2_name = DEFAULT_MATERIAL2_NAME;

    ADualMaterialTexture() {
    }

    /**
     * The constructor for specifying alternate names names for {@link #m_mtl1} and {@link #m_mtl2}
     *
     * @param mtl1Name (String, not null) The name for the material to be assigned to the {@link #m_mtl1} member variable.
     * @param mtl2Name (String, not null) The name for the material to be assigned to the {@link #m_mtl2} member variable.
     */
    ADualMaterialTexture(final @NotNull String mtl1Name,
                         final @NotNull String mtl2Name) {
        m_mtl1_name = mtl1Name;
        m_mtl2_name = mtl2Name;
    }

    /**
     * Attempt to parse an element for a material using the common <tt>"material1"</tt> and <tt>"material2"</tt> materials
     * names set by default; or, the names you set with the constructor that sets the names of the materials. Override this
     * method to include parsing for texture specification parameters in addition to the 2 materials. Remember that your
     * override should call this method for material parsing.
     *
     * @param elementTag    (String, not null) The XML element tag.
     * @param xmlElement    (Element, not null) The XML element.
     * @param refObjectList ({@link LinkedList}&lt;{@link INamedObject}&gt;, nullable) The list of library objects.
     * @return Returns <tt>true</tt> if this node was parsed by the object, <tt>false</tt> otherwise.
     * @throws DynXmlObjParseException Thrown if this was a material element, but the material could not be parsed.
     */
    @Override
    protected boolean lclProcessXmlElement(final @NotNull String elementTag, final @NotNull Element xmlElement,
                                           final LinkedList<INamedObject> refObjectList) throws DynXmlObjParseException {
        if (elementTag.equals(m_mtl1_name)) {
            m_mtl1 = parseMaterial(xmlElement, refObjectList);
            return true;
        } else if (elementTag.equals(m_mtl2_name)) {
            m_mtl2 = parseMaterial(xmlElement, refObjectList);
            return true;
        }
        return false;
    }

    protected void lclAppendChildElements(final @NotNull Element element) {
        if ((null != m_mtl1) && (m_mtl1 instanceof IDynXmlObject)) {
            final Element elMtl1 = element.getOwnerDocument().createElement(m_mtl1_name);
            ((IDynXmlObject) m_mtl1).toChildXmlElement(elMtl1);
            element.appendChild(elMtl1);
        }
        if ((null != m_mtl2) && (m_mtl2 instanceof IDynXmlObject)) {
            final Element elMtl2 = element.getOwnerDocument().createElement(m_mtl2_name);
            ((IDynXmlObject) m_mtl2).toChildXmlElement(elMtl2);
            element.appendChild(elMtl2);
        }
    }
}
