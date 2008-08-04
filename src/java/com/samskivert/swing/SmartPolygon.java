//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2007 Michael Bayne
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
    @SuppressWarnings("deprecation")
    public Rectangle getBoundingBox ()
    {
        return (bounds == null) ? super.getBoundingBox() : bounds;
    }
}
