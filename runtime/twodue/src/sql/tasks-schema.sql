/**
 * $Id: tasks-schema.sql,v 1.1 2002/11/08 09:14:21 mdb Exp $
 *
 * Schema for the Two Due tasks and notes table.
 */

drop table if exists TASKS;

/**
 * Contains basic data for every task in the system.
 */
CREATE TABLE TASKS
(
    /**
     * A unique identifier for this task.
     */
    TASK_ID INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,

    /** 
     * The summary description of this task.
     */
    SUMMARY VARCHAR(255) NOT NULL,

    /** 
     * The comma separated list of categories occupied by this task.
     */
    CATEGORY VARCHAR(255) NOT NULL,

    /**
     * The complexity identifier for this task.
     */
    COMPLEXITY VARCHAR(32) NOT NULL,

    /**
     * The priority of this task.
     */
    PRIORITY INTEGER UNSIGNED NOT NULL,

    /** 
     * The user to which this task is currently assigned.
     */
    OWNER VARCHAR(255),

    /** 
     * The name of the creator of this task.
     */
    CREATOR VARCHAR(255) NOT NULL,

    /** 
     * The time of creation of this task.
     */
    CREATION DATE NOT NULL,

    /** 
     * The name of the completor(s) of this task.
     */
    COMPLETOR VARCHAR(255),

    /** 
     * The time of completion of this task.
     */
    COMPLETION DATE,

    /**
     * Defines our table keys.
     */
    PRIMARY KEY (TASK_ID),
    KEY (CATEGORY),
    KEY (PRIORITY),
    KEY (CREATION),
    KEY (COMPLETION)
);
