//
// $Id: ToolTipUtil.java,v 1.2 2001/12/14 18:58:29 shaper Exp $
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001 Walter Korman
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
 * Miscellaneous useful tool tip related utility functions.
 */
public class ToolTipUtil
{
    /**
     * Returns an eminently reasonable position at which the tool tip
     * may be displayed so as to maximize its visibility while still
     * placing it as near the relevant mouse position as is feasible.
     *
     * @param x the mouse x-position associated with the tool tip.
     * @param y the mouse y-position associated with the tool tip.
     * @param tip the tool tip dimensions.
     * @param bounds the tool tip container's bounding rectangle.
     */
    public static Point getTipPosition (
	int x, int y, Dimension tip, Rectangle bounds)
    {
	Rectangle tiprect = new Rectangle(x, y, tip.width, tip.height);

	// make sure left edge of tip is within bounds
	if (tiprect.x < bounds.x) {
	    tiprect.x = bounds.x;
	}

	// make sure top edge of tip is within bounds
	if (tiprect.y < bounds.y) {
	    tiprect.y = bounds.y;
	}

	// do our best to fit entire tip into bounds horizontally
	if ((tiprect.x + tiprect.width) > (bounds.x + bounds.width)) {
	    tiprect.x = (bounds.x + bounds.width) - tiprect.width;
	}

	// do our best to fit entire tip into bounds vertically
	if ((tiprect.y + tiprect.height) > (bounds.y + bounds.height)) {
	    tiprect.y = (bounds.y + bounds.height) - tiprect.height;
	}

	return new Point(tiprect.x, tiprect.y);
    }
}
