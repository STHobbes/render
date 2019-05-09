/*
 * Blinn.java
 *
 * Created on October 21, 2002, 10:11 AM
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
package cip.render.raytrace.material.D;

import cip.render.IDynXmlObject;
import cip.render.raytrace.interfaces.IRtD;
import cip.render.DynXmlObjLoader;
import cip.render.DynXmlObjParseException;
import cip.render.util.AngleF;
import cip.render.util3d.Vector3f;

import java.util.LinkedList;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

/**
 * The cosine power slope-distribution function introduced by Blinn as
 * D = (<b>N.H</b>)<sup>Ns</sup>.  The original reference is
 * Blinn, James F (1977), "Models of Light Reflection for Computer Synthesized Images", ACM Computer Graphics,
 * (SIGGRAPH 77), vol. 11, no. 2, pp.192-198.
 * <p>
 * The Blinn slope distribution function is specified as a node in an XML file as:<br><br>
 * <tt>
 * &nbsp;&nbsp;&nbsp; <font style="color:blue">&lt;<b>DynamicallyLoadedObject</b>
 * class="cip.raytrace.material.D.Blinn"/&gt;</font><br>
 * </tt>
 *
 * @author royster.hall@gmail.com
 * @version 1.0
 * @since 1.0
 */
public class Blinn implements IDynXmlObject, IRtD {
    private double m_dNs = 10.0f;

    /**
     * Creates a new instance of <tt>Blinn</tt> cosine power slope distribution function.
     */
    public Blinn() {
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IDynXmlObject interface implementation                                                                                //
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void loadFromXml(final @NotNull Element xmlElement, final @Nullable LinkedList refObjectList) throws DynXmlObjParseException {
        // there is actually nothing to load, so this function does nothing
    }

    public void toChildXmlElement(final @NotNull Element parentEl) {
        // the node for the Blinn slope distribution
        final Element element = parentEl.getOwnerDocument().createElement(DynXmlObjLoader.XML_TAG);
        parentEl.appendChild(element);
        element.setAttribute(DynXmlObjLoader.XML_ATTR_CLASS, this.getClass().getName());
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IRtD interface implementation                                                                                         //
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void initialize(final AngleF aBeta) {
        m_dNs = -(Math.log(2.0) / Math.log((double) aBeta.cos()));
    }

    public float evaluate(final Vector3f N, final Vector3f H, final Vector3f V, final Vector3f L) {
        return (float) Math.pow((double) N.dot(H), m_dNs);
    }


}
