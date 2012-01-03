//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.swing.event;

import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

/**
 * An abstract adapter class for receiving ancestor events. The methods in
 * this class are empty. This class exists as a convenience for creating
 * listener objects.
 *
 * <p> This class really ought to have been provided as a standard part of
 * the <code>javax.swing.event</code> package, but somehow the developers
 * missed it and so we've done their job for them.
 */
public abstract class AncestorAdapter implements AncestorListener
{
    // documentation inherited
    public void ancestorAdded (AncestorEvent e) {
        // nothing to do here
    }

    // documentation inherited
    public void ancestorMoved (AncestorEvent e) {
        // nothing to do here
    }

    // documentation inherited
    public void ancestorRemoved (AncestorEvent e) {
        // nothing to do here
    }
}
