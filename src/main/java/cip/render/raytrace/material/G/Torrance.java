/*
 * Torrance.java
 *
 * Created on October 25, 2002, 8:54 PM
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
package cip.render.raytrace.material.G;

import cip.render.DynXmlObjLoader;
import cip.render.DynXmlObjParseException;
import cip.render.IDynXmlObject;
import cip.render.raytrace.interfaces.IRtG;
import cip.render.util.AngleF;
import cip.render.util3d.Vector3f;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

import java.util.LinkedList;

/**
 * The geometric attenuation function introduced by Torrance and Sparrow as
 * G = min<b>(</b>1, 2(<b>N.H</b>)(<b>N.V</b>)/(<b>L.H</b>),
 * 2(<b>N.H</b>)(<b>N.L</b>)/(<b>V.H</b>)<b>)</b>.  The original reference is
 * Torrance, K. E. and E. M. Sparrow (1967), "Theory for Off-Specular Reflection from
 * Roughened Surfaces", Journal of the Optical Society of America, vol. 57, no. 9, pp.1105-1114.
 * <p>
 * The Torrance-Sparrow geometric attenuation function is specified as a node in an XML file as:<br><br>
 * <tt>
 * &nbsp;&nbsp;&nbsp; <font style="color:blue">&lt;<b>DynamicallyLoadedObject</b>
 * class="cip.raytrace.material.G.Torrance"/&gt;</font><br>
 * </tt>
 *
 * @author royster.hall@gmail.com
 * @version 1.0
 * @since 1.0
 */
public class Torrance implements IDynXmlObject, IRtG {

    /**
     * Creates a new instance of <tt>Torrance</tt> and Sparrow geometric attenuation function.
     */
    public Torrance() {
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IDynXmlObject interface implementation                                                                                //
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void loadFromXml(final @NotNull Element xmlElement, final @Nullable LinkedList refObjectList) throws DynXmlObjParseException {
        // there is actually nothing to load, so this function does nothing
    }

    public void toChildXmlElement(final @NotNull Element parentEl) {
        // the node for the Torrance and Sparrow geometric attenuation function
        final Element element = parentEl.getOwnerDocument().createElement(DynXmlObjLoader.XML_TAG);
        parentEl.appendChild(element);
        element.setAttribute(DynXmlObjLoader.XML_ATTR_CLASS, this.getClass().getName());
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IRtG interface implementation                                                                                         //
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void initialize(final AngleF aBeta) {
        // the torrance-sparrow model derives self shadowing independent of surface roughness.
    }

    public float evaluate(final Vector3f N, final Vector3f H, final Vector3f V, final Vector3f L) {
        float fG = 1.0f;
        final float fNdotH = N.dot(H);
        final float fVLdotH = H.dot(V); // note V.H = L.H because H is the vector bisector
        final float fNdotV = N.dot(V);
        final float fNdotL = N.dot(L);
        float fTmp;

        if ((fTmp = (2.0f * fNdotH * fNdotV) / fVLdotH) < fG) {
            fG = fTmp;
        }
        if ((fTmp = (2.0f * fNdotH * fNdotL) / fVLdotH) < fG) {
            fG = fTmp;
        }
        return fG;
    }
}
