//
// $Id: Chain.java,v 1.4 2001/07/17 01:54:19 mdb Exp $

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
    public Chain (String name, Class root)
    {
        _name = name;
        _root = root;
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
     * Returns the name of the class that forms the root of this chain.
     */
    public String getRootName ()
    {
        return _root.getName();
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
        Chain chain = new Chain(name, child);
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

    protected ArrayList _children = new ArrayList();
    protected Rectangle2D _bounds = new Rectangle2D.Double();
}
