//
// $Id: detail.java,v 1.3 2003/12/10 20:33:42 mdb Exp $

package com.samskivert.twodue.logic;

import java.util.StringTokenizer;
import javax.servlet.http.HttpServletRequest;

import com.samskivert.servlet.user.User;
import com.samskivert.servlet.util.ParameterUtil;
import com.samskivert.util.StringUtil;
import com.samskivert.velocity.InvocationContext;

import com.samskivert.twodue.Log;
import com.samskivert.twodue.TwoDueApp;
import com.samskivert.twodue.data.Task;

/**
 * Displays the details of a single task.
 */
public class detail extends UserLogic
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
        } else {
            // format the notes and stuff those in the context
            ctx.put("notes", formatNotes(task.notes));
            // stick the task in the context
            ctx.put("task", task);
        }
    }

    protected String formatNotes (String notes)
    {
        if (StringUtil.blank(notes)) {
            return notes;
        }

        StringBuffer fnotes = new StringBuffer();
        StringTokenizer tok = new StringTokenizer(notes, "\n", true);
        boolean excepting = false, eatnewline = false;

        while (tok.hasMoreTokens()) {
            String line = tok.nextToken();
            boolean sawexcept = false;
            if (line.indexOf("Exception:") != -1) {
                sawexcept = true;
                fnotes.append("<pre>\n");
            }

            if (line.equals("\n")) {
                if (!eatnewline) {
                    fnotes.append("<p>\n");
                } else {
                    eatnewline = false;
                }
                // no further processing on newlines
                continue;

            } else {
                fnotes.append(line).append("\n");
                eatnewline = true;
            }

            if (excepting && line.indexOf(" at ") == -1) {
                excepting = false;
                fnotes.append("</pre>");
            }
            if (sawexcept) {
                excepting = true;
            }
        }
        return fnotes.toString();
    }
}
