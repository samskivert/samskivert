//
// $Id: AtlantiManagerDelegate.java,v 1.2 2002/12/12 05:51:54 mdb Exp $

package com.samskivert.atlanti.server;

import com.threerings.parlor.turn.TurnGameManagerDelegate;

import com.samskivert.atlanti.data.AtlantiCodes;

/**
 * Handles the turn-based gameplay.
 */
public class AtlantiManagerDelegate extends TurnGameManagerDelegate
    implements AtlantiCodes
{
    /**
     * Constructs the delegate and prepares it for operation.
     */
    public AtlantiManagerDelegate (AtlantiManager vmgr)
    {
        super(vmgr);
        _amgr = vmgr;
    }

    /**
     * Continue the game until we're out of tiles.
     */
    protected void setNextTurnHolder ()
    {
        // if we have tiles left, we move to the next player as normal
        if (_amgr.getTilesInBox() > 0) {
            super.setNextTurnHolder();
        } else {
            // if we don't, we ensure that a new turn isn't started by
            // setting _turnIdx to -1
            _turnIdx = -1;
        }
    }

    /** The manager for whom we're delegating. */
    protected AtlantiManager _amgr;
}
