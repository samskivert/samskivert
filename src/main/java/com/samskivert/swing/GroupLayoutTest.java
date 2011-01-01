//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2011 Michael Bayne, et al.
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.samskivert.swing;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class GroupLayoutTest
{
    public static void main (String[] args)
    {
        JFrame frame = new JFrame("GroupLayoutTest");
        JPanel panel = new JPanel();
        GroupLayout layout = new HGroupLayout();
        layout.setJustification(GroupLayout.CENTER);
        layout.setPolicy(GroupLayout.STRETCH);
        layout.setJustification(GroupLayout.RIGHT);
        layout.setGap(15);
        panel.setLayout(layout);

        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JButton butone = new JButton("One");
        panel.add(butone, GroupLayout.FIXED);
        JButton buttwo = new JButton("Two");
        panel.add(buttwo, GroupLayout.FIXED);
        JButton butthree = new JButton("Three to get ready");
        panel.add(butthree, GroupLayout.FIXED);

        frame.addWindowListener(new WindowAdapter () {
            @Override public void windowClosing (WindowEvent e) {
                System.exit(0);
            }
        });
        frame.getContentPane().add(panel, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
    }
}
