//
// $Id: VizFrame.java,v 1.2 2001/08/12 03:59:21 mdb Exp $

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

        VizPanel panel = new VizPanel(viz);
        getContentPane().add(panel, BorderLayout.CENTER);
    }
}
