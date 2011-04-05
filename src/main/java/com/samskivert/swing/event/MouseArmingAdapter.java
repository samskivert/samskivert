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
