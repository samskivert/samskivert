//
// $Id: EntryController.java,v 1.3 2004/01/26 16:10:55 mdb Exp $

package robodj.chooser;

import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

import com.samskivert.io.PersistenceException;
import com.samskivert.swing.util.SwingUtil;
import com.samskivert.swing.util.TaskMaster;
import com.samskivert.util.ArrayUtil;

import robodj.Log;
import robodj.data.PlaylistEntry;
import robodj.repository.Entry;
import robodj.repository.Song;

/**
 * Handles default entry list behavior.
  */
public abstract class EntryController extends ItemController
    implements AncestorListener
{
    public EntryController (EntryList list)
    {
        super(list);
        _list = list;
        _list.addAncestorListener(this);
    }

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

    // documentation inherited from interface
    public void taskCompleted (String name, Object result)
    {
	if (name.equals("readEntries")) {
            // the result should be an array of entry objects with which
            // we can populate our button list
            _entries = (Entry[])result;
            _list.populateEntries(_entries);

	} else if (name.equals("readAndPlay")) {
            appendToPlaylist(_entry.songs);

	} else if (name.equals("readSongs")) {
            _list.populateSong(_entry);

        } else {
            super.taskCompleted(name, result);
        }
    }

    /**
     * Reads the entries from the database. This will be called on a
     * separate thread and should be prepared to be called repeatedly
     * without undue overhead. When the user browses into a song and back
     * up again, this method is called to repopulate the entries.
     */
    public abstract Entry[] readEntries ()
        throws PersistenceException;

    // documentation inherited from interface
    public boolean handleAction (ActionEvent e)
    {
	String cmd = e.getActionCommand();
	if (cmd.equals(EntryItem.BROWSE)) {
            _entry = EntryItem.getEntry(e.getSource());
            // start up the task that reads this CDs songs from the database
            TaskMaster.invokeMethodTask("readSongs", this, this);

	} else if (cmd.equals(EntryItem.PLAY)) {
            _entry = EntryItem.getEntry(e.getSource());
            // start up the task that reads this CDs songs from the database
            TaskMaster.invokeMethodTask("readAndPlay", this, this);

        } else if (cmd.equals(SongItem.PLAY)) {
            Song song = SongItem.getSong(e.getSource());
            appendToPlaylist(new Song[] { song });

        } else if (cmd.equals("refresh")) {
            // re-read the category
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
            return super.handleAction(e);
	}

        return true;
    }

    // documentation inherited from interface
    public void ancestorAdded (AncestorEvent e)
    {
        // stick a "loading" label in the list to let the user know
        // what's up
        _list._bpanel.add(new JLabel("Loading..."));

        // we need to revalidate the component because we added a child
        _list.revalidate();
        _list.repaint();

        // start up the task that reads the CD info from the database
        TaskMaster.invokeMethodTask("readEntries", this, this);
    }

    // documentation inherited from interface
    public void ancestorRemoved (AncestorEvent e)
    {
        // clear out our entry ui elements
        _list._bpanel.removeAll();
    }

    // documentation inherited from interface
    public void ancestorMoved (AncestorEvent e)
    {
        // nothing doing
    }

    protected void appendToPlaylist (Song[] songs)
    {
        PlaylistEntry[] plist = Chooser.djobj.playlist;
        if (plist == null) {
            plist = new PlaylistEntry[0];
        }
        for (int i = 0; i < songs.length; i++) {
            if (songs[i].isHated()) {
                continue;
            }
            PlaylistEntry entry = new PlaylistEntry(
                songs[i].entryid, songs[i].songid, songs[i].title);
            plist = (PlaylistEntry[])ArrayUtil.append(plist, entry);
        }
        Chooser.djobj.setPlaylist(plist);
    }

    protected EntryList _list;
    protected Entry[] _entries;
    protected Entry _entry;
    protected int _newcatid;
}
