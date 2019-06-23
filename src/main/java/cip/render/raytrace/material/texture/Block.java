/*
 * Block.java
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

package cip.render.raytrace.material.texture;

import cip.render.DynXmlObjLoader;
import cip.render.DynXmlObjParseException;
import cip.render.IDynXmlObject;
import cip.render.INamedObject;
import cip.render.raytrace.RayIntersection;
import cip.render.raytrace.interfaces.IRtBackground;
import cip.render.raytrace.interfaces.IRtGeometry;
import cip.render.raytrace.interfaces.IRtLight;
import cip.render.raytrace.interfaces.IRtMaterial;
import cip.render.util2d.Point2f;
import cip.render.util3d.Point3f;
import cip.render.utilColour.RGBf;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.LinkedList;


public class Block implements IDynXmlObject, INamedObject, IRtMaterial {
    protected static final String XML_TAG_MATERIAL1 = "material1";
    protected static final String XML_TAG_MATERIAL2 = "material2";
    protected static final String XML_TAG_REF_NAME_ATTR = "name";
    protected static final String XML_TAG_MATERIAL_REF = "MaterialByRef";

    // The instance definition
    protected String m_strName = cip.render.raytrace.material.PackageConstants.DEFAULT_NAME;      // this material name
    protected IRtMaterial m_mtl1 = null;
    protected IRtMaterial m_mtl2 = null;

    private final float[] rand_numbers;
    private final int rand_size = 256;

    /**
     * Creates a new instance of <tt>Block</tt>
     */
    public Block() {
        int i;

        rand_numbers = new float[rand_size];
        for (i = 0; i < rand_size; i++)
            rand_numbers[i] = (float) Math.random();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IDynXmlObject interface implementation                                                                                //
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void loadFromXml(final @NotNull Element xmlElement, final LinkedList<INamedObject> refObjectList) throws DynXmlObjParseException {
        try {
            Node domNode = xmlElement.getFirstChild();
            while (null != domNode) {
                if (domNode instanceof Element) {
                    final Element element = (Element) domNode;
                    if (element.getTagName().equalsIgnoreCase(XML_TAG_MATERIAL1)) {
                        m_mtl1 = parseMaterial(element, refObjectList);
                    } else if (element.getTagName().equalsIgnoreCase(XML_TAG_MATERIAL2)) {
                        m_mtl2 = parseMaterial(element, refObjectList);
                    } else {
                        throw new DynXmlObjParseException("Unrecognized Checkerboard XML description element <" +
                                element.getTagName() + ">.");
                    }
                }
                domNode = domNode.getNextSibling();
            }
        } catch (final Throwable t) {
            if (t instanceof DynXmlObjParseException) {
                throw (DynXmlObjParseException) t;
            } else {
                throw new DynXmlObjParseException(getClass().getName() + " parse exception", t);
            }
        }
    }

    public void toChildXmlElement(final @NotNull Element parentEl) {
        // This is the dynamically loaded object boilerplate
        final Element element = parentEl.getOwnerDocument().createElement(DynXmlObjLoader.XML_TAG);
        parentEl.appendChild(element);
        element.setAttribute(DynXmlObjLoader.XML_ATTR_CLASS, this.getClass().getName());
        element.setAttribute(DynXmlObjLoader.XML_ATTR_NAME, m_strName);
        // this is the Checkerboard specific stuff
        if ((null != m_mtl1) && (m_mtl1 instanceof IDynXmlObject)) {
            final Element elMtl1 = parentEl.getOwnerDocument().createElement(XML_TAG_MATERIAL1);
            ((IDynXmlObject) m_mtl1).toChildXmlElement(elMtl1);
            element.appendChild(elMtl1);
        }
        if ((null != m_mtl2) && (m_mtl2 instanceof IDynXmlObject)) {
            final Element elMtl2 = parentEl.getOwnerDocument().createElement(XML_TAG_MATERIAL2);
            ((IDynXmlObject) m_mtl2).toChildXmlElement(elMtl2);
            element.appendChild(elMtl2);
        }
    }

    protected IRtMaterial parseMaterial(final Element xmlElement, final LinkedList refObjectList) throws DynXmlObjParseException {
        final IRtMaterial mtl = null;
        try {
            Node domNode = xmlElement.getFirstChild();
            while (null != domNode) {
                if (domNode instanceof Element) {
                    final Element element = (Element) domNode;
                    if (element.getTagName().equalsIgnoreCase(DynXmlObjLoader.XML_TAG)) {
                        // Should be a material - that is the only dynamically loaded object that can be used
                        final Object obj = DynXmlObjLoader.LoadObject(element, refObjectList);
                        if (obj instanceof IRtMaterial) {
                            return (IRtMaterial) obj;
                        } else {
                            throw new DynXmlObjParseException("Block " + m_strName + " material could not be parsed");
                        }
                    } else if (element.getTagName().equalsIgnoreCase(XML_TAG_MATERIAL_REF)) {
                        // a material reference
                        final String strName = element.getAttribute(XML_TAG_REF_NAME_ATTR);
                        return resolveMaterialRef(strName, refObjectList);
                    } else {
                        throw new DynXmlObjParseException("Unrecognized Block XML description element <" +
                                element.getTagName() + ">.");
                    }
                }
                domNode = domNode.getNextSibling();
            }
        } catch (final Throwable t) {
            if (t instanceof DynXmlObjParseException) {
                throw (DynXmlObjParseException) t;
            } else {
                throw new DynXmlObjParseException(getClass().getName() + " parse exception", t);
            }
        }
        return null;
    }

    protected IRtMaterial resolveMaterialRef(final String strName, final LinkedList refObjectList) throws DynXmlObjParseException {
        if (!strName.equals("") && (null != refObjectList)) {
            for (final Object obj : refObjectList) {
                if ((obj instanceof IRtMaterial) && ((INamedObject) obj).getName().equals(strName)) {
                    return (IRtMaterial) obj;
                }
            }
        }
        throw new DynXmlObjParseException("Referenced material \"" + strName + "\" was not found.");
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // INamedObject interface implementation                                                                                 //
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public @NotNull String getName() {
        return m_strName;
    }

    public void setName(final @NotNull String strName) {
        m_strName = strName;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IRtMaterial interface implementation                                                                                  //
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * IGNORED, not applicable to <tt>Block</tt>.
     */
    public void initSampling(final int nSample, final @NotNull float[] f1dSample, final float[] f1dRandom, final @NotNull Point2f[] pt2dSample, final Point2f[] pt2dRandom,
                             final @NotNull Point3f[] pt3dSample, final Point3f[] pt3dRandom) {
    }

    // this needs to always return the same random number when given the same 2 ints
    private float rand2d(final int a, final int b) {
        return (rand_numbers[(a + b) % rand_size] * 0.6f) + 0.2f;
    }

    public void getColor(final @NotNull RGBf rgb, final @NotNull RayIntersection intersection, final @NotNull IRtLight[] lights, final @NotNull IRtGeometry[] rtObjects, final @NotNull IRtBackground rtBkg, final int nMaxRecursions,
                         final int nSample, final int nRandom) {
        final float xf;
        final float yf;
        final float mortar_width = 0.1f;
        final int xi;
        final int yi;
        boolean vert_square = false;
        final float this_offset;
        final float neighbor_offset;

        if (!intersection.m_bTexture) {
            throw new IllegalStateException("Block: texture coordinates have not been set");
        }

        intersection.m_mtl = m_mtl1;

        xf = Math.abs((intersection.m_ptTexture.x + 100000) % 1.0f);
        yf = Math.abs((intersection.m_ptTexture.y + 100000) % 1.0f);

        xi = (int) intersection.m_ptTexture.x + 100000;
        yi = (int) intersection.m_ptTexture.y + 100000;

        this_offset = rand2d(xi, yi);

        if ((xi + yi) % 2 == 0) {
            vert_square = true;

            if (Math.abs(this_offset - xf) < mortar_width)
                intersection.m_mtl = m_mtl2;
            else {
                if (xf > this_offset)  // to the right of the partition
                    neighbor_offset = rand2d(xi + 1, yi);
                else // left of partition
                    neighbor_offset = rand2d(xi - 1, yi);

                if (Math.abs(neighbor_offset - yf) < mortar_width)
                    intersection.m_mtl = m_mtl2;
            }

        } else {
            if (Math.abs(this_offset - yf) < mortar_width)
                intersection.m_mtl = m_mtl2;
            else {
                if (yf > this_offset) // above partition
                    neighbor_offset = rand2d(xi, yi + 1);
                else
                    neighbor_offset = rand2d(xi, yi - 1);

                if (Math.abs(neighbor_offset - xf) < mortar_width)
                    intersection.m_mtl = m_mtl2;
            }

        }

        // System.out.println(Float.toString(xf) + " " + Float.toString(yf));

	/* what's this for ?
    // before we go on, assign a local texture coordinate within the square
	intersection.m_ptTexture.i *= 2.0;
        intersection.m_ptTexture.i -= (float)Math.floor(intersection.m_ptTexture.i);
	intersection.m_ptTexture.j *= 2.0;
        intersection.m_ptTexture.j -= (float)Math.floor(intersection.m_ptTexture.j);
	intersection.m_ptTexture.k *= 2.0;
        intersection.m_ptTexture.k -= (float)Math.floor(intersection.m_ptTexture.k);
	*/

        intersection.m_mtl.getColor(rgb, intersection, lights, rtObjects, rtBkg, nMaxRecursions, nSample, nRandom);
    }
}
