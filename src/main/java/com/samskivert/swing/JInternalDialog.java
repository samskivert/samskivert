//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2010 Michael Bayne, et al.
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

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;

/**
 * Used for displaying dialogs internally. Be sure to use {@link
 * #showDialog} and {@link #dismissDialog} to show and hide these dialogs
 * because they need to do some extra fiddling that the regular show and
 * hide wouldn't do.
 */
public class JInternalDialog extends JInternalFrame
{
    /**
     * Creates a dialog that will display itself in the layered pane of
     * the frame that contains the supplied component.
     */
    public JInternalDialog (JComponent friend)
    {
        this(JLayeredPane.getLayeredPaneAbove(friend));
    }

    /**
     * Creates a dialog that will display itself in the layered pane of
     * the supplied frame.
     */
    public JInternalDialog (JFrame frame)
    {
        this(frame.getLayeredPane());
    }

    /**
     * Creates a dialog that will display itself in the specified layered
     * pane.
     */
    public JInternalDialog (JLayeredPane parent)
    {
        _parent = parent;
    }

    /**
     * Adds this dialog to its parent and shows it.
     */
    public void showDialog ()
    {
        _parent.add(this, JLayeredPane.PALETTE_LAYER);
        setVisible(true);
    }

    /**
     * Hides this dialog and removes it from its parent.
     */
    public void dismissDialog ()
    {
        setVisible(false);
        _parent.remove(this);
    }

    /**
     * Scans up the interface hierarchy looking for the {@link
     * JInternalDialog} that contains the supplied child component and
     * dismisses it.
     */
    public static void dismissDialog (Component child)
    {
        if (child == null) {
            return;
        } else if (child instanceof JInternalDialog) {
            ((JInternalDialog)child).dismissDialog();
        } else {
            dismissDialog(child.getParent());
        }
    }

    /** Our parent. */
    protected JLayeredPane _parent;
}
