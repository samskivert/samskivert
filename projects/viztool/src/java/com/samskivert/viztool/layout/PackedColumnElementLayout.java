//
// $Id: PackedColumnElementLayout.java,v 1.5 2001/07/24 18:07:35 mdb Exp $

package com.samskivert.viztool.viz;

import java.awt.geom.Rectangle2D;
import java.util.*;

/**
 * Lays out the elements in columns of balanced height with elements
 * vertically arranged from tallest to shortest.
 */
public class PackedColumnElementLayout implements ElementLayout
{
    // docs inherited from interface
    public Rectangle2D layout (List elements, double pageWidth,
                               double pageHeight, List overflow)
    {
        // sort the elements first by height then alphabetically
        Element[] elems = new Element[elements.size()];
        elements.toArray(elems);
        Arrays.sort(elems, HEIGHT_COMP);

        // lay out the elements across the page
        double x = 0, y = 0, rowheight = 0, maxwidth = 0;

        for (int i = 0; i < elems.length; i++) {
            Rectangle2D bounds = elems[i].getBounds();

            // see if we fit into this row or not (but force placement if
            // we're currently at the left margin)
            if ((x > 0) && ((x + bounds.getWidth()) > pageWidth)) {
                // strip off the trailing GAP
                x -= GAP;
                // track our maxwidth
                if (x > maxwidth) {
                    maxwidth = x;
                }
                // move down to the next row
                x = 0;
                y += (rowheight + GAP);
                // reset our max rowheight (we set it to -GAP because we
                // will add rowheight to our last y position to compute
                // the total height and if no more elements are laid out,
                // we'll want to remove that trailing gap)
                rowheight = -GAP;
            }

            // make sure we fit on this page (but force placement if we're
            // currently at the top margin)
            if ((y > 0) && ((y + bounds.getHeight()) > pageHeight)) {
                // if we didn't fit, we go onto the overflow list
                overflow.add(elems[i]);
                // and continue on because maybe some shorter elements
                // further down the list will fit
                continue;
            }

            // lay this element out at our current coordinates
            elems[i].setBounds(x, y, bounds.getWidth(), bounds.getHeight());

            // keep track of the maximum row height
            if (bounds.getHeight() > rowheight) {
                rowheight = bounds.getHeight();
            }

            // advance in the x direction
            x += (bounds.getWidth() + GAP);
        }

        // take a final stab at our maxwidth
        x -= GAP;
        if (x > maxwidth) {
            maxwidth = x;
        }

        return new Rectangle2D.Double(0, 0, maxwidth, y+rowheight);
    }

    protected static class HeightComparator implements Comparator
    {
        public int compare (Object o1, Object o2)
        {
            Element e1 = (Element)o1;
            Element e2 = (Element)o2;

            // tallest element wins
            int diff = (int)(e2.getBounds().getHeight() -
                             e1.getBounds().getHeight());

            // if they are the same height, sort alphabetically
            return (diff != 0) ? diff : e1.getName().compareTo(e2.getName());
        }

        public boolean equals (Object other)
        {
            return (other == this);
        }
    }

    // hard coded for now, half inch margins
    protected static final double GAP = 72/4;

    protected static final Comparator HEIGHT_COMP = new HeightComparator();
}
