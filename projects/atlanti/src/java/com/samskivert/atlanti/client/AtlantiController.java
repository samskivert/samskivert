//
// $Id

package com.threerings.venison;

import java.awt.event.ActionEvent;
import com.samskivert.util.ListUtil;

import com.threerings.presents.dobj.AttributeChangedEvent;
import com.threerings.presents.dobj.DSet;
import com.threerings.presents.dobj.ElementAddedEvent;
import com.threerings.presents.dobj.ElementRemovedEvent;
import com.threerings.presents.dobj.ElementUpdatedEvent;
import com.threerings.presents.dobj.MessageEvent;
import com.threerings.presents.dobj.SetListener;

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
    extends TurnGameController implements VenisonCodes, SetListener
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

        // find out what our index is and use that as our piecen color
        _selfIndex = ListUtil.indexOfEqual(_venobj.players, _self.username);
        if (_selfIndex != -1) {
            // use our player index as the piecen color directly
            _panel.board.setNewPiecenColor(_selfIndex);
        }

        // grab the tiles and piecens from the game object and configure
        // the board with them
        _panel.board.setTiles(_venobj.tiles);
        _panel.board.setPiecens(_venobj.piecens);

        // if it's our turn, set the tile to be placed
        if (isOurTurn()) {
            _panel.board.setTileToBePlaced(_venobj.currentTile);
        }
    }

    // documentation inherited
    protected void turnDidChange (String turnHolder)
    {
        super.turnDidChange(turnHolder);

        // if it's our turn, set the tile to be placed
        if (turnHolder.equals(_self.username)) {
            _panel.board.setTileToBePlaced(_venobj.currentTile);
        }
    }

    // documentation inherited
    public void attributeChanged (AttributeChangedEvent event)
    {
        super.attributeChanged(event);

        // handle the setting of the board state
        if (event.getName().equals(VenisonObject.TILES)) {
            _panel.board.setTiles(_venobj.tiles);

        } else if (event.getName().equals(VenisonObject.PIECENS)) {
            _panel.board.setPiecens(_venobj.piecens);
        }
    }

    // documentation inherited
    public void elementAdded (ElementAddedEvent event)
    {
        // we care about additions to TILES and PIECENS
        if (event.getName().equals(VenisonObject.TILES)) {
            // a tile was added, add it to the board
            VenisonTile tile = (VenisonTile)event.getElement();
            _panel.board.addTile(tile);

        } else if (event.getName().equals(VenisonObject.PIECENS)) {
            // a piecen was added, place it on the board
            Piecen piecen = (Piecen)event.getElement();
            _panel.board.placePiecen(piecen);
        }
    }

    // documentation inherited
    public void elementUpdated (ElementUpdatedEvent event)
    {
    }

    // documentation inherited
    public void elementRemoved (ElementRemovedEvent event)
    {
        if (event.getName().equals(VenisonObject.PIECENS)) {
            // a piecen was removed, update the board
            _panel.board.clearPiecen(event.getKey());
        }
    }

    // documentation inherited
    public boolean handleAction (ActionEvent action)
    {
        if (action.getActionCommand().equals(TILE_PLACED)) {
            VenisonTile tile = _panel.board.getPlacedTile();

            // the user placed the tile into a valid location. grab the
            // placed tile from the board and submit it to the server
            Object[] args = new Object[] { tile };
            MessageEvent mevt = new MessageEvent(
                _venobj.getOid(), PLACE_TILE_REQUEST, args);
            _ctx.getDObjectManager().postEvent(mevt);

            // if we have no piecens to place or if there are no unclaimed
            // features on the placed tile, we immediately disable piecen
            // placement in the board and expect that the server will end
            // our turn
            int pcount = TileUtil.countPiecens(_venobj.piecens, _selfIndex);
            if (pcount >= PIECENS_PER_PLAYER || !tile.hasUnclaimedFeature()) {
                _panel.board.cancelPiecenPlacement();
                _panel.noplace.setEnabled(false);

            } else {
                // otherwise, enable the noplace button
                _panel.noplace.setEnabled(true);
            }

        } else if (action.getActionCommand().equals(PIECEN_PLACED)) {
            // the user placed a piecen on the tile. grab the piecen from
            // the placed tile and submit it to the server
            Object[] args = new Object[] {
                _panel.board.getPlacedTile().piecen };
            MessageEvent mevt = new MessageEvent(
                _venobj.getOid(), PLACE_PIECEN_REQUEST, args);
            _ctx.getDObjectManager().postEvent(mevt);

            // disable the noplace button
            _panel.noplace.setEnabled(false);

        } else if (action.getActionCommand().equals(PLACE_NOTHING)) {
            // turn off piecen placement in the board
            _panel.board.cancelPiecenPlacement();

            // the user doesn't want to place anything this turn. send a
            // place nothing request to the server
            MessageEvent mevt = new MessageEvent(
                _venobj.getOid(), PLACE_NOTHING_REQUEST, null);
            _ctx.getDObjectManager().postEvent(mevt);

            // disable the noplace button
            _panel.noplace.setEnabled(false);

        } else if (action.getActionCommand().equals(BACK_TO_LOBBY)) {
            // bail on out
            _ctx.getLocationDirector().moveBack();

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

    /** Our player index or -1 if we're not a player. */
    protected int _selfIndex;
}
