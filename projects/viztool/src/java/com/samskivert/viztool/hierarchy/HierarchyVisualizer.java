//
// $Id: HierarchyVisualizer.java,v 1.1 2001/07/04 18:24:07 mdb Exp $

package com.samskivert.viztool.viz;

import java.util.ArrayList;

import com.samskivert.viztool.Log;
import com.samskivert.viztool.enum.Enumerator;

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
    public HierarchyVisualizer (String pkgroot, Enumerator enum)
    {
        // keep track of these
        _pkgroot = pkgroot;
        _enum = enum;

        // process the classes provided by our enumerator
        _roots = ChainUtil.buildChains(pkgroot, enum);
    }

    /**
     * Processes the hierarchy of class chains assigning coordinates to
     * all of the chains and subchains so that they can be used to
     * generate a visualization.
     */
    public void layoutChains (int pointSize)
    {
    }

    public void dumpClasses ()
    {
        ChainUtil.dumpClasses(_roots);
    }

    protected String _pkgroot;
    protected Enumerator _enum;
    protected ArrayList _roots;
}
