//
// $Id: SwingUtil.java,v 1.2 2001/07/25 23:50:22 mdb Exp $

package com.samskivert.swing.util;

import java.awt.*;

/**
 * Miscellaneous useful Swing-related utility functions.
 */
public class SwingUtil
{
    /**
     * Center the given window within the screen boundaries.
     *
     * @param window the window to be centered.
     */
    public static void centerWindow (Window window)
    {
        Toolkit tk = window.getToolkit();
        Dimension ss = tk.getScreenSize();
        int width = window.getWidth(), height = window.getHeight();
        window.setBounds((ss.width-width)/2, (ss.height-height)/2,
                         width, height);
    }
}
