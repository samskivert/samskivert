//
// $Id$

package robodj.importer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.table.AbstractTableModel;

import com.samskivert.util.HashIntMap;
import com.samskivert.util.QuickSort;

import robodj.repository.Entry;
import robodj.repository.Song;

/**
 * Allows the display and editing of a big list of songs.
 */
public class SongTableModel extends AbstractTableModel
{
    /**
     * Creates a song table model with the supplied set of entries and
     * songs.
     */
    public SongTableModel (HashIntMap byid, HashMap byname, ArrayList songs)
    {
        _byid = byid;
        _byname = byname;
        _songs = songs;
        QuickSort.sort(_songs, SONG_COMP);
    }

    // documentation inherited
    public Class getColumnClass (int column)
    {
        return CLASSES[column];
    }

    // documentation inherited
    public String getColumnName (int column)
    {
        return COLUMNS[column];
    }

    // documentation inherited
    public int getRowCount()
    {
        return _songs.size();
    }

    // documentation inherited
    public int getColumnCount()
    {
        return COLUMNS.length;
    }

    // documentation inherited
    public Object getValueAt (int row, int column) {
        Song song = (Song)_songs.get(row);
        Entry entry = (Entry)_byid.get(song.entryid);

        switch (column) {
        case 0: return entry.artist;
        case 1: return entry.title;
        case 2: return song.title;
        case 3: return new Integer(song.position);
        default: return "<error:" + column + ">";
        }
    }

    // documentation inherited
    public boolean isCellEditable (int row, int column) {
	return true;
    }

    // documentation inherited
    public void setValueAt (Object value, int row, int column) {
        Song song = (Song)_songs.get(row);

        switch (column) {
        case 0: remapSong(song, null, (String)value); break;
        case 1: remapSong(song, (String)value, null); break;
        case 2: song.title = (String)value; return; // avoid fire..Changed
        case 3: song.position = ((Integer)value).intValue(); break;
        default:
            System.err.println("Refusing to update bogus column [row=" + row +
                               ", column=" + column + ", val=" + value + "].");
            break;
        }

        QuickSort.sort(_songs, SONG_COMP);
        fireTableDataChanged();
    }

    /** Updates the album title or artist for the supplied song. */
    protected void remapSong (Song song, String album, String artist)
    {
        Entry entry = (Entry)_byid.get(song.entryid);

        // fill in any unknown data
        if (album == null) {
            album = entry.title;
        }
        if (artist == null) {
            artist = entry.artist;
        }
        String key = getKey(album, artist);
        System.err.println("Considering " + key);

        // if neither album or artist are "Unknown", and the new name does
        // not already exist as an entry, then just update this entry
        if (!entry.title.equals("Unknown") && !entry.artist.equals("Unknown") &&
            !_byname.containsKey(key)) {
            _byname.remove(getKey(entry)); // remove the old key
            entry.title = (album == null) ? entry.title : album;
            entry.artist = (artist == null) ? entry.artist : artist;
            _byname.put(key, entry); // map with the new key
            return;
        }

        // look up (or create) a new entry
        Entry nentry = (Entry)_byname.get(key);
        if (nentry == null) {
            nentry = clone(entry);
            nentry.title = album;
            nentry.artist = artist;
            nentry.entryid = _byname.size();
            _byname.put(getKey(nentry), nentry);
            _byid.put(nentry.entryid, nentry);
        }
        song.entryid = nentry.entryid;
        System.err.println("Remapped " + song);
    }

    /** Duplicates the supplied entry. */
    protected Entry clone (Entry source)
    {
        Entry nentry = new Entry();
        nentry.title = source.title;
        nentry.artist = source.artist;
        nentry.source = source.source;
        return nentry;
    }

    /** Creates a key from the entry title and artist. */
    protected static String getKey (Entry entry)
    {
        return getKey(entry.title, entry.artist);
    }

    /** Creates a key from the entry title and artist. */
    protected static String getKey (String title, String artist)
    {
        return title + ":" + artist;
    }

    protected HashMap _byname = new HashMap();
    protected HashIntMap _byid = new HashIntMap();
    protected ArrayList _songs;

    protected Comparator SONG_COMP = new Comparator() {
        public int compare (Object a, Object b) {
            Song sa = (Song)a, sb = (Song)b;
            Entry ea = (Entry)_byid.get(sa.entryid);
            Entry eb = (Entry)_byid.get(sb.entryid);
            int comp;
            if ((comp = ea.artist.compareTo(eb.artist)) != 0) {
                return comp;
            }
            if ((comp = ea.title.compareTo(eb.title)) != 0) {
                return comp;
            }
            if (sa.position != sb.position) {
                return sa.position - sb.position;
            }
            return sa.title.compareTo(sb.title);
        }
    };

    protected static final String[] COLUMNS = {
        "Artist", "Album", "Title", "#"
    };

    protected static final Class[] CLASSES = {
        String.class, String.class, String.class, Integer.class
    };
}
