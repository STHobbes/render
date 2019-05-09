/*
 * ALight.java
 *
 * Created on October 10, 2002, 11:43 PM
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
package cip.render.raytrace.light;

import cip.render.IDynXmlObject;
import cip.render.INamedObject;
import cip.render.raytrace.interfaces.IRtLight;
import cip.render.raytrace.RayIntersection;
import cip.render.raytrace.LightInfo;
import cip.render.DynXmlObjLoader;
import cip.render.DynXmlObjParseException;
import cip.render.util2d.Point2f;
import cip.render.util3d.Point3f;

import java.util.LinkedList;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

/**
 * @author roy
 */
public class ALight implements IDynXmlObject, INamedObject, IRtLight {
    protected static final String XML_TAG_REF_NAME_ATTR = "name";
    protected static final String XML_TAG_LIGHT_REF = "LightByRef";

    protected static final String DEFAULT_NAME = "<unspecified>";

    // Turns on/off debug output to System.out.  Debug output is limited to object load and/or instantiation.  There is NEVER
    //  any printed output for operations that would occur inside the rendering loop.
    //
    protected static final boolean DEBUG = true;
    // The instance definition
    protected String m_strName = DEFAULT_NAME;  // the geometry name

    /**
     * Creates a new instance of <tt>ALight</tt>.
     */
    public ALight() {
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IDynXmlObject interface implementation                                                                                //
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void loadFromXml(final @NotNull Element xmlElement, final @Nullable LinkedList refObjectList) throws DynXmlObjParseException {
    }

    @Override
    public void toChildXmlElement(final @NotNull Element parentEl) {
        // This is the dynamically loaded object boilerplate
        final Element element = parentEl.getOwnerDocument().createElement(DynXmlObjLoader.XML_TAG);
        parentEl.appendChild(element);
        element.setAttribute(DynXmlObjLoader.XML_ATTR_CLASS, this.getClass().getName());
        element.setAttribute(DynXmlObjLoader.XML_ATTR_NAME, m_strName);
        // this is the light specific stuff
        internalToXml(element);
    }

    // override this for the light specific node information
    protected void internalToXml(final Element element) {
    }

    protected IRtLight resolveLightRef(final String strName, final LinkedList refObjectList) throws DynXmlObjParseException {
        if (!strName.equals("") && (null != refObjectList)) {
            for (final Object obj : refObjectList) {
                if ((obj instanceof IRtLight) && ((INamedObject) obj).getName().equals(strName)) {
                    return (IRtLight) obj;
                }
            }
        }
        throw new DynXmlObjParseException("Referenced light \"" + strName + "\" was not found.");
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

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IRtLight interface implementation                                                                                     //
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void initSampling(final int nSample, final float[] f1dSample, final float[] f1dRandom, final Point2f[] pt2dSample, final Point2f[] pt2dRandom,
                             final Point3f[] pt3dSample, final Point3f[] pt3dRandom) {
    }

    //-------------------------------------------------------------------------------------------------------------------------
    @Override
    public void setDimmer(final float fDimmer) {
    }

    //-------------------------------------------------------------------------------------------------------------------------
    @Override
    public boolean getLight(final LightInfo lightInfo, final RayIntersection intersection, final int nSample, final int nRandom) {
        return false;
    }

}
