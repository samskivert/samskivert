//
// $Id: Category.java,v 1.1 2000/11/08 06:42:57 mdb Exp $

package robodj.repository;

/**
 * The category class represents an entry in the category table which
 * simply maps category ids to category names.
 */
public class Category
{
    /** This category's unique identifier. */
    public int categoryid;

    /** The name of this category. */
    public String name;
}
