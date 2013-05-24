//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.jdbc;

import java.sql.*;

import com.samskivert.io.PersistenceException;
import com.samskivert.util.StringUtil;

import static com.samskivert.jdbc.Log.log;

/**
 * The simple repository should be used for a repository that only needs access to a single JDBC
 * connection instance to perform its persistence services.
 */
public class SimpleRepository extends Repository
{
    /** See {@link #setExecutePreCondition}. */
    public static interface PreCondition
    {
        /** See {@link #setExecutePreCondition}. */
        public boolean validate (String dbident, Operation<?> op);
    }

    /**
     * Configures an operation that will be invoked prior to the execution of every database
     * operation to validate whether some pre-condition is met. This mainly exists for systems that
     * wish to ensure that all database operations take place on a particular thread (or not on a
     * particular thread) as database operations are generally slow and blocking.
     */
    public static void setExecutePreCondition (PreCondition condition)
    {
        _precond = condition;
    }

    /**
     * Creates and initializes a simple repository which will access the database identified by the
     * supplied database identifier.
     *
     * @param provider the connection provider which will be used to obtain our database
     * connection.
     * @param dbident the identifier of the database that will be accessed by this repository or
     * null if the derived class will call {@link #configureDatabaseIdent} by hand later.
     */
    public SimpleRepository (ConnectionProvider provider, String dbident)
    {
        super(provider);

        if (dbident != null) {
            configureDatabaseIdent(dbident);
        }
    }

    /**
     * This is called automatically if a dbident is provided at construct time, but a derived class
     * can pass null to its constructor and then call this method itself later if it wishes to
     * obtain its database identifier from an overridable method which could not otherwise be
     * called at construct time.
     */
    protected void configureDatabaseIdent (String dbident)
    {
        _dbident = dbident;

        // give the repository a chance to do any schema migration before things get further
        // underway
        try {
            executeUpdate(new Operation<Object>() {
                public Object invoke (Connection conn, DatabaseLiaison liaison)
                    throws SQLException, PersistenceException
                {
                    migrateSchema(conn, liaison);
                    return null;
                }
            });
        } catch (PersistenceException pe) {
            log.warning("Failure migrating schema", "dbident", _dbident, pe);
        }
    }

    /**
     * Executes the supplied read-only operation. In the event of a transient failure, the
     * repository will attempt to reestablish the database connection and try the operation again.
     *
     * @return whatever value is returned by the invoked operation.
     */
    protected <V> V execute (Operation<V> op)
        throws PersistenceException
    {
        return execute(op, true, true);
    }

    /**
     * Executes the supplied read-write operation. In the event of a transient failure, the
     * repository will attempt to reestablish the database connection and try the operation again.
     *
     * @return whatever value is returned by the invoked operation.
     */
    protected <V> V executeUpdate (Operation<V> op)
        throws PersistenceException
    {
        return execute(op, true, false);
    }

    /**
     * Executes the supplied operation followed by a call to <code>commit()</code> on the
     * connection unless a <code>PersistenceException</code> or runtime error occurs, in which case
     * a call to <code>rollback()</code> is executed on the connection.
     *
     * @param retryOnTransientFailure if true and the operation fails due to a transient failure
     * (like losing the connection to the database or deadlock detection), the connection to the
     * database will be reestablished (if necessary) and the operation attempted once more.
     * @param readOnly whether or not to request a read-only connection.
     *
     * @return whatever value is returned by the invoked operation.
     */
    protected <V> V execute (Operation<V> op, boolean retryOnTransientFailure, boolean readOnly)
        throws PersistenceException
    {
        Connection conn = null;
        DatabaseLiaison liaison = null;
        V rv = null;
        boolean supportsTransactions = false;
        boolean attemptedOperation = false;
        Boolean oldAutoCommit = null;

        // check our pre-condition
        if (_precond != null && !_precond.validate(_dbident, op)) {
            log.warning("Repository operation failed pre-condition check!", "dbident", _dbident,
                        "op", op, new Exception());
        }

        // obtain our database connection and associated goodies
        conn = _provider.getConnection(_dbident, readOnly);

        // make sure that no one else performs a database operation using the same connection until
        // we're done
        synchronized (conn) {
            try {
                liaison = LiaisonRegistry.getLiaison(conn);

                // find out if we support transactions
                DatabaseMetaData dmd = conn.getMetaData();
                if (dmd != null) {
                    supportsTransactions = dmd.supportsTransactions();
                }

                // turn off auto-commit
                if (supportsTransactions && conn.getAutoCommit()) {
                    oldAutoCommit = conn.getAutoCommit();
                    conn.setAutoCommit(false);
                }

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
                    // back out our changes if something got hosed (but not if the hosage was a
                    // result of losing our connection)
                    try {
                        if (supportsTransactions && !conn.isClosed()) {
                            conn.rollback();
                        }
                    } catch (SQLException rbe) {
                        log.warning("Unable to roll back operation", "err", sqe, "rberr", rbe);
                    }
                }

                if (conn != null) {
                    // let the provider know that the connection failed
                    _provider.connectionFailed(_dbident, readOnly, conn, sqe);
                    // clear out the reference so that we don't release it later
                    conn = null;
                }

                if (!retryOnTransientFailure || liaison == null ||
                    !liaison.isTransientException(sqe)) {
                    String err = "Operation invocation failed";
                    throw new PersistenceException(err, sqe);
                }

                // the MySQL JDBC driver has the annoying habit of including the embedded exception
                // stack trace in the message of their outer exception; if I want a fucking stack
                // trace, I'll call printStackTrace() thanksverymuch
                String msg = StringUtil.split("" + sqe, "\n")[0];
                log.info("Transient failure executing operation, retrying", "error", msg);

            } catch (PersistenceException pe) {
                // back out our changes if something got hosed
                try {
                    if (supportsTransactions && !conn.isClosed()) {
                        conn.rollback();
                    }
                } catch (SQLException rbe) {
                    log.warning("Unable to roll back operation", "origerr", pe, "rberr", rbe);
                }
                throw pe;

            } catch (RuntimeException rte) {
                // back out our changes if something got hosed
                try {
                    if (supportsTransactions && conn != null && !conn.isClosed()) {
                        conn.rollback();
                    }
                } catch (SQLException rbe) {
                    log.warning("Unable to roll back operation", "origerr", rte, "rberr", rbe);
                }
                throw rte;

            } finally {
                if (conn != null) {
                    try {
                        // restore our auto-commit settings
                        if (oldAutoCommit != null && !conn.isClosed()) {
                            conn.setAutoCommit(oldAutoCommit);
                        }
                    } catch (SQLException sace) {
                        log.warning("Unable to restore auto-commit", "err", sace);
                    }
                    // release the database connection
                    _provider.releaseConnection(_dbident, readOnly, conn);
                }
            }
        }

        // we'll only fall through here if the above code failed due to a transient exception (the
        // connection was closed for being idle, for example) and we've been asked to retry; so
        // let's do so
        return execute(op, false, readOnly);
    }

    /**
     * Executes the supplied update query in this repository, returning the number of rows
     * modified.
     */
    protected int update (final String query)
        throws PersistenceException
    {
        return executeUpdate(new Operation<Integer>() {
            public Integer invoke (Connection conn, DatabaseLiaison liaison)
                throws SQLException, PersistenceException
            {
                Statement stmt = null;
                try {
                    stmt = conn.createStatement();
                    return stmt.executeUpdate(query);
                } finally {
                    JDBCUtil.close(stmt);
                }
            }
        });
    }

    /**
     * Executes the supplied update query in this repository, throwing an exception if the
     * modification count is not equal to the specified count.
     */
    protected void checkedUpdate (final String query, final int count)
        throws PersistenceException
    {
        executeUpdate(new Operation<Object>() {
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
     * Executes the supplied update query in this repository, logging a warning if the modification
     * count is not equal to the specified count.
     */
    protected void warnedUpdate (final String query, final int count)
        throws PersistenceException
    {
        executeUpdate(new Operation<Object>() {
            public Object invoke (Connection conn, DatabaseLiaison liaison)
                throws SQLException, PersistenceException
            {
                Statement stmt = null;
                try {
                    stmt = conn.createStatement();
                    JDBCUtil.warnedUpdate(stmt, query, 1);
                } finally {
                    JDBCUtil.close(stmt);
                }
                return null;
            }
        });
    }

    /**
     * Instructs MySQL to perform table maintenance on the specified table.
     *
     * @param action <code>analyze</code> recomputes the distribution of the keys for the specified
     * table. This can help certain joins to be performed more efficiently. <code>optimize</code>
     * instructs MySQL to coalesce fragmented records and reclaim space left by deleted records.
     * This can improve a tables efficiency but can take a long time to run on large tables.
     */
    protected void maintenance (final String action, final String table)
        throws PersistenceException
    {
        executeUpdate(new Operation<Object>() {
            public Object invoke (Connection conn, DatabaseLiaison liaison)
                throws SQLException, PersistenceException
            {
                Statement stmt = null;
                try {
                    stmt = conn.createStatement();
                    ResultSet rs = stmt.executeQuery(action + " table " + table);
                    while (rs.next()) {
                        String result = rs.getString("Msg_text");
                        if (result == null ||
                            (result.indexOf("up to date") == -1 && !result.equals("OK"))) {
                            log.info("Table maintenance [" + SimpleRepository.toString(rs) + "].");
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
     * Derived classes can override this method and perform any schema migration they might need
     * (using the idempotent {@link JDBCUtil} schema migration methods). This is called during the
     * repository's constructor and will thus take place before derived classes (like the {@link
     * JORARepository} introspect on the schema to match it up to associated Java classes).
     */
    protected void migrateSchema (Connection conn, DatabaseLiaison liaison)
        throws SQLException, PersistenceException
    {
    }

    /**
     * Called when we fetch a connection from the provider. This gives derived classes an
     * opportunity to configure whatever internals they might be using with the connection that was
     * fetched.
     */
    protected void gotConnection (Connection conn)
    {
    }

    /**
     * Converts a row of a result set to a string, prepending each column with the column name from
     * the result set metadata.
     */
    protected static String toString (ResultSet rs)
        throws SQLException
    {
        ResultSetMetaData md = rs.getMetaData();
        int ccount = md.getColumnCount();
        StringBuilder buf = new StringBuilder();
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
