//
// $Id: AtlantiConfig.java,v 1.3 2001/10/24 03:24:53 mdb Exp $

package com.threerings.venison;

import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import com.threerings.parlor.game.GameConfig;
import com.threerings.parlor.data.TableConfig;

/**
 * Describes the configuration parameters for a game of Venison.
 */
public class VenisonConfig
    extends GameConfig implements TableConfig
{
    // documentation inherited
    public Class getControllerClass ()
    {
        return VenisonController.class;
    }

    // documentation inherited
    public String getManagerClassName ()
    {
        return "com.threerings.venison.VenisonManager";
    }

    // documentation inherited
    public int getMinimumPlayers ()
    {
        return 2;
    }

    // documentation inherited
    public int getMaximumPlayers ()
    {
        return 5;
    }

    // documentation inherited
    public int getDesiredPlayers ()
    {
        return _desiredPlayers;
    }

    // documentation inherited
    public void setDesiredPlayers (int desiredPlayers)
    {
        _desiredPlayers = desiredPlayers;
    }

    // documentation inherited
    public void writeTo (DataOutputStream out)
        throws IOException
    {
        super.writeTo(out);
        out.writeInt(_desiredPlayers);
    }

    // documentation inherited
    public void readFrom (DataInputStream in)
        throws IOException
    {
        super.readFrom(in);
        _desiredPlayers = in.readInt();
    }

    /** The desired number of players for this game or -1 if there is no
     * specific desired number of players. */
    protected int _desiredPlayers;
}
