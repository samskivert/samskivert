//
// $Id: ToolTipProvider.java,v 1.2 2001/12/14 18:58:29 shaper Exp $
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

package com.samskivert.swing;

import java.awt.*;

/**
 * An interface to be implemented by objects that may have a tool tip
 * associated with themselves.
 */
public interface ToolTipProvider
{
    /**
     * Render a tool tip for this object to the given graphics context.
     *
     * @param g the graphics context.
     * @param x the x-position at which the tip should be drawn.
     * @param y the y-position at which the tip should be drawn.
     */
    public void paintToolTip (Graphics g, int x, int y);

    /**
     * Return the dimensions of the tool tip.
     *
     * @param g the graphics context to which the tip will be rendered.
     */
    public Dimension getToolTipSize (Graphics g);
}
