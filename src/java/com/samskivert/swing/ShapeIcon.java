//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2007 Michael Bayne
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;

import javax.swing.Icon;

/**
 * Fills and or draws the specified shape antialiased as an icon.
 */
public class ShapeIcon
    implements Icon
{
    /**
     * @param shape the shape to paint 
     * @param fillColor the color to paint the shape filled, or null.
     * @param outlineColor the color to paint the shape outline, or null.
     */
    public ShapeIcon (Shape shape, Color fillColor, Color outlineColor)
    {
        _shape = shape;
        _fillColor = fillColor;
        _outlineColor = outlineColor;
    }

    // documentation inherited from interface Icon
    public int getIconHeight ()
    {
        return _shape.getBounds().height;
    }

    // documentation inherited from interface Icon
    public int getIconWidth ()
    {
        return _shape.getBounds().width;
    }

    // documentation inherited from interface Icon
    public void paintIcon (Component c, Graphics g, int x, int y)
    {
        Graphics2D g2 = (Graphics2D) g;
        // turn on anti-aliasing
        Object oldAlias = g2.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        Rectangle bounds = _shape.getBounds();
        int dx = x - bounds.x;
        int dy = y - bounds.y;
        g2.translate(dx, dy);
        if (_fillColor != null) {
            g2.setColor(_fillColor);
            g2.fill(_shape);
        }
        if (_outlineColor != null) {
            g2.setColor(_outlineColor);
            g2.draw(_shape);
        }
        g2.translate(-dx, -dy);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, oldAlias);
    }

    /** The shape we're drawing. */
    protected Shape _shape;

    /** Colors. */
    protected Color _fillColor, _outlineColor;
}
