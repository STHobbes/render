/*
 * TrowRietz.java
 *
 * Created on October 23, 2002, 10:23 AM
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

import cip.render.DynXmlObjLoader;
import cip.render.DynXmlObjParseException;
import cip.render.IDynXmlObject;
import cip.render.raytrace.interfaces.IRtD;
import cip.render.util.AngleF;
import cip.render.util3d.Vector3f;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

import java.util.LinkedList;

/**
 * The slope-distribution function introduced by Trowbridge and Reitz as
 * D = <i>C</i><sup>2</sup> / ((<b>N.H</b>)<sup>2</sup>(<i>C</i><sup>2</sup> - 1) + 1)<sup>2</sup>.  The original
 * reference is Trowbridge, T. S. and K. P. Rietz (1967), "Average Irregularity Representation of a Roughened
 * Surface for Ray Reflection", Journal of the Optical Society of America, vol.65, no.5.
 * <p>
 * The Trowbridge and Rietz slope distribution function is specified as a node in an XML file as:<br><br>
 * <tt>
 * &nbsp;&nbsp;&nbsp; <font style="color:blue">&lt;<b>DynamicallyLoadedObject</b>
 * class="cip.raytrace.material.D.TrowRietz"/&gt;</font><br>
 * </tt>
 *
 * @author royster.hall@gmail.com
 * @version 1.0
 * @since 1.0
 */
public class TrowRietz implements IDynXmlObject, IRtD {
    private double m_dC_2 = 1.0f;  // the is c squared

    /**
     * Creates a new instance of <tt>TrowRietz</tt>
     */
    public TrowRietz() {
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
        final double dCos = Math.cos((double) aBeta.getRadians());
        m_dC_2 = ((dCos * dCos) - 1.0) / ((dCos * dCos) - Math.sqrt(2.0));
    }

    public float evaluate(final Vector3f N, final Vector3f H, final Vector3f V, final Vector3f L) {
        final double dNdotH = (double) N.dot(H);
        final double dTmp = m_dC_2 / (((dNdotH * dNdotH) * (m_dC_2 - 1.0)) + 1.0);
        return (float) (dTmp * dTmp);
    }
}
