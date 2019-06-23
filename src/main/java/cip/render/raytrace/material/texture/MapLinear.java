/*
 * MapLinear.java
 *
 * Created on November 10, 2002, 5:12 PM
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
import cip.render.utilColour.RGBf;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.LinkedList;

/**
 * <p>
 * Performs a linear mapping of either object or world coordinates into the texture coordinates of the intersection.
 * The mapping is simply setting the texture reference equal to either the object or world coordinates of the
 * intersection point.  The perturbation vectors are scaled by the cosine projection of the normal.  The intersection
 * with the new texture coordinates is passed to the next material in the chain for color evaluation.</p>
 * <p>
 * Note that this is a 3D projection. It is 3D coordinates in a 3D space, so using a 2D texture, like an image, will most
 * probably produce sub-optimal results. More directly, don't use 2D textures with this mapping because they will be really
 * disappointing; only use 3D textures.
 * </p>
 * <p>
 * The linear mapping is specified as:<br><br>
 * <pre>
 *     <font style="color:blue">&lt;<b>DynamicallyLoadedObject</b> class="cip.raytrace.material.texture.MapLinear"
 *                              name="<font style="color:magenta"><i>myLinearMap</i></font>"&gt;</font>
 *         <font style="color:blue">&lt;<b>object</b>/&gt;</font>
 *         <font style="color:blue">&lt;<b>world</b>/&gt;</font>
 *         <font style="color:blue">&lt;<b>MaterialByRef</b> name="<font style="color:magenta"><i>materialName</i></font>"/&gt;</font>
 *         <font style="color:blue">&lt;<b>DynamicallyLoadedObject</b> class="<font style="color:magenta"><i>materialClass</i></font>"&gt;</font>
 *               <font style="color:gray"><b>.</b>
 *             <i>material specific nodes and attributes</i>
 *               <b>.</b></font>
 *         <font style="color:blue">&lt;/<b>DynamicallyLoadedObject</b>&gt;</font>
 *     <font style="color:blue">&lt;/<b>DynamicallyLoadedObject</b>&gt;</font>
 * </pre>
 * <table border="0" width="90%">
 * <caption style="text-align:left">where:</caption>
 * <tr>
 * <td style="width:5%"></td>
 * <td><table border="1" summary="">
 * <tr>
 * <td><tt>object</tt></td>
 * <td>Maps the object coordinates of the intersection into the texture coordinates.
 * <tt>object</tt> and <tt>world</tt> are mutually exclusive.
 * </td>
 * </tr>
 * <tr>
 * <td><tt>world</tt></td>
 * <td>Maps the world coordinates of the intersection into the texture coordinates.  <tt>object</tt> and
 * <tt>world</tt> are mutually exclusive.  If not specified, the material defaults to <tt>object</tt>.
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
public class MapLinear extends ATexture {
    protected static final String XML_TAG_OBJECT = "object";
    protected static final String XML_TAG_WORLD = "world";
    protected static final String XML_TAG_REF_NAME_ATTR = "name";
    protected static final String XML_TAG_MATERIAL_REF = "MaterialByRef";

    protected static final int MAP_OBJECT_COORDS = 0;
    protected static final int MAP_WORLD_COORDS = 1;

    // The instance definition
    protected String m_strName = cip.render.raytrace.material.PackageConstants.DEFAULT_NAME;      // this material name
    protected IRtMaterial m_mtl = null;
    protected int m_nMapFrom = MAP_OBJECT_COORDS;

    /**
     * Creates a new instance of <tt>MapLinear</tt>.
     */
    public MapLinear() {
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
                    if (element.getTagName().equalsIgnoreCase(XML_TAG_OBJECT)) {
                        m_nMapFrom = MAP_OBJECT_COORDS;
                    } else if (element.getTagName().equalsIgnoreCase(XML_TAG_WORLD)) {
                        m_nMapFrom = MAP_WORLD_COORDS;
                    } else if (element.getTagName().equalsIgnoreCase(DynXmlObjLoader.XML_TAG)) {
                        // Should be a material - that is the only dynamically loaded object that can be used
                        final Object obj = DynXmlObjLoader.LoadObject(element, refObjectList);
                        if (obj instanceof IRtMaterial) {
                            m_mtl = (IRtMaterial) obj;
                        } else {
                            throw new DynXmlObjParseException("MapLinear " + m_strName + " material could not be parsed");
                        }
                    } else if (element.getTagName().equalsIgnoreCase(XML_TAG_MATERIAL_REF)) {
                        // a material reference
                        final String strName = element.getAttribute(XML_TAG_REF_NAME_ATTR);
                        m_mtl = resolveMaterialRef(strName, refObjectList);
                    } else {
                        throw new DynXmlObjParseException("Unrecognized MapLinear XML description element <" +
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
        // conductor/dielectric
        final Element elConductor =
                element.getOwnerDocument().createElement((m_nMapFrom == MAP_OBJECT_COORDS) ? XML_TAG_OBJECT : XML_TAG_WORLD);
        element.appendChild(elConductor);
        // this is the MapNatural specific stuff
        if ((null != m_mtl) && (m_mtl instanceof IDynXmlObject)) {
            ((IDynXmlObject) m_mtl).toChildXmlElement(element);
        }
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
     * IGNORED, not applicable to <tt>MapLinear</tt>.
     */
    public void initSampling(final int nSample, final @NotNull float[] f1dSample, final float[] f1dRandom, final @NotNull Point2f[] pt2dSample, final Point2f[] pt2dRandom,
                             final @NotNull Point3f[] pt3dSample, final Point3f[] pt3dRandom) {
    }

    public void getColor(final @NotNull RGBf rgb, final @NotNull RayIntersection intersection, final @NotNull IRtLight[] lights, final @NotNull IRtGeometry[] rtObjects, final @NotNull IRtBackground rtBkg, final int nMaxRecursions,
                         final int nSample, final int nRandom) {
        switch (m_nMapFrom) {
            case MAP_OBJECT_COORDS:
                intersection.m_ptTexture.setValue(intersection.m_ptObject);
                intersection.m_vTexture[0].setValue(
                        (float) Math.sqrt(1.0 - (intersection.m_vObjNormal.i * intersection.m_vObjNormal.i)), 0.0f, 0.0f);
                intersection.m_vTexture[1].setValue(
                        0.0f, (float) Math.sqrt(1.0 - (intersection.m_vObjNormal.j * intersection.m_vObjNormal.j)), 0.0f);
                intersection.m_vTexture[2].setValue(
                        0.0f, 0.0f, (float) Math.sqrt(1.0 - (intersection.m_vObjNormal.k * intersection.m_vObjNormal.k)));
                intersection.m_bTexture = true;
                break;
            case MAP_WORLD_COORDS:
                intersection.m_ptTexture.setValue(intersection.m_pt);
                intersection.m_vTexture[0].setValue(
                        (float) Math.sqrt(1.0 - (intersection.m_vNormal.i * intersection.m_vNormal.i)), 0.0f, 0.0f);
                intersection.m_vTexture[1].setValue(
                        0.0f, (float) Math.sqrt(1.0 - (intersection.m_vNormal.j * intersection.m_vNormal.j)), 0.0f);
                intersection.m_vTexture[2].setValue(
                        0.0f, 0.0f, (float) Math.sqrt(1.0 - (intersection.m_vNormal.k * intersection.m_vNormal.k)));
                intersection.m_bTexture = true;
                break;
        }
        intersection.m_mtl = m_mtl;
        m_mtl.getColor(rgb, intersection, lights, rtObjects, rtBkg, nMaxRecursions, nSample, nRandom);
    }

}
