//
// $Id: Task.java,v 1.4 2002/11/09 02:11:22 mdb Exp $

package com.samskivert.twodue.data;

import java.util.ArrayList;
import java.sql.Date;

/**
 * Contains the basic data associated with a task.
 */
public class Task
{
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

    public String getPriorityName ()
    {
        switch (priority) {
        case 50: return "Urgent";
        case 25: return "Next release";
        case 15: return "Soon";
        case 10: return "Before launch";
        case 5: return "Post launch";
        case 1: return "On the list";
        default: return "Unknown";
        }
    }
}
