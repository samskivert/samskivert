//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;

import javax.swing.Icon;

/**
 * Used to track info for each menu item in a {@link RadialMenu}.
 */
public class RadialMenuItem extends RadialLabelSausage
{
    /** The command to issue if this item is selected. */
    public String command;

    /** A special argument to be used when this menu item is selected, rather than the argument
     * provided to the radial menu when it was activated. */
    public Object argument;

    /** Used to determine whether or not this menu item should be included in a menu and whether or
     * not it should be enabled. If no predicate is available, a menu item is assumed always to be
     * included and enabled. */
    public RadialMenu.Predicate predicate;

    /**
     * Constructs a radial menu item with the specified command and label.  No icon or menu
     * predicate will be used.
     */
    public RadialMenuItem (String command, String label)
    {
        this(command, label, null, null);
    }

    /**
     * Constructs a radial menu item with the specified command, label and icon. No menu predicate
     * will be used.
     */
    public RadialMenuItem (String command, String label, Icon icon)
    {
        this(command, label, icon, null);
    }

    /**
     * Constructs a radial menu item with the specified command, label and icon.
     */
    public RadialMenuItem (String command, String label, Icon icon,
                           RadialMenu.Predicate predicate)
    {
        super(label, icon);

        _label.setTextColor(Color.black);

        this.command = command;
        this.predicate = predicate;
    }

    /**
     * Returns true if this menu item should be included in a menu when it is displayed. Calls
     * through to the {@link RadialMenu.Predicate} if we have one.
     */
    public boolean isIncluded (RadialMenu menu)
    {
        return (predicate == null || predicate.isIncluded(menu, this));
    }

    /**
     * Renders this menu item at the specified location.
     */
    public void render (Component host, RadialMenu menu, Graphics2D gfx, int x, int y)
    {
        paint(gfx, x, y, menu);
    }

    /**
     * Returns true if this menu item should be enabled when it is displayed. Calls through to the
     * {@link RadialMenu.Predicate} if we have one.
     */
    @Override
    public boolean isEnabled (RadialMenu menu)
    {
        return (predicate == null || predicate.isEnabled(menu, this));
    }

    /**
     * Menu items are equal if their commands are equal. We also declare ourselves to be equal to a
     * string with the same value as our command.
     */
    @Override
    public boolean equals (Object other)
    {
        if (other instanceof RadialMenuItem) {
            return command.equals(((RadialMenuItem)other).command);
        } else if (other instanceof String) {
            return command.equals(other);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode ()
    {
        return command.hashCode();
    }

    @Override
    protected void drawIcon (Graphics2D gfx, int x, int y, Object cliData)
    {
        super.drawIcon(gfx, x, y, cliData);
        if (predicate instanceof RadialMenu.IconPredicate) {
            Icon icon = ((RadialMenu.IconPredicate)predicate).getIcon((RadialMenu)cliData, this);
            if (icon != null) {
                icon.paintIcon(null, gfx, x + _xoff, y + _yoff);
            }
        }
    }
}
