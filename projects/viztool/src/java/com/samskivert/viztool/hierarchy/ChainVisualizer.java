//
// $Id: ChainVisualizer.java,v 1.1 2001/07/13 23:25:13 mdb Exp $

package com.samskivert.viztool.viz;

/**
 * The chain layout is used to compute the dimensions of chains and their
 * children in preparation for rendering them in a particular manner. In
 * general a layout is coupled with one or more renderers which generate
 * the proper rendering instructions based on the layout information
 * computed by this layout manager. The reason rendering is decoupled from
 * layout is to facilitate easier support for rendering the same layout
 * via different formatting languages (postscript and vml for example).
 *
 * @see ChainRenderer
 */
public interface ChainLayout
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
     * @param pointSize the point size of the font to be used in computing
     * our dimensions (all coordinates are in points).
     */
    public void layoutChain (Chain chain, int pointSize);
}
