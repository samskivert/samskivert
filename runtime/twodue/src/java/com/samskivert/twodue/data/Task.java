//
// $Id: Task.java,v 1.2 2002/11/08 21:49:17 mdb Exp $

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
}
