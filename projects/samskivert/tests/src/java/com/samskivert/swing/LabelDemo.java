//
// $Id: LabelDemo.java,v 1.9 2003/12/09 04:07:31 mdb Exp $

package com.samskivert.swing;

import java.awt.*;
import java.io.*;
import javax.swing.*;

public class LabelDemo extends JPanel
{
    public LabelDemo ()
    {
        // create our labels
        String text = "The quick brown fox jumped over the lazy dog. " +
            "He then popped into the butcher's and picked up some mutton.";
        Font font = new Font("Courier", Font.PLAIN, 10);

        int idx = 0;
        _labels[idx] = new Label("Jealous Angelfish");
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

    public void layout ()
    {
        super.layout();

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
            case 1: g2.fillRect(x, y, 110, size.height);
            case 2: g2.fillRect(x, y, size.width, 30);
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
