//
// $Id: ChainRenderer.java,v 1.1 2001/07/13 23:25:13 mdb Exp $

package com.samskivert.viztool.viz;

import java.io.PrintStream;

/**
 * The chain renderer is used to generate rendering output based on a
 * previously layed-out chain. This generally means that renderers are
 * tightly coupled to layout managers. This is intentional and the
 * decoupling that exists only exists to facilitate multiple renderer
 * implementations for multiple output formats (postscript and vml, for
 * example).
 *
 * @see ChainLayout
 */
public interface ChainRenderer
{
    /**
     * Generates rendering instructions for the specified chain (and its
     * subchains) based on the layout information (dimensions) already
     * computed for this chain.
     *
     * @param chain the chain to be rendered.
     * @param out the output stream on which to write the rendering
     * instructions.
     * @param pointSize the point size of the font to be used when
     * rendering this chain.
     * @param x the x coordinate at which to render the chain (any
     * relative coordinates maintained by the chain should be used as
     * offsets to this absolute coordinate).
     * @param y the y coordinate at which to render the chain.
     */
    public void renderChain (Chain chain, PrintStream out,
                             int pointSize, int x, int y);
}
