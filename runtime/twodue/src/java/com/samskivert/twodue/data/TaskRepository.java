//
// $Id: TaskRepository.java,v 1.4 2002/11/12 22:32:02 mdb Exp $

package com.samskivert.twodue.data;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;

import com.samskivert.io.PersistenceException;
import com.samskivert.jdbc.ConnectionProvider;
import com.samskivert.jdbc.DatabaseLiaison;
import com.samskivert.jdbc.JDBCUtil;
import com.samskivert.jdbc.JORARepository;
import com.samskivert.jdbc.jora.Cursor;
import com.samskivert.jdbc.jora.Session;
import com.samskivert.jdbc.jora.Table;

/**
 * Provides access to the task table.
 */
public class TaskRepository extends JORARepository
{
    /**
     * The database identifier that the repository will use when fetching
     * a connection from the connection provider. The value is
     * <code>twodue</code> which you'll probably need to know to properly
     * configure your connection provider.
     */
    public static final String REPOSITORY_DB_IDENT = "twodue";

    /**
     * Creates the repository and opens the task database. A connection
     * provider should be supplied that will be used to obtain the
     * necessary database connection. The database identifier used to
     * obtain our connection is documented by {@link
     * #REPOSITORY_DB_IDENT}.
     *
     * @param provider a connection provider via which the repository will
     * get its database connection.
     */
    public TaskRepository (ConnectionProvider provider)
	throws PersistenceException
    {
	super(provider, REPOSITORY_DB_IDENT);

        // make sure we can get our database connection
        Connection conn = provider.getConnection(REPOSITORY_DB_IDENT);
        provider.releaseConnection(REPOSITORY_DB_IDENT, conn);
    }

    // documented inherited
    protected void createTables (Session session)
    {
	// create our table objects
	_ttable = new Table(Task.class.getName(),
                            "TASKS", session, "TASK_ID", true);
    }

    /**
     * Creates a task and inserts it into the repository.
     *
     * @return the newly created task.
     */
    public Task createTask (String summary, String category, String complexity,
                            int priority, String creator)
        throws PersistenceException
    {
        // create and configure a task instance
        Task task = new Task();
        task.summary = summary;
        task.category = category;
        task.complexity = complexity;
        task.priority = priority;
        task.creator = creator;
        task.notes = "";
        createTask(task);
        return task;
    }

    /**
     * Inserts the supplied (properly populated) new task into the task
     * repository.
     */
    public void createTask (final Task task)
        throws PersistenceException
    {
        task.creation = new Date(System.currentTimeMillis());
	execute(new Operation() {
            public Object invoke (Connection conn, DatabaseLiaison liaison)
                throws PersistenceException, SQLException
	    {
                // insert the task into the entry table
                _ttable.insert(task);
                // update the taskId now that it's known
                task.taskId = liaison.lastInsertedId(conn);
                return null;
            }
        });
    }

    /**
     * Loads up the specified task.
     *
     * @return the requested task or null if no tasks exists with that id.
     */
    public Task loadTask (final int taskId)
        throws PersistenceException
    {
        return (Task)execute(new Operation () {
            public Object invoke (Connection conn, DatabaseLiaison liaison)
                throws PersistenceException, SQLException
            {
                String query = "where TASK_ID = " + taskId;
                Cursor ec = _ttable.select(query);
                Task task = (Task)ec.next();
                if (task != null) {
                    // call next() again to cause the cursor to close itself
                    ec.next();
                }
                return task;
            }
        });
    }

    /**
     * Loads up and returns all unowned, uncompleted tasks.
     */
    public ArrayList loadTasks ()
        throws PersistenceException
    {
        return loadTasks("where COMPLETOR IS NULL AND OWNER IS NULL");
    }

    /**
     * Loads up and returns all unowned, uncompleted tasks, whose summary
     * or category contain the specified string.
     */
    public ArrayList findTasks (String query)
        throws PersistenceException
    {
        return loadTasks("where COMPLETOR IS NULL AND OWNER IS NULL " +
                         " AND (SUMMARY LIKE '%" + query +
                         "%' OR CATEGORY LIKE '%" + query + "%')");
    }

    /**
     * Loads up all owned tasks, ordered by priority.
     */
    public ArrayList loadOwnedTasks ()
        throws PersistenceException
    {
        return loadTasks("where COMPLETOR IS NULL AND OWNER IS NOT NULL " +
                         "ORDER BY PRIORITY DESC");
    }

    /**
     * Loads up and returns all completed tasks, sorted by completion
     * date.
     *
     * @param start the offset into the sorted list of completed tasks to
     * start returning tasks.
     * @param limit the limit to the number of tasks to be returned, or -1
     * if all completed should be returned.
     */
    public ArrayList loadCompletedTasks (int start, int limit)
        throws PersistenceException
    {
        String query = "where COMPLETOR IS NOT NULL " +
            "ORDER BY COMPLETION DESC LIMIT " + start;
        if (limit != -1) {
            query += (", " + limit);
        }
        return loadTasks(query);
    }

    /** Loads lists of tasks. */
    protected ArrayList loadTasks (final String query)
        throws PersistenceException
    {
        return (ArrayList)execute(new Operation () {
            public Object invoke (Connection conn, DatabaseLiaison liaison)
                throws PersistenceException, SQLException
            {
                Cursor tc = _ttable.select(query);
                return tc.toArrayList();
            }
        });
    }

    /**
     * Marks the specified task as completed by the specified user.
     */
    public void completeTask (final int taskId, final String completor)
        throws PersistenceException
    {
        execute(new Operation () {
            public Object invoke (Connection conn, DatabaseLiaison liaison)
                throws PersistenceException, SQLException
            {
                String query = "update TASKS " +
                    "set COMPLETOR = ?, COMPLETION = ? where TASK_ID = ?";
                PreparedStatement stmt = null;

                try {
                    stmt = conn.prepareStatement(query);
                    stmt.setString(1, completor);
                    stmt.setDate(2, new Date(System.currentTimeMillis()));
                    stmt.setInt(3, taskId);
                    JDBCUtil.checkedUpdate(stmt, 1);

                } finally {
                    JDBCUtil.close(stmt);
                }

                return null;
            }
        });
    }

    /**
     * Marks the specified task as owned by the specified user.
     */
    public void claimTask (final int taskId, final String owner)
        throws PersistenceException
    {
        execute(new Operation () {
            public Object invoke (Connection conn, DatabaseLiaison liaison)
                throws PersistenceException, SQLException
            {
                String query = "update TASKS " +
                    "set OWNER = ? where TASK_ID = ?";
                PreparedStatement stmt = null;

                try {
                    stmt = conn.prepareStatement(query);
                    stmt.setString(1, owner);
                    stmt.setInt(2, taskId);
                    JDBCUtil.checkedUpdate(stmt, 1);

                } finally {
                    JDBCUtil.close(stmt);
                }

                return null;
            }
        });
    }

    /**
     * Updates a task that was previously loaded from the repository.
     */
    public void updateTask (final Task task)
        throws PersistenceException
    {
	execute(new Operation() {
            public Object invoke (Connection conn, DatabaseLiaison liaison)
                throws PersistenceException, SQLException
	    {
                _ttable.update(task);
                return null;
            }
        });
    }

    protected Table _ttable;
}
