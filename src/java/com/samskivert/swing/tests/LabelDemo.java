//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2007 Michael Bayne
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

package com.samskivert.swing.tests;

import java.awt.*;
import java.io.*;
import javax.swing.*;

import com.samskivert.swing.Label;

public class LabelDemo extends JPanel
{
    public LabelDemo ()
    {
        // create our labels
        String text = "The quick brown fox jumped over the lazy dog. " +
            "He then popped into the butcher's and picked up some mutton.";
        Font font = new Font("Courier", Font.PLAIN, 10);

        int idx = 0;
        _labels[idx] = new Label("\u307e\u305b\u3002Amores\u30d1\u30a4\u30e9");
        // _labels[idx].setStyle(Label.OUTLINE);
        _labels[idx].setAlternateColor(Color.green);
        _labels[idx].setFont(new Font("Dialog", Font.PLAIN, 10));
        _labels[idx++].setAlignment(Label.LEFT);

        _labels[idx] = new Label(text);
        // _labels[idx].setFont(font);
        _labels[idx].setTargetWidth(110);
        _labels[idx].setAlignment(Label.RIGHT);
        _labels[idx].setAlternateColor(Color.lightGray);
        _labels[idx].setStyle(Label.SHADOW);
        _labels[idx++].setFont(new Font("Dialog", Font.PLAIN, 12));

        text = "\u306e\u6d77\u306b\u884c\u3063\u3089\u3057\u3083\u3044\u307e" +
            "\u305b\u3002Periwinkle\u30d1\u30a4\u30e9\u30c8\u3092\u9078" +
            "\u3073\u51fa\u3057\u3066\u4e0b\u3055\u3044\u3002";

        _labels[idx] = new Label(text);
        // _labels[idx].setFont(font);
        _labels[idx].setTargetHeight(30);
        _labels[idx].setAlignment(Label.CENTER);
        _labels[idx].setAlternateColor(Color.lightGray);
        _labels[idx].setStyle(Label.BOLD);
        _labels[idx++].setFont(new Font("Dialog", Font.PLAIN, 12));

        _labels[idx] = new Label(text);
        // _labels[idx].setFont(font);
        _labels[idx].setFont(new Font("Dialog", Font.PLAIN, 11));
        _labels[idx++].setGoldenLayout();

        try {
            File file = new File("delarobb.TTF");
            if (file.exists()) {
                InputStream in = new FileInputStream(file);
                Font sfont = Font.createFont(Font.TRUETYPE_FONT, in);
                in.close();
                _labels[idx++] = new Label(String.valueOf(10), Label.OUTLINE,
                                           Color.pink, Color.black,
                                           sfont.deriveFont(Font.PLAIN, 24));
                _labels[idx++] = new Label(String.valueOf(30), Label.OUTLINE,
                                           Color.pink, Color.black,
                                           sfont.deriveFont(Font.PLAIN, 24));
            }
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

    public void doLayout ()
    {
        super.doLayout();

        // layout our labels
        Graphics2D g = (Graphics2D)getGraphics();
        for (int ii = 0; ii < _labels.length; ii++) {
            if (_labels[ii] != null) {
                _labels[ii].layout(g);
                System.out.println("l" + ii + ": " + _labels[ii].getSize());
            }
        }
    }

    public void paintComponent (Graphics g)
    {
        super.paintComponent(g);

        // render our labels
        Graphics2D g2 = (Graphics2D)g;
        Dimension size;
        int x = 10, y = 10;

        for (int ii = 0; ii < _labels.length; ii++) {
            if (_labels[ii] == null) {
                continue;
            }
            size = _labels[ii].getSize();
            g2.setColor(Color.white);
            switch (ii) {
            case 0: break;
            case 1: g2.fillRect(x, y, 110, size.height); break;
            case 2: g2.fillRect(x, y, size.width, 30); break;
            case 3: break;
            }

            g2.setColor(Color.gray);
            g2.fillRect(x, y, size.width, size.height);

            g2.setColor(Color.black);
            _labels[ii].render(g2, x, y);

            y += size.height + 10;
        }
    }

    public Dimension getPreferredSize ()
    {
        return new Dimension(400, 300);
    }

    public static void main (String[] args)
    {
        JFrame frame = new JFrame("Label Demo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        LabelDemo demo = new LabelDemo();
        frame.getContentPane().add(demo);
        frame.pack();
        frame.show();
    }

    protected Label[] _labels = new Label[10];
}
