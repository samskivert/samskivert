//
// $Id: ChainGroup.java,v 1.3 2001/07/17 05:16:16 mdb Exp $

package com.samskivert.viztool.viz;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.font.TextLayout;
import java.util.ArrayList;
import java.util.Iterator;

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
    }

    /**
     * Lays out the chains in this group and returns the total size.
     */
    public Rectangle2D layout (Graphics2D gfx, double pageWidth,
                               double pageHeight)
    {
        // lay out the internal structure of our chains
        ChainVisualizer clay = new CascadingChainVisualizer();
        for (int i = 0; i < _roots.size(); i++) {
            Chain chain = (Chain)_roots.get(i);
            chain.layout(gfx, clay);
        }

        // we'll need room to incorporate our title
        TextLayout layout = new TextLayout(_pkg, gfx.getFont(),
                                           gfx.getFontRenderContext());

        // we let the title stick halfway up out of our rectangular
        // bounding box
        Rectangle2D tbounds = layout.getBounds();
        double titleAscent = tbounds.getHeight()/2;

        // keep room for our border and title
        pageWidth -= 2*BORDER;
        pageHeight -= (2*BORDER + titleAscent);

        // arrange them on the page
        ElementLayout elay = new PackedColumnElementLayout();
        Rectangle2D[] dims = elay.layout(_roots, pageWidth, pageHeight);

        // for now we're punting and assume that no group will exceed a
        // single page in size
        double width = dims[0].getWidth();
        double height = dims[0].getHeight() + titleAscent;

        // make sure we're wide enough for our title
        width = Math.max(width, layout.getAdvance() + 4);

        _size = new Rectangle2D.Double();
        _size.setRect(0, 0, width + 2*BORDER, height + 2*BORDER);

        System.out.println("L(" + _pkg + ") " + _size.getWidth() + "x" +
            _size.getHeight() + "+" + _size.getX() + "+" + _size.getY() + ".");
        return _size;
    }

    /**
     * Renders the chains in this group to the supplied graphics object.
     * This function requires that <code>layoutGroup</code> has previously
     * been called to lay out the group's chains.
     *
     * @see #layoutGroup
     */
    public void render (Graphics2D gfx, double x, double y)
    {
        TextLayout layout = new TextLayout(_pkg, gfx.getFont(),
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

    protected String _pkg;
    protected ArrayList _roots;
    protected Rectangle2D _size;

    protected static final double BORDER = 72/8;
}
