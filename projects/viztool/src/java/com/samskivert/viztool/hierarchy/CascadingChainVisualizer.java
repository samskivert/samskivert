//
// $Id: CascadingChainVisualizer.java,v 1.1 2001/07/13 23:25:13 mdb Exp $

package com.samskivert.viztool.viz;

import java.awt.Dimension;
import java.util.ArrayList;

/**
 * The cascading chain layout lays out chains in the standard cascading
 * format that looks something like this:
 *
 * <pre>
 * Foo
 * |
 * +-> Bar
 * |   |
 * |   +-> Biff
 * |
 * +-> Baz
 * </pre>
 *
 * It should be used in tandem with the
 * <code>CascadingChainRenderer</code>.
 *
 * @see CascadingChainRenderer
 */
public class CascadingChainLayout
    implements ChainLayout, CascadingConstants
{
    // docs inherited from interface
    public void layoutChain (Chain chain, int pointSize)
    {
        // the header will be the name of this chain surrounded by N
        // points of space and a box
        int hwid = PostscriptUtil.estimateWidth(chain.getName(), pointSize) +
            2*HEADER_BORDER;
        int hhei = 2*HEADER_BORDER + pointSize;
        int maxwid = hwid;

        // the children will be below the name of this chain and inset by
        // four points to make space for the connecty lines
        int x = GAP, y = hhei;
        ArrayList kids = chain.getChildren();

        for (int i = 0; i < kids.size(); i++) {
            Chain kid = (Chain)kids.get(i);
            Dimension ksize = kid.getSize();
            y += GAP; // add the gap
            kid.setLocation(x, y);
            y += ksize.height; // add the dimensions of the kid
//              System.err.println("Locating " + kid.getName() +
//                                 " at +" + x + "+" + y + ".");
            // track max width
            if (maxwid < (x + ksize.width)) {
                maxwid = x + ksize.width;
            }
        }

        // set the dimensions of the main chain
//          System.err.println("Sizing " + chain.getName() +
//                             " to " + maxwid + "x" + y + ".");
        chain.setSize(maxwid, y);
    }
}
