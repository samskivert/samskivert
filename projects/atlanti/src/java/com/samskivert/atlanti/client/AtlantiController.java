//
// $Id

package com.threerings.venison;

import java.awt.event.ActionEvent;

import com.threerings.presents.dobj.AttributeChangedEvent;
import com.threerings.presents.dobj.DSet;
import com.threerings.presents.dobj.MessageEvent;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.client.PlaceView;

import com.threerings.parlor.turn.TurnGameController;
import com.threerings.parlor.util.ParlorContext;

import com.threerings.venison.Log;

/**
 * The main coordinator of user interface activities on the client-side of
 * the Venison game.
 */
public class VenisonController
    extends TurnGameController implements VenisonCodes
{
    // documentation inherited
    protected void didInit ()
    {
        super.didInit();

        // get a handle on our body object
        _self = (BodyObject)_ctx.getClient().getClientObject();
    }

    // documentation inherited
    protected PlaceView createPlaceView ()
    {
        _panel = new VenisonPanel(_ctx, this);
        return _panel;
    }

    // documentation inherited
    public void willEnterPlace (PlaceObject plobj)
    {
        super.willEnterPlace(plobj);

        // get a casted reference to our game object
        _venobj = (VenisonObject)plobj;
    }

    // documentation inherited
    protected void turnDidChange (String turnHolder)
    {
        super.turnDidChange(turnHolder);

        // if it's our turn, set the tile to be placed. otherwise clear it
        // out
        if (turnHolder.equals(_self.username)) {
            Log.info("Setting tile to be placed: " + _venobj.currentTile);
            _panel.board.setTileToBePlaced(_venobj.currentTile);
        }

        // and refresh the tiles
        _panel.board.refreshTiles();
    }

    // documentation inherited
    public void attributeChanged (AttributeChangedEvent event)
    {
        super.attributeChanged(event);

        // handle the setting of the board state
        if (event.getName().equals(VenisonObject.TILES)) {
            _panel.board.setTiles(_venobj.tiles);
        }
    }

    // documentation inherited
    public boolean handleAction (ActionEvent action)
    {
        if (action.getActionCommand().equals(TILE_PLACED)) {
            // the user placed the tile into a valid location. the board
            // will have updated the position and orientation information
            // in the tile object accordingly, so we simply submit our
            // move to the server
            Object[] args = new Object[] { _venobj.currentTile };
            MessageEvent mevt = new MessageEvent(
                _venobj.getOid(), PLACE_TILE_REQUEST, args);
            _ctx.getDObjectManager().postEvent(mevt);

        } else {
            return super.handleAction(action);
        }

        return true;
    }

    /** A reference to our game panel. */
    protected VenisonPanel _panel;

    /** A reference to our game panel. */
    protected VenisonObject _venobj;

    /** A reference to our body object. */
    protected BodyObject _self;
}
