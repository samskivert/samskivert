//
// $Id: VizPanel.java,v 1.2 2001/08/12 03:59:21 mdb Exp $

package com.samskivert.viztool;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import javax.swing.*;

import com.samskivert.viztool.viz.HierarchyVisualizer;

/**
 * A very simple UI element for displaying visualizations on screen.
 */
public class VizPanel extends JPanel
{
    public VizPanel (HierarchyVisualizer viz)
    {
        _viz = viz;

        // set the font
        Font font = new Font("Courier", Font.PLAIN, 10);
        setFont(font);
    }

    public void paintComponent (Graphics g)
    {
        Graphics2D gfx = (Graphics2D)g;
        Rectangle2D bounds = getBounds();
        _viz.layout(gfx, 0, 0, bounds.getWidth(), bounds.getHeight());
        _viz.paint(gfx, 0);
    }

    protected HierarchyVisualizer _viz;
}
