//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.jdbc;

import java.sql.*;

import com.samskivert.io.PersistenceException;

/**
 * As the repository aims to interface with whatever database pooling services a project cares to
 * use, it obtains all of its database connections through a connection provider. The connection
 * provider provides connections based on a database identifier (a string) which identifies a
 * particular connection to a particular database. A user of these services would then coordinate
 * the database identifier (or identifiers) used to invoke a database operation with the database
 * connections that are returned by the connection provider that they provided to the repository at
 * construct time.
 */
public interface ConnectionProvider
{
    /**
     * Obtains a database connection based on the supplied database identifier. The repository
     * expects to have exclusive use of this connection instance until it releases it. This
     * connection will be released subsequently with a call to {@link #releaseConnection} or {@link
     * #connectionFailed} depending on the circumstances of the release. <code>close()</code>
     * <em>will not</em> be called on the connection. It is up to the connection provider to close
     * the connection when it is released if appropriate.
     *
     * @param ident the database connection identifier.
     * @param readOnly whether or not the connection may be to a read-only mirror of the
     * repository.
     *
     * @return an active JDBC connection (which may have come from a connection pool).
     *
     * @exception PersistenceException thrown if a problem occurs trying to open the requested
     * connection.
     */
    Connection getConnection (String ident, boolean readOnly)
        throws PersistenceException;

    /**
     * Releases a database connection when it is no longer needed by the repository.
     *
     * @param ident the database identifier used when obtaining this connection.
     * @param readOnly the same value that was passed to {@link #getConnection} to obtain this
     * connection.
     * @param conn the connection to release (back into the pool or to be closed if pooling is not
     * going on).
     */
    void releaseConnection (String ident, boolean readOnly, Connection conn);

    /**
     * Called by the repository if a failure occurred on the connection.  It is expected that the
     * connection will be disposed of and subsequent calls to <code>getConnection</code> will
     * return a freshly established connection to the database.
     *
     * @param ident the database identifier used when obtaining this connection.
     * @param readOnly the same value that was passed to {@link #getConnection} to obtain this
     * connection.
     * @param conn the connection that failed.
     * @param error the error thrown by the connection (which may be used to determine if the
     * connection should be closed or can be reused).
     */
    void connectionFailed (String ident, boolean readOnly, Connection conn, SQLException error);

    /**
     * Returns a connection that can be used in a transaction. See {@link #getConnection}. Note:
     * transaction connections are never read-only. What would be the point?
     */
    Connection getTxConnection (String ident) throws PersistenceException;

    /**
     * Releases a connection obtained by {@link #getTxConnection}. See {@link #releaseConnection}.
     */
    void releaseTxConnection (String ident, Connection conn);

    /**
     * Reports failure of a connection obtained by {@link #getTxConnection}. See {@link
     * #connectionFailed}.
     */
    void txConnectionFailed (String ident, Connection conn, SQLException error);

    /**
     * Returns the URL associated with this database identifier. This should be the same value that
     * would be used if {@link #getConnection} were called.
     */
    String getURL (String ident);

    /**
     * Shuts down this connection provider, closing all connections currently in the pool. This
     * should only be called once all active connections have been released with {@link
     * #releaseConnection}.
     */
    void shutdown ();
}
