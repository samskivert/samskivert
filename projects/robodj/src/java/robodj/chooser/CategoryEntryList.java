//
// $Id: CategoryEntryList.java,v 1.3 2004/01/26 16:10:55 mdb Exp $

package robodj.chooser;

import com.samskivert.io.PersistenceException;
import com.samskivert.swing.Controller;

import robodj.repository.Entry;

public class CategoryEntryList extends EntryList
{
    /**
     * Constructs an entry list for entries in a particular category.
     */
    public CategoryEntryList (int categoryId)
    {
        setCategory(categoryId);
    }

    /**
     * Updates our category.
     */
    public void setCategory (int categoryId)
    {
        if (categoryId != _categoryId) {
            _categoryId = categoryId;
            Controller.postAction(this, "refresh");
        }
    }

    // documentation inherited
    protected Controller createController ()
    {
        return new EntryController(this) {
            public Entry[] readEntries () throws PersistenceException {
                return Chooser.model.getEntries(_categoryId);
            }
        };
    }

    protected String getEmptyString ()
    {
        return "No entries in this category.";
    }

    /** The unique identifier of the category we are displaying. */
    protected int _categoryId;
}
