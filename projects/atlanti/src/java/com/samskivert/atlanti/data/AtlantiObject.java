//
// $Id: AtlantiObject.java,v 1.10 2003/03/23 02:22:51 mdb Exp $

package com.samskivert.atlanti.data;

import com.threerings.presents.dobj.DSet;
import com.threerings.util.Name;

import com.threerings.parlor.game.GameObject;
import com.threerings.parlor.turn.TurnGameObject;

/**
 * The distributed object used to maintain state for the game.
 */
public class AtlantiObject extends GameObject
    implements TurnGameObject
{
    /** The field name of the <code>turnHolder</code> field. */
    public static final String TURN_HOLDER = "turnHolder";

    /** The field name of the <code>tiles</code> field. */
    public static final String TILES = "tiles";

    /** The field name of the <code>currentTile</code> field. */
    public static final String CURRENT_TILE = "currentTile";

    /** The field name of the <code>piecens</code> field. */
    public static final String PIECENS = "piecens";

    /** The field name of the <code>scores</code> field. */
    public static final String SCORES = "scores";

    /** The username of the current turn holder. */
    public Name turnHolder;

    /** A set containing all of the tiles that are in play in this
     * game. */
    public DSet tiles = new DSet();

    /** The tile being placed by the current turn holder. This value is
     * only valid while it is someone's turn. */
    public AtlantiTile currentTile = AtlantiTile.STARTING_TILE;

    /** A set containing all of the piecens that are placed on the
     * board. */
    public DSet piecens = new DSet();

    /** The scores for each player. */
    public int[] scores;

    // documentation inherited from interface
    public Name[] getPlayers ()
    {
        return players;
    }

    // documentation inherited from interface
    public String getTurnHolderFieldName ()
    {
        return TURN_HOLDER;
    }

    // documentation inherited from interface
    public Name getTurnHolder ()
    {
        return turnHolder;
    }

    /**
     * Requests that the <code>turnHolder</code> field be set to the specified
     * value. The local value will be updated immediately and an event
     * will be propagated through the system to notify all listeners that
     * the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setTurnHolder (Name turnHolder)
    {
        requestAttributeChange(TURN_HOLDER, turnHolder);
        this.turnHolder = turnHolder;
    }

    /**
     * Requests that the specified entry be added to the
     * <code>tiles</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    public void addToTiles (DSet.Entry elem)
    {
        requestEntryAdd(TILES, elem);
    }

    /**
     * Requests that the entry matching the supplied key be removed from
     * the <code>tiles</code> set. The set will not change until the
     * event is actually propagated through the system.
     */
    public void removeFromTiles (Comparable key)
    {
        requestEntryRemove(TILES, key);
    }

    /**
     * Requests that the specified entry be updated in the
     * <code>tiles</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    public void updateTiles (DSet.Entry elem)
    {
        requestEntryUpdate(TILES, elem);
    }

    /**
     * Requests that the <code>tiles</code> field be set to the
     * specified value. Generally one only adds, updates and removes
     * entries of a distributed set, but certain situations call for a
     * complete replacement of the set value. The local value will be
     * updated immediately and an event will be propagated through the
     * system to notify all listeners that the attribute did
     * change. Proxied copies of this object (on clients) will apply the
     * value change when they received the attribute changed notification.
     */
    public void setTiles (DSet tiles)
    {
        requestAttributeChange(TILES, tiles);
        this.tiles = tiles;
    }

    /**
     * Requests that the <code>currentTile</code> field be set to the specified
     * value. The local value will be updated immediately and an event
     * will be propagated through the system to notify all listeners that
     * the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setCurrentTile (AtlantiTile currentTile)
    {
        requestAttributeChange(CURRENT_TILE, currentTile);
        this.currentTile = currentTile;
    }

    /**
     * Requests that the specified entry be added to the
     * <code>piecens</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    public void addToPiecens (DSet.Entry elem)
    {
        requestEntryAdd(PIECENS, elem);
    }

    /**
     * Requests that the entry matching the supplied key be removed from
     * the <code>piecens</code> set. The set will not change until the
     * event is actually propagated through the system.
     */
    public void removeFromPiecens (Comparable key)
    {
        requestEntryRemove(PIECENS, key);
    }

    /**
     * Requests that the specified entry be updated in the
     * <code>piecens</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    public void updatePiecens (DSet.Entry elem)
    {
        requestEntryUpdate(PIECENS, elem);
    }

    /**
     * Requests that the <code>piecens</code> field be set to the
     * specified value. Generally one only adds, updates and removes
     * entries of a distributed set, but certain situations call for a
     * complete replacement of the set value. The local value will be
     * updated immediately and an event will be propagated through the
     * system to notify all listeners that the attribute did
     * change. Proxied copies of this object (on clients) will apply the
     * value change when they received the attribute changed notification.
     */
    public void setPiecens (DSet piecens)
    {
        requestAttributeChange(PIECENS, piecens);
        this.piecens = piecens;
    }

    /**
     * Requests that the <code>scores</code> field be set to the specified
     * value. The local value will be updated immediately and an event
     * will be propagated through the system to notify all listeners that
     * the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setScores (int[] scores)
    {
        requestAttributeChange(SCORES, scores);
        this.scores = scores;
    }

    /**
     * Requests that the <code>index</code>th element of
     * <code>scores</code> field be set to the specified value. The local
     * value will be updated immediately and an event will be propagated
     * through the system to notify all listeners that the attribute did
     * change. Proxied copies of this object (on clients) will apply the
     * value change when they received the attribute changed notification.
     */
    public void setScoresAt (int value, int index)
    {
        requestElementUpdate(SCORES, new Integer(value), index);
        this.scores[index] = value;
    }
}
