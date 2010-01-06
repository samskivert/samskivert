//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2010 Michael Bayne, et al.
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
import java.sql.SQLException;
import javax.sql.DataSource;

import com.samskivert.io.PersistenceException;

import static com.samskivert.Log.log;

/**
 * Provides connections using a pair of {@link DataSource} instances (one for read-only operations
 * and one for read-write operations).
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

    // from interface ConnectionProvider
    public Connection getConnection (String ident, boolean readOnly)
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
            return conn;

        } catch (SQLException sqe) {
            throw new PersistenceException(sqe);
        }
    }

    // from interface ConnectionProvider
    public void releaseConnection (String ident, boolean readOnly, Connection conn)
    {
        try {
            conn.close();
        } catch (Exception e) {
            log.warning("Failure closing connection",
                        "ident", ident, "ro", readOnly, "conn", conn, e);
        }
    }

    // from interface ConnectionProvider
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

    // from interface ConnectionProvider
    public String getURL (String ident)
    {
        return _url;
    }

    // from interface ConnectionProvider
    public void shutdown ()
    {
        // nothing doing, the caller has to shutdown the datasources
    }

    protected String _url;
    protected DataSource _readSource, _writeSource;
}
