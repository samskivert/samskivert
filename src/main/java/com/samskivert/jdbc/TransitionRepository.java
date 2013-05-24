//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.samskivert.io.PersistenceException;

import static com.samskivert.jdbc.Log.log;

/**
 * Used to note that transitionary code has been run to migrate persistent data. This is especially
 * useful for data that one cannot examine to determine if it's been transitioned.
 */
public class TransitionRepository extends SimpleRepository
{
    /** The identifier of this connection. */
    public static final String TRANSITION_DB_IDENT = "transitiondb";

    /**
     * An interface for the transition.
     */
    public static interface Transition
    {
        /**
         * Do the transition.
         */
        public void run () throws PersistenceException;
    }

    /**
     * Construct a TransitionRepository for the server.
     */
    public TransitionRepository (ConnectionProvider conprov)
        throws PersistenceException
    {
        super(conprov, TRANSITION_DB_IDENT);
    }

    /**
     * Perform a transition if it has not already been applied, and record that it was applied.
     */
    public void transition (Class<?> clazz, String name, Transition trans)
        throws PersistenceException
    {
        if (!isTransitionApplied(clazz, name) && noteTransition(clazz, name)) {
            try {
                trans.run();

            } catch (PersistenceException e) {
                try {
                    clearTransition(clazz, name);
                } catch (PersistenceException pe) {
                    log.warning("Failed to clear failed transition", "class", clazz, "name", name,
                                pe);
                }
                throw e;

            } catch (RuntimeException rte) {
                try {
                    clearTransition(clazz, name);
                } catch (PersistenceException pe) {
                    log.warning("Failed to clear failed transition", "class", clazz, "name", name,
                                pe);
                }
                throw rte;
            }
        }
    }

    /**
     * Returns whether the specified name transition been applied.
     */
    public boolean isTransitionApplied (Class<?> clazz, final String name)
        throws PersistenceException
    {
        final String cname = clazz.getName();
        return execute(new Operation<Boolean>() {
            public Boolean invoke (Connection conn, DatabaseLiaison liaison)
                throws SQLException, PersistenceException
            {
                PreparedStatement stmt = null;
                try {
                    stmt = conn.prepareStatement(
                        " select " + liaison.columnSQL("NAME") +
                        "   from " + liaison.tableSQL("TRANSITIONS") +
                        "  where " + liaison.columnSQL("CLASS") + "=?" +
                        "    and " + liaison.columnSQL("NAME") + "=?");
                    stmt.setString(1, cname);
                    stmt.setString(2, name);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        return true;
                    }

                } finally {
                    JDBCUtil.close(stmt);
                }
                return false;
            }
        });
    }

    /**
     * Note in the database that a particular transition has been applied.
     *
     * @return true if the transition was noted, false if it could not be noted because another
     * process noted it first.
     */
    public boolean noteTransition (Class<?> clazz, final String name)
        throws PersistenceException
    {
        final String cname = clazz.getName();
        return executeUpdate(new Operation<Boolean>() {
            public Boolean invoke (Connection conn, DatabaseLiaison liaison)
                throws SQLException, PersistenceException
            {
                PreparedStatement stmt = null;
                try {
                    stmt = conn.prepareStatement(
                        "insert into " + liaison.tableSQL("TRANSITIONS") + " (" +
                        liaison.columnSQL("CLASS") + ", " + liaison.columnSQL("NAME") +
                        ", " + liaison.columnSQL("APPLIED") +
                        ") values (?, ?, ?)");
                    stmt.setString(1, cname);
                    stmt.setString(2, name);
                    stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
                    JDBCUtil.checkedUpdate(stmt, 1);
                    return true;

                } catch (SQLException sqe) {
                    if (liaison.isDuplicateRowException(sqe)) {
                        return false;
                    } else {
                        throw sqe;
                    }

                } finally {
                    JDBCUtil.close(stmt);
                }
            }
        });
    }

    /**
     * Clear the transition.
     */
    public void clearTransition (Class<?> clazz, final String name)
        throws PersistenceException
    {
        final String cname = clazz.getName();
        executeUpdate(new Operation<Void>() {
            public Void invoke (Connection conn, DatabaseLiaison liaison)
                throws SQLException, PersistenceException
            {
                PreparedStatement stmt = null;
                try {
                    stmt = conn.prepareStatement(
                        " delete from " + liaison.tableSQL("TRANSITIONS") +
                        "       where " + liaison.columnSQL("CLASS") + "=? " +
                        "         and " + liaison.columnSQL("NAME") + "=?");
                    stmt.setString(1, cname);
                    stmt.setString(2, name);
                    stmt.executeUpdate(); // we don't care if it worked or not

                } finally {
                    JDBCUtil.close(stmt);
                }
                return null;
            }
        });
    }

    @Override
    protected void migrateSchema (Connection conn, DatabaseLiaison liaison)
        throws SQLException, PersistenceException
    {
        liaison.createTableIfMissing(
            conn,
            "TRANSITIONS",
            Arrays.asList("CLASS", "NAME", "APPLIED"),
            Arrays.asList(new ColumnDefinition("VARCHAR(200)", true, false, null),
                          new ColumnDefinition("VARCHAR(50)", true, false, null),
                          new ColumnDefinition("TIMESTAMP")),
            Collections.<List<String>>emptyList(),
            Arrays.asList("CLASS", "NAME"));
    }
}
