/*
 * RenderWindow.java
 *
 * Created on September 11, 2002, 6:56 PM
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

import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * This is the main for window-centric rendering.  It provides the infrastructure to setup an on-screen window and manage
 * paint/repaint, resizing, closing, etc.  This can be used as the container for essentially all CSE581 class assignments.
 * <p>
 * The meat of the assignment is provided by a rendering component that is dynamically loaded when this container is run.  The
 * rendering component must implement the {@link IRenderScene} interface.
 * <p>
 * <b>Usage:</b>
 * <pre>
 *     RenderWindow &lt;-r <i>IRenderSceneImpl</i>&gt; &lt;-d <i>sceneDescFile</i>&gt;
 * </pre>
 * <table border="0" width="90%">
 * <caption style="text-align:left">where:</caption>
 * <tr>
 * <td style="width:5%"></td>
 * <td><table border="1" summary="">
 * <tr>
 * <td style="width:5%" border="1">-r</td>
 * <td>Specify the implementation of the {@link IRenderScene} interface, <i>IRenderSceneImpl</i>,
 * that should be loaded for rendering.  This is loaded into the <i>m_strRenderSceneImpl</i> field.  If you want to setup the
 * container to default to the implementation you are currently working on so you can easily run things in the debugger of the
 * dev environment without worrying about the arguments to the container, set this field to your class when the container is
 * constructed.
 * <p>
 * This container uses only the <tt>loadScene</tt> and <tt>renderScene</tt> methods of the
 * {@link IRenderScene} interface.
 * </td>
 * </tr>
 * <tr>
 * <td style="width:5%">-d</td>
 * <td>Specify the scene description file, <i>sceneDescFile</i>, that should be loaded for rendering.  This string is loaded into
 * the <i>m_strRenderSceneDesc</i> field.  If you want to setup the container to default to a specific scene description
 * you are currently working on so you can easily run things in the debugger of the dev environment without worrying about
 * the arguments to this container, set this field to your scene description filename when the container is constructed.
 * <p>
 * The <tt>loadScene</tt> method of the renderer implementation is called only if a scene description file has been specified.
 * </td>
 * </tr>
 * </table>
 * </td>
 * </tr>
 * </table>
 *
 * @author royster.hall@gmail.com
 * @version 1.0
 * @see IRenderScene
 * @since fall 2002
 */
public class RenderWindow extends Frame implements ActionListener, WindowListener {
//    private int m_nSizeX = 1190;         // the initial X size of the app window (my preferred default, adjust to your needs).
//    private int m_nSizeY = 1216;         // the initial Y size of the app window (my preferred default, adjust to your needs).
    private int m_nSizeX = 565;         // the initial X size of the app window (for images for class website).
    private int m_nSizeY = 574;         // the initial Y size of the app window (for images for class website).
    private final GraphicsConfiguration m_graphicsConfig;       // the graphics configuration of the device the window is on
    private final IRenderScene m_renderScene;          // the renderer implementation
    private final MenuItem m_menuItem_File_Exit;   // the menu file-exit button
    private final MenuItem m_menuItem_File_Load;   // the menu file-load button
    private final MenuItem m_menuItem_File_Save;   // the menu file-save button
    private final JFileChooser m_dlgFileLoad = new JFileChooser();
    private final JFileChooser m_dlgFileSave = new JFileChooser();
    private final RenderCanvas m_canvas;               // the rendering canvas (defined at the end of this file)

    // The default implementation of the IRenderScene interface.  Set this to be the implementation you are
    //  currently working on if you want this to default to your implementation.
    private String m_strRenderSceneImpl = "cip.render.TestRenderScene";
    // The default scene description file.  Set this to be the scene you are currently working on if you
    //  want this to default to that scene.
    private String m_strRenderSceneDesc;

    /**
     * This is the <tt>main</tt> that starts the container.
     *
     * @param args The command line arguments - see usage notes.
     */
    public static void main(final String[] args) {
        String strRenderSceneImpl = null;
        String strRenderSceneDesc = null;
        // parse the commandline arguments
        for (int ix = 0; ix < args.length; ix++) {
            if (args[ix].equalsIgnoreCase("-r") && (ix < (args.length - 1))) {
                ix++;
                strRenderSceneImpl = args[ix];
            } else if (args[ix].equalsIgnoreCase("-d") && (ix < (args.length - 1))) {
                ix++;
                strRenderSceneDesc = args[ix];
            }
        }

        // start the rendering window
        try {
            final RenderWindow renderWindow = new RenderWindow(strRenderSceneImpl, strRenderSceneDesc);
            renderWindow.setVisible(true);
        } catch (final Throwable t) {
            t.printStackTrace();
            System.exit(0);
        }
    }

    /**
     * Creates a new instance of <tt>RenderWindow</tt>
     *
     * @param strRenderSceneImpl The {@link IRenderScene} implementation class to use for rendering.
     * @param strRenderSceneDesc The scene description file.
     * @throws Throwable Thrown if there is a problem during rendering.
     */
    private RenderWindow(final @Nullable String strRenderSceneImpl, final @Nullable String strRenderSceneDesc) throws Throwable {
        super("CSE581-render");
        if (null != strRenderSceneImpl) {
            m_strRenderSceneImpl = strRenderSceneImpl;
        }

        if (null != strRenderSceneDesc) {
            m_strRenderSceneDesc = strRenderSceneDesc;
            m_dlgFileLoad.setSelectedFile(new java.io.File(m_strRenderSceneDesc));
        }
        m_dlgFileLoad.setDialogTitle("Load Scene Environment");
        m_dlgFileLoad.setFileFilter(new MyFileFilter(".xml", "Scene Descriptions, *.xml"));

        m_dlgFileSave.setDialogTitle("Save As Image");
        m_dlgFileSave.setFileFilter(new MyFileFilter(".jpg", "Image Files, *.jpg"));
        m_dlgFileSave.setSelectedFile(new java.io.File("i.jpg"));

        // load the renderer and the scene to be rendered.
        try {
            m_renderScene = (IRenderScene) (Class.forName(m_strRenderSceneImpl).newInstance());
        } catch (ClassNotFoundException e) {
            System.out.println(String.format(
                    "Cannot render frame as requested, cannot load IRenderScene class %s", m_strRenderSceneImpl));
            throw e;
        }
        final long startTime = System.currentTimeMillis();
        if (null != m_strRenderSceneDesc) {
            m_renderScene.loadScene(m_strRenderSceneDesc);
        }
        String title = m_renderScene.getTitle();
        if (null != title) {
            this.setTitle(title);
        }
        System.out.println(String.format("Frame load time: %dms", (System.currentTimeMillis() - startTime)));

        //------------------------------------------------------------------
        // setup the window for rendering
        //------------------------------------------------------------------
        // resize the default if it doesn't fit in the full screen
        m_graphicsConfig = getGraphicsConfiguration();
        final Rectangle boundsRect = m_graphicsConfig.getBounds();
        if (boundsRect.width < m_nSizeX) m_nSizeX = boundsRect.width;
        if (boundsRect.height < m_nSizeY) m_nSizeY = boundsRect.height;
        setSize(m_nSizeX, m_nSizeY);

        //------------------------------------------------------------------
        // create the menu with exit
        //------------------------------------------------------------------
        final MenuBar menubar = new MenuBar();
        final Menu menuFile = new Menu("File");
        menubar.add(menuFile);
        m_menuItem_File_Exit = new MenuItem("Exit");
        m_menuItem_File_Exit.addActionListener(this);
        m_menuItem_File_Load = new MenuItem("Load Environment");
        m_menuItem_File_Load.addActionListener(this);
        m_menuItem_File_Save = new MenuItem("Save Image");
        m_menuItem_File_Save.addActionListener(this);
        menuFile.add(m_menuItem_File_Load);
        menuFile.add(m_menuItem_File_Save);
        menuFile.add(new MenuItem("-"));
        menuFile.add(m_menuItem_File_Exit);
        setMenuBar(menubar);
        // Since the world is so simple, this is the listener for all the window events
        addWindowListener(this);

        //------------------------------------------------------------------
        // create a canvas to draw on
        //------------------------------------------------------------------
        m_canvas = new RenderCanvas(m_graphicsConfig, m_renderScene);
        add(m_canvas, BorderLayout.CENTER);

    }

    //-------------------------------------------------------------------------------------------------------------------------
    // This is the thing that really closes the app - we dispose of the app window.  We do it either in response to the
    //  menu command or to the close button on the main frame.
    private void exitRenderWindow() {
        // closing things - dispose of the frame (window) and print object stats
        dispose();
    }

    //-------------------------------------------------------------------------------------------------------------------------
    private void loadEnvironment() {

        if (JFileChooser.APPROVE_OPTION == m_dlgFileLoad.showOpenDialog(this)) {
            System.out.println(String.format("Open environment dialog returned filename <%s%c%s>",
                    m_dlgFileLoad.getCurrentDirectory(), java.io.File.separatorChar ,m_dlgFileLoad.getSelectedFile().getName()));
            m_strRenderSceneDesc = m_dlgFileLoad.getCurrentDirectory().toString() + java.io.File.separatorChar
                    + m_dlgFileLoad.getSelectedFile().getName();
            try {
                final long startTime = System.currentTimeMillis();
                m_renderScene.loadScene(m_strRenderSceneDesc);
                System.out.println(String.format("Frame load time: %dms",System.currentTimeMillis() - startTime));
                m_canvas.repaint(0, 0, m_canvas.getWidth(), m_canvas.getHeight());
            } catch (final Throwable t) {
                System.out.println(String.format("error opening scene description <%s%c%s>", m_dlgFileLoad.getCurrentDirectory(),
                        java.io.File.separatorChar, m_dlgFileLoad.getSelectedFile().getName()));
                t.printStackTrace();
            }
        }
    }

    //-------------------------------------------------------------------------------------------------------------------------
    private void saveImage() {
        if (JFileChooser.APPROVE_OPTION == m_dlgFileSave.showSaveDialog(this)) {
            // generate a complete and valid filename
            String strFileName = m_dlgFileSave.getCurrentDirectory().toString() +
                    java.io.File.separatorChar + m_dlgFileSave.getSelectedFile().getName();
            final String strExt = MyFileFilter.getExtension(new java.io.File(strFileName));
            if ((null == strExt) || !strExt.equalsIgnoreCase(".jpg")) {
                strFileName += ".jpg";
            }
            System.out.println(String.format("Save image dialog returned filename <%s>", strFileName));
            // get the image - NOTE: it's really important that the format be TYPE_INT_RGB;
            final java.awt.image.BufferedImage bi = new java.awt.image.BufferedImage(
                    m_canvas.getWidth(), m_canvas.getHeight(), java.awt.image.BufferedImage.TYPE_INT_RGB);
            m_renderScene.renderImage(bi);
            // write the image to a jpg
            try {
                final java.io.File file = new java.io.File(strFileName);
                javax.imageio.ImageIO.write(bi, "jpg", file);
            } catch (final Throwable t) {
                System.out.println(String.format("Error writing image file <%s>",strFileName));
                t.printStackTrace();
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // ActionListener interface implementation                                                                               //
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Invoked when an action occurs.
     *
     * @param event The action.
     */
    @Override
    public void actionPerformed(final ActionEvent event) {
        final Object src = event.getSource();

        if (src == m_menuItem_File_Exit) {
            exitRenderWindow();
        } else if (src == m_menuItem_File_Load) {
            loadEnvironment();
        } else if (src == m_menuItem_File_Save) {
            saveImage();
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // WindowListener interface implementation                                                                               //
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // NOTE: The only thing in this interface that we are actually interested in is the windowClosing event which means that
    //  the user has clicked the close button.  All we need to do is dispose of the window so that the cleanup works properly
    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Ignored
     */
    @Override
    public void windowOpened(final WindowEvent windowEvent) {
    }

    //-------------------------------------------------------------------------------------------------------------------------
    @Override
    public void windowClosing(final WindowEvent windowEvent) {
        exitRenderWindow();
    }

    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Ignored
     */
    @Override
    public void windowClosed(final WindowEvent windowEvent) {
    }

    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Ignored
     */
    @Override
    public void windowActivated(final WindowEvent windowEvent) {
    }

    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Ignored
     */
    @Override
    public void windowDeactivated(final WindowEvent windowEvent) {
    }

    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Ignored
     */
    @Override
    public void windowIconified(final WindowEvent windowEvent) {
    }

    //-------------------------------------------------------------------------------------------------------------------------

    /**
     * Ignored
     */
    @Override
    public void windowDeiconified(final WindowEvent windowEvent) {
    }

}

class RenderCanvas extends Canvas {
    private class MouseHandler extends MouseAdapter {
        public void mousePressed(final MouseEvent event) {
            System.out.println(String.format("mouse at: %s", event.getPoint().toString()));
        }
    }

    private final IRenderScene m_renderScene;

    RenderCanvas(final GraphicsConfiguration gConfig, final IRenderScene renderScene) {
        super(gConfig);
        m_renderScene = renderScene;
        addMouseListener(new MouseHandler());
    }

    // Overrides the default paint and asks the renderer to paint the canvas
    public void paint(final Graphics g) {
        final long startTime = System.currentTimeMillis();
        m_renderScene.renderScene(this, g);
        System.out.println(String.format("Frame render time: %dms", (System.currentTimeMillis() - startTime)));
    }

}

/**
 * Provides the dialogue for picking a file to be written to
 */
class MyFileFilter extends javax.swing.filechooser.FileFilter {
    private final String m_strExt;
    private final String m_strDescription;

    MyFileFilter(final String strExt, final String strDescription) {
        m_strExt = strExt;
        m_strDescription = strDescription;
    }

    /*
     * Get the extension of a file.
     * @param f The file.
     */
    static String getExtension(final java.io.File f) {
        String ext = null;
        final String s = f.getName();
        final int i = s.lastIndexOf('.') - 1;

        if (i > 0 && i < s.length() - 1) {
            ext = s.substring(i + 1).toLowerCase();
        }
        return ext;
    }

    public boolean accept(final java.io.File f) {
        if (f.isDirectory()) {
            return true;
        }

        final String extension = getExtension(f);
        return (null != extension) && extension.equalsIgnoreCase(m_strExt);

    }

    public String getDescription() {
        return m_strDescription;
    }

}
