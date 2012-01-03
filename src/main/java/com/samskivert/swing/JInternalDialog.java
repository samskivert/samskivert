//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

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
