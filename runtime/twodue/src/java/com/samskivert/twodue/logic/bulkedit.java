//
// $Id: bulkedit.java,v 1.1 2003/12/10 20:33:42 mdb Exp $

package com.samskivert.twodue.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
 * Allows the bulk update of task properties.
 */
public class bulkedit extends UserLogic
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

        // load up our tasks
        ArrayList tasks = app.getRepository().loadTasks();

	// if they've submitted the form, we update the task database
	if (ParameterUtil.parameterEquals(
                ctx.getRequest(), "action", "update")) {
            int updated = 0;
            for (Iterator iter = tasks.iterator(); iter.hasNext(); ) {
                Task task = (Task)iter.next();
                // check to see if category or priority have been updated
                int nprio = ParameterUtil.getIntParameter(
                    req, "priority" + task.taskId, task.priority,
                    "bulkedit.error.invalid_task_priority");
                String ncat = ParameterUtil.getParameter(
                    req, "category" + task.taskId, false);
                if (nprio != task.priority ||
                    (!StringUtil.blank(ncat) && !ncat.equals(task.category))) {
                    task.priority = nprio;
                    task.category = ncat;
                    app.getRepository().updateTask(task);
                    updated++;
                }
            }
            String rspmsg;
            switch (updated) {
            case 0: rspmsg = "bulkedit.message.nothing_updated"; break;
            case 1: rspmsg = "bulkedit.message.task_updated"; break;
            default: rspmsg = "bulkedit.message.tasks_updated"; break;
            }
            ctx.put("error", rspmsg);
        }

        // now filter them
        int priority = ParameterUtil.getIntParameter(
            req, "priority", 10, "bulkedit.error.invalid_priority");
        ctx.put("priority", priority);
        for (Iterator iter = tasks.iterator(); iter.hasNext(); ) {
            Task task = (Task)iter.next();
            if (task.priority != priority) {
                iter.remove();
            }
        }
        Collections.sort(tasks, TASK_PARATOR);

        CatList[] xtasks = CatList.categorize(tasks, new CatList.Categorizer() {
            public String category (Task task) {
                return task.getPriorityName();
            }
        });
        ctx.put("xtasks", xtasks);
        ctx.put("xcats", new CatList.CategoryTool());
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
}
