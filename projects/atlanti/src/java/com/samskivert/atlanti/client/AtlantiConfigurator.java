//
// $Id: AtlantiConfigurator.java,v 1.1 2002/12/12 05:51:53 mdb Exp $

package com.samskivert.atlanti.client;

import com.samskivert.swing.SimpleSlider;
import com.samskivert.swing.VGroupLayout;

import com.threerings.micasa.lobby.table.TableGameConfigurator;

import com.samskivert.atlanti.data.AtlantiConfig;

/**
 * Provides a user interface for configuring a game.
 */
public class AtlantiConfigurator extends TableGameConfigurator
{
    // documentation inherited
    protected void createConfigInterface ()
    {
        super.createConfigInterface();

//         _boardSize = new SimpleSlider("Board size:", 16, 48, 32);
//         add(_boardSize, VGroupLayout.FIXED);
//         _handSize = new SimpleSlider("Hand size:", 2, 5, 5);
//         add(_handSize, VGroupLayout.FIXED);
    }

    // documentation inherited
    protected void gotGameConfig ()
    {
        super.gotGameConfig();
        _vconfig = (AtlantiConfig)_config;

//         // configure our elements
//         _boardSize.setValue(_vconfig.boardWidth);
//         _handSize.setValue(_vconfig.handSize);
    }

    // documentation inherited
    protected void flushGameConfig ()
    {
        super.flushGameConfig();

//         // configure our elements
//         _vconfig.boardWidth = _boardSize.getValue();
//         _vconfig.boardHeight = _boardSize.getValue();
//         _vconfig.handSize = _handSize.getValue();
    }

    protected AtlantiConfig _vconfig;
//     protected SimpleSlider _boardSize;
//     protected SimpleSlider _handSize;
}
