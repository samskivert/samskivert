//
// $Id: MultiLineLabel.java,v 1.3 2002/09/23 21:19:06 shaper Exp $
//
// samskivert library - useful routines for java programs
// Copyright (C) 2002 Walter Korman
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
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Graphics;
import java.awt.RenderingHints;

import javax.swing.JComponent;
import javax.swing.SwingConstants;

import com.samskivert.Log;
import com.samskivert.util.StringUtil;

/**
 * A Swing component that displays a {@link Label}.
 */
public class MultiLineLabel extends JComponent
    implements SwingConstants, LabelStyleConstants
{
    /**
     * Constructs an empty multi line label.
     */
    public MultiLineLabel ()
    {
        this("");
    }

    /**
     * Constructs a multi line label that displays the supplied text with
     * center-alignment.
     */
    public MultiLineLabel (String text)
    {
        this(text, -1, CENTER);
    }

    /**
     * Constructs a multi line label that displays the supplied text with
     * the specified alignment.  The text will be laid out all on one
     * line.
     */
    public MultiLineLabel (String text, int align)
    {
        this(text, -1, align);
    }

    /**
     * Constructs a multi line label that displays the supplied text with
     * the specified constraints and alignment.  Constraints should be one
     * of {@link #HORIZONTAL}, {@link #VERTICAL}, or <code>-1</code> if no
     * constraints are desired.
     */
    public MultiLineLabel (String text, int constrain, int align)
    {
        _constrain = constrain;
        _label = new Label(text);
        _label.setAlignment(align);
    }

    /**
     * Sets whether this label's text should be rendered with
     * anti-aliasing.
     */
    public void setAntiAliased (boolean antialiased)
    {
        _antialiased = antialiased;
        _dirty = true;
    }

    /**
     * Sets the text displayed by this label.
     */
    public void setText (String text)
    {
        _label.setText(text);
        _dirty = true;
    }

    /**
     * Sets the alternate color used to display the label text.
     */
    public void setAlternateColor (Color color)
    {
        _label.setAlternateColor(color);
        _dirty = true;
    }

    /**
     * Sets the alignment of the text displayed by this label.
     */
    public void setAlignment (int align)
    {
        _label.setAlignment(align);
        _dirty = true;
    }

    /**
     * Sets the off-axis alignment of the text displayed by this label.
     */
    public void setOffAxisAlignment (int align)
    {
        _offalign = align;
        _dirty = true;
    }

    /**
     * Sets the text style used to render this label.
     */
    public void setStyle (int style)
    {
        _label.setStyle(style);
        _dirty = true;
    }

    // documentation inherited
    public void paintComponent (Graphics g)
    {
        super.paintComponent(g);

        // if we're dirty, re-lay things out before painting ourselves
        if (_dirty) {
            layoutLabel();
        }

        Graphics2D gfx = (Graphics2D)g;
        int align = _label.getAlignment();
        int dx = 0, dy = 0;
        int wid = getWidth(), hei = getHeight();
        Dimension ld = _label.getSize();

        // calculate the x-offset at which the label is rendered
        switch (align) {
        case CENTER: dx = (wid - ld.width) / 2; break;
        case RIGHT: dx = wid - ld.width; break;
        }

        // calculate the y-offset at which the label is rendered
        switch (_offalign) {
        case CENTER: dy = (hei - ld.height) / 2; break;
        case BOTTOM: dy = hei - ld.height; break;
        }

        // draw the label
        _label.render(gfx, dx, dy);
    }

    // documentation inherited
    public void doLayout ()
    {
        super.doLayout();

        switch (_constrain) {
        case HORIZONTAL: _label.setTargetWidth(getWidth()); break;
        case VERTICAL: _label.setTargetHeight(getHeight()); break;
        }
        layoutLabel();
    }

    /**
     * Called when the label has changed in some meaningful way and we'd
     * accordingly like to re-layout the label, update our component's
     * size, and repaint everything to suit.
     */
    protected void layoutLabel ()
    {
        Graphics2D gfx = (Graphics2D)getGraphics();
        if (gfx != null) {
            // re-layout the label
            gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                 (_antialiased) ?
                                 RenderingHints.VALUE_ANTIALIAS_ON :
                                 RenderingHints.VALUE_ANTIALIAS_OFF);
            _label.layout(gfx);
            gfx.dispose();

            // update our size and force a layout
            revalidate();

            // note that we're no longer dirty
            _dirty = false;
        }
    }

    // documentation inherited
    public Dimension getPreferredSize ()
    {
        if (_dirty) {
            // attempt to lay out the label before obtaining its preferred
            // dimensions
            layoutLabel();
        }
        Dimension size = _label.getSize();
        return (size == null) ? new Dimension(10, 10) : size;
    }

    /** The dimensions occupied by our label. */
    protected Dimension _psize;

    /** The label we're displaying. */
    protected Label _label;

    /** The constraints we apply to the label text. */
    protected int _constrain;

    /** The off-axis alignment with which the label is positioned. */
    protected int _offalign;

    /** Whether to render the label with anti-aliasing. */
    protected boolean _antialiased;

    /** Whether this label is dirty and should be re-layed out. */
    protected boolean _dirty;
}
