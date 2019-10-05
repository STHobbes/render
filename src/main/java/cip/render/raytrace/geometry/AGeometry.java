/*
 * AGeometry.java
 *
 * Created on October 10, 2002, 6:16 PM
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

package cip.render.raytrace.geometry;

import cip.render.*;
import cip.render.raytrace.RayIntersection;
import cip.render.raytrace.interfaces.IRtGeometry;
import cip.render.raytrace.interfaces.IRtLight;
import cip.render.raytrace.interfaces.IRtMaterial;
import cip.render.raytrace.material.Blinn;
import cip.render.util.AngleF;
import cip.render.util2d.Point2f;
import cip.render.util3d.*;
import cip.render.utilColour.RGBf;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.LinkedList;
import java.util.StringTokenizer;

/**
 * This is an abstract class for a ray tracing geometry.  It offers an implementation of the most common elements
 * of a ray tracing geometry and some functions to help in object parsing.
 *
 * @author royster.hall@gmail.com
 * @version 1.0
 * @since 1.0
 */
public abstract class AGeometry implements IDynXmlObject, INamedObject, IRtGeometry {
    class Face {
        final Plane3f m_pln = new Plane3f();
        final IRtMaterial m_mtl;

        public Face(final float fA, final float fB, final float fC, final float fD, final IRtMaterial mtl)
                throws ZeroLengthVectorException {
            m_pln.setValue(fA, fB, fC, fD).normalize();
            m_mtl = mtl;
        }
    }

    protected static final String XML_TAG_REF_NAME_ATTR = "name";
    protected static final String XML_TAG_GEOMETRY_REF = "GeometryByRef";

    protected static final String DEFAULT_NAME = "<unspecified>";

    protected static final String XML_TAG_FACE = "face";
    protected static final String XML_ATTR_FACE_PLANE = "plane";

    static final IRtMaterial DEFAULT_MATERIAL = new Blinn("default", new RGBf(0.0f, 1.0f, 0.0f),
            false, new AngleF(AngleF.DEGREES, 45.0f));

    /**
     * The name for this geometry instance which is used when referencing a geometry by name
     */
    protected String m_strName = DEFAULT_NAME;  // the object instance name

    /**
     *
     */
    protected String m_strType;

    protected IRtMaterial m_mtl = DEFAULT_MATERIAL;  // the primary object material

    /**
     * Creates a new instance of <tt>AGeometry</tt>.
     */
    AGeometry() {
        m_strType = this.getClass().getName();
    }

    //------------------------------------------------------------------------------------------------------------------------------
    // Accessor/Mutator functions
    public IRtMaterial getMaterial() {
        return m_mtl;
    }

    public void setMaterial(final IRtMaterial mtl) {
        m_mtl = mtl;
    }

    /**
     * @param element
     * @param refObjectList
     * @return
     * @throws DynXmlObjParseException
     */
    Face tryParseFace(@NotNull Element element, final LinkedList<INamedObject> refObjectList) throws DynXmlObjParseException {
        if (element.getTagName().equalsIgnoreCase(XML_TAG_FACE)) {
            // a face element - get the plane, and there may be a material for the face.
            final String strPlane = element.getAttribute(XML_ATTR_FACE_PLANE);
            final StringTokenizer tokens = new StringTokenizer(strPlane, ",");
            if (tokens.countTokens() != 4) {
                throw new IllegalArgumentException("face specification must be in the form <face plane=\"A,B,C,D\">");
            }
            final float fA = Float.parseFloat(tokens.nextToken().trim());
            final float fB = Float.parseFloat(tokens.nextToken().trim());
            final float fC = Float.parseFloat(tokens.nextToken().trim());
            final float fD = Float.parseFloat(tokens.nextToken().trim());
            IRtMaterial mtlFace = null;
            Node mtlNode = element.getFirstChild();
            while (null != mtlNode) {
                if (mtlNode instanceof Element) {
                    if (null != (mtlFace = FrameLoader.tryParseMaterial((Element) mtlNode, refObjectList, getType(), m_strName))) {
                        break;
                    }
                }
                mtlNode = mtlNode.getNextSibling();
            }
            return new Face(fA, fB, fC, fD, mtlFace);
        }
        return null;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IDynXmlObject interface implementation                                                                                     //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * This is the generic load for a geometric object.
     *
     * @param xmlElement    The dynamically loaded object DOM Document element.
     * @param refObjectList A linked list if named objects (implementing the {@link INamedObject} interface)
     *                      that have already been loaded and can be used to resolve object references.
     * @throws DynXmlObjParseException Thrown if there is an error in the scene description XML that cannot be parsed
     *                                 by this object.
     */
    public void loadFromXml(final @NotNull Element xmlElement, final LinkedList<INamedObject> refObjectList)
            throws DynXmlObjParseException {
        try {
            Node domNode = xmlElement.getFirstChild();
            while (null != domNode) {
                if (domNode instanceof Element) {
                    IRtMaterial mtl;
                    final Element element = (Element) domNode;
                    if (!internalParseElement(element, refObjectList)) {
                        if (null != (mtl = FrameLoader.tryParseMaterial(element, refObjectList, getType(), m_strName))) {
                            m_mtl = mtl;
                        } else {
                            pkgThrowUnrecognizedXml(element);
                        }
                    }
                }
                domNode = domNode.getNextSibling();
            }
            internalFinishLoad();
        } catch (final Throwable t) {
            if (t instanceof DynXmlObjParseException) {
                throw (DynXmlObjParseException) t;
            } else {
                throw new DynXmlObjParseException(getClass().getName() + " parse exception", t);
            }
        }
    }

    /**
     * @param element       (not null, readonly) The dom element to be parsed.
     * @param refObjectList A linked list if named objects (implementing the {@link INamedObject} interface)
     *                      that have already been loaded and can be used to resolve object references.
     * @return <tt>true</tt> if the element was parsed, <tt>false</tt> otherwise.
     */
    protected boolean internalParseElement(@NotNull Element element, final LinkedList<INamedObject> refObjectList)
            throws DynXmlObjParseException {
        return false;
    }

    /**
     *
     */
    protected void internalFinishLoad() {
    }

    /**
     * Create the dynamically loaded object node with attributes and then calls
     * {@link cip.render.raytrace.geometry.AGeometry#internalToXml(org.w3c.dom.Element)}.  This provides the
     * dynamically loaded XML boilerplate for the object.  Derived classes should not override this function,
     * but should override the {@link cip.render.raytrace.geometry.AGeometry#internalToXml(org.w3c.dom.Element)} to
     * add object-specific information to the XML node.
     */
    public void toChildXmlElement(final @NotNull Element parentEl) {
        // This is the dynamically loaded object boilerplate
        final Element element = parentEl.getOwnerDocument().createElement(DynXmlObjLoader.XML_TAG);
        parentEl.appendChild(element);
        element.setAttribute(DynXmlObjLoader.XML_ATTR_CLASS, this.getClass().getName());
        element.setAttribute(DynXmlObjLoader.XML_ATTR_NAME, m_strName);
        // this is the geometry specific stuff
        internalToXml(element);
    }

    /**
     * Override this function to add object-specific information to an XML node.
     *
     * @param element The dynamically loaded object node that the object-specific information should be written into. The
     *                {@link IDynXmlObject#loadFromXml(Element, LinkedList)} funtcion must
     *                be overridden by the geometry implementation to be able to read the object-specific information set by
     *                the object here.
     */
    protected void internalToXml(@NotNull final Element element) {
    }

    /**
     * Resolve a geometry reference by finding the geometry in the reference object list.  This is typically used by
     * the implementation of a geometry that contains other geometries that are specified by reference in the XML
     * geometry specification.
     *
     * @param strName       The name of the geometry that will be located in the reference object list.
     * @param refObjectList The reference object list.
     * @return Returns the named geometry from the reference object list.
     * @throws DynXmlObjParseException Thrown if the geometry cannot be found in the reference object list.
     */
    protected IRtGeometry resolveGeometryRef(final String strName, final LinkedList refObjectList) throws DynXmlObjParseException {
        if (!strName.equals("") && (null != refObjectList)) {
            for (final Object obj : refObjectList) {
                if ((obj instanceof IRtGeometry) && ((INamedObject) obj).getName().equals(strName)) {
                    return (IRtGeometry) obj;
                }
            }
        }
        throw new DynXmlObjParseException(String.format("Referenced geometry \"%s\" was not found.", strName));
    }

    void pkgThrowUnrecognizedXml(Element element) throws DynXmlObjParseException {
        throw new DynXmlObjParseException(String.format("Unrecognized %s XML description element <%s>",
                m_strName, element.getTagName()));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // INamedObject interface implementation                                                                                      //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public @NotNull String getName() {
        return m_strName;
    }

    @Override
    public void setName(final @NotNull String strName) {
        m_strName = strName;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IRtGeometry interface implementation                                                                                       //
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @NotNull
    @Override
    public String getType() {
        return m_strType;
    }

    @Override
    public void initSampling(final int nSample, final float[] f1dSample, final float[] f1dRandom, final Point2f[] pt2dSample,
                             final Point2f[] pt2dRandom, final Point3f[] pt3dSample, final Point3f[] pt3dRandom) {
    }

    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * The default implementation returns <tt>false</tt>, that the object is not convex.
     *
     * @return Returns <tt>true</tt> if the object is convex and <tt>false</tt> if the object is not convex
     * or the convexity cannot be verified.
     */
    @Override
    public boolean isConvex() {
        return false;
    }

    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * The default implementation returns <tt>null</tt>, that there is no convex hull.
     *
     * @return Returns the array of convex hull vertices,  Returns <tt>null</tt> if a convex hull cannot be
     * fit to the object.  Being unable to fit a convex hull implies that the object must be explicitly
     * queried for intersection on every ray.
     */
    @Override
    public Point3f[] getConvexHullVertices() {
        return null;
    }

    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * The default implementation reports that we are in the middle of an infinitely extending bounding volume for the
     * object, so, the object must be tested for intersection.
     *
     * @param bvInt The bounding volume intersection.
     * @param ray   The ray being tested for intersection with the bounding volume.
     * @return <tt>true</tt> if the ray intersects the bounding volume and may intersect the object, <tt>false</tt> if
     * the ray does not intersect the bounding volume and cannot intersect the object.
     */
    @Override
    public boolean getBvIntersection(final Bv3fIntersection bvInt, final Line3f ray) {
        bvInt.m_nCode = Bv3fIntersection.INTERSECTS_INSIDE;
        bvInt.m_fDistIn = Float.NEGATIVE_INFINITY;
        bvInt.m_fDistOut = Float.POSITIVE_INFINITY;
        bvInt.m_obj = this;
        return true;
    }
    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Gets the ray intersection.  The default implementation simply returns <tt>false</tt>, there is no intersection.
     *
     * @param intersection  The intersection of the ray and the surface.  The intersection will be either freshly
     *                      initialized or will contain information about the closest intersection discovered thus far.
     * @param ray           The ray being tesed for intersection.
     * @param bStartsInside <tt>false</tt> if this is an outside ray, <tt>true</tt> if this is a ray
     *                      spawned from an intersection with this object and on the inside of the object.
     * @param nSample       The pixel sub-sample index.  This is used in distributed ray-tracing to make sure
     *                      the correct sample displacement is used for samples that are distributed.
     * @param nRandom       The jitter array index..
     * @return Returns <tt>true</tt> if there is an intersection with this object that is closer than the
     * current intersection, and <tt>false</tt> otherwise.
     */
    @Override
    public boolean getRayIntersection(@NotNull final RayIntersection intersection, @NotNull final Line3f ray, final boolean bStartsInside,
                                      final int nSample, final int nRandom) {
        return false;
    }

    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * This is a pseudo-natural mapping that just projects the xy (plan) object coordinates to be the "natural coordinates",
     * and creates the axis aligned and scaled displacement vectors for U and V.  This is a linear projection in plan.  The UV
     * displacement vectors are simply the projections of the i,j vectors rotated as though the following normal that had
     * initially been axis aligned with the Z axis.
     *
     * @param intersection The ray intersection.
     */
    @Override
    public void getNaturalCoordinates(@NotNull final RayIntersection intersection) {
        final Vector3f vN = intersection.m_vObjNormal;

        intersection.m_ptNatural.x = intersection.m_ptObject.x;
        intersection.m_ptNatural.y = intersection.m_ptObject.y;
        intersection.m_ptNatural.z = Float.NaN;

        intersection.m_vNatural[0].i = (float) Math.sqrt((vN.j * vN.j) + (vN.k * vN.k));
        intersection.m_vNatural[0].j = 0.0f;
        intersection.m_vNatural[0].k = 0.0f;

        intersection.m_vNatural[1].i = 0.0f;
        intersection.m_vNatural[1].j = (float) Math.sqrt((vN.i * vN.i) + (vN.k * vN.k));
        intersection.m_vNatural[1].k = 0.0f;

        intersection.m_bNatural = true;
    }

    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * The default implementation returns <tt>false</tt>, that there is no cast shadow.
     *
     * @param intersection The intersection being tested for shadow.
     * @param vLight       The direction from the intersection to the light.
     * @param fDistLight   The distance from the intersection to the light.
     * @param light        The light.  This is supplied as an argument in the event an object is both a light source
     *                     and a geometry and the geometry of the light should not be tested for casting a shadow from that light.
     * @param nSample      The pixel sub-sample index.  This is used in distributed ray-tracing to make sure
     *                     the correct sample displacement is used for samples that are distributed.
     * @param nRandom      The jitter array index.
     * @return Returns <tt>true</tt> if this object casts a shadow from the light and <tt>false</tt>
     * otherwise.
     */
    public boolean testShadow(final RayIntersection intersection, final Vector3f vLight, final float fDistLight,
                              final IRtLight light, final int nSample, final int nRandom) {
        return false;
    }


}
