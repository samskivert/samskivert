//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.swing.util;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;

import javax.swing.JFrame;
import javax.swing.JPanel;

import com.samskivert.swing.JInternalDialog;

/**
 * Utility methods for dialogs.
 */
public class DialogUtil
{
    /**
     * Creates and shows an internal dialog with the specified panel.
     */
    public static JInternalDialog createDialog (JFrame frame, JPanel content)
    {
        return createDialog(frame, null, content);
    }

    /**
     * Creates and shows an internal dialog with the specified title and
     * panel.
     */
    public static JInternalDialog createDialog (
        JFrame frame, String title, JPanel content)
    {
        JInternalDialog dialog = new JInternalDialog(frame);
        dialog.setOpaque(false);
        if (title != null) {
            dialog.setTitle(title);
        }
        setContent(dialog, content);
        SwingUtil.centerComponent(frame, dialog);
        dialog.showDialog();
        return dialog;
    }

    /**
     * Sets the content panel of the supplied internal dialog.
     */
    public static void setContent (JInternalDialog dialog, JPanel content)
    {
        Container holder = dialog.getContentPane();
        holder.removeAll();
        holder.add(content, BorderLayout.CENTER);
        dialog.pack();
    }

    /**
     * Returns the internal dialog that is a parent of the specified
     * component.
     */
    public static JInternalDialog getInternalDialog (Component any)
    {
        Component parent = any;
        while (parent != null && !(parent instanceof JInternalDialog)) {
            parent = parent.getParent();
        }

        return (JInternalDialog) parent;
    }

    /**
     * Invalidates and resizes the entire dialog given any component
     * within the dialog in question.
     */
    public static void invalidateDialog (Component any)
    {
        JInternalDialog dialog = getInternalDialog(any);
        if (dialog == null) {
            return;
        }

        SwingUtil.applyToHierarchy(dialog, new SwingUtil.ComponentOp() {
            public void apply (Component comp) {
                comp.invalidate();
            }
        });

        dialog.setSize(dialog.getPreferredSize());
    }
}
