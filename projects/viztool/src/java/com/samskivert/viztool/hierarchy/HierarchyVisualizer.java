//
// $Id: HierarchyVisualizer.java,v 1.3 2001/07/14 00:55:21 mdb Exp $

package com.samskivert.viztool.viz;

import java.awt.Dimension;
import java.awt.Point;
import java.io.PrintStream;
import java.util.*;

import com.samskivert.viztool.Log;
import com.samskivert.viztool.enum.PackageEnumerator;
import com.samskivert.util.Comparators;

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
        while (iter.hasNext()) {
            _classes.add(iter.next());
        }

        // compile a list of all packages in our collection
        HashSet pkgset = new HashSet();
        iter = _classes.iterator();
        while (iter.hasNext()) {
            pkgset.add(ChainUtil.pkgFromClass((String)iter.next()));
        }

        // sort our package names
        _packages = new String[pkgset.size()];
        iter = pkgset.iterator();
        for (int i = 0; iter.hasNext(); i++) {
            _packages[i] = (String)iter.next();
        }
        Arrays.sort(_packages, Comparators.STRING);

        // now create chain groups for each package
        _groups = new ChainGroup[_packages.length];
        for (int i = 0; i < _groups.length; i++) {
            PackageEnumerator penum = new PackageEnumerator(
                _packages[i], _classes.iterator(), false);
            _groups[i] = new ChainGroup(pkgroot, _packages[i], penum);
        }
    }

    /**
     * Lays out and renders each of the chain groups that make up this
     * package hierarchy visualization.
     */
    public void render (int pointSize, PrintStream out)
    {
        int x = X_MARGIN, y = Y_MARGIN;

        // print the preamble for the first page
        out.println("0 setlinewidth");

        for (int i = 0; i < _groups.length; i++) {
            // lay out the group in question
            Dimension dims = _groups[i].layout(pointSize);

            // determine if we need to skip to the next page or not
            if ((y > 0) && (y + dims.height > PAGE_HEIGHT)) {
                // print the postamble for this page and preamble for the
                // new page
                out.println("showpage");
                out.println("0 setlinewidth");
                y = Y_MARGIN;
            }

            // render the group at the requested location
            _groups[i].render(pointSize, out, x, y);

            // increment our y location
            y += (dims.height + GAP);
        }

        // print the postamble for the final page
        out.println("showpage");
    }

    protected String _pkgroot;
    protected ArrayList _classes = new ArrayList();

    protected String[] _packages;
    protected ChainGroup[] _groups;

    protected static final int PAGE_WIDTH = (int)(72 * 7.5);
    protected static final int PAGE_HEIGHT = (int)(72 * 10);

    protected static final int X_MARGIN = 72/2;
    protected static final int Y_MARGIN = 72/2;

    protected static final int GAP = 72/4;
}
