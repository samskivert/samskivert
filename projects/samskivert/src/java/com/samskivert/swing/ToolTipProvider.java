//
// $Id: ToolTipProvider.java,v 1.1 2001/08/28 23:51:48 shaper Exp $

package com.samskivert.swing;

import java.awt.*;

/**
 * An interface to be implemented by objects that may have a tool tip
 * associated with themselves.
 */
public interface ToolTipProvider
{
    /**
     * Render a tool tip for this object to the given graphics context.
     *
     * @param g the graphics context.
     * @param x the x-position at which the tip should be drawn.
     * @param y the y-position at which the tip should be drawn.
     */
    public void paintToolTip (Graphics g, int x, int y);

    /**
     * Return the dimensions of the tool tip.
     *
     * @param g the graphics context to which the tip will be rendered.
     */
    public Dimension getToolTipSize (Graphics g);
}
