//
// $Id: ElementLayout.java,v 1.2 2001/07/17 01:54:19 mdb Exp $

package com.samskivert.viztool.viz;

import java.awt.geom.Rectangle2D;
import java.util.List;

/**
 * The element layout is used to lay a collection of elements out on a
 * page. It computes the desired position of each element and sets it via
 * <code>setBounds()</code> with the expectation that the location of the
 * elements will be used later in the rendering process.
 */
public interface ElementLayout
{
    /**
     * Lay out the supplied list of elements. Page numbers should be
     * assigned to all elements if the layout spans multiple pages.
     *
     * @return an array of rectangle objects representing the width and
     * height of each page that was laid out.
     */
    public Rectangle2D[] layout (List elements,
                                 double pageWidth, double pageHeight);
}
