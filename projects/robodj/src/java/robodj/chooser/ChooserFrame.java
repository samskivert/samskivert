//
// $Id: ChooserFrame.java,v 1.10 2002/11/11 17:04:41 mdb Exp $

package robodj.chooser;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import com.samskivert.swing.*;
import com.samskivert.swing.util.TaskAdapter;
import com.samskivert.swing.util.TaskObserver;
import com.samskivert.swing.util.TaskMaster;

import robodj.Log;
import robodj.Version;
import robodj.repository.*;
import robodj.util.ButtonUtil;
import robodj.util.ServerControl.PlayingListener;

public class ChooserFrame extends JFrame
    implements ActionListener, TaskObserver, PlayingListener
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
        cbar.add(ButtonUtil.createControlButton(
                     BACK_TIP, "back", BACK_ICON_PATH, this, true));
        _stop = ButtonUtil.createControlButton(
            STOP_TIP, "stop", STOP_ICON_PATH, this, true);
        cbar.add(_stop);
        _pauseIcon = ButtonUtil.getIcon(PAUSE_ICON_PATH);
        _playIcon = ButtonUtil.getIcon(PLAY_ICON_PATH);
        _pause = ButtonUtil.createControlButton(
            "", "pause", _pauseIcon, this, true);
        cbar.add(_pause);
        cbar.add(ButtonUtil.createControlButton(
                     SKIP_TIP, "skip", SKIP_ICON_PATH, this, true));
        cbar.add(new Spacer(50, 10));
        cbar.add(ButtonUtil.createControlButton(
                     EXIT_TIP, "exit", EXIT_ICON_PATH, this, true));

	// stick it into the frame
	top.add(cbar, GroupLayout.FIXED);

	// now add our top-level panel (we'd not use this if we could set
	// a border on the content pane returned by the frame... alas)
	getContentPane().add(top, BorderLayout.CENTER);

        // add ourselves as a playing listener
        Chooser.scontrol.addPlayingListener(this);

        // read our playing state
        TaskMaster.invokeMethodTask("refreshPlaying", Chooser.scontrol, this);
    }

    public void actionPerformed (ActionEvent e)
    {
	String cmd = e.getActionCommand();
	if (cmd.equals("exit")) {
	    System.exit(0);

        } else if (cmd.equals("skip")) {
            TaskMaster.invokeMethodTask("skip", Chooser.scontrol, this);

        } else if (cmd.equals("back")) {
            TaskMaster.invokeMethodTask("back", Chooser.scontrol, this);

        } else if (cmd.equals("pause")) {
            TaskMaster.invokeMethodTask("pause", Chooser.scontrol, this);

        } else if (cmd.equals("play")) {
            TaskMaster.invokeMethodTask("play", Chooser.scontrol, this);

        } else if (cmd.equals("stop")) {
            TaskMaster.invokeMethodTask("stop", Chooser.scontrol, this);

	} else {
	    System.out.println("Unknown action event: " + cmd);
	}
    }

    public void taskCompleted (String name, Object result)
    {
        // nothing to do here
    }

    public void taskFailed (String name, Throwable exception)
    {
        String msg;
        if (Exception.class.equals(exception.getClass())) {
            msg = exception.getMessage();
        } else {
            msg = exception.toString();
        }
        JOptionPane.showMessageDialog(this, msg, "Error",
                                      JOptionPane.ERROR_MESSAGE); 
        Log.logStackTrace(exception);
    }

    public void playingUpdated (int songid, boolean paused)
    {
        if (songid == -1 || paused) {
            _pause.setToolTipText(PLAY_TIP);
            _pause.setActionCommand("play");
            _pause.setIcon(_playIcon);

        } else {
            _pause.setToolTipText(PAUSE_TIP);
            _pause.setActionCommand("pause");
            _pause.setIcon(_pauseIcon);
        }

        _stop.setEnabled(songid != -1);
    }

    /** A reference to the play/pause button. */
    protected JButton _pause;

    /** A reference to the stop button. */
    protected JButton _stop;

    // used when toggling between play and pause
    protected ImageIcon _playIcon;
    protected ImageIcon _pauseIcon;

    // icon paths
    protected static final String ICON_ROOT = "/robodj/chooser/images/";
    protected static final String PLAY_ICON_PATH = ICON_ROOT + "play.png";
    protected static final String PAUSE_ICON_PATH = ICON_ROOT + "pause.png";
    protected static final String STOP_ICON_PATH = ICON_ROOT + "stop.png";
    protected static final String SKIP_ICON_PATH = ICON_ROOT + "skip.png";
    protected static final String BACK_ICON_PATH = ICON_ROOT + "back.png";
    protected static final String EXIT_ICON_PATH = ICON_ROOT + "exit.png";

    // button tips
    protected static final String PLAY_TIP = "Play";
    protected static final String PAUSE_TIP =
        "Pause the currently playing song";
    protected static final String STOP_TIP = "Stop the currently playing song";
    protected static final String SKIP_TIP = "Skip to the next song";
    protected static final String BACK_TIP = "Skip to the previous song";
    protected static final String EXIT_TIP = "Exit";
}
