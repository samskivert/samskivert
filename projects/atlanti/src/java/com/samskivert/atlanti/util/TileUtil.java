//
// $Id: TileUtil.java,v 1.1 2001/10/10 06:14:57 mdb Exp $

package com.threerings.venison;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

/**
 * Utility functions relating to the Venison tiles.
 */
public class TileUtil implements TileCodes
{
    /** The starting tile. */
    public static final VenisonTile STARTING_TILE =
        new VenisonTile(CITY_ONE_ROAD_STRAIGHT, false, NORTH, 0, 0);

    /**
     * Returns a list containing the standard tile set for the Venison
     * game. The list is a clone, so it can be bent, folded and modified
     * by the caller.
     */
    public static List getStandardTileSet ()
    {
        return (List)TILE_SET.clone();
    }

    /**
     * Scans the supplied tile set to determine which of the four
     * orientations of the supplied target tile would result in a valid
     * placement of that tile (valid placement meaning that all of its
     * edges match up with neighboring tiles, it abuts at least one tile
     * and it does not occupy the same space as any existing tile). The
     * position of the target tile is assumed to be the desired placement
     * position and the current orientation of the target tile is ignored.
     *
     * @param tiles an iterator that will iterate over all of the tiles
     * currently placed on the board.
     * @param target the tile whose valid orientations we wish to compute.
     *
     * @return an array of boolean values indicating whether or not the
     * tile can be placed in each of the cardinal directions (which match
     * up with the direction constants specified in {@link TileCodes}.
     */
    public static boolean[] computeValidOrients (
        Iterator tiles, VenisonTile target)
    {
        // this contains a count of tiles that match up with the candidate
        // tile in each of its four orientations
        int[] matches = new int[4];

        while (tiles.hasNext()) {
            VenisonTile tile = (VenisonTile)tiles.next();

            // figure out where this tile is in relation to the candidate
            int xdiff = tile.x - target.x;
            int ydiff = tile.y - target.y;
            int sum = Math.abs(xdiff) + Math.abs(ydiff);

            if (sum == 0) {
                // they overlap, nothing doing
                return new boolean[4];

            } else if (sum ==  1) {
                // they're neighbors, we may have a match
                int targetEdge = EDGE_MAP[(ydiff+1)*3 + xdiff+1];
                // we want the edge of the placed tile that matches up
                // with the tile in the candidate location, but we also
                // need to take into account the orientation of the placed
                // tile
                int tileEdge = (targetEdge+(4-tile.orientation)+2) % 4;

                // we iterate over the four possible orientations of the
                // target tile
                for (int i = 0; i < 4; i++) {
                    // we compare the edge of the placed tile (which never
                    // changes) with the edge of the target tile which is
                    // adjusted based on the target tile's orientation
                    if (getEdge(tile.type, tileEdge) ==
                        getEdge(target.type, (targetEdge+(4-i)) % 4)) {
                        // increment the edge matches
                        matches[i]++;

                    } else {
                        // if we have a mismatch, we want to ensure that
                        // we screw this orientation up for good, so we
                        // deduct a large value from the array to ensure
                        // that it will remain less than zero regardless
                        // of which of the other three tiles match in this
                        // orientation
                        matches[i] -= 10;
                    }
                }
            }
        }

        // for every orientation that we have a positive number of edge
        // matches, we have a valid orientation
        boolean[] orients = new boolean[4];
        for (int i = 0; i < matches.length; i++) {
            orients[i] = (matches[i] > 0);
        }
        return orients;
    }

    /**
     * Returns the edge type for specified edge of the specified tile
     * type.
     *
     * @param tileType the type of the tile in question.
     * @param edge the direction constant indicating the edge in which we
     * are interested.
     *
     * @return the edge constant for the edge in question.
     */
    public static int getEdge (int tileType, int edge)
    {
        return TILE_EDGES[4*tileType + edge];
    }

    /** Used to generate our standard tile set. */
    protected static void addTiles (int count, List list, VenisonTile tile)
    {
        for (int i = 0; i  < count-1; i++) {
            list.add(tile.clone());
        }
        list.add(tile);
    }

    /** The standard tile set for a game of Venison. */
    protected static ArrayList TILE_SET = new ArrayList();

    // create our standard tile set
    static {
        addTiles(1, TILE_SET, new VenisonTile(CITY_FOUR, true));

        addTiles(3, TILE_SET, new VenisonTile(CITY_THREE, false));
        addTiles(1, TILE_SET, new VenisonTile(CITY_THREE, true));
        addTiles(1, TILE_SET, new VenisonTile(CITY_THREE_ROAD, false));
        addTiles(2, TILE_SET, new VenisonTile(CITY_THREE_ROAD, true));

        addTiles(3, TILE_SET, new VenisonTile(CITY_TWO, false));
        addTiles(2, TILE_SET, new VenisonTile(CITY_TWO, true));
        addTiles(3, TILE_SET, new VenisonTile(CITY_TWO_ROAD, false));
        addTiles(2, TILE_SET, new VenisonTile(CITY_TWO_ROAD, true));
        addTiles(1, TILE_SET, new VenisonTile(CITY_TWO_ACROSS, false));
        addTiles(2, TILE_SET, new VenisonTile(CITY_TWO_ACROSS, true));

        addTiles(2, TILE_SET, new VenisonTile(DISCONNECTED_CITY_TWO, false));
        addTiles(3, TILE_SET,
                 new VenisonTile(DISCONNECTED_CITY_TWO_ACROSS, false));

        addTiles(5, TILE_SET, new VenisonTile(CITY_ONE, false));
        addTiles(3, TILE_SET, new VenisonTile(CITY_ONE_ROAD_RIGHT, false));
        addTiles(3, TILE_SET, new VenisonTile(CITY_ONE_ROAD_LEFT, false));
        addTiles(3, TILE_SET, new VenisonTile(CITY_ONE_ROAD_TEE, false));
        addTiles(3, TILE_SET, new VenisonTile(CITY_ONE_ROAD_STRAIGHT, false));

        addTiles(4, TILE_SET, new VenisonTile(CLOISTER, false));
        addTiles(2, TILE_SET, new VenisonTile(CLOISTER_ROAD, false));

        addTiles(1, TILE_SET, new VenisonTile(FOUR_WAY_ROAD, false));
        addTiles(4, TILE_SET, new VenisonTile(THREE_WAY_ROAD, false));
        addTiles(8, TILE_SET, new VenisonTile(STRAIGHT_ROAD, false));
        addTiles(9, TILE_SET, new VenisonTile(CURVED_ROAD, false));
    }

    /** Used to figure out which edges match up to which when comparing
     * adjacent tiles. */
    protected static final int[] EDGE_MAP = new int[] {
        -1, NORTH, -1,
        WEST, -1, EAST,
        -1, SOUTH, -1
    };

    /** A table indicating which tiles have which edges. */
    protected static final int[] TILE_EDGES = new int[] {
        -1, -1, -1, -1, // null tile
        CITY, CITY, CITY, CITY, // CITY_FOUR
        CITY, CITY, GRASS, CITY, // CITY_THREE
        CITY, CITY, ROAD, CITY, // CITY_THREE_ROAD
        CITY, GRASS, GRASS, CITY, // CITY_TWO
        CITY, ROAD, ROAD, CITY, // CITY_TWO_ROAD
        GRASS, CITY, GRASS, CITY, // CITY_TWO_ACROSS
        CITY, CITY, GRASS, GRASS, // DISCONNECTED_CITY_TWO
        GRASS, CITY, GRASS, CITY, // DISCONNECTED_CITY_TWO_ACROSS
        CITY, GRASS, GRASS, GRASS, // CITY_ONE
        CITY, ROAD, ROAD, GRASS, // CITY_ONE_ROAD_RIGHT
        CITY, GRASS, ROAD, ROAD, // CITY_ONE_ROAD_LEFT
        CITY, ROAD, ROAD, ROAD, // CITY_ONE_ROAD_TEE
        CITY, ROAD, GRASS, ROAD, // CITY_ONE_ROAD_STRAIGHT
        GRASS, GRASS, GRASS, GRASS, // CLOISTER
        GRASS, GRASS, ROAD, GRASS, // CLOISTER_ROAD
        ROAD, ROAD, ROAD, ROAD, // FOUR_WAY_ROAD
        GRASS, ROAD, ROAD, ROAD, // THREE_WAY_ROAD
        ROAD, GRASS, ROAD, GRASS, // STRAIGHT_ROAD
        GRASS, GRASS, ROAD, ROAD, // CURVED_ROAD
    };
}
