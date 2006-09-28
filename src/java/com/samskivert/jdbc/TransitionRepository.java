//
// $Id$

package com.samskivert.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.samskivert.io.PersistenceException;

/**
 * Used to note that transitionary code has been run to migrate persistent
 * data. The TransitionRepository is especially useful for data that
 * one cannot examine to determine if it's been transitioned.
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
     * Perform a transition if it has not already been applied, and record
     * that it was applied.
     */
    public void transition (Class clazz, String name, Transition trans)
        throws PersistenceException
    {
        if (!isTransitionApplied(clazz, name)) {
            trans.run();
            noteTransition(clazz, name);
        }
    }

    /**
     * Has the specified name transition been applied.
     */
    public boolean isTransitionApplied (Class clazz, final String name)
        throws PersistenceException
    {
        final String cname = clazz.getName();
        return execute(new Operation<Boolean>() {
            public Boolean invoke (Connection conn, DatabaseLiaison liaison)
                throws SQLException, PersistenceException
            {
                Object found = null;
                PreparedStatement stmt = null;
                try {
                    stmt = conn.prepareStatement("select NAME " +
                        "from TRANSITIONS where CLASS=? and NAME=?");
                    stmt.setString(1, cname);
                    stmt.setString(2, name);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        while (rs.next()); // is this really necessary?
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
     */
    public void noteTransition (Class clazz, final String name)
        throws PersistenceException
    {
        final String cname = clazz.getName();
        executeUpdate(new Operation<Void>() {
            public Void invoke (Connection conn, DatabaseLiaison liaison)
                throws SQLException, PersistenceException
            {
                PreparedStatement stmt = null;
                try {
                    stmt = conn.prepareStatement("insert into TRANSITIONS " +
                        "(CLASS, NAME) values (?, ?)");
                    stmt.setString(1, cname);
                    stmt.setString(2, name);

                    JDBCUtil.checkedUpdate(stmt, 1);
                } finally {
                    JDBCUtil.close(stmt);
                }
                return null;
            }
        });
    }

    /**
     * Clear the transition.
     */
    public void clearTransition (Class clazz, final String name)
        throws PersistenceException
    {
        final String cname = clazz.getName();
        executeUpdate(new Operation<Void>() {
            public Void invoke (Connection conn, DatabaseLiaison liaison)
                throws SQLException, PersistenceException
            {
                PreparedStatement stmt = null;
                try {
                    stmt = conn.prepareStatement("delete from TRANSITIONS " +
                        "where CLASS=? and NAME=?");
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
        JDBCUtil.createTableIfMissing(conn, "TRANSITIONS", new String[] {
            "CLASS varchar(200) not null",
            "NAME varchar(50) not null",
            "APPLIED timestamp not null",
            "primary key (CLASS, NAME)" }, "");
    }
}
