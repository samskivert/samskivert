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

import java.io.IOException;
import java.sql.*;
import java.util.*;

import com.samskivert.io.PersistenceException;
import com.samskivert.util.ConfigUtil;
import com.samskivert.util.PropertiesUtil;
import com.samskivert.util.StringUtil;

import static com.samskivert.Log.log;

/**
 * The static connection provider generates JDBC connections based on configuration information
 * provided via a properties file. It does no connection pooling and always returns the same
 * connection for a particular identifier (unless that connection need be closed because of a
 * connection failure, in which case it opens a new one the next time the connection is requested).
 *
 * <p> The configuration properties file should contain the following information:
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
 * Where <code>IDENT</code> is the database identifier for a particular database connection. When a
 * particular database identifier is requested, the configuration information will be fetched from
 * the properties.
 *
 * <p> Additionally, a default set of properties can be provided using the identifier
 * <code>default</code>. Values not provided for a specific identifier will be sought from the
 * defaults. For example:
 *
 * <pre>
 * default.driver=[jdbc driver class]
 * default.url=[jdbc driver class]
 *
 * IDENT1.username=[jdbc username]
 * IDENT1.password=[jdbc password]
 *
 * IDENT2.username=[jdbc username]
 * IDENT2.password=[jdbc password]
 *
 * [...]
 * </pre>
 */
public class StaticConnectionProvider implements ConnectionProvider
{
    /**
     * Constructs a static connection provider which will load its configuration from a properties
     * file accessible via the classpath of the running application and identified by the specified
     * path.
     *
     * @param propPath the path (relative to the classpath) to the properties file that will be
     * used for configuration information.
     *
     * @exception IOException thrown if an error occurs locating or loading the specified
     * properties file.
     */
    public StaticConnectionProvider (String propPath)
        throws IOException
    {
        this(ConfigUtil.loadProperties(propPath));
    }

    /**
     * Constructs a static connection provider which will fetch its configuration information from
     * the specified properties object.
     *
     * @param props the configuration for this connection provider.
     */
    public StaticConnectionProvider (Properties props)
    {
        _props = props;
    }

    // from ConnectionProvider
    public String getURL (String ident)
    {
        Properties props = PropertiesUtil.getSubProperties(_props, ident, DEFAULTS_KEY);
        return props.getProperty("url");
    }

    // documentation inherited
    public Connection getConnection (String ident, boolean readOnly)
        throws PersistenceException
    {
        String mapkey = ident + ":" + readOnly;
        Mapping conmap = _idents.get(mapkey);

        // open the connection if we haven't already
        if (conmap == null) {
            Properties props = PropertiesUtil.getSubProperties(_props, ident, DEFAULTS_KEY);

            // get the JDBC configuration info
            String err = "No driver class specified [ident=" + ident + "].";
            String driver = requireProp(props, "driver", err);
            err = "No driver URL specified [ident=" + ident + "].";
            String url = requireProp(props, "url", err);
            err = "No driver username specified [ident=" + ident + "].";
            String username = requireProp(props, "username", err);
            String password = props.getProperty("password", "");
            String autoCommit = props.getProperty("autocommit");

            // if this is a read-only connection, we cache connections by username+url+readOnly to
            // avoid making more that one connection to a particular database server
            String key = username + "@" + url + ":" + readOnly;
            conmap = _keys.get(key);
            if (conmap == null) {
                log.debug("Creating " + key + " for " + ident + ".");
                conmap = new Mapping();
                conmap.key = key;
                conmap.connection = openConnection(driver, url, username, password);

                // if we were requested to configure auto-commit, then do so
                if (autoCommit != null) {
                    try {
                        conmap.connection.setAutoCommit(Boolean.valueOf(autoCommit));
                    } catch (SQLException sqe) {
                        closeConnection(ident, conmap.connection);
                        err = "Failed to configure auto-commit [key=" + key +
                            ", ident=" + ident + ", autoCommit=" + autoCommit + "].";
                        throw new PersistenceException(err, sqe);
                    }
                }

                // make the connection read-only to let the JDBC driver know that it can and should
                // use the read-only mirror(s)
                if (readOnly) {
                    try {
                        conmap.connection.setReadOnly(true);
                    } catch (SQLException sqe) {
                        closeConnection(ident, conmap.connection);
                        err = "Failed to make connection read-only [key=" + key +
                            ", ident=" + ident + "].";
                        throw new PersistenceException(err, sqe);
                    }
                }
                _keys.put(key, conmap);

            } else {
                log.debug("Reusing " + key + " for " + ident + ".");
            }

            // cache the connection
            conmap.idents.add(mapkey);
            _idents.put(mapkey, conmap);
        }

        return conmap.connection;
    }

    // documentation inherited
    public void releaseConnection (String ident, boolean readOnly, Connection conn)
    {
        // nothing to do here, all is well
    }

    // documentation inherited
    public void connectionFailed (
        String ident, boolean readOnly, Connection conn, SQLException error)
    {
        String mapkey = ident + ":" + readOnly;
        Mapping conmap = _idents.get(mapkey);
        if (conmap == null) {
            log.warning("Unknown connection failed!?", "key", mapkey);
            return;
        }

        // attempt to close the connection
        closeConnection(ident, conmap.connection);

        // clear it from our mapping tables
        for (int ii = 0; ii < conmap.idents.size(); ii++) {
            _idents.remove(conmap.idents.get(ii));
        }
        _keys.remove(conmap.key);
    }

    // from ConnectionProvider
    public void shutdown ()
    {
        // close all of the connections
        for (Map.Entry<String, Mapping> entry : _keys.entrySet()) {
            Mapping conmap = entry.getValue();
            try {
                conmap.connection.close();
            } catch (SQLException sqe) {
                log.warning("Error shutting down connection", "key", entry.getKey(), "err", sqe);
            }
        }

        // clear out our mapping tables
        _keys.clear();
        _idents.clear();
    }

    protected Connection openConnection (String driver, String url, String username, String passwd)
        throws PersistenceException
    {
        // create an instance of the driver
        Driver jdriver;
        try {
            jdriver = (Driver)Class.forName(driver).newInstance();
        } catch (Exception e) {
            String err = "Error loading driver [class=" + driver + "].";
            throw new PersistenceException(err, e);
        }

        // create the connection
        try {
            Properties props = new Properties();
            props.put("user", username);
            props.put("password", passwd);
            return jdriver.connect(url, props);

        } catch (SQLException sqe) {
            String err = "Error creating database connection [driver=" + driver + ", url=" + url +
                ", username=" + username + "].";
            throw new PersistenceException(err, sqe);
        }
    }

    protected void closeConnection (String ident, Connection conn)
    {
        try {
            conn.close();
        } catch (SQLException sqe) {
            log.warning("Error closing failed connection", "ident", ident, "error", sqe);
        }
    }

    protected static String requireProp (Properties props, String name, String errmsg)
	throws PersistenceException
    {
	String value = props.getProperty(name);
	if (StringUtil.isBlank(value)) {
            errmsg = "Unable to get connection. " + errmsg; // augment the error message
	    throw new PersistenceException(errmsg);
	}
	return value;
    }

    /** Contains information on a particular connection to which any number of database identifiers
     * can be mapped. */
    protected static class Mapping
    {
        /** The combination of username and JDBC url that uniquely identifies our database
         * connection. */
        public String key;

        /** The connection itself. */
        public Connection connection;

        /** The database identifiers that are mapped to this connection. */
        public List<String> idents = new ArrayList<String>();
    }

    /** Our configuration in the form of a properties object. */
    protected Properties _props;

    /** A mapping from database identifier to connection records. */
    protected HashMap<String,Mapping> _idents = new HashMap<String,Mapping>();

    /** A mapping from connection key to connection records. */
    protected HashMap<String,Mapping> _keys = new HashMap<String,Mapping>();

    /** The key used as defaults for the database definitions. */
    protected static final String DEFAULTS_KEY = "default";
}
