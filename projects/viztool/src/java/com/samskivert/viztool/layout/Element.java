//
// $Id: Element.java,v 1.2 2001/07/17 01:54:19 mdb Exp $

package com.samskivert.viztool.viz;

import java.awt.geom.Rectangle2D;

/**
 * A page is composed of elements which have some rectangular bounds.
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
     * Returns the bounds of this element.
     */
    public Rectangle2D getBounds ();

    /**
     * Sets the bounds of this element.
     */
    public void setBounds (double x, double y, double width, double height);
}
