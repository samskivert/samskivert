//
// $Id: SwingUtil.java,v 1.1 2001/07/21 00:49:26 shaper Exp $

package com.samskivert.swing.util;

import java.awt.*;

/**
 * Miscellaneous useful Swing-related utility functions.
 */
public class SwingUtil
{
    /**
     * Center the given frame within the screen boundaries.
     *
     * @param frame the frame to be centered.
     */
    public static void centerFrame (Frame frame)
    {
        Toolkit tk = frame.getToolkit();
        Dimension ss = tk.getScreenSize();
        int width = frame.getWidth(), height = frame.getHeight();
        frame.setBounds((ss.width-width)/2, (ss.height-height)/2,
                        width, height);
    }
}
