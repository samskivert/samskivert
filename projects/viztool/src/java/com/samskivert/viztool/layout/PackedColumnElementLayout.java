//
// $Id: PackedColumnElementLayout.java,v 1.3 2001/07/17 01:54:19 mdb Exp $

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
    public Rectangle2D[] layout (List elements,
                                 double pageWidth, double pageHeight)
    {
        // create a new list containing the elements whose order we can
        // manipulate willy nilly
        Element[] elems = new Element[elements.size()];
        elements.toArray(elems);
        Arrays.sort(elems, HEIGHT_COMP);

        System.out.println("Laying out in " +
                           pageWidth + "x" + pageHeight + ".");

        // lay out the elements across the page
        ArrayList pagedims = new ArrayList();
        double x = 0, y = 0, rowheight = 0, maxwidth = 0;
        int pageno = 0;

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
                // reset our max rowheight
                rowheight = bounds.getHeight();
            }

            // make sure we fit on this page (but force placement if we're
            // currently at the top margin)
            if ((y > 0) && ((y + bounds.getHeight()) > pageHeight)) {
                // make a note of how big the current page is
                pagedims.add(new Rectangle2D.Double(0, 0, maxwidth, y));
                // move to the next page
                x = 0;
                y = 0;
                rowheight = bounds.getHeight();
                maxwidth = 0;
                pageno++;
            }

            // lay this element out at our current coordinates
            elems[i].setBounds(x, y, bounds.getWidth(), bounds.getHeight());
//              elems[i].setPage(pageno);
            System.out.println("Laying out " + elems[i].getName() +
                               " at " + elems[i].getBounds() + ".");

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

        // make a note of how big the final page is
        pagedims.add(new Rectangle2D.Double(0, 0, maxwidth, y+rowheight));
        Rectangle2D[] dims = new Rectangle2D[pagedims.size()];
        pagedims.toArray(dims);
        return dims;
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
