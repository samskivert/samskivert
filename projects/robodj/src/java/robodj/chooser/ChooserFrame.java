//
// $Id: ChooserFrame.java,v 1.13 2004/01/26 16:35:27 mdb Exp $

package robodj.chooser;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import com.samskivert.swing.*;
import com.samskivert.swing.util.*;
import com.samskivert.util.StringUtil;

import robodj.Log;
import robodj.Version;
import robodj.repository.*;
import robodj.util.ButtonUtil;
import robodj.util.FancyPanel;
import robodj.util.RDJPrefs;
import robodj.util.ServerControl.PlayingListener;

public class ChooserFrame extends JFrame
    implements PlayingListener, ControllerProvider
{
    public ChooserFrame ()
    {
	super("RoboDJ Chooser " + Version.RELEASE_VERSION);

        // quit if we're closed
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // we create a top-level panel to manage everything
	GroupLayout gl = new VGroupLayout(GroupLayout.STRETCH);
	gl.setOffAxisPolicy(GroupLayout.STRETCH);
	JPanel top = new FancyPanel(gl, ButtonUtil.getImage(BACKGROUND_PATH));
	top.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // the top of the UI is the browser and the playlist manager
        JTabbedPane tpane = new JTabbedPane();
        tpane.setBackground(BGCOLOR);
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

        // create a label and text field where the chooser user can
        // identify themselves
        cbar.add(new JLabel("Initials:"));
        cbar.add(_userField = new JTextField());
        Dimension ups = _userField.getPreferredSize();
        ups.width = 35;
        _userField.setPreferredSize(ups);
        cbar.add(new Spacer(100, 10));

        // enforce a maximum of 3 letters in initials
        SwingUtil.setDocumentHelpers(
            _userField, new SwingUtil.DocumentValidator() {
                public boolean isValid (String text) {
                    return (text.length() <= 3);
                }
            }, null);

        // display any previously configured initials
        _userField.setText(RDJPrefs.getUser());

        // add some fake control buttons for now
        cbar.add(ButtonUtil.createControlButton(
                     BACK_TIP, "back", BACK_ICON_PATH, true));
        _stop = ButtonUtil.createControlButton(
            STOP_TIP, "stop", STOP_ICON_PATH, true);
        cbar.add(_stop);
        _pauseIcon = ButtonUtil.getIcon(PAUSE_ICON_PATH);
        _playIcon = ButtonUtil.getIcon(PLAY_ICON_PATH);
        _pause = ButtonUtil.createControlButton(
            "", "pause", _pauseIcon, true);
        cbar.add(_pause);
        cbar.add(ButtonUtil.createControlButton(
                     SKIP_TIP, "skip", SKIP_ICON_PATH, true));
        cbar.add(new Spacer(50, 10));
        cbar.add(ButtonUtil.createControlButton(
                     EXIT_TIP, "exit", EXIT_ICON_PATH, true));

	// stick it into the frame
	top.add(cbar, GroupLayout.FIXED);

	// now add our top-level panel (we'd not use this if we could set
	// a border on the content pane returned by the frame... alas)
	getContentPane().add(top, BorderLayout.CENTER);
        SwingUtil.applyToHierarchy(top, new SwingUtil.ComponentOp() {
            public void apply (Component comp) {
                if (comp instanceof JPanel || comp instanceof JButton) {
                    ((JComponent) comp).setOpaque(false);
                }
            }
        });

        // add ourselves as a playing listener
        Chooser.scontrol.addPlayingListener(this);

        // create our controller
        _controller = new ChooserController();
    }

    // documentation inherited from interface
    public Controller getController ()
    {
        return _controller;
    }

    /**
     * Returns the string currently entered into the "user" field.
     */
    public String getUser (boolean showError)
    {
        String user = _userField.getText();
        // if they haven't supplied a user, complain
        if (StringUtil.blank(user)) {
            if (!showError) {
                return null;
            }
            String errmsg = "The feature you have requested requires " +
                "that you identify yourself by entering your initials in " +
                "the 'Initials' box at the bottom of the window.";
            JOptionPane.showMessageDialog(this, errmsg, "Initials required",
                                          JOptionPane.ERROR_MESSAGE);
            return null;
        }

        // if they typed in mixed or upper case, lower it for them
        user = user.toLowerCase();
        if (!user.equals(_userField.getText())) {
            _userField.setText(user);
        }

        // make sure we've saved these initials values
        RDJPrefs.config.setValue(RDJPrefs.USER_KEY, user);
        return user;
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

    /** Our top-level controller. */
    protected Controller _controller;

    /** A field where the user can identify themselves. */
    protected JTextField _userField;

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

    protected static final String BACKGROUND_PATH =
        ICON_ROOT + "background.png";

    // button tips
    protected static final String PLAY_TIP = "Play";
    protected static final String PAUSE_TIP =
        "Pause the currently playing song";
    protected static final String STOP_TIP = "Stop the currently playing song";
    protected static final String SKIP_TIP = "Skip to the next song";
    protected static final String BACK_TIP = "Skip to the previous song";
    protected static final String EXIT_TIP = "Exit";

    // our common background color
    protected static final Color BGCOLOR = new Color(0x9999CC);
}
