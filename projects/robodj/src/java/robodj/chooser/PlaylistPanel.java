//
// $Id: PlaylistPanel.java,v 1.2 2001/07/13 00:11:05 mdb Exp $

package robodj.chooser;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.swing.*;

import com.samskivert.swing.*;
import com.samskivert.swing.util.*;
import com.samskivert.util.StringUtil;

import robodj.Log;
import robodj.repository.*;

public class PlaylistPanel
    extends JPanel
    implements TaskObserver, ActionListener
{
    public PlaylistPanel ()
    {
	GroupLayout gl = new VGroupLayout(GroupLayout.STRETCH);
        gl.setOffAxisPolicy(GroupLayout.STRETCH);
	setLayout(gl);

        // create the pane that will hold the buttons
        gl = new VGroupLayout(GroupLayout.NONE);
        gl.setJustification(GroupLayout.TOP);
        _bpanel = new JPanel(gl);

	// give ourselves a wee bit of a border
	_bpanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // put it into a scrolling pane
	JScrollPane bscroll = new JScrollPane(_bpanel);
        add(bscroll);

        GroupLayout bgl = new HGroupLayout(GroupLayout.NONE);
        bgl.setJustification(GroupLayout.RIGHT);
        JPanel cbar = new JPanel(bgl);

        // add our control buttons
        cbar.add(_clearbut = createControlButton("Clear", "clear"));
        cbar.add(createControlButton("Skip", "skip"));
        cbar.add(createControlButton("Refresh", "refresh"));
        add(cbar, GroupLayout.FIXED);

        // use a special font for our name buttons
        _nameFont = new Font("Helvetica", Font.PLAIN, 10);

        // load up the playlist
        refreshPlaylist();
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
	if (cmd.equals("...")) {

        } else if (cmd.equals("clear")) {
            Chooser.scontrol.clear();

        } else if (cmd.equals("skip")) {
            Chooser.scontrol.skip();

        } else if (cmd.equals("refresh")) {
            refreshPlaylist();

        } else if (cmd.equals("skipto")) {
            JButton src = (JButton)e.getSource();
            PlaylistEntry entry =
                (PlaylistEntry)src.getClientProperty("entry");
            Chooser.scontrol.skipto(entry.song.songid);
            refreshPlaylist();

        } else if (cmd.equals("remove")) {
            JButton src = (JButton)e.getSource();
            PlaylistEntry entry =
                (PlaylistEntry)src.getClientProperty("entry");
            Chooser.scontrol.remove(entry.song.songid);
            refreshPlaylist();
        }
    }

    protected void refreshPlaylist ()
    {
        // stick a "loading" label in the list to let the user know
        // what's up
        _bpanel.removeAll();
        _bpanel.add(new JLabel("Loading..."));

        // start up the task that reads the CD info from the database
        TaskMaster.invokeMethodTask("readPlaylist", this, this);
    }

    public void readPlaylist ()
        throws SQLException
    {
        // clear out any previous playlist
        _plist.clear();

        // find out what's currently playing
        String playing = Chooser.scontrol.getPlaying();
        playing = StringUtil.split(playing, ":")[1].trim();
        _playid = -1;
        if (!playing.equals("<none>")) {
            try {
                _playid = Integer.parseInt(playing);
            } catch (NumberFormatException nfe) {
                Log.warning("Unable to parse currently playing id '" +
                            playing + "'.");
            }
        }

        // get the playlist from the music daemon
        String[] plist = Chooser.scontrol.getPlaylist();

        // parse it into playlist entries
        for (int i = 0; i < plist.length; i++) {
            String[] toks = StringUtil.split(plist[i], "\t");
            int eid, sid;
            try {
                eid = Integer.parseInt(toks[0]);
                sid = Integer.parseInt(toks[1]);
            } catch (NumberFormatException nfe) {
                Log.warning("Bogus playlist record: '" + plist[i] + "'.");
                continue;
            }
            Entry entry = Chooser.model.getEntry(eid);
            Song song = entry.getSong(sid);
            _plist.add(new PlaylistEntry(entry, song));
        }
    }

    public void taskCompleted (String name, Object result)
    {
	if (name.equals("readPlaylist")) {
            populatePlaylist();
        }
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

    protected void populatePlaylist ()
    {
        // clear out any existing children
        _bpanel.removeAll();

        // adjust our layout policy
        GroupLayout gl = (GroupLayout)_bpanel.getLayout();
        gl.setOffAxisPolicy(GroupLayout.EQUALIZE);
        gl.setOffAxisJustification(GroupLayout.LEFT);

        // add buttons for every entry
        String title = null;
        for (int i = 0; i < _plist.size(); i++) {
            PlaylistEntry entry = (PlaylistEntry)_plist.get(i);

            // add record/artist indicators when the record and artist
            // changes
            if (!entry.entry.title.equals(title)) {
                title = entry.entry.title;
                JLabel label = new JLabel(entry.entry.title + " - " +
                                          entry.entry.artist);
                _bpanel.add(label);
            }

            // create a browse and a play button
            JPanel hpanel = new JPanel();
            gl = new HGroupLayout(GroupLayout.NONE);
            gl.setOffAxisPolicy(GroupLayout.STRETCH);
            gl.setJustification(GroupLayout.LEFT);
            hpanel.setLayout(gl);

            entry.label = new JLabel(entry.song.title);
            if (entry.song.songid == _playid) {
                entry.label.setForeground(Color.red);
            }
            entry.label.setFont(_nameFont);
            hpanel.add(entry.label);

            JButton button;
            button = new JButton("Skip to");
            button.setFont(_nameFont);
            button.setActionCommand("skipto");
            button.addActionListener(this);
            button.putClientProperty("entry", entry);
            hpanel.add(button);

            button = new JButton("Remove");
            button.setActionCommand("remove");
            button.setFont(_nameFont);
            button.addActionListener(this);
            button.putClientProperty("entry", entry);
            hpanel.add(button);

            _bpanel.add(hpanel);
        }

        // if there were no entries, stick a label in to that effect
        if (_plist.size() == 0) {
            _bpanel.add(new JLabel("Nothing playing."));
        }
    }

    protected static class PlaylistEntry
    {
        public Entry entry;

        public Song song;

        public JLabel label;

        public PlaylistEntry (Entry entry, Song song)
        {
            this.entry = entry;
            this.song = song;
        }
    }

    protected JPanel _bpanel;
    protected JButton _clearbut;

    protected ArrayList _plist = new ArrayList();
    protected int _playid;

    protected Font _nameFont;
}
