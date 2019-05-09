/*
 * DynXmlObjParseException.java
 *
 * Created on October 3, 2002, 12:45 AM
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

/**
 * The exception thrown by a dynamically loaded object described in an XML file.  When this exception is thrown,
 * it means the object was not successfully loaded from the XML description.  While a specific object may
 * not be successfully loaded, it does not mean that the rest of the file will not load correctly.  The
 * application handling this exception must carefully conside the implications of an exception while an
 * object is loading from an XML file.
 *
 * @author royster.hall@gmail.com
 * @version 1.0
 * @since 1.0
 */
public class DynXmlObjParseException extends Exception {
    /**
     * Creates a new instance of <tt>DynXmlObjParseException</tt>.
     */
    public DynXmlObjParseException() {
    }

    /**
     * Creates a new instance of <tt>DynXmlObjParseException</tt>.
     */
    public DynXmlObjParseException(final String strDetail) {
        super(strDetail);
    }

    /**
     * Creates a new instance of <tt>DynXmlObjParseException</tt>.
     */
    public DynXmlObjParseException(final String strDetail, final Throwable t) {
        super(strDetail, t);
    }

}
