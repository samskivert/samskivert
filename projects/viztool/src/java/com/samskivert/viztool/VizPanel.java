//
// $Id: VizPanel.java,v 1.4 2001/08/13 23:43:09 mdb Exp $
// 
// viztool - a tool for visualizing collections of java classes
// Copyright (C) 2001 Michael Bayne
// 
// This program is free software; you can redistribute it and/or modify it
// under the terms of the GNU General Public License as published by the
// Free Software Foundation; either version 2.1 of the License, or (at your
// option) any later version.
// 
// This program is distributed in the hope that it will be useful, but
// WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// General Public License for more details.
// 
// You should have received a copy of the GNU General Public License along
// with this program; if not, write to the Free Software Foundation, Inc.,
// 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

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
        super.paintComponent(g);

        Graphics2D gfx = (Graphics2D)g;
        Rectangle2D bounds = getBounds();
        _viz.layout(gfx, 0, 0, bounds.getWidth(), bounds.getHeight());
        _viz.paint(gfx, 0);
    }

    protected HierarchyVisualizer _viz;
}
