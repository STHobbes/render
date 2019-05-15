/*
 * XfmLight.java
 *
 * Created on October 5, 2002, 11:30 PM
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
import cip.render.raytrace.interfaces.IRtLight;
import cip.render.raytrace.RayIntersection;
import cip.render.raytrace.LightInfo;
import cip.render.DynXmlObjLoader;
import cip.render.DynXmlObjParseException;
import cip.render.util3d.Xfm4x4f;

import java.util.LinkedList;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This is a transformed light implementation.  It applies a transform to a light to reposition the
 * local object coordinate system within the parent coordinate system (the coordinate system containing this
 * node).  The components of the transformed light are the positioning transform and the light to be
 * transformed.  Before rendering starts, the associated transformations for normals, and the back-transform
 * from object to world are precomputed.
 * <p>
 * This object is implemented primarily as a pass-through implementation where the normals, vectors, and
 * points for any illumination query are transformed into object space, the object operator is
 * called, and the result is back-transformed to world space if required.
 * <p>
 * The transformed light is specified as a node in an XML file as:<br><br>
 * <pre>
 *     <font style="color:blue">&lt;<b>DynamicallyLoadedObject</b> class="cip.render.raytrace.light.XfmLight"
 *                              name="<font style="color:magenta"><i>xfmLgtName</i></font>"&gt;</font>
 *         <font style="color:blue">&lt;<b>position</b> <font style="color:magenta"><i>Xfm4x4f_attributes</i></font>/&gt;</font>
 *         <font style="color:blue">&lt;<b>LightByRef</b> name="<font style="color:magenta"><i>lgtName</i></font>"/&gt;</font>
 *         <font style="color:blue">&lt;<b>DynamicallyLoadedObject</b> class="<font style="color:magenta"><i>transformedLightClass</i></font>"&gt;</font>
 *               <font style="color:gray"><b>.</b>
 *               <b>.</b>
 *             <i>transformed light specific node content</i>
 *               <b>.</b>
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
 * <td><tt>position</tt></td>
 * <td>The light position as specified by the <tt><i>Xfm4x4f_attributes</i></tt> which are described in
 * {@link Xfm4x4f#setValue(Element, boolean, boolean)}.  This object supports only
 * rigid-body transformation.  Specifically, translation and rotation are supported, scale and shear are
 * NOT supported.<br><br>
 * </td>
 * </tr>
 * <tr>
 * <td><tt>LightByRef</tt></td>
 * <td>A light specified by reference to the name of a previously loaded light.  The <tt>LightByRef</tt> is
 * mutually exclusive with the <tt>DynamicallyLoadedObject</tt> specification of a light.  If no light
 * is specified this results in a parse exception.
 * </td>
 * </tr>
 * <tr>
 * <td><tt>DynamicallyLoadedObject</tt></td>
 * <td>The specification fof a light to be transformed.  <tt>LightByRef</tt> is
 * mutually exclusive with the <tt>DynamicallyLoadedObject</tt> specification of a light.  If no light
 * is specified this results in a parse exception.  The dynamically
 * loaded light must implement the  {@link IRtLight} interface.
 * </td>
 * </tr>
 * </table>
 * </td>
 * </tr>
 * </table><br>
 * <p>
 * <b>Example of XML Specification</b>
 * <p>
 * The following specifies a {@link cip.render.raytrace.light.Spot} point source of colour 1,1,1 centered at (10,20,30) aimed at (0,0,0):
 * <pre>
 *     <font style="color:blue">&lt;<b>DynamicallyLoadedObject</b> class="cip.render.raytrace.light.XfmLight"
 *                              name="<font style="color:magenta">spot1</font>"&gt;</font>
 *         <font style="color:blue">&lt;<b>position</b> originAt="<font style="color:magenta">10,20,30</font>"
 *                   aimedAt="<font style="color:magenta">0,0,0</font>"/&gt;</font>
 *         <font style="color:blue">&lt;<b>DynamicallyLoadedObject</b> class="<font style="color:magenta">cip.render.raytrace.light.Spot</font>"&gt;</font>
 *             <font style="color:blue">&lt;<b>intensity</b> rgb="<font style="color:magenta">1,1,1</font>"/&gt;</font>
 *             <font style="color:blue">&lt;<b>beta</b>&gt;<font style="color:magenta">10</font>&lt;/<b>beta</b>&gt;</font>
 *         <font style="color:blue">&lt;/<b>DynamicallyLoadedObject</b>&gt;</font>
 *     <font style="color:blue">&lt;/<b>DynamicallyLoadedObject</b>&gt;</font>
 * </pre>
 *
 * @author royster.hall@gmail.com
 * @version 1.0
 * @since 1.0
 */
public class XfmLight extends ALight {
    private static final String XML_TAG_POSITION = "position";

    // The instance definition
    private final Xfm4x4f m_xfm = new Xfm4x4f().identity();         // the positioning transform for the light (light->world)
    private final Xfm4x4f m_xfmNormal = new Xfm4x4f();              // the light->world normal transform (transpose of m_xfm)
    //  This transforms both position and direction vectors
    private final Xfm4x4f m_xfxWldLgt = new Xfm4x4f();              // the world-light transform (inverse of m_xfm)
    private final Xfm4x4f m_xfxWldLgtNormal = new Xfm4x4f();        // the world-light normal transform (transpose of m_xfxWldLgt)
    private IRtLight m_lgt = null;                                  // the transformed light

    /**
     * Creates a new instance of XfmLight
     */
    public XfmLight() {
    }

    //-------------------------------------------------------------------------------------------------------------------------
    public IRtLight getLight() {
        return m_lgt;
    }

    public void setLight(final IRtLight lgt) {
        m_lgt = lgt;
    }

    public void getXfm(final Xfm4x4f xfm) {
        xfm.setValue(m_xfm);
    }

    public void setXfm(final Xfm4x4f xfm) {
        m_xfm.setValue(xfm);
        initForRender();
    }

    //-------------------------------------------------------------------------------------------------------------------------
    //  This initialization for rendering is the computation of the back transform (world->obj) and forward
    //  transform (obj->world) for normals.  Note that if we do not allow scale and shear, the forward transform for normals is
    //  equal to the forward transform for points and directions.  Otherwise the back forward for normals is the transpose
    //  of the inverse of the  forward transform for points.  NOTE: the back transform is already the inverse of the forward
    //  transform -- so we only need to transpose that to get the forward transform for normals.
    protected final void initForRender() {
        m_xfxWldLgt.setValue(m_xfm).invert();
        m_xfmNormal.setValue(m_xfxWldLgt).transpose();
        m_xfxWldLgtNormal.setValue(m_xfm).transpose();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IDynXmlObject interface implementation                                                                                //
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void loadFromXml(final @NotNull Element xmlElement, final @Nullable LinkedList refObjectList) throws DynXmlObjParseException {
        try {
            Node domNode = xmlElement.getFirstChild();
            while (null != domNode) {
                if (domNode instanceof Element) {
                    final Element element = (Element) domNode;
                    if (element.getTagName().equalsIgnoreCase(DynXmlObjLoader.XML_TAG)) {
                        // if this should be the contained light - note, we can load only one, and the last
                        //  one we encounter is the one that is saved.
                        final Object obj = DynXmlObjLoader.LoadObject(element, refObjectList);
                        if (obj instanceof IRtLight) {
                            m_lgt = (IRtLight) obj;
                        } else {
                            throw new DynXmlObjParseException("Transformed light " + m_strName + ": contained light could not be parsed");
                        }
                    } else if (element.getTagName().equalsIgnoreCase(XML_TAG_POSITION)) {
                        // pass the position on to the transformation for parsing
                        m_xfm.setValue(element, false, false);
                    } else if (element.getTagName().equalsIgnoreCase(XML_TAG_LIGHT_REF)) {
                        final String strName = element.getAttribute(XML_TAG_REF_NAME_ATTR);
                        m_lgt = resolveLightRef(strName, refObjectList);
                    } else {
                        throw new DynXmlObjParseException("Unrecognized Transformed Light element <" +
                                element.getTagName() + ">");
                    }
                }
                domNode = domNode.getNextSibling();
            }
            if (null == m_lgt) {
                throw new DynXmlObjParseException("Transformed light " + m_strName + ": no contained geometry specified.");
            }
            initForRender();
        } catch (final Throwable t) {
            if (t instanceof DynXmlObjParseException) {
                throw (DynXmlObjParseException) t;
            } else {
                throw new DynXmlObjParseException("Transformed light parse exception", t);
            }
        }
    }

    //-------------------------------------------------------------------------------------------------------------------------
    @Override
    protected void internalToXml(final Element element) {
        // The position transform
        try {
            // the material colour
            final Element elXfm = element.getOwnerDocument().createElement(XML_TAG_POSITION);
            m_xfm.toXmlAttr(elXfm, false, false);
            element.appendChild(elXfm);
        } catch (final Throwable t) {
            // there is a singularity in the transform, so we can't decompose it -- oops (means we couldn't render it either)
        }
        // the object
        if (null != m_lgt) {
            ((IDynXmlObject) m_lgt).toChildXmlElement(element);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IRtLight interface implementation                                                                                     //
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void setDimmer(final float fDimmer) {
        if (null != m_lgt) {
            m_lgt.setDimmer(fDimmer);
        }
    }

    //-------------------------------------------------------------------------------------------------------------------------
    @Override
    public boolean getLight(final LightInfo lightInfo, final RayIntersection intersection, final int nSample, final int nRandom) {
        boolean bRet = false;
        if (null != m_lgt) {
            // transform the intersection into the space of the light
            final RayIntersection rayIntTmp = intersection.borrowIntersection();
            try {
                m_xfxWldLgt.transform(intersection.m_pt, rayIntTmp.m_pt);
                m_xfxWldLgtNormal.transform(intersection.m_vNormal, rayIntTmp.m_vNormal);
                // test whether the intersection is illuminated
                if (bRet = m_lgt.getLight(lightInfo, rayIntTmp, nSample, nRandom)) {
                    // the intersection is illuminated, back transform the illumination info
                    m_xfm.transform(lightInfo.m_ptFrom);
                    m_xfmNormal.transform(lightInfo.m_vDir);
                }
            } finally {
                    // return the temporary ray intersection.
                    intersection.returnIntersection(rayIntTmp);
            }
        }
        return bRet;
    }

    //-------------------------------------------------------------------------------------------------------------------------
    @Override
    public String toString() {
        return m_lgt.toString();
    }
}
