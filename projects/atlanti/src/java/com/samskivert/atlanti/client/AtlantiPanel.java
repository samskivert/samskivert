//
// $Id

package com.threerings.venison;

import java.awt.BorderLayout;
import javax.swing.*;

import com.samskivert.swing.Controller;
import com.samskivert.swing.ControllerProvider;

import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.client.PlaceView;

import com.threerings.parlor.util.ParlorContext;

/**
 * The top-level user interface component for the Venison game display.
 */
public class VenisonPanel
    extends JPanel implements PlaceView, ControllerProvider
{
    /** A reference to the board that is accessible to the controller. */
    public VenisonBoard board;

    /**
     * Constructs a new Venison game display.
     */
    public VenisonPanel (ParlorContext ctx, VenisonController controller)
    {
        // add the board
        board = new VenisonBoard();
        add(board, BorderLayout.CENTER);

        // we'll need this later to provide it
        _controller = controller;
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

    // documentation inherited
    public Controller getController ()
    {
        return _controller;
    }

    /** A reference to our controller that we need to implement the {@link
     * ControllerProvider} interface. */
    protected VenisonController _controller;
}
