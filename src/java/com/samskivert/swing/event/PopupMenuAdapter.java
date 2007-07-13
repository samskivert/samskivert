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
