//
// $Id: PlayerInfoView.java,v 1.4 2002/05/21 04:45:09 mdb Exp $

package com.threerings.venison;

import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.threerings.presents.dobj.AttributeChangeListener;
import com.threerings.presents.dobj.AttributeChangedEvent;
import com.threerings.presents.dobj.SetListener;
import com.threerings.presents.dobj.EntryAddedEvent;
import com.threerings.presents.dobj.EntryUpdatedEvent;
import com.threerings.presents.dobj.EntryRemovedEvent;

import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.data.PlaceObject;

/**
 * Displays each of the players in the game, their piece color and their
 * score.
 */
public class PlayerInfoView
    extends JPanel
    implements PlaceView, AttributeChangeListener, SetListener
{
    /**
     * Constructs a new player info panel, ready for insertion into a
     * Venison game panel.
     */
    public PlayerInfoView ()
    {
        setLayout(new GridBagLayout());

        // we don't add our player info until we know how many players are
        // in the game (which we find out once we enter the game room)
    }

    // documentation inherited
    public void willEnterPlace (PlaceObject plobj)
    {
        // we want to grab a reference to the game object and add
        // ourselves as an attribute change listener
        _venobj = (VenisonObject)plobj;
        _venobj.addListener(this);

        // we need score and piecen labels arrays so that we can keep
        // track of per player information
        _scoreLabels = new JLabel[_venobj.players.length];
        _piecenLabels = new JLabel[_venobj.players.length];

        // now that we're here, we can add an entry for every player
        for (int i = 0; i < _venobj.players.length; i++) {
            addPlayer(i, _venobj.players[i]);
        }

        // if we have scores, update them
        if (_venobj.scores.length == _venobj.players.length) {
            updateScores();
        }

        // update the piecen count
        updatePiecenCount();
    }

    /**
     * Adds a player to the display.
     */
    protected void addPlayer (int idx, String username)
    {
        GridBagConstraints c = new GridBagConstraints();

        // create a label for their username
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0; // use up extra space on this label
        JLabel unlabel = new JLabel(username);
        unlabel.setForeground(Feature.PIECEN_COLOR_MAP[idx]);
        add(unlabel, c);

        // create a label for their score
        c.weightx = 0.0; // size label to its preferred width
        c.ipadx = 5; // 5 pixels between this label and its neighbors
        c.fill = GridBagConstraints.NONE;
        _scoreLabels[idx] = new JLabel("0");
        _scoreLabels[idx].setForeground(Color.black);
        add(_scoreLabels[idx], c);

        // create a label for their piecen count
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.ipadx = 0; // turn off padding
        _piecenLabels[idx] = new JLabel("(0)");
        _piecenLabels[idx].setForeground(Feature.PIECEN_COLOR_MAP[idx]);
        add(_piecenLabels[idx], c);
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
        // we care about the scores (which change)
        if (event.getName().equals(VenisonObject.SCORES)) {
            updateScores();
        }
    }

    // documentation inherited
    public void entryAdded (EntryAddedEvent event)
    {
        updatePiecenCount();
    }

    // documentation inherited
    public void entryUpdated (EntryUpdatedEvent event)
    {
    }

    // documentation inherited
    public void entryRemoved (EntryRemovedEvent event)
    {
        updatePiecenCount();
    }

    /**
     * Reads the scores from the game object and updates the view.
     */
    protected void updateScores ()
    {
        for (int i = 0; i < _venobj.scores.length; i++) {
            _scoreLabels[i].setText(Integer.toString(_venobj.scores[i]));
        }
    }

    /**
     * Updates the count of remaining piecens for all of the players.
     */
    protected void updatePiecenCount ()
    {
        for (int i = 0; i < _venobj.scores.length; i++) {
            int pcount = TileUtil.countPiecens(_venobj.piecens, i);
            int pleft = VenisonCodes.PIECENS_PER_PLAYER - pcount;
            _piecenLabels[i].setText("(" + pleft + ")");
        }
    }

    /** A reference to the game object. */
    protected VenisonObject _venobj;

    /** References to our score label components. */
    protected JLabel[] _scoreLabels;

    /** References to our piecen label components. */
    protected JLabel[] _piecenLabels;
}
