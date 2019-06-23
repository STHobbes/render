/*
 * IDynXmlObject.java
 *
 * Created on October 3, 2002, 12:35 AM
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

import java.util.LinkedList;

/**
 * This is the interface to a dynamically loaded object described in an XML node.  The general idea
 * here is that the node identifies the classname, the object is instantiated by a factory, and this
 * interface provides the mechanism for the instantiated object to read itself.  The object is also
 * responsible for being able to write itself to an XML node.
 * <p>
 * In practice, an application using these objects would instantiate and read the object and then
 * query what interfaces the object supports (what capabilities the object possesses).
 *
 * @author royster.hall@gmail.com
 * @version 1.0
 * @see cip.render.DynXmlObjLoader
 * @since 1.0
 */
public interface IDynXmlObject {
    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Loads this object from an XML DOM Document element.  The {@link cip.render.DynXmlObjLoader} has
     * parsed the element and attributes to determine that this is a dynamically loaded object, found
     * and instantiated the class, and is now sending the node to the object so it can initialize
     * itself based on the contents of the node.
     *
     * @param xmlElement    The dynamically loaded object DOM Document element.
     * @param refObjectList A linked list if named objects (implementing the {@link INamedObject} interface)
     *                      that have already been loaded and can be used to resolve object references.
     * @throws DynXmlObjParseException Thrown if there was an error parsing the object.  If this exception is
     *                                 thrown it should be assumed that this object was incorrectly initialized,
     *                                 is invalid, and should not be used in any further operations.
     */
    void loadFromXml(@NotNull Element xmlElement, LinkedList<INamedObject> refObjectList) throws DynXmlObjParseException;

    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Add this object as a <tt>&lt;DynamicallyLoadedObject&gt;</tt> DOM element that is a child of some parent element.
     *
     * @param parentEl The parent element.
     */
    void toChildXmlElement(@NotNull Element parentEl);
}
