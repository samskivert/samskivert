//
// $Id: Repository.java,v 1.5 2001/06/07 08:37:47 mdb Exp $

package robodj.repository;

import java.sql.*;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import com.samskivert.jdbc.MySQLRepository;
import com.samskivert.jdbc.JDBCUtil;
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
	_ctable = new Table(Category.class.getName(), "category_names",
                            _session, "categoryid");
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
        // make sure the connection is open
        ensureConnection();

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
     * @param query a WHERE clause describing which entries should be
     * selected (ie. 'where title like "%foo%"').
     *
     * @return all entries in the table matching the query
     * parameters. <em>Note</em>: this member function does not populate
     * the songs arrays of the returned entries. Call
     * <code>populateSongs</code> on an entry by entry basis for that.
     */
    public Entry[] getEntries (final String query)
	throws SQLException
    {
	return (Entry[])execute(new Operation() {
	    public Object invoke () throws SQLException
	    {
                // look up the entry
                Cursor ec = _etable.select(query);

                // fetch the entries from the cursor
                List elist = ec.toArrayList();
                Entry[] entries = new Entry[elist.size()];
                elist.toArray(entries);

                return entries;
            }
        });
    }

    /**
     * Loads the songs for this entry from the database and inserts them
     * into its songs array. If the songs are already loaded, this
     * function does nothing.
     */
    public void populateSongs (final Entry entry)
	throws SQLException
    {
        if (entry.songs == null) {
            execute(new Operation() {
                public Object invoke () throws SQLException
                {
                    // load up the songs for this entry
                    String query = "where entryid = " + entry.entryid;
                    Cursor sc = _stable.select(query);
                    List slist = sc.toArrayList();
                    entry.songs = new Song[slist.size()];
                    slist.toArray(entry.songs);
                    return null;
                }
            });
        }
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
	execute(new Operation() {
	    public Object invoke () throws SQLException
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

                return null;
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
	execute(new Operation() {
	    public Object invoke () throws SQLException
	    {
                // update the entry
                _etable.update(entry);

                // and the entry's songs
                if (entry.songs != null) {
                    for (int i = 0; i < entry.songs.length; i++) {
                        _stable.update(entry.songs[i]);
                    }
                }

                return null;
            }
        });
    }

    /**
     * Removes the entry (and all associated songs) from the database.
     */
    public void deleteEntry (final Entry entry)
	throws SQLException
    {
	execute(new Operation() {
	    public Object invoke () throws SQLException
	    {
                // remove the entry from the entry table and the foreign
                // key constraints will automatically remove all of the
                // corresponding songs
                _etable.delete(entry);
                return null;
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
	execute(new Operation() {
	    public Object invoke () throws SQLException
	    {
                // fill in the appropriate entry id value
                song.entryid = entry.entryid;

                // and stick the song into the database
                _stable.insert(song);
                // communicate the songid back to the caller
                song.songid = lastInsertedId();

                return null;
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
	execute(new Operation() {
	    public Object invoke () throws SQLException
	    {
                _stable.update(song);
                return null;
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
	execute(new Operation() {
	    public Object invoke () throws SQLException
	    {
                Category cat = new Category();
                cat.name = name;
                _ctable.insert(cat);
                return null;
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
	return (Category[])execute(new Operation() {
	    public Object invoke () throws SQLException
	    {
                Cursor ccur = _ctable.select("");
                List clist = ccur.toArrayList();
                Category[] cats = new Category[clist.size()];
                clist.toArray(cats);
                return cats;
            }
        });
    }

    /**
     * Fetches all of the entry ids that are associated with this category
     * and returns them. If <code>categoryid</code> is -1, it instead
     * returns all of the entry ids that are not in any category.
     */
    public int[] getEntryIds (final int categoryid)
        throws SQLException
    {
        return (int[])execute(new Operation() {
            public Object invoke () throws SQLException
            {
                if (categoryid >= 0) {
                    String query = "where categoryid=" + categoryid;
                    Cursor cmcur = _cmtable.select(query);
                    List cmlist = cmcur.toArrayList();
                    int[] eids = new int[cmlist.size()];
                    for (int i = 0; i < cmlist.size(); i++) {
                        eids[i] = ((CategoryMapping)cmlist.get(i)).entryid;
                    }
                    return eids;

                } else {
                    // i wish i knew how to do this sort of jockeying in
                    // SQL, but I can't be bothered to sort it out for a
                    // data set that isn't going to be so big that it
                    // can't be done in Java
                    IntMap ids = new IntMap();
                    Statement stmt = _session.connection.createStatement();
                    try {
                        // first add all the entryids in the repository
                        String query = "select entryid from entries";
                        ResultSet rs = stmt.executeQuery(query);
                        while (rs.next()) {
                            ids.put(rs.getInt(1), "");
                        }

                        // now remove those that are mapped to a category
                        query = "select entryid from category_map";
                        rs = stmt.executeQuery(query);
                        while (rs.next()) {
                            ids.remove(rs.getInt(1));
                        }

                    } finally {
                        JDBCUtil.close(stmt);
                    }

                    int[] eids = new int[ids.size()];
                    Enumeration keys = ids.keys();
                    for (int i = 0; keys.hasMoreElements(); i++) {
                        eids[i] = ((Integer)keys.nextElement()).intValue();
                    }
                    return eids;
                }
            }
        });
    }

    /**
     * Associates the specified entry with the specified category. An
     * entry can be mapped into multiple categories.
     */
    public void associateEntry (final Entry entry, final int categoryid)
	throws SQLException
    {
        execute(new Operation() {
            public Object invoke () throws SQLException
            {
                CategoryMapping map =
                    new CategoryMapping(categoryid, entry.entryid);
                _cmtable.insert(map);
                return null;
            }
        });
    }

    /**
     * Disassociates the specified entry from the specified category.
     */
    public void disassociateEntry (final Entry entry, final int categoryid)
	throws SQLException
    {
        execute(new Operation() {
            public Object invoke () throws SQLException
            {
                CategoryMapping map =
                    new CategoryMapping(categoryid, entry.entryid);
                _cmtable.delete(map);
                return null;
            }
        });
    }

    protected Table _etable;
    protected Table _stable;
    protected Table _ctable;
    protected Table _cmtable;
}
