//
// $Id: CategoryMapping.java,v 1.1 2000/11/08 06:42:57 mdb Exp $

package robodj.repository;

/**
 * This is a convenience class that allows us to easily manipulate rows in
 * the category_map table.
 */
public class CategoryMapping
{
    /** The id of the category to which the entry is mapped. */
    public int categoryid;

    /** The entry that is mapped to the specified category. */
    public int entryid;

    public CategoryMapping ()
    {
    }

    public CategoryMapping (int catid, int entid)
    {
	categoryid = catid;
	entryid = entid;
    }
}
