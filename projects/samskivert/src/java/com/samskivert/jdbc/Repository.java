//
// $Id: Repository.java,v 1.9 2001/08/11 22:43:28 mdb Exp $
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
import java.util.List;
import java.util.Properties;

import com.samskivert.Log;
import com.samskivert.util.*;
import com.samskivert.jdbc.jora.*;

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
     * Creates and initializes the repository. A properties object should
     * be supplied with the following fields:
     *
     * <pre>
     * driver=[jdbc driver class]
     * url=[jdbc driver url]
     * username=[jdbc username]
     * password=[jdbc password]
     * </pre>
     *
     * <p> The connection to the database will not be established until
     * the first operation is executed. It is important that database
     * operations be invoked via <code>execute()</code> rather than
     * directly so that the repository can manage the database session
     * properly (reestablishing the session on transient failures and so
     * on). If a derived class wishes to perform operations directly, it
     * should call <code>ensureConnection()</code> before performing any
     * database activity.
     *
     * @param props a properties object containing the configuration
     * parameters for the repository.
     */
    public Repository (Properties props)
	throws SQLException
    {
        // extract our configuration parameters
	_dclass =
	    requireProp(props, "driver", "No driver class specified.");
	_url =
	    requireProp(props, "url", "No driver url specified.");
	_username =
	    requireProp(props, "username", "No driver username specified.");
	_password =
	    requireProp(props, "password", "No driver password specified.");

        // we attempt to reestablish the connection if there's a transient
        // failure (like the database is down or our old connection gets
        // closed by the database), but we don't want to get too crazy
        // about connecting to the database automatically, so we limit our
        // attempts to twice every thirty seconds
        _throttle = new Throttle(2, 30);
    }

    /** Establishes a connection to the database. */
    protected synchronized void ensureConnection ()
        throws SQLException
    {
        // nothing doing if we've already got a connection
        if (_session != null) {
            return;
        }

        // make sure we don't try to reestablish the connection
        // overzealously in times of lengthy database outage
        if (_throttle.throttleOp()) {
            throw new SQLException("Connection attempt throttled.");
        }

        try {
            // create our JORA session
            _session = new Session(_dclass);

            // connect the session to the database. the only reason
            // session.open() fails is class not found
            if (!_session.open(_url, _username, _password)) {
                throw new SQLException("Unable to load JDBC driver class: " +
                                       _dclass);
            }

            // set auto-commit to false
            if (supportsTransactions()) {
                _session.connection.setAutoCommit(false);
            }

            // create our table objects
            createTables();

        } catch (SQLException sqe) {
            // if we have any problems establishing a connection, close
            // and clear out our session reference to prevent attempted
            // use of a half initialized session
            try {
                shutdown();
            } catch (SQLException sqe2) {
                Log.warning("Error shutting down half-initialized " +
                            "connection [primary_failure=" + sqe +
                            ", secondary_failure=" + sqe2 + "].");
            }
            throw sqe;
        }
    }

    /**
     * Shuts down the existing connection to the database and
     * reestablishes the connection.
     */
    public void reestablishConnection ()
        throws SQLException
    {
        // first close the old connection
        shutdown();
        // then open a new one
        ensureConnection();
    }

    /**
     * After the database session is begun, this function will be called
     * to give the repository implementation the opportunity to create its
     * table objects. If the database session fails, a new session will be
     * established and this function will be called again for the new
     * session.
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
    public synchronized void shutdown ()
	throws SQLException
    {
        if (_session != null) {
            _session.close();
            _session = null;
        }
    }

    /**
     * Used by <code>execute</code>.
     *
     * @see #execute
     */
    protected interface Operation
    {
	public Object invoke () throws SQLException;
    }

    /**
     * Executes the supplied operation. In the event of a transient
     * failure, the repository will attempt to reestablish the database
     * connection and try the operation again.
     *
     * @return whatever value is returned by the invoked operation.
     */
    protected Object execute (Operation op)
	throws SQLException
    {
        return execute(op, true);
    }

    /**
     * Executes the supplied operation followed by a call to commit() on
     * the session unless an SQLException or runtime error occurs, in
     * which case a call to rollback() is executed on the session.
     *
     * @param retryOnTransientFailure if true and the operation fails due
     * to a transient failure (like losing the connection to the database
     * or deadlock detection), the connection to the database will be
     * reestablished and the operation attempted once more.
     *
     * @return whatever value is returned by the invoked operation.
     */
    protected Object execute (Operation op, boolean retryOnTransientFailure)
	throws SQLException
    {
        Object rv = null;

        // make sure our connection is established
        ensureConnection();

	try {
	    // invoke the operation
	    rv = op.invoke();

	    // commit the session
            if (supportsTransactions()) {
                _session.commit();
            }

            // return the operation result
            return rv;

	} catch (SQLException sqe) {
	    // back out our changes if something got hosed
            if (supportsTransactions()) {
                try {
                    _session.rollback();
                } catch (SQLException rbe) {
                    Log.warning("Unable to roll back operation.");
                    Log.logStackTrace(rbe);
                }
            }

            // if this is a transient failure and we've been requested to
            // retry such failures, close and reopen the database
            // connection and try one more time
            if (retryOnTransientFailure && isTransientException(sqe)) {
                Log.info("Transient failure executing operation, " +
                         "retrying [error=" + sqe + "].");
                // to reset the connection, we simply close it. it will be
                // reopened on our subsequent call to execute()
                shutdown();
                return execute(op, false);

            } else {
                Log.info("Non-transient exception, we're hosed " +
                         "[error=" + sqe + "].");
                throw sqe;
            }

	} catch (RuntimeException rte) {
	    // back out our changes if something got hosed
            if (supportsTransactions()) {
                try {
                    _session.rollback();
                } catch (SQLException rbe) {
                    Log.warning("Unable to roll back operation.");
                    Log.logStackTrace(rbe);
                }
            }
	    throw rte;
	}
    }

    /**
     * Specializations of this class for particular RDBMS/JDBC driver
     * combinations should override this method and indicate whether or
     * not transactions are supported.
     */
    protected boolean supportsTransactions ()
    {
        return false;
    }

    /**
     * Determines whether or not the supplied SQL exception is a transient
     * failure, meaning one that is not related to the SQL being executed,
     * but instead to the environment at the time of execution, like the
     * connection to the database having been lost.
     *
     * <p> Specializations of the repository class for particular
     * database/JDBC driver combos should override this function and match
     * the appropriate exceptions.
     *
     * @return true if the exception was thrown due to a transient
     * failure, false if not.
     */
    protected boolean isTransientException (SQLException sqe)
    {
        return false;
    }

    protected Session _session;
    protected String _dclass;
    protected String _url;
    protected String _username;
    protected String _password;

    // we use this to prevent too many database connection attempts
    protected Throttle _throttle;
}
