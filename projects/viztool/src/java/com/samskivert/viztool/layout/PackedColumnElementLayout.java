//
// $Id: PackedColumnElementLayout.java,v 1.1 2001/07/13 23:25:13 mdb Exp $

package com.samskivert.viztool.viz;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Lays out the elements in columns of balanced height with elements
 * vertically arranged from tallest to shortest.
 */
public class PackedColumnElementLayout implements ElementLayout
{
    // docs inherited from interface
    public Dimension[] layout (List elements, int pageWidth, int pageHeight)
    {
        // create a new list containing the elements whose order we can
        // manipulate willy nilly
        Element[] elems = new Element[elements.size()];
        elements.toArray(elems);
        Arrays.sort(elems, HEIGHT_COMP);

        // lay out the elements across the page
        ArrayList pagedims = new ArrayList();
        int x = 0, y = 0, rowheight = 0, maxwidth = 0, pageno = 0;
        for (int i = 0; i < elems.length; i++) {
            Dimension size = elems[i].getSize();

            // see if we fit into this row or not (but force placement if
            // we're currently at the left margin)
            if ((x > 0) && ((x + size.width) > pageWidth)) {
                // track our maxwidth
                if (x > maxwidth) {
                    maxwidth = x;
                }
                // move down to the next row
                x = 0;
                y += (rowheight + GAP);
                // reset our max rowheight
                rowheight = size.height;
            }

            // make sure we fit on this page (but force placement if we're
            // currently at the top margin)
            if ((y > 0) && ((y + size.height) > pageHeight)) {
                // make a note of how big the current page is
                pagedims.add(new Dimension(maxwidth, y));
                // move to the next page
                x = 0;
                y = 0;
                rowheight = size.height;
                maxwidth = 0;
                pageno++;
            }

            // lay this element out at our current coordinates
            elems[i].setLocation(x, y);
            elems[i].setPage(pageno);

            // keep track of the maximum row height
            if (size.height > rowheight) {
                rowheight = size.height;
            }

            // advance in the x direction
            x += (size.width + GAP);
        }

        // make a note of how big the final page is
        pagedims.add(new Dimension(maxwidth, y+rowheight));
        Dimension[] dims = new Dimension[pagedims.size()];
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
            int diff = e2.getSize().height - e1.getSize().height;

            // if they are the same height, sort alphabetically
            return (diff != 0) ? diff : e1.getName().compareTo(e2.getName());
        }

        public boolean equals (Object other)
        {
            return (other == this);
        }
    }

    // hard coded for now, half inch margins
    protected static final int GAP = 72/4;

    protected static final Comparator HEIGHT_COMP = new HeightComparator();
}
