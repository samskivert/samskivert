//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import com.samskivert.swing.util.SwingUtil;

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
     * Constructs a multi line label that displays the supplied text with center-alignment. The
     * default layout is all on one line.
     *
     * @see #setLayout
     */
    public MultiLineLabel (String text)
    {
        this(text, CENTER);
    }

    /**
     * Constructs a multi line label that displays the supplied text with the specified
     * alignment. The default layout is all on one line.
     *
     * @see #setLayout
     */
    public MultiLineLabel (String text, int align)
    {
        this(text, align, NONE, 0);
    }

    /**
     * Constructs a multi line label that displays the supplied text with the specified
     * alignment. The default layout is all on one line.
     *
     * @see #setLayout
     */
    public MultiLineLabel (String text, int align, int constrain, int size)
    {
        _label = createLabel(text);
        _label.setAlignment(align);
        noteConstraints(constrain, size);
    }

    /**
     * @deprecated see {@link SwingUtil#getDefaultTextAntialiasing} on how to control text
     * antialiasing.
     */
    @Deprecated
    public void setAntiAliased (boolean antialiased)
    {
    }

    /**
     * Sets the constraints to be used when laying out the label.
     *
     * @param constrain {@link #HORIZONTAL} or {@link #VERTICAL} or {@link #GOLDEN} if the label
     * should be laid out in a rectangle whose bounds approximate the golden ratio.
     * @param size the width or height respectively to be targeted by the label or 0 if the label
     * should react the first time it is laid out and use the dimension available at that
     * point. <em>Note:</em> this requires that the label invalidate itself during its first
     * validation which will cause it to change size visibly in the user interface. This argument
     * is ignored if <code>constrain</code> is {@link #GOLDEN}.
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
            throw new IllegalArgumentException("Invalid constraint orientation " + constrain);
        }
    }

    /**
     * Sets the text displayed by this label.
     */
    public void setText (String text)
    {
        if (_label.setText(text)) {
            _dirty = true;
            // clear out our constrained size where appropriate
            if (_constrain == HORIZONTAL || _constrain == VERTICAL) {
                _constrainedSize = 0;
                _label.clearTargetDimens();
            }
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

    @Override
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

        // set the text color to our foreground color
        _label.setTextColor(getForeground());

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

    @Override
    public void doLayout ()
    {
        super.doLayout();

        // if we have been configured to relay ourselves out once we know our constrained width or
        // height, take care of that here
        int size;
        boolean delayedRevalidate = false;
        switch (_constrain) {
        case HORIZONTAL:
            size = getWidth();
            // sanity check; sometimes labels are laid out with completely invalid dimensions, so
            // we just quietly play along
            if (size > 0 && size != _constrainedSize) {
                _constrainedSize = size;
                _label.setTargetWidth(size);
                delayedRevalidate = true;
            }
            break;

        case VERTICAL:
            size = getHeight();
            if (size > 0 && size != _constrainedSize) {
                _constrainedSize = size;
                _label.setTargetHeight(size);
                delayedRevalidate = true;
            }
            break;
        }

        // we can't just call revalidate() because we're in the middle of a validation traversal;
        // our parent will, when we return from this method, declare itself to be valid; if we call
        // revalidate() it will mark us and all of our parents as invalid, but then those of our
        // parents that are involved in the first validation traversal will mark themselves as
        // valid and in the validation pass that results from our call to revalidate() we will be
        // skipped over; dooh! instead we delay a call to revalidate so that the current validation
        // traversal will be completed before we mark ourselves and our parents as invalid
        if (delayedRevalidate) {
            Runnable callRevalidate = new Runnable() {
                public void run() {
                    revalidate();
                }
            };
            SwingUtilities.invokeLater(callRevalidate);
        }

        // go ahead and lay out the label in all cases so that we assume some sort of size
        layoutLabel();
    }

    /**
     * Creates the underlying {@link Label} that we use to render our text.
     */
    protected Label createLabel (String text)
    {
        return new Label(text);
    }

    /**
     * Called when the label has changed in some meaningful way and we'd accordingly like to
     * re-layout the label, update our component's size, and repaint everything to suit.
     */
    protected void layoutLabel ()
    {
        Graphics2D gfx = (Graphics2D)getGraphics();
        if (gfx != null) {
            // re-layout the label
            _label.layout(gfx);
            gfx.dispose();

            // note that we're no longer dirty
            _dirty = false;
        }
    }

    @Override
    public Dimension getPreferredSize ()
    {
        if (isPreferredSizeSet()) {
            return super.getPreferredSize();
        }

        if (_dirty) {
            // attempt to lay out the label before obtaining its preferred dimensions
            layoutLabel();
        }

        Dimension size = _label.getSize();
        if (size != null) {
            // never let our preferred size shrink in our constrained direction
            switch (_constrain) {
            case HORIZONTAL:
                _prefd.width = Math.max(_prefd.width, size.width);
                _prefd.height = size.height;
                break;

            case VERTICAL:
                _prefd.width = size.width;
                _prefd.height = Math.max(_prefd.height, size.height);
                break;

            default:
                _prefd.width = size.width;
                _prefd.height = size.height;
                break;
            }
        }

        return _prefd;
    }

    /** Our preferred size. */
    protected Dimension _prefd = new Dimension(5, 5);

    /** The label we're displaying. */
    protected Label _label;

    /** The off-axis alignment with which the label is positioned. */
    protected int _offalign;

    /** Pending constraint adjustments. */
    protected int _constrain = NONE;

    /** The size to which we constrained ourselves when most recently laid out. */
    protected int _constrainedSize;

    /** Whether this label is dirty and should be re-layed out. */
    protected boolean _dirty = true;
}
