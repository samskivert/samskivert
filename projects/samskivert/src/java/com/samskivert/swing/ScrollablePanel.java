//
// $Id: ScrollablePanel.java,v 1.1 2002/02/28 16:24:03 mdb Exp $

package com.samskivert.swing;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;

import javax.swing.JPanel;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

/**
 * A {@link JPanel} extension that implements the {@link Scrollable}
 * interface, instructing it to scroll partially obscured components into
 * view if scrolling is done in the direction of a partially obscured
 * component. If no component is partially obscured, the next component is
 * scrolled into view in its entirety. Presently the code only works with
 * vertically laid out components, horizontal scrolling simply moves by a
 * default number of pixels every time.
 */
public class ScrollablePanel extends JPanel
    implements Scrollable
{
    /**
     * Pass through constructor.
     */
    public ScrollablePanel (LayoutManager layout, boolean isDoubleBuffered)
    {
        super(layout, isDoubleBuffered);
    }

    /**
     * Pass through constructor.
     */
    public ScrollablePanel (LayoutManager layout)
    {
        super(layout);
    }

    /**
     * Pass through constructor.
     */
    public ScrollablePanel (boolean isDoubleBuffered)
    {
        super(isDoubleBuffered);
    }

    // documentation inherited from interface
    public Dimension getPreferredScrollableViewportSize ()
    {
        return getPreferredSize();
    }

    // documentation inherited from interface
    public int getScrollableUnitIncrement (
        Rectangle visibleRect, int orientation, int direction)
    {
        Insets insets = getInsets();

        if (orientation == SwingConstants.HORIZONTAL) {
            // nothing sensible to do here

        } else {
            if (direction > 0) {
                int rectbot = visibleRect.y + visibleRect.height;
                Component comp = getComponentAt(insets.left, rectbot);

                // if there's no component at the edge, drop down 5
                // pixels and look again
                if (comp == this) {
                    comp = getComponentAt(insets.left, rectbot+5);
                }

                if (comp != this && comp != null) {
                    int compbot = comp.getY() + comp.getHeight();
                    return compbot - rectbot + 5;
                }

            } else {
                Component comp = getComponentAt(insets.left, visibleRect.y);

                // if there's no component at the edge, move up 5
                // pixels and look again
                if (comp == this) {
                    comp = getComponentAt(insets.left, visibleRect.y-5);
                }

                if (comp != this && comp != null) {
                    return visibleRect.y - comp.getY() + 5;
                }
            }
        }

        return DEFAULT_SCROLL_AMOUNT;
    }

    // documentation inherited from interface
    public int getScrollableBlockIncrement (
        Rectangle visibleRect, int orientation, int direction)
    {
        if (orientation == SwingConstants.HORIZONTAL) {
            return visibleRect.width;
        } else {
            return visibleRect.height;
        }
    }

    // documentation inherited from interface
    public boolean getScrollableTracksViewportWidth ()
    {
        return false;
    }

    // documentation inherited from interface
    public boolean getScrollableTracksViewportHeight ()
    {
        return false;
    }

    /** The number of pixels to scroll if we can't find a component to
     * scroll into view or if we're scrolling horizontally. */
    protected static final int DEFAULT_SCROLL_AMOUNT = 10;
}
