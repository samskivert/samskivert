//
// $Id: Repository.java,v 1.1 2001/02/13 00:25:14 mdb Exp $

package com.samskivert.jdbc;

import java.sql.*;
import java.util.List;
import java.util.Properties;

import com.samskivert.util.*;
import jora.*;

/**
 * The repository class provides basic functionality upon which to build
 * an interface to a repository of information stored in a database (a
 * table or set of tables).
 *
 * <p> These services are based on the JORA Java/RDBMS interoperability
 * package.
 */
public abstract class Repository
{
    /**
     * Creates the repository and opens music database. A properties
     * object should be supplied with the following fields:
     *
     * <pre>
     * driver=[jdbc driver class]
     * url=[jdbc driver url]
     * username=[jdbc username]
     * password=[jdbc password]
     * </pre>
     *
     * @param props a properties object containing the configuration
     * parameters for the repository.
     */
    public Repository (Properties props)
	throws SQLException
    {
	// create our JORA session
	String dclass =
	    requireProp(props, "driver", "No driver class specified.");
	_session = new Session(dclass);

	// connect the session to the database
	String url =
	    requireProp(props, "url", "No driver url specified.");
	String username =
	    requireProp(props, "username", "No driver username specified.");
	String password =
	    requireProp(props, "password", "No driver password specified.");
	_session.open(url, username, password);

	// set auto-commit to false
	_session.connection.setAutoCommit(false);

	// create our table objects
	createTables();
    }

    /**
     * After the database session is begun, this function will be called
     * to give the repository implementation the opportunity to create its
     * table objects. As this is done during the initialization of the
     * repository, the implementation has a chance to fail the entire
     * repository creation process if the necessary tables do not exist.
     */
    protected abstract void createTables () throws SQLException;

    protected static String requireProp (Properties props,
					 String name, String errmsg)
	throws SQLException
    {
	String value = props.getProperty(name);
	if (StringUtil.blank(value)) {
	    throw new SQLException(errmsg);
	}
	return value;
    }

    /**
     * A repository should be shutdown before the calling code disposes of
     * it. This allows the repository to cleanly terminate the underlying
     * database services.
     */
    public void shutdown ()
	throws SQLException
    {
	_session.close();
    }

    protected Session _session;
}
