//
// $Id: CategoryEntryList.java,v 1.1 2002/02/22 07:06:33 mdb Exp $

package robodj.chooser;

import com.samskivert.io.PersistenceException;

import robodj.repository.Entry;

public class CategoryEntryList extends EntryList
{
    /**
     * Constructs an entry list for entries in a particular category.
     */
    public CategoryEntryList (int categoryId)
    {
        _categoryId = categoryId;
    }

    /**
     * Reads in the entries for this category.
     */
    public Entry[] readEntries ()
        throws PersistenceException
    {
        return Chooser.model.getEntries(_categoryId);
    }

    protected String getEmptyString ()
    {
        return "No entries in this category.";
    }

    /** The unique identifier of the category we are displaying. */
    protected int _categoryId;
}
