//
// $Id: HierarchyVisualizer.java,v 1.11 2001/08/12 04:36:58 mdb Exp $
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
            // strip out inner classes, we'll catch those via their
            // declaring classes
            String name = (String)iter.next();
            if (name.indexOf("$") != -1) {
                continue;
            }
            _classes.add(name);
        }
        // System.err.println("Scanned " + _classes.size() + " classes.");

        // compile a list of all packages in our collection
        HashSet pkgset = new HashSet();
        iter = _classes.iterator();
        while (iter.hasNext()) {
            pkgset.add(ChainUtil.pkgFromClass((String)iter.next()));
        }

        // remove the packages on our exclusion list
        String expkg = System.getProperty("exclude");
        if (expkg != null) {
            StringTokenizer tok = new StringTokenizer(expkg, ":");
            while (tok.hasMoreTokens()) {
                pkgset.remove(tok.nextToken());
            }
        }

        // sort our package names
        _packages = new String[pkgset.size()];
        iter = pkgset.iterator();
        for (int i = 0; iter.hasNext(); i++) {
            _packages[i] = (String)iter.next();
        }
        Arrays.sort(_packages, Comparators.STRING);
        // System.err.println("Scanned " + _packages.length + " packages.");

        // now create chain groups for each package
        _groups = new ArrayList();
        for (int i = 0; i < _packages.length; i++) {
            PackageEnumerator penum = new PackageEnumerator(
                _packages[i], _classes.iterator(), false);
            _groups.add(new ChainGroup(pkgroot, _packages[i], penum));
        }
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
        for (int i = 0; i < _groups.size(); i++) {
            ChainGroup group = (ChainGroup)_groups.get(i);

            // skip groups not on this page
            if (group.getPage() != pageIndex) {
                continue;
            }

            Rectangle2D bounds = group.getBounds();
            group.render(gfx, bounds.getX(), bounds.getY());
            rendered++;
        }

        return (rendered > 0) ? PAGE_EXISTS : NO_SUCH_PAGE;
    }

    public void layout (Graphics2D gfx, double x, double y,
                        double width, double height)
    {
        double starty = y;
        int pageno = 0;

        // lay out our groups
        for (int i = 0; i < _groups.size(); i++) {
            ChainGroup group = (ChainGroup)_groups.get(i);

            // lay out the group in question
            ChainGroup ngrp = group.layout(gfx, width, height);
            // if the process of laying this group out caused it to become
            // split across pages, insert this new group into the list
            if (ngrp != null) {
                _groups.add(i+1, ngrp);
            }

            // determine if we need to skip to the next page or not
            Rectangle2D bounds = group.getBounds();
            if ((y > starty) && (y + bounds.getHeight() > height + starty)) {
                y = starty;
                pageno++;
            }

            // assign x and y coordinates to this group
            group.setPosition(x, y);
            // make a note of our page index
            group.setPage(pageno);

            // increment our y location
            y += (bounds.getHeight() + GAP);
        }
    }

    public void paint (Graphics2D gfx, int pageIndex)
    {
        // render the groups on the requested page
        for (int i = 0; i < _groups.size(); i++) {
            ChainGroup group = (ChainGroup)_groups.get(i);

            // skip groups not on this page
            if (group.getPage() != pageIndex) {
                continue;
            }

            Rectangle2D bounds = group.getBounds();
            group.render((Graphics2D)gfx, bounds.getX(), bounds.getY());
        }
    }

    protected String _pkgroot;
    protected ArrayList _classes = new ArrayList();

    protected String[] _packages;
    protected ArrayList _groups;

    protected PageFormat _format;

    protected static final int PAGE_WIDTH = (int)(72 * 7.5);
    protected static final int PAGE_HEIGHT = (int)(72 * 10);

    protected static final int X_MARGIN = 72/2;
    protected static final int Y_MARGIN = 72/2;

    protected static final int GAP = 72/4;
}
