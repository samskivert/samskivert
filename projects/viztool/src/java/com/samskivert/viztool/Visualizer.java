//
// $Id: Visualizer.java,v 1.4 2001/11/30 22:57:31 mdb Exp $
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

package com.samskivert.viztool;

import java.awt.Graphics2D;
import java.awt.print.Printable;

/**
 * The interface via which the driver accesses whichever visualizer is
 * desired for a particular invocation.
 */
public interface Visualizer extends Printable
{
    /**
     * Requests that the visualization lay itself out in pages with the
     * specified dimensions. Subsequent calls to {@link #print} or {@link
     * #paint} will assume that things are laid out according to the most
     * recent call to this method.
     */
    public void layout (Graphics2D gfx, double x, double y,
                        double width, double height);

    /**
     * Renders the specified page of this visualization.
     */
    public void paint (Graphics2D gfx, int pageIndex);

    /**
     * Returns the number of pages occupied by the visualization.
     */
    public int getPageCount ();
}
