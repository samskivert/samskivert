//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

import com.samskivert.io.PersistenceException;

import static com.samskivert.jdbc.Log.log;

/**
 * Provides connections using a pair of {@link DataSource} instances (one for read-only operations
 * and one for read-write operations). Note: if transactions are going to be used, the data sources
 * must be pooled data sources, otherwise bad things will happen.
 */
public class DataSourceConnectionProvider implements ConnectionProvider
{
    /**
     * Creates a connection provider that will obtain connections from the supplied read-only and
     * read-write sources. The <code>ident</code> mechanism is not used by this provider.
     * Responsibility for the lifecycle of the supplied datasources is left to the caller and is
     * not managed by the connection provider ({@link #shutdown} does nothing).
     *
     * @param url a URL prefix that can be used by the {@link DatabaseLiaison} to identify the type
     * of database being accessed by the supplied data sources, this should be one of "jdbc:mysql"
     * or "jdbc:postgresql" as those are the only two types of database currently supported.
     */
    public DataSourceConnectionProvider (String url, DataSource readSource, DataSource writeSource)
    {
        _url = url;
        _readSource = readSource;
        _writeSource = writeSource;
    }

    // from ConnectionProvider
    public Connection getConnection (String ident, boolean readOnly)
        throws PersistenceException
    {
        return getConnection(ident, readOnly, null);
    }

    // from ConnectionProvider
    public void releaseConnection (String ident, boolean readOnly, Connection conn)
    {
        try {
            conn.close();
        } catch (Exception e) {
            log.warning("Failure closing connection",
                        "ident", ident, "ro", readOnly, "conn", conn, e);
        }
    }

    // from ConnectionProvider
    public void connectionFailed (String ident, boolean readOnly, Connection conn,
                                  SQLException error)
    {
        try {
            conn.close();
        } catch (Exception e) {
            log.warning("Failure closing failed connection",
                        "ident", ident, "ro", readOnly, "conn", conn, e);
        }
    }

    // from ConnectionProvider
    public Connection getTxConnection (String ident) throws PersistenceException
    {
        // our connections are pooled, so we can just get them normally
        return getConnection(ident, false, false);
    }

    // from ConnectionProvider
    public void releaseTxConnection (String ident, Connection conn)
    {
        // our connections are pooled, so we can just release them normally
        releaseConnection(ident, false, conn);
    }

    // from ConnectionProvider
    public void txConnectionFailed (String ident, Connection conn, SQLException error)
    {
        // our connections are pooled, so we can just fail them normally
        connectionFailed(ident, false, conn, error);
    }

    // from ConnectionProvider
    public String getURL (String ident)
    {
        return _url;
    }

    // from ConnectionProvider
    public void shutdown ()
    {
        // nothing doing, the caller has to shutdown the datasources
    }

    protected Connection getConnection (String ident, boolean readOnly, Boolean autoCommit)
        throws PersistenceException
    {
        try {
            Connection conn;
            if (readOnly) {
                conn = _readSource.getConnection();
                conn.setReadOnly(true);
            } else {
                conn = _writeSource.getConnection();
            }
            if (autoCommit != null) conn.setAutoCommit(autoCommit);
            return conn;

        } catch (SQLException sqe) {
            throw new PersistenceException(sqe);
        }
    }

    protected String _url;
    protected DataSource _readSource, _writeSource;
}
