//
// $Id: AtlantiCodes.java,v 1.5 2002/05/21 04:45:10 mdb Exp $

package com.threerings.venison;

/**
 * Constants used by the Venison game code.
 */
public interface VenisonCodes
{
    /** The message bundle identifier for translation messages. */
    public static final String VENISON_MESSAGE_BUNDLE = "venison";

    /** The number of piecens provided to each player. */
    public static final int PIECENS_PER_PLAYER = 7;

    /** The name of the command posted by the {@link VenisonBoard} when
     * the user places a tile into a valid position. */
    public static final String TILE_PLACED = "tile_placed";

    /** The name of the command posted by the {@link VenisonBoard} when
     * the user places a piecen onto an unclaimed feature. */
    public static final String PIECEN_PLACED = "piecen_placed";

    /** The name of the command posted by the "place nothing" button in
     * the side bar. */
    public static final String PLACE_NOTHING = "place_nothing";

    /** The message submitted by the client to the server when they have
     * chosen where they wish to place their tile. */
    public static final String PLACE_TILE_REQUEST = "place_tile";

    /** The message submitted by the client to the server when they have
     * chosen where they wish to place their piecen. */
    public static final String PLACE_PIECEN_REQUEST = "place_piecen";

    /** The message submitted by the client to the server when they decide
     * that they don't want to (or can't) place any piecen this turn. */
    public static final String PLACE_NOTHING_REQUEST = "place_nothing";

    /** The name of the command posted by the "Back to lobby" button in
     * the side bar. */
    public static final String BACK_TO_LOBBY = "back_to_lobby";
}
