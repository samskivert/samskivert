//
// $Id: ScrollBox.java,v 1.1 2003/03/26 09:27:43 ray Exp $

package com.samskivert.swing;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

import java.awt.event.MouseEvent;

import javax.swing.BoundedRangeModel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputAdapter;

/**
 * A non-annoying way to do two-dimensional scrolling.
 * Because horizontal scrollbars are the devil's toys.
 */
public class ScrollBox extends JPanel
{
    /**
     * Construct the box to work on the specified scrollpane.
     */
    public ScrollBox (JScrollPane pane)
    {
        this(pane.getHorizontalScrollBar().getModel(),
             pane.getVerticalScrollBar().getModel());
    }

    /**
     * Construct with the specified range models.
     */
    public ScrollBox (BoundedRangeModel horz, BoundedRangeModel vert)
    {
        _horz = horz;
        _vert = vert;

        addMouseListener(_mouser);
        addMouseMotionListener(_mouser);
    }

    // documentation inherited
    public void addNotify ()
    {
        super.addNotify();

        _horz.addChangeListener(_changebob);
        _vert.addChangeListener(_changebob);
        updateBox();
    }

    // documentation inherited
    public void removeNotify ()
    {
        super.removeNotify();

        _horz.removeChangeListener(_changebob);
        _vert.removeChangeListener(_changebob);
    }

    // documentation inherited
    public void setBounds (int x, int y, int w, int h)
    {
        super.setBounds(x, y, w, h);
        updateBox();
    }

    // documentation inherited
    public void paintComponent (Graphics g)
    {
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setColor(getForeground());
        g.drawRect(_box.x, _box.y, _box.width, _box.height);
    }

    /**
     * Recalculate the size of the box.
     */
    protected void updateBox ()
    {
        _hFactor = (getWidth() - 1) /
            (float) (_horz.getMaximum() - _horz.getMinimum());
        _vFactor = (getHeight() - 1) /
            (float) (_vert.getMaximum() - _horz.getMinimum());

        _box.x = (int) Math.round(_horz.getValue() * _hFactor);
        _box.width = (int) Math.round(_horz.getExtent() * _hFactor);

        _box.y = (int) Math.round(_vert.getValue() * _vFactor);
        _box.height = (int) Math.round(_vert.getExtent() * _vFactor);
    }

    /** The bounds that we observe / modify. */
    protected BoundedRangeModel _horz, _vert;

    /** The box that we're spanking. */
    protected Rectangle _box = new Rectangle();

    /** The conversion factor from one pixel to one unit of bounded range. */
    protected float _hFactor, _vFactor;

    /**
     * Listens to mouse events and updates the models.
     */
    protected MouseInputAdapter _mouser = new MouseInputAdapter ()
    {
        // documentation inherited
        public void mousePressed (MouseEvent e)
        {
            if (e.getButton() == MouseEvent.BUTTON1) {
                Point p = e.getPoint();
                if (_box.contains(p)) {
                    _lastPoint = p;
                }
            }
        }

        // documentation inherited
        public void mouseDragged (MouseEvent e)
        {
            if (_lastPoint != null) {
                Point p = e.getPoint();
                _horz.setValue(_horz.getValue() +
                    (int) Math.round((p.x - _lastPoint.x) / _hFactor));
                _vert.setValue(_vert.getValue() +
                    (int) Math.round((p.y - _lastPoint.y) / _vFactor));
                _lastPoint = p;
            }
        }

        // documentation inherited
        public void mouseReleased (MouseEvent e)
        {
            if (e.getButton() == MouseEvent.BUTTON1) {
                _lastPoint = null;
            }
        }

        /** The last point with which we compare drag points. */
        protected Point _lastPoint;
    };

    /**
     * Listens to both range models and updates us when they change.
     */
    protected ChangeListener _changebob = new ChangeListener() {
        // documentation inherited
        public void stateChanged (ChangeEvent e)
        {
            updateBox();
            repaint();
        }
    };
}
