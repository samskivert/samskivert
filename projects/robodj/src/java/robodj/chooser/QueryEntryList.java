//
// $Id: QueryEntryList.java,v 1.1 2002/02/22 07:06:33 mdb Exp $

package robodj.chooser;

import com.samskivert.io.PersistenceException;

import robodj.repository.Entry;

public class QueryEntryList extends EntryList
{
    /**
     * Constructs an entry list to display entries that match a query.
     */
    public QueryEntryList (String query)
    {
        _query = query;
    }

    /**
     * Reads in the entries for this query.
     */
    public Entry[] readEntries ()
        throws PersistenceException
    {
        if (_entries == null) {
            _entries = Chooser.repository.matchEntries(_query);
        }
        return _entries;
    }

    protected String getEmptyString ()
    {
        return "No matches to your query.";
    }

    /** The query with which we're looking up entries. */
    protected String _query;

    /** A cached copy of our query results. */
    protected Entry[] _entries;
}
