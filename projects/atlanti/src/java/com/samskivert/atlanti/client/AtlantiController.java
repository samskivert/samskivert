//
// $Id

package com.threerings.venison;

import com.threerings.crowd.client.PlaceView;
import com.threerings.parlor.client.GameController;
import com.threerings.parlor.util.ParlorContext;

import com.threerings.venison.Log;

/**
 * The main coordinator of user interface activities on the client-side of
 * the Venison game.
 */
public class VenisonController extends GameController
{
    protected PlaceView createPlaceView ()
    {
        return new VenisonPanel(_ctx);
    }

    protected void gameDidStart ()
    {
        super.gameDidStart();

        Log.info("Venison game did start.");
    }
}
