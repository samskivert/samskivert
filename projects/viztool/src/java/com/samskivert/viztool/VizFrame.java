//
// $Id: VizFrame.java,v 1.4 2001/08/14 00:04:08 mdb Exp $
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

import java.awt.BorderLayout;
import javax.swing.*;

import com.samskivert.viztool.viz.HierarchyVisualizer;

/**
 * The top-level frame in which visualizations are displayed.
 */
public class VizFrame extends JFrame
{
    public VizFrame (HierarchyVisualizer viz)
    {
        super("viztool");

        // quit if we're closed
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel content = new JPanel();

        // put a border around everything
        content.setBorder(BorderFactory.createEmptyBorder(
            BORDER, BORDER, BORDER, BORDER));

        VizPanel panel = new VizPanel(viz);
        content.add(panel, BorderLayout.CENTER);

        setContentPane(content);
    }

    protected static final int BORDER = 5; // pixels
}
