//
// $Id: Spacer.java,v 1.1 2002/04/30 02:31:09 mdb Exp $

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
        setPreferredSize(new Dimension(width, height));
        setOpaque(false);
    }
}
