/*
 * IRenderScene.java
 *
 * Created on September 9, 2002, 2:00 PM
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
 * This is the interface to components that render a scene.  These components load the scene data and render the scene
 * either to an {@link Image} or directly to a {@link Graphics} context for interactive display of the image as
 * it is being rendered.
 *
 * @author royster.hall@gmail.com
 * @version 1.0
 * @since 1.0
 */
public interface IRenderScene {
    /**
     * Load a scene from the specified scene description file.  The scene description is an XML file that describes the camera,
     * materials, objects, and lighting for a scene along with specifics about rendering the scene.
     *
     * @param strSceneDesc The scene description file.
     * @throws Exception The exception that may be thrown is specific to the loader. If an exception is thrown the caller is
     *      expected to try an create a meaningful message for the user, and then to exit.
     */
    void loadScene(String strSceneDesc) throws Exception;

    /**
     * Renders the scene, or a part of the scene, to the an {@link Image}.
     *
     * @param image The {@link Image} the scene should be rendered to.
     */
    void renderImage(Image image);

    /**
     * Renders the scene, or a part of the scene, to the {@link Graphics} context of a window.  The component provides the
     * pixel width and height of the drawing surface.  The clipBounds of the graphics context describe what part of the
     * component needs to be redrawn. If the component is re-sized, the clipBounds will be the component canvas.  If the
     * exposure of the window changes, the clipBounds may only
     * be a small part of the entire component.
     *
     * @param component The component the scene is being rendered into.
     * @param gc        The {@link Graphics} context of the component the scene should be rendered to.
     */
    void renderScene(Component component, Graphics gc);
}
