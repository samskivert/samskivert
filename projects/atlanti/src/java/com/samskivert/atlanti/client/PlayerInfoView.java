//
// $Id: PlayerInfoView.java,v 1.1 2001/10/17 23:27:52 mdb Exp $

package com.threerings.venison;

import java.awt.Color;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

import javax.swing.JLabel;
import javax.swing.JPanel;

import com.threerings.presents.dobj.AttributeChangeListener;
import com.threerings.presents.dobj.AttributeChangedEvent;

import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.data.PlaceObject;

/**
 * Displays each of the players in the game, their piece color and their
 * score.
 */
public class PlayerInfoView
    extends JPanel implements PlaceView, AttributeChangeListener
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

        // we need a score labels array so that we can keep track of the
        // score labels
        _scoreLabels = new JLabel[_venobj.players.length];

        // now that we're here, we can add an entry for every player
        for (int i = 0; i < _venobj.players.length; i++) {
            addPlayer(i, _venobj.players[i]);
        }

        // if we have scores, update them
        if (_venobj.scores.length == _venobj.players.length) {
            updateScores();
        }
    }

    /**
     * Adds a player to the display.
     */
    protected void addPlayer (int idx, String username)
    {
        GridBagConstraints c = new GridBagConstraints();

        // create a label for their username
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        JLabel unlabel = new JLabel(username);
        unlabel.setForeground(Feature.PIECEN_COLOR_MAP[idx]);
        add(unlabel, c);

        // create a label for their score
        c.weightx = 0.0;
        c.fill = GridBagConstraints.NONE;
        c.gridwidth = GridBagConstraints.REMAINDER;
        _scoreLabels[idx] = new JLabel("0");
        _scoreLabels[idx].setForeground(Color.black);
        add(_scoreLabels[idx], c);
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

    /**
     * Reads the scores from the game object and updates the view.
     */
    protected void updateScores ()
    {
        for (int i = 0; i < _venobj.scores.length; i++) {
            _scoreLabels[i].setText(Integer.toString(_venobj.scores[i]));
        }
    }

    /** A reference to the game object. */
    protected VenisonObject _venobj;

    /** References to our score label components. */
    protected JLabel[] _scoreLabels;
}
