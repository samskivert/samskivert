//
// $Id: tasks.java,v 1.3 2003/12/10 20:33:42 mdb Exp $

package com.samskivert.twodue.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import javax.servlet.http.HttpServletRequest;

import com.samskivert.util.StringUtil;

import com.samskivert.servlet.RedirectException;
import com.samskivert.servlet.user.User;
import com.samskivert.servlet.util.ParameterUtil;
import com.samskivert.velocity.InvocationContext;

import com.samskivert.twodue.Log;
import com.samskivert.twodue.TwoDueApp;
import com.samskivert.twodue.data.Task;

/**
 * Displays a summary of unclaimed tasks.
 */
public class tasks extends UserLogic
{
    public void invoke (InvocationContext ctx, TwoDueApp app, User user)
        throws Exception
    {
        HttpServletRequest req = ctx.getRequest();

        // display any message we've been asked to display
        String msg = ParameterUtil.getParameter(req, "message", true);
        if (!StringUtil.blank(msg)) {
            ctx.put("error", msg);
        }

        ArrayList tasks = null;
        String query = ParameterUtil.getParameter(req, "query", false);
        if (StringUtil.blank(query)) {
            tasks = app.getRepository().loadTasks();
        } else {
            ctx.put("query", query);
            tasks = app.getRepository().findTasks(query);
        }

        // sort the tasks by priority, then complexity
        Collections.sort(tasks, TASK_PARATOR);

        CatList[] xtasks = categorize(tasks, new Categorizer() {
            public String category (Task task) {
                return task.getPriorityName();
            }
        });
        ctx.put("xtasks", xtasks);
        ctx.put("xcats", new CategoryTool());

        if (!StringUtil.blank(query) && xtasks.length == 0) {
            ctx.put("error", "index.error.no_matching_tasks");
        }
    }

    protected static class CatList
        implements Comparable
    {
        public String name;
        public ArrayList tasks;
        public int compareTo (Object other)
        {
            return name.compareTo(((CatList)other).name);
        }
    }

    protected static interface Categorizer
    {
        public String category (Task task);
    }

    protected CatList[] categorize (ArrayList tasks, Categorizer catter)
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

    protected static final Comparator TASK_PARATOR = new Comparator() {
        public int compare (Object o1, Object o2) {
            Task t1 = (Task)o1, t2 = (Task)o2;
            // sort first by reverse priority, then by category, then complexity
            if (t1.priority == t2.priority) {
                if (t1.category.equals(t2.category)) {
                    return t1.getComplexityValue() - t2.getComplexityValue();
                } else {
                    return t1.category.compareTo(t2.category);
                }
            } else {
                return t2.priority - t1.priority;
            }
        }
    };

    // used to easy category generation in UI
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
}
