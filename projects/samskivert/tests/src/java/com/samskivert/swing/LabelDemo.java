//
// $Id: LabelDemo.java,v 1.1 2001/12/19 07:02:50 mdb Exp $

package com.samskivert.swing;

import java.awt.*;
import javax.swing.*;

public class LabelDemo extends JPanel
{
    public LabelDemo ()
    {
        // create our labels
        String text = "The quick brown fox jumped over the lazy dog. " +
            "He then popped into the butcher's and picked up some mutton.";
        Font font = new Font("Courier", Font.PLAIN, 10);

        _labelZero = new Label(text);
        _labelZero.setFont(font);

        _labelOne = new Label(text);
        _labelOne.setFont(font);
        _labelOne.setTargetWidth(100);

        _labelTwo = new Label(text);
        _labelTwo.setFont(font);
        _labelTwo.setTargetHeight(30);
    }

    public void layout ()
    {
        super.layout();

        // layout our labels
        Graphics2D g = (Graphics2D)getGraphics();
        _labelZero.layout(g);
        System.out.println("l0: " + _labelZero.getSize());
        _labelOne.layout(g);
        System.out.println("l1: " + _labelOne.getSize());
        _labelTwo.layout(g);
        System.out.println("l2: " + _labelTwo.getSize());
    }

    public void paintComponent (Graphics g)
    {
        super.paintComponent(g);

        // render our labels
        Graphics2D g2 = (Graphics2D)g;
        Dimension size;
        int x = 10, y = 10;

        g2.setColor(Color.black);
        _labelZero.render(g2, x, y);
        g2.setColor(Color.red);
        size = _labelZero.getSize();
        g2.drawRect(x, y, size.width, size.height);

        y += 20;
        g2.setColor(Color.black);
        _labelTwo.render(g2, x, y);
        g2.setColor(Color.red);
        size = _labelTwo.getSize();
        g2.drawRect(x, y, size.width, size.height);
        g2.setColor(Color.blue);
        g2.drawRect(x, y, size.width, 30);

        y += 40;
        g2.setColor(Color.black);
        _labelOne.render(g2, x, y);
        g2.setColor(Color.red);
        size = _labelOne.getSize();
        g2.drawRect(x, y, size.width, size.height);
        g2.setColor(Color.blue);
        g2.drawRect(x, y, 100, size.height);
    }

    public Dimension getPreferredSize ()
    {
        // lay out label zero if necessary
        int width = _labelZero.getSize().width;
        if (width == 0) {
            Graphics2D g = (Graphics2D)getGraphics();
            _labelZero.layout(g);
            width = _labelZero.getSize().width;
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

    protected Label _labelZero;
    protected Label _labelOne;
    protected Label _labelTwo;
}
