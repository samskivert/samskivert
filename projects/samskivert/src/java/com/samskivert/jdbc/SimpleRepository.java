//
// $Id: SimpleRepository.java,v 1.3 2001/09/21 03:01:46 mdb Exp $
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
import java.util.Properties;

import com.samskivert.Log;
import com.samskivert.io.PersistenceException;

/**
 * The simple repository should be used for a repository that only needs
 * access to a single JDBC connection instance to perform its persistence
 * services.
 */
public class SimpleRepository extends Repository
{
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
        DatabaseMetaData dmd = null;
        DatabaseLiaison liaison = null;

        // obtain our database connection and associated goodies
        try {
            conn = _provider.getConnection(_dbident);
            conn.setAutoCommit(false);
            dmd = conn.getMetaData();
            liaison = LiaisonRegistry.getLiaison(conn);
            gotConnection(conn);
        } catch (SQLException sqe) {
            String err = "Unable to obtain connection.";
            throw new PersistenceException(err, sqe);
        }

        Object rv = null;

        try {
	    // invoke the operation
	    rv = op.invoke(conn, liaison);

	    // commit the transaction
            if (dmd.supportsTransactions()) {
                conn.commit();
            }

            // return the operation result
            return rv;

	} catch (SQLException sqe) {
	    // back out our changes if something got hosed
            try {
                if (dmd.supportsTransactions()) {
                    conn.rollback();
                }
            } catch (SQLException rbe) {
                Log.warning("Unable to roll back operation.");
                Log.logStackTrace(rbe);
            }

            // let the connection provider know that the connection failed
            _provider.connectionFailed(_dbident, conn, sqe);
            // clear out the reference so that we don't release it later
            conn = null;

            // if this is a transient failure and we've been requested to
            // retry such failures, try one more time
            if (retryOnTransientFailure &&
                liaison.isTransientException(sqe)) {
                Log.info("Transient failure executing operation, " +
                         "retrying [error=" + sqe + "].");
                return execute(op, false);
            }

            String err = "Operation invocation failed.";
            throw new PersistenceException(err, sqe);

	} catch (PersistenceException pe) {
	    // back out our changes if something got hosed
            try {
                if (dmd.supportsTransactions()) {
                    conn.rollback();
                }
            } catch (SQLException rbe) {
                Log.warning("Unable to roll back operation.");
                Log.logStackTrace(rbe);
            }
            throw pe;

	} catch (RuntimeException rte) {
	    // back out our changes if something got hosed
            try {
                if (conn != null && dmd.supportsTransactions()) {
                    conn.rollback();
                }
            } catch (SQLException rbe) {
                Log.warning("Unable to roll back operation.");
                Log.logStackTrace(rbe);
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
     * Called when we fetch a connection from the provider. This gives
     * derived classes an opportunity to configure whatever internals they
     * might be using with the connection that was fetched.
     */
    protected void gotConnection (Connection conn)
    {
    }

    protected String _dbident;
}
