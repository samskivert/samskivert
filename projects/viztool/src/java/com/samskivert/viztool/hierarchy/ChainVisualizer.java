//
// $Id: ChainVisualizer.java,v 1.3 2001/08/12 04:36:58 mdb Exp $
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

package com.samskivert.viztool.viz;

import java.awt.Graphics2D;

/**
 * The chain visualizer is used to compute the dimensions of chains and
 * their children in preparation for rendering and then to perform said
 * rendering.
 */
public interface ChainVisualizer
{
    /**
     * Assigns positions to the children of the supplied chain based on
     * the layout policies desired by the implementation and assigns
     * dimensions to the specified chain based on the dimensions of its
     * children and the aforementioned layout policies. The children of
     * the provided chain instance are guaranteed to have been layed out
     * (meaning they have dimensions but no position) prior to this call.
     *
     * @param chain the chain to be layed out.
     * @param gfx the graphics context to use when computing dimensions.
     */
    public void layoutChain (Chain chain, Graphics2D gfx);

    /**
     * Renders the specified chain (and its subchains) based on the layout
     * information (dimensions) already computed for this chain.
     *
     * @param chain the chain to be rendered.
     * @param gfx the graphics context in which to render the chain.
     */
    public void renderChain (Chain chain, Graphics2D gfx);
}
