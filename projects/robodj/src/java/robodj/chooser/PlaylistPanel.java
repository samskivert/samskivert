//
// $Id: PlaylistPanel.java,v 1.17 2004/01/28 02:36:23 mdb Exp $

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

public class PlaylistPanel extends ControlledPanel
{
    public static class PlaylistEntry
    {
        public Entry entry;

        public Song song;

        public SongItem item;

        public PlaylistEntry (Entry entry, Song song)
        {
            this.entry = entry;
            this.song = song;
        }

        public String toString ()
        {
            return StringUtil.fieldsToString(this);
        }
    }

    public static final String REFRESH = "refresh";

    public static final String CLEAR = "clear";

    public static final String REMOVE_ALL = "remove_all";

    public ArrayList plist = new ArrayList();

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
            CLEAR_TIP, "clear", CLEAR_ICON_PATH);
        cbar.add(_clearbut);
        cbar.add(ButtonUtil.createControlButton(
                     REFRESH_TIP, "refresh", REFRESH_ICON_PATH));
        add(cbar, GroupLayout.FIXED);

        // use a special font for our name buttons
        _nameFont = new Font("Helvetica", Font.PLAIN, 12);

        // create our icons
        _removeEntryIcon = ButtonUtil.getIcon(REMOVE_ENTRY_ICON_PATH);
    }

    // documentation inherited
    public void addNotify ()
    {
        super.addNotify();
        // load up the playlist
        _controller.postAction(this, REFRESH);
    }

    // documentation inherited
    protected Controller createController ()
    {
        return new PlaylistController(this);
    }

    protected void prepareForRefresh ()
    {
        // stick a "loading" label in the list to let the user know
        // what's up
        _bpanel.removeAll();
        _bpanel.add(new JLabel("Loading..."));
        // swing doesn't automatically validate after adding/removing
        // children
        _bpanel.revalidate();
    }

    protected void populatePlaylist (ArrayList plist, int playid)
    {
        // clear out any existing children
        _bpanel.removeAll();

        // adjust our layout policy
        GroupLayout gl = (GroupLayout)_bpanel.getLayout();
        gl.setOffAxisPolicy(GroupLayout.EQUALIZE);
        gl.setOffAxisJustification(GroupLayout.LEFT);

        // add buttons for every entry
        String title = null;
        for (int i = 0; i < plist.size(); i++) {
            PlaylistEntry entry = (PlaylistEntry)plist.get(i);
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
                tpanel.add(newButton(REMOVE_ENTRY_TIP, REMOVE_ALL,
                                     _removeEntryIcon, entry));

                _bpanel.add(tpanel);
            }

            // create a song item to display this playlist entry
            entry.item = new SongItem(entry.song, SongItem.PLAYLIST);
            entry.item.setIsPlaying(entry.song.songid == playid);
            entry.item.extra = entry;
            _bpanel.add(entry.item);

            // let the bpanel know that we want to scroll the active track
            // label into place once we're all laid out
            if (entry.song.songid == playid) {
                _bpanel.setScrollTarget(entry.item);
            }
        }

        // if there were no entries, stick a label in to that effect
        if (plist.size() == 0) {
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
            tooltip, command, icon, true);
        button.putClientProperty("entry", clientProperty);
        return button;
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
