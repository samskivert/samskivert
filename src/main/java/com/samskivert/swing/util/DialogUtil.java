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
