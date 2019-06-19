/*
 * FrameLoader.java
 *
 * Created on October 6, 2002, 7:44 PM
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
package cip.render;

import cip.render.raytrace.interfaces.*;
import cip.render.util2d.Point2f;
import cip.render.util3d.Point3f;
import cip.render.utilColour.RGBf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.FileOutputStream;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is a frame loader for an XML description of a frame.  The description includes the frame rendering options plus
 * a description of the frame (lights, cameras, geometry).  The general form of the XML scene description file is a root
 * <tt>RenderFrame</tt> node with rendering specific attributes such as default resolution, oversampling, which camera
 * to use, dimmer, etc.; and has as children nodes the description of the geometry and lights in the scene.  The phylosophy
 * is to specify all of the contained geometry, lights, cameras, materials, etc. as dynamically loaded objects.  By this,
 * we mean that the node describing some object specifies the class that should be instantiated to handle the object,
 * and the contents of the node will be specific to that node.  All dynamic nodes are loaded by the
 * {@link cip.render.DynXmlObjLoader}.  The <tt>FrameLoader</tt> queries the interfaces of the loaded objects
 * to determine object capabilities and how they should be used in the scene.
 * <p>
 * The general form of the XML file for a rendered frame is:
 * <pre>
 *     <font style="color:blue">&lt;?xml version="1.0" encoding="UTF-8"?&gt;</font>
 *
 *     <font style="color:blue">&lt;<b>RenderedFrame</b> <i>frameAttributes</i>&gt;</font>
 *         <font style="color:blue">&lt;<b>LibraryObjects</b>&gt;</font><br>
 *             <font style="color:gray">&lt;!-- These objects are loaded, but not used in the scene unless references by scene objects --&gt;</font>
 *             <font style="color:blue">&lt;<b>DynamicallyLoadedObject</b> class="<font style="color:magenta"><i>classname</i></font>"
 *                                      name="<font style="color:magenta"><i>objectName</i></font>"&gt;</font>
 *                   <font style="color:gray"><b>.</b>
 *                 <i>class specific nodes and attributes</i>
 *                   <b>.</b>
 *             <font style="color:blue">&lt;/<b>DynamicallyLoadedObject</b>&gt;</font>
 *               <b>.</b>
 *               <b>.</b></font>
 *         <font style="color:blue">&lt;/<b>LibraryObjects</b>&gt;</font>
 *
 *         <font style="color:gray">&lt;!-- These objects are the scene objects --&gt;</font>
 *         <font style="color:blue">&lt;<b>DynamicallyLoadedObject</b> class="<font style="color:magenta"><i>classname</i></font>"
 *                                  name="<font style="color:magenta"><i>objectName</i></font>"&gt;</font>
 *               <font style="color:gray"><b>.</b>
 *             <i>class specific nodes and attributes</i>
 *               <b>.</b></font>
 *         <font style="color:blue">&lt;/<b>DynamicallyLoadedObject</b>&gt;</font>
 *         <font style="color:blue">&lt;<b>GeometryByRef</b> name="<font style="color:magenta"><i>geometrytName</i></font>"/&gt;</font>
 *         <font style="color:blue">&lt;<b>LightByRef</b> name="<font style="color:magenta"><i>lightName</i></font>"/&gt;</font>
 *           <font style="color:gray"><b>.</b>
 *           <b>.</b></font>
 *     <font style="color:blue">&lt;/<b>RenderedFrame</b>&gt;</font>
 * </pre>
 * <table border="0" width="90%">
 * <caption style="text-align:left">where:</caption>
 * <tr>
 * <td style="width:5%"></td>
 * <td><table border="1" summary="">
 * <tr>
 * <td><tt>dimmer</tt></td>
 * <td>The light source global dimmer.  This is normally between 0 and 1 and would be specified as:
 * <br><br>
 * <pre>
 *     dimmer="0.5"
 * </pre>
 * The default value is 1.0 if this attribute has not been specified.
 * </td>
 * </tr>
 * <tr>
 * <td><tt>pixelSamples</tt></td>
 * <td><p>The number of sample points per pixel - actually the size of one side of the sampling grid in a
 * a pixel. For example: 1 is a 1x1 sampling grid or 1 sample per pixel, 2 is a 2x2 sampling grid or 4
 * samples per pixel, 3 is a 3x3 sampling grid or 9 samples per pixel.  The default is 1 if
 * <tt>pixelSamples</tt> is not specified.
 * <p>
 * Processing this attribute is optional and dependent on the implementation of
 * {@link IRenderScene} used to render the scene.
 * </td>
 * </tr>
 * <tr>
 * <td><tt>pixelKernel</tt></td>
 * <td><p>The size of the pixel sampling kernel.  This is normally either 1 or 2 where 1 means all the
 * samples in a pixel contribute only to that pixel, while 2 means that a pixel accumulates contributions
 * from all on the nieghboring pixels.
 * <p>
 * Processing this attribute is optional and dependent on the implementation of
 * {@link IRenderScene} used to render the scene.
 * </td>
 * </tr>
 * </table>
 * </td>
 * </tr>
 * </table>
 * <table border="0" width="90%">
 * <caption style="text-align:left">and the elements that can be used within the <tt>RenderFrame</tt> element are:</caption>
 * <tr>
 * <td style="width:5%"></td>
 * <td><table border="1" summary="">
 * <tr>
 * <td><tt>LibraryObjects</tt></td>
 * <td>Within this element specify library geometry, lights, and materials that will be referenced by
 * name when other objects are loaded.  Library objects are not loaded into the scene unless they
 * are referenced within the scene either directly using the <tt>GeometryByRef</tt> or
 * <tt>LightByRef</tt> element tags.
 * </td>
 * </tr>
 * <tr>
 * <td><tt>DynamicallyLoadedObject</tt></td>
 * <td>These are dynamically loaded objects (see {@link cip.render.DynXmlObjLoader}) that are either geometry
 * implement the {@link cip.render.IDynXmlObject} interface for loading from an XML file, the
 * {@link cip.render.INamedObject} for object naming and referencing, and at least one of the
 * following interfaces so that it can do something in the context of a ray tracer:
 * <br><br>
 * Implements {@link cip.render.raytrace.interfaces.IRtBackground} so that it can act as a background.
 * <br><br>
 * Implements {@link cip.render.raytrace.interfaces.IRtCamera} so that it can act as a camera.
 * <br><br>
 * Implements {@link cip.render.raytrace.interfaces.IRtGeometry} so that it can act as a geometry.
 * <br><br>
 * Implements {@link cip.render.raytrace.interfaces.IRtLight} so that it can act as a light source.
 * <br><br>
 * Implements {@link cip.render.raytrace.interfaces.IRtMaterial} so that it can act as a material.
 * </td>
 * </tr>
 * <tr>
 * <td><tt>GeometryByRef</tt></td>
 * <td>A reference to a library or previously loaded geometry.  This will establish a reference to
 * the first instance of that geometry that was loaded - whether as a library object or within
 * the scene.
 * </td>
 * </tr>
 * <tr>
 * <td><tt>LightByRef</tt></td>
 * <td>A reference to a library or previously loaded light.  This will establish a reference to
 * the first instance of that light that was loaded - whether as a library object or within
 * the scene.
 * </td>
 * </tr>
 * </table>
 * </td>
 * </tr>
 * </table>
 *
 * @author royster.hall@gmail.com
 * @version 1.0
 * @see cip.render.DynXmlObjLoader
 * @since 1.0
 */
public class FrameLoader {
    static final Logger logger = Logger.getLogger(FrameLoader.class.getName());
    static boolean loggingWarning = logger.isLoggable(Level.WARNING);

    private static final String XML_TAG_ROOT = "RenderedFrame";
    private static final String XML_TAG_LIB_OBJ = "LibraryObjects";
    private static final String XML_TAG_GEOMETRY_REF = "GeometryByRef";
    private static final String XML_TAG_LIGHT_REF = "LightByRef";
    private static final String XML_TAG_MATERIAL_REF = "MaterialByRef";

    private static final String XML_ATTR_DIMMER = "dimmer";
    private static final String XML_ATTR_PIXELSAMPLES = "pixelSamples";
    private static final String XML_ATTR_PIXELKERNEL = "pixelKernel";

    // instance fields
    // The reference list of all loaded and named objects
    protected LinkedList<IDynXmlObject> m_refObjList = new LinkedList<>();
    // the camera
    protected IRtCamera m_defCamera = new cip.render.raytrace.camera.PinHole();
    protected IRtCamera m_camera = null;
    // the background
    protected IRtBackground m_defBackground = new cip.render.raytrace.background.ConstantColour(new RGBf(0.0f, 0.0f, 0.0f));
    protected IRtBackground m_background = null;
    // The top level object/group list
    protected LinkedList<IRtGeometry> m_objectList = new LinkedList<>();
    // The flattened geometry list
    protected LinkedList<IRtGeometry> m_objectFlatList = new LinkedList<>();
    // the flattened light list
    protected LinkedList<IRtLight> m_lightList = new LinkedList<>();
    // the dimmer
    protected float m_fDimmer = 1.0f;
    // pixel sampling
    protected int m_nSamplePerPixel = 1;
    protected int m_nSampleKernel = 1;

    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Creates a new instance of <tt>FrameLoader</tt> which is initialized to the described scene.
     *
     * @param strXmlFile The name of the scene description file.  See class notes for format information.
     * @throws Exception An exception is thrown if the scene could not be loaded.
     */
    public FrameLoader(final String strXmlFile) throws Exception {
        // load the XML file into a DOM Document, then start parsing.  The DOM document should be a RenderedFrame node
        //  which includes rendering attributes, a camera, light(s), and geometry.
        final javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        final javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
        final org.w3c.dom.Document domDoc = db.parse(strXmlFile);
        final Element domDocEl = domDoc.getDocumentElement();

        // check that the root node is a RenderedFrame node
        if (!domDocEl.getTagName().equalsIgnoreCase(XML_TAG_ROOT)) {
            throw new DynXmlObjParseException("The XML document is not a RenderedFrame document.");
        }
        // parse the attributes for render
        final String strDimmer = domDocEl.getAttribute(XML_ATTR_DIMMER);
        if (!strDimmer.equals("")) m_fDimmer = Float.parseFloat(strDimmer.trim());
        final String strSamplesPerPixel = domDocEl.getAttribute(XML_ATTR_PIXELSAMPLES);
        if (!strSamplesPerPixel.equals("")) m_nSamplePerPixel = Integer.parseInt(strSamplesPerPixel.trim());
        final String strSamplesKernel = domDocEl.getAttribute(XML_ATTR_PIXELKERNEL);
        if (!strSamplesKernel.equals("")) m_nSampleKernel = Integer.parseInt(strSamplesKernel.trim());

        // loop through the nodes in the RenderedFrame and build the frame description
        Node domNode = domDocEl.getFirstChild();
        while (null != domNode) {
            if (domNode instanceof Element) {
                final Element domEl = (Element) domNode;

                if (domEl.getTagName().equalsIgnoreCase(DynXmlObjLoader.XML_TAG)) {
                    // this is a dynamically loaded scene object -- we don't know what it is yet.  we need to load it and
                    //  then ask it to find out.
                    final Object obj = DynXmlObjLoader.LoadObject(domEl, m_refObjList);
                    boolean bCanRender = false;
                    // now that we have loaded it, lets find out what it is
                    if (obj instanceof IRtGeometry) {
                        bCanRender = true;
                        m_objectList.add((IRtGeometry) obj);
                        m_objectFlatList.add((IRtGeometry) obj);
                    }
                    if (obj instanceof IRtLight) {
                        bCanRender = true;
                        m_lightList.add((IRtLight) obj);
                    }

                    if (obj instanceof IRtBackground) {
                        bCanRender = true;
                        if (null == m_background) {
                            m_background = (IRtBackground) obj;
                        }
                    }

                    if (obj instanceof IRtCamera) {
                        bCanRender = true;
                        if (null == m_camera) {
                            m_camera = (IRtCamera) obj;
                        }
                    }

                    if (!bCanRender) {
                        if (loggingWarning) {
                            logger.warning(String.format("Loaded object %s cannot be rendered.", obj.getClass().getName()));
                        }
                    }
                } else if (domEl.getTagName().equalsIgnoreCase(XML_TAG_GEOMETRY_REF)) {
                    // look through the ref objects for a geometry of this name and add it to the scene
                    int iObj = -1;
                    final String strName = domEl.getAttribute(DynXmlObjLoader.XML_ATTR_NAME);
                    if (!strName.equals("") && (null != m_refObjList)) {
                        for (iObj = 0; iObj < m_refObjList.size(); iObj++) {
                            final Object obj = m_refObjList.get(iObj);
                            if ((obj instanceof IRtGeometry) && ((INamedObject) obj).getName().equals(strName)) {
                                m_objectList.add((IRtGeometry) obj);
                                m_objectFlatList.add((IRtGeometry) obj);
                                break;
                            }
                        }
                    }
                    if ((iObj < 0) || (iObj >= m_refObjList.size())) {
                        if (loggingWarning) {
                            logger.warning(String.format("Referenced geometry \"%s\" cannot be resolved.", strName));
                        }
                    }

                } else if (domEl.getTagName().equalsIgnoreCase(XML_TAG_LIGHT_REF)) {
                    // look through the ref objects for a light of this name and add it to the scene
                    int iObj = -1;
                    final String strName = domEl.getAttribute(DynXmlObjLoader.XML_ATTR_NAME);
                    if (!strName.equals("") && (null != m_refObjList)) {
                        for (iObj = 0; iObj < m_refObjList.size(); iObj++) {
                            final Object obj = m_refObjList.get(iObj);
                            if ((obj instanceof IRtLight) && ((INamedObject) obj).getName().equals(strName)) {
                                m_lightList.add((IRtLight) obj);
                                break;
                            }
                        }
                    }
                    if ((iObj < 0) || (iObj >= m_refObjList.size())) {
                        if (loggingWarning) {
                            logger.warning(String.format("Warning: referenced light \"" + strName + "\" cannot be resolved."));
                        }
                    }

                } else if (domEl.getTagName().equalsIgnoreCase(XML_TAG_LIB_OBJ)) {
                    // This is the start of a section of dynamically loaded reference objects.
                    loadRefObjects(domEl);
                }
            }
            domNode = domNode.getNextSibling();
        }
    }

    private void loadRefObjects(final Element refObjElement) throws DynXmlObjParseException {
        try {
            Node domNode = refObjElement.getFirstChild();
            while (null != domNode) {
                if (domNode instanceof Element) {
                    final Element element = (Element) domNode;
                    if (element.getTagName().equalsIgnoreCase(DynXmlObjLoader.XML_TAG)) {
                        // if this should be the contained geometry - note, we can load only one, and the last
                        //  one we encounter is the one that is saved.
                        final Object obj = DynXmlObjLoader.LoadObject(element, m_refObjList);
                    } else {
                        throw new DynXmlObjParseException("Unrecognized Reference Object element <" +
                                element.getTagName() + ">");
                    }
                }
                domNode = domNode.getNextSibling();
            }
        } catch (final Throwable t) {
            if (t instanceof DynXmlObjParseException) {
                throw (DynXmlObjParseException) t;
            } else {
                throw new DynXmlObjParseException("Reference Object parse exception", t);
            }
        }
    }

    /**
     * A method that can be called by geometry and texture loaders to try to parse a material or a reference to a material.
     *
     * @param element (not null, readonly) The XML element this methods will try to parse as a material.
     * @param refObjectList (nullable, readonly) The reference object list.
     * @param strObjType (not null) The calling object type, to be used in error messages.
     * @param strObjName (not null) The calling object name in the scene description file, to be used in error messages.
     * @return Returns <tt>null</tt> if this element is NOT a dynamically loaded material or material reference.
     * @throws DynXmlObjParseException Thrown if an in-line material has a parsing error or if a material reference cannot be
     * found in the reference object list.
     */
    public static IRtMaterial tryParseMaterial(@NotNull final Element element, final @Nullable LinkedList refObjectList,
                                               @NotNull final String strObjType, @NotNull final String strObjName)
            throws DynXmlObjParseException {
        if (element.getTagName().equalsIgnoreCase(DynXmlObjLoader.XML_TAG)) {
            // Should be a material - that is the only dynamically loaded object that can be used
            final Object obj = DynXmlObjLoader.LoadObject(element, refObjectList);
            if (obj instanceof IRtMaterial) {
                return (IRtMaterial) obj;
            } else {
                throw new DynXmlObjParseException(String.format("%s %s material could not be parsed",strObjType, strObjName));
            }
        } else if (element.getTagName().equalsIgnoreCase(XML_TAG_MATERIAL_REF)) {
            // a material reference
            final String strName = element.getAttribute(DynXmlObjLoader.XML_ATTR_NAME);
            if (!strName.equals("") && (null != refObjectList)) {
                for (final Object obj : refObjectList) {
                    if ((obj instanceof IRtMaterial) && ((INamedObject) obj).getName().equals(strName)) {
                        return (IRtMaterial) obj;
                    }
                }
            }
            throw new DynXmlObjParseException(String.format("Referenced material \"%s\" was not found.",strName));
        }
        return null;
    }
    /**
     * Initialize all the loaded objects in the scene for sampling or oversampling.  This function should be called between
     * the time the frame is loaded and when you start to get the loaded elements from the frame.  It loops through all the
     * loaded elements and calls the initSampling function for the element,
     *
     * @param nSample    (readonly) The number of sub-samples (over-samples) per pixel in x and y; i.e. 3 means a 3x3 sampling grid.
     * @param f1dSample  The 1d sample displacement array.  The length of this array will be equal to the number of sub-samples
     *                   per pixel.  This array is for oversampling linear phenomena.
     * @param f1dRandom  The 1d 'jitter' array.  No assumptions should be made about the length of this array.
     * @param pt2dSample The 2d sample displacement array.  The length of this array will be equal to the number of sub-samples
     *                   per pixel.  This array is for oversampling area phenomena.
     * @param pt2dRandom The 2d 'jitter' array.  No assumptions should be made about the length of this array.
     * @param pt3dSample The 3d sample displacement array.  The length of this array will be equal to the number of sub-samples
     *                   per pixel.  This array is for oversampling volume phenomena.
     * @param pt3dRandom The 3d 'jitter' array.  No assumptions should be made about the length of this array.
     */
    public void initSampling(final int nSample, final float[] f1dSample, final float[] f1dRandom, final Point2f[] pt2dSample,
                             final Point2f[] pt2dRandom, final Point3f[] pt3dSample, final Point3f[] pt3dRandom) {
        for (final Object obj : m_refObjList) {
            if (obj instanceof IRtGeometry) {
                ((IRtGeometry) obj).initSampling(nSample, f1dSample, f1dRandom, pt2dSample, pt2dRandom, pt3dSample, pt3dRandom);
            }

            if (obj instanceof IRtLight) {
                ((IRtLight) obj).initSampling(nSample, f1dSample, f1dRandom, pt2dSample, pt2dRandom, pt3dSample, pt3dRandom);
            }

            if (obj instanceof IRtMaterial) {
                ((IRtMaterial) obj).initSampling(nSample, f1dSample, f1dRandom, pt2dSample, pt2dRandom, pt3dSample, pt3dRandom);
            }

            if (obj instanceof IRtCamera) {
                ((IRtCamera) obj).initSampling(nSample, f1dSample, f1dRandom, pt2dSample, pt2dRandom, pt3dSample, pt3dRandom);
            }
        }
    }

    /**
     * Return the camera.  If a camera is not specified in the scene description file, this defaults to a front view through
     * a {@link cip.render.raytrace.camera.PinHole} camera.  If more than one camera is specified in the scene
     * description, the first encountered camera is the one that is returned here.
     *
     * @return Returns the interface to the camera.
     */
    public IRtCamera getCamera() {
        return (null != m_camera) ? m_camera : m_defCamera;
    }

    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Return the background.  If a background is not specified in the scene description file, this defaults to a black
     * {@link cip.render.raytrace.background.ConstantColour} background.  If more than one background is specified in the scene
     * description, the first encountered background is the one that is returned here.
     *
     * @return Returns the interface to the background.
     */
    public IRtBackground getBackground() {
        return (null != m_background) ? m_background : m_defBackground;
    }

    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Return a linked list of all the first-level geometries in the scene.  Any groups are preserved as groups, so the
     * hierarchy of the scene description is maintained.
     *
     * @return Returns the hierarchical list of geometries in the scene.
     */
    public LinkedList getGeometryHierarchy() {
        return m_objectList;
    }

    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Returns a flatted list of geometries in the scene.  All groups are expanded and the transformations accumulated
     * and applied to individual geometry.  While each geometry returned will have a sngle leaf node (the actual geometry)
     * and the transformations will be aggregated into a single {@link cip.render.raytrace.geometry.XfmGeometry} if possible.
     *
     * @return Returns the flatted list of geometries in the scene.
     */
    public LinkedList getGeometryFlat() {
        return m_objectFlatList;
    }

    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Returns a flattened list of lights in the scene.
     *
     * @return Returns the flattened list of lights in the scene.
     */
    public LinkedList getLights() {
        return m_lightList;
    }

    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Returns the global dimmer.  The global dimmer defaults to 1.0 if not specified in the scene description.
     *
     * @return Returns the global dimmer.
     */
    public float getDimmer() {
        return m_fDimmer;
    }

    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Returns the samples-per-pixel that should be used for rendering.  The default is 1 sample per pixel if not
     * specified in the scene description. The interpretation is left to the rendering implementation using the
     * loader, but, generally: a value of 1 means 1 sample per pixel; a value or 2 means a 2x2 sampling grid or
     * 4 samples per pixel; a value of 3 menas a 3x3 sampling grid or 9 samples per pivel, etc.
     *
     * @return Returns the target samples per pixel.
     */
    public int getSamplesPerPixel() {
        return m_nSamplePerPixel;
    }

    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Returns the size of the filter kernel.  The default is 1 pixel if not
     * specified in the scene description.  A 1 pixel kernel means only the samples within the pixel are
     * used to compute the pixel colour.  A 2 pixle kernel generally means the sampling  extends into the adjacent pixels using
     * a wieghted sampling - though this is dependent on renderer implementation.
     *
     * @return Returns the sample kernel.
     */
    public int getSampleKernel() {
        return m_nSampleKernel;
    }

    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * This main is for the express purpose of testing the loader.  It reads an XML scene description then writes it
     * back out from the loaded description.  This tests both the scene loading at a high level, and the
     * {@link cip.render.IDynXmlObject} implementation at an object level.  In the current implementation of
     * the test, there is no automatic comparison of the input and output for equivalence.  This must be done
     * by visual inspection of the output files.
     * <p>
     * Note: What comes out will probably not be a literal copy of what went in.  There is no processing on output
     * to consolidate multiply used objects into reference objects, and, no reference objects are written out.  The
     * ONLY thing written out is a fully specified  description of the environment as if all objects in the environment
     * are unique instances.
     * <p>
     * <b>Usage:</b><br>
     *
     * @param args The arguments
     */
    public static void main(final String[] args) {
        String strRenderSceneDesc = "c:/users/CIP/src/java/cip/CSE581/core/TestEnv.xml";
        final String strTestOutHierarchy = "TestHierarchy.xml";
        final String strTestOutFlat = "TestFlat.xml";

        if (args.length >= 1) {
            strRenderSceneDesc = args[0];
        }
        if (args.length >= 2) {
            strRenderSceneDesc = args[1];
        }
        if (args.length >= 3) {
            strRenderSceneDesc = args[2];
        }

        FrameLoader frameLoader = null;
        try {
            frameLoader = new FrameLoader(strRenderSceneDesc);
        } catch (final Exception e) {
            System.out.println("Cannot load the scene file: " + strRenderSceneDesc);
            e.printStackTrace();
            System.exit(0);
        }

        System.out.println();
        System.out.println("Scene file: " + strRenderSceneDesc + " has been loaded");
        System.out.println();

        //---------------------------------------------------------------------------------------------------------------------
        //  The hierarchy representation
        //---------------------------------------------------------------------------------------------------------------------
        // now create a DOM to write the objects into 
        try {
            final LinkedList listGeom = frameLoader.getGeometryHierarchy();
            final LinkedList listLight = frameLoader.getLights();
            final javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            final javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
            final org.w3c.dom.Document doc = db.newDocument();
            // create and load the root of the document
            final Element docEl = doc.createElement(XML_TAG_ROOT);
            doc.appendChild(docEl);
            // write the camera
            ((IDynXmlObject) (frameLoader.getCamera())).toChildXmlElement(docEl);
            // write the backgrounnd
            ((IDynXmlObject) (frameLoader.getBackground())).toChildXmlElement(docEl);
            // write the light list
            for (Object aListLight : listLight) {
                ((IDynXmlObject) aListLight).toChildXmlElement(docEl);
            }
            // write the object list
            for (Object aListGeom : listGeom) {
                ((IDynXmlObject) aListGeom).toChildXmlElement(docEl);
            }

            final TransformerFactory transformerFactory = TransformerFactory.newInstance();
            final Transformer serializer = transformerFactory.newTransformer();
            serializer.transform(new DOMSource(doc), new StreamResult(new FileOutputStream(strTestOutHierarchy)));
            System.out.println();
            System.out.println("Hierarchical rep file: " + strTestOutHierarchy + " has been written");
            System.out.println();
        } catch (final Throwable t) {
            System.out.println("ERROR: creating and writing the hierarchy XML");
            t.printStackTrace();
        }


    }

}
