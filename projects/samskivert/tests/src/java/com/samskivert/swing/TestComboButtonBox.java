//
// $Id: TestComboButtonBox.java,v 1.1 2002/03/10 05:10:37 mdb Exp $

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
        BufferedImage img =
            new BufferedImage(24, 24, BufferedImage.TYPE_INT_ARGB);
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

        ComboButtonBox box =
            new ComboButtonBox(ComboButtonBox.HORIZONTAL, model);
        frame.getContentPane().add(box);
        frame.pack();
        frame.show();
    }
}
