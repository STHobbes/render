/*
 * ADynamicNamedObject.java
 *
 * Created on January 9, 2017, 6:16 PM
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
package cip.render;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.LinkedList;

/**
 * @author royster.hall@gmail.com
 * @version 1.0
 * @since 1.0
 */
public abstract class ADynamicNamedObject implements IDynXmlObject, INamedObject {
    protected String m_strName = DynXmlObjLoader.DEFAULT_NAME;      // this material name

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IDynXmlObject interface implementation                                                                                //
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void loadFromXml(final @NotNull Element xmlElement,
                            final @Nullable LinkedList refObjectList) throws DynXmlObjParseException {
        try {
            Node domNode = xmlElement.getFirstChild();
            while (null != domNode) {
                if (domNode instanceof Element) {
                    final Element element = (Element) domNode;
                    final String elementTag = element.getTagName().toLowerCase();
                    if (!lclProcessXmlElement(elementTag, element, refObjectList)) {
                        throw new DynXmlObjParseException(
                                String.format("Unrecognized sphere XML description element <%s>.", element.getTagName()));
                    }
                }
                domNode = domNode.getNextSibling();
            }
            lclValidate();

        } catch (final Exception t) {
            throw new DynXmlObjParseException(
                    String.format("Error Loading: class='%s', wraps:\n   %s", getClass().getName(), t.getMessage()), t);
        }
    }

    protected boolean lclProcessXmlElement(final @NotNull String elementTag, final @NotNull Element xmlElement,
                                           final @Nullable LinkedList refObjectList) throws DynXmlObjParseException {
        return false;
    }

    /**
     * Called once the object XML has been parsed to validate and/or trigger post load initialization. Override this method if
     * there are required parameters that do not defult, or if additional initialization is required after object parsing.
     *
     * @throws DynXmlObjParseException thrown if there was a problem in the XML description and it could not be parsed.
     */
    protected void lclValidate() throws DynXmlObjParseException {

    }


    @Override
    public void toChildXmlElement(final @NotNull Element parentEl) {
        // This is the dynamically loaded object boilerplate
        final Element element = parentEl.getOwnerDocument().createElement(DynXmlObjLoader.XML_TAG);
        parentEl.appendChild(element);
        element.setAttribute(DynXmlObjLoader.XML_ATTR_CLASS, this.getClass().getName());
        element.setAttribute(DynXmlObjLoader.XML_ATTR_NAME, m_strName);
        // this is the class-specific stuff
        lclAppendChildElements(element);
    }

    protected void lclAppendChildElements(final @NotNull Element element) {

    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // INamedObject interface implementation                                                                                 //
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public @NotNull String getName() {
        return m_strName;
    }

    @Override
    public void setName(final @NotNull String strName) {
        m_strName = strName;
    }

}
