//
// $Id: MultiLineLabel.java,v 1.8 2002/11/12 06:41:29 mdb Exp $
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
    /** A layout constant used by {@link #setLayout}. */
    public static final int GOLDEN = HORIZONTAL+VERTICAL+1;

    /** A layout constant used by {@link #setLayout}. */
    public static final int NONE = GOLDEN+1;

    /**
     * Constructs an empty multi line label.
     */
    public MultiLineLabel ()
    {
        this("");
    }

    /**
     * Constructs a multi line label that displays the supplied text with
     * center-alignment. The default layout is all on one line.
     *
     * @see #setLayout
     */
    public MultiLineLabel (String text)
    {
        this(text, CENTER);
    }

    /**
     * Constructs a multi line label that displays the supplied text with
     * the specified alignment. The default layout is all on one line.
     *
     * @see #setLayout
     */
    public MultiLineLabel (String text, int align)
    {
        this(text, align, NONE, 0);
    }

    /**
     * Constructs a multi line label that displays the supplied text with
     * the specified alignment. The default layout is all on one line.
     *
     * @see #setLayout
     */
    public MultiLineLabel (String text, int align, int constrain, int size)
    {
        _label = new Label(text);
        _label.setAlignment(align);
        noteConstraints(constrain, size);
    }

    /**
     * Sets whether this label's text should be rendered with
     * anti-aliasing.
     */
    public void setAntiAliased (boolean antialiased)
    {
        _antialiased = antialiased;
        _dirty = true;
        repaint();
    }

    /**
     * Sets the constraints to be used when laying out the label.
     *
     * @param constrain {@link #HORIZONTAL} or {@link #VERTICAL} or {@link
     * #GOLDEN} if the label should be laid out in a rectangle whose
     * bounds approximate the golden ratio.
     * @param size the width or height respectively to be targeted by the
     * label or 0 if the label should react the first time it is laid out
     * and use the dimension available at that point. <em>Note:</em> this
     * requires that the label invalidate itself during its first
     * validation which will cause it to change size visibly in the user
     * interface. This argument is ignored if <code>constrain</code> is
     * {@link #GOLDEN}.
     */
    public void setLayout (int constrain, int size)
    {
        noteConstraints(constrain, size);
        _dirty = true;
        repaint();
    }

    /** Helper function. */
    protected void noteConstraints (int constrain, int size)
    {
        switch (constrain) {
        case HORIZONTAL:
            if (size == 0) {
                _constrain = HORIZONTAL;
            } else {
                _label.setTargetWidth(size);
            }
            break;

        case VERTICAL:
            if (size == 0) {
                _constrain = VERTICAL;
            } else {
                _label.setTargetHeight(size);
            }
            break;

        case GOLDEN:
            _label.setGoldenLayout();
            break;

        case NONE:
            // nothing doing
            break;

        default:
            throw new IllegalArgumentException(
                "Invalid constraint orientation " + constrain);
        }
    }

    /**
     * Sets the text displayed by this label.
     */
    public void setText (String text)
    {
        if (_label.setText(text)) {
            _dirty = true;
            revalidate();
            repaint();
        }
    }

    /**
     * Returns the text displayed by this label.
     */
    public String getText ()
    {
        return _label.getText();
    }

    /**
     * Sets the alternate color used to display the label text.
     */
    public void setAlternateColor (Color color)
    {
        _label.setAlternateColor(color);
        _dirty = true;
        repaint();
    }

    /**
     * Sets the alignment of the text displayed by this label.
     */
    public void setAlignment (int align)
    {
        _label.setAlignment(align);
        _dirty = true;
        repaint();
    }

    /**
     * Sets the off-axis alignment of the text displayed by this label.
     */
    public void setOffAxisAlignment (int align)
    {
        _offalign = align;
        _dirty = true;
        repaint();
    }

    /**
     * Sets the text style used to render this label.
     */
    public void setStyle (int style)
    {
        _label.setStyle(style);
        _dirty = true;
        repaint();
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

        // if we have been configured to relay ourselves out once we know
        // our constrained width or height, take care of that here
        switch (_constrain) {
        case HORIZONTAL:
            // sanity check; sometimes labels are laid out with completely
            // invalid dimensions, so we just quietly play along
            if (getWidth() > 0) {
                _constrain = NONE;
                _label.setTargetWidth(getWidth());
                revalidate();
            }
            break;

        case VERTICAL:
            if (getHeight() > 0) {
                _constrain = NONE;
                _label.setTargetHeight(getHeight());
                revalidate();
            }
            break;
        }

        // go ahead and lay out the label in all cases so that we assume
        // some sort of size
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

    /** The off-axis alignment with which the label is positioned. */
    protected int _offalign;

    /** Pending constraint adjustments. */
    protected int _constrain = NONE;

    /** Whether to render the label with anti-aliasing. */
    protected boolean _antialiased;

    /** Whether this label is dirty and should be re-layed out. */
    protected boolean _dirty;
}
