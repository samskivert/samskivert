//
// $Id: Repository.java,v 1.3 2001/03/18 06:58:55 mdb Exp $

package robodj.repository;

import java.sql.*;
import java.util.List;
import java.util.Properties;

import com.samskivert.jdbc.MySQLRepository;
import com.samskivert.jdbc.jora.*;
import com.samskivert.util.*;

/**
 * The repository class provides access to the music information
 * repository which contains information on all of the music registered
 * with the system (album names, track names, paths to the actual media,
 * etc.).
 *
 * <p> Entries are stored in the repository according to genre. An entry
 * may be associated with multiple genres.
 */
public class Repository extends MySQLRepository
{
    /**
     * Creates the repository and opens the music database. A properties
     * object should be supplied with the following fields:
     *
     * <pre>
     * driver=[jdbc driver class]
     * url=[jdbc driver url]
     * username=[jdbc username]
     * password=[jdbc password]
     * </pre>
     *
     * @param props a properties object containing the configuration
     * parameters for the repository.
     */
    public Repository (Properties props)
	throws SQLException
    {
	super(props);
    }

    protected void createTables ()
	throws SQLException
    {
	// create our table objects
	_etable = new Table(Entry.class.getName(), "entries", _session,
			    "entryid");
	_stable = new Table(Song.class.getName(), "songs", _session,
			    "songid");
	_ctable = new Table(Category.class.getName(), "categories", _session,
			    "categoryid");
	_cmtable = new Table(CategoryMapping.class.getName(), "category_map",
			     _session, new String[] {"categoryid", "entryid"});
    }

    /**
     * @return the entry with the specified entry id or null if no entry
     * with that id exists.
     */
    public Entry getEntry (int entryid)
	throws SQLException
    {
	// look up the entry
	Cursor ec = _etable.select("where entryid = " + entryid);

	// fetch the entry from the cursor
	Entry entry = (Entry)ec.next();

	if (entry != null) {
	    // and call next() again to cause the cursor to close itself
	    ec.next();

	    // load up the songs for this entry
	    Cursor sc = _stable.select("where entryid = " + entryid);
	    List slist = sc.toArrayList();
	    entry.songs = new Song[slist.size()];
	    slist.toArray(entry.songs);
	}

	return entry;
    }

    /**
     * Inserts a new entry into the table. All fields except the entryid
     * should contain valid values. The entryid field should be zero. The
     * songs array should contain song objects for all of the songs
     * associated with this entry. The entryid field (in the entry object
     * and the song objects) will be filled in with the entryid of the
     * newly created entry.
     */
    public void insertEntry (final Entry entry)
	throws SQLException
    {
	execute(new Operation () {
	    public void invoke () throws SQLException
	    {
                // insert the entry into the entry table
                _etable.insert(entry);

                // update the entryid now that it's known
                entry.entryid = lastInsertedId();

                // and insert all of it's songs into the songs table
                if (entry.songs != null) {
                    for (int i = 0; i < entry.songs.length; i++) {
                        // insert the proper entryid
                        entry.songs[i].entryid = entry.entryid;
                        _stable.insert(entry.songs[i]);
                        // find out what songid was assigned
                        entry.songs[i].songid = lastInsertedId();
                    }
                }
            }
        });
    }

    /**
     * Updates an entry that was previously fetched from the database. The
     * number of songs in the songs array <em>must not</em> have changed
     * (the fields of those songs can have changed, however). If you need
     * to add or remove songs, you should use the specific member
     * functions for doing that.
     *
     * @see addSongToEntry
     * @see removeSongFromEntry
     */
    public void updateEntry (final Entry entry)
	throws SQLException
    {
	execute(new Operation () {
	    public void invoke () throws SQLException
	    {
                // update the entry
                _etable.update(entry);

                // and the entry's songs
                if (entry.songs != null) {
                    for (int i = 0; i < entry.songs.length; i++) {
                        _stable.update(entry.songs[i]);
                    }
                }
            }
        });
    }

    /**
     * Removes the entry (and all associated songs) from the database.
     */
    public void deleteEntry (final Entry entry)
	throws SQLException
    {
	execute(new Operation () {
	    public void invoke () throws SQLException
	    {
                // remove the entry from the entry table and the foreign
                // key constraints will automatically remove all of the
                // corresponding songs
                _etable.delete(entry);
            }
        });
    }

    /**
     * Adds the specified song to the specified entry (both in the
     * database and in the current Java object that represents the
     * database information). The song should not have a value supplied
     * for the songid field because it is expected that this song will be
     * newly added to the database (use updateSong to update an existing
     * song's information). Upon successful return from this function, the
     * songid field will be filled in with the songid value assigned to
     * the newly created song.
     *
     * @see updateSong
     */
    public void addSongToEntry (final Entry entry, final Song song)
	throws SQLException
    {
	execute(new Operation () {
	    public void invoke () throws SQLException
	    {
                // fill in the appropriate entry id value
                song.entryid = entry.entryid;

                // and stick the song into the database
                _stable.insert(song);
                // communicate the songid back to the caller
                song.songid = lastInsertedId();
            }
        });
    }

    /**
     * Updates the specified song individually. The songid and entryid
     * parameters should already be set to the appropriate values.
     */
    public void updateSong (final Song song)
	throws SQLException
    {
	execute(new Operation () {
	    public void invoke () throws SQLException
	    {
                _stable.update(song);
            }
        });
    }

    /**
     * Creates a category with the specified name and returns the id of
     * that new category.
     */
    public int createCategory (final String name)
	throws SQLException
    {
	execute(new Operation () {
	    public void invoke () throws SQLException
	    {
                Category cat = new Category();
                cat.name = name;
                _ctable.insert(cat);
            }
        });

        return lastInsertedId();
    }

    /**
     * @return an array of all of the categories that currently exist in
     * the category table.
     */
    public Category[] getCategories ()
	throws SQLException
    {
	Cursor ccur = _ctable.select("");
	List clist = ccur.toArrayList();
	Category[] cats = new Category[clist.size()];
	clist.toArray(cats);
	return cats;
    }

    /**
     * Associates the specified entry with the specified category. An
     * entry can be mapped into multiple categories.
     */
    public void associateEntry (Entry entry, int categoryid)
	throws SQLException
    {
	try {
	    CategoryMapping map =
		new CategoryMapping(categoryid, entry.entryid);
	    _cmtable.insert(map);
	    _session.commit();

	} catch (SQLException sqe) {
	    // back out our changes if something got hosed
	    _session.rollback();
	    throw sqe;

	} catch (RuntimeException rte) {
	    // back out our changes if something got hosed
	    _session.rollback();
	    throw rte;
	}
    }

    /**
     * Disassociates the specified entry from the specified category.
     */
    public void disassociateEntry (Entry entry, int categoryid)
	throws SQLException
    {
	try {
	    CategoryMapping map =
		new CategoryMapping(categoryid, entry.entryid);
	    _cmtable.delete(map);
	    _session.commit();

	} catch (SQLException sqe) {
	    // back out our changes if something got hosed
	    _session.rollback();
	    throw sqe;

	} catch (RuntimeException rte) {
	    // back out our changes if something got hosed
	    _session.rollback();
	    throw rte;
	}
    }

    protected Table _etable;
    protected Table _stable;
    protected Table _ctable;
    protected Table _cmtable;
}
