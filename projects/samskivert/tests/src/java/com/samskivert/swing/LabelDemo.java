//
// $Id: LabelDemo.java,v 1.7 2002/11/13 23:17:26 mdb Exp $

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
        _labels[idx] = new Label(text);
        _labels[idx++].setFont(font);

        _labels[idx] = new Label(text);
        _labels[idx].setFont(font);
        _labels[idx].setTargetWidth(100);
        _labels[idx].setAlignment(Label.RIGHT);
        _labels[idx].setAlternateColor(Color.white);
        _labels[idx++].setFont(new Font("Dialog", Font.PLAIN, 12));

        _labels[idx] = new Label(text);
        _labels[idx].setFont(font);
        _labels[idx].setTargetHeight(30);
        _labels[idx].setAlignment(Label.CENTER);
        _labels[idx].setAlternateColor(Color.white);
        _labels[idx++].setFont(new Font("Dialog", Font.PLAIN, 12));

        _labels[idx] = new Label(text);
        _labels[idx].setFont(font);
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
            case 1: g2.fillRect(x, y, 100, size.height);
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
        // lay out label zero if necessary
        int width = _labels[0].getSize().width;
        if (width == 0) {
            Graphics2D g = (Graphics2D)getGraphics();
            _labels[0].layout(g);
            width = _labels[0].getSize().width;
        }

        return new Dimension(width + 20, 300);
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
