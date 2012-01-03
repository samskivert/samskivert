//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

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
