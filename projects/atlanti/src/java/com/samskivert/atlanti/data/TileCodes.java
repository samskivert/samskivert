//
// $Id: TileCodes.java,v 1.7 2001/10/18 19:08:50 mdb Exp $

package com.threerings.venison;

/**
 * A repository for constants related to the tiles that are used in the
 * game of Venison.
 */
public interface TileCodes
{
    /** A four-sided city tile. */
    public static final int CITY_FOUR = 1;

    /** A three-sided city tile. */
    public static final int CITY_THREE = 2;

    /** A three-sided city tile with a road. */
    public static final int CITY_THREE_ROAD = 3;

    /** A two-sided city tile with city openings adjacent to one
     * another. */
    public static final int CITY_TWO = 4;

    /** A two-sided city tile with city openings adjacent to one another
     * and a road connecting the other two sides. */
    public static final int CITY_TWO_ROAD = 5;

    /** A two-sided city tile with city openings on opposite sides of the
     * tile. */
    public static final int CITY_TWO_ACROSS = 6;

    /** A two-sided city tile with two separate city arcs adjacent to one
     * another and not connected to each other. */
    public static final int TWO_CITY_TWO = 7;

    /** A two-sided city tile with two separate city arcs on opposite
     * sides of the tile. */
    public static final int TWO_CITY_TWO_ACROSS = 8;

    /** A one-sided city tile. */
    public static final int CITY_ONE = 9;

    /** A one-sided city tile with a city arc on top and a right facing
     * curved road segment beneath it. */
    public static final int CITY_ONE_ROAD_RIGHT = 10;

    /** A one-sided city tile with a city arc on top and a left facing
     * curved road segment beneath it. */
    public static final int CITY_ONE_ROAD_LEFT = 11;

    /** A one-sided city tile with a city arc on top and a road tee
     * beneath it. */
    public static final int CITY_ONE_ROAD_TEE = 12;

    /** A one-sided city tile with a city arc on top and straight road
     * segment beneath it. */
    public static final int CITY_ONE_ROAD_STRAIGHT = 13;

    /** A cloister tile. */
    public static final int CLOISTER_PLAIN = 14;

    /** A cloister tile with a road extending from the cloister. */
    public static final int CLOISTER_ROAD = 15;

    /** A four-way road intersection. */
    public static final int FOUR_WAY_ROAD = 16;

    /** A three-way road intersection. */
    public static final int THREE_WAY_ROAD = 17;

    /** A straight road segment. */
    public static final int STRAIGHT_ROAD = 18;

    /** A curved road segment. */
    public static final int CURVED_ROAD = 19;

    /** The number of different tile types. */
    public static final int TILE_TYPES = 19;


    /** A tile orientation constant indicating the tile is in its default
     * orientation. */
    public static final int NORTH = 0;

    /** A tile orientation constant indicating the tile is rotated 90
     * degrees clockwise from its default orientation. */
    public static final int EAST = 1;

    /** A tile orientation constant indicating the tile is rotated 180
     * degrees clockwise from its default orientation. */
    public static final int SOUTH = 2;

    /** A tile orientation constant indicating the tile is rotated 270
     * degrees clockwise from its default orientation. */
    public static final int WEST = 3;

    /** A mapping from orientation codes to a string representation. */
    public static final String[] ORIENT_NAMES =
        new String[] { "N", "E", "S", "W" };


    /** The tile image width in pixels. */
    public static int TILE_WIDTH = 64;

    /** The tile image height in pixels. */
    public static int TILE_HEIGHT = 64;


    /** A tile edge constant indicating a city edge. */
    public static final int CITY = 0;

    /** A tile edge constant indicating a grass edge. */
    public static final int GRASS = 1;

    /** A tile edge constant indicating a road edge. */
    public static final int ROAD = 2;

    /** A constant indicating a cloister. */
    public static final int CLOISTER = 3;


    /** A flag used to mark a tile as part of a completed city. */
    public static final int COMPLETED_CITY = 0x01;
}
