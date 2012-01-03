//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.swing;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;

import javax.swing.Icon;

/**
 * Draws an icon with a specified scale.
 */
public class ScaledIcon implements Icon
{
    /**
     * Construct an icon that is drawn at 10% normal.
     */
    public ScaledIcon (Icon icon)
    {
        this(icon, .1f);
    }

    /**
     * Construct a scaled icon that is drawn at the specified scale.
     */
    public ScaledIcon (Icon icon, float scale)
    {
        _icon = icon;
        _scale = scale;
    }

    /**
     * Construct a scaled icon restricted to its larger dimension.
     */
    public ScaledIcon (Icon icon, int maxWidth, int maxHeight)
    {
        this(icon,
            Math.min(1f, Math.min(maxWidth / (float) icon.getIconWidth(),
                                  maxHeight / (float) icon.getIconHeight())));
    }

    // documentation inherited from interface Icon
    public int getIconWidth ()
    {
        return Math.round(_icon.getIconWidth() * _scale);
    }

    // documentation inherited from interface Icon
    public int getIconHeight ()
    {
        return Math.round(_icon.getIconHeight() * _scale);
    }

    // documentation inherited from interface Icon
    public void paintIcon (Component c, Graphics g, int x, int y)
    {
        Graphics2D gfx = (Graphics2D) g;
        AffineTransform otrans = gfx.getTransform();
        RenderingHints ohints = gfx.getRenderingHints();

        gfx.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        gfx.scale(_scale, _scale);
        _icon.paintIcon(c, g, x, y);

        gfx.setTransform(otrans);
        gfx.setRenderingHints(ohints);
    }

    /** The icon we're actually drawing. */
    protected Icon _icon;

    /** Our scale factor. */
    protected float _scale;
}
