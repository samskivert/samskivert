//
// $Id: AtlantiManager.java,v 1.5 2001/10/12 20:34:13 mdb Exp $

package com.threerings.venison;

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
        _tiles = TileUtil.getStandardTileSet();
        Collections.shuffle(_tiles);

        // clear out the tile set
        _venobj.setTiles(new DSet(VenisonTile.class));
        _venobj.addToTiles(TileUtil.STARTING_TILE);
    }

    protected void turnWillStart ()
    {
        // let the players know what the next tile is that should be
        // played
        VenisonTile tile = (VenisonTile)_tiles.remove(0);
        _venobj.setCurrentTile(tile);
    }

    /** Handles place tile requests. */
    protected class PlaceTileHandler implements MessageHandler
    {
        public void handleEvent (MessageEvent event)
        {
            VenisonTile tile = (VenisonTile)event.getArgs()[0];
            // don't do no checking at present
            _venobj.addToTiles(tile);
            // and since they placed their tile, we end the turn
            endTurn();
        }
    }

    /** Handles place piecen requests. */
    protected class PlacePiecenHandler implements MessageHandler
    {
        public void handleEvent (MessageEvent event)
        {
        }
    }

    /** A casted reference to our Venison game object. */
    protected VenisonObject _venobj;

    /** The (shuffled) list of tiles remaining to be played in this
     * game. */
    protected List _tiles;
}
