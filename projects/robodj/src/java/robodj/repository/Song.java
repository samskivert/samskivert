//
// $Id: Song.java,v 1.1 2000/11/08 06:42:57 mdb Exp $

package robodj.repository;

/**
 * A song maps approximately to an individual piece of music. In most
 * cases repository entries are created from albums which contain some
 * number of songs. Some special cases involve mixed CDs with one big song
 * or streaming Internet music sources. In these cases, the entry tends to
 * be comprised of a single song which represents the entire contents of
 * the entry.
 */
public class Song
{
    /** The unique identifier for this song. */
    public int songid;

    /** The unique identifier of the entry to which this song maps. */
    public int entryid;

    /**
     * The position of this song on the media (counting from 1 since
     * that's what people do on the actual media). I'd call it index but
     * that's a reserved word in SQL.
     */
    public int position;

    /** The title of this song. */
    public String title;

    /** The location of the media for this song. */
    public String location;

    /**
     * The duration of this song (in seconds) or -1 if the duration is
     * unknown.
     */
    public int duration;

    public String toString ()
    {
	return "[songid=" + songid + ", entryid=" + entryid +
	    ", position=" + position + ", title=" + title +
	    ", location=" + location + ", duration=" + duration + "]";
    }
}
