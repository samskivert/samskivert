//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2006 Michael Bayne, Pär Winzell
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

package com.samskivert.jdbc.depot;

import java.util.HashMap;

import com.samskivert.jdbc.depot.annotation.TableGenerator;

import java.sql.Connection;
import java.sql.SQLException;

import com.samskivert.io.PersistenceException;

import com.samskivert.util.StringUtil;

import com.samskivert.jdbc.ConnectionProvider;
import com.samskivert.jdbc.DuplicateKeyException;


/**
 * Defines a scope in which global annotations are shared.
 */
public class PersistenceContext
{
    /** Map {@link TableGenerator} instances by name. */
    public HashMap<String, TableGenerator> tableGenerators = new HashMap<String, TableGenerator>();

    /**
     * Creates a persistence context that will use the supplied provider to
     * obtain JDBC connections.
     *
     * @param ident the identifier to provide to the connection provider when
     * requesting a connection.
     */
    public PersistenceContext (String ident, ConnectionProvider conprov)
    {
        _ident = ident;
        _conprov = conprov;
    }

    /**
     * Registers a migration for the specified entity class.
     *
     * <p> This method must be called <b>before</b> an Entity is used by any repository. Thus you
     * should register all migrations immediately after creating your persistence context or if you
     * are careful to ensure that your Entities are only used by a single repository, you can
     * register your migrations in the constructor for that repository.
     *
     * <p> Note that the migration process is as follows:
     *
     * <ul><li> Note the difference between the entity's declared version and the version recorded
     * in the database.
     * <li> Run all registered pre-migrations
     * <li> Perform all default migrations (column additions and removals)
     * <li> Run all registered post-migrations </ul>
     * 
     * Thus you must either be prepared for the entity to be at <b>any</b> version prior to your
     * migration target version because we may start up, find the schema at version 1 and the
     * Entity class at version 8 and do all "standard" migrations in one fell swoop. So if a column
     * got added in version 2 and renamed in version 6 and your migration was registered for
     * version 6 to do that migration, it must be prepared for the column not to exist at all.
     *
     * <p> If you want a completely predictable migration process, never use the default migrations
     * and register a pre-migration for every single schema migration and they will then be
     * guaranteed to be run in registration order and with predictable pre- and post-conditions.
     */
    public <T> void registerMigration (Class<T> type, EntityMigration migration)
    {
        getRawMarshaller(type).registerMigration(migration);
    }

    /**
     * Returns the marshaller for the specified persistent object class, creating and initializing
     * it if necessary.
     */
    public <T> DepotMarshaller<T> getMarshaller (Class<T> type)
        throws PersistenceException
    {
        DepotMarshaller<T> marshaller = getRawMarshaller(type);
        if (!marshaller.isInitialized()) {
            // initialize the marshaller which may create or migrate the table for its underlying
            // persistent object
            marshaller.init(this);
        }
        return marshaller;
    }

    /**
     * Invokes a non-modifying query and returns its result.
     */
    @SuppressWarnings("unchecked")
    public <T> T invoke (Query<T> query)
        throws PersistenceException
    {
        // TODO: check the cache using query.getKey()
        return (T) invoke(query, null, true);
    }

    /**
     * Invokes a modifying query and returns the number of rows modified.
     */
    public int invoke (Modifier modifier)
        throws PersistenceException
    {
        // TODO: invalidate the cache using the modifier's key

        int result = (Integer) invoke(null, modifier, true);
        // TODO: (optionally) cache the results of the modifier
        return result;
    }

    /**
     * Looks up and creates, but does not initialize, the marshaller for the specified Entity
     * type.
     */
    protected <T> DepotMarshaller<T> getRawMarshaller (Class<T> type)
    {
        @SuppressWarnings("unchecked") DepotMarshaller<T> marshaller =
            (DepotMarshaller<T>)_marshallers.get(type);
        if (marshaller == null) {
            _marshallers.put(type, marshaller = new DepotMarshaller<T>(type, this));
        }
        return marshaller;
    }

    /**
     * Internal invoke method that takes care of transient retries
     * for both queries and modifiers.
     */
    protected <T> Object invoke (
        Query<T> query, Modifier modifier, boolean retryOnTransientFailure)
        throws PersistenceException
    {
        boolean isReadOnlyQuery = (query != null);
        Connection conn = _conprov.getConnection(_ident, isReadOnlyQuery);
        try {
            if (isReadOnlyQuery) {
                // if this becomes more complex than this single statement,
                // then this should turn into a method call that contains
                // the complexity
                return query.invoke(conn);

            } else {
                // if this becomes more complex than this single statement,
                // then this should turn into a method call that contains
                // the complexity
                return modifier.invoke(conn);
            }

        } catch (SQLException sqe) {
            if (!isReadOnlyQuery) {
                // convert this exception to a DuplicateKeyException if
                // appropriate
                String msg = sqe.getMessage();
                if (msg != null && msg.indexOf("Duplicate entry") != -1) {
                    throw new DuplicateKeyException(msg);
                }
            }

            // let the provider know that the connection failed
            _conprov.connectionFailed(_ident, isReadOnlyQuery, conn, sqe);
            conn = null;

            if (retryOnTransientFailure && isTransientException(sqe)) {
                // the MySQL JDBC driver has the annoying habit of including
                // the embedded exception stack trace in the message of their
                // outer exception; if I want a fucking stack trace, I'll call
                // printStackTrace() thanksverymuch
                String msg = StringUtil.split(String.valueOf(sqe), "\n")[0];
                Log.info("Transient failure executing operation, " +
                    "retrying [error=" + msg + "].");

            } else {
                String msg = isReadOnlyQuery ? "Query failure " + query
                                             : "Modifier failure " + modifier;
                throw new PersistenceException(msg, sqe);
            }

        } finally {
            _conprov.releaseConnection(_ident, isReadOnlyQuery, conn);
        }

        // if we got here, we want to retry a transient failure
        return invoke(query, modifier, false);
    }

    /**
     * Check whether the specified exception is a transient failure
     * that can be retried.
     */
    protected boolean isTransientException (SQLException sqe)
    {
        // TODO: this is MySQL specific. This was snarfed from MySQLLiaison.
        String msg = sqe.getMessage();
        return (msg != null &&
            (msg.indexOf("Lost connection") != -1 ||
             msg.indexOf("link failure") != -1 ||
             msg.indexOf("Broken pipe") != -1));
    }

    protected String _ident;
    protected ConnectionProvider _conprov;

    protected HashMap<Class<?>, DepotMarshaller<?>> _marshallers =
        new HashMap<Class<?>, DepotMarshaller<?>>();
}
