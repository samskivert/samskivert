//
// $Id: AtlantiManager.java,v 1.9 2001/10/17 04:34:14 mdb Exp $

package com.threerings.venison;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.samskivert.util.StringUtil;

import com.threerings.presents.dobj.DSet;
import com.threerings.presents.dobj.MessageEvent;

import com.threerings.parlor.turn.TurnGameManager;

/**
 * The main coordinator of the Venison game on the server side.
 */
public class VenisonManager
    extends TurnGameManager implements VenisonCodes
{
    // documentation inherited
    protected Class getPlaceObjectClass ()
    {
        return VenisonObject.class;
    }

    // documentation inherited
    protected void didInit ()
    {
        super.didInit();

        // register our message handlers
        registerMessageHandler(PLACE_TILE_REQUEST, new PlaceTileHandler());
        registerMessageHandler(
            PLACE_PIECEN_REQUEST, new PlacePiecenHandler());
        registerMessageHandler(
            PLACE_NOTHING_REQUEST, new PlaceNothingHandler());
    }

    // documentation inherited
    protected void didStartup ()
    {
        super.didStartup();

        // grab our own casted game object reference
        _venobj = (VenisonObject)_gameobj;
    }

    /**
     * In preparation for starting the game, we clear out the tile set and
     * put the starting tile into place.
     */
    protected void gameWillStart ()
    {
        super.gameWillStart();

        // generate a shuffled tile list
        _tilesInBox = TileUtil.getStandardTileSet();
        Collections.shuffle(_tilesInBox);

        // clear out our board tiles
        _tiles.clear();

        // create a claim group vector
        _claimGroupVector = new int[_players.length];

        // clear out the scores
        _venobj.setScores(new int[_players.length]);

        // clear out the tile and piecen set
        _venobj.setTiles(new DSet(VenisonTile.class));
        _venobj.setPiecens(new DSet(Piecen.class));

        // and add the starting tile
        _venobj.addToTiles(VenisonTile.STARTING_TILE);
        _tiles.add(VenisonTile.STARTING_TILE);
    }

    protected void turnWillStart ()
    {
        super.turnWillStart();

        // let the players know what the next tile is that should be
        // played
        VenisonTile tile = (VenisonTile)_tilesInBox.remove(0);
        _venobj.setCurrentTile(tile);
    }

    protected void turnDidEnd ()
    {
        super.turnDidEnd();

        // if there are no tiles left, we end the game
        if (_tilesInBox.size() == 0) {
            endGame();
        }
    }

    /**
     * Continue the game until we're out of tiles.
     */
    protected void setNextTurnHolder ()
    {
        // if we have tiles left, we move to the next player as normal
        if (_tilesInBox.size() > 0) {
            super.setNextTurnHolder();
        } else {
            // if we don't, we ensure that a new turn isn't started by
            // setting _turnIdx to -1
            _turnIdx = -1;
        }
    }

    /**
     * Determines whether or not the placement of this tile has completed
     * features that were previously incomplete.
     */
    protected void scoreCompletedFeatures (VenisonTile tile)
    {
        // potentially score all features on the tile
        for (int i = 0; i < tile.features.length; i++) {
            // we only need to worry about ROAD and CITY features because
            // those are the only features on this tile that we might have
            // completed
            Feature f = tile.features[i];
            if (f.type != TileCodes.CITY && f.type != TileCodes.ROAD) {
                continue;
            }

            // see if any piecens are even on a feature in this group
            int cgroup = tile.claims[i];
            int[] cgv = getClaimGroupVector(cgroup);
            if (cgv == null) {
                // if not, we don't have anything to score
                Log.info("Not scoring unclaimed feature " +
                         "[ttype=" + tile.type + ", feat=" + f +
                         ", cgroup=" + cgroup + "].");
                continue;
            }

            // we do have something to score, so we compute the score for
            // this feature
            int score = TileUtil.computeFeatureScore(_tiles, tile, i);

            // if the score is positive, it's a completed feature, so we
            // dole out the points and clear the associated piecens
            if (score > 0) {
                Log.info("Scoring feature [ttype=" + tile.type +
                         ", feature=" + f + ", score=" + score + "].");

                // adjust the scores
                for (int p = 0; p < cgv.length; p++) {
                    _venobj.scores[p] += (score * cgv[p]);
                }

                // broadcast the new scores
                _venobj.setScores(_venobj.scores);
                Log.info("New scores: " + StringUtil.toString(_venobj.scores));

                // and free up the scored piecens
                Iterator iter = _venobj.piecens.elements();
                while (iter.hasNext()) {
                    Piecen p = (Piecen)iter.next();
                    if (p.claimGroup == cgroup) {
                        Log.info("Removing piecen " + p + ".");
                        _venobj.removeFromPiecens(p.getKey());
                    }
                }

            } else {
                Log.info("Not scoring incomplete feature " +
                         "[ttype=" + tile.type + ", feat=" + f +
                         ", score=" + score + "].");
            }
        }
    }

    /**
     * Returns an int array with zeros and ones in the appropriate places
     * so that a score can be multiplied by a player's position in the
     * vector to determine whether or not they receive any points for the
     * scoring of a particular claim group.
     *
     * <p> Note that this function returns a static (to this instance)
     * vector, so it cannot be called again without overwriting values
     * returned previously.
     *
     * @return an array for the specified claim group or null if no
     * players have a piecen claiming the specified claim group.
     */
    protected int[] getClaimGroupVector (int claimGroup)
    {
        // clear out the vector
        Arrays.fill(_claimGroupVector, 0);

        // iterate over the piecens
        int max = 0;
        Iterator iter = _venobj.piecens.elements();
        while (iter.hasNext()) {
            Piecen piecen = (Piecen)iter.next();
            if (piecen.claimGroup == claimGroup) {
                // color == player index... somewhat sketchy
                if (++_claimGroupVector[piecen.color] > max) {
                    // keep track of the highest scorer
                    max = _claimGroupVector[piecen.color];
                }
            }
        }

        // now cut out everyone with scores less than the highest score
        for (int i = 0; i < _claimGroupVector.length; i++) {
            _claimGroupVector[i] = (_claimGroupVector[i] < max) ? 0 : 1;
        }

        return (max == 0) ? null : _claimGroupVector;
    }

    /** Handles place tile requests. */
    protected class PlaceTileHandler implements MessageHandler
    {
        public void handleEvent (MessageEvent event)
        {
            VenisonTile tile = (VenisonTile)event.getArgs()[0];

            // make sure this is a valid placement
            if (TileUtil.isValidPlacement(_tiles, tile)) {
                // add the tile to the list and resort it
                _tiles.add(tile);
                Collections.sort(_tiles);

                // inherit its claim groups
                TileUtil.inheritClaims(_tiles, tile);

                // add the tile to the tiles set
                _venobj.addToTiles(tile);

                // placing a piece may have completed road or city
                // features. if it did, we score them now
                scoreCompletedFeatures(tile);

                Log.info("Placed tile " + tile + ".");

            } else {
                Log.warning("Received invalid placement " + event + ".");
            }
        }
    }

    /** Handles place piecen requests. */
    protected class PlacePiecenHandler implements MessageHandler
    {
        public void handleEvent (MessageEvent event)
        {
            Piecen piecen = (Piecen)event.getArgs()[0];

            // make sure this is a valid placement
            VenisonTile tile = (VenisonTile)_venobj.tiles.get(piecen.getKey());
            if (tile == null) {
                Log.warning("Can't find tile for requested piecen " +
                            "placement " + piecen + ".");

            } else if (tile.claims[piecen.featureIndex] != 0) {
                Log.warning("Requested to place piecen on claimed feature " +
                            "[tile=" + tile + ", piecen=" + piecen + "].");

            } else {
                // otherwise stick the piece in the tile to update the
                // claim groups
                tile.setPiecen(piecen, _tiles);

                // and add the piecen to the game object
                _venobj.addToPiecens(piecen);
            }

            // end the turn
            endTurn();
        }
    }

    /** Handles place nothing requests. */
    protected class PlaceNothingHandler implements MessageHandler
    {
        public void handleEvent (MessageEvent event)
        {
            // player doesn't want to place anything, so we just end the
            // turn
            endTurn();
        }
    }

    /** A casted reference to our Venison game object. */
    protected VenisonObject _venobj;

    /** The (shuffled) list of tiles remaining to be played in this
     * game. */
    protected List _tilesInBox;

    /** A sorted list of the tiles that have been placed on the board. */
    protected List _tiles = new ArrayList();

    /** Used to score features groups. */
    protected int[] _claimGroupVector;
}
