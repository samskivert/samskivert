//
// $Id: edit.java,v 1.2 2003/01/23 21:22:56 mdb Exp $

package com.samskivert.twodue.logic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import javax.servlet.http.HttpServletRequest;

import com.samskivert.util.QuickSort;
import com.samskivert.util.StringUtil;

import com.samskivert.servlet.RedirectException;
import com.samskivert.servlet.user.User;
import com.samskivert.servlet.util.ParameterUtil;
import com.samskivert.velocity.InvocationContext;

import com.samskivert.twodue.Log;
import com.samskivert.twodue.TwoDueApp;
import com.samskivert.twodue.data.Task;

/**
 * Allows a single task to be edited.
 */
public class edit extends UserLogic
{
    public void invoke (InvocationContext ctx, TwoDueApp app, User user)
        throws Exception
    {
        HttpServletRequest req = ctx.getRequest();

        int taskId = ParameterUtil.requireIntParameter(
            ctx.getRequest(), "task", "task.error.missing_taskid");

        // load up the task in question
        Task task = app.getRepository().loadTask(taskId);
        if (task == null) {
            ctx.put("error", "error.no_such_task");
            return;
        }

        // stick the task in the context
        ctx.put("task", task);

	// if they've submitted the form, we update the task database
	if (ParameterUtil.parameterEquals(
                ctx.getRequest(), "action", "update")) {
	    // extract our fields
	    task.summary = ParameterUtil.requireParameter(
                ctx.getRequest(), "summary", "task.error.missing_summary");
            // remove extra spaces introduced by our friend the textarea
            task.summary = task.summary.trim();

	    task.category = ParameterUtil.requireParameter(
                ctx.getRequest(), "category",
                "task.error.missing_category");
	    task.complexity = ParameterUtil.requireParameter(
                ctx.getRequest(), "complexity",
                "task.error.missing_complexity");
	    task.priority = ParameterUtil.requireIntParameter(
                ctx.getRequest(), "priority",
                "task.error.invalid_priority");
	    task.creator = ParameterUtil.requireParameter(
                ctx.getRequest(), "creator",
                "task.error.missing_creator");

            // preserve the null-status of non-owned tasks
            String owner = ParameterUtil.getParameter(
                ctx.getRequest(), "owner", true);
            task.owner = (StringUtil.blank(owner)) ? null : owner;

            app.getRepository().updateTask(task);
            ctx.put("error", "edit.message.task_updated");

        } else if (ParameterUtil.parameterEquals(
                       ctx.getRequest(), "action", "delete")) {
            app.getRepository().deleteTask(taskId);
            ctx.put("error", "edit.message.task_deleted");
            ctx.remove("task"); // clear out the task

        } else if (ParameterUtil.parameterEquals(
                       ctx.getRequest(), "action", "reopen")) {
            // clear out the completor and completion dates
            task.completor = null;
            task.completion = null;

            app.getRepository().updateTask(task);
            ctx.put("error", "edit.message.task_reopened");

        } else if (ParameterUtil.parameterEquals(
                       ctx.getRequest(), "action", "addnote")) {
	    // extract our fields
	    String note = ParameterUtil.requireParameter(
                ctx.getRequest(), "note", "edit.error.missing_note");
            // prefix the note with a creator identifier
            note = "[" + user.username + "] " + note.trim();

            if (StringUtil.blank(task.notes)) {
                task.notes = note;
            } else {
                task.notes += "\n\n" + note;
            }

            app.getRepository().updateTask(task);
            ctx.put("error", "edit.message.note_added");

        } else if (ParameterUtil.parameterEquals(
                       ctx.getRequest(), "action", "editnotes")) {
            task.notes = ParameterUtil.getParameter(
                ctx.getRequest(), "notes", false).trim();
            app.getRepository().updateTask(task);
            ctx.put("error", "edit.message.notes_updated");
        }
    }
}
