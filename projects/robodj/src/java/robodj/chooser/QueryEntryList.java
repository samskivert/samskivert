//
// $Id: QueryEntryList.java,v 1.2 2003/05/04 18:16:06 mdb Exp $

package robodj.chooser;

import com.samskivert.io.PersistenceException;
import com.samskivert.swing.Controller;

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

    // documentation inherited
    protected Controller createController ()
    {
        return new EntryController(this) {
            public Entry[] readEntries () throws PersistenceException {
                if (_entries == null) {
                    _entries = Chooser.repository.matchEntries(_query);
                }
                return _entries;
            }
        };
    }

    protected String getEmptyString ()
    {
        return "No matches to your query.";
    }

    /** The query with which we're looking up entries. */
    protected String _query;
}
