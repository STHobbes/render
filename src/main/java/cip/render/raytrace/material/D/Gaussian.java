/*
 * Gaussian.java
 *
 * Created on October 23, 2002, 10:21 AM
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
import cip.render.DynXmlObjLoader;
import cip.render.raytrace.interfaces.IRtD;
import cip.render.DynXmlObjParseException;
import cip.render.util.AngleF;
import cip.render.util3d.Vector3f;

import java.util.LinkedList;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

/**
 * The gaussian slope-distribution function ias used by Torrance and Sparrow.  This function is
 * D = (exp(-(<i>C</i>acos(<b>N.H</b>))<sup>2</sup>).  The original reference is
 * Torrance, K. E. and E. M. Sparrow, "Theory for Off-Specular Reflection from Roughened Surfaces",
 * Journal of the Optical Society of America, vol. 57, no.9, pp.1105-1114.
 * <p>
 * The Gaussian slope distribution function is specified as a node in an XML file as:<br><br>
 * <tt>
 * &nbsp;&nbsp;&nbsp; <font style="color:blue">&lt;<b>DynamicallyLoadedObject</b>
 * class="cip.raytrace.material.D.Gaussion"/&gt;</font><br>
 * </tt>
 *
 * @author royster.hall@gmail.com
 * @version 1.0
 * @since 1.0
 */
public class Gaussian implements IDynXmlObject, IRtD {
    private double m_dC = 1.0f;

    /**
     * Creates a new instance of <tt>Gaussian</tt>
     */
    public Gaussian() {
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IDynXmlObject interface implementation                                                                                //
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void loadFromXml(final @NotNull Element xmlElement, final @Nullable LinkedList refObjectList) throws DynXmlObjParseException {
        // there is actually nothing to load, so this function does nothing
    }

    public void toChildXmlElement(final @NotNull Element parentEl) {
        // the node for the Gaussian mslope distribution
        final Element element = parentEl.getOwnerDocument().createElement(DynXmlObjLoader.XML_TAG);
        parentEl.appendChild(element);
        element.setAttribute(DynXmlObjLoader.XML_ATTR_CLASS, this.getClass().getName());
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IRtD interface implementation                                                                                         //
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void initialize(final AngleF aBeta) {
        m_dC = Math.sqrt(Math.log(2.0)) / (double) aBeta.getRadians();
    }

    public float evaluate(final Vector3f N, final Vector3f H, final Vector3f V, final Vector3f L) {
        final double dTmp = Math.acos((double) N.dot(H)) * m_dC;
        return (float) Math.exp(-(dTmp * dTmp));
    }
}
