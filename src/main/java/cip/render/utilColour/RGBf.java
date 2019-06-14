/*
 * RGBf.java
 *
 * Created on September 12, 2002, 4:01 PM
 * Copyright(c) 1993-2019 Crisis in Perspective, Inc.
 *                        PO Box 1949
 *                        Hood River, OR 97031
 *                        www.crisisinperspecive.com
 */
package cip.render.utilColour;

import org.w3c.dom.Element;

import java.util.StringTokenizer;

/**
 * A class representing an RGB colour of single precision (the components are represented by <tt>float</tt> values),
 * hence the name <tt>RGBf</tt>.
 * <p>
 * This class implements the basic functionality for an RGB colour as required for rendering and 3D graphics use.  This class is
 * patterned after and most code adapted from the <tt>CRGBf</tt> class of the <b><i>JOEY</i></b> toolkit written and
 * distributed by Crisis in Perspective, Inc.  Additionally, the normalized RGB sampling for spectral curves as described in
 * Hall, "Comparing Spectral Computation Methods", IEEE Computer Graphics and Applications, July 1999 is implemented as
 * part of this class.
 *
 * @author royster.hall@gmail.com
 * @version 1.0
 * @since 1.0
 */
public class RGBf {
    private static final String XML_ATTR_RGB = "rgb";
    private static final String XML_ATTR_SPECTRAL = "spectral";
    private static final String XML_ATTR_SCALE = "scale";
    private static final boolean DEBUG = true;

    // This is the raw data for the CIEXYZ curves taken from Judd and Wyszecki, Color in Business, Science, and Industry,
    //  (1975), table 2.6, these are for the 1931 standard observer with a 2-degree visual field.
    public static final float[][] s_fCIEXYZ = {
            {380, 0.0014f, 0.0000f, 0.0065f}, {385, 0.0022f, 0.0001f, 0.0105f},
            {390, 0.0042f, 0.0001f, 0.0201f}, {395, 0.0076f, 0.0002f, 0.0362f},
            {400, 0.0143f, 0.0004f, 0.0679f}, {405, 0.0232f, 0.0006f, 0.1102f},
            {410, 0.0435f, 0.0012f, 0.2074f}, {415, 0.0776f, 0.0022f, 0.3713f},
            {420, 0.1344f, 0.0040f, 0.6456f}, {425, 0.2148f, 0.0073f, 1.0391f},
            {430, 0.2839f, 0.0116f, 1.3856f}, {435, 0.3285f, 0.0168f, 1.6230f},
            {440, 0.3483f, 0.0230f, 1.7471f}, {445, 0.3481f, 0.0298f, 1.7826f},
            {450, 0.3362f, 0.0380f, 1.7721f}, {455, 0.3187f, 0.0480f, 1.7441f},
            {460, 0.2908f, 0.0600f, 1.6692f}, {465, 0.2511f, 0.0739f, 1.5281f},
            {470, 0.1954f, 0.0910f, 1.2876f}, {475, 0.1421f, 0.1126f, 1.0419f},
            {480, 0.0956f, 0.1390f, 0.8130f}, {485, 0.0580f, 0.1693f, 0.6162f},
            {490, 0.0320f, 0.2080f, 0.4652f}, {495, 0.0147f, 0.2586f, 0.3533f},
            {500, 0.0049f, 0.3230f, 0.2720f}, {505, 0.0024f, 0.4073f, 0.2123f},
            {510, 0.0093f, 0.5030f, 0.1582f}, {515, 0.0291f, 0.6082f, 0.1117f},
            {520, 0.0633f, 0.7100f, 0.0782f}, {525, 0.1096f, 0.7932f, 0.0573f},
            {530, 0.1655f, 0.8620f, 0.0422f}, {535, 0.2257f, 0.9149f, 0.0298f},
            {540, 0.2904f, 0.9540f, 0.0203f}, {545, 0.3597f, 0.9803f, 0.0134f},
            {550, 0.4334f, 0.9950f, 0.0087f}, {555, 0.5121f, 1.0000f, 0.0057f},
            {560, 0.5945f, 0.9950f, 0.0039f}, {565, 0.6784f, 0.9786f, 0.0027f},
            {570, 0.7621f, 0.9520f, 0.0021f}, {575, 0.8425f, 0.9154f, 0.0018f},
            {580, 0.9163f, 0.8700f, 0.0017f}, {585, 0.9786f, 0.8163f, 0.0014f},
            {590, 1.0263f, 0.7570f, 0.0011f}, {595, 1.0567f, 0.6949f, 0.0010f},
            {600, 1.0622f, 0.6310f, 0.0008f}, {605, 1.0456f, 0.5668f, 0.0006f},
            {610, 1.0026f, 0.5030f, 0.0003f}, {615, 0.9384f, 0.4412f, 0.0002f},
            {620, 0.8544f, 0.3810f, 0.0002f}, {625, 0.7514f, 0.3210f, 0.0001f},
            {630, 0.6424f, 0.2650f, 0.0000f}, {635, 0.5419f, 0.2170f, 0.0000f},
            {640, 0.4479f, 0.1750f, 0.0000f}, {645, 0.3608f, 0.1382f, 0.0000f},
            {650, 0.2835f, 0.1070f, 0.0000f}, {655, 0.2187f, 0.0816f, 0.0000f},
            {660, 0.1649f, 0.0610f, 0.0000f}, {665, 0.1212f, 0.0446f, 0.0000f},
            {670, 0.0874f, 0.0320f, 0.0000f}, {675, 0.0636f, 0.0232f, 0.0000f},
            {680, 0.0468f, 0.0170f, 0.0000f}, {685, 0.0329f, 0.0119f, 0.0000f},
            {690, 0.0227f, 0.0082f, 0.0000f}, {695, 0.0158f, 0.0057f, 0.0000f},
            {700, 0.0114f, 0.0041f, 0.0000f}, {705, 0.0081f, 0.0029f, 0.0000f},
            {710, 0.0058f, 0.0021f, 0.0000f}, {715, 0.0041f, 0.0015f, 0.0000f},
            {720, 0.0029f, 0.0010f, 0.0000f}, {725, 0.0020f, 0.0007f, 0.0000f},
            {730, 0.0014f, 0.0005f, 0.0000f}, {735, 0.0010f, 0.0004f, 0.0000f},
            {740, 0.0007f, 0.0002f, 0.0000f}, {745, 0.0005f, 0.0002f, 0.0000f},
            {750, 0.0003f, 0.0001f, 0.0000f}, {755, 0.0002f, 0.0001f, 0.0000f},
            {760, 0.0002f, 0.0001f, 0.0000f}, {765, 0.0001f, 0.0000f, 0.0000f},
            {770, 0.0001f, 0.0000f, 0.0000f}, {775, 0.0001f, 0.0000f, 0.0000f},
            {780, 0.0000f, 0.0000f, 0.0000f}};

    /**
     * A pointer to tne next <tt>RGBf</tt> in a cache if this object is cached.
     */
    public RGBf m_next = null;

    public float r;
    public float g;
    public float b;

    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Creates a new instance of <tt>RGBf</tt> initialized to 0,0,0.
     */
    public RGBf() {
    }

    /**
     * Creates a new instance of <tt>RGBf</tt> initialized to a specified colour.
     *
     * @param red   The red component.
     * @param green The green component.
     * @param blue  The blue component.
     */
    public RGBf(final float red, final float green, final float blue) {
        setValue(red, green, blue);
    }

    /**
     * Creates a new instance of <tt>RGBf</tt> initialized to a specified colour.
     *
     * @param rgbInit A colour that the newly instantiated colour should be set to.
     */
    @SuppressWarnings("CopyConstructorMissesField")
    public RGBf(final RGBf rgbInit) {
        setValue(rgbInit);
    }

    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Get the red component of this RGB colour.
     *
     * @return The red component of this RGB colour.
     */
    public float getRed() {
        return r;
    }

    /**
     * Set the red component of this RGB colour
     *
     * @param fRed The new red component.
     */
    public void setRed(final float fRed) {
        r = fRed;
    }

    /**
     * Get the green component of this RGB colour.
     *
     * @return The green component of this RGB colour.
     */
    public float getGreen() {
        return g;
    }

    /**
     * Set the green component of the RGB colour.
     *
     * @param fGreen The new green component.
     */
    public void setGreen(final float fGreen) {
        g = fGreen;
    }

    /**
     * Get the blue component of the RGB colour.
     *
     * @return The blue component of the RGB colour
     */
    public float getBlue() {
        return b;
    }

    /**
     * Set the blue component of the RGB colour.
     *
     * @param fBlue The new blue component.
     */
    public void setBlue(final float fBlue) {
        b = fBlue;
    }

    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Sets the components of this RGB colour as specified.
     *
     * @param red   The red component.
     * @param green The green component.
     * @param blue  The blue component.
     * @return Returns this RGB colour with the components set as specified.
     */
    public RGBf setValue(final float red, final float green, final float blue) {
        r = red;
        g = green;
        b = blue;
        return this;
    }

    /**
     * Sets this RGB colour equal to another RGB colour.
     *
     * @param rgb The RGB colour this colour will be set equal to.
     * @return Returns this RGB colour after its value has been set.
     */
    public RGBf setValue(final RGBf rgb) {
        r = rgb.r;
        g = rgb.g;
        b = rgb.b;
        return this;
    }

    /**
     * Sets this RGB colour to be a metamer for a spectral curve.
     *
     * @param strSpectralFile The file containing the spectral data.
     * @param bLight          <tt>true</tt> if this is a light source, <tt>false</tt> otherwise.  If <tt>true</tt>,
     *                        the RGB components are set per the CIEXYZ sampling and transformation into RGB.  If <tt>false</tt> the
     *                        sampling is normalized to be a reflectance value using the normalization that would result in a perfect mirror
     *                        having a RGB reflectance of 1,1,1
     * @return Returns this RGB colour after its value has been set.
     */
    public RGBf setValue(final String strSpectralFile, final boolean bLight) {
        return this;
    }

    /**
     * Sets the value of this RGB colour from the attributes of an XML DOM Element.  Colours are specified through
     * attributes so that the element tag can be used to identify how the colour should be applied - for example,
     * an object may have several colour components in the object definition.  The colour is specified either as an
     * RGB value, or, loaded from a spectral curve as follows:
     * <pre>
     *     <font style="color:blue">&lt;<i>someTag</i> rgb="<font style="color:magenta"><i>r,g,b</i></font>"
     *              scale="<font style="color:magenta"><i>scale</i></font>"/&gt;</font><br><br>
     * </pre>
     * or:
     * <pre>
     *     <font style="color:blue">&lt;<i>someTag</i> spectral="<font style="color:magenta"><i>spectralFileName</i></font>"
     *              scale="<font style="color:magenta"><i>scale</i></font>"/&gt;</font><br><br>
     * </pre>
     * <table border="0" width="90%">
     * <caption style="text-align:left">where:</caption>
     * <tr>
     * <td style="width:5%"></td>
     * <td><table border="1" summary="">
     * <tr>
     * <td><tt>rgb</tt></td>
     * <td>The colour as specified as an r,g,b value.  R, g, and b are normally specified in the range 0-1.
     * </td>
     * </tr>
     * <tr>
     * <td><tt>spectral</tt></td>
     * <td>The colour as specified in a spectral curve description.  The spectral curve will be sampled using
     * the CIEXYZ 1931 standard observer 2 degree FOV curves and then converted to RGB based on the
     * primary chromaticities used to initialize the RGBf class.
     * </td>
     * </tr>
     * <tr>
     * <td><tt>scale</tt></td>
     * <td>A multiplier for the colour or spectral curve.  Generally this would ony be used with spectral
     * curves for light sources which are normalized to an average intensity of 1 when they are read.  The
     * default is a scale of 1 (i.e. no scale is applied) if unspecified.
     * </td>
     * </tr>
     * </table>
     * </td>
     * </tr>
     * </table><br>
     *
     * @param xmlElement The XML DOM Element.
     * @param bLight     <tt>true</tt> if this is a light source, <tt>false</tt> otherwise.  <tt>bLight</tt> is used
     *                   only if the RGB colour is being initialized from spectral curve data, and it controls how the
     *                   CIEXYZ sampling to RGB is normalized.
     * @return Returns this RGB colour after its value has been set.
     */
    public RGBf setValue(final Element xmlElement, final boolean bLight) {
        // an RGB value is set either from a specified RGB tuple of a filename for a spectral curve.  Within the XML element
        //  these are specified as either an rgb attribute or a spectral attribute
        final String strRgb = xmlElement.getAttribute(XML_ATTR_RGB);
        final String strSpectral = xmlElement.getAttribute(XML_ATTR_SPECTRAL);
        if (((strRgb.equals("")) && (strSpectral.equals(""))) ||
                ((!strRgb.equals("")) && (!strSpectral.equals("")))) {
            throw new IllegalArgumentException("either an rgb or a spectral attribute is required to specify an RGB colour");
        }

        if (!strRgb.equals("")) {
            // initialize the rgb value from a string of the form red,green,blue
            final StringTokenizer tokens = new StringTokenizer(strRgb, ",");
            if (tokens.countTokens() != 3) {
                throw new IllegalArgumentException("rgb colour specification must be in the form \"red,green,blue\"");
            }
            r = Float.parseFloat(tokens.nextToken().trim());
            g = Float.parseFloat(tokens.nextToken().trim());
            b = Float.parseFloat(tokens.nextToken().trim());
        } else {
            // initialize the RGB from a spectral curve
            setValue(strSpectral, bLight);
        }

        final String strScale = xmlElement.getAttribute(XML_ATTR_SCALE);
        if (!strScale.equals("")) {
            final float fScale = Float.parseFloat(strScale.trim());
            scale(fScale);
        }
        return this;
    }

    /**
     * Adds the RGB definition attribute to the XML DOM element.
     *
     * @param element The element the colour attribute should be added to.
     */
    public void toXmlAttr(final Element element) {
        final StringBuilder strBuff = new StringBuilder(64).append(r).append(',').append(g).append(',').append(b);
        element.setAttribute(XML_ATTR_RGB, strBuff.substring(0));
    }

    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Scales this RGB colour by a scalar.  This is a common operation in illumination computations when scaling the
     * material reflectance based on the surface roughness.  Each of the components are multiplied by the scale factor.
     *
     * @param fScale The scale factor.
     * @return Returns the RGB colour after scaling.
     */
    public RGBf scale(final float fScale) {
        r *= fScale;
        g *= fScale;
        b *= fScale;
        return this;
    }

    /**
     * Add another RGB colour to this one.  This is a common operation in illumination when summing the illumination
     * contributions of multiple lights.  This is a component-wise addition of the colours.
     *
     * @param rgb The RGB colour that will be added to this one.
     * @return Returns this colour after addition.
     */
    public RGBf add(final RGBf rgb) {
        r += rgb.r;
        g += rgb.g;
        b += rgb.b;
        return this;
    }

    /**
     * Multiplies this RGB colour by another.  This is a common operation in illumination computations when multiplying the
     * incident light by the reflectance of a surface.  This is a component-wise multiplication of the colours.
     *
     * @param rgb The RGB colour that will be multiplied with this colour.
     * @return Returns this colour after multiplication.
     */
    public RGBf mult(final RGBf rgb) {
        r *= rgb.r;
        g *= rgb.g;
        b *= rgb.b;
        return this;
    }

    /**
     * Clamps the red, green, and blue colour components to be in the range 0.0 to 1.0.  NOTE that clamping in this context
     * means setting colour component &lt; 0.0 to 0.0 and any colour component &gt; 1.0 to 1.0.  A color clamped in this fashion
     * may change hue, saturation, and value.
     *
     * @return Returns this RGB colour after clamping.
     */
    public RGBf clamp() {
        if (r < 0.0f) {
            r = 0.0f;
        } else if (r > 1.0f) {
            r = 1.0f;
        }
        if (g < 0.0f) {
            g = 0.0f;
        } else if (g > 1.0f) {
            g = 1.0f;
        }
        if (b < 0.0f) {
            b = 0.0f;
        } else if (b > 1.0f) {
            b = 1.0f;
        }
        return this;
    }
    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Tests another RGB colour, <tt>rgb</tt>, for equality with this RGB colour.
     *
     * @param rgb The RGB colour to be tested.  This colour is unchanged.
     * @return Returns <tt>true</tt> if <tt>rgb</tt> is equal to this RGB colour(identical
     * in all components), and <tt>false</tt> otherwise.
     */
    public boolean equals(final RGBf rgb) {
        return this == rgb || (null != rgb && r == rgb.r && g == rgb.g && b == rgb.b);
    }
    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Tests another object, <tt>obj</tt>, for equality with this RGB colour.
     *
     * @param obj The object to be tested.  This colour is unchanged.
     * @return Returns <tt>true</tt> if <tt>obj</tt> is of class <tt>RGBf</tt> and is equal to this RGB colour (identical
     * in all components), and <tt>false</tt> otherwise.
     */
    public boolean equals(final Object obj) {
        if ((null == obj) ||
                (getClass() != obj.getClass())) {
            return false;
        }
        return equals((RGBf) obj);
    }

    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Clone this RGB colour.
     *
     * @return Returns a clone of this RGB colour.  The clone is NOT obtained from the object cache.
     */
    public Object clone() {
        return cloneRGBf();
    }

    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Clone this RGB colour.
     *
     * @return Returns a clone of this RGB colour.  The clone is NOT obtained from the object cache.
     */
    public RGBf cloneRGBf() {
        return new RGBf(r, g, b);
    }

}
