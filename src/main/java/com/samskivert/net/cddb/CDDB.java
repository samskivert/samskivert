//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.net.cddb;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

/**
 * The CDDB class provides access to the information provided by servers compliant with the
 * <a href="http://www.freedb.org/software/old/CDDBPROTO">CDDB protocol</a>.
 */
public class CDDB
{
    /**
     * The port on which CDDB servers are generally run.
     */
    public static final int STANDARD_PORT = 888;

    /**
     * The client name reported to the CDDB server.
     */
    public static final String CLIENT_NAME = "TSP/CDDB_client";

    /**
     * The client version reported to the CDDB server.
     */
    public static final String CLIENT_VERSION = "1.0";

    /**
     * This class encapsulates the information needed to look up a full CDDB record for a
     * particular disc. An array of them are returned in response to a CDDB query.
     */
    public static class Entry
    {
        /** The category to which this entry belongs. */
        public String category;

        /** The unique identifier for this entry. */
        public String cdid;

        /** The human readable title of this entry. */
        public String title;

        /**
         * Parses values for this entry from the supplied source string. The source string should
         * contain an entry description as formatted by the CDDB server.
         *
         * @exception CDDBException Thrown if the entry is not properly formatted.
         */
        public void parse (String source)
            throws CDDBException
        {
            int sidx1 = source.indexOf(" ");
            int sidx2 = source.indexOf(" ", sidx1+1);

            if (sidx1 == -1 || sidx2 == -1) {
                throw new CDDBException(499, "Malformed entry '" + source + "'");
            }

            category = source.substring(0, sidx1);
            cdid = source.substring(sidx1+1, sidx2);
            title = source.substring(sidx2+1);
        }
    }

    /**
     * Connects this CDDB instance to the CDDB server running on the supplied host using the
     * standard port, name and version.
     *
     * <p> <b>Note well:</b> you must close a CDDB connection once you are done with it, otherwise
     * the socket connection will remain open and pointlessly consume machine resources.
     *
     * @param hostname The host to which to connect.
     *
     * @exception IOException Thrown if a network error occurs attempting to connect to the host.
     * @exception CDDBException Thrown if an error occurs after identifying ourselves to the host.
     *
     * @return The message supplied with the succesful connection response.
     *
     * @see #STANDARD_PORT
     * @see #close
     */
    public String connect (String hostname)
        throws IOException, CDDBException
    {
        return connect(hostname, STANDARD_PORT);
    }

    /**
     * Connects this CDDB instance to the CDDB server running on the supplied host using the
     * specified port and default client name and version.
     *
     * <p> <b>Note well:</b> you must close a CDDB connection once you are done with it, otherwise
     * the socket connection will remain open and pointlessly consume machine resources.
     *
     * @param hostname The host to which to connect.
     * @param port The port number on which to connect to the host.
     *
     * @exception IOException Thrown if a network error occurs attempting to connect to the host.
     * @exception CDDBException Thrown if an error occurs after identifying ourselves to the host.
     *
     * @return The message supplied with the succesful connection response.
     *
     * @see #close
     */
    public String connect (String hostname, int port)
        throws IOException, CDDBException
    {
        return connect(hostname, port, CLIENT_NAME, CLIENT_VERSION);
    }

    /**
     * Connects this CDDB instance to the CDDB server running on the supplied host using the
     * specified port.
     *
     * <p> <b>Note well:</b> you must close a CDDB connection once you are done with it, otherwise
     * the socket connection will remain open and pointlessly consume machine resources.
     *
     * @param hostname The host to which to connect.
     * @param port The port number on which to connect to the host.
     * @param clientName The name of the application establishing this connection.
     * @param clientVersion The version of the application establishing this connection.
     *
     * @exception IOException Thrown if a network error occurs attempting to connect to the host.
     * @exception CDDBException Thrown if an error occurs after identifying ourselves to the host.
     *
     * @return The message supplied with the succesful connection response.
     *
     * @see #close
     */
    public String connect (String hostname, int port, String clientName, String clientVersion)
        throws IOException, CDDBException
    {
        // obtain the necessary information we'll need to identify
        // ourselves to the CDDB server
        String localhost = InetAddress.getLocalHost().getHostName();
        String username = System.getProperty("user.name");
        if (username == null) {
            username = "anonymous";
        }

        // establish our socket connection and IO streams
        InetAddress addr = InetAddress.getByName(hostname);
        _sock = new Socket(addr, port);
        _in = new BufferedReader(new InputStreamReader(_sock.getInputStream(), ISO8859));
        _out = new PrintStream(new BufferedOutputStream(_sock.getOutputStream()), false, ISO8859);

        // first read (and discard) the banner string
        _in.readLine();

        // try and change to the latest protocol version
        Response rsp = request("proto 6");
        if (rsp.code == 201 || rsp.code == 501) {
            // Protocol 6 uses UTF-8
            _in = new BufferedReader(new InputStreamReader(_sock.getInputStream(), UTF8));
            _out = new PrintStream(new BufferedOutputStream(_sock.getOutputStream()), false, UTF8);
        }

        // send a hello request
        StringBuilder req = new StringBuilder("cddb hello ");
        req.append(username).append(" ");
        req.append(localhost).append(" ");
        req.append(clientName).append(" ");
        req.append(clientVersion);

        rsp = request(req.toString());

        // confirm that the response was a successful one
        if (CDDBProtocol.codeFamily(rsp.code) != CDDBProtocol.OK &&
            rsp.code != 402 /* already shook hands */) {
            throw new CDDBException(rsp.code, rsp.message);
        }

        return rsp.message;
    }

    /**
     * Specifies the number of milliseconds that the client should wait for a response from the
     * server before aborting. This must be called <em>after</em> a successful call to connect,
     * otherwise the timeout will not be set.
     */
    public void setTimeout (int timeout)
        throws SocketException
    {
        if (_sock != null) {
            _sock.setSoTimeout(timeout);
        }
    }

    /**
     * @return true if this CDDB instance is connected to a CDDB server.
     */
    public boolean connected ()
    {
        return _sock != null;
    }

    /**
     * Closes the connection to the CDDB server previously opened with a call to {@link #connect}.
     * If the connection was not previously established, this member function does nothing.
     */
    public void close ()
        throws IOException
    {
        if (_sock != null) {
            _sock.close();

            // clear out our references
            _sock = null;
            _in = null;
            _out = null;
        }
    }

    /**
     * Fetches and returns the list of categories supported by the server.
     *
     * @throws IOException if a problem occurs chatting to the server.
     * @throws CDDBException if the server responds with an error.
     */
    public String[] lscat()
        throws IOException, CDDBException
    {
        // sanity check
        if (_sock == null) {
            throw new CDDBException(500, "Not connected");
        }

        // make the request
        Response rsp = request("cddb lscat");

        // anything other than an OK response earns an exception
        if (rsp.code != 210) {
            throw new CDDBException(rsp.code, rsp.message);
        }

        ArrayList<String> list = new ArrayList<String>();
        String input;
        while (!(input = _in.readLine()).equals(CDDBProtocol.TERMINATOR)) {
            list.add(input);
        }

        String[] categories = new String[list.size()];
        list.toArray(categories);
        return categories;
    }

    /**
     * Issues a query to the CDDB server using the supplied CD identifying information.
     *
     * @param discid The disc identifier (information on how to compute the disc ID is available <a
     * href="http://www.freedb.org/sections.php?op=viewarticle&artid=6"> here</a>.
     * @param frameOffsets The frame offset of each track. The length of this array is assumed to
     * be the number of tracks on the CD and is used in the query.
     * @param length The length (in seconds) of the CD.
     *
     * @return If no entry matches the query, null is returned. Otherwise one or more entries is
     * returned that matched the query parameters.
     */
    public Entry[] query (String discid, int[] frameOffsets, int length)
        throws IOException, CDDBException
    {
        // sanity check
        if (_sock == null) {
            throw new CDDBException(500, "Not connected");
        }

        // construct the query parameter
        StringBuilder req = new StringBuilder("cddb query ");
        req.append(discid).append(" ");
        req.append(frameOffsets.length).append(" ");
        for (int frameOffset : frameOffsets) {
            req.append(frameOffset).append(" ");
        }
        req.append(length);

        // make the request
        Response rsp = request(req.toString());
        Entry[] entries = null;

        // if this is an exact match, parse the entry and return it
        if (rsp.code == 200 /* exact match */) {
            entries = new Entry[1];
            entries[0] = new Entry();
            entries[0].parse(rsp.message);

        } else if (rsp.code == 211 /* inexact matches */) {
            // read the matches from the server
            ArrayList<Entry> list = new ArrayList<Entry>();
            String input = _in.readLine();
            while (input != null && !input.equals(CDDBProtocol.TERMINATOR)) {
                System.out.println("...: " + input);
                Entry e = new Entry();
                e.parse(input);
                list.add(e);
                input = _in.readLine();
            }
            entries = new Entry[list.size()];
            list.toArray(entries);

        } else if (CDDBProtocol.codeFamily(rsp.code) != CDDBProtocol.OK) {
            throw new CDDBException(rsp.code, rsp.message);
        }

        return entries;
    }

    /**
     * A detail object contains all of the detailed information about a particular CD as retrieved
     * from the CDDB server.
     */
    public static class Detail
    {
        /** The unique identifier for this CD. */
        public String discid;

        /** The category to which this CD belongs. */
        public String category;

        /** The title of this CD. */
        public String title;

        /** The year the CD was published (or -1 if not known). */
        public int year = -1;

        /** The genre of this CD. */
        public String genre;

        /** The track names. */
        public String[] trackNames;

        /** The extended data for the CD. */
        public String extendedData;

        /** The extended data for each track. */
        public String[] extendedTrackData;
    }

    /**
     * Requests the detail information for a particular disc in a particular category from the CDDB
     * server.
     *
     * @return A detail instance filled with the information for the requested CD.
     */
    public Detail read (String category, String discid)
        throws IOException, CDDBException
    {
        // sanity check
        if (_sock == null) {
            throw new CDDBException(500, "Not connected");
        }

        // construct the query
        StringBuilder req = new StringBuilder("cddb read ");
        req.append(category).append(" ");
        req.append(discid);

        // make the request
        Response rsp = request(req.toString());
        // anything other than an OK response earns an exception
        if (rsp.code != 210 /* OK, entry follows */) {
            throw new CDDBException(rsp.code, rsp.message);
        }

        Detail detail = new Detail();

        // parse the category and discid from the response string
        int sidx = rsp.message.indexOf(" ");
        if (sidx == -1) {
            throw new CDDBException(500, "Malformed read response: " +
                                    rsp.message);
        }
        detail.category = rsp.message.substring(0, sidx);
        detail.discid = rsp.message.substring(sidx+1);

        ArrayList<String> tnames = new ArrayList<String>();
        ArrayList<String> texts = new ArrayList<String>();

        // now parse the contents
        String input = _in.readLine();
        for (int lno = 0; !input.equals(CDDBProtocol.TERMINATOR); lno++) {
            if (input.startsWith("#")) {
                // skip comments

            } else if (input.startsWith("DTITLE")) {
                if (detail.title == null) {
                    detail.title = contents(input, lno);
                } else {
                    detail.title += contents(input, lno);
                }

            } else if (input.startsWith("DYEAR")) {
                String yearStr = contents(input, lno);
                try {
                    detail.year = Integer.parseInt(yearStr);
                } catch (NumberFormatException nfEx) {
                    detail.year = -1;
                }

            } else if (input.startsWith("DGENRE")) {
                if (detail.genre == null) {
                    detail.genre = contents(input, lno);
                } else {
                    detail.genre += contents(input, lno);
                }

            } else if (input.startsWith("EXTD")) {
                if (detail.extendedData == null) {
                    detail.extendedData = contents(input, lno);
                } else {
                    detail.extendedData += contents(input, lno);
                }

            } else if (input.startsWith("TTITLE")) {
                append(tnames, index(input, "TTITLE", lno), contents(input, lno));

            } else if (input.startsWith("EXTT")) {
                append(texts, index(input, "EXTT", lno), contents(input, lno));
            }

            // read in the next line of input
            input = _in.readLine();
        }

        // convert the lists into arrays
        detail.trackNames = new String[tnames.size()];
        tnames.toArray(detail.trackNames);
        detail.extendedTrackData = new String[texts.size()];
        texts.toArray(detail.extendedTrackData);

        return detail;
    }

    /**
     * Extracts the track index of the supplied line (the number immediately following the supplied
     * prefix and preceding the equals sign) or throws a CDDBException if the line contains no
     * equals sign or track index.
     */
    protected final int index (String source, String prefix, int lineno)
        throws CDDBException
    {
        int eidx = source.indexOf("=", prefix.length());
        if (eidx == -1) {
            throw new CDDBException(500, "Malformed line '" + source + "' number " + lineno);
        }

        try {
            return Integer.parseInt(source.substring(prefix.length(), eidx));
        } catch (NumberFormatException nfe) {
            throw new CDDBException(500, "Malformed line '" + source + "' number " + lineno);
        }
    }

    /**
     * Extracts the contents of the supplied line (everything after the equals sign) or throws a
     * CDDBException if the line contains no equals sign.
     */
    protected final String contents (String source, int lineno)
        throws CDDBException
    {
        int eidx = source.indexOf("=");
        if (eidx == -1) {
            throw new CDDBException(500, "Malformed line '" + source + "' number " + lineno);
        }
        return source.substring(eidx+1);
    }

    /**
     * Appends the supplied string to the contents of the list at the supplied index. If the list
     * has no contents at the supplied index, the supplied value becomes the contents at that
     * index.
     */
    protected final void append (
        ArrayList<String> list, int index, String value)
    {
        // expand the list as necessary
        while (list.size() <= index) {
            list.add("");
        }
        list.set(index, list.get(index) + value);
    }

    /**
     * Issues a request to the CDDB server and parses the response.
     */
    protected Response request (String req)
        throws IOException
    {
        System.err.println("REQ:" + req);

        // send the request to the server
        _out.println(req);
        _out.flush();

        // now read the response
        String rspstr = _in.readLine();
        // if they closed the connection on us, we should deal
        if (rspstr == null) {
            throw new EOFException();
        }
        System.err.println("RSP:" + rspstr);

        // parse the response
        Response rsp = new Response();
        String codestr = rspstr;

        // assume the response is just a code unless we see a space
        int sidx = rspstr.indexOf(" ");
        if (sidx != -1) {
            codestr = rspstr.substring(0, sidx);
            rsp.message = rspstr.substring(sidx+1);
        }

        try {
            rsp.code = Integer.parseInt(codestr);
        } catch (NumberFormatException nfe) {
            rsp.code = 599;
            rsp.message = "Unparseable response";
        }

        return rsp;
    }

    /**
     * A simple class to encapsulate the response from the CDDB server.
     */
    protected static class Response
    {
        public int code;
        public String message;
    }

    protected Socket _sock;
    protected BufferedReader _in;
    protected PrintStream _out;

    protected static final String ISO8859 = "ISO-8859-1", UTF8 = "UTF-8";
}
