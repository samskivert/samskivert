//
// $Id: VizFrame.java,v 1.6 2001/11/30 22:57:31 mdb Exp $
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

import com.samskivert.swing.*;

/**
 * The top-level frame in which visualizations are displayed.
 */
public class VizFrame extends JFrame
{
    public VizFrame (Visualizer viz)
    {
        super("viztool");

        // quit if we're closed
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // create our controller and panel for displaying visualizations
        VizPanel vpanel = new VizPanel(viz);
        VizController vctrl = new VizController(vpanel);

        // create some control buttons
        GroupLayout gl = new HGroupLayout(GroupLayout.NONE);
        gl.setJustification(GroupLayout.RIGHT);
        JPanel bpanel = new JPanel(gl);
        JButton btn;

        btn = new JButton("Print");
        btn.setActionCommand(VizController.PRINT);
        btn.addActionListener(VizController.DISPATCHER);
        bpanel.add(btn);

        btn = new JButton("Previous page");
        btn.setActionCommand(VizController.BACKWARD_PAGE);
        btn.addActionListener(VizController.DISPATCHER);
        bpanel.add(btn);

        btn = new JButton("Next page");
        btn.setActionCommand(VizController.FORWARD_PAGE);
        btn.addActionListener(VizController.DISPATCHER);
        bpanel.add(btn);

        btn = new JButton("Quit");
        btn.setActionCommand(VizController.QUIT);
        btn.addActionListener(VizController.DISPATCHER);
        bpanel.add(btn);

        // create a content pane to contain everything
        JPanel content = new ContentPanel(vctrl);
        gl = new VGroupLayout(GroupLayout.STRETCH);
        content.setLayout(gl);
        content.setBorder(BorderFactory.createEmptyBorder(
            BORDER, BORDER, BORDER, BORDER));
        content.add(vpanel);
        content.add(bpanel, GroupLayout.FIXED);
        setContentPane(content);
    }

    protected static final class ContentPanel
        extends JPanel
        implements ControllerProvider
    {
        public ContentPanel (VizController ctrl)
        {
            _ctrl = ctrl;
        }

        public Controller getController ()
        {
            return _ctrl;
        }

        protected Controller _ctrl;
    }

    protected static final int BORDER = 5; // pixels
}
