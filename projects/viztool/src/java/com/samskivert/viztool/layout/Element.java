//
// $Id: Element.java,v 1.1 2001/07/13 23:25:13 mdb Exp $

package com.samskivert.viztool.viz;

import java.awt.Point;
import java.awt.Dimension;

/**
 * A page is composed of elements which have some rectangular dimension.
 * They can be laid out by <code>ElementLayout</code> implementations in a
 * general purpose way.
 */
public interface Element
{
    /**
     * Returns the name of this element.
     */
    public String getName ();

    /**
     * Returns the size of this element. All coordinates are in points.
     */
    public Dimension getSize ();

    /**
     * Sets the upper left position of this element. All coordinates are
     * in points.
     */
    public void setLocation (int x, int y);

    /**
     * Sets the page number of this element.
     */
    public void setPage (int pageno);

    /**
     * Returns the page number previously set by <code>setPage</code>.
     */
    public int getPage ();
}
