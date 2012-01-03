//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.swing;

import java.awt.AlphaComposite;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.Icon;

/**
 * Draws an icon with a specified alpha level.
 */
public class DimmedIcon implements Icon
{
    /**
     * Construct a dimmed icon that is drawn at 50% normal.
     */
    public DimmedIcon (Icon icon)
    {
        this(icon, .5f);
    }

    /**
     * Construct a dimmed icon that is drawn at the specified alpha level.
     */
    public DimmedIcon (Icon icon, float alpha)
    {
        _alpha = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
        _icon = icon;
    }

    // documentation inherited from interface Icon
    public int getIconWidth ()
    {
        return _icon.getIconWidth();
    }

    // documentation inherited from interface Icon
    public int getIconHeight ()
    {
        return _icon.getIconHeight();
    }

    // documentation inherited from interface Icon
    public void paintIcon (Component c, Graphics g, int x, int y)
    {
        Graphics2D gfx = (Graphics2D) g;
        Composite ocomp = gfx.getComposite();
        gfx.setComposite(_alpha);
        _icon.paintIcon(c, gfx, x, y);
        gfx.setComposite(ocomp);
    }

    /** The icon we're actually drawing. */
    protected Icon _icon;

    /** Our alpha composite. */
    protected Composite _alpha;
}
