//
// $Id: Chain.java,v 1.5 2001/07/17 07:18:09 mdb Exp $

package com.samskivert.viztool.viz;

import java.util.ArrayList;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

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
        String[] names = new String[decls.length];

        for (int i = 0; i < decls.length; i++) {
            String name = decls[i].getName();
            int didx = name.indexOf("$");
            names[i] = (didx == -1) ? name : name.substring(didx+1);
        }

        return names;
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
     */
    public void layout (Graphics2D gfx, ChainVisualizer cviz)
    {
        // first layout our children
        for (int i = 0; i < _children.size(); i++) {
            Chain child = (Chain)_children.get(i);
            child.layout(gfx, cviz);
        }

        // now lay ourselves out
        cviz.layoutChain(this, gfx);
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
