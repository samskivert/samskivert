//
// $Id: MouseArmingAdapter.java,v 1.1 2002/10/01 17:31:14 ray Exp $

package com.samskivert.swing;

import java.awt.Component;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import javax.swing.event.MouseInputAdapter;

/**
 * A mouse arming adapter is a MouseAdapter that translates 'sloppy'
 * clicks into a call to mouseClicked, as well as provides
 * a method that can be used to draw a component as armed or not.
 */
public abstract class MouseArmingAdapter
{
    /**
     * Construct a simple MouseArmingAdapter on the specified component.
     */
    public MouseArmingAdapter (Component component)
    {
        this(component, null);
    }

    /**
     * Construct a MouseArmingAdapter with the specified active area
     * within the component.
     */
    public MouseArmingAdapter (final Component component, final Shape bounds)
    {
        MouseInputAdapter mia = new MouseInputAdapter() {

            // documentation inherited
            public void mousePressed (MouseEvent e)
            {
                if (button1(e) && contains(e)) {
                    _armed = _startedArmed = true;
                    setArmed(true);
                }
            }

            // documentation inherited
            public void mouseReleased (MouseEvent e)
            {
                if (button1(e)) {
                    if (_armed && contains(e)) {
                        clicked();
                    }
                    _startedArmed = _armed = false;
                }
            }

            // documentation inherited
            public void mouseDragged (MouseEvent e)
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
     * Will be called when a click is registered over the component.
     */
    public void clicked ()
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
