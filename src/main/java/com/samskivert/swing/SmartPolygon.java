//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.swing;

import java.awt.Polygon;
import java.awt.Rectangle;

/**
 * Extends the AWT polygon class and provides a {@link #getBounds} method
 * that doesn't annoyingly make a copy of the bounds rectangle every time
 * it is returned.
 */
public class SmartPolygon extends Polygon
{
    /**
     * Returns the internally cached bounds rectangle for this polygon.
     * <em>Don't modify it!</em>
     */
    @Override
    @SuppressWarnings("deprecation")
    public Rectangle getBoundingBox ()
    {
        return (bounds == null) ? super.getBoundingBox() : bounds;
    }
}
