//
// $Id

package com.threerings.venison;

import java.awt.BorderLayout;
import javax.swing.*;

import com.samskivert.swing.Controller;
import com.samskivert.swing.ControllerProvider;
import com.samskivert.swing.HGroupLayout;
import com.samskivert.swing.VGroupLayout;

import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.client.PlaceView;

import com.threerings.parlor.util.ParlorContext;

/**
 * The top-level user interface component for the Venison game display.
 */
public class VenisonPanel
    extends JPanel implements PlaceView, ControllerProvider, VenisonCodes
{
    /** A reference to the board that is accessible to the controller. */
    public VenisonBoard board;

    /** A reference to our _noplace button. */
    public JButton noplace;

    /**
     * Constructs a new Venison game display.
     */
    public VenisonPanel (ParlorContext ctx, VenisonController controller)
    {
	// give ourselves a wee bit of a border
	setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

	HGroupLayout gl = new HGroupLayout(HGroupLayout.STRETCH);
	gl.setOffAxisPolicy(HGroupLayout.STRETCH);
	setLayout(gl);

        // create the board
        board = new VenisonBoard();

        // create a scroll area to contain the board
        JScrollPane scrolly = new JScrollPane(board);
        add(scrolly);

        // create our side panel
        VGroupLayout sgl = new VGroupLayout(VGroupLayout.STRETCH);
        sgl.setOffAxisPolicy(VGroupLayout.STRETCH);
        sgl.setJustification(VGroupLayout.TOP);
        JPanel sidePanel = new JPanel(sgl);

        // add a player info view to the side panel
        sidePanel.add(new JLabel("Scores:"), VGroupLayout.FIXED);
        sidePanel.add(new PlayerInfoView(), VGroupLayout.FIXED);

        // add a turn indicator to the side panel
        sidePanel.add(new JLabel("Current turn:"), VGroupLayout.FIXED);
        sidePanel.add(new TurnIndicatorView(), VGroupLayout.FIXED);

        // add a "place nothing" button
        noplace = new JButton("Place nothing");
        noplace.setEnabled(false);
        noplace.setActionCommand(PLACE_NOTHING);
        noplace.addActionListener(Controller.DISPATCHER);
        sidePanel.add(noplace, VGroupLayout.FIXED);

        // add our side panel to the main display
        add(sidePanel, HGroupLayout.FIXED);

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
