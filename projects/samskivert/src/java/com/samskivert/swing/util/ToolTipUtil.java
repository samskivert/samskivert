//
// $Id: ToolTipUtil.java,v 1.1 2001/08/28 23:51:48 shaper Exp $

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
