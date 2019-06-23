/*
 * Aperture.java
 *
 * Created on October 31, 2002, 11:28 AM
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
package cip.render.raytrace.camera;

import cip.render.DynXmlObjParseException;
import cip.render.INamedObject;
import cip.render.raytrace.RayIntersection;
import cip.render.util3d.Line3f;
import cip.render.util3d.Point3f;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.LinkedList;

/**
 * This is The implementation of a perspective camera focused through a lens with variable aperture.  The aperture camera
 * is a camera in which the lens is an area lens, modeled as a roughly square area parallel to the target plane.  This
 * camera will simulate depth-of-field using distributed ray techniques.
 * <p>
 * The area camera is specified as a node in an XML file as:<br><br>
 * <tt>
 * &nbsp;&nbsp;&nbsp; <font style="color:blue">&lt;<b>DynamicallyLoadedObject</b>
 * class="cip.raytrace.camera.Aperture" name="<font style="color:magenta"><i>cameraName</i></font>"&gt;</font><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <font style="color:blue">&lt;<b>position</b> <font style="color:magenta"><i>Xfm4x4f_attributes</i></font>/&gt;</font><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <font style="color:blue">&lt;<b>targetWidth</b>&gt;<font style="color:magenta"><i>width</i></font>&lt;/<b>targetWidth</b>&gt;</font><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <font style="color:blue">&lt;<b>targetDist</b>&gt;<font style="color:magenta"><i>distance</i></font>&lt;/<b>targetDist</b>&gt;</font><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <font style="color:blue">&lt;<b>lensDiameter</b>&gt;<font style="color:magenta"><i>diameter</i></font>&lt;/<b>lensDiameter</b>&gt;</font><br>
 * &nbsp;&nbsp;&nbsp; <font style="color:blue">&lt;/<b>DynamicallyLoadedObject</b>&gt;</font><br><br>
 * </tt>
 * The elements are optional and are applied in the order specified.  For example if you specify a target distance, but then specify the
 * camera position by origin and aimed at - the target distance will be adjusted to be the distance between the origin and the aimed at.  Conversly,
 * if you specify the position first and then the distance, the camera position and orientation is preserved and the target point is moved to the
 * distance specified from the eye position.  If you specify camera position more than once, the last position will override the earlier positions.
 * <table style="width:90%">
 * <caption style="text-align:left">where:</caption>
 * <tr>
 * <td style="width:5%"></td>
 * <td><table border="1" summary="">
 * <tr>
 * <td><tt>postion</tt></td>
 * <td>The camera position and orientation as specified by the <tt><i>Xfm4x4f_attributes</i></tt> which are described in
 * {@link cip.render.util3d.Xfm4x4f#setValue(Element, boolean, boolean)}.  The most common methods of
 * specification are <i>originAt</i> and <i>aimedAt</i> or <i>originAt</i> and <i>aximuth</i>, <i>altitude</i> orientation.
 * If <i>originAt</i> is set, this becomes the camera location.  If <i>aimedAt</i> is set, this becomes the parget point.
 * </td>
 * </tr>
 * <tr>
 * <td><tt>targetWidth</tt></td>
 * <td>The target plane width.
 * </td>
 * </tr>
 * <tr>
 * <td><tt>targetDist</tt></td>
 * <td>The distance from the parget point to the camera.  If the camera position has been previously set, then it is maintained
 * and the target point is moved to meet the specified target distance.  If the camera position has not previously been set, the
 * target point is maintained and the camera is moved to meet the specified target distance.
 * </td>
 * </tr>
 * <tr>
 * <td><tt>lensDiameter</tt> or <tt>aperture</tt></td>
 * <td>The diameter of the lens.  The default is a lens diameter of 0.5.  As in real life, depth-of-field (the range of distance
 * that the image is in focus) is inversely proportional to the diameter of the lens.  The larger the lens, the smaller the
 * depth-of-field, the smaller the lens, the larger the depth-of-field.  The image plane (target plane) is where the camera
 * is focused.
 * </td>
 * </tr>
 * </table>
 * </td>
 * </tr>
 * </table><br>
 * <p>
 * <b>Example of XML Specification</b>
 * <p>
 * The following specifies a pinhole camera with the camera location and target point specified in the position, and a target plane
 * width of 6:<br><br>
 * <tt>
 * &nbsp;&nbsp;&nbsp; <font style="color:blue">&lt;<b>DynamicallyLoadedObject</b>
 * class="cip.raytrace.camera.Aperture" name="<font style="color:magenta">camera</font>"&gt;</font><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <font style="color:blue">&lt;<b>position</b>
 * originAt="<font style="color:magenta">5.0,-5.0,5.0</font>"
 * aimedAt="<font style="color:magenta">1.0f,-1.0f,-0.5f</font>"/&gt;</font><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <font style="color:blue">&lt;<b>targetWidth</b>&gt;<font style="color:magenta">6</font>&lt;/<b>targetWidth</b>&gt;</font><br>
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <font style="color:blue">&lt;<b>lensDiameter</b>&gt;<font style="color:magenta">.5</font>&lt;/<b>lensDiameter</b>&gt;</font><br>
 * &nbsp;&nbsp;&nbsp; <font style="color:blue">&lt;/<b>DynamicallyLoadedObject</b>&gt;</font><br><br>
 * </tt>
 *
 * @author royster.hall@gmail.com
 * @version 1.0
 * @since 1.0
 */
public class Aperture extends ACamera {
    protected static final String XML_TAG_LENS_DIA = "lensDiameter";
    protected static final String XML_TAG_APERTURE = "aperture";

    Point3f m_ptEye = new Point3f();
    float m_fDia = 0.5f;

    /**
     * Creates a new instance of an <tt>Aperture</tt> camera.
     */
    public Aperture() {
        initForRender();
    }

    protected void initForRender() {
        super.initForRender();
        m_ptEye.x = m_ptTarget.x - (m_vView.i * m_fTargetDist);
        m_ptEye.y = m_ptTarget.y - (m_vView.j * m_fTargetDist);
        m_ptEye.z = m_ptTarget.z - (m_vView.k * m_fTargetDist);

        if (DEBUG) {
            System.out.println("Lens.m_vView: (" + m_vView.i + ',' + m_vView.j + ',' + m_vView.k + ')');
            System.out.println("Lens.m_vSide: (" + m_vSide.i + ',' + m_vSide.j + ',' + m_vSide.k + ')');
            System.out.println("Lens.m_vUp:   (" + m_vUp.i + ',' + m_vUp.j + ',' + m_vUp.k + ')');
            System.out.println("Lens.m_ptEye: (" + m_ptEye.x + ',' + m_ptEye.y + ',' + m_ptEye.z + ')');
            System.out.println("Lens.m_fDia:  (" + m_ptEye.x + ',' + m_ptEye.y + ',' + m_ptEye.z + ')');
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IDynXmlObject interface implementation                                                                                //
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void loadFromXml(final @NotNull Element xmlElement, final LinkedList<INamedObject> refObjectList) throws DynXmlObjParseException {
        initForParse();
        try {
            // Read the specified components for the camera
            Node domNode = xmlElement.getFirstChild();
            while (null != domNode) {
                if (domNode instanceof Element) {
                    final Element element = (Element) domNode;
                    if (element.getTagName().equalsIgnoreCase(XML_TAG_LENS_DIA) ||
                            element.getTagName().equalsIgnoreCase(XML_TAG_APERTURE)) {
                        Node textNode = element.getFirstChild();
                        while (null != textNode) {
                            if (textNode.getNodeType() == Node.TEXT_NODE) {
                                m_fDia = Float.parseFloat(textNode.getNodeValue().trim());
                                break;
                            }
                            textNode = textNode.getNextSibling();
                        }
                    } else if (!parseACameraTag(element)) {
                        throw new DynXmlObjParseException("Unrecognized Lens camera element <" +
                                element.getTagName() + ">, terminating model parsing.");
                    }
                }
                domNode = domNode.getNextSibling();
            }
            // The camera node has been read.  Derive the other components of the camera.
            initForRender();
        } catch (final Throwable t) {
            if (t instanceof DynXmlObjParseException) {
                throw (DynXmlObjParseException) t;
            } else {
                throw new DynXmlObjParseException("Lens Camera parse exception", t);
            }
        }
    }

    // camera specific node information
    protected void internalToXml(final Element element) {
        // lens diameter
        final Element elDia = element.getOwnerDocument().createElement(XML_TAG_LENS_DIA);
        element.appendChild(elDia);
        elDia.appendChild(element.getOwnerDocument().createTextNode(Float.toString(m_fDia)));
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IRtCamera interface implementation                                                                                    //
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void getRay(final Line3f ray, final RayIntersection rayIntersection, final int nXpixel, final int nYpixel, final int nSample, final int nRandom) {
        if (!m_bScreenInit) throw new IllegalStateException("Cannot get pixel rays before the screen information is set.");
        // use the ray origin as a temporary point to retreive the pixel location (this is so we can keep things thread-safe without
        //  screwing around with creating or caching a bunch of temporary stuff or synchonizing things).
        getPixelPoint(ray.m_ptOrg, nXpixel, nYpixel, nSample, nRandom);
        // Ctemporarily hold ths target point in the ray direction
        ray.m_vDir.i = ray.m_ptOrg.x;
        ray.m_vDir.j = ray.m_ptOrg.y;
        ray.m_vDir.k = ray.m_ptOrg.z;

        // Set the ray origin - this is the eye point offset by the scaled offset array
        if (null == m_ptSample) {
            ray.m_ptOrg.setValue(m_ptEye);
        } else {
            float xScale = m_ptSample[nSample].x;
            float yScale = m_ptSample[nSample].y;
            if (null != m_ptRandom) {
                xScale += m_ptRandom[nRandom].x;
                yScale += m_ptRandom[nRandom].y;
            }
            xScale *= m_fDia;
            yScale *= m_fDia;
            ray.m_ptOrg.x = m_ptEye.x + (m_vSide.i * xScale) + (m_vUp.i * yScale);
            ray.m_ptOrg.y = m_ptEye.y + (m_vSide.j * xScale) + (m_vUp.j * yScale);
            ray.m_ptOrg.z = m_ptEye.z + (m_vSide.k * xScale) + (m_vUp.k * yScale);
        }

        // Compute the unnornalized direction vector for the pixel
        ray.m_vDir.i -= ray.m_ptOrg.x;
        ray.m_vDir.j -= ray.m_ptOrg.y;
        ray.m_vDir.k -= ray.m_ptOrg.z;
        // Normalize the direction (NOTE: we are holding on the the length here in case we need to initialize the ray intersection)
        final float fSampleDist = ray.m_vDir.getLength();
        ray.m_vDir.scale(1.0f / fSampleDist);
        // initialize the rayIntersection if one was provided
        if (null != rayIntersection) {
            rayIntersection.initialize(ray.m_vDir);
            rayIntersection.m_fSampleStartOffset = 0.0f;
            rayIntersection.m_fSampleSolidAngle = (m_fSampleArea * ray.m_vDir.dot(m_vView)) / (fSampleDist * fSampleDist);
        }
    }

}
