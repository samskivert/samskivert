//
// $Id: AtlantiManager.java,v 1.3 2001/10/10 06:14:57 mdb Exp $

package com.threerings.venison;

import com.threerings.cocktail.cher.dobj.DSet;
import com.threerings.parlor.server.GameManager;

/**
 * The main coordinator of the Venison game on the server side.
 */
public class VenisonManager
    extends GameManager
{
    // documentation inherited
    protected Class getPlaceObjectClass ()
    {
        return VenisonObject.class;
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

        // clear out the tile set
        _venobj.setTiles(new DSet(VenisonTile.class));
        _venobj.addToTiles(TileUtil.STARTING_TILE);
    }

    protected VenisonObject _venobj;
}
