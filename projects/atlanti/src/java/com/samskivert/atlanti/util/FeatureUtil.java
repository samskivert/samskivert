//
// $Id: FeatureUtil.java,v 1.5 2001/12/18 11:58:53 mdb Exp $

package com.threerings.venison;

import java.awt.geom.Point2D;

/**
 * Feature related constants and utility functions.
 */
public class FeatureUtil implements TileCodes
{
    /** Bit mask for a north connecting feature. */
    public static final int NORTH_F = 0x1 << 0;

    /** Bit mask for an east connecting feature. */
    public static final int EAST_F = 0x1 << 1;

    /** Bit mask for a south connecting feature. */
    public static final int SOUTH_F = 0x1 << 2;

    /** Bit mask for a west connecting feature. */
    public static final int WEST_F = 0x1 << 3;

    /** Bit mask for a north by northeast connecting feature. */
    public static final int NNE_F = 0x1 << 4;

    /** Bit mask for an east by northeast connecting feature. */
    public static final int ENE_F = 0x1 << 5;

    /** Bit mask for an east by southeast connecting feature. */
    public static final int ESE_F = 0x1 << 6;

    /** Bit mask for a south by southeast connecting feature. */
    public static final int SSE_F = 0x1 << 7;

    /** Bit mask for a south by southwest connecting feature. */
    public static final int SSW_F = 0x1 << 8;

    /** Bit mask for a west by southwest connecting feature. */
    public static final int WSW_F = 0x1 << 9;

    /** Bit mask for a west by northwest connecting feature. */
    public static final int WNW_F = 0x1 << 10;

    /** Bit mask for a north by northwest connecting feature. */
    public static final int NNW_F = 0x1 << 11;

    /** A mapping from feature edge masks to tile directions and
     * corresponding feature edge masks. */
    public static final int[] ADJACENCY_MAP = new int[] {
        NORTH_F, NORTH, SOUTH_F,
        EAST_F, EAST, WEST_F,
        SOUTH_F, SOUTH, NORTH_F,
        WEST_F, WEST, EAST_F,
        NNW_F, NORTH, SSW_F,
        NNE_F, NORTH, SSE_F,
        ENE_F, EAST, WNW_F,
        ESE_F, EAST, WSW_F,
        SSE_F, SOUTH, NNE_F,
        SSW_F, SOUTH, NNW_F,
        WSW_F, WEST, ESE_F,
        WNW_F, WEST, ENE_F,
    };

    /** A mapping for city tiles to the grass features that are adjacent
     * to the city tiles. */
    public static final int[][] CITY_GRASS_MAP = new int[][] {
        { }, 
        { 1 }, // CITY_THREE
        { 1, 2 }, // CITY_THREE_ROAD
        { 1 }, // CITY_TWO
        { 1 }, // CITY_TWO_ROAD
        { 1, 2 }, // CITY_TWO_ROAD_ACROSS
        { 0 }, // TWO_CITY_TWO
        { 0 }, // TWO_CITY_TWO_ACROSS
        { 0 }, // CITY_ONE
        { 0 }, // CITY_ONE_ROAD_RIGHT
        { 0 }, // CITY_ONE_ROAD_LEFT
        { 0 }, // CITY_ONE_ROAD_TEE
        { 0 }, // CITY_ONE_ROAD_STRAIGHT
        { }, 
        { }, 
        { }, 
        { }, 
        { }, 
        { }, 
    };

    /**
     * Returns the feature array for the tile of the specified type.
     */
    public static Feature[] getTileFeatures (int type)
    {
        // create the features array
        Feature[] features = new Feature[TILE_FEATURES[type-1].length];

        // initialize it with features from the repeated feature table or
        // with newly constructed features
        for (int i = 0; i < features.length; i++) {
            int[] desc = TILE_FEATURES[type-1][i];
            // if the description is a length one array, it is the index
            // into the reused feature table of the desired feature
            if (desc.length == 1) {
                features[i] = _reusedFeatures[desc[0]];
            } else {
                // otherwise it is the description of this unique feature
                features[i] = new Feature(desc);
            }
        }

        return features;
    }

    /**
     * Returns the position of the shield on a tile of the given
     * orientation.
     */
    public static Point2D getShieldSpot (int orientation)
    {
        return _shieldFeature.piecenSpots[orientation];
    }

    /**
     * Returns a string describing the supplied type code.
     */
    public static String typeToString (int type)
    {
        return TYPE_CODES[type];
    }

    /**
     * Returns a string describing the supplied edge mask.
     */
    public static String edgeMaskToString (int edgeMask)
    {
        StringBuffer buf = new StringBuffer();
        if ((edgeMask & NORTH_F) != 0) buf.append("NORTH_F|");
        if ((edgeMask & EAST_F) != 0) buf.append("EAST_F|");
        if ((edgeMask & SOUTH_F) != 0) buf.append("SOUTH_F|");
        if ((edgeMask & WEST_F) != 0) buf.append("WEST_F|");
        if ((edgeMask & NNW_F) != 0) buf.append("NNW_F|");
        if ((edgeMask & NNE_F) != 0) buf.append("NNE_F|");
        if ((edgeMask & SSW_F) != 0) buf.append("SSW_F|");
        if ((edgeMask & SSE_F) != 0) buf.append("SSE_F|");
        if ((edgeMask & WNW_F) != 0) buf.append("WNW_F|");
        if ((edgeMask & WSW_F) != 0) buf.append("WSW_F|");
        if ((edgeMask & ENE_F) != 0) buf.append("ENE_F|");
        if ((edgeMask & ESE_F) != 0) buf.append("ESE_F|");
        // strip off the trailing bar if there is one
        if (buf.length() > 0) {
            buf.deleteCharAt(buf.length()-1);
        }
        return buf.toString();
    }

    /**
     * Translates the feature edge mask into the orientation specified.
     * For a forward translation, provide a positive valued orientation
     * constant. For a backward translation, provide a negative valued
     * orientation constant.
     *
     * @return the translated feature mask.
     */
    public static int translateMask (int featureMask, int orientation)
    {
        int[] map = FEATURE_ORIENT_MAP[0];
        if ((featureMask & (NNE_F|ESE_F|SSW_F|WNW_F)) != 0) {
            map = FEATURE_ORIENT_MAP[1];
        } else if ((featureMask & (ENE_F|SSE_F|WSW_F|NNW_F)) != 0) {
            map = FEATURE_ORIENT_MAP[2];
        }
        return xlateMask(map, featureMask, orientation);
    }

    /** {@link #translateMask} helper function. */
    protected static int xlateMask (
        int[] map, int featureMask, int orientation)
    {
        int index = 0;
        for (int i = 0; i < map.length; i++) {
            if (map[i] == featureMask) {
                return map[(i + 4 + orientation) % 4];
            }
        }
        return featureMask;
    }

    /** A reused feature identifier. */
    protected static final int NESW_CITY = 0;

    /** A reused feature identifier. */
    protected static final int NEW_CITY = 1;

    /** A reused feature identifier. */
    protected static final int EW_CITY = 2;

    /** A reused feature identifier. */
    protected static final int NW_CITY = 3;

    /** A reused feature identifier. */
    protected static final int N_CITY = 4;

    /** A reused feature identifier. */
    protected static final int E_CITY = 5;

    /** A reused feature identifier. */
    protected static final int W_CITY = 6;

    /** A reused feature identifier. */
    protected static final int E_ROAD = 7;

    /** A reused feature identifier. */
    protected static final int S_ROAD = 8;

    /** A reused feature identifier. */
    protected static final int W_ROAD = 9;

    /** A reused feature identifier. */
    protected static final int S_GRASS = 10;

    /** A reused feature identifier. */
    protected static final int SE_GRASS = 11;

    /** A reused feature identifier. */
    protected static final int SW_GRASS = 12;

    /** An array of features used more than once. */
    protected static final int[][] FEATURES = new int[][] {
        { CITY, NORTH_F|EAST_F|SOUTH_F|WEST_F, // NESW_CITY
          2,2, 0,0, 4,0, 4,4, 0,4 },
        { CITY, NORTH_F|EAST_F|WEST_F, // NEW_CITY
          2,2, 0,0, 4,0, 4,4, 3,3, 1,3, 0,4 },
        { CITY, EAST_F|WEST_F, // EW_CITY
          2,2, 0,0, 1,1, 3,1, 4,0, 4,4, 3,3, 1,3, 0,4 },
        { CITY, NORTH_F|WEST_F, // NW_CITY
          1,2, 0,0, 4,0, 0,4 },
        { CITY, NORTH_F, // N_CITY
          2,-1, 0,0, 1,1, 3,1, 4,0 },
        { CITY, EAST_F, // E_CITY
          -4,2, 4,0, 3,1, 3,3, 4,4 },
        { CITY, WEST_F, // W_CITY
          -1,2, 0,0, 1,1, 1,3, 0,4 },
        { ROAD, EAST_F, // E_ROAD
          3,2, 2,2, 4,2 },
        { ROAD, SOUTH_F, // S_ROAD
          2,3, 2,2, 2,4 },
        { ROAD, WEST_F, // W_ROAD
          1,2, 0,2, 2,2 },
        { GRASS, SOUTH_F, // S_GRASS
          2,-4, 0,4, 1,3, 3,3, 4,4 },
        { GRASS, ESE_F|SSE_F, // SE_GRASS
          -4,-4, 2,2, 4,2, 4,4, 2,4 },
        { GRASS, WSW_F|SSW_F, // SW_GRASS
          -1,-4, 0,2, 2,2, 2,4, 0,4 },
    };

    /** A feature that is used to obtain the position information for
     * rendering shields on city tiles that use shields. */
    protected static final int[] SHIELD_FEATURE = new int[]
        { -1, NORTH_F|WEST_F, -1,-1, 0,0, 4,0, 0,4 };

    /** A table describing the features of each tile. */
    protected static final int[][][] TILE_FEATURES = new int[][][] {
        // one must offset tile type by one when indexing into this array

        { { NESW_CITY } }, // CITY_FOUR

        { { NEW_CITY }, // CITY_THREE
          { S_GRASS } },

        { { NEW_CITY }, // CITY_THREE_ROAD
          { GRASS, SSW_F, 1,-4, 0,4, 1,3, 2,3, 2,4 },
          { GRASS, SSE_F, 3,-4, 2,4, 2,3, 3,3, 4,4 },
          { ROAD, SOUTH_F, 2,-4, 2,3, 2,4 } },

        { { NW_CITY }, // CITY_TWO
          { GRASS, EAST_F|SOUTH_F, 3,-4, 0,4, 4,0, 4,4 } },

        { { NW_CITY }, // CITY_TWO_ROAD
          { GRASS, ENE_F|SSW_F, -3,-2, 0,4, 4,0, 4,2, 2,4 },
          { GRASS, ESE_F|SSE_F, -4,-4, 2,4, 4,2, 4,4 },
          { ROAD, EAST_F|SOUTH_F, 3,3, 2,4, 4,2 } },

        { { EW_CITY }, // CITY_TWO_ACROSS
          { GRASS, NORTH_F, 2,-1, 0,0, 1,1, 3,1, 4,0 }, 
          { S_GRASS } },

        { { GRASS, WEST_F|SOUTH_F, // TWO_CITY_TWO
            -1,-3, 0,0, 4,0, 4,4, 0,4 },
          { CITY, NORTH_F,
            2,-1, 0,0, 2,1, 4,0 },
          { CITY, EAST_F,
            -4,2, 4,0, 3,2, 4,4 } },

        { { GRASS, NORTH_F|SOUTH_F, // TWO_CITY_TWO_ACROSS
            2,-4, 0,0, 4,0, 3,1, 3,3, 4,4, 0,4, 1,3, 1,1 },
          { W_CITY },
          { E_CITY } },

        { { GRASS, EAST_F|SOUTH_F|WEST_F, // CITY_ONE
            2,-3, 0,0, 1,1, 3,1, 4,0, 4,4, 0,4 },
          { N_CITY } },

        { { GRASS, ENE_F|SSW_F|WEST_F, // CITY_ONE_ROAD_RIGHT
            1,2, 0,0, 1,1, 3,1, 4,0, 4,2, 2,2, 2,4, 0,4 },
          { SE_GRASS },
          { ROAD, EAST_F|SOUTH_F, 3,2, 4,2, 2,2, 2,4 },
          { N_CITY } },

        { { GRASS, EAST_F|SSE_F|WNW_F, // CITY_ONE_ROAD_LEFT
            3,2, 0,0, 1,1, 3,1, 4,0, 4,4, 2,4, 2,2, 0,2 },
          { SW_GRASS },
          { ROAD, SOUTH_F|WEST_F, 1,2, 0,2, 2,2, 2,4 },
          { N_CITY } },

        { { GRASS, ENE_F|WNW_F, // CITY_ONE_ROAD_TEE
            -1,1, 0,0, 1,1, 3,1, 4,0, 4,2, 0,2 },
          { SE_GRASS },
          { SW_GRASS },
          { E_ROAD },
          { S_ROAD },
          { W_ROAD },
          { N_CITY } },

        { { GRASS, ENE_F|WNW_F, // CITY_ONE_ROAD_STRAIGHT
            -1,1, 0,0, 1,1, 3,1, 4,0, 4,2, 0,2 },
          { GRASS, ESE_F|SOUTH_F|WSW_F, 2,3, 0,2, 4,2, 4,4, 0,4 },
          { ROAD, EAST_F|WEST_F, 2,2, 0,2, 4,2 },
          { N_CITY } },

        { { GRASS, NORTH_F|EAST_F|SOUTH_F|WEST_F, // CLOISTER_PLAIN
            -1,-1, 0,0, 4,0, 4,4, 0,4 },
          { CLOISTER, 0, 2,2, 1,1, 3,1, 3,3, 1,3 } },

        { { GRASS, NORTH_F|EAST_F|WEST_F|SSE_F|SSW_F, // CLOISTER_ROAD
            -1,-1, 0,0, 4,0, 4,4, 0,4 },
          { CLOISTER, 0, 2,2, 1,1, 3,1, 3,3, 1,3 },
          { ROAD, SOUTH_F, 2,-4, 2,3, 2,4 } },

        { { GRASS, WNW_F|NNW_F, // FOUR_WAY_ROAD
            -1,-1, 0,0, 2,0, 2,2, 0,2 },
          { GRASS, NNE_F|ENE_F, -4,-1, 2,0, 4,0, 4,2, 2,2 },
          { SE_GRASS },
          { SW_GRASS },
          { ROAD, NORTH_F, 2,1, 2,0, 2,2 },
          { E_ROAD },
          { S_ROAD },
          { W_ROAD } },

        { { GRASS, WNW_F|NORTH_F|ENE_F, // THREE_WAY_ROAD
            2,-1, 0,0, 4,0, 4,2, 0,2 },
          { SE_GRASS },
          { SW_GRASS },
          { E_ROAD },
          { S_ROAD },
          { W_ROAD } },

        { { GRASS, NNW_F|WEST_F|SSW_F, // STRAIGHT_ROAD
            -1,2, 0,0, 2,0, 2,4, 0,4 },
          { GRASS, SSE_F|EAST_F|NNE_F, -4,2, 2,0, 4,0, 4,4, 2,4 },
          { ROAD, NORTH_F|SOUTH_F, 2,2, 2,0, 2,4 } },

        { { GRASS, WNW_F|NORTH_F|EAST_F|SSE_F, // CURVED_ROAD
            3,1, 0,0, 4,0, 4,4, 2,4, 2,2, 0,2 },
          { SW_GRASS },
          { ROAD, SOUTH_F|WEST_F, 1,2, 0,2, 2,2, 2,4 } },
    };

    /** Mapping table used to rotate feature facements. */
    protected static final int[][] FEATURE_ORIENT_MAP = new int[][] {
        // orientations rotate through one of three four-cycles
        { NORTH_F, EAST_F, SOUTH_F, WEST_F },
        { NNE_F, ESE_F, SSW_F, WNW_F },
        { ENE_F, SSE_F, WSW_F, NNW_F },
    };

    /** String representations of the feature type codes. */
    protected static final String[] TYPE_CODES = {
        "CITY", "GRASS", "ROAD", "CLOISTER" };

    /** A table of features that are used on more than one tile. */
    protected static Feature[] _reusedFeatures;

    /** The feature that provides the location information for shield
     * rendering. */
    protected static Feature _shieldFeature;

    // create our reused features table
    static {
        _reusedFeatures = new Feature[FEATURES.length];
        for (int i = 0; i < FEATURES.length; i++) {
            _reusedFeatures[i] = new Feature(FEATURES[i]);
        }
        _shieldFeature = new Feature(SHIELD_FEATURE);
    }
}
