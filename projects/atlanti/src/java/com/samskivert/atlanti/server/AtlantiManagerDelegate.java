//
// $Id: AtlantiManagerDelegate.java,v 1.1 2002/05/21 04:45:10 mdb Exp $

package com.threerings.venison;

import com.threerings.parlor.turn.TurnGameManagerDelegate;

/**
 * Handles the turn-based gameplay.
 */
public class VenisonManagerDelegate extends TurnGameManagerDelegate
    implements VenisonCodes
{
    /**
     * Constructs the delegate and prepares it for operation.
     */
    public VenisonManagerDelegate (VenisonManager vmgr)
    {
        super(vmgr);
        _vmgr = vmgr;
    }

    /**
     * Continue the game until we're out of tiles.
     */
    protected void setNextTurnHolder ()
    {
        // if we have tiles left, we move to the next player as normal
        if (_vmgr.getTilesInBox() > 0) {
            super.setNextTurnHolder();
        } else {
            // if we don't, we ensure that a new turn isn't started by
            // setting _turnIdx to -1
            _turnIdx = -1;
        }
    }

    /** The manager for whom we're delegating. */
    protected VenisonManager _vmgr;
}
