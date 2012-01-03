//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.swing;

import java.awt.Dimension;
import javax.swing.JPanel;

/**
 * A widget whose sole purpose is to introduce blank space between other
 * widgets. A sorry lot, but he gets the job done.
 */
public class Spacer extends JPanel
{
    /**
     * Constructs a spacer with the specified width and height.
     */
    public Spacer (int width, int height)
    {
        this(new Dimension(width, height));
    }

    /**
     * Constructs a spacer with the specified width and height.
     */
    public Spacer (Dimension d)
    {
        setPreferredSize(d);
        setOpaque(false);
    }
}
