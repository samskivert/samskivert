//
// $Id: HierarchyVisualizer.java,v 1.6 2001/07/17 05:28:46 mdb Exp $

package com.samskivert.viztool.viz;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.print.*;
import java.util.*;

import com.samskivert.viztool.Log;
import com.samskivert.viztool.enum.PackageEnumerator;
import com.samskivert.util.Comparators;

/**
 * The hierarchy visualizer displays inheritance hierarchies in a compact
 * representation so that an entire package can be displayed on a single
 * page (or small number of pages).
 */
public class HierarchyVisualizer implements Printable
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

        // we'll need these for later
        _bounds = new Rectangle2D.Double[_packages.length];
        _pagenos = new int[_packages.length];
    }

    /**
     * Lays out and renders each of the chain groups that make up this
     * package hierarchy visualization.
     */
    public int print (Graphics g, PageFormat pf, int pageIndex)
        throws PrinterException
    {
        Graphics2D gfx = (Graphics2D)g;

        // adjust the stroke
        gfx.setStroke(new BasicStroke(0.1f));

        // only relay things out if the page format has changed
        if (!pf.equals(_format)) {
            // keep this around
            _format = pf;

            // and do the layout
            layout(gfx, pf.getImageableX(), pf.getImageableY(),
                   pf.getImageableWidth(), pf.getImageableHeight());
        }

        // render the groups on the requested page
        int rendered = 0;
        for (int i = 0; i < _groups.length; i++) {
            // skip groups not on this page
            if (_pagenos[i] != pageIndex) {
                continue;
            }
            _groups[i].render(gfx, _bounds[i].getX(), _bounds[i].getY());
            rendered++;
        }

        return (rendered > 0) ? PAGE_EXISTS : NO_SUCH_PAGE;
    }

    public void layout (Graphics2D gfx, double x, double y,
                        double width, double height)
    {
        double starty = x;
        int pageno = 0;

        // lay out our groups
        for (int i = 0; i < _groups.length; i++) {
            // lay out the group in question
            Rectangle2D bounds = _groups[i].layout(gfx, width, height);

            // determine if we need to skip to the next page or not
            if ((y > 0) && (y + bounds.getHeight() > height)) {
                y = starty;
                pageno++;
            }

            // assign x and y coordinates to this group
            bounds.setRect(x, y, bounds.getWidth(), bounds.getHeight());
            // and store it
            _bounds[i] = bounds;
            // also make a note of our page index
            _pagenos[i] = pageno;

            // increment our y location
            y += (bounds.getHeight() + GAP);
        }
    }

    public void paint (Graphics2D gfx, int pageIndex)
    {
        // render the groups on the requested page
        for (int i = 0; i < _groups.length; i++) {
            // skip groups not on this page
            if (_pagenos[i] != pageIndex) {
                continue;
            }
            _groups[i].render((Graphics2D)gfx,
                              _bounds[i].getX(), _bounds[i].getY());
        }
    }

    protected String _pkgroot;
    protected ArrayList _classes = new ArrayList();

    protected String[] _packages;
    protected ChainGroup[] _groups;

    protected PageFormat _format;
    protected Rectangle2D[] _bounds;
    protected int[] _pagenos;

    protected static final int PAGE_WIDTH = (int)(72 * 7.5);
    protected static final int PAGE_HEIGHT = (int)(72 * 10);

    protected static final int X_MARGIN = 72/2;
    protected static final int Y_MARGIN = 72/2;

    protected static final int GAP = 72/4;
}
