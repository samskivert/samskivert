//
// $Id: AtlantiConfig.java,v 1.4 2002/12/12 05:51:54 mdb Exp $

package com.samskivert.atlanti.data;

import java.io.IOException;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

import com.threerings.parlor.data.TableConfig;
import com.threerings.parlor.game.GameConfig;

import com.samskivert.atlanti.client.AtlantiConfigurator;
import com.samskivert.atlanti.client.AtlantiController;

/**
 * Describes the configuration parameters for the game.
 */
public class AtlantiConfig extends GameConfig
    implements TableConfig
{
    // documentation inherited
    public Class getControllerClass ()
    {
        return AtlantiController.class;
    }

    // documentation inherited from interface
    public Class getConfiguratorClass ()
    {
        return AtlantiConfigurator.class;
    }

    // documentation inherited
    public String getManagerClassName ()
    {
        return "com.samskivert.atlanti.server.AtlantiManager";
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
    public void writeObject (ObjectOutputStream out)
        throws IOException
    {
        out.defaultWriteObject();
        out.writeInt(_desiredPlayers);
    }

    // documentation inherited
    public void readObject (ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        _desiredPlayers = in.readInt();
    }

    /** The desired number of players for this game or -1 if there is no
     * specific desired number of players. */
    protected int _desiredPlayers;
}
