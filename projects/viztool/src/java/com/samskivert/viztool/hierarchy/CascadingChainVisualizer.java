//
// $Id: CascadingChainVisualizer.java,v 1.11 2001/12/01 05:28:01 mdb Exp $
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
import com.samskivert.viztool.util.LayoutUtil;
import com.samskivert.viztool.util.RenderUtil;

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
        FontRenderContext frc = gfx.getFontRenderContext();

        // the header will be the name of this chain surrounded by N
        // points of space and a box
        Rectangle2D bounds = LayoutUtil.getTextBox(
            chain.getRoot().isInterface() ? FontPicker.getInterfaceFont() :
            FontPicker.getClassFont(), frc, chain.getName());

        // add our inner classes and interface implementations, but only
        // if we're not an out of package class
        if (chain.inPackage()) {
            String[] impls = chain.getImplementsNames();
            bounds = LayoutUtil.accomodate(
                bounds, FontPicker.getImplementsFont(),
                frc, LayoutUtil.SUBORDINATE_INSET, impls);
            String[] decls = chain.getDeclaresNames();
            bounds = LayoutUtil.accomodate(
                bounds, FontPicker.getDeclaresFont(),
                frc, LayoutUtil.SUBORDINATE_INSET, decls);
        }

        double maxwid = bounds.getWidth();

        // the children will be below the name of this chain and inset by
        // four points to make space for the connecty lines
        double x = 2*LayoutUtil.GAP, y = bounds.getHeight();
        ArrayList kids = chain.getChildren();

        for (int i = 0; i < kids.size(); i++) {
            Chain kid = (Chain)kids.get(i);
            Rectangle2D kbounds = kid.getBounds();
            y += LayoutUtil.GAP; // add the gap
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

    // docs inherited from interface
    public void renderChain (Chain chain, Graphics2D gfx)
    {
        // figure out where we'll be rendering
        Rectangle2D bounds = chain.getBounds();
        double x = bounds.getX() + LayoutUtil.HEADER_BORDER;
        double y = bounds.getY() + LayoutUtil.HEADER_BORDER;
        double maxwid = 0;

        // draw the name
        FontRenderContext frc = gfx.getFontRenderContext();
        Font font = chain.getRoot().isInterface() ?
            FontPicker.getInterfaceFont() : FontPicker.getClassFont();
        Rectangle2D bnds = 
            RenderUtil.renderString(gfx, frc, font, x, y, chain.getName());
        maxwid = Math.max(maxwid, bnds.getWidth() +
                          2*LayoutUtil.HEADER_BORDER);
        y += bnds.getHeight();

        // draw the interface and inner class info, but only if we're not
        // an out of package class
        if (chain.inPackage()) {
            // render the implemented interfaces
            String[] impls = chain.getImplementsNames();
            bnds = RenderUtil.renderStrings(
                gfx, frc, FontPicker.getImplementsFont(),
                x + LayoutUtil.SUBORDINATE_INSET, y, impls);
            maxwid = Math.max(maxwid, bnds.getWidth() +
                              2*LayoutUtil.HEADER_BORDER +
                              LayoutUtil.SUBORDINATE_INSET);
            y += bnds.getHeight();

            // render the declared inner classes
            String[] decls = chain.getDeclaresNames();
            bnds = RenderUtil.renderStrings(
                gfx, frc, FontPicker.getDeclaresFont(),
                x + LayoutUtil.SUBORDINATE_INSET, y, decls);
            maxwid = Math.max(maxwid, bnds.getWidth() +
                              2*LayoutUtil.HEADER_BORDER +
                              LayoutUtil.SUBORDINATE_INSET);
            y += bnds.getHeight();
        }

        // leave a border at the bottom as well
        y += LayoutUtil.HEADER_BORDER;

        // stroke a box that will contain the name
        Rectangle2D outline = new Rectangle2D.Double(
            bounds.getX(), bounds.getY(), maxwid, y - bounds.getY());
        gfx.draw(outline);

        // keep track of the bottom
        double height = y;

        // reset our top level coords
        x = bounds.getX();
        y = bounds.getY();

        // render our connecty lines
        ArrayList kids = chain.getChildren();
        if (kids.size() > 0) {
            GeneralPath path = new GeneralPath();
            Rectangle2D kbounds = ((Chain)kids.get(0)).getBounds();
            double half = kbounds.getX()/2;
            path.moveTo((float)(x + half), (float)height);

            for (int i = 0; i < kids.size(); i++) {
                Chain kid = (Chain)kids.get(i);
                kbounds = kid.getBounds();
                double ly = y + kbounds.getY() + 2*LayoutUtil.HEADER_BORDER;
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
}
