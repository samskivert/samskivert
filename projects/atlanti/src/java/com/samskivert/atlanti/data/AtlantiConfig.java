//
// $Id: AtlantiConfig.java,v 1.2 2001/10/12 20:34:13 mdb Exp $

package com.threerings.venison;

import com.threerings.parlor.game.GameConfig;

public class VenisonConfig extends GameConfig
{
    public Class getControllerClass ()
    {
        return VenisonController.class;
    }

    public String getManagerClassName ()
    {
        return "com.threerings.venison.VenisonManager";
    }
}
