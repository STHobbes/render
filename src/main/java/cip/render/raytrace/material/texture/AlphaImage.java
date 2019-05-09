/*
 * AlphaImage.java
 *
 * Created on January 8, 2017, 11:14 AM
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

import cip.render.raytrace.RayIntersection;
import cip.render.raytrace.interfaces.IRtBackground;
import cip.render.raytrace.interfaces.IRtGeometry;
import cip.render.raytrace.interfaces.IRtLight;
import cip.render.DynXmlObjParseException;
import cip.render.utilColour.RGBf;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

/**
 * This is a texture that uses 1 channel of an image as the alpha for mixing two materials
 */
public class AlphaImage extends ADualMaterialTexture {

    // The instance definition
    private String m_imageName = "./src/main/resources/rubota/rubota_r.jpg";
    private BufferedImage m_image = null;
    private int xRes;
    private int yRes;

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IDynXmlObject interface implementation                                                                                //
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    protected boolean lclProcessXmlElement(final @NotNull String elementTag, final @NotNull Element xmlElement,
                                           final @Nullable LinkedList refObjectList) throws DynXmlObjParseException {
        return lclProcessXmlElement(elementTag, xmlElement, refObjectList) ||
                lclParseAndLoadImage(elementTag, xmlElement);
    }
    private boolean lclParseAndLoadImage(final @NotNull String elementTag,
                                         final @NotNull Element xmlElement) throws DynXmlObjParseException {
        return false;
    }

    protected void lclValidate() throws DynXmlObjParseException {
        try {
            m_image = ImageIO.read(new File(m_imageName));
            xRes = m_image.getWidth();
            yRes = m_image.getHeight();
        } catch (final IOException e) {
            throw new DynXmlObjParseException(
                    String.format("Error Loading: class='%s', wraps:\n   %s", getClass().getName(), e.getMessage()), e);
        }

    }

    @Override
    public void toChildXmlElement(final @NotNull Element parentEl) {

    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IRtMaterial interface implementation                                                                                  //
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public void getColor(final @NotNull RGBf rgb, final @NotNull RayIntersection intersection,
                         final @NotNull IRtLight[] lights, final @NotNull IRtGeometry[] rtObjects,
                         final @NotNull IRtBackground rtBkg, final int nMaxRecursions, final int nSample, final int nRandom) {

        final RGBf rgb1 = intersection.borrowRGB();
        final RGBf rgb2 = intersection.borrowRGB();
        try {
            intersection.m_ptTexture.x -= (float) Math.floor(intersection.m_ptTexture.x);
            intersection.m_ptTexture.y -= (float) Math.floor(intersection.m_ptTexture.y);
            intersection.m_mtl = m_mtl1;
            intersection.m_mtl.getColor(rgb1, intersection, lights, rtObjects, rtBkg, nMaxRecursions, nSample, nRandom);
            intersection.m_mtl = m_mtl2;
            intersection.m_mtl.getColor(rgb2, intersection, lights, rtObjects, rtBkg, nMaxRecursions, nSample, nRandom);

            // need bilinear interpolation here - we can see the image pixels in the texture
            final int imageX = (int) (intersection.m_ptTexture.x * xRes);
            final int imageY = (int) ((1.0 - intersection.m_ptTexture.y) * yRes);
            final int imageRGBA = m_image.getRGB(imageX == xRes ? xRes - 1 : imageX, imageY == yRes ? yRes - 1 : imageY);
            final float alpha = (float)((imageRGBA & 0x00ff0000) >> 16) / 255.0f;
            rgb1.scale(alpha);
            rgb2.scale(1-alpha);
            rgb.setValue(rgb1).add(rgb2).scale(0.5f);
//            if ((imageRGBA & 0x00ff0000) < 0x007f0000) {
//                intersection.m_mtl = m_mtl2;
//            }
//
//            intersection.m_mtl.getColor(rgb, intersection, lights, rtObjects, rtBkg, nMaxRecursions, nSample, nRandom);
        } finally {
            intersection.returnRGB(rgb1);
            intersection.returnRGB(rgb2);
        }
    }

}
