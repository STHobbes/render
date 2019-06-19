/*
 * DynXmlObjLoader.java
 *
 * Created on October 3, 2002, 12:57 AM
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

import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is the loader for dynamically loaded objects from XML files.  The general form of the object
 * specification in the XML file is:<br><br>
 * <pre>
 *     &lt;DynamicallyLoadedObject class="<i>className</i>" name="<i>objectName</i>"gt;
 *           <font style="color:gray"><b>.</b>
 *         <i>object specific node content</i>
 *           <b>.</b></font>
 *     &lt;/DynamicallyLoadedObject&gt;
 * </pre>
 *
 * @author royster.hall@gmail.com
 * @version 1.0
 * @see cip.render.IDynXmlObject
 * @since 1.0
 */
public final class DynXmlObjLoader {
    private static final Logger logger = Logger.getLogger(DynXmlObjLoader.class.getName());
    private static boolean loggingFiner = logger.isLoggable(Level.FINER);

    public static final String DEFAULT_NAME = "<unspecified>";
    /**
     * The XML tag name for a dynamically loaded object in an XML file.
     */
    public static final String XML_TAG = "DynamicallyLoadedObject";
    public static final String XML_TAG_LOWER = XML_TAG.toLowerCase();
    public static final String XML_ATTR_CLASS = "class";
    public static final String XML_ATTR_NAME = "name";

    private DynXmlObjLoader() {
        // This is a utility class and cannot be instantiated
    }

    /**
     * Load a dynamically loaded object from an XML description.  The XML description includes a class
     * that will be instantiated.  Once an object of that class is instantiated, it will be tested to
     * see if it implements the {@link cip.render.IDynXmlObject} interface.  If so, it is loaded
     * from the XML node.
     *
     * @param xmlElement    The XML node containing the description of the object.
     * @param refObjectList A linked list of previously loaded named objects.  The loaded object is added to this
     *                      list if the object is loaded successfully.  This list is passed to the object being loaded so
     *                      that object references can be resolved.
     * @return Returns the instantiated and initialized object.
     * @throws DynXmlObjParseException Thrown if there was an error instantiating and/or loading the object from the node.
     */
    public static Object LoadObject(final @NotNull Element xmlElement,
                                    final @Nullable LinkedList refObjectList) throws DynXmlObjParseException {
        // check that this is really a model
        if (!xmlElement.getTagName().toLowerCase().equals(XML_TAG_LOWER)) {
            throw new DynXmlObjParseException(String.format("This is not a <%s> element", XML_TAG));
        }
        // parse the name and class attributes for the light
        final String strClass = xmlElement.getAttribute(XML_ATTR_CLASS);
        if (strClass.equals("")) {
            throw new DynXmlObjParseException("no class specified");
        }
        String strName = xmlElement.getAttribute(XML_ATTR_NAME);
        if (strName.equals("")) {
            strName = DEFAULT_NAME;
        }
        // instantiate the object class
        if (loggingFiner) {
            logger.finer(String.format("Loading: class='%s', name=%s", strClass, strName));
        }
        final Object obj;
        try {
            obj = Class.forName(strClass).newInstance();
        } catch (final Exception t) {
            throw new DynXmlObjParseException(
                    String.format("Error Loading: class='%s', name=%s, wraps:\n   %s", strClass, strName, t.getMessage()), t);
        }
        // read the object from the XML node
        if (obj instanceof IDynXmlObject) {
            try {
                ((IDynXmlObject) obj).loadFromXml(xmlElement, refObjectList);
                if (obj instanceof INamedObject) {
                    ((INamedObject) obj).setName(strName);
                    if (null != refObjectList) {
                        refObjectList.add(obj);
                    }
                }
                return obj;
            } catch (final Exception t) {
                throw new DynXmlObjParseException(
                        String.format("Error Loading: class='%s', name=%s, wraps:\n   %s", strClass, strName, t.getMessage()), t);
            }
        } else {
            throw new DynXmlObjParseException(String.format("%s does not implement IDynXmlObject", strClass));
        }
    }
}
