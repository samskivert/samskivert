//
// $Id$

package robodj.data;

import com.threerings.io.SimpleStreamableObject;
import com.threerings.presents.dobj.DSet;

import robodj.repository.Entry;
import robodj.repository.Song;

/**
 * Defines an entry currently in the playlist.
 */
public class PlaylistEntry extends SimpleStreamableObject
    implements DSet.Entry
{
    /** The album with which we are associated. */
    public int entryId;

    /** The id of the song represented by this entry. */
    public int songId;

    /** The title of this entry (duplicated from the Song to simplify
     * playlist display. */
    public String title;

    /** Zero argument constructor for streaming. */
    public PlaylistEntry ()
    {
    }

    /** Creates a playlist entry from the specified entry and song. */
    public PlaylistEntry (Entry entry, Song song)
    {
        this(entry.entryid, song.songid, song.title);
    }

    /** Creates a playlist entry from the specified information. */
    public PlaylistEntry (int entryId, int songId, String title)
    {
        this.entryId = entryId;
        this.songId = songId;
        this.title = title;
        _key = entryId + ":" + songId;
    }

    // documentation inherited
    public Comparable getKey ()
    {
        return _key;
    }

    // documentation inherited
    public boolean equals (Object other)
    {
        if (other instanceof PlaylistEntry) {
            return _key.equals(((PlaylistEntry)other)._key);
        } else {
            return false;
        }
    }

    // documentation inherited
    public int hashCode ()
    {
        return _key.hashCode();
    }

    protected String _key;
}
