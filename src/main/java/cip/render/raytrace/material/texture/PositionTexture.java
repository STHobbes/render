/*
 * PositionTexture.java
 *
 * Created on November 7, 2002, 2:05 PM
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
import cip.render.util3d.Xfm4x4f;
import cip.render.utilColour.RGBf;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.LinkedList;

/**
 * Positions the texture by transforming the texture coordinates by a specified positioning transformation.
 * The texture coordinates and vectors are transformed 'in place' in the RayIntersection and then the
 * intersection is passed to the next material in the chain for color evaluation.
 * <p>
 * The texture positioning is specified as:
 * <pre>
 *     <font style="color:blue">&lt;<b>DynamicallyLoadedObject</b> class="cip.raytrace.material.texture.PositionTexture" name="<font style="color:magenta"><i>myPositionedTexture</i></font>"&gt;</font>
 *       <font style="color:blue">&lt;<b>position</b> <font style="color:magenta"><i>Xfm4x4f_attributes</i></font>/&gt;</font>
 *       <font style="color:blue">&lt;<b>MaterialByRef</b> name="<font style="color:magenta"><i>materialName</i></font>"/&gt;</font>
 *       <font style="color:blue">&lt;<b>DynamicallyLoadedObject</b> class="<font style="color:magenta"><i>materialClass</i></font>"&gt;</font>
 *           <font style="color:gray"><b>.</b>
 *         <i>material specific nodes and attributes</i>
 *           <b>.</b></font>
 *       <font style="color:blue">&lt;/<b>DynamicallyLoadedObject</b>&gt;</font>
 *    <font style="color:blue">&lt;/<b>DynamicallyLoadedObject</b>&gt;</font>
 * </pre>
 * <table border="0" width="90%">
 * <caption style="text-align:left">where:</caption> <tr>
 * <td style="width:5%"></td>
 * <td><table border="1" summary="">
 * <tr>
 * <td><tt>position</tt></td>
 * <td>The geometry position as specified by the <tt><i>Xfm4x4f_attributes</i></tt> which are described in
 * {@link cip.render.util3d.Xfm4x4f#setValue(org.w3c.dom.Element, boolean, boolean)}.  All transformation options
 * including scale and shear are supported for texture positioning.<br>
 * </td>
 * </tr>
 * <tr>
 * <td><tt>MaterialByRef</tt></td>
 * <td>A material specfied by reference to the name of a previously loaded material.  <tt>MaterialByRef</tt> is
 * mutually exclusive with the <tt>DynamicallyLoadedObject</tt> specification of a material.  If no material
 * is specified the mapping will generate an exception during rendering.
 * </td>
 * </tr>
 * <tr>
 * <td><tt>DynamicallyLoadedObject</tt></td>
 * <td>The specification fof a material for the sphere.  <tt>MaterialByRef</tt> is
 * mutually exclusive with the <tt>DynamicallyLoadedObject</tt> specification of a material.  The dynamically
 * loaded object must implement the  {@link cip.render.raytrace.interfaces.IRtMaterial} interface.  If no material
 * is specified the mapping will generate an exception during rendering.
 * </td>
 * </tr>
 * </table>
 * </td>
 * </tr>
 * </table><br>
 *
 * @author royster.hall@gmail.com
 * @version 1.0
 * @since 1.0
 */
public class PositionTexture extends ATexture {
    protected static final String XML_TAG_REF_NAME_ATTR = "name";
    protected static final String XML_TAG_MATERIAL_REF = "MaterialByRef";
    private static final String XML_TAG_POSITION = "position";

    // The instance definition
    protected String m_strName = cip.render.raytrace.material.PackageConstants.DEFAULT_NAME;      // this material name
    protected Xfm4x4f m_xfm = new Xfm4x4f();
    protected IRtMaterial m_mtl = null;

    /**
     * Creates a new instance of <tt>PositionTexture</tt>
     */
    public PositionTexture() {
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IDynXmlObject interface implementation                                                                                     //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void loadFromXml(final @NotNull Element xmlElement, final LinkedList<INamedObject> refObjectList)
            throws DynXmlObjParseException {
        try {
            Node domNode = xmlElement.getFirstChild();
            while (null != domNode) {
                if (domNode instanceof Element) {
                    final Element element = (Element) domNode;
                    if (element.getTagName().equalsIgnoreCase(DynXmlObjLoader.XML_TAG)) {
                        // Should be a material - that is the only dynamically loaded object that can be used
                        final Object obj = DynXmlObjLoader.LoadObject(element, refObjectList);
                        if (obj instanceof IRtMaterial) {
                            m_mtl = (IRtMaterial) obj;
                        } else {
                            throw new DynXmlObjParseException("PositionTexture " + m_strName + " material could not be parsed");
                        }
                    } else if (element.getTagName().equalsIgnoreCase(XML_TAG_MATERIAL_REF)) {
                        // a material reference
                        final String strName = element.getAttribute(XML_TAG_REF_NAME_ATTR);
                        m_mtl = resolveMaterialRef(strName, refObjectList);
                    } else if (element.getTagName().equalsIgnoreCase(XML_TAG_POSITION)) {
                        // pass the position on to the transformation for parsing
                        m_xfm.setValue(element, true, true);
                    } else {
                        throw new DynXmlObjParseException("Unrecognized PositionTexture XML description element <" +
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
        try {
            // the transformed position
            final Element elXfm = element.getOwnerDocument().createElement(XML_TAG_POSITION);
            m_xfm.toXmlAttr(elXfm, true, true);
            element.appendChild(elXfm);
        } catch (final Throwable t) {
            // there is a singularity in the transform, so we can't decompose it -- oops (means we couldn't render it either)
        }
        // this is the MapNatural specific stuff
        if ((null != m_mtl) && (m_mtl instanceof IDynXmlObject)) {
            ((IDynXmlObject) m_mtl).toChildXmlElement(element);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // INamedObject interface implementation                                                                                      //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public @NotNull String getName() {
        return m_strName;
    }

    public void setName(final @NotNull String strName) {
        m_strName = strName;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IRtMaterial interface implementation                                                                                       //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * IGNORED, not applicable to <tt>PositionTexture</tt>.
     */
    public void initSampling(final int nSample, final @NotNull float[] f1dSample, final float[] f1dRandom,
                             final @NotNull Point2f[] pt2dSample, final Point2f[] pt2dRandom,
                             final @NotNull Point3f[] pt3dSample, final Point3f[] pt3dRandom) {
    }

    public void getColor(final @NotNull RGBf rgb, final @NotNull RayIntersection intersection, final @NotNull IRtLight[] lights,
                         final @NotNull IRtGeometry[] rtObjects, final @NotNull IRtBackground rtBkg, final int nMaxRecursions,
                         final int nSample, final int nRandom) {
        if (!intersection.m_bTexture) {
            throw new IllegalStateException("PositionTexture: texture coordinates have not been set");
        }
        // So this could be only a 2D texture, in which case, the z is Float.NaN, so we really can't transform the point.
        if (Float.isNaN(intersection.m_ptTexture.z)) {
            intersection.m_ptTexture.z = 0.0f;
            m_xfm.transform(intersection.m_ptTexture);
            m_xfm.transform(intersection.m_vTexture[0]);
            m_xfm.transform(intersection.m_vTexture[1]);
            intersection.m_ptTexture.z = Float.NaN;
        } else {
            m_xfm.transform(intersection.m_ptTexture);
            m_xfm.transform(intersection.m_vTexture[0]);
            m_xfm.transform(intersection.m_vTexture[1]);
            m_xfm.transform(intersection.m_vTexture[2]);
        }
        intersection.m_mtl = m_mtl;
        m_mtl.getColor(rgb, intersection, lights, rtObjects, rtBkg, nMaxRecursions, nSample, nRandom);
    }

}
