/*
 * Phong.java
 *
 * Created on October 21, 2002, 10:12 AM
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
 * The cosine power slope-distribution function introduced by
 * Phong as D = (<b>R.V</b>)<sup>Ns</sup>.  The original reference is
 * Phong, Bui Toung (1975), "Illumination for Computer Generated Pictures", ACM Computer Graphics,
 * (SIGGRAPH 77), vol. 11, no. 2, pp.192-198.
 * <p>
 * The Blinn slope distribution function is specified as a node in an XML file as:<br><br>
 * <tt>
 * &nbsp;&nbsp;&nbsp; <font style="color:blue">&lt;<b>DynamicallyLoadedObject</b>
 * class="cip.raytrace.material.D.Phong"/&gt;</font><br>
 * </tt>
 *
 * @author royster.hall@gmail.com
 * @version 1.0
 * @since 1.0
 */
public class Phong implements IDynXmlObject, IRtD {
    private double m_dNs = 10.0f;

    private Vector3f m_cacheVector;

    /**
     * Creates a new instance of Phong slope distribution.
     */
    public Phong() {
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IDynXmlObject interface implementation                                                                                //
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void loadFromXml(final @NotNull Element xmlElement, final @Nullable LinkedList refObjectList) throws DynXmlObjParseException {
        // there is actually nothing to load, so this function does nothing
    }

    public void toChildXmlElement(final @NotNull Element parentEl) {
        // the node for the Blinn mslope distribution
        final Element element = parentEl.getOwnerDocument().createElement(DynXmlObjLoader.XML_TAG);
        parentEl.appendChild(element);
        element.setAttribute(DynXmlObjLoader.XML_ATTR_CLASS, getClass().getName());
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IRtD interface implementation                                                                                         //
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void initialize(final AngleF aBeta) {
        final AngleF tmpAng = new AngleF(aBeta).mult(2.0f);
        if (tmpAng.getDegrees() > 60.0f) tmpAng.setDegrees(60.0f);
        m_dNs = -(Math.log(2.0) / Math.log((double) tmpAng.cos()));
    }

    public float evaluate(final Vector3f N, final Vector3f H, final Vector3f V, final Vector3f L) {
        final Vector3f R = borrowVector();
        try {
            R.setToReflection(N, L);
            final float fVdotR = V.dot(R);
            if (fVdotR > 0.0f) {
                return (float) Math.pow((double) fVdotR, m_dNs);
            } else {
                return 0.0f;
            }
        } catch (final Throwable t) {
            return 0.0f;
        } finally {
            returnVector(R);
        }
    }

    private synchronized Vector3f borrowVector() {
        final Vector3f v = m_cacheVector;
        if (null == v) {
            return new Vector3f();
        }
        m_cacheVector = v.m_next;
        return v;
    }

    /**
     * Returns a borrowed vector.  This function <b>is not</b> thread-safe.
     *
     * @param v The vector being returned.
     */
    public void returnVector(final Vector3f v) {
        v.m_next = m_cacheVector;
        m_cacheVector = v;
    }

}
