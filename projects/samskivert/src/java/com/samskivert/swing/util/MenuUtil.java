//
// $Id: MenuUtil.java,v 1.1 2001/07/24 20:11:19 shaper Exp $

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
