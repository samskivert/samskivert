//
// $Id: GroupLayoutTest.java,v 1.1 2000/12/07 05:41:07 mdb Exp $

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
	layout.setGap(15);
	panel.setLayout(layout);

	JButton butone = new JButton("One");
	panel.add(butone, GroupLayout.FIXED);
	JButton buttwo = new JButton("Two");
	panel.add(buttwo, GroupLayout.FIXED);
	JButton butthree = new JButton("Three to get ready");
	panel.add(butthree, GroupLayout.FIXED);

        frame.addWindowListener(new WindowAdapter ()
	{
            public void windowClosing (WindowEvent e)
	    {
                System.exit(0);
            }
        });
        frame.getContentPane().add(panel, BorderLayout.CENTER);
	frame.pack();
	frame.setVisible(true);
    }
}
