//
// $Id: MenuUtil.java,v 1.2 2001/08/11 22:43:29 mdb Exp $
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001 Michael Bayne
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

import java.awt.event.*;
import javax.swing.*;

/**
 * The MenuUtil class provides miscellaneous useful utility routines
 * for working with menus.
 *
 * Adding a menu item with both a mnemonic and an accelerator to a
 * menu in a frame that listens to its own menus (a common case) can
 * be simplified like so:
 *
 * <code>
 * accel = KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.ALT_MASK);
 * MenuUtil.addMenuItem(this, menuFile, "New", KeyEvent.VK_N, acc);
 * </code>
 */
public class MenuUtil
{
    /**
     * Add a new menu item to the menu with the specified name and
     * attributes.
     *
     * @param l the action listener.
     * @param menu the menu to add the item to.
     * @param name the item name.
     * @param mnem the mnemonic key for the item or null if none.
     * @param accel the keystroke for the item or null if none.
     */
    public static void addMenuItem (ActionListener l, JMenu menu,
                                    String name, Integer mnem,
                                    KeyStroke accel)
    {
	JMenuItem item = new JMenuItem(name);
	item.addActionListener(l);
        if (mnem != null) item.setMnemonic(mnem.intValue());
        if (accel != null) item.setAccelerator(accel);
	menu.add(item);
    }

    /**
     * Add a new menu item to the menu with the specified name and
     * attributes.
     *
     * @param l the action listener.
     * @param menu the menu to add the item to.
     * @param name the item name.
     * @param mnem the mnemonic key for the item.
     * @param accel the keystroke for the item or null if none.
     */
    public static void addMenuItem (ActionListener l, JMenu menu,
                                    String name, int mnem, KeyStroke accel)
    {
        addMenuItem(l, menu, name, new Integer(mnem), accel);
    }

    /**
     * Add a new menu item to the menu with the specified name and
     * attributes.
     *
     * @param l the action listener.
     * @param menu the menu to add the item to.
     * @param name the item name.
     * @param mnem the mnemonic key for the item.
     */
    public static void addMenuItem (ActionListener l, JMenu menu,
                                    String name, int mnem)
    {
        addMenuItem(l, menu, name, new Integer(mnem), null);
    }

    /**
     * Add a new menu item to the menu with the specified name.
     *
     * @param l the action listener.
     * @param menu the menu to add the item to.
     * @param name the item name.
     */
    public static void addMenuItem (ActionListener l, JMenu menu, String name)
    {
        addMenuItem(l, menu, name, null, null);
    }
}
