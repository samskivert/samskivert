//
// $Id: Task.java,v 1.7 2003/12/10 20:33:42 mdb Exp $

package com.samskivert.twodue.data;

import java.sql.Date;
import java.util.ArrayList;

import com.samskivert.util.StringUtil;

/**
 * Contains the basic data associated with a task.
 */
public class Task
{
    public static final String[] COMPLEXITIES = {
        "Simple hack", "Minor feature", "Major feature",
        "Subsystem", "Major refactor",
    };

    public static final int[] COMPLEXITY_VALUES = {
        5, 10, 15, 20, 25,
    };

    public int taskId;

    public String summary;

    public String category;

    public String complexity;

    public int priority;

    public String creator;

    public Date creation;

    public String owner;

    public String completor;

    public Date completion;

    public String notes;

    /**
     * Converts numerical priority to a string.
     */
    public String getPriorityName ()
    {
        if (priority > 25) {
            return "Urgent";
        } else if (priority > 15) {
            return "Next release";
        } else if (priority > 10) {
            return "Near term";
        } else if (priority > 5) {
            return "Medium term";
        } else if (priority > 1) {
            return "Long term";
        } else {
            return "On the list";
        }
    }

    /**
     * Converts string complexity to a value, lower meaning less complex,
     * higher meaning more complex.
     */
    public int getComplexityValue ()
    {
        for (int ii = 0; ii < COMPLEXITIES.length; ii++) {
            if (COMPLEXITIES[ii].equals(complexity)) {
                return COMPLEXITY_VALUES[ii];
            }
        }
        return 50;
    }

    public String getDisplayCategory ()
    {
        return StringUtil.replace(category, ",", "<br>");
    }

    /**
     * Returns a string representation of this instance.
     */
    public String toString ()
    {
        return StringUtil.fieldsToString(this);
    }
}
