/*
 * Xfm4x4f.java
 *
 * Created on September 19, 2002, 8:16 PM
 * Copyright(c) 1993-2019 Crisis in Perspective, Inc.
 *                        PO Box 1949
 *                        Hood River, OR 97031
 *                        www.crisisinperspecive.com
 */
package cip.render.util3d;

import cip.render.util.AngleF;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Element;

import java.util.StringTokenizer;

/**
 * A class representing a
 * transformation (Xfm) in 3D of single precision (the components are represented by <tt>float</tt> values),
 * hence the name <tt>Xfm4x4f</tt>.
 * <p>
 * This class implements the basic functionality for a 3D transformation required for rendering and 3D graphics use.  This class is
 * patterned after and most code adapted from the <tt>CXfm4x4f</tt> class of the <b><i>JOEY</i></b> toolkit written and
 * distributed by Crisis in Perspective, Inc.
 * <p>
 * NOTE: the transformation conventions as described int the OpenGL reference manuals are used so that the translation components
 * are in the <tt>xfm[0][3]</tt>, <tt>xfm[1][3]</tt>, and <tt>xfm[2][3]</tt> locations (the transposed representations that put the
 * translations in <tt>xfm[3][0]</tt>, <tt>xfm[3][1]</tt>, and <tt>xfm[3][2]</tt> locations were used in <b>JOEY</b>).
 * <p>
 * Specifically, point transformation is of the form:<br><br>
 * <pre>
 *     [ xfm[0][0] xfm[0][1] xfm[0][2] xfm[0][3] ] [ X ]   [ Xt ]
 *     | xfm[1][0] xfm[1][1] xfm[1][2] xfm[1][3] | | Y | = | Yt |
 *     | xfm[2][0] xfm[2][1] xfm[2][2] xfm[2][3] | | Z |   | Zt |
 *     [ xfm[3][0] xfm[3][1] xfm[3][2] xfm[3][3] ] [ H ]   [ Ht ]
 * </pre>
 * Where <tt>[X,Y,Z,H]</tt> is the un-transformed point or vector and <tt>[Xt,Yt,Zt,Ht]</tt> is the transformed point or
 * vector.  Normally, <tt>H = 1</tt> for a point transformation and <tt>H = 0</tt> for a vector transformation.
 *
 * @author royster.hall@gmail.com
 * @version 1.0
 * @since 1.0
 */
public class Xfm4x4f {
    public static final String XML_ATTR_ORIGIN = "originAt";
    public static final String XML_ATTR_SCALE = "scale";
    public static final String XML_ATTR_SHEAR = "shear";
    public static final String XML_ATTR_AZIMUTH = "azimuth";
    public static final String XML_ATTR_ALTITUDE = "altitude";
    public static final String XML_ATTR_ROLL = "roll";
    public static final String XML_ATTR_AIMED = "aimedAt";
    private static final boolean DEBUG = false;
    // the axis for rotation
    public static final int AXIS_X = 0;
    public static final int AXIS_Y = 1;
    public static final int AXIS_Z = 2;

    // -----------------------------------------------------------------------------------------------------------------------------
    // NOTE: This class was originally subclassed from the Java3d transformation - which had a variable for each element
    // in the matrix. Java3d never really developed any traction so this is being converted back to a 2D array form for
    // clarity and debugging.
    // -----------------------------------------------------------------------------------------------------------------------------
    /**
     * A pointer to tne next <tt>Xfm4x4f</tt> in a cache if this object is cached.
     */
    public Xfm4x4f m_next = null;

    private float[][] xfm = new float[4][4];

    /**
     * Creates a new instance of <tt>Xfm4x4f</tt> that is initialized to an identity.
     */
    public Xfm4x4f() {
        this.identity();
    }

    /**
     * Creates a new instance of <tt>Xfm4x4f</tt> that is initialized to another transform.
     *
     * @param xfmInit The trans formation this transformation should be set equal to.
     */
    @SuppressWarnings("CopyConstructorMissesField")
    public Xfm4x4f(final Xfm4x4f xfmInit) {
        this.setValue(xfmInit);
    }

    /**
     * Sets the value of this transformation to be equal to the value of another transformation.  By setting equal, we mean
     * that each of the 16 terms in the transformation is set equal.
     *
     * @param xfmInit The transformation to set this transformation equal to.
     * @return Returns this transformation after it have been set equal to <tt>xfmInit</tt>.
     */
    public Xfm4x4f setValue(final Xfm4x4f xfmInit) {
        for (int row = 0; row < 4; row++) {
            System.arraycopy(xfmInit.xfm[row], 0, xfm[row], 0, 4);
        }
        return this;
    }

    public Xfm4x4f setValue(final Point3f ptOrigin, final Point3f ptAimedAt) throws ZeroLengthVectorException {
        identity();
        // set temp vector - from the originr to the aimedAt point
        final Vector3f vTmp = new Vector3f(ptAimedAt.x - ptOrigin.x, ptAimedAt.y - ptOrigin.y, ptAimedAt.z - ptOrigin.z).normalize();
        final AngleF aAzimuth = new AngleF().atan2(-vTmp.i, -vTmp.j);
        final AngleF aAltitude = new AngleF().asin(-vTmp.k);
        final AngleF aRoll = new AngleF(AngleF.DEGREES, 0.0f);
        compose(aAzimuth, aAltitude, aRoll, new Vector3f(ptOrigin.x, ptOrigin.y, ptOrigin.z));
        return this;
    }

    /**
     * Get the value of an element of the transformation.
     * @param row (int) The row of the element.
     * @param col (int) The column of the element.
     * @return (float) Returns the eelment.
     */
    public float get(final int row, final int col) {
        return xfm[row][col];
    }

    /**
     * Sets the value of this 4x4 transform from the attributes of an XML DOM Element.  transforms are specified through
     * attributes so that the element tag can be used to identify how the transform should be applied - for example,
     * an object may have several transformation components in the object definition.  The transformation is specified
     * as follows:<br><br>
     * <pre>
     *     <font style="color:blue">&lt;<i>someTag</i> originAt="<font style="color:magenta">Tx,Ty,Tx</font>"
     *              scale="<font style="color:magenta">Sx,Sy,Sz</font>"
     *              shear="<font style="color:magenta">Kxy,Kxz,Kyz</font>"
     *              azimuth="<font style="color:magenta">azimuthDegrees</font>"
     *              altitude="<font style="color:magenta">altitudeDegrees</font>"
     *              roll="<font style="color:magenta">rollDegrees</font>"
     *              aimedAt="<font style="color:magenta">Xtarget,Ytarget,Ztarget</font>"/&gt;</font><br><br>
     * </pre>
     * <table style="width:90%">
     * <caption style="text-align:left">where:</caption>
     * <tr>
     * <td style="width:5%"></td>
     * <td><table border="1" summary="">
     * <tr>
     * <td><tt>originAt</tt></td>
     * <td>Specify the new location of the origin (the translation of the origin).  The default
     * is to leave the location of the origin at 0,0,0 if unspecified.
     * </td>
     * </tr>
     * <tr>
     * <td><tt>scale</tt></td>
     * <td>Specify the scale.  The default scale is 1,1,1 (no change in scale) if unspecified.  A scale
     * of near zero in any of the components will squash an object into a plane and will result in
     * a transformation that cannot be inverted.  If <tt>bAllowScale == false</tt> the scale
     * specification will be ignored.
     * </td>
     * </tr>
     * <tr>
     * <td><tt>shear</tt></td>
     * <td>Specify the shear.  The default shear is 0,0,0 (no shear distortion) if unspecified.  If
     * <tt>bAllowShear == false</tt> the shear specification will be ignored.
     * </td>
     * </tr>
     * <tr>
     * <td><tt>azimuth</tt></td>
     * <td> The azimuth or plan angle specified in degrees.  Azimuth is applied using the convention that
     * XY is the ground plane and +Y is north.  The azimuth is measured clockwise looking down.  The azimuth angle
     * is mutually incompatible with <tt>aimedAt</tt> specification of orientation.
     * </td>
     * </tr>
     * <tr>
     * <td><tt>altitude</tt></td>
     * <td> The altitude or elevation angle above the ground plane specified in degrees. Altitude is applied using
     * the convention that XY is the ground plane and +Y is north.
     * The altitude angle is mutually incompatible with  <tt>aimedAt</tt> specification of orientation.
     * </td>
     * </tr>
     * <tr>
     * <td><tt>roll</tt></td>
     * <td> The roll angle in degrees.
     * </td>
     * </tr>
     * <tr>
     * <td><tt>aimedAt</tt></td>
     * <td>The point the +Y axis is aimed at from the origin.  The Azimuth and Altitude are derived from the
     * relative positions of the origin and aim points.  AimedAt is mutually incompatible with the
     * <tt>azimuth</tt> and <tt>altitude</tt> specification of orientation.
     * </td>
     * </tr>
     * </table>
     * </td>
     * </tr>
     * </table><br>
     *
     * @param xmlElement  The XML DOM Element.
     * @param bAllowScale If <tt>true</tt> then scale specification will be processed, if <tt>false</tt>, then
     *                    scale specification will be ignored.  This should be <tt>false</tt> for rigid-body transformation.
     * @param bAllowShear If <tt>true</tt> then shear specification will be processed, if <tt>false</tt>, then
     *                    shear specification will be ignored.  This should be <tt>false</tt> for rigid-body transformation.
     * @return Returns this 4x4 transform after its value has been set.
     * @throws ZeroLengthVectorException Thrown if an <tt>originAt</tt> and <tt>aimedAt</tt> specification is used
     *                                   and the points are so close together that the direction vector is undefined.
     */
    public Xfm4x4f setValue(final Element xmlElement, final boolean bAllowScale, final boolean bAllowShear) throws ZeroLengthVectorException {
        final String strTranslate = xmlElement.getAttribute(XML_ATTR_ORIGIN).trim();
        final String strScale = xmlElement.getAttribute(XML_ATTR_SCALE).trim();
        final String strShear = xmlElement.getAttribute(XML_ATTR_SHEAR).trim();
        final String strAzimuth = xmlElement.getAttribute(XML_ATTR_AZIMUTH).trim();
        final String strAltitude = xmlElement.getAttribute(XML_ATTR_ALTITUDE).trim();
        final String strRoll = xmlElement.getAttribute(XML_ATTR_ROLL).trim();
        final String strAimAt = xmlElement.getAttribute(XML_ATTR_AIMED).trim();
        if (DEBUG) {
            System.out.println(XML_ATTR_ORIGIN + "=" + strTranslate);
        }
        if (DEBUG) {
            System.out.println(XML_ATTR_SCALE + "=" + strScale);
        }
        if (DEBUG) {
            System.out.println(XML_ATTR_SHEAR + "=" + strShear);
        }
        if (DEBUG) {
            System.out.println(XML_ATTR_AZIMUTH + "=" + strAzimuth);
        }
        if (DEBUG) {
            System.out.println(XML_ATTR_ALTITUDE + "=" + strAltitude);
        }
        if (DEBUG) {
            System.out.println(XML_ATTR_ROLL + "=" + strRoll);
        }
        if (DEBUG) {
            System.out.println(XML_ATTR_AIMED + "=" + strAimAt);
        }
        if (!strAimAt.equals("") && (!strAzimuth.equals("") || !strAltitude.equals(""))) {
            throw new IllegalArgumentException("redundant orientation specification, use either aimedAt or azimuth,altitude.");
        }
        if (!strAimAt.equals("") && strTranslate.equals("")) {
            throw new IllegalArgumentException("the origin must be specified to use the aimedAt positioning.");
        }

        final Vector3f vTranslate = new Vector3f();
        float fSx = 1.0f;
        float fSy = 1.0f;
        float fSz = 1.0f;
        float fShearXY = 0.0f;
        float fShearXZ = 0.0f;
        float fShearYZ = 0.0f;
        final AngleF aAzimuth = new AngleF(AngleF.DEGREES, 0.0f);
        final AngleF aAltitude = new AngleF(AngleF.DEGREES, 0.0f);
        final AngleF aRoll = new AngleF(AngleF.DEGREES, 0.0f);

        if (!strTranslate.equals("")) {
            // initialize the translate value from a string of the form i,j,k
            final StringTokenizer tokens = new StringTokenizer(strTranslate, ",");
            if (tokens.countTokens() != 3) {
                throw new IllegalArgumentException("originAt specification must be in the form \"i,j,k\"");
            }
            vTranslate.i = getNextFloat(tokens);
            vTranslate.j = getNextFloat(tokens);
            vTranslate.k = getNextFloat(tokens);
        }
        if (bAllowScale && !strScale.equals("")) {
            // initialize the scale value from a string of the form Sx,Sy,Sz
            final StringTokenizer tokens = new StringTokenizer(strScale, ",");
            if (tokens.countTokens() != 3) {
                throw new IllegalArgumentException("scale specification must be in the form \"Sx,Sy,Sz\"");
            }
            fSx = getNextFloat(tokens);
            fSy = getNextFloat(tokens);
            fSz = getNextFloat(tokens);
        }
        if (bAllowShear && !strShear.equals("")) {
            // initialize the shear value from a string of the form kXY,kXZy,kYZ
            final StringTokenizer tokens = new StringTokenizer(strShear, ",");
            if (tokens.countTokens() != 3) {
                throw new IllegalArgumentException("shear specification must be in the form \"kXY,kXZy,kYZ\"");
            }
            fShearXY = getNextFloat(tokens);
            fShearXZ = getNextFloat(tokens);
            fShearYZ = getNextFloat(tokens);
        }
        if (!strAimAt.equals("")) {
            // the aimed at is a string of the form i,j,k.
            final Point3f ptAt = new Point3f();
            final StringTokenizer tokens = new StringTokenizer(strAimAt, ",");
            if (tokens.countTokens() != 3) {
                throw new IllegalArgumentException("aimedAt specification must be in the form \"i,j,k\"");
            }
            ptAt.x = getNextFloat(tokens);
            ptAt.y = getNextFloat(tokens);
            ptAt.z = getNextFloat(tokens);
            // This vector - from the center to the aimedAt point
            final Vector3f vTmp = new Vector3f(ptAt.x - vTranslate.i, ptAt.y - vTranslate.j, ptAt.z - vTranslate.k).normalize();
            aAzimuth.atan2(-vTmp.i, -vTmp.j);
            aAltitude.asin(-vTmp.k);
        }
        if (!strAzimuth.equals("")) {
            aAzimuth.setValue(AngleF.DEGREES, Float.parseFloat(strAzimuth));
        }
        if (!strAltitude.equals("")) {
            aAltitude.setValue(AngleF.DEGREES, Float.parseFloat(strAltitude));
        }
        if (!strRoll.equals("")) {
            aRoll.setValue(AngleF.DEGREES, Float.parseFloat(strRoll));
        }
        return compose(fSx, fSy, fSz, fShearXY, fShearXZ, fShearYZ, aAzimuth, aAltitude, aRoll, vTranslate);
    }

    private float getNextFloat(StringTokenizer tokens) {
        return Float.parseFloat(tokens.nextToken().trim());
    }

    /**
     * Adds the transformation definition attributes to the XML DOM element.
     *
     * @param element     The element the transform attributes should be added to.
     * @param bAllowScale If <tt>true</tt> then scale specification will be included, if <tt>false</tt>, then
     *                    scale specification will not be included.  This should be <tt>false</tt> for rigid-body transformation.
     * @param bAllowShear If <tt>true</tt> then shear specification will be included, if <tt>false</tt>, then
     *                    shear specification will not be included.  This should be <tt>false</tt> for rigid-body transformation.
     * @throws ZeroLengthVectorException Thrown if the transformation is singular and cannot be uniquely decomposed.
     */
    public void toXmlAttr(final Element element, final boolean bAllowScale, final boolean bAllowShear) throws ZeroLengthVectorException {
        final AngleF aAzimuth = new AngleF();
        final AngleF aAltitude = new AngleF();
        final AngleF aRoll = new AngleF();
        final Vector3f trans = new Vector3f();
        final Vector3f scale = new Vector3f();
        final Vector3f shear = new Vector3f();
        // decompose the transform toi its components
        decompose(scale, shear, trans, aAzimuth, aAltitude, aRoll);
        // now generate the string
        final StringBuilder strBuff = new StringBuilder(64);
        if (bAllowScale && ((scale.i != 1.0f) || (scale.j != 1.0f) || (scale.k != 1.0f))) {
            strBuff.append(scale.i).append(',').append(scale.j).append(',').append(scale.k);
            element.setAttribute(XML_ATTR_SCALE, strBuff.substring(0));
        }
        if (bAllowShear && ((shear.i != 0.0f) || (shear.j != 0.0f) || (shear.k != 0.0f))) {
            strBuff.delete(0, strBuff.capacity()).append(shear.i).append(',').append(shear.j).append(',').append(shear.k);
            element.setAttribute(XML_ATTR_SHEAR, strBuff.substring(0));
        }
        if ((trans.i != 0.0f) || (trans.j != 0.0f) || (trans.k != 0.0f)) {
            strBuff.delete(0, strBuff.capacity()).append(trans.i).append(',').append(trans.j).append(',').append(trans.k);
            element.setAttribute(XML_ATTR_ORIGIN, strBuff.substring(0));
        }
        if (aAzimuth.getDegrees() != 0.0f) {
            element.setAttribute(XML_ATTR_AZIMUTH, Float.toString(aAzimuth.getDegrees()));
        }
        if (aAltitude.getDegrees() != 0.0f) {
            element.setAttribute(XML_ATTR_ALTITUDE, Float.toString(aAltitude.getDegrees()));
        }
        if (aRoll.getDegrees() != 0.0f) {
            element.setAttribute(XML_ATTR_ROLL, Float.toString(aRoll.getDegrees()));
        }
    }

    /**
     * Sets this transformation to an identity transformation as:<br>
     * <pre>
     *     [ 1 0 0 0 ]
     *     | 0 1 0 0 |
     *     | 0 0 1 0 |
     *     [ 0 0 0 1 ]
     * </pre>
     *
     * @return Returns this transform after it has been set to an identity transform.
     */
    public Xfm4x4f identity() {
        for (int row = 0; row < 4; row++) {
            for (int col = 0; col < 4; col++) {
                xfm[row][col] = (row == col) ? 1.0f : 0.0f;
            }
        }
        return this;
    }

    /**
     * Premultiply this transformation by a translation transformation of the form:
     * <pre>
     *     [ 1 0 0 fTx ]
     *     | 0 1 0 fTy |
     *     | 0 0 1 fTz |
     *     [ 0 0 0  1  ]
     * </pre>
     * <p>
     * If you want to set this transformation to be the translation transformation,
     * set this transformation to be an identity transformation before applying the translate.
     *
     * @param fTx The X translation.
     * @param fTy The Y translation.
     * @param fTz The Z translation.
     * @return Returns this transform after it has been premultiplied by a translation transformation.
     */
    public Xfm4x4f translate(final float fTx, final float fTy, final float fTz) {
        // The revised implementation taking advantage of the form of the transformation matrix to
        //  optimize the operations and do the premultiply locally.
        if (0.0f != xfm[3][0]) {
            xfm[0][0] += xfm[3][0] * fTx;
            xfm[1][0] += xfm[3][0] * fTy;
            xfm[2][0] += xfm[3][0] * fTz;
        }
        if (0.0f != xfm[3][1]) {
            xfm[0][1] += xfm[3][1] * fTx;
            xfm[1][1] += xfm[3][1] * fTy;
            xfm[2][1] += xfm[3][1] * fTz;
        }
        if (0.0f != xfm[3][2]) {
            xfm[0][2] += xfm[3][2] * fTx;
            xfm[1][2] += xfm[3][2] * fTy;
            xfm[2][2] += xfm[3][2] * fTz;
        }
        if (0.0f != xfm[3][3]) {
            xfm[0][3] += xfm[3][3] * fTx;
            xfm[1][3] += xfm[3][3] * fTy;
            xfm[2][3] += xfm[3][3] * fTz;
        }
        return this;
    }

    /**
     * Premultiply this transformation by a translation transformation of the form:
     * <pre>
     *     [ 1 0 0 vTranslate.i ]
     *     | 0 1 0 vTranslate.j |
     *     | 0 0 1 vTranslate.k |
     *     [ 0 0 0      1       ]
     * </pre>
     * <p>
     * If you want to set this transformation to be the translation transformation,
     * set this transformation to be an identity transformation before applying the translate.
     *
     * @param vTranslate The translation vector (direction and magnitude of translation).
     * @return Returns this transform after it has been premultiplied by a translation transformation.
     */
    public Xfm4x4f translate(final Vector3f vTranslate) {
        return translate(vTranslate.i, vTranslate.j, vTranslate.k);
    }

    /**
     * Premultiply this transformation by a scaling transformation of the form:
     * <pre>
     *     [ fSx  0   0  0 ]
     *     |  0  fSy  0  0 |
     *     |  0   0  fSz 0 |
     *     [  0   0   0  1 ]
     * </pre>
     * <p>
     * If you want to set this transformation to be the scaling transformation,
     * set this transformation to be an identity transformation before applying the scale.
     *
     * @param fSx The X scale factor.
     * @param fSy The Y scale factor.
     * @param fSz The Z scale factor.
     * @return Returns this transform after it has been premultiplied by a scaling transformation.
     */
    public Xfm4x4f scale(final float fSx, final float fSy, final float fSz) {
        return preMul(fSx, 0.0f, 0.0f, 0.0f, fSy, 0.0f, 0.0f, 0.0f, fSz);
    }

    /**
     * Premultiply this transformation by a rotation transformation for the specified rotation about the specified axis.
     * This function is a front for the version of {@link Xfm4x4f#rotate(int, AngleF)} that takes
     * the sin and cosine of the rotation angle as arguments.
     * <p>
     * If you want to set this transformation to be the rotation transformation,
     * set this transformation to be an identity transformation before applying the rotate.
     *
     * @param nAxis The axis of rotation: {@link Xfm4x4f#AXIS_X}, {@link Xfm4x4f#AXIS_Y},
     *              or {@link Xfm4x4f#AXIS_Z}
     * @param aRot  The angle of rotation.
     * @return Returns this transform after it has been premultiplied by a rotation transformation.
     */
    public Xfm4x4f rotate(final int nAxis, final AngleF aRot) {
        final float fSin = aRot.sin();
        final float fCos = aRot.cos();
        return rotate(nAxis, fSin, fCos);
    }

    public Xfm4x4f rotate(final int nAxis, final float fSin, final float fCos) {
        switch (nAxis) {
            case AXIS_X:
                return preMul(1.0f, 0.0f, 0.0f, 0.0f, fCos, -fSin, 0.0f, fSin, fCos);
            case AXIS_Y:
                return preMul(fCos, 0.0f, fSin, 0.0f, 1.0f, 0.0f, -fSin, 0.0f, fCos);
            case AXIS_Z:
                return preMul(fCos, -fSin, 0.0f, fSin, fCos, 0.0f, 0.0f, 0.0f, 1.0f);
            default:
                // this is an unknown axis
                throw new IllegalArgumentException("Unrecognised rotation axis specified");
        }
    }

    /**
     * @param vAxis The axis of rotation.
     * @param aRot  The angle of rotation.
     * @return Returns the transformation with the rotation applied.
     */
    public Xfm4x4f rotate(final Vector3f vAxis, final AngleF aRot) {
        // rotation for an arbitrary axis (from Rogers and Adams) -
        //  > Check length of the input vector (0,0,0 vector provides no axis info, and an identity is returned)
        //  > Rotate the axis to be coincident with the Z axis by an i and j rotation.
        //      Apply the rotation in Z
        //      Apply the inverse of the X and Y rotations
        final float fLength = vAxis.getLength();
        if (PackageConstants.isZero(fLength)) {
            throw new ZeroLengthVectorException();
        } else {
            // get the rotation into an axis aligned state
            final Xfm4x4f xfmRotIn = new Xfm4x4f().identity();
            final float fxyLen = (float) Math.sqrt((vAxis.j * vAxis.j) + (vAxis.i * vAxis.i));
            if (!PackageConstants.isZero(fxyLen)) {
                xfmRotIn.rotate(AXIS_Z, -(vAxis.j / fxyLen), vAxis.i / fxyLen);
            }
            xfmRotIn.rotate(AXIS_Y, -fxyLen, vAxis.k);
            // get the rotation out of the axis aligned state
            final Xfm4x4f xfmRotOut = new Xfm4x4f(xfmRotIn).invert();    // save and invert to get the rotation out
            //  apply the rotation in the axis aligned state
            xfmRotIn.rotate(AXIS_Z, aRot);
            // now do the multiplications to the current transform
            preMul(xfmRotIn);
            preMul(xfmRotOut);
        }
        return this;
    }

    /**
     * Premultiply this transformation by a shearing transformation of the form:
     * <pre>
     *     [ 1  Kxy Kxz  0 ]
     *     | 0   1  Kyz  0 |
     *     | 0   0   1   0 |
     *     [ 0   0   0   1 ]
     * </pre>
     * <p>
     * If you want to set this transformation to be the shearing transformation,
     * set this transformation to be an identity transformation before applying the shear.
     *
     * @param fShearXY The shear in X as a function of Y (Kxy in the matrix above).  For example a value of 2 means that for
     *                 every unit of displacement Y, the X value is shifted by 2.
     * @param fShearXZ The shear in X as a function of Z (Kxz in the matrix above).  For example a value of 2 means that for
     *                 every unit of displacement Z, the X value is shifted by 2.
     * @param fShearYZ The shear in Y as a function of Z (Kyz in the matrix above).  For example a value of 2 means that for
     *                 every unit of displacement Z, the Y value is shifted by 2.
     * @return Returns this transform after it has been premultiplied by a shearing transformation.
     */
    public Xfm4x4f shear(final float fShearXY, final float fShearXZ, final float fShearYZ) {
        return preMul(1.0f, fShearXY, fShearXZ, 0.0f, 1.0f, fShearYZ, 0.0f, 0.0f, 1.0f);
    }

    /**
     * Build up a transformation that include combined scale, shear, rotation, and translation - in that order.  Rotation is
     * applied as roll (rotation around Z) followed by altitude (rotation about X) followed by azimuth (rotation about Z).  To
     * follow the geographic conventions of azimuth specification, this is a clockwise rotation when looking down the positive
     * Z axis towards the origin in a right handed coordinate system (which is a -azimuth rotation around Z using standard
     * graphics conventions).
     * <p>
     * If a composed transformation is decomposed, the scale, shear, rotation, and translation that create this transformation
     * will be returned.  It may be the case that these vary from the original specification due to numerical and round off
     * error, and that the angles will be set the their -180 degree to +180 degree equivalents.
     *
     * @param fSx       The X scale factor.
     * @param fSy       The Y scale factor.
     * @param fSz       The Z scale factor.
     * @param fKxy      The shear in X as a function of Y.  For example a value of 2 means that for
     *                  every unit of displacement Y, the X value is shifted by 2.
     * @param fKxz      The shear in X as a function of Z.  For example a value of 2 means that for
     *                  every unit of displacement Z, the X value is shifted by 2.
     * @param fKyz      The shear in Y as a function of Z.  For example a value of 2 means that for
     *                  every unit of displacement Z, the Y value is shifted by 2.
     * @param aAzimuth  The azimuth angle.
     * @param aAltitude The altitude angle.
     * @param aRoll     The roll angle.
     * @param vTrans    The translation vector (direction and magnitude of translation).
     * @return Returns this transformation after it has been set as specified.
     */
    public Xfm4x4f compose(final float fSx, final float fSy, final float fSz,
                           final float fKxy, final float fKxz, final float fKyz,
                           final AngleF aAzimuth, final AngleF aAltitude, final AngleF aRoll,
                           final Vector3f vTrans) {
        // this assumes xy is the ground plane and k is up (right handed)
        return identity().scale(fSx, fSy, fSz).shear(fKxy, fKxz, fKyz).
                rotate(AXIS_Y, aRoll).
                rotate(AXIS_X, aAltitude).
                rotate(AXIS_Z, -aAzimuth.sin(), aAzimuth.cos()).
                translate(vTrans);
    }

    /**
     * Build up a rigid body transformation that include combined rotation and translation - in that order.  Rotation is applied
     * as roll (rotation around Z) followed by altitude (rotation about X) followed by azimuth (rotation about Z).  To follow
     * the geographic conventions of azimuth specification, this is a clockwise rotation when looking down the positive Z axis
     * towards the origin in a right handed coordinate system (which is a -azimuth rotation around Z using standard graphics
     * conventions).
     * <p>
     * If a composed transformation is decomposed, the scale, shear, rotation, and translation that create this transformation
     * will be returned.  It may be the case that these vary from the original specification due to numerical and round off
     * error, and that the angles will be set the their -180 degree to +180 degree equivalents.
     *
     * @param aAzimuth  The azimuth angle.
     * @param aAltitude The altitude angle.
     * @param aRoll     The roll angle.
     * @param vTrans    The translation vector (direction and magnitude of translation).
     * @return Returns this transformation after it has been set as specified.
     */
    public Xfm4x4f compose(final AngleF aAzimuth, final AngleF aAltitude, final AngleF aRoll, final Vector3f vTrans) {
        // this assumes xy is the ground plane and k is up (right handed)
        return identity().
                rotate(AXIS_Y, aRoll).
                rotate(AXIS_X, aAltitude).
                rotate(AXIS_Z, -aAzimuth.sin(), aAzimuth.cos()).
                translate(vTrans);
    }

    /**
     * Decompose this transformation into a set of scale, shear, rotate, and translate components that will generate the
     * transformation.  Decomposition and recomposition is a useful operation set when trying to reduce numerical errors
     * accumulated during interactive manipulations that repeatedly pre-multiply the transformation by incremental
     * repositioning transformations.
     * <p>
     * NOTE: the use of tuples for returning the decomposition is not my favorite choice, but it allows you to get the
     * values in whatever form is most convenient for future manipulation.
     *
     * @param scale     The Sx, Sy, Sz scaling as the i, j, k fields of the tuple.
     * @param shear     The XY, XZ, YZ sharing as the i, j, k fields of the tuple.
     * @param translate The Tx, Ty, Tz translation as the i, j, k fields of the tuple.
     * @param aAzimuth  The azimuth (plan angle).
     * @param aAltitude The altitude angle.
     * @param aRoll     The roll angle.
     * @throws ZeroLengthVectorException Thrown if the transformation is singular and cannot be uniquely decomposed.
     */
    public void decompose(final Vector3f scale, final Vector3f shear, final Vector3f translate, final AngleF aAzimuth,
                          final AngleF aAltitude, final AngleF aRoll) throws ZeroLengthVectorException {
        final float fSx;
        final float fSy;
        final float fSz;
        // first back out the translation part of the transform
        translate.i = xfm[0][3];
        translate.j = xfm[1][3];
        translate.k = xfm[2][3];
        // this is the easiest way to visualize what is happening - think of passing the axis vectors (1,0,0),
        //  (0,1,0), (0,0,1) through this transform.  This produces the transformed axis system i',j',k' (almost).
        //  If there is no scale or shear, we can look at the rotations of these axis and build the rotation
        //  angles.  If there is scale and shear, we need to compute  and undo those before we compute the
        //  rotation angles.
        final Vector3f vXrot = new Vector3f(xfm[0][0], xfm[1][0], xfm[2][0]);
        final Vector3f vYxfm = new Vector3f(xfm[0][1], xfm[1][1], xfm[2][1]);
        final Vector3f vZxfm = new Vector3f(xfm[0][2], xfm[1][2], xfm[2][2]);
        fSx = vXrot.getLength();
        vXrot.normalize();
        // Assume X transforms to i'.  Y transforms to be on the i'j' plane - though if there was shear, it may not be
        //  perpendicular to i' and the shear factored in.  the transformed k needs to be scaled and sheared to be
        //  perpendicular to the i'j' plane and of length 1
        final Vector3f vZrot = new Vector3f(vXrot).cross(vYxfm).normalize();   // true k'
        final Vector3f vYrot = new Vector3f(vZrot).cross(vXrot);                   // true j'
        // We can use the dot products between the true i'
        fSy = vYxfm.dot(vYrot);
        fSz = vZxfm.dot(vZrot);
        if (null != scale) {
            scale.i = fSx;
            scale.j = fSy;
            scale.k = fSz;
        }
        if (PackageConstants.isZero(fSy) || PackageConstants.isZero(fSz)) {
            throw new ZeroLengthVectorException();
        }
        if (null != shear) {
            shear.i = vXrot.dot(vYxfm) / fSy;
            shear.j = vXrot.dot(vZxfm) / fSz;
            shear.k = vYrot.dot(vZxfm) / fSz;
        }
        // work from the rotated coordinate system to derive the rotations that would get me there
        if (PackageConstants.isZero(vYrot.k - 1.0f)) {
            // the Y axis has been rotated to be coincident with the +Z (pointing up)
            aAltitude.setDegrees(90.0f);
            aAzimuth.atan2(-vXrot.j, vXrot.i);
        } else if (PackageConstants.isZero(vYrot.k + 1.0f)) {
            // the Y axis has been rotated to be coincident with the -Z (pointing down)
            aAltitude.setDegrees(-90.0f);
            aAzimuth.atan2(-vXrot.j, vXrot.i);
        } else if (PackageConstants.isZero(vXrot.k - 1.0f)) {
            // the X axis has been rotated to be coincident with the +Z (pointing up)
            aRoll.setDegrees(-90.0f);
            aAzimuth.atan2(vYrot.i, vYrot.j);
        } else if (PackageConstants.isZero(vXrot.k + 1.0f)) {
            // the X axis has been rotated to be coincident with the -Z (pointing down)
            aRoll.setDegrees(90.0f);
            aAzimuth.atan2(vYrot.i, vYrot.j);
        } else {
            aRoll.atan2(-vXrot.k, vZrot.k);
            aAltitude.asin(vYrot.k);
            if (Math.abs(vXrot.k) < Math.abs(vYrot.k)) {
                aAzimuth.atan2(-vXrot.j, vXrot.i);
            } else {
                aAzimuth.atan2(vYrot.i, vYrot.j);
            }
        }
    }

    public final void transpose() {
        float temp;
        for (int row=0; row<4; row++) {
            for (int col = row + 1; col < 4; col++) {
                temp = this.xfm[row][col];
                this.xfm[row][col] = this.xfm[col][row];
                this.xfm[col][row] = temp;
            }
        }
    }

    /**
     * General invert routine.  Inverts m1 and places the result in "this".
     * Note that this routine handles both the "this" version and the
     * non-"this" version.
     * <p>
     * Also note that since this routine is slow anyway, we won't worry
     * about allocating a little bit of garbage.
     *
     * @param xfm (readonly) The transformation to be inverted into this transformation.
     */
    private void invertGeneral(final @NotNull Xfm4x4f xfm) {
        final double[] temp = new double[16];
        final double[] result = new double[16];
        final int[] row_perm = new int[4];
        int i;
        // Use LU decomposition and back-substitution code specifically
        // for floating-point 4x4 matrices.

        // Copy source matrix to t1tmp
        temp[0] = xfm.xfm[0][0];
        temp[1] = xfm.xfm[0][1];
        temp[2] = xfm.xfm[0][2];
        temp[3] = xfm.xfm[0][3];

        temp[4] = xfm.xfm[1][0];
        temp[5] = xfm.xfm[1][1];
        temp[6] = xfm.xfm[1][2];
        temp[7] = xfm.xfm[1][3];

        temp[8] = xfm.xfm[2][0];
        temp[9] = xfm.xfm[2][1];
        temp[10] = xfm.xfm[2][2];
        temp[11] = xfm.xfm[2][3];

        temp[12] = xfm.xfm[3][0];
        temp[13] = xfm.xfm[3][1];
        temp[14] = xfm.xfm[3][2];
        temp[15] = xfm.xfm[3][3];

        // Calculate LU decomposition: Is the matrix singular?
        if (!luDecomposition(temp, row_perm)) {
            // Matrix has no inverse
            throw new SingularMatrixException();
        }

        // Perform back substitution on the identity matrix
        for (i = 0; i < 16; i++) result[i] = 0.0;
        result[0] = 1.0;
        result[5] = 1.0;
        result[10] = 1.0;
        result[15] = 1.0;
        luBackSubstitution(temp, row_perm, result);

        this.xfm[0][0] = (float) result[0];
        this.xfm[0][1] = (float) result[1];
        this.xfm[0][2] = (float) result[2];
        this.xfm[0][3] = (float) result[3];

        this.xfm[1][0] = (float) result[4];
        this.xfm[1][1] = (float) result[5];
        this.xfm[1][2] = (float) result[6];
        this.xfm[1][3] = (float) result[7];

        this.xfm[2][0] = (float) result[8];
        this.xfm[2][1] = (float) result[9];
        this.xfm[2][2] = (float) result[10];
        this.xfm[2][3] = (float) result[11];

        this.xfm[3][0] = (float) result[12];
        this.xfm[3][1] = (float) result[13];
        this.xfm[3][2] = (float) result[14];
        this.xfm[3][3] = (float) result[15];
    }

    /**
     * Given a 4x4 array "matrix0", this function replaces it with the
     * LU decomposition of a row-wise permutation of itself.  The input
     * parameters are "matrix0" and "row_perm".  The array "matrix0" is also
     * an output parameter.  The vector "row_perm[4]" is an output
     * parameter that contains the row permutations resulting from partial
     * pivoting.  The output parameter "even_row_xchg" is 1 when the
     * number of row exchanges is even, or -1 otherwise.  Assumes data
     * type is always double.
     * <p>
     * This function is similar to luDecomposition, except that it
     * is tuned specifically for 4x4 matrices.
     *
     * @param matrix0  (modified) The matrix being decomposed.
     * @param row_perm (modified) The row permutation array (where the original rows moved)
     * @return <tt>true</tt> if the decomposition was successful, false otherwise.
     */
    //
    // Reference: Press, Flannery, Teukolsky, Vetterling,
    //        _Numerical_Recipes_in_C_, Cambridge University Press,
    //        1988, pp 40-45.
    //
    private static boolean luDecomposition(final @NotNull double[] matrix0,
                                           final @NotNull int[] row_perm) {

        final double[] row_scale = new double[4];

        // Determine implicit scaling information by looping over rows
        {
            int i, j;
            int ptr, rs;
            double big, temp;

            ptr = 0;
            rs = 0;

            // For each row ...
            i = 4;
            while (i-- != 0) {
                big = 0.0;

                // For each column, find the largest element in the row
                j = 4;
                while (j-- != 0) {
                    temp = matrix0[ptr++];
                    temp = Math.abs(temp);
                    if (temp > big) {
                        big = temp;
                    }
                }

                // Is the matrix singular?
                if (big == 0.0) {
                    return false;
                }
                row_scale[rs++] = 1.0 / big;
            }
        }

        {
            int j;
            final int mtx;

            mtx = 0;

            // For all columns, execute Crout's method
            for (j = 0; j < 4; j++) {
                int i, imax, k;
                int target, p1, p2;
                double sum, big, temp;

                // Determine elements of upper diagonal matrix U
                for (i = 0; i < j; i++) {
                    target = mtx + (4 * i) + j;
                    sum = matrix0[target];
                    k = i;
                    p1 = mtx + (4 * i);
                    p2 = mtx + j;
                    while (k-- != 0) {
                        sum -= matrix0[p1] * matrix0[p2];
                        p1++;
                        p2 += 4;
                    }
                    matrix0[target] = sum;
                }

                // Search for largest pivot element and calculate
                // intermediate elements of lower diagonal matrix L.
                big = 0.0;
                imax = -1;
                for (i = j; i < 4; i++) {
                    target = mtx + (4 * i) + j;
                    sum = matrix0[target];
                    k = j;
                    p1 = mtx + (4 * i);
                    p2 = mtx + j;
                    while (k-- != 0) {
                        sum -= matrix0[p1] * matrix0[p2];
                        p1++;
                        p2 += 4;
                    }
                    matrix0[target] = sum;

                    // Is this the best pivot so far?
                    if ((temp = row_scale[i] * Math.abs(sum)) >= big) {
                        big = temp;
                        imax = i;
                    }
                }

                if (imax < 0) {
                    throw new RuntimeException();
                }

                // Is a row exchange necessary?
                if (j != imax) {
                    // Yes: exchange rows
                    k = 4;
                    p1 = mtx + (4 * imax);
                    p2 = mtx + (4 * j);
                    while (k-- != 0) {
                        temp = matrix0[p1];
                        matrix0[p1++] = matrix0[p2];
                        matrix0[p2++] = temp;
                    }

                    // Record change in scale factor
                    row_scale[imax] = row_scale[j];
                }

                // Record row permutation
                row_perm[j] = imax;

                // Is the matrix singular
                if (matrix0[(mtx + (4 * j) + j)] == 0.0) {
                    return false;
                }

                // Divide elements of lower diagonal matrix L by pivot
                if (j != (4 - 1)) {
                    temp = 1.0 / (matrix0[(mtx + (4 * j) + j)]);
                    target = mtx + (4 * (j + 1)) + j;
                    i = 3 - j;
                    while (i-- != 0) {
                        matrix0[target] *= temp;
                        target += 4;
                    }
                }
            }
        }

        return true;
    }

    /**
     * Solves a set of linear equations.  The input parameters "matrix1",
     * and "row_perm" come from luDecompostionD4x4 and do not change
     * here.  The parameter "matrix2" is a set of column vectors assembled
     * into a 4x4 matrix of floating-point values.  The procedure takes each
     * column of "matrix2" in turn and treats it as the right-hand side of the
     * matrix equation Ax = LUx = b.  The solution vector replaces the
     * original column of the matrix.
     * <p>
     * If "matrix2" is the identity matrix, the procedure replaces its contents
     * with the inverse of the matrix from which "matrix1" was originally
     * derived.
     *
     * @param matrix1 (readonly) The decomposed matrix
     * @param row_perm (readonly) Th row permutation array.
     * @param matrix2 (modified) and identity matrix.
     */
    //
    // Reference: Press, Flannery, Teukolsky, Vetterling,
    //        _Numerical_Recipes_in_C_, Cambridge University Press,
    //        1988, pp 44-45.
    //
    private static void luBackSubstitution(final double[] matrix1,
                                           final int[] row_perm,
                                           final double[] matrix2) {
        int i, ii, ip, j, k;
        final int rp;
        int cv, rv;

        //  rp = row_perm;
        rp = 0;

        // For each column vector of matrix2 ...
        for (k = 0; k < 4; k++) {
            //      cv = &(matrix2[0][k]);
            cv = k;
            ii = -1;

            // Forward substitution
            for (i = 0; i < 4; i++) {
                double sum;

                ip = row_perm[rp + i];
                sum = matrix2[cv + 4 * ip];
                matrix2[cv + 4 * ip] = matrix2[cv + 4 * i];
                if (ii >= 0) {
                    //          rv = &(matrix1[i][0]);
                    rv = i * 4;
                    for (j = ii; j <= i - 1; j++) {
                        sum -= matrix1[rv + j] * matrix2[cv + 4 * j];
                    }
                } else if (sum != 0.0) {
                    ii = i;
                }
                matrix2[cv + 4 * i] = sum;
            }

            // Back substitution
            //      rv = &(matrix1[3][0]);
            rv = 3 * 4;
            matrix2[cv + 4 * 3] /= matrix1[rv + 3];

            rv -= 4;
            matrix2[cv + 4 * 2] = (matrix2[cv + 4 * 2] -
                    matrix1[rv + 3] * matrix2[cv + 4 * 3]) / matrix1[rv + 2];

            rv -= 4;
            matrix2[cv + 4 * 1] = (matrix2[cv + 4 * 1] -
                    matrix1[rv + 2] * matrix2[cv + 4 * 2] -
                    matrix1[rv + 3] * matrix2[cv + 4 * 3]) / matrix1[rv + 1];

            rv -= 4;
            matrix2[cv + 4 * 0] = (matrix2[cv + 4 * 0] -
                    matrix1[rv + 1] * matrix2[cv + 4 * 1] -
                    matrix1[rv + 2] * matrix2[cv + 4 * 2] -
                    matrix1[rv + 3] * matrix2[cv + 4 * 3]) / matrix1[rv + 0];
        }
    }

    /**
     * Inverts this transform and returns the result in this transformation.
     * @return Returns this transformation.
     */
    public @NotNull Xfm4x4f invert() {
        invertGeneral(this);
        return this;
    }

    /**
     *
     * @param inverse
     * @return
     */
    public @NotNull Xfm4x4f invert(@NotNull Xfm4x4f inverse) {
        inverse.invertGeneral(this);
        return inverse;
    }

    /**
     * Pre-multiply this transformation by another transformation..  Pre-transformation is used when I already have a
     * transformation that locates an object and I want to apply additional transformation operators to the object.  This
     * often happens during interactive environment editing.
     *
     * @param xfm The transformation which will be pre-multiplied with this transformation.
     * @return Returns this transformation after pre-multiplication with <tt>xfm</tt>
     */
    public Xfm4x4f preMul(final Xfm4x4f xfm) {
        final Xfm4x4f xfmTmp = new Xfm4x4f(this);
        for (int row=0; row<4; row++) {
            for (int col=0; col < 4; col++) {
                this.xfm[row][col] = 0.0f;
                for (int i=0; i<4; i++) {
                    this.xfm[row][col] += (xfm.xfm[row][i] * xfmTmp.xfm[i][col]);
                }
            }
        }
        return this;
    }

    /**
     *
     * @param srs00
     * @param srs01
     * @param srs02
     * @param srs10
     * @param srs11
     * @param srs12
     * @param srs20
     * @param srs21
     * @param srs22
     * @return
     */
    private Xfm4x4f preMul(final float srs00, final float srs01, final float srs02,
                           final float srs10, final float srs11, final float srs12,
                           final float srs20, final float srs21, final float srs22) {
        // This is a local pre-multiply by the scale-rotation-shear 3x3 transform.  It is used to minimize wasted work and to
        //  eliminate the need to borrow an intermediate transformation when performing a scale, rotate, or shear operation.
        float c0, c1, c2;     // the temporary column - we process this transform by column - these are the untransformed
        // state of the column
        for (int col=0; col < 3; col++) {
            c0 = xfm[0][col];
            c1 = xfm[1][col];
            c2 = xfm[2][col];
            xfm[0][col] = (srs00 * c0) + (srs01 * c1) + (srs02 * c2);
            xfm[1][col] = (srs10 * c0) + (srs11 * c1) + (srs12 * c2);
            xfm[2][col] = (srs20 * c0) + (srs21 * c1) + (srs22 * c2);

        }
        return this;
    }

    /**
     * Transform a point in place.
     *
     * @param pt (Point3f, modified) The point to be transformed by this transformation.
     * @return Returns the transformed point <tt>pt</tt>.
     */
    public Point3f transform(final Point3f pt) {
        return transform(pt, pt);
    }

    /**
     * Transform a point into a target point.
     *
     * @param pt (Point3f, readonly) The point to be transformed.
     * @param xfmPt (Point3f, modified) The point to receive the transformed point (the target point).
     * @return Returns the transformed point, <tt>xfmPt</tt>.
     */
    public Point3f transform(final Point3f pt, final Point3f xfmPt) {
        return xfmPt.setValue(
                (xfm[0][0] * pt.x) + (xfm[0][1] * pt.y) + (xfm[0][2] * pt.z) + xfm[0][3],
                (xfm[1][0] * pt.x) + (xfm[1][1] * pt.y) + (xfm[1][2] * pt.z) + xfm[1][3],
                (xfm[2][0] * pt.x) + (xfm[2][1] * pt.y) + (xfm[2][2] * pt.z) + xfm[2][3]);
    }

    /**
     * Transform an array of points in place.
     *
     * @param pts
     * @return
     */
    public Point3f[] transform(final Point3f[] pts) {
        for (int iPt = pts.length; --iPt >= 0; ) {
            final Point3f pt = pts[iPt];
            transform(pt, pt);
        }
        return pts;
    }

    /**
     * Transform an vector in place.
     *
     * @param v (Vector3f, modified) The vector to be transformed by this transformation.
     * @return Returns the transformed vector <tt>v</tt>.
     */
    public Vector3f transform(final Vector3f v) {
        return transform(v, v);
    }

    /**
     * Transform an vector into a target vector.
     *
     * @param v (Vector3f, readonly) The vector to be transformed.
     * @param xfmV (Vector3f, modified) The vector to receive the transformed vector (the target vector).
     * @return Returns the transformed vector, <tt>xfmV</tt>.
     */
    public Vector3f transform(final Vector3f v, final Vector3f xfmV) {
        return xfmV.setValue(
                (xfm[0][0] * v.i) + (xfm[0][1] * v.j) + (xfm[0][2] * v.k),
                (xfm[1][0] * v.i) + (xfm[1][1] * v.j) + (xfm[1][2] * v.k),
                (xfm[2][0] * v.i) + (xfm[2][1] * v.j) + (xfm[2][2] * v.k));
    }

    /**
     * Transform an array of vectors into a target vector array.
     *
     * @param vs (Vector3f[], readonly) An array of vectors to be transformed.
     * @param xfmVs (Vector3f[], modified) An array of vectors to receive the transformed vectors.
     * @return Returns the transformed vector array, <tt>xfmVs</tt>.
     */
    public Vector3f[] transform(final Vector3f[] vs,  final Vector3f[] xfmVs) {
        for (int iV = vs.length; --iV >= 0; ) {
            transform(vs[iV], xfmVs[iV]);
        }
        return xfmVs;
    }
}
