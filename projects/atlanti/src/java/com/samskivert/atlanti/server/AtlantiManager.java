//
// $Id: AtlantiManager.java,v 1.1 2001/10/09 20:27:35 mdb Exp $

package com.threerings.venison;

import com.threerings.parlor.server.GameManager;

/**
 * The main coordinator of the Venison game on the server side.
 */
public class VenisonManager extends GameManager
{
    // documentation inherited
    protected Class getPlaceObjectClass ()
    {
        return VenisonObject.class;
    }
}
