//
// $Id: RenderUtil.java,v 1.1 2001/12/01 05:28:01 mdb Exp $
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

package com.samskivert.viztool.util;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;

/**
 * Rendering related utility functions.
 */
public class RenderUtil
{
    /**
     * Renders a string to the specified graphics context, in the
     * specified font at the specified coordinates.
     *
     * @return the bounds occupied by the rendered string.
     */
    public static Rectangle2D renderString (
        Graphics2D gfx, FontRenderContext frc, Font font,
        double x, double y, String text)
    {
        // do the rendering
        TextLayout ilay = new TextLayout(text, font, frc);
        Rectangle2D ibounds = ilay.getBounds();
        ilay.draw(gfx, (float)(x - ibounds.getX()),
                  (float)(y - ibounds.getY()));

        // return the dimensions occupied by the rendered string
        return ibounds;
    }

    /**
     * Renders an array of strings to the specified graphics context, in
     * the specified font at the specified coordinates.
     *
     * @return the bounds occupied by the rendered strings.
     */
    public static Rectangle2D renderStrings (
        Graphics2D gfx, FontRenderContext frc, Font font,
        double x, double y, String[] text)
    {
        return renderStrings(gfx, frc, font, x, y, text, null);
    }

    /**
     * Renders an array of strings to the specified graphics context, in
     * the specified font at the specified coordinates. If prefix is
     * non-null, it will be prefixed to the first string and subsequent
     * strings will be rendered with the space necessary to line them up
     * with the first string.
     *
     * @return the bounds occupied by the rendered strings.
     */
    public static Rectangle2D renderStrings (
        Graphics2D gfx, FontRenderContext frc, Font font,
        double x, double y, String[] text, String prefix)
    {
        double maxwid = 0, starty = y;
        double inset = 0;

        if (prefix != null) {
            TextLayout play = new TextLayout(prefix, font, frc);
            inset = play.getBounds().getWidth();
        }

        for (int i = 0; i < text.length; i++) {
            // figure some stuff out
            String string = (i == 0 && prefix != null) ?
                (prefix + text[i]) : text[i];
            double sinset = ((i == 0) ? 0 : inset);

            // do the rendering
            TextLayout ilay = new TextLayout(string, font, frc);
            Rectangle2D ibounds = ilay.getBounds();
            ilay.draw(gfx, (float)(x - ibounds.getX() + sinset),
                      (float)(y - ibounds.getY()));

            maxwid = Math.max(sinset + ibounds.getWidth(), maxwid);
            y += ibounds.getHeight();
        }

        // return the dimensions occupied by the rendered strings
        return new Rectangle2D.Double(x, y, maxwid, y-starty);
    }
}
