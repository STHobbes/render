/*
 * ACamera.java
 *
 * Created on October 17, 2002, 3:21 PM
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

import cip.render.IDynXmlObject;
import cip.render.INamedObject;
import cip.render.raytrace.interfaces.IRtCamera;
import cip.render.raytrace.RayIntersection;
import cip.render.DynXmlObjLoader;
import cip.render.util.AngleF;
import cip.render.util2d.Point2f;
import cip.render.util3d.Point3f;
import cip.render.util3d.Line3f;
import cip.render.util3d.Vector3f;
import cip.render.util3d.Xfm4x4f;
import cip.render.util3d.ZeroLengthVectorException;

import java.util.StringTokenizer;

import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This is an abstract class for a ray tracing camera.  It offers an implementation of the most common elements
 * of a ray tracing camera and some functions to help in camera parsing.
 *
 * @author royster.hall@gmail.com
 * @version 1.0
 * @since 1.0
 */
public abstract class ACamera implements IDynXmlObject, INamedObject, IRtCamera {
    protected static final String XML_TAG_ORIENTATION = "position";
    protected static final String XML_TAG_TARGET_WID = "targetWidth";
    protected static final String XML_TAG_TARGET_DIST = "targetDist";

    protected static final String DEFAULT_NAME = "<unspecified>";

    // Turns on/off debug output to System.out.  Debug output is limited to object load and/or instantiation.  There is NEVER
    //  any printed output for operations that would occur inside the rendering loop.
    //
    protected static final boolean DEBUG = false;
    // The instance definition
    protected String m_strName = DEFAULT_NAME;  // the camera name

    protected Point3f m_ptTarget = new Point3f();
    protected float m_fTargetWidth = 4.0f;
    protected float m_fTargetHeight = 4.0f;
    protected float m_fTargetDist = 6.0f;
    protected Xfm4x4f m_xfmOrientation = new Xfm4x4f().identity().
            rotate(Xfm4x4f.AXIS_Z, new AngleF(AngleF.DEGREES, -180.0f));  // front view
    protected Point2f[] m_ptSample = null;
    protected Point2f[] m_ptRandom = null;

    // derived informtation for the camera
    protected boolean m_bScreenInit = false;
    protected Vector3f m_vView = new Vector3f();
    protected Vector3f m_vUp = new Vector3f();
    protected Vector3f m_vSide = new Vector3f();
    protected Point3f m_ptTargetUL = new Point3f();     // the upper left of the target plane
    protected float m_fIncX;                            // the scale of the side vector per pixel X
    protected float m_fIncY;                            // the scale of the up vector per pixel Y
    protected float m_fSampleArea;                      // the area of a sample on the target plane

    // parsing related
    private boolean m_bSetCamEye;


    /**
     * Creates a new instance of <tt>ACamera</tt>
     */
    public ACamera() {
    }

    //-------------------------------------------------------------------------------------------------------------------------
    public ACamera setPosition(final Point3f ptTarget, final Point3f ptEye, final float width) throws ZeroLengthVectorException {
        m_xfmOrientation.setValue(ptEye, ptTarget);
        m_ptTarget = ptTarget;
        m_fTargetWidth = width;
        m_fTargetDist = ptEye.getDistanceTo(ptTarget);
        initForRender();
        return this;
    }

    public Point3f getTargetPt(final Point3f ptTarget) {
        ptTarget.setValue(m_ptTarget);
        return ptTarget;
    }

    public float getTargetWidth() {
        return m_fTargetWidth;
    }

    public float getTargetDistance() {
        return m_fTargetDist;
    }

    public Vector3f getView(final Vector3f vView) {
        vView.setValue(m_vView);
        return vView;
    }

    public Vector3f getSide(final Vector3f vSide) {
        vSide.setValue(m_vSide);
        return vSide;
    }

    public Vector3f getUp(final Vector3f vUp) {
        vUp.setValue(m_vUp);
        return vUp;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IDynXmlObject interface implementation                                                                                //
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void initForParse() {
        m_bSetCamEye = false;
    }

    public boolean parseACameraTag(final Element element) throws Throwable {
        if (element.getTagName().equalsIgnoreCase(XML_TAG_ORIENTATION)) {
            // parse the orientation element into the transform
            m_xfmOrientation.setValue(element, false, false);
            resetCamVectors();
            // check for a position and 'aimed at' specification.
            final String strTranslate = element.getAttribute(Xfm4x4f.XML_ATTR_ORIGIN).trim();
            final String strAimAt = element.getAttribute(Xfm4x4f.XML_ATTR_AIMED).trim();
            if (!strAimAt.equals("")) {
                // the aimed at is a string of the form i,j,k. -- This is the new target point.  Reset the distance based
                //  on the position and the aimed-at point
                m_bSetCamEye = true;
                final StringTokenizer tokens = new StringTokenizer(strAimAt, ",");
                m_ptTarget.x = Float.parseFloat(tokens.nextToken().trim());
                m_ptTarget.y = Float.parseFloat(tokens.nextToken().trim());
                m_ptTarget.z = Float.parseFloat(tokens.nextToken().trim());
                m_fTargetDist = (float) Math.sqrt(
                        ((m_ptTarget.x - m_xfmOrientation.get(0,3)) * (m_ptTarget.x - m_xfmOrientation.get(0,3))) +
                        ((m_ptTarget.y - m_xfmOrientation.get(1,3)) * (m_ptTarget.y - m_xfmOrientation.get(1,3))) +
                        ((m_ptTarget.z - m_xfmOrientation.get(2,3)) * (m_ptTarget.z - m_xfmOrientation.get(2,3))));
            } else if (!strTranslate.equals("")) {
                // the camera position was set, use the new camera position and distance to reset the target
                m_bSetCamEye = true;
                m_ptTarget.x = m_xfmOrientation.get(0,3) + (m_fTargetDist * m_vView.i);
                m_ptTarget.y = m_xfmOrientation.get(1,3) + (m_fTargetDist * m_vView.j);
                m_ptTarget.z = m_xfmOrientation.get(2,3) + (m_fTargetDist * m_vView.k);
            }

        } else if (element.getTagName().equalsIgnoreCase(XML_TAG_TARGET_WID)) {
            Node textNode = element.getFirstChild();
            while (null != textNode) {
                if (textNode.getNodeType() == Node.TEXT_NODE) {
                    m_fTargetWidth = Float.parseFloat(textNode.getNodeValue().trim());
                    break;
                }
                textNode = textNode.getNextSibling();
            }
        } else if (element.getTagName().equalsIgnoreCase(XML_TAG_TARGET_DIST)) {
            Node textNode = element.getFirstChild();
            while (null != textNode) {
                if (textNode.getNodeType() == Node.TEXT_NODE) {
                    m_fTargetDist = Float.parseFloat(textNode.getNodeValue().trim());
                    break;
                }
                textNode = textNode.getNextSibling();
            }
            if (m_bSetCamEye) {
                // an explicit camera position was specified - reset the target point
                m_ptTarget.x = m_xfmOrientation.get(0,3) + (m_fTargetDist * m_vView.i);
                m_ptTarget.y = m_xfmOrientation.get(1,3) + (m_fTargetDist * m_vView.j);
                m_ptTarget.z = m_xfmOrientation.get(2,3) + (m_fTargetDist * m_vView.k);
            }
        } else {
            return false;
        }
        return true;
    }

    protected void resetCamVectors() {
        m_vView.setValue(-m_xfmOrientation.get(0,1), -m_xfmOrientation.get(1,1), -m_xfmOrientation.get(2,1));
        m_vSide.setValue(-m_xfmOrientation.get(0,0), -m_xfmOrientation.get(1,0), -m_xfmOrientation.get(2,0));
        m_vUp.setValue(m_xfmOrientation.get(0,2), m_xfmOrientation.get(1,2), m_xfmOrientation.get(2,2));
    }

    // Called at the end of parsing to initialize the camera for rendering
    protected void initForRender() {
        resetCamVectors();
        m_bScreenInit = false;
    }

    //-------------------------------------------------------------------------------------------------------------------------
    public void toChildXmlElement(final @NotNull Element parentEl) {
        // This is the dynamically loaded object boilerplate
        final Element element = parentEl.getOwnerDocument().createElement(DynXmlObjLoader.XML_TAG);
        parentEl.appendChild(element);
        element.setAttribute(DynXmlObjLoader.XML_ATTR_CLASS, this.getClass().getName());
        element.setAttribute(DynXmlObjLoader.XML_ATTR_NAME, m_strName);
        // the camera position
        try {
            final Element elXfm = element.getOwnerDocument().createElement(XML_TAG_ORIENTATION);
            m_xfmOrientation.toXmlAttr(elXfm, false, false);
            element.appendChild(elXfm);
        } catch (final Throwable t) {
            // there is a singularity in the transform, so we can't decompose it -- oops (means we couldn't render it either)
        }
        // The target distance
        final Element elDist = element.getOwnerDocument().createElement(XML_TAG_TARGET_DIST);
        element.appendChild(elDist);
        elDist.appendChild(element.getOwnerDocument().createTextNode(Float.toString(m_fTargetDist)));
        // The target width
        final Element elWid = element.getOwnerDocument().createElement(XML_TAG_TARGET_WID);
        element.appendChild(elWid);
        elWid.appendChild(element.getOwnerDocument().createTextNode(Float.toString(m_fTargetWidth)));
        // this is the light specific stuff
        internalToXml(element);
    }

    // override this for the camera specific node information
    protected void internalToXml(final Element element) {
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
    // IRtCamera interface implementation                                                                                    //
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void initSampling(final int nSample, final float[] f1dSample, final float[] f1dRandom, final Point2f[] pt2dSample, final Point2f[] pt2dRandom,
                             final Point3f[] pt3dSample, final Point3f[] pt3dRandom) {
        m_ptSample = pt2dSample;
        m_ptRandom = pt2dRandom;
    }

    //-------------------------------------------------------------------------------------------------------------------------
    public void initPicturePlane(final int nXres, final int nYres, final float fPixelAspect) {
        // setup the comutation of spatial locations (in XYZ) of points on the pixel plane
        m_fTargetHeight = (m_fTargetWidth * (float) nYres) / (fPixelAspect * (float) nXres);

        // Setup the pixel dispatch parameters
        m_ptTargetUL.x = m_ptTarget.x + (m_vSide.i * (-0.5f) * m_fTargetWidth) + (m_vUp.i * 0.5f * m_fTargetHeight);
        m_ptTargetUL.y = m_ptTarget.y + (m_vSide.j * (-0.5f) * m_fTargetWidth) + (m_vUp.j * 0.5f * m_fTargetHeight);
        m_ptTargetUL.z = m_ptTarget.z + (m_vSide.k * (-0.5f) * m_fTargetWidth) + (m_vUp.k * 0.5f * m_fTargetHeight);
        m_fIncX = m_fTargetWidth / (float) nXres;
        m_fIncY = -m_fTargetHeight / (float) nYres;
        if ((m_fSampleArea = m_fIncX * m_fIncY) < 0.0f) {
            m_fSampleArea = -m_fSampleArea;
        }
        m_bScreenInit = true;
        if (DEBUG) {
            System.out.println("ACamera.m_ptTarget:   (" + m_ptTarget.x + ',' + m_ptTarget.y + ',' + m_ptTarget.z + ')');
            System.out.println("ACamera.m_ptTargetUL: (" + m_ptTargetUL.x + ',' + m_ptTargetUL.y + ',' + m_ptTargetUL.z + ')');
            System.out.println("ACamera.m_fIncX:       " + m_fIncX);
            System.out.println("ACamera.m_fIncY:       " + m_fIncY);
        }
    }

    //-------------------------------------------------------------------------------------------------------------------------
    public void getRay(final Line3f ray, final RayIntersection rayIntersection, final int nXpixel, final int nYpixel, final int nSample, final int nRandom) {
    }

    //-------------------------------------------------------------------------------------------------------------------------
    protected void getPixelPoint(final Point3f ptPixel, final int nX, final int nY, final int nSample, final int nRandom) {
        float xScale = (float) nX;
        float yScale = (float) nY;
        if (null != m_ptSample) {
            xScale += m_ptSample[nSample].x;
            yScale += m_ptSample[nSample].y;
        }
        if (null != m_ptRandom) {
            xScale += m_ptRandom[nRandom].x;
            yScale += m_ptRandom[nRandom].y;
        }
        xScale *= m_fIncX;
        yScale *= m_fIncY;
        ptPixel.x = m_ptTargetUL.x + (m_vSide.i * xScale) + (m_vUp.i * yScale);
        ptPixel.y = m_ptTargetUL.y + (m_vSide.j * xScale) + (m_vUp.j * yScale);
        ptPixel.z = m_ptTargetUL.z + (m_vSide.k * xScale) + (m_vUp.k * yScale);
    }


}
