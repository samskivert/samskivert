//
// $Id: AtlantiConfig.java,v 1.1 2001/10/09 20:27:35 mdb Exp $

package com.threerings.venison;

import com.threerings.parlor.data.GameConfig;

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
