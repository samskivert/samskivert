//
// $Id: EntryList.java,v 1.12 2002/03/03 20:56:12 mdb Exp $

package robodj.chooser;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.*;

import javax.swing.*;
import javax.swing.event.*;

import com.samskivert.io.PersistenceException;
import com.samskivert.swing.*;
import com.samskivert.swing.util.*;

import robodj.Log;
import robodj.repository.*;
import robodj.util.ButtonUtil;

public abstract class EntryList extends JScrollPane
    implements TaskObserver, ActionListener, AncestorListener
{
    public EntryList ()
    {
        // create the pane that will hold the buttons
        GroupLayout gl = new VGroupLayout(GroupLayout.NONE);
        gl.setOffAxisPolicy(GroupLayout.STRETCH);
        gl.setJustification(GroupLayout.TOP);
        gl.setGap(2);
        _bpanel = new ScrollablePanel() {
            // make the playlist fit the width of the scrolling viewport
            public boolean getScrollableTracksViewportWidth () {
                return true;
            }
        };
        _bpanel.setLayout(gl);

	// give ourselves a wee bit of a border
	_bpanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // set our viewport view
        setViewportView(_bpanel);

        // use a special font for our name buttons
        _titleFont = new Font("Helvetica", Font.BOLD, 14);
        _nameFont = new Font("Helvetica", Font.PLAIN, 12);

        // create our icons
        _browseIcon = ButtonUtil.getIcon(BROWSE_ICON_PATH);
        _playIcon = ButtonUtil.getIcon(PLAY_ICON_PATH);

        // listen to ancestor events
        addAncestorListener(this);
    }

    public void taskCompleted (String name, Object result)
    {
	if (name.equals("readEntries")) {
            // the result should be an array of entry objects with which
            // we can populate our button list
            _entries = (Entry[])result;
            populateEntries(_entries);

	} else if (name.equals("readAndPlay")) {
            for (int i = 0; i < _entry.songs.length; i++) {
                Chooser.scontrol.append(_entry.songs[i].entryid,
                                        _entry.songs[i].songid,
                                        _entry.songs[i].location);
            }

	} else if (name.equals("readSongs")) {
            populateSong(_entry);
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

    /**
     * Reads the entries from the database. This will be called on a
     * separate thread and should be prepared to be called repeatedly
     * without undue overhead. When the user browses into a song and back
     * up again, this method is called to repopulate the entries.
     */
    public abstract Entry[] readEntries ()
        throws PersistenceException;

    /** The string to display when there are no matching results. */
    protected abstract String getEmptyString ();

    /**
     * Reads the entries from the database.
     */
    public void readSongs ()
        throws PersistenceException
    {
        Chooser.repository.populateSongs(_entry);
    }

    /**
     * Reads the entries from the database and plays them all.
     */
    public void readAndPlay ()
        throws PersistenceException
    {
        Chooser.repository.populateSongs(_entry);
    }

    /**
     * Updates the category for the displayed entry.
     */
    public void recategorizeEntry ()
        throws PersistenceException
    {
        Chooser.model.recategorize(_entry, _newcatid);
    }

    /**
     * Creates the proper buttons, etc. for each entry.
     */
    protected void populateEntries (Entry[] entries)
    {
        // clear out any existing children
        _bpanel.removeAll();

        // sort our entries
        Comparator ecomp = new Comparator() {
            public int compare (Object o1, Object o2) {
                Entry e1 = (Entry)o1, e2 = (Entry)o2;
                int rv = e1.artist.compareTo(e2.artist);
                return rv == 0 ? e1.title.compareTo(e2.title) : rv;
            }
            public boolean equals (Object o1) {
                return o1.equals(this);
            }
        };
        Arrays.sort(entries, ecomp);

        // and add buttons for every entry
        String artist = null;
        for (int i = 0; i < entries.length; i++) {
            if (!entries[i].artist.equals(artist)) {
                artist = entries[i].artist;
                JLabel label = new JLabel(entries[i].artist);
                _bpanel.add(label);
            }

            // create a browse and a play button
            GroupLayout gl = new HGroupLayout(GroupLayout.NONE);
            gl.setOffAxisPolicy(GroupLayout.STRETCH);
            gl.setJustification(GroupLayout.LEFT);
            JPanel hpanel = new JPanel(gl);
            JButton button;

            // add a browse button
            button = ButtonUtil.createControlButton(
                BROWSE_ENTRY_TIP, "browse", _browseIcon, this, true);
            button.putClientProperty("entry", entries[i]);
            hpanel.add(button, GroupLayout.FIXED);

            // add a play all button
            button = ButtonUtil.createControlButton(
                PLAY_ENTRY_TIP, "playall", _playIcon, this, true);
            button.putClientProperty("entry", entries[i]);
            hpanel.add(button, GroupLayout.FIXED);

            // add the entry title
            JLabel entryLabel = new JLabel(entries[i].title);
            entryLabel.setFont(_nameFont);
            entryLabel.setToolTipText(entries[i].title +
                                      " (" + entries[i].entryid + ")");
            hpanel.add(entryLabel);

            _bpanel.add(hpanel);
        }

        // if there were no entries, stick a label in to that effect
        if (_entries.length == 0) {
            _bpanel.add(new JLabel(getEmptyString()));
        }

        // reset our scroll position so that we're displaying the top of
        // the entry list. we'd like to save our scroll position and
        // restore it when the user clicks "up", but as we're rebuilding
        // the entry display the scroll bar is still configured for the
        // old contents and we're not around when it gets configured with
        // the new contents, so we can't ensure that the scrollbar has
        // been properly configured before we adjust its position back to
        // where we were... oh the complication.
        clearScrollPosition();

        // we've removed and added components so we need to revalidate
        revalidate();
        repaint();
    }

    protected void populateSong (Entry entry)
    {
        // clear out any existing children
        _bpanel.removeAll();

        GroupLayout gl = new HGroupLayout(HGroupLayout.STRETCH);
        gl.setJustification(GroupLayout.LEFT);
	JPanel header = new JPanel(gl);

        // add the album title
        JLabel label = new JLabel(entry.title);
        label.setToolTipText(entry.title);
        label.setFont(_titleFont);
        header.add(label);

        // add a button for getting the heck out of here
        JButton upbtn = ButtonUtil.createControlButton(
            UP_TIP, "up", ButtonUtil.getIcon(UP_ICON_PATH), this, true);
        header.add(upbtn, GroupLayout.FIXED);

        // create an edit button
        JButton ebtn = ButtonUtil.createControlButton(
            EDIT_TIP, "edit", ButtonUtil.getIcon(EDIT_ICON_PATH), this, true);
        header.add(ebtn, GroupLayout.FIXED);

        // add a combo box for categorizing
        JComboBox catcombo =
            new JComboBox(ModelUtil.catBoxNames(Chooser.model));
	header.add(catcombo, GroupLayout.FIXED);

        // configure the combo box
        catcombo.addActionListener(this);
        catcombo.setActionCommand("categorize");
        int catid = Chooser.model.getCategory(entry.entryid);
        int catidx = ModelUtil.getCategoryIndex(Chooser.model, catid);
        catcombo.setSelectedIndex(catidx+1);

	_bpanel.add(header, GroupLayout.FIXED);

        // sort the songs by position
        Comparator scomp = new Comparator() {
            public int compare (Object o1, Object o2) {
                Song s1 = (Song)o1, s2 = (Song)o2;
                return s1.position - s2.position;
            }
            public boolean equals (Object o1) {
                return o1.equals(this);
            }
        };
        Arrays.sort(entry.songs, scomp);

        // and add buttons for every song
        for (int i = 0; i < entry.songs.length; i++) {
            gl = new HGroupLayout(GroupLayout.NONE);
            gl.setJustification(GroupLayout.LEFT);
            JPanel hpanel = new JPanel(gl);

            // add a button for playing the song
            JButton button = ButtonUtil.createControlButton(
                PLAY_SONG_TIP, "play", _playIcon, this, true);
            button.putClientProperty("song", entry.songs[i]);
            hpanel.add(button, GroupLayout.FIXED);

            // add the song title
            JLabel trackLabel = new JLabel(entry.songs[i].title);
            trackLabel.setFont(_nameFont);
            trackLabel.setToolTipText(entry.songs[i].title);
            hpanel.add(trackLabel);

            _bpanel.add(hpanel);
        }

        // reset our scroll position so that we're displaying the top of
        // the song list
        clearScrollPosition();

        // we've removed and added components so we need to revalidate
        revalidate();
        repaint();
    }

    protected void clearScrollPosition ()
    {
        BoundedRangeModel model = getVerticalScrollBar().getModel();
        model.setValue(model.getMinimum());
    }

    public void actionPerformed (ActionEvent e)
    {
	String cmd = e.getActionCommand();
	if (cmd.equals("browse")) {
            JButton src = (JButton)e.getSource();
            _entry = (Entry)src.getClientProperty("entry");
            // start up the task that reads this CDs songs from the database
            TaskMaster.invokeMethodTask("readSongs", this, this);

	} else if (cmd.equals("playall")) {
            JButton src = (JButton)e.getSource();
            _entry = (Entry)src.getClientProperty("entry");
            // start up the task that reads this CDs songs from the database
            TaskMaster.invokeMethodTask("readAndPlay", this, this);

        } else if (cmd.equals("play")) {
            JButton src = (JButton)e.getSource();
            Song song = (Song)src.getClientProperty("song");
            Chooser.scontrol.append(song.entryid, song.songid, song.location);

        } else if (cmd.equals("up")) {
            // re-read the category beacuse this entry may have been
            // recategorized
            TaskMaster.invokeMethodTask("readEntries", this, this);

        } else if (cmd.equals("edit")) {
            EditDialog dialog = new EditDialog(_entry);
            dialog.setSize(400, 400);
            SwingUtil.centerWindow(dialog);
            dialog.show();

        } else if (cmd.equals("categorize")) {
            JComboBox cb = (JComboBox)e.getSource();
            String catname = (String)cb.getSelectedItem();
            _newcatid = Chooser.model.getCategoryId(catname);
            // do the recategorization in a separate task
            TaskMaster.invokeMethodTask("recategorizeEntry", this, this);

	} else {
	    Log.warning("Unknown action event: " + cmd);
	}
    }

    public void ancestorAdded (AncestorEvent e)
    {
        // stick a "loading" label in the list to let the user know
        // what's up
        _bpanel.add(new JLabel("Loading..."));

        // we need to revalidate the component because we added a child
        revalidate();
        repaint();

        // start up the task that reads the CD info from the database
        TaskMaster.invokeMethodTask("readEntries", this, this);
    }

    public void ancestorRemoved (AncestorEvent e)
    {
        // clear out our entry ui elements
        _bpanel.removeAll();
    }

    public void ancestorMoved (AncestorEvent e)
    {
        // nothing doing
    }

    protected JPanel _bpanel;

    protected Entry[] _entries;
    protected Entry _entry;
    protected int _newcatid;

    protected Font _titleFont;
    protected Font _nameFont;
    protected ImageIcon _playIcon;
    protected ImageIcon _browseIcon;

    protected static final String PLAY_ENTRY_TIP =
        "Append this album to the playlist";
    protected static final String PLAY_SONG_TIP =
        "Append this song to the playlist";
    protected static final String BROWSE_ENTRY_TIP =
        "Browse the songs in this album";
    protected static final String UP_TIP =
        "Back up to albums listing";
    protected static final String EDIT_TIP =
        "Edit the album information";

    protected static final String ICON_ROOT = "/robodj/chooser/images/";
    protected static final String PLAY_ICON_PATH = ICON_ROOT + "play.png";
    protected static final String BROWSE_ICON_PATH = ICON_ROOT + "browse.png";
    protected static final String UP_ICON_PATH = ICON_ROOT + "up.png";
    protected static final String EDIT_ICON_PATH = ICON_ROOT + "edit.png";
}
