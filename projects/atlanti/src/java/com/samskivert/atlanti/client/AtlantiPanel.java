//
// $Id

package com.threerings.venison;

import java.awt.BorderLayout;
import javax.swing.*;

import com.threerings.cocktail.party.data.PlaceObject;
import com.threerings.cocktail.party.client.PlaceView;

import com.threerings.parlor.util.ParlorContext;

/**
 * The top-level user interface component for the Venison game display.
 */
public class VenisonPanel
    extends JPanel implements PlaceView
{
    /**
     * Constructs a new Venison game display.
     */
    public VenisonPanel (ParlorContext ctx)
    {
        add(new JLabel("Venison panel"), BorderLayout.CENTER);
    }

    // documentation inherited
    public void willEnterPlace (PlaceObject plobj)
    {
        Log.info("Panel entered place.");
    }

    // documentation inherited
    public void didLeavePlace (PlaceObject plobj)
    {
        Log.info("Panel left place.");
    }
}
