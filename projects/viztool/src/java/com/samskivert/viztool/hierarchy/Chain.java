//
// $Id: Chain.java,v 1.10 2001/11/30 22:57:31 mdb Exp $
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

package com.samskivert.viztool.hierarchy;

import java.util.*;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import com.samskivert.viztool.layout.Element;

/**
 * A chain is used by the hierarchy visualizer to represent inheritance
 * chains.
 */
public class Chain implements Element
{
    /**
     * Constructs a chain with the specified class as its root.
     */
    public Chain (String name, Class root, boolean inpkg)
    {
        _name = name;
        _root = root;
        _inpkg = inpkg;
    }

    /**
     * Returns the name of this chain (which should be used when
     * displaying the chain).
     */
    public String getName ()
    {
        return _name;
    }

    /**
     * Returns the class that forms the root of this chain.
     */
    public Class getRoot ()
    {
        return _root;
    }

    /**
     * Returns true if the class represented by this point in the chain is
     * in the package we are visualizing, false if it is not.
     */
    public boolean inPackage ()
    {
        return _inpkg;
    }

    /**
     * Returns the name of the class that forms the root of this chain.
     */
    public String getRootName ()
    {
        return _root.getName();
    }

    /**
     * Sorts this chain's children and the children's children so that a
     * particular ordering is observed throughout the tree (generally
     * alphabetical, but other orderings could be used).
     */
    public void sortChildren (Comparator comp)
    {
        // my kingdom for List.sort() or even ArrayList.sort()... sigh.
        Chain[] kids = new Chain[_children.size()];
        _children.toArray(kids);
        Arrays.sort(kids, comp);
        _children.clear();
        for (int i = 0; i < kids.length; i++) {
            // sort each child
            kids[i].sortChildren(comp);
            _children.add(kids[i]);
        }
    }

    /**
     * Returns the names of the interfaces implemented by this class.
     */
    public String[] getImplementsNames ()
    {
        Class[] ifaces = _root.getInterfaces();
        String[] names = new String[ifaces.length];
        String pkg = ChainUtil.pkgFromClass(_root.getName());

        for (int i = 0; i < ifaces.length; i++) {
            String name = ifaces[i].getName();
            String ipkg = ChainUtil.pkgFromClass(name);
            if (pkg.equals(ipkg)) {
                names[i] = ChainUtil.nameFromClass(name);
            } else {
                names[i] = removeOverlap(pkg, name);
            }
        }

        return names;
    }

    protected static String removeOverlap (String pkg, String name)
    {
        // strip off package elements until we've eliminated all but one
        // level of overlap
        String overlap = "";
        int didx;

        while ((didx = pkg.indexOf(".", overlap.length()+1)) != -1) {
            // see if this chunk still overlaps
            String prefix = pkg.substring(0, didx);
            if (name.startsWith(prefix)) {
                overlap = prefix;
            } else {
                // we've stopped overlapping, we need to back up one
                // element and then we're good to go
                if ((didx = overlap.lastIndexOf(".")) == -1) {
                    overlap = "";
                } else {
                    overlap = overlap.substring(0, didx);
                }
                break;
            }
        }

        // if there's an overlap, remove it
        if (overlap.length() > 0) {
            return ".." + name.substring(overlap.length());
        } else {
            return name;
        }
    }

    /**
     * Returns the names of the inner classes declared by this class.
     */
    public String[] getDeclaresNames ()
    {
        Class[] decls = _root.getDeclaredClasses();
        ArrayList names = new ArrayList();

        for (int i = 0; i < decls.length; i++) {
            String name = decls[i].getName();

            // strip off anything up to and including the dollar
            int didx = name.indexOf("$");
            if (didx != -1) {
                name = name.substring(didx+1);
            }

            // if this inner class name is a number, it's an anonymous
            // class and we want to skip it
            try {
                Integer.parseInt(name);
                continue;
            } catch (NumberFormatException nfe) {
            }

            // otherwise it passes the test
            names.add(name);
        }

        String[] anames = new String[names.size()];
        names.toArray(anames);
        return anames;
    }

    /**
     * Returns a <code>Rectangle2D</code> instance representing the size
     * of this chain (and all contained subchains).
     */
    public Rectangle2D getBounds ()
    {
        return _bounds;
    }

    /**
     * Sets the bounds of this chain.
     *
     * @see #getBounds
     */
    public void setBounds (double x, double y, double width, double height)
    {
        _bounds.setRect(x, y, width, height);
    }

    /**
     * Returns an array list containing the children chains of this chain.
     * If this chain has no children the list will be of zero length but
     * will not be null. This list should <em>not</em> be modified. Oh,
     * for a <code>const</code> keyword.
     */
    public ArrayList getChildren ()
    {
        return _children;
    }

    /**
     * Lays out all of the children of this chain and then requests that
     * the supplied layout manager arrange those children and compute the
     * dimensions of this chain based on all of that information.
     *
     * @param gfx the graphics context to use when computing dimensions.
     * @param cviz the chain visualizer to be used for laying out.
     * @param width the width in which the chain must fit.
     * @param height the height in which the chain must fit.
     *
     * @return if the chain cannot be laid out in the required dimensions,
     * branches of the chain will be removed so that it can fit into the
     * necessary dimensions. A new chain will be created with the
     * remaining branches which should be laid out itself so that it too
     * may further prune itself to fit into the necessary dimensions. If
     * the chain fits into the requested dimensions, null will be
     * returned.
     */
    public Chain layout (Graphics2D gfx, ChainVisualizer cviz,
                         double width, double height)
    {
        // lay everything out
        layout(gfx, cviz);

        // determine if we need to do some pruning (we only deal with
        // height pruning presently)
        if (_bounds.getHeight() <= height) {
            return null;
        }

        double x = 0, y = 0;
        Chain oflow = pruneOverflow(x, y, width, height);
        // if something wigged out and we try to overflow our whole selves
        // at this point (like this chain is one big fat chain with no
        // children that doesn't fit into the space) we must punt and
        // pretend like there's no overflow
        return (oflow == this) ? null : oflow;
    }

    protected void layout (Graphics2D gfx, ChainVisualizer cviz)
    {
        // first layout our children
        for (int i = 0; i < _children.size(); i++) {
            Chain child = (Chain)_children.get(i);
            child.layout(gfx, cviz);
        }

        // now lay ourselves out
        cviz.layoutChain(this, gfx);
    }

    protected Chain pruneOverflow (
        double x, double y, double width, double height)
    {
        // offset our current position by the position of this chain
        Rectangle2D bounds = getBounds();
        x += bounds.getX();
        y += bounds.getY();

        Chain oflow = null;

        for (int i = 0; i < _children.size(); i++) {
            Chain child = (Chain)_children.get(i);

            // if we've switched to flowing over, just add this chain to
            // the overflow chain (and remove it from ourselves)
            if (oflow != null) {
                _children.remove(i--);
                oflow._children.add(child);
                continue;
            }

            Rectangle2D cbounds = child.getBounds();
            double cx = cbounds.getX(), cy = cbounds.getY();

            // if this child doesn't fit in the current space, we need to
            // break it up and overflow the extra nodes
            if (y + cy + cbounds.getHeight() > height) {
                Chain coflow = child.pruneOverflow(x, y, width, height);

                // make sure this child claims some sort of overflow (if
                // it doesn't something is wacked, but we'll try to deal)
                if (coflow != null) {
                    // if this entire child is overflowed
                    if (coflow == child) {
                        // remove the child from this chain and add it to our
                        // overflow chain
                        _children.remove(i--);
                    }
                    // regardless, add what we got to the overflow chain
                    oflow = createOverflowChain();
                    oflow._children.add(coflow);

                } else {
                    System.err.println("Overflowing child unable " +
                                       "to overflow " + child._name + ".");
                }
            }
        }

        // if we have no children (or we overflowed *all* of our children)
        // then we need to overflow ourselves rather than create a new
        // chain for overflowing
        if (_children.size() == 0) {
            // grab our kids back from the overflow chain
            if (oflow != null) {
                _children = oflow._children;
            }
            return this;

        } else {
            // otherwise, adjust our height to just enclose the height of
            // our last child because we removed some children and changed
            // our dimensions. this is sort of a hack because it assumes
            // that the chain visualizer isn't leaving any gap beyond the
            // bottom of a child chain but it's somewhat safe because if
            // they did, that gap would accumulate unaesthetically
            Chain child = (Chain)_children.get(_children.size()-1);
            Rectangle2D cbounds = child.getBounds();
            _bounds.setRect(_bounds.getX(), _bounds.getY(), _bounds.getWidth(),
                            cbounds.getY() + cbounds.getHeight());
        }

        return oflow;
    }

    protected Chain createOverflowChain ()
    {
        return new Chain(_name, _root, _inpkg);
    }

    /**
     * Adds a child to this chain. The specified class is assumed to
     * directly inherit from the class that is the root of this chain.
     */
    public void addClass (String name, Class child)
    {
        // we assume that the addition of a derived class is only done for
        // classes that are in the package we're visualizing. out of
        // package classes are only included as roots of chains that
        // subsequently contain classes that are in the package
        Chain chain = new Chain(name, child, true);
        if (!_children.contains(chain)) {
            _children.add(chain);
        }
    }

    /**
     * Locates the chain for the specified target class and returns it if
     * it is a registered child of this chain. Returns null if no child
     * chain of this chain contains the specified target class.
     */
    public Chain getChain (Class target)
    {
        if (_root.equals(target)) {
            return this;
        }

        // just do a depth first search because it's fun
        for (int i = 0; i < _children.size(); i++) {
            Chain chain = ((Chain)_children.get(i)).getChain(target);
            if (chain != null) {
                return chain;
            }
        }

        return null;
    }

    public boolean equals (Object other)
    {
        if (other == null) {
            return false;
        } else if (other instanceof Chain) {
            return ((Chain)other)._root.equals(_root);
        } else {
            return false;
        }
    }

    public String toString ()
    {
        StringBuffer out = new StringBuffer();
        toString("", out);
        return out.toString();
    }

    protected void toString (String indent, StringBuffer out)
    {
        out.append(indent).append(_name).append("\n");
        for (int i = 0; i < _children.size(); i++) {
            Chain child = (Chain)_children.get(i);
            child.toString(indent + "  ", out);
        }
    }

    protected String _name;
    protected Class _root;
    protected boolean _inpkg;

    protected ArrayList _children = new ArrayList();
    protected Rectangle2D _bounds = new Rectangle2D.Double();
}
