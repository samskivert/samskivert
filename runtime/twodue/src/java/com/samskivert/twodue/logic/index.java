//
// $Id: index.java,v 1.6 2002/11/12 22:50:54 mdb Exp $

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

        // we use this to determine whether to show "complete" buttons for
        // tasks
        ctx.put("username", user.username);

	// if they've submitted the form, we create a new task and stick
	// it into the dataabse
	if (ParameterUtil.parameterEquals(
                ctx.getRequest(), "action", "create")) {
	    // set the creator from the username of the calling user
	    Task task = new Task();
	    task.creator = user.username;
	    task.notes = ""; // no notes to start

	    // parse our fields
	    task.summary = ParameterUtil.requireParameter(
                ctx.getRequest(), "summary", "task.error.missing_summary");
	    task.category = ParameterUtil.requireParameter(
                ctx.getRequest(), "category", "task.error.missing_category");
	    task.complexity = ParameterUtil.requireParameter(
                ctx.getRequest(), "complexity",
                "task.error.missing_complexity");
	    task.priority = ParameterUtil.requireIntParameter(
                ctx.getRequest(), "priority", "task.error.invalid_priority");

	    // insert the task into the repository
            app.getRepository().createTask(task);

            // flip back to this same page minus our query parameters to
            // clear out the creation form
            throw new RedirectException(
                "index.wm?msg=index.message.task_created");

        } else if (ParameterUtil.parameterEquals(
                       ctx.getRequest(), "action", "complete")) {
            int taskId = ParameterUtil.requireIntParameter(
                ctx.getRequest(), "task", "task.error.missing_taskid");
            app.getRepository().completeTask(taskId, user.username);

	    // let the user know we updated the database
	    ctx.put("error", "index.message.task_completed");

        } else if (ParameterUtil.parameterEquals(
                       ctx.getRequest(), "action", "claim")) {
            int taskId = ParameterUtil.requireIntParameter(
                ctx.getRequest(), "task", "task.error.missing_taskid");
            app.getRepository().claimTask(taskId, user.username);

	    // let the user know we updated the database
	    ctx.put("error", "index.message.task_claimed");
        }

        // load up outstanding tasks and break them down by complexity
        String expand = ParameterUtil.getParameter(req, "expand", false);
        ctx.put("expand", expand);

        ArrayList tasks = null;
        String query = ParameterUtil.getParameter(req, "query", false);
        if (StringUtil.blank(query)) {
            tasks = app.getRepository().loadTasks();
        } else {
            ctx.put("query", query);
            tasks = app.getRepository().findTasks(query);
            // force expand to all
            expand = "all";
        }

        // sort the tasks by priority, then complexity
        Collections.sort(tasks, PLEX_PARATOR);

        CatList[] xtasks = categorize(tasks, expand, new Categorizer() {
            public String category (Task task) {
                return task.getPriorityName();
            }
        });
        ctx.put("xtasks", xtasks);
        ctx.put("xcats", new CategoryTool());

        if (!StringUtil.blank(query) && xtasks.length == 0) {
            ctx.put("error", "index.error.no_matching_tasks");
        }

        // load up owned tasks and break them down by owner
        tasks = app.getRepository().loadOwnedTasks();
        Collections.sort(tasks, PLEX_PARATOR);
        CatList[] otasks = categorize(tasks, null, new Categorizer() {
            public String category (Task task) {
                return task.owner;
            }
        });
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
        public int pruned;
        public int compareTo (Object other)
        {
            return name.compareTo(((CatList)other).name);
        }
    }

    protected static interface Categorizer
    {
        public String category (Task task);
    }

    protected CatList[] categorize (
        ArrayList tasks, String expand, Categorizer catter)
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
            if (expand == null || clist.tasks.size() < 2 ||
                expand.equals("all") ||
                (task.priority > 15) || expand.equals(category)) {
                clist.tasks.add(task);
            } else {
                clist.pruned++;
            }
        }

        CatList[] ctasks = new CatList[cats.size()];
        cats.toArray(ctasks);

        return ctasks;
    }

    // sorts tasks by relative complexity, simplest to most complex
    protected static final Comparator PLEX_PARATOR = new Comparator() {
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
        protected String _category;
    }
}
