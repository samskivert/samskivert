//
// $Id: AtlantiCodes.java,v 1.1 2001/10/12 20:34:13 mdb Exp $

package com.threerings.venison;

/**
 * Constants used by the Venison game code.
 */
public interface VenisonCodes
{
    /** The name of the command posted by the {@link VenisonBoard} when
     * the user places a tile into a valid position. */
    public static final String TILE_PLACED = "tile_placed";

    /** The message submitted by the client to the server when they have
     * chosen where they wish to place their tile. */
    public static final String PLACE_TILE_REQUEST = "place_tile";

    /** The message submitted by the client to the server when they have
     * chosen where they wish to place their piecen. */
    public static final String PLACE_PIECEN_REQUEST = "place_piecen";
}
