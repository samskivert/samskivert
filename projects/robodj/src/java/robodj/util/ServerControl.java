//
// $Id: ServerControl.java,v 1.5 2002/03/03 17:21:29 mdb Exp $

package robodj.util;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.StringTokenizer;
import javax.swing.SwingUtilities;

import com.samskivert.util.StringUtil;

import robodj.Log;

/**
 * A simple class used to remotely control the music server through its
 * network interface.
 */
public class ServerControl
    implements Runnable
{
    /**
     * Used to report changes to the playing song.
     */
    public static interface PlayingListener
    {
        /**
         * Called when the playing song is known to have changed to the
         * specified songid.
         *
         * @param songid the id of the song that's playing or -1 if
         * nothing is playing.
         * @param paused true if the music daemon is paused.
         */
        public void playingUpdated (int songid, boolean paused);
    }

    public ServerControl (String host, int port)
        throws IOException
    {
        // create our server connection
        _conn = new Socket(host, port);
        // create our IO objects
        _in = new BufferedReader(new InputStreamReader(
            _conn.getInputStream()));
        _out = new PrintWriter(_conn.getOutputStream());
    }

    public void addPlayingListener (PlayingListener listener)
    {
        _listeners.add(listener);
    }

    public void removePlayingListener (PlayingListener listener)
    {
        _listeners.remove(listener);
    }

    public void pause ()
    {
        sendCommand("PAUSE");
        refreshPlaying();
    }

    public void play ()
    {
        sendCommand("PLAY");
        refreshPlaying();
    }

    public void stop ()
    {
        sendCommand("STOP");
        refreshPlaying();
    }

    public void clear ()
    {
        sendCommand("CLEAR");
    }

    public void back ()
    {
        sendCommand("BACK");
        refreshPlaying();
    }

    public void skip ()
    {
        sendCommand("SKIP");
        refreshPlaying();
    }

    public void append (int eid, int sid, String trackPath)
    {
        sendCommand("APPEND " + eid + " " + sid + " " + trackPath);
    }

    public void remove (int sid)
    {
        sendCommand("REMOVE " + sid);
        refreshPlaying();
    }

    public void removeGroup (int sid, int count)
    {
        sendCommand("REMOVEGRP " + sid + " " + count);
        refreshPlaying();
    }

    public void skipto (int sid)
    {
        sendCommand("SKIPTO " + sid);
        refreshPlaying();
    }

    public int getPlaying ()
    {
        refreshPlaying();
        return _playingSongId;
    }

    public void refreshPlaying ()
    {
        String playing = sendCommand("PLAYING");

        // figure out if we're paused
        _paused = (playing.indexOf("paused") != -1);

        // figure out what song is playing
        playing = StringUtil.split(playing, ":")[1].trim();
        _playingSongId = -1;
        if (!playing.equals("<none>")) {
            try {
                _playingSongId = Integer.parseInt(playing);
            } catch (NumberFormatException nfe) {
                Log.warning("Unable to parse currently playing id '" +
                            playing + "'.");
            }
        }

        // let our listeners know about the new info
        SwingUtilities.invokeLater(this);
    }

    public String[] getPlaylist ()
    {
        String result = sendCommand("PLAYLIST");
        ArrayList songs = new ArrayList();
        // parse the result string and then read the proper number of
        // playlist entries from the output
        if (!result.startsWith("200")) {
            return null;
        }

        // the result looks like this:
        // 200 Playlist songs: 9 current: /export/.../02.mp3
        StringTokenizer tok = new StringTokenizer(result);
        // skip the first three tokens to get to the actual count
        tok.nextToken(); tok.nextToken(); tok.nextToken();
        int count = 0;

        try {
            count = Integer.parseInt(tok.nextToken());
            for (int i = 0; i < count; i++) {
                songs.add(_in.readLine());
            }

        } catch (IOException ioe) {
            Log.warning("Error communicating with music server: " + ioe);
            return null;

        } catch (NumberFormatException nfe) {
            Log.warning("Bogus response from music server: " + result);
            return null;
        }

        String[] plist = new String[count];
        songs.toArray(plist);
        return plist;
    }

    public void run ()
    {
        // notify our playing listeners
        for (int i = 0; i < _listeners.size(); i++) {
            PlayingListener listener = (PlayingListener)_listeners.get(i);
            try {
                listener.playingUpdated(_playingSongId, _paused);
            } catch (Exception e) {
                Log.warning("PlayingListener choked during update " +
                            "[listener=" + listener +
                            ", songid=" + _playingSongId + "].");
                Log.logStackTrace(e);
            }
        }
    }

    protected String sendCommand (String command)
    {
        try {
            Log.info("Sending: " + command);
            _out.println(command);
            _out.flush();
            String rsp = _in.readLine();
            Log.info("Read response: " + rsp);
            return rsp;

        } catch (IOException ioe) {
            Log.warning("Error communicating with server: " + ioe);
            return null;
        }
    }

    protected Socket _conn;
    protected PrintWriter _out;
    protected BufferedReader _in;

    /** A list of entities that are informed when the playing song
     * changes. */
    protected ArrayList _listeners = new ArrayList();

    /** The most recently fetched playing song id. */
    protected int _playingSongId;

    /** The most recently fetched paused state. */
    protected boolean _paused;
}
