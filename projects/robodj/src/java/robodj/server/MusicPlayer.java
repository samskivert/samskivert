//
// $Id$

package robodj.server;

import java.io.FileInputStream;

import javazoom.jl.player.Player;

import com.threerings.presents.dobj.AttributeChangeListener;
import com.threerings.presents.dobj.AttributeChangedEvent;

import robodj.Log;
import robodj.data.DJObject;
import robodj.data.PlaylistEntry;
import robodj.repository.Entry;
import robodj.repository.Song;

/**
 * Handles the actual playing of music in the playlist.
 */
public class MusicPlayer extends Thread
    implements AttributeChangeListener
{
    public MusicPlayer ()
    {
        // start up the music playing thread
        setDaemon(true);
        start();
    }

    public void init (DJObject djobj)
    {
        _djobj = djobj;
        _djobj.addListener(this);
    }

    // documentation inherited from interface
    public void attributeChanged (AttributeChangedEvent event)
    {
        if (event.getName().equals(DJObject.PLAYING)) {
            playEntry(_djobj.getPlaying());
            recomputeNext();

        } else if (event.getName().equals(DJObject.PLAYLIST)) {
            recomputeNext();

        } else if (event.getName().equals(DJObject.PAUSED)) {
            // wake the music thread if necessary
            synchronized (this) {
                notify();
            }
        }
    }

    public void run ()
    {
        while (true) {
            try {
                // this will block if there's nothing to play
                Player player = getPlaying();
                Log.info("Playing " + _pentry + "...");
                while (player.play(PLAY_FRAMES)) {
                    checkPaused();
                }

            } catch (Throwable t) {
                Log.warning("Music player choked.");
                Log.logStackTrace(t);
            }
        }
    }

    /**
     * Configures the specified playlist entry as the currently playing
     * entry. Even if we are currently playing the same entry, we will
     * restart play from the beginning.
     */
    protected synchronized void playEntry (PlaylistEntry entry)
    {
        // if we're already playing what we want to be playing, stop
        if (equals(_pentry, entry)) {
            return;
        }

        Log.info("Forcing play [entry=" + entry + "].");

        // queue up the next track
        _fnentry = entry;
        _fnext = getPlayer(_fnentry);

        // if we're clearing the current entry, clear next as well
        if (entry == null && _next != null) {
            _next.close();
            _next = null;
            _nentry = null;
        }

        // clear out any current track
        if (_playing != null) {
            Log.info("Stopping current track.");
            _playing.close();
            _pentry = null;
        }

        // finally wake up the player in case it is sleeping
        notify();
    }

    protected synchronized void recomputeNext ()
    {
        // if we're properly wired up, NOOP
        PlaylistEntry nentry = _djobj.getNext();
        if (equals(_nentry, nentry)) {
            return;
        }

        // clear things out in preparation
        if (_next != null) {
            _next.close();
            _next = null;
        }

        // compute and load up info on the next playing song
        _nentry = nentry;
        Log.info("Queuing up " + _nentry + "...");
        _next = getPlayer(_nentry);

        // wake up the music player if necessary
        notify();
    }

    protected Player getPlayer (PlaylistEntry entry)
    {
        if (entry == null) {
            return null;
        }

        Song song = null;
        try {
            Entry album = MusicDaemon.repo.getEntry(entry.entryId);
            if (album != null) {
                song = album.getSong(entry.songId);
            }
            if (album == null || song == null) {
                Log.warning("Bogus playlist entry! [entry=" + entry +
                            ", album=" + album + ", song=" + song + "].");
                return null;
            }
            return new Player(new FileInputStream(song.location));

        } catch (Exception e) {
            Log.warning("Failed to load song [pentry=" + entry +
                        ", song=" + song + "].");
            Log.logStackTrace(e);
            return null;
        }
    }

    protected synchronized Player getPlaying ()
    {
        while (_next == null && _fnext == null) {
            try {
                Log.info("Waiting for track...");
                wait();
            } catch (InterruptedException ie) {
                Log.warning("Interrupted waiting for track to play.");
            }
        }

        if (_fnext != null) {
            _playing = _fnext;
            _pentry = _fnentry;
            _fnext = null;
            _fnentry = null;
        } else {
            _playing = _next;
            _pentry = _nentry;
            _nentry = null;
            _next = null;
            _djobj.skip(); // move forward in the playlist
        }
        return _playing;
    }

    protected synchronized void checkPaused ()
    {
        while (_djobj.paused) {
            try {
                wait();
            } catch (InterruptedException ie) {
                Log.warning("Interrupted waiting for unpause.");
            }
        }
    }

    /** Helper function. */
    protected boolean equals (PlaylistEntry e1, PlaylistEntry e2)
    {
        return (e1 == e2 || (e1 != null && e1.equals(e2)));
    }

    protected DJObject _djobj;

    protected Player _playing;
    protected PlaylistEntry _pentry;

    protected Player _next, _fnext;
    protected int _nidx;
    protected PlaylistEntry _nentry, _fnentry;

    /** The number of frames to play before checking up on things. */
    protected static final int PLAY_FRAMES = 1;
}
