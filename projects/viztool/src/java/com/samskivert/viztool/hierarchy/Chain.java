//
// $Id: Chain.java,v 1.1 2001/07/04 18:24:07 mdb Exp $

package com.samskivert.viztool.viz;

import java.util.ArrayList;
import java.awt.Dimension;
import java.awt.Point;

/**
 * A chain is used by the hierarchy visualizer to represent inheritance
 * chains.
 */
public class Chain
{
    /**
     * Constructs a chain with the specified class as its root.
     */
    public Chain (Class root)
    {
        _root = root;
    }

    /**
     * Returns a <code>Dimension</code> instance representing the size of
     * this chain (and all contained subchains) in whatever coordinates
     * are being used to diagram this chain.
     */
    public Dimension getSize ()
    {
        return _size;
    }

    /**
     * Returns the location of this chain in whatever coordinate system
     * that is being used to diagram this chain.
     */
    public Point getLocation ()
    {
        return _location;
    }

    /**
     * Sets the size of this chain.
     *
     * @see #getSize
     */
    public void setSize (int width, int height)
    {
        _size = new Dimension(width, height);
    }

    /**
     * Sets the location of this chain.
     *
     * @see #getLocation
     */
    public void setLocation (int x, int y)
    {
        _location = new Point(x, y);
    }

    /**
     * Adds a child to this chain. The specified class is assumed to
     * directly inherit from the class that is the root of this chain.
     */
    public void addClass (Class child)
    {
        Chain chain = new Chain(child);
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
        out.append(indent).append(_root.getName()).append("\n");
        for (int i = 0; i < _children.size(); i++) {
            Chain child = (Chain)_children.get(i);
            child.toString(indent + "  ", out);
        }
    }

    protected Class _root;
    protected ArrayList _children = new ArrayList();
    protected Dimension _size = new Dimension(0, 0);
    protected Point _location = new Point(0, 0);
}
