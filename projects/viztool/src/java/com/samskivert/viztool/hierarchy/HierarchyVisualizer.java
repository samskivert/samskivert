//
// $Id: HierarchyVisualizer.java,v 1.2 2001/07/13 23:25:13 mdb Exp $

package com.samskivert.viztool.viz;

import java.awt.Dimension;
import java.awt.Point;
import java.util.*;

import com.samskivert.viztool.Log;

/**
 * The hierarchy visualizer displays inheritance hierarchies in a compact
 * representation so that an entire package can be displayed on a single
 * page (or small number of pages).
 */
public class HierarchyVisualizer
{
    /**
     * Constructs a hierarchy visualizer with the supplied enumerator as
     * its source of classes. If the hierarchy enumerator should be
     * limited to a particular set of classes, a filter enumerator should
     * be supplied that returns only the classes to be visualized.
     *
     * @param pkgroot The name of the package that is being visualized.
     * @param enum The enumerator that will return the names of all of the
     * classes in the specified package.
     */
    public HierarchyVisualizer (String pkgroot, Iterator iter)
    {
        // keep track of the package root
        _pkgroot = pkgroot;

        // dump all the classes into an array list so that we can
        // repeatedly scan through the list
        _classes = new ArrayList();
        while (iter.hasNext()) {
            _classes.add(iter.next());
        }

        // process the classes provided by our enumerator
        _roots = ChainUtil.buildChains(pkgroot, _classes.iterator());

        // now we need to sort out 
    }

    /**
     * Processes the hierarchy of class chains assigning coordinates to
     * all of the chains and subchains so that they can be used to
     * generate a visualization.
     */
    public void layoutChains (int pointSize)
    {
        // lay out the internal structure of our chains
        ChainLayout clay = new CascadingChainLayout();
        for (int i = 0; i < _roots.size(); i++) {
            Chain chain = (Chain)_roots.get(i);
            chain.layout(pointSize, clay);
        }

        // arrange them on the page
        ElementLayout elay = new PackedColumnElementLayout();
        Dimension[] pdims = elay.layout(_roots, PAGE_WIDTH, PAGE_HEIGHT);

        // render them
        for (int p = 0; p < pdims.length; p++) {
            ChainRenderer renderer = new CascadingChainRenderer();
            for (int i = 0; i < _roots.size(); i++) {
                Chain chain = (Chain)_roots.get(i);
                // skip chains not on this page
                if (chain.getPage() != p) {
                    continue;
                }
                Point loc = chain.getLocation();
                renderer.renderChain(chain, System.out, pointSize,
                                     X_MARGIN, Y_MARGIN);
            }
            System.out.println("showpage");
        }
    }

    public void dumpClasses ()
    {
        ChainUtil.dumpClasses(System.err, _roots);
    }

    protected String _pkgroot;
    protected ArrayList _roots;
    protected ArrayList _classes;

    protected static final int PAGE_WIDTH = (int)(72 * 7.5);
    protected static final int PAGE_HEIGHT = (int)(72 * 10);

    protected static final int X_MARGIN = 72/2;
    protected static final int Y_MARGIN = 72/2;
}
