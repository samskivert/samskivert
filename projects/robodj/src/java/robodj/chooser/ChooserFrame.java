//
// $Id: ChooserFrame.java,v 1.7 2002/03/03 06:32:12 mdb Exp $

package robodj.chooser;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

import com.samskivert.swing.*;

import robodj.Version;
import robodj.repository.*;

public class ChooserFrame
    extends JFrame
    implements ActionListener
{
    public ChooserFrame ()
    {
	super("RoboDJ Chooser " + Version.RELEASE_VERSION);

        // quit if we're closed
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // we create a top-level panel to manage everything
	JPanel top = new JPanel();
	GroupLayout gl = new VGroupLayout(GroupLayout.STRETCH);
	gl.setOffAxisPolicy(GroupLayout.STRETCH);
	top.setLayout(gl);

	// give ourselves a wee bit of a border
	top.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // the top of the UI is the browser and the playlist manager
        JTabbedPane tpane = new JTabbedPane();
        PlaylistPanel ppanel = new PlaylistPanel();
        String tip = "View and manipulate the playlist.";
        tpane.addTab("Playlist", null, ppanel, tip);

        BrowsePanel bpanel = new BrowsePanel();
        tip = "Browse and select tunes to play.";
        tpane.addTab("Browse", null, bpanel, tip);

        SearchPanel spanel = new SearchPanel();
        tip = "Search titles and artists.";
        tpane.addTab("Search", null, spanel, tip);
        top.add(tpane);

        // the bottom is the control bar
        GroupLayout bgl = new HGroupLayout(GroupLayout.NONE);
        bgl.setJustification(GroupLayout.RIGHT);
        JPanel cbar = new JPanel(bgl);

        // add some fake control buttons for now
        cbar.add(_pause = createControlButton("Pause", "pause"));
        cbar.add(createControlButton("Stop", "stop"));
        cbar.add(createControlButton("Exit", "exit"));

	// stick it into the frame
	top.add(cbar, GroupLayout.FIXED);

	// now add our top-level panel (we'd not use this if we could set
	// a border on the content pane returned by the frame... alas)
	getContentPane().add(top, BorderLayout.CENTER);
    }

    protected JButton createControlButton (String label, String action)
    {
        JButton cbut = new JButton(label);
        cbut.setActionCommand(action);
        cbut.addActionListener(this);
        return cbut;
    }

    public void actionPerformed (ActionEvent e)
    {
	String cmd = e.getActionCommand();
	if (cmd.equals("exit")) {
	    System.exit(0);

        } else if (cmd.equals("pause")) {
            Chooser.scontrol.pause();
            _pause.setLabel("Play");
            _pause.setActionCommand("play");

        } else if (cmd.equals("play")) {
            Chooser.scontrol.play();
            _pause.setLabel("Pause");
            _pause.setActionCommand("Pause");

        } else if (cmd.equals("clear")) {
            Chooser.scontrol.clear();

        } else if (cmd.equals("stop")) {
            Chooser.scontrol.stop();

        } else if (cmd.equals("skip")) {
            Chooser.scontrol.skip();

	} else {
	    System.out.println("Unknown action event: " + cmd);
	}
    }

    protected JButton _pause;
}
