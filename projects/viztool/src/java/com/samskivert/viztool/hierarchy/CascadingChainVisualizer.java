//
// $Id: CascadingChainVisualizer.java,v 1.5 2001/07/17 05:23:49 mdb Exp $

package com.samskivert.viztool.viz;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.TextLayout;
import java.awt.geom.*;
import java.util.ArrayList;

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
    implements ChainVisualizer, CascadingConstants
{
    // docs inherited from interface
    public void layoutChain (Chain chain, Graphics2D gfx)
    {
        // create a text layout based on the current rendering conditions
        Font font = chain.getRoot().isInterface() ? _ifaceFont : _classFont;
        TextLayout layout = new TextLayout(chain.getName(), font,
                                           gfx.getFontRenderContext());

        // the header will be the name of this chain surrounded by N
        // points of space and a box
        Rectangle2D bounds = getTextBox(gfx, layout);
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

    // docs inherited from interface
    public void renderChain (Chain chain, Graphics2D gfx)
    {
        // figure out where we'll be rendering
        Rectangle2D bounds = chain.getBounds();
        double x = bounds.getX();
        double y = bounds.getY();

        // create a text layout based on the current rendering conditions
        Font font = chain.getRoot().isInterface() ? _ifaceFont : _classFont;
        TextLayout layout = new TextLayout(chain.getName(), font,
                                           gfx.getFontRenderContext());

        // stroke a box that will contain the name
        Rectangle2D tbounds = getTextBox(gfx, layout);
        double dx = -tbounds.getX(), dy = -tbounds.getY();
        tbounds.setRect(x, y, tbounds.getWidth(), tbounds.getHeight());
        gfx.draw(tbounds);

        // now draw the name
        layout.draw(gfx, (float)(x + dx + HEADER_BORDER),
                    (float)(y + dy + HEADER_BORDER));

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

    protected static Font _classFont = new Font("Helvetica", Font.PLAIN, 8);
    protected static Font _ifaceFont = new Font("Helvetica", Font.ITALIC, 8);
}
