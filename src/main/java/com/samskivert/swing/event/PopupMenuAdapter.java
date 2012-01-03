//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.swing.event;

import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

/**
 * An abstract adapter class for receiving popup menu events. The methods
 * in this class are empty. This class exists as a convenience for
 * creating listener objects.
 *
 * <p> This class really ought to have been provided as a standard part of
 * the <code>javax.swing.event</code> package, but somehow the developers
 * missed it and so we've done their job for them.
 */
public abstract class PopupMenuAdapter implements PopupMenuListener
{
    // documentation inherited
    public void popupMenuWillBecomeInvisible (PopupMenuEvent e)
    {
        // nothing to do here
    }

    // documentation inherited
    public void popupMenuCanceled (PopupMenuEvent e)
    {
        // nothing to do here
    }

    // documentation inherited
    public void popupMenuWillBecomeVisible (PopupMenuEvent e)
    {
        // nothing to do here
    }
}
