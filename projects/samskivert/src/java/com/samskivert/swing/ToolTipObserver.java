//
// $Id: ToolTipObserver.java,v 1.4 2001/12/14 18:58:29 shaper Exp $
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001 Walter Korman
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

package com.samskivert.swing;

import javax.swing.JComponent;

/**
 * An interface to be implemented by container objects that would like
 * to be notified by the {@link ToolTipManager} when they should
 * display tool tips associated with the objects that they manage.
 */
public interface ToolTipObserver
{
    /**
     * Called when the tool tip associated with the given provider
     * should be displayed.
     *
     * @param tipper the tool tip provider.
     * @param x the last mouse x-position.
     * @param y the last mouse y-position.
     */
    public void showToolTip (ToolTipProvider tipper, int x, int y);

    /**
     * Called when any visible tool tip should be hidden and so the
     * observer is likely to want to repaint itself without the tip.
     */
    public void hideToolTip ();

    /**
     * Return the component associated with the observer so that the
     * tool tip manager can restrict monitoring the component to when
     * it's actually visible.
     */
    public JComponent getComponent ();
}
