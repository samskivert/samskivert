//
// $Id: ElementLayout.java,v 1.3 2001/07/24 18:07:35 mdb Exp $

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
     * Lay out the supplied list of elements. Any elements that do not fit
     * into the allotted space should be added to the overflow list. The
     * supplied elements list should not be modified.
     *
     * @return the bounding dimensions of the collection of elements that
     * were laid out.
     */
    public Rectangle2D layout (List elements, double pageWidth,
                               double pageHeight, List overflow);
}
