//
// $Id: CatList.java,v 1.1 2003/12/10 20:33:42 mdb Exp $

package com.samskivert.twodue.logic;

import java.util.ArrayList;
import java.util.HashMap;

import com.samskivert.twodue.data.Task;

/**
 * Used to categorize tasks.
 */
public class CatList
    implements Comparable
{
    /** This indicates which task to which a category belongs. */
    public static interface Categorizer
    {
        public String category (Task task);
    }

    /** Used to ease category generation in UI. */
    public static class CategoryTool
    {
        public boolean checkCategory (String category)
        {
            if (category.equals(_category)) {
                return false;
            } else {
                _category = category;
                return true;
            }
        }

        public void clear ()
        {
            _category = null;
        }

        protected String _category;
    }

    /** The name of the category. */
    public String name;

    /** The tasks within it. */
    public ArrayList tasks;

    // documentation inherited from interface
    public int compareTo (Object other)
    {
        return name.compareTo(((CatList)other).name);
    }

    /**
     * Categorizes a list of tasks.
     */
    public static CatList[] categorize (ArrayList tasks, Categorizer catter)
    {
        if (tasks == null) {
            return new CatList[0];
        }

        ArrayList cats = new ArrayList();
        HashMap cmap = new HashMap();
        int tcount = tasks.size();
        for (int ii = 0; ii < tcount; ii++) {
            Task task = (Task)tasks.get(ii);
            String category = catter.category(task);
            CatList clist = (CatList)cmap.get(category);
            if (clist == null) {
                clist = new CatList();
                clist.name = category;
                clist.tasks = new ArrayList();
                cats.add(clist);
                cmap.put(category, clist);
            }
            clist.tasks.add(task);
        }

        CatList[] ctasks = new CatList[cats.size()];
        cats.toArray(ctasks);

        return ctasks;
    }
}
