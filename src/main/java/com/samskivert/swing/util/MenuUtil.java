//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.swing.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.lang.reflect.Method;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

import static com.samskivert.swing.Log.log;

/**
 * The menu util class provides miscellaneous useful utility routines for
 * working with menus. Adding a menu item with both a mnemonic and an
 * accelerator to a menu in a frame that listens to its own menus (a
 * common case) can be simplified like so:
 *
 * <pre>
 * accel = KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.ALT_MASK);
 * MenuUtil.addMenuItem(this, menuFile, "New", KeyEvent.VK_N, acc);
 * </pre>
 */
public class MenuUtil
{
    /**
     * Adds a new menu item to the menu with the specified name and
     * attributes.
     *
     * @param l the action listener.
     * @param menu the menu to add the item to.
     * @param name the item name.
     * @param mnem the mnemonic key for the item or null if none.
     * @param accel the keystroke for the item or null if none.
     *
     * @return the new menu item.
     */
    public static JMenuItem addMenuItem (
        ActionListener l, JMenu menu, String name, Integer mnem,
        KeyStroke accel)
    {
        JMenuItem item = createItem(name, mnem, accel);
        item.addActionListener(l);
        menu.add(item);
        return item;
    }

    /**
     * Adds a new menu item to the menu with the specified name and
     * attributes.
     *
     * @param l the action listener.
     * @param menu the menu to add the item to.
     * @param name the item name.
     * @param mnem the mnemonic key for the item.
     * @param accel the keystroke for the item or null if none.
     *
     * @return the new menu item.
     */
    public static JMenuItem addMenuItem (
        ActionListener l, JMenu menu, String name, int mnem, KeyStroke accel)
    {
        return addMenuItem(l, menu, name, Integer.valueOf(mnem), accel);
    }

    /**
     * Adds a new menu item to the menu with the specified name and
     * attributes.
     *
     * @param l the action listener.
     * @param menu the menu to add the item to.
     * @param name the item name.
     * @param mnem the mnemonic key for the item.
     *
     * @return the new menu item.
     */
    public static JMenuItem addMenuItem (
        ActionListener l, JMenu menu, String name, int mnem)
    {
        return addMenuItem(l, menu, name, Integer.valueOf(mnem), null);
    }

    /**
     * Adds a new menu item to the menu with the specified name.
     *
     * @param l the action listener.
     * @param menu the menu to add the item to.
     * @param name the item name.
     *
     * @return the new menu item.
     */
    public static JMenuItem addMenuItem (
        ActionListener l, JMenu menu, String name)
    {
        return addMenuItem(l, menu, name, null, null);
    }

    /**
     * Adds a new menu item to the menu with the specified name and
     * attributes. The supplied method name will be called (it must have
     * the same signature as {@link ActionListener#actionPerformed} but
     * can be named whatever you like) when the menu item is selected.
     *
     * @param menu the menu to add the item to.
     * @param name the item name.
     * @param mnem the mnemonic key for the item.
     * @param accel the keystroke for the item or null if none.
     * @param target the object on which to invoke a method when the menu is selected.
     * @param callbackName the name of the method to invoke when the menu is selected.
     *
     * @return the new menu item.
     */
    public static JMenuItem addMenuItem (
        JMenu menu, String name, int mnem, KeyStroke accel, Object target, String callbackName)
    {
        JMenuItem item = createItem(name, Integer.valueOf(mnem), accel);
        item.addActionListener(new ReflectedAction(target, callbackName));
        menu.add(item);
        return item;
    }

    /**
     * Adds a new menu item to the menu with the specified name and
     * attributes. The supplied method name will be called (it must have
     * the same signature as {@link ActionListener#actionPerformed} but
     * can be named whatever you like) when the menu item is selected.
     *
     * @param menu the menu to add the item to.
     * @param name the item name.
     * @param target the object on which to invoke a method when the menu is selected.
     * @param callbackName the name of the method to invoke when the menu is selected.
     *
     * @return the new menu item.
     */
    public static JMenuItem addMenuItem (
        JMenu menu, String name, Object target, String callbackName)
    {
        JMenuItem item = createItem(name, null, null);
        item.addActionListener(new ReflectedAction(target, callbackName));
        menu.add(item);
        return item;
    }

    /**
     * Adds a new menu item to the popup menu with the specified name and
     * attributes. The supplied method name will be called (it must have
     * the same signature as {@link ActionListener#actionPerformed} but
     * can be named whatever you like) when the menu item is selected.
     *
     * <p> Note that this <code>JPopupMenu</code>-specific implementation
     * exists solely because <code>JPopupMenu</code> doesn't extend
     * <code>JMenu</code> and so we have to explicitly call {@link
     * JPopupMenu#add} rather than {@link JMenu#add}.
     *
     * @param menu the menu to add the item to.
     * @param name the item name.
     * @param target the object on which to invoke a method when the menu is selected.
     * @param callbackName the name of the method to invoke when the menu is selected.
     *
     * @return the new menu item.
     */
    public static JMenuItem addMenuItem (
        JPopupMenu menu, String name, Object target, String callbackName)
    {
        JMenuItem item = createItem(name, null, null);
        item.addActionListener(new ReflectedAction(target, callbackName));
        menu.add(item);
        return item;
    }

    /**
     * Creates and configures a menu item.
     */
    protected static JMenuItem createItem (String name, Integer mnem, KeyStroke accel)
    {
        JMenuItem item = new JMenuItem(name);
        if (mnem != null) {
            item.setMnemonic(mnem.intValue());
        }
        if (accel != null) {
            item.setAccelerator(accel);
        }
        return item;
    }

    /**
     * Used to wire menu items directly up to method calls.
     */
    protected static class ReflectedAction implements ActionListener
    {
        public ReflectedAction (Object target, String methodName) {
            try {
                // look up the method we'll be calling
                _method = target.getClass().getMethod(methodName, METHOD_ARGS);
                _target = target;

            } catch (Exception e) {
                log.warning("Unable to obtain menu callback method. Item will not function.",
                            "target", target, "method", _method, "error", e);
            }
        }

        public void actionPerformed (ActionEvent event) {
            if (_method != null) {
                try {
                    _method.invoke(_target, new Object[] { event });
                } catch (Exception e) {
                    log.warning("Failure invoking menu callback", "target", _target,
                                "method", _method, e);
                }
            }
        }

        /** The object on which to invoke the reflected method. */
        protected Object _target;

        /** The method to call when the menu item is invoked. */
        protected Method _method;
    }

    /** The method signature for menu callback methods. */
    protected static final Class<?>[] METHOD_ARGS = new Class<?>[] { ActionEvent.class };
}
