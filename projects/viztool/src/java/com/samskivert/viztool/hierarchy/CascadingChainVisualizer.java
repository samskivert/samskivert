//
// $Id: CascadingChainVisualizer.java,v 1.10 2001/11/30 22:57:31 mdb Exp $
// 
// viztool - a tool for visualizing collections of java classes
// Copyright (C) 2001 Michael Bayne
// 
// This program is free software; you can redistribute it and/or modify it
// under the terms of the GNU General Public License as published by the
// Free Software Foundation; either version 2.1 of the License, or (at your
// option) any later version.
// 
// This program is distributed in the hope that it will be useful, but
// WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// General Public License for more details.
// 
// You should have received a copy of the GNU General Public License along
// with this program; if not, write to the Free Software Foundation, Inc.,
// 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.samskivert.viztool.hierarchy;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.TextLayout;
import java.awt.font.FontRenderContext;
import java.awt.geom.*;
import java.util.ArrayList;

import com.samskivert.viztool.util.FontPicker;

/**
 * The cascading chain visualizer lays out chains in the standard
 * cascading format that looks something like this:
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
 */
public class CascadingChainVisualizer
    implements ChainVisualizer
{
    // docs inherited from interface
    public void layoutChain (Chain chain, Graphics2D gfx)
    {
        // create a text layout based on the current rendering conditions
        Font font = chain.getRoot().isInterface() ?
            FontPicker.getInterfaceFont() : FontPicker.getClassFont();
        TextLayout layout = new TextLayout(chain.getName(), font,
                                           gfx.getFontRenderContext());

        // the header will be the name of this chain surrounded by N
        // points of space and a box
        Rectangle2D bounds = getTextBox(gfx, layout);

        // add our inner classes and interface implementations, but only
        // if we're not an out of package class
        if (chain.inPackage()) {
            String[] impls = chain.getImplementsNames();
            for (int i = 0; i < impls.length; i++) {
                bounds = accomodate(bounds, impls[i],
                                    FontPicker.getImplementsFont(),
                                    gfx.getFontRenderContext());
            }
            String[] decls = chain.getDeclaresNames();
            for (int i = 0; i < decls.length; i++) {
                bounds = accomodate(bounds, decls[i],
                                    FontPicker.getDeclaresFont(),
                                    gfx.getFontRenderContext());
            }
        }

        double maxwid = bounds.getWidth();

        // the children will be below the name of this chain and inset by
        // four points to make space for the connecty lines
        double x = 2*GAP, y = bounds.getHeight();
        ArrayList kids = chain.getChildren();

        for (int i = 0; i < kids.size(); i++) {
            Chain kid = (Chain)kids.get(i);
            Rectangle2D kbounds = kid.getBounds();
            y += GAP; // add the gap
            kid.setBounds(x, y, kbounds.getWidth(), kbounds.getHeight());
            y += kbounds.getHeight(); // add the dimensions of the kid
            // track max width
            if (maxwid < (x + kbounds.getWidth())) {
                maxwid = x + kbounds.getWidth();
            }
        }

        // set the dimensions of the main chain
        Rectangle2D cbounds = chain.getBounds();
        chain.setBounds(cbounds.getX(), cbounds.getY(), maxwid, y);
    }

    protected Rectangle2D accomodate (Rectangle2D bounds, String name,
                                      Font font, FontRenderContext frc)
    {
        TextLayout layout = new TextLayout(name, font, frc);
        Rectangle2D tbounds = layout.getBounds();
        bounds.setRect(bounds.getX(), bounds.getY(),
                       Math.max(bounds.getWidth(), tbounds.getWidth()+INSET),
                       bounds.getHeight() + tbounds.getHeight());
        return bounds;
    }

    // docs inherited from interface
    public void renderChain (Chain chain, Graphics2D gfx)
    {
        // figure out where we'll be rendering
        Rectangle2D bounds = chain.getBounds();
        double x = bounds.getX();
        double y = bounds.getY();

        // create a text layout based on the current rendering conditions
        Font font = chain.getRoot().isInterface() ?
            FontPicker.getInterfaceFont() : FontPicker.getClassFont();
        TextLayout layout = new TextLayout(chain.getName(), font,
                                           gfx.getFontRenderContext());

        Rectangle2D tbounds = getTextBox(gfx, layout);
        double dx = -tbounds.getX(), dy = -tbounds.getY();
        double maxwid = tbounds.getWidth();

        // draw the name
        layout.draw(gfx, (float)(x + dx + HEADER_BORDER),
                    (float)(y + dy + HEADER_BORDER));

        // draw the interface and inner class info, but only if we're not
        // an out of package class
        double ix = x + HEADER_BORDER + INSET;
        double iy = y + tbounds.getHeight() - HEADER_BORDER;

        if (chain.inPackage()) {
            String[] impls = chain.getImplementsNames();
            for (int i = 0; i < impls.length; i++) {
                TextLayout ilay =
                    new TextLayout(impls[i], FontPicker.getImplementsFont(),
                                   gfx.getFontRenderContext());
                Rectangle2D ibounds = ilay.getBounds();
                double newwid = ibounds.getWidth() + 2*HEADER_BORDER + INSET;
                if (newwid > maxwid) {
                    maxwid = newwid;
                }
                ilay.draw(gfx, (float)(ix - ibounds.getX()),
                          (float)(iy - ibounds.getY()));
                iy += ibounds.getHeight();
            }

            String[] decls = chain.getDeclaresNames();
            for (int i = 0; i < decls.length; i++) {
                TextLayout ilay =
                    new TextLayout(decls[i], FontPicker.getDeclaresFont(),
                                   gfx.getFontRenderContext());
                Rectangle2D ibounds = ilay.getBounds();
                double newwid = ibounds.getWidth() + 2*HEADER_BORDER + INSET;
                if (newwid > maxwid) {
                    maxwid = newwid;
                }
                ilay.draw(gfx, (float)(ix - ibounds.getX()),
                          (float)(iy - ibounds.getY()));
                iy += ibounds.getHeight();
            }
        }

        // stroke a box that will contain the name
        tbounds.setRect(x, y, maxwid, iy - y + HEADER_BORDER);
        gfx.draw(tbounds);

        // render our connecty lines
        ArrayList kids = chain.getChildren();
        if (kids.size() > 0) {
            GeneralPath path = new GeneralPath();
            Rectangle2D kbounds = ((Chain)kids.get(0)).getBounds();
            double half = kbounds.getX()/2;
            path.moveTo((float)(x + half), (float)(y + tbounds.getHeight()));

            for (int i = 0; i < kids.size(); i++) {
                Chain kid = (Chain)kids.get(i);
                kbounds = kid.getBounds();
                double ly = y + kbounds.getY() + dy + HEADER_BORDER;
                path.lineTo((float)(x + half), (float)ly);
                path.lineTo((float)(x + kbounds.getX()), (float)ly);
                path.moveTo((float)(x + half), (float)ly);
            }

            gfx.draw(path);
        }

        // translate the gfx so that 0,0 is at our origin
        gfx.translate(x, y);

        // now render the kids
        for (int i = 0; i < kids.size(); i++) {
            Chain kid = (Chain)kids.get(i);
            renderChain(kid, gfx);
        }

        // undo our prior translation
        gfx.translate(-x, -y);
    }

    protected static Rectangle2D getTextBox (Graphics2D gfx,
                                             TextLayout layout)
    {
        Rectangle2D bounds = layout.getBounds();
        // incorporate room for the border in the bounds
        bounds.setRect(bounds.getX(), bounds.getY(),
                       bounds.getWidth() + 2*HEADER_BORDER, 
                       bounds.getHeight() + 2*HEADER_BORDER);
        return bounds;
    }

    /**
     * The number of points surrounding the name of the chain.
     */
    protected static final double HEADER_BORDER = 3;

    /**
     * The number of points of spacing between each child chain.
     */
    protected static final double GAP = 4;

    /**
     * The number of points that interfaces and inner class declarations
     * are indented.
     */
    protected static final double INSET = 3;
}
