//
// $Id: SimpleRepository.java,v 1.9 2004/05/11 05:43:15 mdb Exp $
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001 Michael Bayne
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

import java.sql.*;

import com.samskivert.Log;
import com.samskivert.io.PersistenceException;
import com.samskivert.util.StringUtil;

/**
 * The simple repository should be used for a repository that only needs
 * access to a single JDBC connection instance to perform its persistence
 * services.
 */
public class SimpleRepository extends Repository
{
    /** See {@link #setExecutePreCondition}. */
    public static interface PreCondition
    {
        /** See {@link #setExecutePreCondition}. */
        public boolean validate (String dbident, Operation op);
    }

    /**
     * Configures an operation that will be invoked prior to the execution
     * of every database operation to validate whether some pre-condition
     * is met. This mainly exists for systems that wish to ensure that all
     * database operations take place on a particular thread (or not on a
     * particular thread) as database operations are generally slow and
     * blocking.
     */
    public static void setExecutePreCondition (PreCondition condition)
    {
        _precond = condition;
    }

    /**
     * Creates and initializes a simple repository which will access the
     * database identified by the supplied database identifier.
     *
     * @param provider the connection provider which will be used to
     * obtain our database connection.
     * @param dbident the identifier of the database that will be accessed
     * by this repository.
     */
    public SimpleRepository (ConnectionProvider provider, String dbident)
    {
        super(provider);
        _dbident = dbident;
    }

    /**
     * Executes the supplied operation. In the event of a transient
     * failure, the repository will attempt to reestablish the database
     * connection and try the operation again.
     *
     * @return whatever value is returned by the invoked operation.
     */
    protected Object execute (Operation op)
	throws PersistenceException
    {
        return execute(op, true);
    }

    /**
     * Executes the supplied operation followed by a call to
     * <code>commit()</code> on the connection unless a
     * <code>PersistenceException</code> or runtime error occurs, in which
     * case a call to <code>rollback()</code> is executed on the
     * connection.
     *
     * @param retryOnTransientFailure if true and the operation fails due
     * to a transient failure (like losing the connection to the database
     * or deadlock detection), the connection to the database will be
     * reestablished (if necessary) and the operation attempted once more.
     *
     * @return whatever value is returned by the invoked operation.
     */
    protected Object execute (Operation op, boolean retryOnTransientFailure)
	throws PersistenceException
    {
        Connection conn = null;
        DatabaseLiaison liaison = null;
        Object rv = null;
        boolean supportsTransactions = false;
        boolean attemptedOperation = false;

        // check our pre-condition
        if (_precond != null && !_precond.validate(_dbident, op)) {
            Log.warning("Repository operation failed pre-condition check! " +
                        "[dbident=" + _dbident + ", op=" + op + "].");
            Thread.dumpStack();
        }

        try {
            // obtain our database connection and associated goodies
            conn = _provider.getConnection(_dbident);
            liaison = LiaisonRegistry.getLiaison(conn);

            // find out if we support transactions
            DatabaseMetaData dmd = conn.getMetaData();
            if (dmd != null) {
                supportsTransactions = dmd.supportsTransactions();
            }

            // turn off auto-commit
            conn.setAutoCommit(false);

            // let derived classes do any got-connection processing
            gotConnection(conn);

	    // invoke the operation
            attemptedOperation = true;
	    rv = op.invoke(conn, liaison);

	    // commit the transaction
            if (supportsTransactions) {
                conn.commit();
            }

            // return the operation result
            return rv;

	} catch (SQLException sqe) {
            if (attemptedOperation) {
                // back out our changes if something got hosed
                try {
                    if (supportsTransactions) {
                        conn.rollback();
                    }
                } catch (SQLException rbe) {
                    Log.warning("Unable to roll back operation " +
                                "[origerr=" + sqe + ", rberr=" + rbe + "].");
                }
            }

            if (conn != null) {
                // let the connection provider know that the connection failed
                _provider.connectionFailed(_dbident, conn, sqe);

                // clear out the reference so that we don't release it later
                conn = null;
            }

            // if this is a transient failure and we've been requested to
            // retry such failures, try one more time
            if (retryOnTransientFailure &&
                liaison != null && liaison.isTransientException(sqe)) {
                // the MySQL JDBC driver has the annoying habit of
                // including the embedded exception stack trace in the
                // message of their outer exception; if I want a fucking
                // stack trace, I'll call printStackTrace() thanksverymuch
                String msg = StringUtil.split("" + sqe, "\n")[0];
                Log.info("Transient failure executing operation, " +
                         "retrying [error=" + msg + "].");
                return execute(op, false);
            }

            String err = "Operation invocation failed";
            throw new PersistenceException(err, sqe);

	} catch (PersistenceException pe) {
	    // back out our changes if something got hosed
            try {
                if (supportsTransactions) {
                    conn.rollback();
                }
            } catch (SQLException rbe) {
                Log.warning("Unable to roll back operation " +
                            "[origerr=" + pe + ", rberr=" + rbe + "].");
            }
            throw pe;

	} catch (RuntimeException rte) {
	    // back out our changes if something got hosed
            try {
                if (conn != null && supportsTransactions) {
                    conn.rollback();
                }
            } catch (SQLException rbe) {
                Log.warning("Unable to roll back operation " +
                            "[origerr=" + rte + ", rberr=" + rbe + "].");
            }
	    throw rte;

        } finally {
            if (conn != null) {
                // release the database connection
                _provider.releaseConnection(_dbident, conn);
            }
	}
    }

    /**
     * Executes the supplied update query in this repository, returning
     * the number of rows modified.
     */
    protected int update (final String query)
        throws PersistenceException
    {
        Integer rv = (Integer)execute(new Operation() {
            public Object invoke (Connection conn, DatabaseLiaison liaison)
                throws SQLException, PersistenceException
            {
                Statement stmt = null;
                try {
                    stmt = conn.createStatement();
                    return new Integer(stmt.executeUpdate(query));
                } finally {
                    JDBCUtil.close(stmt);
                }
            }
        });
        return rv.intValue();
    }

    /**
     * Executes the supplied update query in this repository, throwing an
     * exception if the modification count is not equal to the specified
     * count.
     */
    protected void checkedUpdate (final String query, final int count)
        throws PersistenceException
    {
        execute(new Operation() {
            public Object invoke (Connection conn, DatabaseLiaison liaison)
                throws SQLException, PersistenceException
            {
                Statement stmt = null;
                try {
                    stmt = conn.createStatement();
                    JDBCUtil.checkedUpdate(stmt, query, 1);
                } finally {
                    JDBCUtil.close(stmt);
                }
                return null;
            }
        });
    }

    /**
     * Instructs MySQL to perform table maintenance on the specified
     * table.
     *
     * @param action <code>analyze</code> recomputes the distribution of
     * the keys for the specified table. This can help certain joins to be
     * performed more efficiently. <code>optimize</code> instructs MySQL
     * to coalesce fragmented records and reclaim space left by deleted
     * records. This can improve a tables efficiency but can take a long
     * time to run on large tables.
     */
    protected void maintenance (final String action, final String table)
        throws PersistenceException
    {
        execute(new Operation() {
            public Object invoke (Connection conn, DatabaseLiaison liaison)
                throws SQLException, PersistenceException
            {
                Statement stmt = null;
                try {
                    stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(
                        action + " table " + table);
                    while (rs.next()) {
                        String result = rs.getString("Msg_text");
                        if (result == null ||
                            result.indexOf("up to date") == -1 &&
                            !result.equals("OK")) {
                            Log.info("Table maintenance [" +
                                     SimpleRepository.toString(rs) + "].");
                        }
                    }

                } finally {
                    JDBCUtil.close(stmt);
                }
                return null;
            }
        });
    }

    /**
     * Called when we fetch a connection from the provider. This gives
     * derived classes an opportunity to configure whatever internals they
     * might be using with the connection that was fetched.
     */
    protected void gotConnection (Connection conn)
    {
    }

    /**
     * Converts a row of a result set to a string, prepending each column
     * with the column name from the result set metadata.
     */
    protected static String toString (ResultSet rs)
        throws SQLException
    {
        ResultSetMetaData md = rs.getMetaData();
        int ccount = md.getColumnCount();
        StringBuffer buf = new StringBuffer();
        for (int ii = 1; ii <= ccount; ii++) {
            if (buf.length() > 0) {
                buf.append(", ");
            }
            buf.append(md.getColumnName(ii)).append("=");
            buf.append(rs.getObject(ii));
        }
        return buf.toString();
    }

    protected String _dbident;
    protected static PreCondition _precond;
}
