/*
 * TestRenderScene.java
 *
 * Created on September 15, 2002, 8:32 PM
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

import java.awt.*;

/**
 * This is a test {@link IRenderScene} implementation that simply paints a repeating horizontal red gradation overlaid with
 * a repeating vertical blue gradation.  The idea is just to make sure we know how to paint the pixels on the screen or
 * in the image.
 *
 * @author royster.hall@gmail.com
 * @version 1.0
 * @see IRenderScene
 * @since fall 2002
 */
@SuppressWarnings("unused")  // This class is dynamically instantiated.
public class TestRenderScene implements IRenderScene {

    /**
     * Creates a new instance of TestRenderScene
     */
    @SuppressWarnings("unused") // This class is dynamically instantiated.
    public TestRenderScene() {
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // IRenderScene interface implementation                                                                                 //
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // there is no required initialization.
    @Override
    public void loadScene(final String strSceneDesc) {
    }

    //-------------------------------------------------------------------------------------------------------------------------
    @Override
    public void renderImage(final Image image) {
    }

    //-------------------------------------------------------------------------------------------------------------------------
    @Override
    public void renderScene(final Component component, final Graphics gc) {
        // get the bounds of what need to be drawn on the hither plane
        final Rectangle rectCam = gc.getClipBounds();
        // the loop through the horizontal lines of the display
        for (int iy = rectCam.y; iy < rectCam.y + rectCam.height; iy++) {
            // the loop through the pixels in a line
            for (int ix = rectCam.x; ix < rectCam.x + rectCam.width; ix++) {
                // Set the color and drw the pixel.
                gc.setColor(new Color(ix % 255, (ix + iy) % 255, iy % 255));
                gc.drawRect(ix, iy, 0, 0);
            }
        }
    }

}
