//
// $Id: ModelUtil.java,v 1.1 2001/06/07 08:37:47 mdb Exp $

package robodj.chooser;

import robodj.repository.*;

/**
 * Some utility functions.
 */
public class ModelUtil
{
    /**
     * Generates a string array suitable for sticking into a combo box
     * with the category names.
     */
    public static String[] catBoxNames (Model model)
    {
        Category[] cats = model.getCategories();
        String[] names = new String[cats.length+1];
        names[0] = "<uncategorized>";
        for (int i = 0; i < cats.length; i++) {
            names[i+1] = cats[i].name;
        }
        return names;
    }

    /**
     * Returns the index of the specified category in the category array.
     * Useful for activating a particular category in a combo box, for
     * example. -1 is returned if no category with the specified id exists
     * in the category list (but if that's the case, something really
     * wacky is going on).
     */
    public static int getCategoryIndex (Model model, int categoryid)
    {
        Category[] cats = model.getCategories();
        for (int i = 0; i < cats.length; i++) {
            if (cats[i].categoryid == categoryid) {
                return i;
            }
        }
        return -1;
    }
}
