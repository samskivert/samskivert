//
// $Id: TileUtil.java,v 1.8 2001/10/17 04:34:13 mdb Exp $

package com.threerings.venison;

import java.awt.Polygon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.samskivert.util.IntTuple;

/**
 * Utility functions relating to the Venison tiles.
 */
public class TileUtil implements TileCodes
{
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
     * @param tiles a list of the tiles on the board.
     * @param target the tile whose valid orientations we wish to compute.
     *
     * @return an array of boolean values indicating whether or not the
     * tile can be placed in each of the cardinal directions (which match
     * up with the direction constants specified in {@link TileCodes}.
     */
    public static boolean[] computeValidOrients (
        List tiles, VenisonTile target)
    {
        // this contains a count of tiles that match up with the candidate
        // tile in each of its four orientations
        int[] matches = new int[4];

        int tsize = tiles.size();
        for (int i = 0; i < tsize; i++) {
            VenisonTile tile = (VenisonTile)tiles.get(i);

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
                for (int o = 0; o < 4; o++) {
                    // we compare the edge of the placed tile (which never
                    // changes) with the edge of the target tile which is
                    // adjusted based on the target tile's orientation
                    if (getEdge(tile.type, tileEdge) ==
                        getEdge(target.type, (targetEdge+(4-o)) % 4)) {
                        // increment the edge matches
                        matches[o]++;

                    } else {
                        // if we have a mismatch, we want to ensure that
                        // we screw this orientation up for good, so we
                        // deduct a large value from the array to ensure
                        // that it will remain less than zero regardless
                        // of which of the other three tiles match in this
                        // orientation
                        matches[o] -= 10;
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
     * @param tiles a list of the tiles already on the board.
     * @param target the tile whose validity we want to determine.
     *
     * @return true if the target tile is configured with a valid position
     * and orientation, false if it is not.
     */
    public static boolean isValidPlacement (List tiles, VenisonTile target)
    {
        boolean matchedAnEdge = false;

        int tsize = tiles.size();
        for (int i = 0; i < tsize; i++) {
            VenisonTile tile = (VenisonTile)tiles.get(i);

            // figure out where this tile is in relation to the candidate
            int xdiff = tile.x - target.x;
            int ydiff = tile.y - target.y;
            int sum = Math.abs(xdiff) + Math.abs(ydiff);

            if (sum == 0) {
                // they overlap, nothing doing
                Log.warning("Tile overlaps another [candidate=" + target +
                            ", overlapped=" + tile + "].");
                return false;

            } else if (sum ==  1) {
                // they're neighbors, we may have a match
                int targetEdge = EDGE_MAP[(ydiff+1)*3 + xdiff+1];

                // we want the edge of the placed tile that matches up
                // with the tile in the candidate location, but we also
                // need to take into account the orientation of the placed
                // tile
                int tileEdge = (targetEdge+(4-tile.orientation)+2) % 4;

                // now rotate the target edge according to our orientation
                targetEdge = ((targetEdge+(4-target.orientation)) % 4);

                // see if the edges match
                if (getEdge(tile.type, tileEdge) ==
                    getEdge(target.type, targetEdge)) {
                    // make a note that we matched at least one edge
                    matchedAnEdge = true;

                } else {
                    // the edges don't match, nothing doing
                    Log.warning("Edge mismatch [candidate=" + target +
                                ", tile=" + tile +
                                ", candidateEdge=" + targetEdge +
                                ", tileEdge=" + tileEdge + "].");
                    return false;
                }
            }
        }

        // if we got this far, we didn't have any mismatches, so we need
        // only know that we matched at least one edge
        return matchedAnEdge;
    }

    /**
     * When a tile is placed on the board, this method should be called on
     * it to propagate existing claims to the appropriate features on this
     * tile.  It will determine if any city features are connected to
     * cities that are already claimed, and if any road features are
     * connected to roads that are already claimed and if any grassland is
     * connected to grassland that is claimed.
     *
     * <p> If, in the process of initializing the claims for this tile, we
     * discover that this tile connects two previously disconnected
     * claims, those claims will be joined. The affected tiles and piecens
     * will have their claim groups updated.
     *
     * @param tiles a sorted list of the tiles on the board (which need
     * not include the tile whose features are being configured).
     * @param tile the tile whose features should be configured.
     */
    public static void inheritClaims (List tiles, VenisonTile tile)
    {
        // obtain our neighboring tiles
        VenisonTile[] neighbors = new VenisonTile[4];
        neighbors[NORTH] = findTile(tiles, tile.x, tile.y-1);
        neighbors[EAST] = findTile(tiles, tile.x+1, tile.y);
        neighbors[SOUTH] = findTile(tiles, tile.x, tile.y+1);
        neighbors[WEST] = findTile(tiles, tile.x-1, tile.y);

        // for each feature in the tile, determine whether or not the
        // neighboring tile's matching feature is claimed
        for (int i = 0; i < tile.features.length; i ++) {
            int ftype = tile.features[i].type;
            int fmask = tile.features[i].edgeMask;
            int cgroup = 0;

            // iterate over all of the possible adjacency possibilities,
            // first looking for a claim group to inherit
            for (int c = 0; c < FeatureUtil.ADJACENCY_MAP.length; c += 3) {
                int mask = FeatureUtil.ADJACENCY_MAP[c];
                int dir = FeatureUtil.ADJACENCY_MAP[c+1];
                int opp_mask = FeatureUtil.ADJACENCY_MAP[c+2];

                // if this feature doesn't have this edge, skip it
                if ((fmask & mask) == 0) {
                    continue;
                }

                // translate the target direction accordingly
                dir = (dir + tile.orientation) % 4;

                // make sure we have a neighbor in the appropriate
                // direction
                if (neighbors[dir] == null) {
                    continue;
                }

                // it looks like we have a match, so translate the target
                // stuff into our orientation
                mask = FeatureUtil.translateMask(mask, tile.orientation);
                opp_mask = FeatureUtil.translateMask(
                    opp_mask, tile.orientation);

                // inherit the group of the opposing feature
                cgroup = neighbors[dir].getFeatureGroup(opp_mask);

                // and bail as soon as we find a non-zero claim group
                if (cgroup != 0) {
                    Log.info("Inherited claim [tile=" + tile +
                             ", fidx=" + i + ", cgroup=" + cgroup +
                             ", source=" + neighbors[dir] + "].");
                    break;
                }
            }

            // if we didn't inherit a claim group, skip to the next
            // feature
            if (cgroup == 0) {
                continue;
            }

            // initialize the feature's claim group
            tile.claims[i] = cgroup;

            // otherwise, iterate over all of the possible adjacency
            // possibilities, propagating our group to connected features
            for (int c = 0; c < FeatureUtil.ADJACENCY_MAP.length; c += 3) {
                int mask = FeatureUtil.ADJACENCY_MAP[c];
                int dir = FeatureUtil.ADJACENCY_MAP[c+1];
                int opp_mask = FeatureUtil.ADJACENCY_MAP[c+2];

                // if this feature doesn't have this edge, skip it
                if ((fmask & mask) == 0) {
                    continue;
                }

                // translate the target direction accordingly
                dir = (dir + tile.orientation) % 4;

                // make sure we have a neighbor in the appropriate
                // direction
                if (neighbors[dir] == null) {
                    continue;
                }

                // it looks like we have a match, so translate the target
                // stuff into our orientation
                mask = FeatureUtil.translateMask(mask, tile.orientation);
                opp_mask = FeatureUtil.translateMask(
                    opp_mask, tile.orientation);

                // make sure the neighbor in question isn't the one from
                // which we inherited the group
                int ogroup = neighbors[dir].getFeatureGroup(opp_mask);
                if (ogroup == cgroup) {
                    continue;
                }

                // if we've already been assigned to a group, we propagate
                // our group to the opposing feature
                int fidx = neighbors[dir].getFeatureIndex(opp_mask);
                if (fidx >= 0) {
                    Log.info("Propagating group [fidx=" + i +
                             ", cgroup=" + cgroup +
                             ", dir=" + dir + "].");
                    setFeatureGroup(tiles, neighbors[dir],
                                    fidx, cgroup, mask);

                } else {
                    Log.warning("Can't join-propagate feature " +
                                "[self=" + tile +
                                ", target=" + neighbors[dir] +
                                ", fidx=" + fidx + ", cgroup=" + cgroup +
                                ", destEdge=" + opp_mask + 
                                ", srcEdge=" + mask + "].");
                }
            }
        }
    }

    /**
     * Sets the claim group for the specified feature in this tile and
     * propagates that claim group to all connected features.
     *
     * @param tiles a sorted list of the tiles on the board.
     * @param tile the tile that contains the feature whose claim group is
     * being set.
     * @param featureIndex the index of the feature.
     * @param claimGroup the claim group value to set.
     * @param entryEdgeMask the edge from which we are propagating this
     * claim group (to avoid traversing back over that edge when
     * propagating the group further).
     */
    public static void setFeatureGroup (
        List tiles, VenisonTile tile, int featureIndex,
        int claimGroup, int entryEdgeMask)
    {
        // set the claim group for this feature on this tile
        tile.setFeatureGroup(featureIndex, claimGroup);

        // now propagate this feature to connected features
        int ftype = tile.features[featureIndex].type;
        int fmask = tile.features[featureIndex].edgeMask;

        // iterate over all of the possible adjacency possibilities
        for (int c = 0; c < FeatureUtil.ADJACENCY_MAP.length; c += 3) {
            int mask = FeatureUtil.ADJACENCY_MAP[c];
            int dir = FeatureUtil.ADJACENCY_MAP[c+1];
            int opp_mask = FeatureUtil.ADJACENCY_MAP[c+2];
            VenisonTile neighbor = null;

            // if this feature doesn't have this edge, skip it
            if ((fmask & mask) == 0) {
                continue;
            }

            // figure out if this would be the tile from which we
            // propagated into our current tile and skip it if so
            opp_mask = FeatureUtil.translateMask(opp_mask, tile.orientation);
            if ((opp_mask & entryEdgeMask) != 0) {
                continue;
            }

            // make sure we have a neighbor in this direction
            dir = (dir + tile.orientation) % 4;
            switch (dir) {
            case NORTH: neighbor = findTile(tiles, tile.x, tile.y-1); break;
            case EAST: neighbor = findTile(tiles, tile.x+1, tile.y); break;
            case SOUTH: neighbor = findTile(tiles, tile.x, tile.y+1); break;
            case WEST: neighbor = findTile(tiles, tile.x-1, tile.y); break;
            }
            if (neighbor == null) {
                continue;
            }

            // it looks like we have a match, so translate the target mask
            // into our orientation
            mask = FeatureUtil.translateMask(mask, tile.orientation);

            // propagate, propagate, propagate
            int fidx = neighbor.getFeatureIndex(opp_mask);
            if (fidx >= 0) {
                // only set the feature in the neighbor if they aren't
                // already set to the appropriate group (prevent loops)
                if (neighbor.claims[fidx] != claimGroup) {
                    setFeatureGroup(tiles, neighbor, fidx, claimGroup, mask);
                }

            } else {
                Log.warning("Can't propagate feature [self=" + tile +
                            ", target=" + neighbor + ", fidx=" + fidx +
                            ", cgroup=" + claimGroup + ", srcEdge=" + mask +
                            ", destEdge=" + opp_mask + "].");
            }
        }
    }

    /**
     * Computes the score for the specified feature and returns it. If the
     * feature is complete (has no unconnected edges), the score will be
     * positive. If it is incomplete, the score will be negative.
     *
     * @param tiles a sorted list of the tiles on the board.
     * @param tile the tile that contains the feature whose score should
     * be computed.
     * @param featureIndex the index of the feature in the containing
     * tile.
     *
     * @return a positive score for a completed feature group, a negative
     * score for a partial feature group.
     */
    public static int computeFeatureScore (
        List tiles, VenisonTile tile, int featureIndex)
    {
        // determine what kind of feature it is
        Feature feature = tile.features[featureIndex];

        switch (feature.type) {
        case ROAD:
            return computeFeatureScore(tiles, tile, feature, new ArrayList());

        case CITY: {
            int score =
                computeFeatureScore(tiles, tile, feature, new ArrayList());
            // cities receive a 2x multiplier if they are larger than size
            // two and are completed
            return (score > 2) ? score*2 : score;
        }

        case CLOISTER:
            // cloister's score specially
            return computeCloisterScore(tiles, tile);

        default:
        case GRASS:
            // grass doesn't score
            return 0;
        }
    }

    /**
     * A helper function for {@link
     * #computeFeatureScore(List,VenisonTile,int)}.
     */
    protected static int computeFeatureScore (
        List tiles, VenisonTile tile, Feature feature, List seen)
    {
        // if we've already counted this tile, bail
        if (seen.contains(tile)) {
            return 0;
        }

        // otherwise add this tile to the seen list
        seen.add(tile);

        // cities have a base score of one but get a one point bonus if
        // they have a shield on them
        int score = (feature.type == CITY && tile.hasShield) ? 2 : 1;
        boolean missedNeighbor = false;

        // now figure out what connected features there are, if any
        int ftype = feature.type;
        int fmask = feature.edgeMask;

        // iterate over all of the possible adjacency possibilities
        for (int c = 0; c < FeatureUtil.ADJACENCY_MAP.length; c += 3) {
            int mask = FeatureUtil.ADJACENCY_MAP[c];
            int dir = FeatureUtil.ADJACENCY_MAP[c+1];
            int opp_mask = FeatureUtil.ADJACENCY_MAP[c+2];
            VenisonTile neighbor = null;

            // if this feature doesn't have this edge, skip it
            if ((fmask & mask) == 0) {
                continue;
            }

            // make sure we have a neighbor in this direction
            dir = (dir + tile.orientation) % 4;
            switch (dir) {
            case NORTH: neighbor = findTile(tiles, tile.x, tile.y-1); break;
            case EAST: neighbor = findTile(tiles, tile.x+1, tile.y); break;
            case SOUTH: neighbor = findTile(tiles, tile.x, tile.y+1); break;
            case WEST: neighbor = findTile(tiles, tile.x-1, tile.y); break;
            }
            if (neighbor == null) {
                // if we don't have a neighbor in a direction that we
                // need, we're an incomplete feature. alas
                missedNeighbor = true;
                continue;
            }

            // translate the target mask into our orientation
            mask = FeatureUtil.translateMask(mask, tile.orientation);
            opp_mask = FeatureUtil.translateMask(opp_mask, tile.orientation);

            // propagate, propagate, propagate
            int fidx = neighbor.getFeatureIndex(opp_mask);
            if (fidx < 0) {
                Log.warning("Tile mismatch while scoring [self=" + tile +
                            ", target=" + neighbor + ", fidx=" + fidx +
                            ", srcEdge=" + mask +
                            ", destEdge=" + opp_mask + "].");

            } else {
                // add the score for this neighbor
                int nscore = computeFeatureScore(
                    tiles, neighbor, neighbor.features[fidx], seen);
                // if our neighbor returned a negative score, we convert
                // it back to positive and make a note to make it negative
                // at the end
                if (nscore < 0) {
                    score += -nscore;
                    missedNeighbor = true;
                } else {
                    score += nscore;
                }
            }
        }

        return missedNeighbor ? -score : score;
    }

    /**
     * A helper function for {@link
     * #computeFeatureScore(List,VenisonTile,int)}.
     */
    protected static int computeCloisterScore (List tiles, VenisonTile tile)
    {
        int score = 0;

        // all we need to know are how many neighbors this guy has (we
        // count ourselves as well, just for code simplicity)
        for (int dx = -1; dx < 1; dx++) {
            for (int dy = -1; dy < 1; dy++) {
                if (findTile(tiles, tile.x + dx, tile.y + dy) != null) {
                    score++;
                }
            }
        }

        // incomplete cloisters return a negative score
        return (score == 9) ? 9 : -score;
    }

    /**
     * Locates and returns the tile with the specified coordinates.
     *
     * @param tiles a sorted list of tiles.
     *
     * @return the tile with the requested coordinates or null if no tile
     * exists at those coordinates.
     */
    protected static VenisonTile findTile (List tiles, int x, int y)
    {
        IntTuple coord = new IntTuple(x, y);
        int tidx = Collections.binarySearch(tiles, coord);
        return (tidx >= 0) ? (VenisonTile)tiles.get(tidx) : null;
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
     * Returns the next unused claim group value.
     */
    public static int nextClaimGroup ()
    {
        return ++_claimGroupCounter;
    }

    /** Used to generate our standard tile set. */
    protected static void addTiles (int count, List list, VenisonTile tile)
    {
        for (int i = 0; i  < count-1; i++) {
            list.add(tile.clone());
        }
        list.add(tile);
    }

    /** Used to generate claim group values. */
    protected static int _claimGroupCounter;

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
        CITY, CITY, GRASS, GRASS, // TWO_CITY_TWO
        GRASS, CITY, GRASS, CITY, // TWO_CITY_TWO_ACROSS
        CITY, GRASS, GRASS, GRASS, // CITY_ONE
        CITY, ROAD, ROAD, GRASS, // CITY_ONE_ROAD_RIGHT
        CITY, GRASS, ROAD, ROAD, // CITY_ONE_ROAD_LEFT
        CITY, ROAD, ROAD, ROAD, // CITY_ONE_ROAD_TEE
        CITY, ROAD, GRASS, ROAD, // CITY_ONE_ROAD_STRAIGHT
        GRASS, GRASS, GRASS, GRASS, // CLOISTER_PLAIN
        GRASS, GRASS, ROAD, GRASS, // CLOISTER_ROAD
        ROAD, ROAD, ROAD, ROAD, // FOUR_WAY_ROAD
        GRASS, ROAD, ROAD, ROAD, // THREE_WAY_ROAD
        ROAD, GRASS, ROAD, GRASS, // STRAIGHT_ROAD
        GRASS, GRASS, ROAD, ROAD, // CURVED_ROAD
    };

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

        addTiles(2, TILE_SET, new VenisonTile(TWO_CITY_TWO, false));
        addTiles(3, TILE_SET, new VenisonTile(TWO_CITY_TWO_ACROSS, false));

        addTiles(5, TILE_SET, new VenisonTile(CITY_ONE, false));
        addTiles(3, TILE_SET, new VenisonTile(CITY_ONE_ROAD_RIGHT, false));
        addTiles(3, TILE_SET, new VenisonTile(CITY_ONE_ROAD_LEFT, false));
        addTiles(3, TILE_SET, new VenisonTile(CITY_ONE_ROAD_TEE, false));
        addTiles(3, TILE_SET, new VenisonTile(CITY_ONE_ROAD_STRAIGHT, false));

        addTiles(4, TILE_SET, new VenisonTile(CLOISTER_PLAIN, false));
        addTiles(2, TILE_SET, new VenisonTile(CLOISTER_ROAD, false));

        addTiles(1, TILE_SET, new VenisonTile(FOUR_WAY_ROAD, false));
        addTiles(4, TILE_SET, new VenisonTile(THREE_WAY_ROAD, false));
        addTiles(8, TILE_SET, new VenisonTile(STRAIGHT_ROAD, false));
        addTiles(9, TILE_SET, new VenisonTile(CURVED_ROAD, false));
    }
}
