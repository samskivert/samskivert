//
// $Id: AtlantiObject.java,v 1.7 2001/10/17 04:34:14 mdb Exp $

package com.threerings.venison;

import com.samskivert.util.StringUtil;
import com.threerings.presents.dobj.DSet;
import com.threerings.parlor.turn.TurnGameObject;

/**
 * The distributed object used to maintain state for the Venison game.
 */
public class VenisonObject extends TurnGameObject
{
    /** The field name of the <code>tiles</code> field. */
    public static final String TILES = "tiles";

    /** The field name of the <code>currentTile</code> field. */
    public static final String CURRENT_TILE = "currentTile";

    /** The field name of the <code>piecens</code> field. */
    public static final String PIECENS = "piecens";

    /** The field name of the <code>scores</code> field. */
    public static final String SCORES = "scores";

    /** A set containing all of the tiles that are in play in this
     * game. */
    public DSet tiles = new DSet(VenisonTile.class);

    /** The tile being placed by the current turn holder. This value is
     * only valid while it is someone's turn. */
    public VenisonTile currentTile = VenisonTile.STARTING_TILE;

    /** A set containing all of the piecens that are placed on the
     * board. */
    public DSet piecens = new DSet(Piecen.class);

    /** The scores for each player. */
    public int[] scores;

    /**
     * Requests that the <code>tiles</code> field be set to the specified
     * value.
     */
    public void setTiles (DSet value)
    {
        requestAttributeChange(TILES, value);
    }

    /**
     * Requests that the specified element be added to the
     * <code>tiles</code> set.
     */
    public void addToTiles (DSet.Element elem)
    {
        requestElementAdd(TILES, elem);
    }

    /**
     * Requests that the element matching the supplied key be removed from
     * the <code>tiles</code> set.
     */
    public void removeFromTiles (Object key)
    {
        requestElementRemove(TILES, key);
    }

    /**
     * Requests that the specified element be updated in the
     * <code>tiles</code> set.
     */
    public void updateTiles (DSet.Element elem)
    {
        requestElementUpdate(TILES, elem);
    }

    /**
     * Requests that the <code>currentTile</code> field be set to the
     * specified value.
     */
    public void setCurrentTile (VenisonTile value)
    {
        requestAttributeChange(CURRENT_TILE, value);
    }

    /**
     * Requests that the <code>piecens</code> field be set to the specified
     * value.
     */
    public void setPiecens (DSet value)
    {
        requestAttributeChange(PIECENS, value);
    }

    /**
     * Requests that the specified element be added to the
     * <code>piecens</code> set.
     */
    public void addToPiecens (DSet.Element elem)
    {
        requestElementAdd(PIECENS, elem);
    }

    /**
     * Requests that the element matching the supplied key be removed from
     * the <code>piecens</code> set.
     */
    public void removeFromPiecens (Object key)
    {
        requestElementRemove(PIECENS, key);
    }

    /**
     * Requests that the specified element be updated in the
     * <code>piecens</code> set.
     */
    public void updatePiecens (DSet.Element elem)
    {
        requestElementUpdate(PIECENS, elem);
    }

    /**
     * Requests that the <code>scores</code> field be set to the specified
     * value.
     */
    public void setScores (int[] value)
    {
        requestAttributeChange(SCORES, value);
    }

    // documentation inherited
    protected void toString (StringBuffer buf)
    {
        super.toString(buf);
        buf.append(", tiles=").append(tiles);
        buf.append(", currentTile=").append(currentTile);
        buf.append(", piecens=").append(piecens);
        buf.append(", scores=").append(StringUtil.toString(scores));
    }
}
