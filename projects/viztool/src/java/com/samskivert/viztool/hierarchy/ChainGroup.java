//
// $Id: ChainGroup.java,v 1.11 2001/08/12 04:36:58 mdb Exp $
// 
// viztool - a tool for visualizing collections of java classes
// Copyright (C) 2001 Michael Bayne
// 
// This program is free software; you can redistribute it and/or modify it
// under the terms of the GNU General Public License as published by the
// Free Software Foundation; either version 2.1 of the License, or (at your
// option) any later version.
// 
// This program is distributed in the hope that it will be useful, but
// WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// General Public License for more details.
// 
// You should have received a copy of the GNU General Public License along
// with this program; if not, write to the Free Software Foundation, Inc.,
// 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.samskivert.viztool.viz;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.font.TextLayout;
import java.util.*;

/**
 * A chain group is used to group together all of the classes from a
 * particular package.
 */
public class ChainGroup
{
    /**
     * Constructs a chain group for a particular package with the
     * specified package root and an iterator that is configured only to
     * return classes from the specified package.
     */
    public ChainGroup (String pkgroot, String pkg, Iterator iter)
    {
        // keep track of the package
        _pkg = pkg;

        // process the classes provided by our enumerator
        _roots = ChainUtil.buildChains(pkgroot, iter);

        // sort our roots
        for (int i = 0; i < _roots.size(); i++) {
            Chain root = (Chain)_roots.get(i);
            root.sortChildren(NAME_COMP);
        }

        // System.err.println(_roots.size() + " chains for " + pkg + ".");
    }

    protected ChainGroup (String pkg, ArrayList roots)
    {
        _pkg = pkg;
        _roots = roots;
    }

    /**
     * Returns the dimensions of this chain group. This value is only
     * valid after <code>layout</code> has been called.
     */
    public Rectangle2D getBounds ()
    {
        return _size;
    }

    /**
     * Sets the upper left coordinate of this group. The group itself
     * never looks at this information, but it will be made available as
     * the x and y coordinates of the rectangle returned by
     * <code>getBounds</code>.
     */
    public void setPosition (double x, double y)
    {
        _size.setRect(x, y, _size.getWidth(), _size.getHeight());
    }

    /**
     * Returns the page on which this group should be rendered.
     */
    public int getPage ()
    {
        return _page;
    }

    /**
     * Sets the page on which this group should be rendered.
     */
    public void setPage (int page)
    {
        _page = page;
    }

    /**
     * Lays out the chains in this group and returns the total size. If
     * the layout process requires that this chain group be split across
     * multiple pages, a new chain group containing the overflow chains
     * will be returned. If the group fits in the allotted space, null
     * will be returned.
     */
    public ChainGroup layout (
        Graphics2D gfx, double pageWidth, double pageHeight)
    {
        // we'll need room to incorporate our title
        TextLayout layout = new TextLayout(_pkg, FontPicker.getTitleFont(),
                                           gfx.getFontRenderContext());

        // we let the title stick halfway up out of our rectangular
        // bounding box
        Rectangle2D tbounds = layout.getBounds();
        double titleAscent = tbounds.getHeight()/2;

        // keep room for our border and title
        pageWidth -= 2*BORDER;
        pageHeight -= (2*BORDER + titleAscent);

        // lay out the internal structure of our chains
        ChainVisualizer clay = new CascadingChainVisualizer();
        for (int i = 0; i < _roots.size(); i++) {
            Chain chain = (Chain)_roots.get(i);
            Chain oflow = chain.layout(gfx, clay, pageWidth, pageHeight);
            // if this chain overflowed when being laid out, add the newly
            // created root to our list
            if (oflow != null) {
                _roots.add(i+1, oflow);
            }
        }

        // arrange them on the page
        ElementLayout elay = new PackedColumnElementLayout();
        ArrayList overflow = new ArrayList();
        _size = elay.layout(_roots, pageWidth, pageHeight, overflow);

        // make sure we're wide enough for our title
        double width = Math.max(_size.getWidth(), layout.getAdvance() + 4);

        // adjust for our border and title
        double height = _size.getHeight() + titleAscent;
        _size.setRect(0, 0, width + 2*BORDER, height + 2*BORDER);

        // if we have overflow elements, create a new chain group with
        // these elements, remove them from our roots list and be on our
        // way
        if (overflow.size() > 0) {
            // remove the overflow roots from our list
            for (int i = 0; i < overflow.size(); i++) {
                _roots.remove(overflow.get(i));
            }
            return new ChainGroup(_pkg, overflow);
        }

        return null;
    }

    /**
     * Renders the chains in this group to the supplied graphics object.
     * This function requires that <code>layout</code> has previously been
     * called to lay out the group's chains.
     *
     * @see #layout
     */
    public void render (Graphics2D gfx, double x, double y)
    {
        TextLayout layout = new TextLayout(_pkg, FontPicker.getTitleFont(),
                                           gfx.getFontRenderContext());

        // we let the title stick halfway up out of our rectangular
        // bounding box
        Rectangle2D tbounds = layout.getBounds();
        double titleAscent = tbounds.getHeight()/2;
        double dy = -tbounds.getY();

        // print our title
        layout.draw(gfx, (float)(x + BORDER + 2), (float)(y + dy));

        // shift everything down by the ascent of the title
        y += titleAscent;

        // translate to our rendering area
        double cx = x + BORDER;
        double cy = y + BORDER;
        gfx.translate(cx, cy);

        // render our chains
        ChainVisualizer renderer = new CascadingChainVisualizer();
        for (int i = 0; i < _roots.size(); i++) {
            Chain chain = (Chain)_roots.get(i);
            Rectangle2D bounds = chain.getBounds();
            // render the chain
            renderer.renderChain(chain, gfx);
        }

        // undo the translation
        gfx.translate(-cx, -cy);

        // print our border box
        double height = _size.getHeight() - titleAscent;
        GeneralPath path = new GeneralPath();
        path.moveTo((float)(x + BORDER), (float)y);
        path.lineTo((float)x, (float)y);
        path.lineTo((float)x, (float)(y + height));
        path.lineTo((float)(x + _size.getWidth()),
                    (float)(y + height));
        path.lineTo((float)(x + _size.getWidth()), (float)y);
        path.lineTo((float)(x + BORDER + layout.getAdvance() + 4), (float)y);
        gfx.draw(path);
    }

    public Chain getRoot (int index)
    {
        return (Chain)_roots.get(index);
    }

    public String toString ()
    {
        return "[pkg=" + _pkg + ", roots=" + _roots.size() +
            ", size=" + _size + ", page=" + _page + "]";
    }

    protected String _pkg;
    protected ArrayList _roots;

    protected Rectangle2D _size;
    protected int _page;

    protected static final double BORDER = 72/8;

    /**
     * Compares the names of two chains. Used to sort them into
     * alphabetical order.
     */
    protected static class NameComparator implements Comparator
    {
        public int compare (Object o1, Object o2)
        {
            Chain c1 = (Chain)o1;
            Chain c2 = (Chain)o2;
            return c1.getName().compareTo(c2.getName());
        }

        public boolean equals (Object other)
        {
            return (other == this);
        }
    }

    protected static final Comparator NAME_COMP = new NameComparator();
}
