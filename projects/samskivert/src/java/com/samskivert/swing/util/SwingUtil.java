//
// $Id: SwingUtil.java,v 1.6 2002/03/26 19:32:16 mdb Exp $
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001 Michael Bayne
// 
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.samskivert.swing.util;

import java.awt.*;

/**
 * Miscellaneous useful Swing-related utility functions.
 */
public class SwingUtil
{
    /**
     * Center the given window within the screen boundaries.
     *
     * @param window the window to be centered.
     */
    public static void centerWindow (Window window)
    {
        Toolkit tk = window.getToolkit();
        Dimension ss = tk.getScreenSize();
        int width = window.getWidth(), height = window.getHeight();
        window.setBounds((ss.width-width)/2, (ss.height-height)/2,
                         width, height);
    }

    /**
     * Draw a string centered within a rectangle.  The string is drawn
     * using the graphics context's current font and color.
     *
     * @param g the graphics context.
     * @param str the string.
     * @param x the bounding x position.
     * @param y the bounding y position.
     * @param width the bounding width.
     * @param height the bounding height.
     */
    public static void drawStringCentered (
	Graphics g, String str, int x, int y, int width, int height)
    {
        FontMetrics fm = g.getFontMetrics(g.getFont());
	int xpos = x + ((width - fm.stringWidth(str)) / 2);
	int ypos = y + ((height + fm.getAscent()) / 2);
	g.drawString(str, xpos, ypos);
    }

    /**
     * Return a polygon representing the rectangle defined by the
     * specified upper left coordinate and the supplied dimensions.
     *
     * @param x the left edge of the rectangle.
     * @param y the top of the rectangle.
     * @param d the rectangle's dimensions.
     *
     * @return the bounding polygon.
     */
    public static Polygon getPolygon (int x, int y, Dimension d)
    {
	Polygon poly = new Polygon();
	poly.addPoint(x, y);
	poly.addPoint(x + d.width, y);
	poly.addPoint(x + d.width, y + d.height);
	poly.addPoint(x, y + d.height);
	poly.addPoint(x, y);
	return poly; 
    }

    /**
     * Enables (or disables) the specified component, <em>and all of its
     * children.</cite> A simple call to {@link Container#setEnabled}
     * does not propagate the enabled state to the children of a
     * component, which is senseless in our opinion, but was surely done
     * for some arguably good reason.
     */
    public static void setEnabled (Container comp, boolean enabled)
    {
        // set the state of our children
        int ccount = comp.getComponentCount();
        for (int i = 0; i < ccount; i++) {
            Component child = comp.getComponent(i);
            if (child instanceof Container) {
                setEnabled((Container)child, enabled);
            } else {
                child.setEnabled(enabled);
            }
        }

        // set our state
        comp.setEnabled(enabled);
    }
}
