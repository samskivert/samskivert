//
// $Id: StaticConnectionProvider.java,v 1.2 2001/09/21 03:01:46 mdb Exp $
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

import java.io.IOException;
import java.sql.*;
import java.util.*;

import com.samskivert.Log;
import com.samskivert.io.PersistenceException;
import com.samskivert.util.ConfigUtil;
import com.samskivert.util.PropertiesUtil;
import com.samskivert.util.StringUtil;

/**
 * The static connection provider generates JDBC connections based on
 * configuration information provided via a properties file. It does no
 * connection pooling and always returns the same connection for a
 * particular identifier (unless that connection need be closed because of
 * a connection failure, in which case it opens a new one the next time
 * the connection is requested).
 *
 * <p> The configuration properties file should contain the following
 * information:
 *
 * <pre>
 * IDENT.driver=[jdbc driver class]
 * IDENT.url=[jdbc driver url]
 * IDENT.username=[jdbc username]
 * IDENT.password=[jdbc password]
 *
 * [...]
 * </pre>
 *
 * Where <code>IDENT</code> is the database identifier for a particular
 * database connection. When a particular database identifier is
 * requested, the configuration information will be fetched from the
 * properties.
 */
public class StaticConnectionProvider implements ConnectionProvider
{
    /**
     * Constructs a static connection provider which will load its
     * configuration from a properties file accessible via the classpath
     * of the running application and identified by the specified path.
     *
     * @param propPath the path (relative to the classpath) to the
     * properties file that will be used for configuration information.
     *
     * @exception IOException thrown if an error occurs locating or
     * loading the specified properties file.
     */
    public StaticConnectionProvider (String propPath)
        throws IOException
    {
        this(ConfigUtil.loadProperties(propPath));
    }

    /**
     * Constructs a static connection provider which will fetch its
     * configuration information from the specified properties object.
     *
     * @param props the configuration for this connection provider.
     */
    public StaticConnectionProvider (Properties props)
    {
        _props = props;
    }

    /**
     * Closes all of the open database connections in preparation for
     * shutting down.
     */
    public void shutdown ()
    {
        // close all of the connections
        Iterator iter = _conns.keySet().iterator();
        while (iter.hasNext()) {
            String ident = (String)iter.next();
            Connection conn = (Connection)_conns.get(ident);
            try {
                conn.close();
            } catch (SQLException sqe) {
                Log.warning("Error shutting down connection " +
                            "[ident=" + ident + ", err=" + sqe + "].");
            }
        }

        // clear out the connection table
        _conns.clear();
    }

    // documentation inherited
    public Connection getConnection (String ident)
        throws PersistenceException
    {
        Connection conn = (Connection)_conns.get(ident);

        // open the connection if we haven't already
        if (conn == null) {
            Properties props =
                PropertiesUtil.getSubProperties(_props, ident);

            // get the JDBC configuration info
            String err = "No driver class specified [ident=" + ident + "].";
            String driver = requireProp(props, "driver", err);
            err = "No driver URL specified [ident=" + ident + "].";
            String url = requireProp(props, "url", err);
            err = "No driver username specified [ident=" + ident + "].";
            String username = requireProp(props, "username", err);
            err = "No driver password specified [ident=" + ident + "].";
            String password = requireProp(props, "password", err);

            // load up the driver class
            try {
                Class.forName(driver);
            } catch (Exception e) {
                err = "Error loading driver [ident=" + ident +
                    ", class=" + driver + "].";
                throw new PersistenceException(err, e);
            }

            // create the connection
            try {
                conn = DriverManager.getConnection(url, username, password);
            } catch (SQLException sqe) {
                err = "Error creating database connection " +
                    "[ident=" + ident + "].";
                throw new PersistenceException(err, sqe);
            }

            // cache the connection
            _conns.put(ident, conn);
        }

        return conn;
    }

    // documentation inherited
    public void releaseConnection (String ident, Connection conn)
    {
        // nothing to do here, all is well
    }

    // documentation inherited
    public void connectionFailed (String ident, Connection conn,
                                  SQLException error)
    {
        // attempt to close the connection
        try {
            conn.close();
        } catch (SQLException sqe) {
            Log.warning("Error closing failed connection [ident=" + ident +
                        ", error=" + sqe + "].");
        }

        // and remove it from the cache
        _conns.remove(ident);
    }

    protected static String requireProp (Properties props,
					 String name, String errmsg)
	throws PersistenceException
    {
	String value = props.getProperty(name);
	if (StringUtil.blank(value)) {
	    throw new PersistenceException(errmsg);
	}
	return value;
    }

    protected Properties _props;
    protected HashMap _conns = new HashMap();
}
