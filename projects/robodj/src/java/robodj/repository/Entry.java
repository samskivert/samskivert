//
// $Id: Entry.java,v 1.2 2001/06/07 08:37:47 mdb Exp $

package robodj.repository;

import com.samskivert.util.StringUtil;

/**
 * The repository contains entries which represent music taken from any of
 * a variety of sources (CDs, CD singles, downloaded from the Internet,
 * stored remotely on the net, etc.). An entry is a collection of songs
 * with a title and some other metadata.
 */
public class Entry
{
    /** The unique identifier for this entry. */
    public int entryid;

    /** The title of this entry. */
    public String title;

    /**
     * The artist that created the music for this entry or "Various
     * Artists" if it is not a single-artist work.
     */
    public String artist;

    /**
     * The source media for this entry (CD, record, tape, Internet, etc.).
     */
    public String source;

    /** The songs associated with this entry. */
    public transient Song[] songs;

    public String toString ()
    {
	return "[entryid=" + entryid + ", title=" + title +
	    ", artist=" + artist + ", source=" + source +
	    ", songs=" + StringUtil.toString(songs) + "]";
    }
}
