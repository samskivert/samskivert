//
// $Id: PlaylistPanel.java,v 1.13 2002/03/03 20:56:12 mdb Exp $

package robodj.chooser;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

import java.util.ArrayList;
import java.util.Iterator;

import com.samskivert.io.PersistenceException;
import com.samskivert.swing.*;
import com.samskivert.swing.util.*;
import com.samskivert.util.StringUtil;

import robodj.Log;
import robodj.repository.*;
import robodj.util.ButtonUtil;
import robodj.util.ServerControl.PlayingListener;

public class PlaylistPanel extends JPanel
    implements TaskObserver, ActionListener, PlayingListener
{
    public PlaylistPanel ()
    {
	GroupLayout gl = new VGroupLayout(GroupLayout.STRETCH);
        gl.setOffAxisPolicy(GroupLayout.STRETCH);
	setLayout(gl);

        // create the pane that will hold the buttons
        gl = new VGroupLayout(GroupLayout.NONE);
        gl.setJustification(GroupLayout.TOP);
        gl.setGap(2);
        _bpanel = new SmartPanel();
        _bpanel.setLayout(gl);

	// give ourselves a wee bit of a border
	_bpanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // put it into a scrolling pane
	JScrollPane bscroll = new JScrollPane(_bpanel);
        add(bscroll);

        GroupLayout bgl = new HGroupLayout(GroupLayout.NONE);
        bgl.setJustification(GroupLayout.RIGHT);
        JPanel cbar = new JPanel(bgl);

        // add our control buttons
        _clearbut = ButtonUtil.createControlButton(
            CLEAR_TIP, "clear", CLEAR_ICON_PATH, this);
        cbar.add(_clearbut);
        cbar.add(ButtonUtil.createControlButton(
                     REFRESH_TIP, "refresh", REFRESH_ICON_PATH, this));
        add(cbar, GroupLayout.FIXED);

        // use a special font for our name buttons
        _nameFont = new Font("Helvetica", Font.PLAIN, 12);

        // create our icons
        _skiptoIcon = ButtonUtil.getIcon(SKIPTO_ICON_PATH);
        _removeSongIcon = ButtonUtil.getIcon(REMOVE_SONG_ICON_PATH);
        _removeEntryIcon = ButtonUtil.getIcon(REMOVE_ENTRY_ICON_PATH);

        // add ourselves as a playing listener
        Chooser.scontrol.addPlayingListener(this);

        // load up the playlist
        refreshPlaylist();
    }

    public void actionPerformed (ActionEvent e)
    {
	String cmd = e.getActionCommand();
	if (cmd.equals("clear")) {
            TaskMaster.invokeMethodTask("clear", Chooser.scontrol, this);

        } else if (cmd.equals("refresh")) {
            refreshPlaylist();

        } else if (cmd.equals("skipto")) {
            JButton src = (JButton)e.getSource();
            PlaylistEntry entry =
                (PlaylistEntry)src.getClientProperty("entry");

            // fire up a task to talk to the music daemon
            final int songid = entry.song.songid;
            TaskMaster.invokeTask("noop", new TaskAdapter() {
                public Object invoke () throws Exception {
                    Chooser.scontrol.skipto(songid);
                    return null;
                }
            }, this);

        } else if (cmd.equals("remove")) {
            JButton src = (JButton)e.getSource();
            PlaylistEntry entry =
                (PlaylistEntry)src.getClientProperty("entry");

            // remove the entry UI elements
            JPanel epanel = (JPanel)entry.label.getParent();
            epanel.getParent().remove(epanel);
            revalidate(); // relayout

            // remove the entry from the playlist
            _plist.remove(entry);

            // fire up a task to talk to the music daemon
            final int songid = entry.song.songid;
            TaskMaster.invokeTask("noop", new TaskAdapter() {
                public Object invoke () throws Exception {
                    Chooser.scontrol.remove(songid);
                    return null;
                }
            }, this);

        } else if (cmd.equals("remove_all")) {
            JButton src = (JButton)e.getSource();
            PlaylistEntry entry =
                (PlaylistEntry)src.getClientProperty("entry");

            // remove all entries starting with this one until we get to
            // one that has a different entryid
            int count = 0;
            Iterator iter = _plist.iterator();
            while (iter.hasNext()) {
                PlaylistEntry pe = (PlaylistEntry)iter.next();
                if (entry == pe) {
                    count++;
                    // remove the entry UI elements
                    JPanel epanel = (JPanel)pe.label.getParent();
                    epanel.getParent().remove(epanel);
                    // remove the entry
                    iter.remove();

                } else if (count > 0) {
                    if (pe.entry.entryid == entry.entry.entryid) {
                        count++;
                        // remove the entry UI elements
                        JPanel epanel = (JPanel)pe.label.getParent();
                        epanel.getParent().remove(epanel);
                        // remove the entry
                        iter.remove();

                    } else {
                        // we hit an entry that doesn't match, bail
                        break;
                    }
                }
            }

            // remove the title and stuff
            JPanel tpanel = (JPanel)src.getParent();
            tpanel.getParent().remove(tpanel);

            revalidate(); // relayout

            // fire up a task to talk to the music daemon, telling it to
            // remove those entries
            final int songid = entry.song.songid;
            final int fcount = count;
            TaskMaster.invokeTask("noop", new TaskAdapter() {
                public Object invoke () throws Exception {
                    Chooser.scontrol.removeGroup(songid, fcount);
                    return null;
                }
            }, this);
        }
    }

    protected void refreshPlaylist ()
    {
        // stick a "loading" label in the list to let the user know
        // what's up
        _bpanel.removeAll();
        _bpanel.add(new JLabel("Loading..."));
        // swing doesn't automatically validate after adding/removing
        // children
        _bpanel.revalidate();

        // start up the task that reads the CD info from the database
        TaskMaster.invokeMethodTask("readPlaylist", this, this);
    }

    public void readPlaylist ()
        throws PersistenceException
    {
        // clear out any previous playlist
        _plist.clear();

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
            if (entry != null) {
                Song song = entry.getSong(sid);
                _plist.add(new PlaylistEntry(entry, song));
            } else {
                Log.warning("Unable to load entry [eid=" + eid + "].");
            }
        }

        // now find out what's currently playing
        Chooser.scontrol.refreshPlaying();
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
            JButton button;

            // add record/artist indicators when the record and artist
            // changes
            if (!entry.entry.title.equals(title)) {
                JPanel tpanel = new JPanel();
                gl = new HGroupLayout(GroupLayout.NONE);
                gl.setOffAxisPolicy(GroupLayout.STRETCH);
                gl.setJustification(GroupLayout.LEFT);
                tpanel.setLayout(gl);

                title = entry.entry.title;
                JLabel label = new JLabel(entry.entry.title + " - " +
                                          entry.entry.artist);
                tpanel.add(label);
                tpanel.add(newButton(REMOVE_ENTRY_TIP, "remove_all",
                                     _removeEntryIcon, entry));

                _bpanel.add(tpanel);
            }

            // create a browse and a play button
            JPanel hpanel = new JPanel();
            gl = new HGroupLayout(GroupLayout.NONE);
            gl.setOffAxisPolicy(GroupLayout.STRETCH);
            gl.setJustification(GroupLayout.LEFT);
            hpanel.setLayout(gl);

            hpanel.add(newButton(SKIPTO_TIP, "skipto", _skiptoIcon, entry));
            hpanel.add(newButton(REMOVE_SONG_TIP, "remove",
                                 _removeSongIcon, entry));

            entry.label = new JLabel(entry.song.title);
            entry.label.setForeground((entry.song.songid == _playid) ?
                                      Color.red : Color.black);
            entry.label.setFont(_nameFont);
            entry.label.setToolTipText(entry.song.title);
            hpanel.add(entry.label);

            // let the bpanel know that we want to scroll the active track
            // label into place once we're all laid out
            if (entry.song.songid == _playid) {
                _bpanel.setScrollTarget(hpanel);
            }

            _bpanel.add(hpanel);
        }

        // if there were no entries, stick a label in to that effect
        if (_plist.size() == 0) {
            _bpanel.add(new JLabel("Nothing playing."));
        }

        // swing doesn't automatically validate after adding/removing
        // children
        _bpanel.revalidate();
    }

    protected JButton newButton (String tooltip, String command,
                                 ImageIcon icon, Object clientProperty)
    {
        JButton button = ButtonUtil.createControlButton(
            tooltip, command, icon, this, true);
        button.putClientProperty("entry", clientProperty);
        return button;
    }

    public void playingUpdated (int songid, boolean paused)
    {
        // unhighlight whoever is playing now
        PlaylistEntry pentry = getPlayingEntry();
        if (pentry != null && pentry.label != null) {
            pentry.label.setForeground(Color.black);
        }

        // grab the new playing song id
        _playid = songid;

        // highlight the playing song
        for (int i = 0; i < _plist.size(); i++) {
            PlaylistEntry entry = (PlaylistEntry)_plist.get(i);
            if (entry.song.songid == _playid &&
                entry.label != null) {
                entry.label.setForeground(Color.red);
            }
        }
        repaint();
    }

    protected PlaylistEntry getPlayingEntry ()
    {
        for (int i = 0; i < _plist.size(); i++) {
            PlaylistEntry entry = (PlaylistEntry)_plist.get(i);
            if (entry.song.songid == _playid) {
                return entry;
            }
        }
        return null;
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

    /**
     * A panel that can be made to scroll a particular child into view
     * when it becomes visible.
     */
    protected static class SmartPanel extends ScrollablePanel
    {
        public void setScrollTarget (JComponent comp)
        {
            _target = comp;
        }

        public void doLayout ()
        {
            super.doLayout();

            if (_target != null) {
                Rectangle bounds = _target.getBounds();
                _target = null;
                // this seems to sometimes call layout(), so we need to
                // prevent recursion
                scrollRectToVisible(bounds);
            }
        }

        // make the playlist fit the width of the scrolling viewport
        public boolean getScrollableTracksViewportWidth ()
        {
            return true;
        }

        protected JComponent _target;
    }

    protected SmartPanel _bpanel;
    protected JButton _clearbut;

    protected ArrayList _plist = new ArrayList();
    protected int _playid;
    protected int _oldid = -1;

    protected Font _nameFont;

    protected ImageIcon _skiptoIcon;
    protected ImageIcon _removeSongIcon;
    protected ImageIcon _removeEntryIcon;

    // icon paths
    protected static final String ICON_ROOT = "/robodj/chooser/images/";
    protected static final String REFRESH_ICON_PATH = ICON_ROOT + "refresh.png";
    protected static final String CLEAR_ICON_PATH = ICON_ROOT + "clear.png";
    protected static final String SKIPTO_ICON_PATH = ICON_ROOT + "skip.png";
    protected static final String REMOVE_ENTRY_ICON_PATH =
        ICON_ROOT + "remove_entry.png";
    protected static final String REMOVE_SONG_ICON_PATH =
        ICON_ROOT + "remove_song.png";

    // button tips
    protected static final String REFRESH_TIP = "Refresh the playlist";
    protected static final String CLEAR_TIP =
        "Clear all songs from the playlist";
    protected static final String SKIPTO_TIP = "Skip to this song";
    protected static final String REMOVE_ENTRY_TIP =
        "Remove all songs in this entry from the playlist";
    protected static final String REMOVE_SONG_TIP =
        "Remove this song from the playlist";
}
