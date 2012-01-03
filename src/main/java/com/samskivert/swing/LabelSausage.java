//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;

import javax.swing.Icon;

import com.samskivert.swing.util.SwingUtil;

/**
 * An abstract lightweight renderer that sizes and renders a label (with optional icon) in a
 * roundy-ended sausage.
 */
public abstract class LabelSausage
{
    /**
     * Constructs a label sausage.
     */
    protected LabelSausage (Label label, Icon icon)
    {
        _label = label;
        _icon = icon;
    }

    /**
     * Lays out the label sausage.  It is assumed that the desired label font is already set in the
     * label.
     */
    protected void layout (Graphics2D gfx, int extraPadding)
    {
        layout(gfx, 0, extraPadding);
    }

    /**
     * Lays out the label sausage.  It is assumed that the desired label font is already set in the
     * label.
     *
     * @param iconPadding the number of pixels in the x direction to pad around the icon.
     */
    protected void layout (Graphics2D gfx, int iconPadding, int extraPadding)
    {
        // if we have an icon, let that dictate our size; otherwise just lay out our label all on
        // one line
        int sqwid, sqhei;
        if (_icon == null) {
            sqwid = sqhei = 0;
        } else {
            sqwid = _icon.getIconWidth();
            sqhei = _icon.getIconHeight();
            _label.setTargetHeight(sqhei);
        }

        // lay out our label
        _label.layout(gfx);

        Dimension lsize = _label.getSize();

        // if we have no icon, make sure that the label has enough room
        if (_icon == null) {
            sqhei = lsize.height + extraPadding * 2;
            sqwid = extraPadding * 2;
        }

        // compute the diameter of the circle that perfectly encompasses our icon
        int hhei = sqhei / 2;
        int hwid = sqwid / 2;
        _dia = (int) (Math.sqrt(hwid * hwid + hhei * hhei) * 2);

        // compute the x and y offsets at which we'll start rendering
        _xoff = (_dia - sqwid) / 2;
        _yoff = (_dia - sqhei) / 2;

        // and for the label
        _lxoff = _dia - _xoff;
        _lyoff = (_dia - lsize.height) / 2;

        // now compute our closed and open sizes
        _size.height = _dia;

        // width is the diameter of the circle that contains the icon plus space for the label when
        // we're open
        _size.width = _dia + lsize.width + _xoff;

        // and if we are actually rendering the icon, we need to account for the space between it
        // and the label.
        if (_icon != null) {
            // and add the padding needed for the icon
            _size.width += _xoff + (iconPadding * 2);
            _xoff += iconPadding;
            _lxoff += iconPadding * 2;
        }
    }

    /**
     * Paints the label sausage.
     */
    protected void paint (Graphics2D gfx, int x, int y, Color background, Object cliData)
    {
        // turn on anti-aliasing (for our sausage lines)
        Object oalias = SwingUtil.activateAntiAliasing(gfx);

        // draw the base sausage
        gfx.setColor(background);
        drawBase(gfx, x, y);

        // render our icon if we've got one
        drawIcon(gfx, x, y, cliData);

        drawLabel(gfx, x, y);
        drawBorder(gfx, x, y);

        drawExtras(gfx, x, y, cliData);

        // restore original hints
        SwingUtil.restoreAntiAliasing(gfx, oalias);
    }

    /**
     * Draws the base sausage within which all the other decorations are added.
     */
    protected void drawBase (Graphics2D gfx, int x, int y)
    {
        gfx.fillRoundRect(
            x, y, _size.width - 1, _size.height - 1, _dia, _dia);
    }

    /**
     * Draws the icon, if applicable.
     */
    protected void drawIcon (Graphics2D gfx, int x, int y, Object cliData)
    {
        if (_icon != null) {
            _icon.paintIcon(null, gfx, x + _xoff, y + _yoff);
        }
    }

    /**
     * Draws the label.
     */
    protected void drawLabel (Graphics2D gfx, int x, int y)
    {
        _label.render(gfx, x + _lxoff, y + _lyoff);
    }

    /**
     * Draws the black outer border.
     */
    protected void drawBorder (Graphics2D gfx, int x, int y)
    {
        // draw the black outer border
        gfx.setColor(Color.black);
        gfx.drawRoundRect(x, y, _size.width - 1, _size.height - 1, _dia, _dia);
    }

    /**
     * Draws any extras that may be required.
     */
    protected void drawExtras (Graphics2D gfx, int x, int y, Object cliData)
    {
        // nothing by default
    }

    /** The label. */
    protected Label _label;

    /** The optional icon. */
    protected Icon _icon;

    /** The size of this label sausage. */
    protected Dimension _size = new Dimension();

    /** The diameter of the circle that perfectly surrounds our icon. */
    protected int _dia = 0;

    /** The x offset of the icon. */
    protected int _xoff = 0;

    /** The y offset of the icon. */
    protected int _yoff = 0;

    /** The y offset for the label. */
    protected int _lyoff = 0;

    /** The x offset for the label. */
    protected int _lxoff = 0;
}
