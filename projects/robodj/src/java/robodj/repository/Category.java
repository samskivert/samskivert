//
// $Id: Category.java,v 1.2 2004/01/26 16:10:55 mdb Exp $

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

    /** Creates an uninitialized category instance. */
    public Category ()
    {
    }

    /** Creates an initialized category instance. */
    public Category (int categoryid, String name)
    {
        this.categoryid = categoryid;
        this.name = name;
    }

    /** Generates a string representation of this instance. */
    public String toString ()
    {
        return name;
    }
}
