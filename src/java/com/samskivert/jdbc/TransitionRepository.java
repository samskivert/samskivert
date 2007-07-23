//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2007 Michael Bayne
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.samskivert.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.samskivert.io.PersistenceException;

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
                    stmt = conn.prepareStatement(
                        "insert into " + liaison.tableSQL("TRANSITIONS") + " (" +
                        liaison.columnSQL("CLASS") + ", " + liaison.columnSQL("NAME") +
                        ") values (?, ?)");
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
            new String[] { "CLASS", "NAME", "APPLIED" },
            new String[] { "VARCHAR(200)", "VARCHAR(50)", "TIMESTAMP NOT NULL" },
            null,
            new String[] { "CLASS", "NAME" });
    }
}
