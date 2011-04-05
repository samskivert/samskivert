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
