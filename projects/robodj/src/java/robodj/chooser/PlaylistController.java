//
// $Id: PlaylistController.java,v 1.3 2004/02/24 12:40:24 mdb Exp $

package robodj.chooser;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.samskivert.io.PersistenceException;
import com.samskivert.util.StringUtil;

import com.samskivert.swing.util.SwingUtil;
import com.samskivert.swing.util.TaskAdapter;
import com.samskivert.swing.util.TaskMaster;

import com.threerings.presents.dobj.AttributeChangeListener;
import com.threerings.presents.dobj.AttributeChangedEvent;

import robodj.Log;
import robodj.data.DJObject;
import robodj.data.PlaylistEntry;
import robodj.repository.Entry;
import robodj.repository.Song;

/**
 * Handles playlist UI commands.
 */
public class PlaylistController extends ItemController
    implements AttributeChangeListener
{
    public PlaylistController (PlaylistPanel panel)
    {
        super(panel);
        _panel = panel;

        // listen to the DJ object
        Chooser.djobj.addListener(this);
    }

    public void wasAdded ()
    {
        super.wasAdded();

        // start up the task that reads the CD info from the database
        TaskMaster.invokeMethodTask("readPlaylist", this, this);
    }

    // documentation inherited
    public boolean handleAction (ActionEvent e)
    {
	String cmd = e.getActionCommand();
	if (cmd.equals(PlaylistPanel.CLEAR)) {
            Chooser.djobj.clear();

        } else if (cmd.equals(SongItem.SKIP_TO)) {
            JButton button = (JButton)e.getSource();
            SongItem item =  (SongItem)button.getParent();
            Chooser.djobj.play(((Integer)item.extra).intValue());

        } else if (cmd.equals(SongItem.REMOVE)) {
            JButton button = (JButton)e.getSource();
            SongItem item =  (SongItem)button.getParent();
            int sidx = ((Integer)item.extra).intValue();
            Chooser.djobj.remove(sidx, 1);

        } else if (cmd.equals(PlaylistPanel.REMOVE_ALL)) {
            JButton src = (JButton)e.getSource();
            PlaylistPanel.PLE entry =
                (PlaylistPanel.PLE)src.getClientProperty("entry");

            // remove all entries starting with this one until we get to
            // one that has a different entryid
            int start = -1, length = 1;
            for (int ii = 0; ii < _panel.plist.size(); ii++) {
                PlaylistPanel.PLE pe = (PlaylistPanel.PLE)_panel.plist.get(ii);
                if (entry == pe) {
                    start = ii;
                } else if (start != -1) {
                    if (pe.entry.entryid == entry.entry.entryid) {
                        length++;
                    } else {
                        // we hit an entry that doesn't match, bail
                        break;
                    }
                }
            }
            if (start != -1) {
                Chooser.djobj.remove(start, length);
            }

        } else if (cmd.equals(PlaylistPanel.SHUFFLE)) {
            // shuffle!
            Chooser.djobj.shuffle();

        } else {
            return super.handleAction(e);
        }

        return true;
    }

    // documentation inherited from interface
    public void attributeChanged (AttributeChangedEvent event)
    {
        if (event.getName().equals(DJObject.PLAYLIST)) {
            TaskMaster.invokeMethodTask("readPlaylist", this, this);

        } else if (event.getName().equals(DJObject.PLAYING)) {
            playingUpdated();
        }
    }

    public void readPlaylist ()
        throws PersistenceException
    {
        // clear out any previous playlist
        _panel.plist.clear();

        int pcount = (Chooser.djobj.playlist == null) ? 0 :
            Chooser.djobj.playlist.length;
        for (int ii = 0; ii < pcount; ii++) {
            PlaylistEntry pentry = Chooser.djobj.playlist[ii];
            Entry entry = Chooser.model.getEntry(pentry.entryId);
            if (entry != null) {
                Song song = entry.getSong(pentry.songId);
                if (song != null) {
                    _panel.plist.add(new PlaylistPanel.PLE(entry, song));
                } else {
                    Log.warning("No song for entry? [pentry=" + pentry + "].");
                }
            } else {
                Log.warning("Unable to load entry? [pentry=" + pentry + "].");
            }
        }
    }

    public void playingUpdated ()
    {
        // unhighlight whoever is playing now
        if (_playing != null) {
            _playing.item.setIsPlaying(false);
            _playing = null;
        }

        int pidx = Chooser.djobj.playing;
        if (pidx >= 0 && _panel.plist.size() > pidx) {
            _playing = (PlaylistPanel.PLE)_panel.plist.get(pidx);
        }

        if (_playing != null) {
            _playing.item.setIsPlaying(true);
        }
        _panel.repaint();
    }

    public void taskCompleted (String name, Object result)
    {
	if (name.equals("readPlaylist")) {
            _panel.populatePlaylist(_panel.plist);
            playingUpdated();
        } else {
            super.taskCompleted(name, result);
        }
    }

    protected PlaylistPanel _panel;
    protected PlaylistPanel.PLE _playing;
}
