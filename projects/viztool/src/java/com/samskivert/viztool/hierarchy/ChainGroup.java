//
// $Id: ChainGroup.java,v 1.1 2001/07/14 00:55:21 mdb Exp $

package com.samskivert.viztool.viz;

import java.awt.Dimension;
import java.awt.Point;
import java.io.PrintStream;
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
    public Dimension layout (int pointSize)
    {
        // lay out the internal structure of our chains
        ChainLayout clay = new CascadingChainLayout();
        for (int i = 0; i < _roots.size(); i++) {
            Chain chain = (Chain)_roots.get(i);
            chain.layout(pointSize, clay);
        }

        // arrange them on the page
        ElementLayout elay = new PackedColumnElementLayout();
        Dimension[] dims = elay.layout(_roots, MAX_WIDTH, MAX_HEIGHT);

        // for now we're punting and assume that no group will exceed a
        // single page in size
        _size = new Dimension();
        _size.width = dims[0].width + 2*BORDER;
        _size.height = dims[0].height + 2*BORDER;

        return _size;
    }

    /**
     * Renders the chains in this group to the supplied output stream.
     * This function requires that <code>layoutGroup</code> has previously
     * been called to lay out the group's chains.
     *
     * @return the size of the rectangle occupied by the rendered chain
     * group.
     *
     * @see #layoutGroup
     */
    public void render (int pointSize, PrintStream out, int x, int y)
    {
        // render our chains
        ChainRenderer renderer = new CascadingChainRenderer();
        for (int i = 0; i < _roots.size(); i++) {
            Chain chain = (Chain)_roots.get(i);
            Point loc = chain.getLocation();
            renderer.renderChain(chain, out, pointSize,
                                 x + BORDER, y + BORDER);
        }

        // print our title and a box around our border
        out.println("/tname (" + _pkg + ") def");
        out.println("/twid tname stringwidth pop def");

        int bx = x + BORDER + 2, by = y - pointSize/2;
        out.println(bx + " " + by + " moveto");
        out.println("tname abshow");

        out.println((x + BORDER - 2) + " " + y + " moveto");
        out.println("-" + (BORDER - 2) + " 0 rlineto");
        out.println("0 " + _size.height + " rlineto");
        out.println(_size.width + " 0 rlineto");
        out.println("0 -" + _size.height + " rlineto");
        out.println((_size.width - BORDER - 4) + " twid sub neg 0 rlineto");
        out.println("stroke");
    }

    protected String _pkg;
    protected ArrayList _roots;
    protected Dimension _size;

    protected static final int PAGE_WIDTH = (int)(72 * 7.5);
    protected static final int PAGE_HEIGHT = (int)(72 * 10);

    protected static final int BORDER = 72/8;

    protected static final int MAX_WIDTH = PAGE_WIDTH - 2*BORDER;
    protected static final int MAX_HEIGHT = PAGE_HEIGHT - 2*BORDER;
}
