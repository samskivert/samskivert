//
// $Id: Entry.java,v 1.4 2002/02/22 07:06:33 mdb Exp $

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

    /**
     * Entry equality is based on entry id.
     */
    public boolean equals (Object other)
    {
        if (other instanceof Entry) {
            return entryid == ((Entry)other).entryid;
        } else {
            return false;
        }
    }

    /**
     * Scans the songs array for the song with the specified id and
     * returns it if found. Returns null otherwise.
     */
    public Song getSong (int songid)
    {
        for (int i = 0; i < songs.length; i++) {
            if (songs[i].songid == songid) {
                return songs[i];
            }
        }
        return null;
    }

    public String toString ()
    {
	return "[entryid=" + entryid + ", title=" + title +
	    ", artist=" + artist + ", source=" + source +
	    ", songs=" + StringUtil.toString(songs) + "]";
    }
}
