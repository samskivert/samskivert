//
// $Id: Model.java,v 1.1 2001/06/07 08:37:47 mdb Exp $

package robodj.repository;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import com.samskivert.util.IntMap;
import com.samskivert.util.IntIntMap;

/**
 * The model provides an interface to the contents of the repository which
 * efficiently accesses the database, caches information and preserves a
 * consistent view of the repository in light of modifications made by the
 * user (through the model). It is structured so that the client can
 * maintain no in-memory state and instead fetch content via the model
 * every time it needs to make use of it.
 */
public class Model
{
    /**
     * Constructs a new model instance from the supplied repository.
     */
    public Model (Repository rep)
        throws SQLException
    {
        _rep = rep;

        // load up our category information
        _cats = _rep.getCategories();

        // sort them into alphabetical order
        Arrays.sort(_cats, new Comparator() {
            public int compare (Object o1, Object o2)
            {
                return ((Category)o1).name.compareTo(((Category)o2).name);
            }

            public boolean equals (Object other)
            {
                return other == this;
            }
        });
    }

    /**
     * Returns the categories.
     */
    public Category[] getCategories ()
    {
        return _cats;
    }

    /**
     * Looks up the category id of the category with the specified name.
     *
     * @return category id of the matching category or -1 if no category
     * matched.
     */
    public int getCategoryId (String catname)
    {
        for (int i = 0; i < _cats.length; i++) {
            if (_cats[i].name.equals(catname)) {
                return _cats[i].categoryid;
            }
        }
        return -1;
    }

    /**
     * Returns a reference to the specified entry id. This call blocks
     * if database access takes place.
     */
    public Entry getEntry (int entryid)
        throws SQLException
    {
        Entry entry = (Entry)_entries.get(entryid);
        if (entry == null) {
            _entries.put(entryid, entry = _rep.getEntry(entryid));
        }
        return entry;
    }

    /**
     * Returns an array containing of the entries in the specified
     * category. This call blocks if database access takes place.
     */
    public Entry[] getEntries (int categoryid)
        throws SQLException
    {
        ArrayList catlist = getCatList(categoryid);
        Entry[] ents = new Entry[catlist.size()];
        catlist.toArray(ents);
        return ents;
    }

    /**
     * Associates the specified entry with the specified category. An
     * entry can be mapped into multiple categories.
     */
    public void associateEntry (Entry entry, int categoryid)
	throws SQLException
    {
        // keep our category mapping up to date
        ArrayList catlist = getCatList(categoryid);
        // if the entry's not already in the list, stick it in
        if (!catlist.contains(entry)) {
            catlist.add(entry);
            // update the entry to category map
            _entmap.put(entry.entryid, categoryid);
            // and update the database
            _rep.associateEntry(entry, categoryid);
        }
    }

    /**
     * Disassociates the specified entry from the specified category.
     */
    public void disassociateEntry (Entry entry, int categoryid)
	throws SQLException
    {
        // keep our category mapping up to date
        ArrayList catlist = getCatList(categoryid);
        // if the entry's already in the list, take it out
        if (catlist.contains(entry)) {
            catlist.remove(entry);
            // update the entry to category map
            _entmap.remove(entry.entryid);
            // and update the database
            _rep.disassociateEntry(entry, categoryid);
        }
    }

    /**
     * Returns the category to which this entry is mapped or -1 if it's
     * mapped to no category.
     */
    public int getCategory (int entryid)
    {
        return _entmap.get(entryid);
    }

    /**
     * Removes the entry from whatever category it is currently occupying
     * and adds it to the specified category. If the entry is already in
     * the specified category, this function does nothing. This is
     * temporary until we fully implement multi-category support.
     */
    public void recategorize (Entry entry, int categoryid)
        throws SQLException
    {
        // see if we need to change categories
        int curcatid = _entmap.get(entry.entryid);
        if (curcatid != categoryid) {
            disassociateEntry(entry, curcatid);
            associateEntry(entry, categoryid);
        }
    }

    /**
     * Ensures that the entry's songs array is populated. This call blocks
     * if database access takes place.
     */
    public void populateSongs (Entry entry)
        throws SQLException
    {
        _rep.populateSongs(entry);
    }

    protected ArrayList getCatList (int categoryid)
        throws SQLException
    {
        ArrayList catlist = (ArrayList)_catmap.get(categoryid);

        if (catlist == null) {
            // create a new category list and stick it into the table
            catlist = new ArrayList();
            _catmap.put(categoryid, catlist);

            // figure out which entries are in this category
            int[] entids = _rep.getEntryIds(categoryid);
            
            // create a select string with the eids that aren't already
            // loaded
            StringBuffer query = new StringBuffer();
            for (int i = 0; i < entids.length; i++) {
                // make sure the entry to category id mapping table
                // contains this information
                _entmap.put(entids[i], categoryid);

                // see if we've cached the entry
                Entry entry  = (Entry)_entries.get(entids[i]);
                if (entry != null) {
                    // if we have already loaded this entry, stick it into
                    // the category list
                    catlist.add(entry);

                } else {
                    // otherwise, append it to the query to be loaded
                    if (query.length() > 0) {
                        query.append(", ");
                    }
                    query.append(entids[i]);
                }
            }

            // load up these entries if we have any to load
            if (query.length() > 0) {
                query.insert(0, "where entryid in (");
                query.append(")");

                // now load up these entries
                Entry[] ents = _rep.getEntries(query.toString());
                for (int i = 0; i < ents.length; i++) {
                    // cache 'em
                    _entries.put(ents[i].entryid, ents[i]);
                    // and stick 'em in the category list
                    catlist.add(ents[i]);
                }
            }
        }

        return catlist;
    }

    protected Repository _rep;
    protected Category[] _cats;

    protected IntMap _entries = new IntMap();
    protected IntMap _catmap = new IntMap();
    protected IntIntMap _entmap = new IntIntMap();
}
