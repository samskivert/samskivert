//
// $Id: CascadingChainRenderer.java,v 1.1 2001/07/13 23:25:13 mdb Exp $

package com.samskivert.viztool.viz;

import java.awt.Dimension;
import java.awt.Point;
import java.io.PrintStream;
import java.util.ArrayList;

/**
 * Renders a chain that has been layed out by the cascading chain layout
 * manager.
 *
 * @see CascadingChainLayout
 */
public class CascadingChainRenderer
    implements ChainRenderer, CascadingConstants
{
    // docs inherited from interface
    public void renderChain (Chain chain, PrintStream out,
                             int pointSize, int x, int y)
    {
        Point loc = chain.getLocation();

        // figure out where we'll be located
        x += loc.x;
        y += loc.y;

//          System.err.println("Rendering " + chain.getName() +
//                             " at +" + x + "+" + y + ".");

        // work out some useful stuff
        out.println("gsave");
        out.println("/size " + pointSize + " def");
        out.println(x + " " + y + " translate");
        out.println("/cname (" + chain.getName() + ") def");
        out.println("/nwid cname stringwidth pop def");
        out.println("/nhei size def");
        out.println("/border " + HEADER_BORDER + " def");
        out.println("/dborder border 2 mul def");

        // stroke a box that will contain the name
        out.println("0 0 nwid dborder add nhei dborder add rectstroke");
//        out.println("border border nwid nhei rectstroke");
        out.println("border border moveto cname abshow");

        // render our connecty lines
        ArrayList kids = chain.getChildren();
        if (kids.size() > 0) {
            Point kloc = ((Chain)kids.get(0)).getLocation();
            int half = kloc.x/2;
            out.println(half + " nhei dborder add moveto");

            for (int i = 0; i < kids.size(); i++) {
                Chain kid = (Chain)kids.get(i);
                kloc = kid.getLocation();
                out.println(half + " " +
                            (kloc.y + pointSize/2 + HEADER_BORDER) + " lineto");
                out.println(half + " 0 rlineto");
                out.println(half + " neg 0 rmoveto");
            }
            out.println("stroke");
        }
        out.println("grestore");

        // now render the kids
        for (int i = 0; i < kids.size(); i++) {
            Chain kid = (Chain)kids.get(i);
            renderChain(kid, out, pointSize, x, y);
        }
    }
}
