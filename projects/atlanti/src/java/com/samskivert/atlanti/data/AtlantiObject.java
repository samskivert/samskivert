//
// $Id: AtlantiObject.java,v 1.2 2001/10/10 03:35:02 mdb Exp $

package com.threerings.venison;

import com.threerings.cocktail.cher.dobj.DSet;

import com.threerings.parlor.data.GameObject;

/**
 * The distributed object used to maintain state for the Venison game.
 */
public class VenisonObject extends GameObject
{
    /** The field name of the <code>tiles</code> field. */
    public static final String TILES = "tiles";

    /** A set containing all of the tiles that are in play in this
     * game. */
    public DSet tiles = new DSet(VenisonTile.class);

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

    // documentation inherited
    protected void toString (StringBuffer buf)
    {
        super.toString(buf);
        buf.append(", tiles=").append(tiles);
    }
}
