//
// $Id: Spacer.java,v 1.2 2003/04/01 11:04:18 ray Exp $

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
