//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.swing.event;

import java.awt.Component;
import java.awt.Shape;
import java.awt.event.MouseEvent;

import javax.swing.event.MouseInputAdapter;

/**
 * A mouse arming adapter is a {@link MouseInputAdapter} that translates
 * 'sloppy' clicks into a call to {@link #clicked}, as well as provides a
 * method that can be used to draw a component as armed or not.
 */
public abstract class MouseArmingAdapter
{
    /**
     * Constructs a simple mouse arming adapter for the specified
     * component.
     */
    public MouseArmingAdapter (Component component)
    {
        this(component, null);
    }

    /**
     * Constructs a mouse arming adapter with the specified active area
     * within the component.
     */
    public MouseArmingAdapter (final Component component, final Shape bounds)
    {
        MouseInputAdapter mia = new MouseInputAdapter() {
            @Override public void mousePressed (MouseEvent e)
            {
                if (button1(e) && contains(e)) {
                    _armed = _startedArmed = true;
                    setArmed(true);
                }
            }

            @Override public void mouseReleased (MouseEvent e)
            {
                if (button1(e)) {
                    if (_armed && contains(e)) {
                        clicked(e);
                    }
                    _startedArmed = _armed = false;
                }
            }

            @Override public void mouseDragged (MouseEvent e)
            {
                if (_startedArmed && (contains(e) != _armed)) {
                    _armed = !_armed;
                    setArmed(_armed);
                }
            }

            /**
             * Is the specified mouse event within the active area?
             */
            protected boolean contains (MouseEvent e)
            {
                if (bounds != null) {
                    return bounds.contains(e.getX(), e.getY());
                } else {
                    return component.contains(e.getX(), e.getY());
                }
            }

            /**
             * Was the first mouse button used?
             */
            protected boolean button1 (MouseEvent e)
            {
                return e.getButton() == MouseEvent.BUTTON1;
            }

            /** Are we armed and did we start armed in this drag?  */
            protected boolean _armed, _startedArmed;
        };

        component.addMouseListener(mia);
        component.addMouseMotionListener(mia);
    }

    /**
     * Called when a click is registered over the component with the mouse
     * event that resulted in the click.
     */
    public void clicked (MouseEvent e)
    {
    }

    /**
     * Called during drags to let us know if the component should be drawn
     * armed or not.
     */
    public void setArmed (boolean armed)
    {
    }
}
