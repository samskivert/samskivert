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
import com.samskivert.util.ArrayUtil;
import com.samskivert.util.StringUtil;

import com.samskivert.swing.util.SwingUtil;
import com.samskivert.swing.util.TaskAdapter;
import com.samskivert.swing.util.TaskMaster;

import robodj.Log;
import robodj.chooser.PlaylistPanel.PlaylistEntry;
import robodj.repository.Entry;
import robodj.repository.Song;
import robodj.util.ServerControl.PlayingListener;

/**
 * Handles playlist UI commands.
 */
public class PlaylistController extends ItemController
    implements PlayingListener
{
    public PlaylistController (PlaylistPanel panel)
    {
        super(panel);
        _panel = panel;

        // add ourselves as a playing listener
        Chooser.scontrol.addPlayingListener(this);
    }

    // documentation inherited
    public boolean handleAction (ActionEvent e)
    {
	String cmd = e.getActionCommand();
	if (cmd.equals(PlaylistPanel.CLEAR)) {
            TaskMaster.invokeMethodTask("clear", Chooser.scontrol, this);

        } else if (cmd.equals(PlaylistPanel.REFRESH)) {
            _panel.prepareForRefresh();

            // start up the task that reads the CD info from the database
            TaskMaster.invokeMethodTask("readPlaylist", this, this);

        } else if (cmd.equals(SongItem.SKIP_TO)) {
            JButton button = (JButton)e.getSource();
            SongItem item =  (SongItem)button.getParent();

            // fire up a task to talk to the music daemon
            final int songid = item.getSong().songid;
            TaskMaster.invokeTask("noop", new TaskAdapter() {
                public Object invoke () throws Exception {
                    Chooser.scontrol.skipto(songid);
                    return null;
                }
            }, this);

        } else if (cmd.equals(SongItem.REMOVE)) {
            JButton button = (JButton)e.getSource();
            SongItem item =  (SongItem)button.getParent();

            // remove the entry UI elements
            item.getParent().remove(item);
            SwingUtil.refresh(_panel); // relayout

            // remove the entry from the playlist
            _panel.plist.remove((PlaylistEntry)item.extra);

            // fire up a task to talk to the music daemon
            final int songid = item.getSong().songid;
            TaskMaster.invokeTask("noop", new TaskAdapter() {
                public Object invoke () throws Exception {
                    Chooser.scontrol.remove(songid);
                    return null;
                }
            }, this);

        } else if (cmd.equals(PlaylistPanel.REMOVE_ALL)) {
            JButton src = (JButton)e.getSource();
            PlaylistEntry entry =
                (PlaylistEntry)src.getClientProperty("entry");

            // remove all entries starting with this one until we get to
            // one that has a different entryid
            int count = 0;
            Iterator iter = _panel.plist.iterator();
            while (iter.hasNext()) {
                PlaylistEntry pe = (PlaylistEntry)iter.next();
                if (entry == pe) {
                    count++;
                    // remove the entry UI elements
                    pe.item.getParent().remove(pe.item);
                    // remove the entry
                    iter.remove();

                } else if (count > 0) {
                    if (pe.entry.entryid == entry.entry.entryid) {
                        count++;
                        // remove the entry UI elements
                        pe.item.getParent().remove(pe.item);
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

            _panel.revalidate(); // relayout

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

        } else if (cmd.equals(PlaylistPanel.SHUFFLE)) {
            // put all of the songs in the playlist into a list and
            // shuffle them
            ArrayList slist = new ArrayList();
            for (Iterator iter = _panel.plist.iterator(); iter.hasNext(); ) {
                slist.add(((PlaylistEntry)iter.next()).song);
            }
            final Song[] songs = (Song[])slist.toArray(new Song[slist.size()]);
            ArrayUtil.shuffle(songs);

            // now clear and reload the playlist entirely
            TaskMaster.invokeTask("noop", new TaskAdapter() {
                public Object invoke () throws Exception {
                    Chooser.scontrol.clear();
                    for (int ii = 0; ii < songs.length; ii++) {
                        Chooser.scontrol.append(
                            songs[ii].entryid, songs[ii].songid,
                            songs[ii].location);
                    }
                    // finally queue up a UI refresh
                    postAction(_panel, PlaylistPanel.REFRESH);
                    return null;
                }
            }, this);

        } else {
            return super.handleAction(e);
        }

        return true;
    }

    public void readPlaylist ()
        throws PersistenceException
    {
        // clear out any previous playlist
        _panel.plist.clear();

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
                if (song != null) {
                    _panel.plist.add(new PlaylistEntry(entry, song));
                } else {
                    Log.warning("No song for entry? [sid=" + sid +
                                ", entry=" + entry + "].");
                }
            } else {
                Log.warning("Unable to load entry [eid=" + eid + "].");
            }
        }

        // now find out what's currently playing
        Chooser.scontrol.refreshPlaying();
    }

    public void playingUpdated (int songid, boolean paused)
    {
        // unhighlight whoever is playing now
        PlaylistEntry pentry = getPlayingEntry();
        if (pentry != null && pentry.item != null) {
            pentry.item.setIsPlaying(false);
        }

        // grab the new playing song id
        _playid = songid;

        // highlight the playing song
        for (int i = 0; i < _panel.plist.size(); i++) {
            PlaylistEntry entry = (PlaylistEntry)_panel.plist.get(i);
            if (entry.song.songid == _playid && entry.item != null) {
                entry.item.setIsPlaying(true);
            }
        }
        _panel.repaint();
    }

    protected PlaylistEntry getPlayingEntry ()
    {
        for (int i = 0; i < _panel.plist.size(); i++) {
            PlaylistEntry entry = (PlaylistEntry)_panel.plist.get(i);
            if (entry.song.songid == _playid) {
                return entry;
            }
        }
        return null;
    }

    public void taskCompleted (String name, Object result)
    {
	if (name.equals("readPlaylist")) {
            _panel.populatePlaylist(_panel.plist, _playid);
        } else {
            super.taskCompleted(name, result);
        }
    }

    protected PlaylistPanel _panel;

    protected int _playid;
}
