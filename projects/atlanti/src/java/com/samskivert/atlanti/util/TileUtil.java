//
// $Id: TileUtil.java,v 1.3 2001/10/15 19:55:15 mdb Exp $

package com.threerings.venison;

import java.awt.Polygon;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

import com.samskivert.util.IntTuple;

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
     * Returns true if the position and orientation of the target tile is
     * legal given the placement of all of the existing tiles.
     *
     * @param tiles an iterator that enumerates all of the tile already on
     * the board.
     * @param target the tile whose validity we want to determine.
     *
     * @return true if the target tile is configured with a valid position
     * and orientation, false if it is not.
     */
    public static boolean isValidPlacement (
        Iterator tiles, VenisonTile target)
    {
        boolean matchedAnEdge = false;

        while (tiles.hasNext()) {
            VenisonTile tile = (VenisonTile)tiles.next();

            // figure out where this tile is in relation to the candidate
            int xdiff = tile.x - target.x;
            int ydiff = tile.y - target.y;
            int sum = Math.abs(xdiff) + Math.abs(ydiff);

            if (sum == 0) {
                // they overlap, nothing doing
                return false;

            } else if (sum ==  1) {
                // they're neighbors, we may have a match
                int targetEdge = EDGE_MAP[(ydiff+1)*3 + xdiff+1];

                // we want the edge of the placed tile that matches up
                // with the tile in the candidate location, but we also
                // need to take into account the orientation of the placed
                // tile
                int tileEdge = (targetEdge+(4-tile.orientation)+2) % 4;

                // see if the edges match
                if (getEdge(tile.type, tileEdge) ==
                    getEdge(target.type, targetEdge)) {
                    // make a note that we matched at least one edge
                    matchedAnEdge = true;

                } else {
                    // the edges don't match, nothing doing
                    return false;
                }
            }
        }

        // if we got this far, we didn't have any mismatches, so we need
        // only know that we matched at least one edge
        return matchedAnEdge;
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

    /**
     * Massages a road segment (specified in tile feature coordinates)
     * into a polygon (in screen coordinates) that can be used to render
     * or hit test the road. The coordinates must obey the following
     * constraints: (x1 < x2 and y1 == y2) or (x1 == x2 and y1 < y2) or
     * (x1 < x2 and y1 > y2).
     *
     * @return a polygon representing the road segment (with origin at 0,
     * 0).
     */
    public static Polygon roadSegmentToPolygon (
        int x1, int y1, int x2, int y2)
    {
        // first convert the coordinates into screen coordinates
        x1 = (x1 * TILE_WIDTH) / 4;
        y1 = (y1 * TILE_HEIGHT) / 4;
        x2 = (x2 * TILE_WIDTH) / 4;
        y2 = (y2 * TILE_HEIGHT) / 4;

        Polygon poly = new Polygon();
        int dx = 4, dy = 4;

        // figure out what sort of line segment it is
        if (x1 == x2) { // vertical
            // make adjustments to ensure that we stay inside the tile
            // bounds
            if (y1 == 0) {
                y1 += dy;
            } else if (y2 == TILE_HEIGHT) {
                y2 -= dy;
            }
            poly.addPoint(x1 - dx, y1 - dy);
            poly.addPoint(x1 + dx, y1 - dy);
            poly.addPoint(x2 + dx, y2 + dy);
            poly.addPoint(x2 - dx, y2 + dy);

        } else if (y1 == y2) { // horizontal
            // make adjustments to ensure that we stay inside the tile
            // bounds
            if (x1 == 0) {
                x1 += dx;
            } else if (x2 == TILE_WIDTH) {
                x2 -= dx;
            }
            poly.addPoint(x1 - dx, y1 - dy);
            poly.addPoint(x1 - dx, y1 + dy);
            poly.addPoint(x2 + dx, y2 + dy);
            poly.addPoint(x2 + dx, y2 - dy);

        } else { // diagonal
            poly.addPoint(x1 - dx, y1);
            poly.addPoint(x1 + dx, y1);
            poly.addPoint(x2, y2 + dy);
            poly.addPoint(x2, y2 - dy);
        }

        return poly;
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

    /** A table describing the geometry of the features (cities, roads,
     * etc.) of each tile. */
    protected static final Object[] TILE_FEATURES = new Object[] {
        new Object[0], // null tile

        new Object[] { new IntTuple(CITY, 0), }, // CITY_FOUR

        new Object[] { new IntTuple(CITY, 0), // CITY_THREE
                       new IntTuple(GRASS, 0),
                       new int[] { 0, 4, 1, 3, 3, 3, 4, 4 }},

        new Object[] { new IntTuple(CITY, 0), // CITY_THREE_ROAD
                       new IntTuple(GRASS, 0),
                       new int[] { 0, 4, 1, 3, 2, 3, 2, 4 },
                       new IntTuple(GRASS, 1),
                       new int[] { 2, 4, 2, 3, 3, 3, 4, 4 },
                       new IntTuple(ROAD, 0),
                       new int[] { 2, 3, 2, 4 }},

        new Object[] { new IntTuple(CITY, 0), // CITY_TWO
                       new IntTuple(GRASS, 0),
                       new int[] { 0, 4, 4, 0, 4, 4 }},

        new Object[] { new IntTuple(CITY, 0), // CITY_TWO_ROAD
                       new IntTuple(GRASS, 0),
                       new int[] { 0, 4, 4, 0, 4, 2, 2, 4 },
                       new IntTuple(ROAD, 0),
                       new int[] { 2, 4, 4, 2 },
                       new IntTuple(GRASS, 0),
                       new int[] { 2, 4, 4, 2, 4, 4 }},

        new Object[] { new IntTuple(CITY, 0), // CITY_TWO_ACROSS
                       new IntTuple(GRASS, 0),
                       new int[] { 0, 4, 1, 3, 3, 3, 4, 4 },
                       new IntTuple(GRASS, 1),
                       new int[] { 0, 0, 1, 1, 3, 1, 4, 0 }},

        new Object[] { new IntTuple(GRASS, 0), // DISCONNECTED_CITY_TWO
                       new IntTuple(CITY, 0),
                       new int[] { 0, 0, 1, 1, 3, 1, 4, 0 },
                       new IntTuple(CITY, 1),
                       new int[] { 4, 0, 3, 1, 3, 3, 4, 4 }},

        new Object[] { new IntTuple(GRASS, 0), // DISCONNECTED_CITY_TWO_ACROSS
                       new IntTuple(CITY, 0),
                       new int[] { 0, 0, 1, 1, 1, 3, 0, 4 },
                       new IntTuple(CITY, 1),
                       new int[] { 4, 0, 3, 1, 3, 3, 4, 4 }},

        new Object[] { new IntTuple(GRASS, 0), // CITY_ONE
                       new IntTuple(CITY, 0),
                       new int[] { 0, 0, 1, 1, 3, 1, 4, 0 }},

        new Object[] { new IntTuple(GRASS, 0), // CITY_ONE_ROAD_RIGHT
                       new IntTuple(CITY, 0),
                       new int[] { 0, 0, 1, 1, 3, 1, 4, 0 },
                       new IntTuple(ROAD, 0),
                       new int[] { 2, 2, 2, 4 },
                       new IntTuple(ROAD, 0),
                       new int[] { 2, 2, 4, 2 }},

        new Object[] { new IntTuple(GRASS, 0), // CITY_ONE_ROAD_LEFT
                       new IntTuple(CITY, 0),
                       new int[] { 0, 0, 1, 1, 3, 1, 4, 0 },
                       new IntTuple(ROAD, 0),
                       new int[] { 2, 2, 2, 4 },
                       new IntTuple(ROAD, 0),
                       new int[] { 0, 2, 2, 2 }},

        new Object[] { new IntTuple(GRASS, 0), // CITY_ONE_ROAD_TEE
                       new IntTuple(CITY, 0),
                       new int[] { 0, 0, 1, 1, 3, 1, 4, 0 },
                       new IntTuple(ROAD, 0),
                       new int[] { 0, 2, 2, 2 },
                       new IntTuple(ROAD, 1),
                       new int[] { 2, 2, 4, 2 },
                       new IntTuple(ROAD, 2),
                       new int[] { 2, 2, 2, 4 }},

        new Object[] { new IntTuple(GRASS, 0), // CITY_ONE_ROAD_STRAIGHT
                       new IntTuple(CITY, 0),
                       new int[] { 0, 0, 1, 1, 3, 1, 4, 0 },
                       new IntTuple(ROAD, 0),
                       new int[] { 0, 2, 4, 2 }},

        new Object[] { new IntTuple(GRASS, 0), // CLOISTER
                       new IntTuple(CITY, 0),
                       new int[] { 1, 1, 3, 1, 3, 3, 1, 3 }},

        new Object[] { new IntTuple(GRASS, 0), // CLOISTER_ROAD
                       new IntTuple(CITY, 0),
                       new int[] { 1, 1, 3, 1, 3, 3, 1, 3 },
                       new IntTuple(ROAD, 0),
                       new int[] { 2, 3, 2, 4 }},

        new Object[] { new IntTuple(GRASS, 0), // FOUR_WAY_ROAD
                       new IntTuple(ROAD, 0),
                       new int[] { 2, 0, 2, 2 },
                       new IntTuple(ROAD, 1),
                       new int[] { 2, 2, 4, 2 },
                       new IntTuple(ROAD, 2),
                       new int[] { 2, 2, 2, 4 },
                       new IntTuple(ROAD, 3),
                       new int[] { 0, 2, 2, 2 }},

        new Object[] { new IntTuple(GRASS, 0), // THREE_WAY_ROAD
                       new IntTuple(ROAD, 0),
                       new int[] { 0, 2, 2, 2 },
                       new IntTuple(ROAD, 1),
                       new int[] { 2, 2, 4, 2 },
                       new IntTuple(ROAD, 2),
                       new int[] { 2, 2, 2, 4 }},

        new Object[] { new IntTuple(GRASS, 0), // STRAIGHT_ROAD
                       new IntTuple(ROAD, 0),
                       new int[] { 2, 0, 2, 4 }},

        new Object[] { new IntTuple(GRASS, 0), // CURVED_ROAD
                       new IntTuple(ROAD, 0),
                       new int[] { 2, 2, 2, 4 },
                       new IntTuple(ROAD, 0),
                       new int[] { 0, 2, 2, 2 }},
    };
}
