//
// $Id: ElementLayout.java,v 1.1 2001/07/13 23:25:13 mdb Exp $

package com.samskivert.viztool.viz;

import java.awt.Dimension;
import java.util.List;

/**
 * The element layout is used to lay a collection of elements out on a
 * page. It computes the desired position of each element and sets it via
 * <code>setLocation()</code> with the expectation that the location of the
 * elements will be used later in the rendering process.
 */
public interface ElementLayout
{
    /**
     * Lay out the supplied list of elements. Page numbers should be
     * assigned to all elements if the layout spans multiple pages.
     *
     * @return an array of dimension objects representing the width and
     * height of each page that was laid out.
     */
    public Dimension[] layout (List elements, int pageWidth, int pageHeight);
}
