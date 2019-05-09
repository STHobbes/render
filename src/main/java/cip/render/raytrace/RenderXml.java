/*
 * RenderXmlHierarchy.java
 *
 * Created on October 3, 2002, 9:21 AM
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
package cip.render.raytrace;

import cip.render.DynXmlObjParseException;
import cip.render.FrameLoader;
import cip.render.IRenderScene;
import cip.render.raytrace.interfaces.IRtBackground;
import cip.render.raytrace.interfaces.IRtCamera;
import cip.render.raytrace.interfaces.IRtGeometry;
import cip.render.raytrace.interfaces.IRtLight;
import cip.render.util3d.Line3f;
import cip.render.utilColour.RGBf;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.LinkedList;

/**
 * This is a ray tracer that loads the scene to be rendered from an XML scene description file, and renders that scene using a
 * single sample per pixel. The XML scene description file is
 * loaded by {@link FrameLoader}. Refer to the {@link FrameLoader} documentation for XML scene description file format
 * and conventions. The entire scene description is dynamically loaded (i.e. this renderer has no knowledge of implemented
 * geometries, lights, materials, textures, etc. comprising an environment. It only knows about interfaces, so, anything that
 * implements the rendering interfaces and is in the classpath can be loaded and rendered). Dynamic loading makes it easy to
 * add new geometry, lights, materials, textures, backgrounds , and illumination models simply by making sure the
 * classpath includes the location of the implementation of the new element, and asking to load it in
 * the XML file.
 * <p>
 * <b>Usage:</b>
 * <pre>
 *     RenderWindow -r cip.render.raytrace.RenderXml -d <i>&lt;sceneDescFile&gt;</i>
 * </pre>
 * <table border="0" width="90%">
 * <caption style="text-align:left">where:</caption>
 * <tr>
 * <td style="width:5%"></td>
 * <td><table border="1" summary="">
 * <tr>
 * <td style="width:5%">-d</td>
 * <td>Specify the scene description file, <i>sceneDescFile</i>, that should be loaded for rendering.  This string is the
 * name of an XML scene description file that can be parsed by {@link FrameLoader}. NOTE: a scene description file is required.
 * </td>
 * </tr>
 * </table>
 * </td>
 * </tr>
 * </table>
 * <b>Note:</b>
 * <p>
 * This implementation anticipates implementations with oversampling and jitter (future topics in the course); so, the
 * <tt>dispatchPixel</tt> and <tt>setSampleColor</tt> methods of <tt>Assignment4</tt> have been renamed <tt>dispatchSample</tt>
 * and <tt>setSample</tt>. Additionally, they carry a couple arguments that are unused for a single sample per pixel. This
 * lets us use inheritance through extensions of this renderer that include oversampling and distributed ray
 * tracing.
 * </p>
 *
 * @author royster.hall@gmail.com
 * @version 1.0
 * @see FrameLoader
 * @see cip.render.RenderWindow
 * @since fall 2002
 */
public class RenderXml implements IRenderScene {

    //-------------------------------------------------------------------------------------------------------------------------
    // RenderPixel
    //-------------------------------------------------------------------------------------------------------------------------
    /**
     * This is the implementation of a pixel renderer intended to be run in a rendering thread.  The pixel renderer asks the
     * parent renderer pixel dispatcher (really a sample dispatcher) for a pixel (sample); gets its colour, and gives it to
     * the colour collector.  Rendering threads are created when the rendering of an image starts, and finish when there are
     * no more pixels left to render (the dispatcher does not setup a new sample when called).
     */
    class RenderPixel implements Runnable {
        public RenderXml m_parent;
        public int m_nX;
        public int m_nY;
        public Line3f m_ray = new Line3f();
        public RayIntersection m_intersection = new RayIntersection();

        /**
         * Instantiate a pixel render to be run in a rendering thread.
         * @param parent (RenderXml) The parent renderer
         */
        RenderPixel(final RenderXml parent) {
            m_parent = parent;
        }

        /**
         *
         */
        @Override
        public void run() {
            // this is the actual rendering part.
            // render pixels while there are pixels to render
            while (m_parent.dispatchPixel(this)) {
                final Color clr = m_parent.getSampleColor(m_ray, m_intersection);
                m_parent.setSampleColor(m_nX, m_nY, clr);
            }

            // let the main thread know we are done
            synchronized (m_parent.m_threadLock) {
                m_parent.m_threadCt--;
                if (m_parent.m_threadCt <= 0) {
                    // this is the last thread still running - the image is
                    //  done - release the main thread
                    m_parent.m_threadLock.notify();
                }
            }
        }
    }

    //-------------------------------------------------------------------------------------------------------------------------
    // RenderXmlHierarchy
    //-------------------------------------------------------------------------------------------------------------------------
    final int m_nMaxRecursions = 6;

    protected boolean m_bNewScene = true;
    // the camera for viewing the scene
    private IRtCamera m_camera = null;
    // the background for the scene
    private IRtBackground m_bkg = null;
    // The geometry array
    private IRtGeometry[] m_rtObjects = null;
    // the light array
    private IRtLight[] m_rtLights = null;

    // The rendering window description and where we are in dispatching pixels
    int m_nXmin;                            // the minimum X
    int m_nYmin;                            // the minimum Y
    int m_nXcur;                            // the current pixel X (will be dispatched next)
    int m_nYcur;                            // the current pixel Y (will be dispatched next)
    int m_nXmax;                            // the maximum X
    int m_nYmax;                            // the maximum Y
    Graphics m_gc;                          // the graphics we are drawing into

    // the thread synchronizer for when the image is done
    byte[] m_threadLock = new byte[0];
    int m_threadCt = 0;

    // This is the image that pixels are drawn into as computation completes
    int m_pixArrayWidth = 0;
    int m_pixArrayHeight = 0;
    BufferedImage m_bi = null;

    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Creates a new instance of <tt>RenderXml</tt>
     */
    public RenderXml() {
    }

    //-------------------------------------------------------------------------------------------------------------------------
    //  Here we do the work of getting the pixel colour.  The RayIntersection object is a cached object so that
    //  we could use this function in a multi-threaded environment without the need to create a new intersection
    //  object at every invocation.
    Color getSampleColor(final Line3f ray, final RayIntersection intersection) {
        boolean bIntersectObj = false;
        Color clr;
        final RGBf rgb = intersection.borrowRGB();

        try {
            for (IRtGeometry m_rtObject : m_rtObjects) {
                if (m_rtObject.getRayIntersection(intersection, ray, false, 0, 0)) {
                    bIntersectObj = true;
                }
            }
            if (bIntersectObj) {
                intersection.m_mtl.getColor(rgb, intersection, m_rtLights, m_rtObjects, m_bkg, m_nMaxRecursions, 0, 0);
                rgb.clamp();
            } else {
                m_bkg.getColor(rgb, ray, null);
            }
            clr = new Color(rgb.r, rgb.g, rgb.b);
        } catch (final Throwable t) {
            // something bad happened - color code this pixel yellow
            t.printStackTrace();
            clr = Color.YELLOW;
        } finally {
            intersection.returnRGB(rgb);
        }

        return clr;

    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IRenderScene interface implementation                                                                                 //
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void loadScene(final String strSceneDesc) throws Exception {
        try {
            final FrameLoader frameLoader = new FrameLoader(strSceneDesc);
            final LinkedList objectList = frameLoader.getGeometryHierarchy();
            final LinkedList lightList = frameLoader.getLights();

            if (objectList.isEmpty()) {
                throw new DynXmlObjParseException("No geometry to be rendered was loaded.");
            }
            if (lightList.isEmpty()) {
                throw new DynXmlObjParseException("No lights illuminating the scene were loaded.");
            }
            if (null == frameLoader.getBackground()) {
                throw new DynXmlObjParseException("No background was loaded.");
            }
            if (null == frameLoader.getCamera()) {
                throw new DynXmlObjParseException("No camera was loaded for rendering.");
            }

            m_bkg = frameLoader.getBackground();
            m_camera = frameLoader.getCamera();

            // Now that we've completely parsed the scene definition file, transfer the geometry and light lists into
            //  arrays for optimal performance when looping through the lists.

            // setup the geometry list
            m_rtObjects = new IRtGeometry[objectList.size()];
            for (int iObj = 0; iObj < objectList.size(); iObj++) {
                m_rtObjects[iObj] = (IRtGeometry) (objectList.get(iObj));
            }

            // setup the light list
            m_rtLights = new IRtLight[lightList.size()];
            for (int iLgt = 0; iLgt < lightList.size(); iLgt++) {
                m_rtLights[iLgt] = (IRtLight) (lightList.get(iLgt));
                m_rtLights[iLgt].setDimmer(frameLoader.getDimmer());
            }

            m_bNewScene = true;

        } catch (final Throwable t) {
            if (t instanceof DynXmlObjParseException) {
                throw (DynXmlObjParseException) t;
            } else {
                throw new DynXmlObjParseException("Error parsing <" + strSceneDesc + "> into an XML DOM", t);
            }
        }
    }

    @Override
    public String getTitle() {
        return null;
    }

    //-------------------------------------------------------------------------------------------------------------------------
    public void renderImage(final Image image) {
        if ((null != m_bi) &&
                (image.getWidth(null) == m_bi.getWidth(null)) && (image.getHeight(null) == m_bi.getHeight(null))) {
            // copying pre-draw image into
//          System.out.println("IRenderScene.renderImage -- copy image");       
            image.getGraphics().drawImage(m_bi, 0, 0, m_pixArrayWidth, m_pixArrayHeight, null);
        } else {
//          System.out.println("IRenderScene.renderImage -- image mismatch");
        }
    }

    //-------------------------------------------------------------------------------------------------------------------------
    public void renderScene(final Component component, final Graphics gc) {
        final Dimension dimScreen = component.getSize();
        // get the bounds of the hither plane (picture plane)
        final Rectangle rectRender = gc.getClipBounds();
        // create the objects to represent the ray from the eye through the pixel on the pixel plane
        //  and to represent the intersection at each pixel.  These objects are created outside the
        //  pixel loop for increased performance.
        final Line3f ray = new Line3f();
        m_camera.initPicturePlane(dimScreen.width, dimScreen.height, 1.0f);

        if ((dimScreen.width != m_pixArrayWidth) || (dimScreen.height != m_pixArrayHeight)) {
            // allocate and initialize the pixel array and the image buffer
            m_pixArrayWidth = dimScreen.width;
            m_pixArrayHeight = dimScreen.height;
            m_bi = new BufferedImage(m_pixArrayWidth, m_pixArrayHeight, BufferedImage.TYPE_INT_ARGB);
            m_bNewScene = true;
        }

        if (m_bNewScene) {
            m_nXmin = 0;
            m_nYmin = 0;
            m_nXmax = m_pixArrayWidth;
            m_nYmax = m_pixArrayHeight;
        } else {
            m_nXmin = rectRender.x;
            m_nYmin = rectRender.y;
            m_nXmax = rectRender.x + rectRender.width;
            m_nYmax = rectRender.y + rectRender.height;
        }
        m_gc = gc;
        m_nXcur = m_nXmin;
        m_nYcur = m_nYmin;

        if (m_bNewScene) {
            // now start the threads
            final int nProcessors = Runtime.getRuntime().availableProcessors();
            int nThreads = nProcessors;     // start a rendering thread for each processor
//            nThreads = nProcessors;
            nThreads = 1;
            System.out.println("Starting " + nThreads + " threads on " + nProcessors + " processors");

            // The deal here is we start the threads and keep a count of the running threads.  This count is
            //  protected by the thread lock.  Once the threads are started, they run till there are no
            //  more pixels, acquire the lock, and decrement the thread count.  If the count drops to 0, then
            //  the last thread (the one that dropped the count to 0) notifies the thread lock so this thread
            //  catch (exit the redraw function.
            //
            // This thread (the one we are in right now) needs to stop here because we lose the gc after it
            //  returns.  There are alternate ways to implement this -- this thread could also be computung pixels.
            //  I find it hard (as in needlessly confusing - especially when reviewing code later) to implement the
            //  multiple use for this thread, so I just block this thread while the rendering threads do their stuff.
            synchronized (m_threadLock) {
                m_threadCt = 0;
                for (int iThread = 0; iThread < nThreads; iThread++) {
                    m_threadCt++;
                    new Thread(new RenderPixel(this)).start();
                }
                try {
                    m_threadLock.wait();
                } catch (final Throwable t) {
                    // the wait was interrupted, probably because the window closed
                }
            }

            m_bNewScene = false;
        } else {
            gc.drawImage(m_bi, m_nXmin, m_nYmin, m_nXmax, m_nYmax, m_nXmin, m_nYmin, m_nXmax, m_nYmax, null);
        }
    }

    /**
     * Dispatch an un-rendered pixel in the image whenever this method is called.
     *
     * @param renderThread (RenderPixel, modified) The pixel rendering thread to be loaded with a new pixel to render.
     * @return <tt>true</tt> if a new pixel was dispatched, <tt>false</tt> if all pixels have been dispatch and there are no
     * un-rendered pixels left in the image.
     */
    synchronized boolean dispatchPixel(final RenderPixel renderThread) {
        // this function is synchronized because we want to limit the access to the current pixel position to a single
        //  thread.  The thread comes in, pixel positions are computed and the pixel indecies incremented and then
        //  the function returns.
        while (m_nYcur < m_nYmax) {
            try {
                // setup the ray and intersection for this pixel
                m_camera.getRay(renderThread.m_ray, renderThread.m_intersection, m_nXcur, m_nYcur, 0, 0);
                renderThread.m_nX = m_nXcur;
                renderThread.m_nY = m_nYcur;
                m_nXcur++;
                if (m_nXcur >= m_nXmax) {
                    m_nXcur = m_nXmin;
                    m_nYcur++;
                }
                return true;
            } catch (final Throwable t) {
                // something bad happened - color code this pixel yellow
                t.printStackTrace();
                setSampleColor(m_nXcur, m_nYcur, Color.YELLOW);
                m_nXcur++;
                if (m_nXcur >= m_nXmax) {
                    m_nXcur = m_nXmin;
                    m_nYcur++;
                }
            }
        }
        System.out.println("no more pixels for this thread: " + renderThread.toString());
        return false;
    }

    /**
     * Set the color for a pixel in the image.
     * @param nX (int) The X location of the pixel in the image.
     * @param nY (int) The Y location of the pixel in the image.
     * @param clr (Color, readonly) The color of the pixel.
     */
    void setSampleColor(final int nX, final int nY, final Color clr) {
        // this function is synchronized on m_gc so access to the gc is thread safe
        synchronized (m_gc) {
            m_bi.setRGB(nX, nY, clr.getRGB());
            m_gc.setColor(clr);
            m_gc.drawRect(nX, nY, 0, 0);
        }
    }

}
