//
// $Id

package com.threerings.venison;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import com.samskivert.swing.Controller;
import com.samskivert.swing.ControllerProvider;
import com.samskivert.swing.HGroupLayout;
import com.samskivert.swing.VGroupLayout;

import com.threerings.resource.ResourceManager;
import com.threerings.media.tile.TileManager;

import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.client.PlaceView;

import com.threerings.parlor.util.ParlorContext;
import com.threerings.micasa.client.ChatPanel;

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

        // add a big fat label because we love it!
        JLabel vlabel = new JLabel("Venison!");
        vlabel.setFont(new Font("Helvetica", Font.BOLD, 24));
        vlabel.setForeground(Color.black);
        sidePanel.add(vlabel, VGroupLayout.FIXED);

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

        // add a chat box
        ChatPanel chat = new ChatPanel(ctx);
        chat.removeSendButton();
        sidePanel.add(chat);

        // add a "back" button
        JButton back = new JButton("Back to lobby");
        back.setActionCommand(BACK_TO_LOBBY);
        back.addActionListener(Controller.DISPATCHER);
        sidePanel.add(back, VGroupLayout.FIXED);

        // add our side panel to the main display
        add(sidePanel, HGroupLayout.FIXED);

        // we'll need this later to provide it
        _controller = controller;

        // we can't create our image manager until we have access to our
        // containing frame
        addAncestorListener(new AncestorListener() {
            public void ancestorAdded (AncestorEvent event) {
                // create our image manager
                JRootPane rpane = getRootPane();
                TileManager tmgr = new TileManager(_rmgr);
                VenisonTile.setTileManager(tmgr);
            }
            public void ancestorMoved (AncestorEvent event) {
            }
            public void ancestorRemoved (AncestorEvent event) {
            }
        });
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

    // this stuff is all a bit of a hack right now. by all rights, the
    // venison app should set up the resource manager, because it knows
    // about that sort of stuff, and make it available via the venison
    // context and we may have some better place for the tile manager to
    // live. but it's late and i want to get this working, so fooey.

    /** Our resource manager. */
    protected static ResourceManager _rmgr = new ResourceManager(null, "rsrc");
}
