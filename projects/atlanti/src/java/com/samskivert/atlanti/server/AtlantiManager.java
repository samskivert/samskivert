//
// $Id: AtlantiManager.java,v 1.19 2001/11/24 22:41:21 mdb Exp $

package com.threerings.venison;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.samskivert.util.HashIntMap;
import com.samskivert.util.StringUtil;

import com.threerings.presents.dobj.ElementAddedEvent;
import com.threerings.presents.dobj.ElementRemovedEvent;
import com.threerings.presents.dobj.ElementUpdatedEvent;
import com.threerings.presents.dobj.DSet;
import com.threerings.presents.dobj.SetListener;
import com.threerings.presents.dobj.MessageEvent;

import com.threerings.crowd.chat.ChatService;
import com.threerings.crowd.chat.ChatMessageHandler;
import com.threerings.crowd.chat.ChatProvider;

import com.threerings.parlor.turn.TurnGameManager;

/**
 * The main coordinator of the Venison game on the server side.
 */
public class VenisonManager
    extends TurnGameManager implements VenisonCodes, SetListener
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
        registerMessageHandler(
            ChatService.SPEAK_REQUEST, new ChatMessageHandler());
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

//          // shave off most of the tiles for the moment
//          while (_tilesInBox.size() > 15) {
//              _tilesInBox.remove(0);
//          }

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
        VenisonTile start = TileUtil.getStartingTile();
        _venobj.addToTiles(start);
        _tiles.add(start);
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
     * At the end of the game, we need to compute the final scores.
     */
    protected void gameDidEnd ()
    {
        super.gameDidEnd();

        // compute the final scores by iterating over each tile and
        // scoring its features
        Piecen[] piecens = getPiecens();
        Iterator iter = _venobj.tiles.elements();
        while (iter.hasNext()) {
            VenisonTile tile = (VenisonTile)iter.next();
            scoreFeatures(tile, piecens, true);
        }

        // lastly, we have to score the farms (cue the ominous drums)...
        scoreFarms();

        // update the final scores
        _venobj.setScores(_venobj.scores);
    }

    /**
     * Creates an array of piecens based on the contents of the piecens
     * set in the game object, suitable for passing to {@link
     * #scoreFeatures}.
     */
    protected Piecen[] getPiecens ()
    {
        // create a piecen array that we can manipulate while scoring
        Piecen[] piecens = new Piecen[_venobj.piecens.size()];
        Iterator iter = _venobj.piecens.elements();
        for (int i = 0; iter.hasNext(); i++) {
            piecens[i] = (Piecen)iter.next();
        }
        return piecens;
    }

    /**
     * Scores the features on this tile.
     *
     * @param tile the tile whose features should be scored.
     * @param piecens an array of the pieces on the board which we can
     * manipulate directly without having to wait for element removed
     * events to be dispatched.
     * @param finalTally during the final tally, we score differently and
     * we don't remove piecens from the board as we score them.
     */
    protected void scoreFeatures (
        VenisonTile tile, Piecen[] piecens, boolean finalTally)
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
            int[] cgv = getClaimGroupVector(cgroup, piecens);
            if (cgv == null) {
                // if not, we don't have anything to score
//                  Log.info("Not scoring unclaimed feature " +
//                           "[ttype=" + tile.type + ", feat=" + f +
//                           ", cgroup=" + cgroup + "].");
                continue;
            }

            // we do have something to score, so we compute the score for
            // this feature
            int score = TileUtil.computeFeatureScore(_tiles, tile, i);

            // if the score is positive, it's a completed feature and we
            // score it regardless, we score incomplete features only
            // during the final tally
            if (score > 0 || finalTally) {
                String qual = (score > 0) ? "Completed" : "Incomplete";

                // convert the score into a positive value
                score = Math.abs(score);

                // adjust and report the scores
                StringBuffer names = new StringBuffer();
                for (int p = 0; p < cgv.length; p++) {
                    // adjust the score
                    _venobj.scores[p] += (score * cgv[p]);

                    // append the scorers name to the list
                    if (cgv[p] > 0) {
                        if (names.length() > 0) {
                            names.append(", ");
                        }
                        names.append(_players[p]);
                    }
                }

                String message = qual + " " + TileCodes.FEATURE_NAMES[f.type] +
                    " scored " + score + " points for " + names + ".";
                ChatProvider.sendSystemMessage(_venobj.getOid(), message);

                Log.info("New scores: " + StringUtil.toString(_venobj.scores));

                // broadcast the new scores if this isn't the final tally
                if (!finalTally) {
                    _venobj.setScores(_venobj.scores);
                }

                // and free up the scored piecens
                removePiecens(cgroup, piecens, finalTally);

            } else {
//                  Log.info("Not scoring incomplete feature " +
//                           "[ttype=" + tile.type + ", feat=" + f +
//                           ", score=" + score + "].");
            }
        }

        // we also may have completed a cloister, so we check that as well
        for (int dx = -1; dx < 2; dx++) {
            for (int dy = -1; dy < 2; dy++) {
                // find our neighbor and make sure they exist
                VenisonTile neighbor =
                    TileUtil.findTile(_tiles, tile.x + dx, tile.y + dy);
                if (neighbor == null) {
                    continue;
                }

                // scan their features arrays for claimed cloisters
                for (int i = 0; i < neighbor.features.length; i++) {
                    Feature f = neighbor.features[i];
                    Piecen p = neighbor.piecen;

                    // is a cloister
                    if (f.type != TileCodes.CLOISTER) {
                        continue;
                    }

                    // tile has a piecen
                    if (p == null) {
//                          Log.info("Skipping non-piecen having " +
//                                   "cloister tile [tile=" + neighbor +
//                                   ", feat=" + f + "].");
                        continue;
                    }

                    // piecen is on cloister feature
                    if (neighbor.claims[i] != p.claimGroup) {
//                          Log.info("Skipping cloister tile with piecen on " +
//                                   "non-cloister [tile=" + neighbor +
//                                   ", feat=" + f + "].");
                        continue;
                    }

                    // score the cloister
                    int score = TileUtil.computeFeatureScore(
                        _tiles, neighbor, i);

                    // if it's completed or if we're doing the final
                    // tally, we score it
                    if (score > 0 || finalTally) {
                        String qual = (score > 0) ?
                            "complete" : "incomplete";

                        // coerce the score into positive land
                        score = Math.abs(score);

                        // deliver a chat notification to tell the
                        // players about the score
                        String message = _players[p.owner] + " scored " +
                            score + " points for " + qual + " cloister.";
                        ChatProvider.sendSystemMessage(
                            _venobj.getOid(), message);

                        // add the score to the owning player
                        _venobj.scores[p.owner] += score;

                        // only broadcast the updated scores if this isn't
                        // the final tally
                        if (!finalTally) {
                            _venobj.setScores(_venobj.scores);
                        }

                        // and clear out the piecen (only removing it from
                        // the piecen set if we're not in the final tally)
                        removePiecen(p, !finalTally);
                    }
                }
            }
        }
    }

    /**
     * Scores the farms, which is the final act of scoring.
     */
    protected void scoreFarms ()
    {
        HashIntMap cities = new HashIntMap();
        int[] cityScores = new int[_players.length];

        // clear out the claims for incompleted cities and claim unclaimed
        // completed cities
        TileUtil.prepCitiesForScoring(_tiles);

        // do the big process-ola
        int tsize = _tiles.size();
        for (int i = 0; i < tsize; i++) {
            VenisonTile tile = (VenisonTile)_tiles.get(i);

            // iterate over all of the city features in this tile
            for (int f = 0; f < tile.features.length; f++) {
                // get the claim group for this feature
                int cityClaim = tile.claims[f];

                // skip unclaimed and non-city features
                if (tile.features[f].type != TileCodes.CITY ||
                    cityClaim == 0) {
                    continue;
                }

                // get the list associated with this claim group
                int[] claims = (int[])cities.get(cityClaim);
                if (claims == null) {
                    // create a claim vector if we've not got one.  if a
                    // city had 35 separately claimed farms around it, all
                    // the piecens in the game would be in play and the
                    // city would not have been claimed which would be an
                    // extremely pathological case, but we love pathology
                    // (especially when we don't have a resizable int list
                    // class handy)
                    claims = new int[35];
                    cities.put(cityClaim, claims);
                }

                // iterate over all of the grass features that are
                // connected to city features on this tile and add their
                // claim groups the list for this city feature
                int[] grasses = FeatureUtil.CITY_GRASS_MAP[tile.type-1];
                for (int g = 0; g < grasses.length; g++) {
                    int farmClaim = tile.claims[grasses[g]];

                    // only worry about claimed grass regions
                    if (farmClaim == 0) {
                        Log.info("Ignoring unclaimed farm group " +
                                 "[tile=" + tile +
                                 ", fidx=" + grasses[g] + "].");
                        continue;
                    }

                    // and the farm claim group to the list
                    for (int c = 0; c < claims.length; c++) {
                        // don't add the farm claim twice
                        if (claims[c] == farmClaim) {
                            break;
                        } else if (claims[c] == 0) {
                            claims[c] = farmClaim;
                            Log.info("Noting city/farm abuttal " +
                                     "[tile=" + tile +
                                     ", cityClaim=" + cityClaim +
                                     ", farmClaim=" + farmClaim + "].");
                            break;
                        }
                    }
                }
            }
        }

        // now for each city, we look to see who has the most piecens that
        // are connected to the city by farms
        Iterator iter = cities.keys();
        while (iter.hasNext()) {
            int cityClaim = ((Integer)iter.next()).intValue();
            int[] farmClaims = (int[])cities.get(cityClaim);
            int[] pcount = new int[_players.length];
            int max = 0;

            Iterator piter = _venobj.piecens.elements();
            while (piter.hasNext()) {
                Piecen p = (Piecen)piter.next();
                // see if the piecen is on any of the farms
                for (int c = 0; c < farmClaims.length; c++) {
                    if (p.claimGroup == farmClaims[c]) {
                        Log.info("Counting piecen [cityClaim=" + cityClaim +
                                 ", farmClaim=" + farmClaims[c] +
                                 ", piecen=" + p + "].");
                        // increment their count and track the max
                        if (max < ++pcount[p.owner]) {
                            max = pcount[p.owner];
                        }
                    }
                }
            }

            Log.info("Counted city [cityClaim=" + cityClaim +
                     ", counts=" + StringUtil.toString(pcount) + "].");

            // ignore this city if no one has any farmers nearby
            if (max == 0) {
                continue;
            }

            // now score four points for every player that has the max
            for (int i = 0; i < pcount.length; i++) {
                if (pcount[i] == max) {
                    Log.info("Scoring city for player [cgroup=" + cityClaim +
                             ", player=" + _players[i] +
                             ", pcount=" + pcount[i] + "].");
                    cityScores[i] += 4;
                }
            }
        }

        // now report the scoring and transfer the counts to the score
        // array
        for (int i = 0; i < _players.length; i++) {
            if (cityScores[i] > 0) {
                _venobj.scores[i] += cityScores[i];
                String message = _players[i] + " scores " + cityScores[i] +
                    " points for farmed cities.";
                ChatProvider.sendSystemMessage(_venobj.getOid(), message);
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
     * @param claimGroup the claim group that we're scoring.
     * @param piecens an array to use when looking for matching piecens.
     *
     * @return an array for the specified claim group or null if no
     * players have a piecen claiming the specified claim group.
     */
    protected int[] getClaimGroupVector (int claimGroup, Piecen[] piecens)
    {
        // clear out the vector
        Arrays.fill(_claimGroupVector, 0);

        // iterate over the piecens
        int max = 0;
        for (int i = 0; i < piecens.length; i++) {
            Piecen piecen = piecens[i];
            if (piecen == null) {
                continue;
            } else if (piecen.claimGroup == claimGroup) {
                // color == player index... somewhat sketchy
                if (++_claimGroupVector[piecen.owner] > max) {
                    // keep track of the highest scorer
                    max = _claimGroupVector[piecen.owner];
                }
            }
        }

        // now cut out everyone with scores less than the highest score
        for (int i = 0; i < _claimGroupVector.length; i++) {
            _claimGroupVector[i] = (_claimGroupVector[i] < max) ? 0 : 1;
        }

        return (max == 0) ? null : _claimGroupVector;
    }

    /**
     * Removes piecens either from the supplied array or from the game
     * object piecen set if no array is supplied.
     */
    protected void removePiecens (int claimGroup, Piecen[] piecens,
                                  boolean finalTally)
    {
        // we always clear the piecens from the array
        for (int i = 0; i < piecens.length; i++) {
            if (piecens[i] == null) {
                continue;
            } else if (piecens[i].claimGroup == claimGroup) {
                piecens[i] = null;
            }
        }

        // if this isn't the final tally, we also clear 'em from the board
        if (!finalTally) {
            Iterator iter = _venobj.piecens.elements();
            while (iter.hasNext()) {
                Piecen p = (Piecen)iter.next();
                if (p.claimGroup == claimGroup) {
                    removePiecen(p, true);
                }
            }
        }
    }

    /**
     * Removes the piecen from the board and optionally removes it from
     * the piecen set.
     *
     * @param piecen the piecen to be removed.
     * @param removeFromPiecens if true, the piecen will also be removed
     * from the piecens set in the game object.
     */
    protected void removePiecen (Piecen piecen, boolean removeFromPiecens)
    {
        // locate the tile that contains this piecen
        int tidx = _tiles.indexOf(piecen);
        if (tidx == -1) {
            Log.warning("Requested to remove piecen that is not " +
                        "associated with any tile [piecen=" + piecen + "].");
        } else {
            VenisonTile tile = (VenisonTile)_tiles.get(tidx);
            // and clear the piecen
            tile.clearPiecen();
        }

        // also remove from the piecens dset if requested
        if (removeFromPiecens) {
            _venobj.removeFromPiecens(piecen.getKey());
        }
    }

    // documentation inherited
    public void elementAdded (ElementAddedEvent event)
    {
        // we react to piecen additions by potentially scoring the placed
        // piecen. we allow the piecen to be added to the piecens set
        // before scoring so that the players can see the piecen pop up on
        // their screen and then disappear with a scoring notice rather
        // than never show up at all; plus it simplifies our code
        if (event.getName().equals(VenisonObject.PIECENS)) {
            Piecen piecen = (Piecen)event.getElement();

            // make sure this is a valid placement
            VenisonTile tile = (VenisonTile)_venobj.tiles.get(piecen.getKey());
            if (tile == null) {
                Log.warning("Can't find tile for piecen scoring " +
                            piecen + ".");

            } else {
                // check to see if we added the piecen to a completed
                // feature, in which case we score and remove it
                scoreFeatures(tile, getPiecens(), false);
            }

            // now that we've scored the piecen, we can end the turn
            endTurn();
        }
    }

    // documentation inherited
    public void elementUpdated (ElementUpdatedEvent event)
    {
    }

    // documentation inherited
    public void elementRemoved (ElementRemovedEvent event)
    {
    }

    /** Handles place tile requests. */
    protected class PlaceTileHandler implements MessageHandler
    {
        public void handleEvent (MessageEvent event)
        {
            VenisonTile tile = (VenisonTile)event.getArgs()[0];
            int pidx = getTurnHolderIndex();

            // make sure it's this player's turn
            if (_playerOids[pidx] != event.getSourceOid()) {
                Log.warning("Requested to place tile by non-turn holder " +
                            "[event=" + event +
                            ", turnHolder=" + _venobj.turnHolder + "].");

            // make sure this is a valid placement
            } else if (TileUtil.isValidPlacement(_tiles, tile)) {
                // add the tile to the list and resort it
                _tiles.add(tile);
                Collections.sort(_tiles);

                // inherit its claim groups
                TileUtil.inheritClaims(_tiles, tile);

                // add the tile to the tiles set
                _venobj.addToTiles(tile);

                // placing a piece may have completed road or city
                // features. if it did, we score them now
                scoreFeatures(tile, getPiecens(), false);

                Log.info("Placed tile " + tile + ".");

                // if the player has no free piecens or if there are no
                // unclaimed features on this tile, we end their turn
                // straight away
                int pcount = TileUtil.countPiecens(_tiles, pidx);
                if (pcount >= PIECENS_PER_PLAYER ||
                    !tile.hasUnclaimedFeature()) {
                    endTurn();
                }

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
            VenisonTile tile = (VenisonTile)_venobj.tiles.get(piecen.getKey());
            int pidx = getTurnHolderIndex();
            int pcount = TileUtil.countPiecens(_venobj.piecens, pidx);

            // make sure it's this player's turn
            if (_playerOids[pidx] != event.getSourceOid()) {
                Log.warning("Requested to place piecen by non-turn holder " +
                            "[event=" + event +
                            ", turnHolder=" + _venobj.turnHolder + "].");

            // do some checking before we place the piecen
            } else if (pcount >= PIECENS_PER_PLAYER) {
                Log.warning("Requested to place piecen for player that " +
                            "has all of their piecens in play " +
                            "[event=" + event + "].");

            } else if (tile == null) {
                Log.warning("Can't find tile for requested piecen " +
                            "placement " + piecen + ".");

            } else if (tile.claims[piecen.featureIndex] != 0) {
                Log.warning("Requested to place piecen on claimed feature " +
                            "[tile=" + tile + ", piecen=" + piecen + "].");

            } else {
                // otherwise stick the piece in the tile to update the
                // claim groups
                tile.setPiecen(piecen, _tiles);

                // and add the piecen to the game object. when we receive
                // the piecen added event, we'll score it and then end the
                // turn
                _venobj.addToPiecens(piecen);
            }
        }
    }

    /** Handles place nothing requests. */
    protected class PlaceNothingHandler implements MessageHandler
    {
        public void handleEvent (MessageEvent event)
        {
            int pidx = getTurnHolderIndex();
            if (_playerOids[pidx] != event.getSourceOid()) {
                Log.warning("Requested to place nothing by non-turn holder " +
                            "[event=" + event +
                            ", turnHolder=" + _venobj.turnHolder + "].");

            } else {
                // player doesn't want to place anything, so we just end
                // the turn
                endTurn();
            }
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
