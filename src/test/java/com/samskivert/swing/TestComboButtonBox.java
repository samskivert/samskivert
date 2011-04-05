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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JFrame;

/**
 * Tests the image button box.
 */
public class TestComboButtonBox
{
    protected static Image createImage (Color color)
    {
        BufferedImage img = new BufferedImage(24, 24, BufferedImage.TYPE_INT_ARGB);
        Graphics g = img.getGraphics();
        g.setColor(color);
        g.fillRect(0, 0, 24, 24);
        g.dispose();
        return img;
    }

    public static void main (String[] args)
    {
        JFrame frame = new JFrame("Test ComboButtonBox");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        DefaultComboBoxModel model = new DefaultComboBoxModel();
        model.addElement(createImage(Color.blue));
        model.addElement(createImage(Color.green));
        model.addElement(createImage(Color.red));
        model.addElement(createImage(Color.yellow));

        ComboButtonBox box = new ComboButtonBox(ComboButtonBox.HORIZONTAL, model);
        frame.getContentPane().add(box);
        frame.pack();
        frame.setVisible(true);
    }
}
