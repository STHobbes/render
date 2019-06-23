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
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.LinkedList;

/**
 * The abstract implementation of a dynamic and named object that can be read from or written to an XML file. Extend your
 * dynamic named object from this class and implement the <tt>lcl*</tt> methods fpr object-specific behaviour.
 *
 * @author royster.hall@gmail.com
 * @version 1.0
 * @since 1.0
 */
public abstract class ADynamicNamedObject implements IDynXmlObject, INamedObject {
    protected String m_strName = DynXmlObjLoader.DEFAULT_NAME;      // initialize the name to the default name.

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IDynXmlObject interface implementation                                                                                //
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void loadFromXml(final @NotNull Element xmlElement,
                            final LinkedList<INamedObject> refObjectList) throws DynXmlObjParseException {
        try {
            Node domNode = xmlElement.getFirstChild();
            while (null != domNode) {
                if (domNode instanceof Element) {
                    final Element element = (Element) domNode;
                    final String elementTag = element.getTagName();
                    if (!lclProcessXmlElement(elementTag, element, refObjectList)) {
                        throw new DynXmlObjParseException(
                                String.format("Unrecognized %s XML description element <%s>.", this.getClass().getSimpleName(),
                                        element.getTagName()));
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

    /**
     * Called for each XML element within the XML description of the object.
     *
     * @param elementTag    (String, not null) The XML element tag.
     * @param xmlElement    (Element, not null) The XML element.
     * @param refObjectList ({@link LinkedList}&lt;{@link INamedObject}&gt;, nullable) The list of library objects.
     * @return Returns <tt>true</tt> if this node was parsed by the object, <tt>false</tt> otherwise - which means there was
     * garbage in the element (from the point of view of the object being loaded), which will cause a
     * {@link DynXmlObjParseException} to be thrown.
     * @throws DynXmlObjParseException Thrown if there was an error parsing this element.
     */
    protected boolean lclProcessXmlElement(final @NotNull String elementTag, final @NotNull Element xmlElement,
                                           final LinkedList<INamedObject> refObjectList) throws DynXmlObjParseException {
        return false;
    }

    /**
     * Called once the object XML has been parsed to validate and/or trigger post load initialization. Override this method if
     * there are required parameters that do not default, or if additional initialization is required after object parsing.
     *
     * @throws DynXmlObjParseException thrown if there was a problem in the XML description and the loaded object is not valid.
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

    /**
     * Called by {@link #toChildXmlElement(Element)} after the XML element for instantiating the object has been created to fill
     * in the child initialization parameters for the object.
     * @param element ({@link Element}, not null) The element that instantiates the object. The object initialization parameters
     *                should be created as children of this element.
     */
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
