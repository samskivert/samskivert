//
// $Id: index.java,v 1.12 2003/12/10 20:33:42 mdb Exp $

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
 * Displays a summary out outstanding and completed tasks.
 */
public class index extends UserLogic
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

	// if they've submitted the form, we create a new task and stick
	// it into the dataabse
	if (ParameterUtil.parameterEquals(req, "action", "create")) {
	    // set the creator from the username of the calling user
	    Task task = new Task();
	    task.creator = user.username;
            // if they requested to do so, claim the task for them
            if (ParameterUtil.isSet(req, "claim")) {
                task.owner = user.username;
            }
	    task.notes = ""; // no notes to start

	    // parse our fields
	    task.summary = ParameterUtil.requireParameter(
                req, "summary", "task.error.missing_summary");
	    task.category = ParameterUtil.requireParameter(
                req, "category", "task.error.missing_category");
	    task.complexity = ParameterUtil.requireParameter(
                req, "complexity",
                "task.error.missing_complexity");
	    task.priority = ParameterUtil.requireIntParameter(
                req, "priority", "task.error.invalid_priority");

	    // insert the task into the repository
            app.getRepository().createTask(task);

            // if they want to edit this task, shoot them to the edit
            // page, otherwise flip back to this same page minus our query
            // parameters to clear out the creation form
            if (ParameterUtil.isSet(req, "edit")) {
                throw new RedirectException("edit.wm?task=" + task.taskId);
            } else {
                throw new RedirectException(
                    "index.wm?msg=index.message.task_created");
            }

        } else if (ParameterUtil.parameterEquals(req, "action", "complete")) {
            int taskId = ParameterUtil.requireIntParameter(
                req, "task", "task.error.missing_taskid");
            app.getRepository().completeTask(taskId, user.username);

	    // let the user know we updated the database
	    ctx.put("error", "index.message.task_completed");

        } else if (ParameterUtil.parameterEquals(req, "action", "claim")) {
            int taskId = ParameterUtil.requireIntParameter(
                req, "task", "task.error.missing_taskid");
            app.getRepository().claimTask(taskId, user.username);

	    // let the user know we updated the database
	    ctx.put("error", "index.message.task_claimed");
        }

        // load up owned tasks and break them down by owner
        ArrayList tasks = app.getRepository().loadOwnedTasks();
        Collections.sort(tasks, OWNED_PARATOR);
        CatList[] otasks = categorize(tasks, new Categorizer() {
            public String category (Task task) {
                return task.owner;
            }
        });
        // look for our name and swap that into the zeroth position
        for (int ii = 0; ii < otasks.length; ii++) {
            if (otasks[ii].name.equals(user.username)) {
                CatList tlist = otasks[0];
                otasks[0] = otasks[ii];
                otasks[ii] = tlist;
                break;
            }
        }
        ctx.put("otasks", otasks);
        ctx.put("ocats", new CategoryTool());

        // load up recently completed tasks
        tasks = app.getRepository().loadCompletedTasks(0, 6);
        ctx.put("dtasks", tasks);
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

    protected static final Comparator OPEN_PARATOR = new Comparator() {
        public int compare (Object o1, Object o2) {
            Task t1 = (Task)o1, t2 = (Task)o2;
            // sort first by reverse priority, then by complexity
            if (t1.priority == t2.priority) {
                return t1.getComplexityValue() - t2.getComplexityValue();
            } else {
                return t2.priority - t1.priority;
            }
        }
    };

    protected static final Comparator OWNED_PARATOR = new Comparator() {
        public int compare (Object o1, Object o2) {
            Task t1 = (Task)o1, t2 = (Task)o2;
            // sort by complexity
            return t1.getComplexityValue() - t2.getComplexityValue();
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
