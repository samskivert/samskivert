//
// $Id: TurnIndicatorView.java,v 1.1 2001/10/16 01:41:55 mdb Exp $

package com.threerings.venison;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.samskivert.swing.VGroupLayout;

import com.threerings.presents.dobj.AttributeChangeListener;
import com.threerings.presents.dobj.AttributeChangedEvent;

import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.client.PlaceView;

/**
 * Displays who the current turn holder is as well as the tile they are
 * currently placing.
 */
public class TurnIndicatorView
    extends JPanel implements PlaceView, AttributeChangeListener
{
    /**
     * Creates a new turn indicator view which in turn creates its
     * subcomponents.
     */
    public TurnIndicatorView ()
    {
	setLayout(new VGroupLayout());

        // add our turn holder display
        _whoLabel = new JLabel();
        add(_whoLabel);

        // and add our tile display
        _tileLabel = new TileLabel();
        add(_tileLabel);

        // and add a tile's remaining label
        _countLabel = new JLabel();
        add(_countLabel);
    }

    // documentation inherited
    public void willEnterPlace (PlaceObject plobj)
    {
        // we want to grab a reference to the game object and add
        // ourselves as an attribute change listener
        _venobj = (VenisonObject)plobj;
        _venobj.addListener(this);
    }

    // documentation inherited
    public void didLeavePlace (PlaceObject plobj)
    {
        // remove our listening self
        _venobj.removeListener(this);
        _venobj = null;
    }

    // documentation inherited
    public void attributeChanged (AttributeChangedEvent event)
    {
        // we care about the current tile display
        if (event.getName().equals(VenisonObject.CURRENT_TILE)) {
            // display the tile to be placed
            _tileLabel.setTile(_venobj.currentTile);

        } else if (event.getName().equals(VenisonObject.STATE)) {
            switch (_venobj.state) {
            case VenisonObject.AWAITING_PLAYERS:
                _whoLabel.setText("Awaiting players...");
                _tileLabel.setTile(null);
                break;
            case VenisonObject.GAME_OVER:
                _whoLabel.setText("Game over.");
                _tileLabel.setTile(null);
                break;
            case VenisonObject.CANCELLED:
                _whoLabel.setText("Cancelled.");
                _tileLabel.setTile(null);
                break;
            }

            // update the tiles remaining
            updateRemainingTiles();

        } else if (event.getName().equals(VenisonObject.TURN_HOLDER)) {
            if (_venobj.state == VenisonObject.IN_PLAY) {
                _whoLabel.setText(_venobj.turnHolder);
            }

            // update the tiles remaining when the turn changes
            updateRemainingTiles();
        }
    }

    protected void updateRemainingTiles ()
    {
        _countLabel.setText("Tiles remaining: " + (72-_venobj.tiles.size()));
    }

    /** The label displaying whose turn it is. */
    protected JLabel _whoLabel;

    /** The label displaying the tile to be placed. */
    protected TileLabel _tileLabel;

    /** The label displaying the number of tiles remaining. */
    protected JLabel _countLabel;

    /** A reference to the game object. */
    protected VenisonObject _venobj;
}
