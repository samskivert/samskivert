//
// $Id: ServerControl.java,v 1.4 2002/02/22 08:37:34 mdb Exp $

package robodj.util;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.StringTokenizer;

import robodj.Log;

/**
 * A simple class used to remotely control the music server through its
 * network interface.
 */
public class ServerControl
{
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

    public void pause ()
    {
        sendCommand("PAUSE");
    }

    public void play ()
    {
        sendCommand("PLAY");
    }

    public void stop ()
    {
        sendCommand("STOP");
    }

    public void clear ()
    {
        sendCommand("CLEAR");
    }

    public void skip ()
    {
        sendCommand("SKIP");
    }

    public void append (int eid, int sid, String trackPath)
    {
        sendCommand("APPEND " + eid + " " + sid + " " + trackPath);
    }

    public void remove (int sid)
    {
        sendCommand("REMOVE " + sid);
    }

    public void removeGroup (int sid, int count)
    {
        sendCommand("REMOVEGRP " + sid + " " + count);
    }

    public void skipto (int sid)
    {
        sendCommand("SKIPTO " + sid);
    }

    public String getPlaying ()
    {
        return sendCommand("PLAYING");
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
}
