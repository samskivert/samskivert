//
// $Id: AtlantiManager.java,v 1.8 2001/10/17 02:19:54 mdb Exp $

package com.threerings.venison;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    /** Handles place tile requests. */
    protected class PlaceTileHandler implements MessageHandler
    {
        public void handleEvent (MessageEvent event)
        {
            VenisonTile tile = (VenisonTile)event.getArgs()[0];

            // make sure this is a valid placement
            if (TileUtil.isValidPlacement(_tiles, tile)) {
                // add the tile to the lsit
                _tiles.add(tile);

                // inherit its claim groups
                TileUtil.inheritClaims(_tiles, tile);

                Log.info("Placed tile " + tile + ".");

                // add the tile to the tiles set
                _venobj.addToTiles(tile);

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
}
