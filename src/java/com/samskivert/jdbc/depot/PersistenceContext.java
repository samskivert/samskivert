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

import javax.persistence.TableGenerator;

import java.sql.Connection;
import java.sql.SQLException;

import com.samskivert.io.PersistenceException;
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
     * Returns the marshaller for the specified persistent object class,
     * creating and initializing it if necessary.
     */
    public <T> DepotMarshaller<T> getMarshaller (Class<T> type)
        throws PersistenceException
    {
        @SuppressWarnings("unchecked") DepotMarshaller<T> marshaller =
            (DepotMarshaller<T>)_marshallers.get(type);
        if (marshaller == null) {
            _marshallers.put(
                type, marshaller = new DepotMarshaller<T>(type, this));
            // initialize the marshaller which may create or migrate the table
            // for its underlying persistent object
            final DepotMarshaller<T> fm = marshaller;
            invoke(new Modifier(null) {
                public int invoke (Connection conn) throws SQLException {
                    fm.init(conn);
                    return 0;
                }
            });
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

        // TODO: retry query on transient failure
        Connection conn = _conprov.getConnection(_ident, true);
        try {
            return query.invoke(conn);

        } catch (SQLException sqe) {
            throw new PersistenceException("Query failure " + query, sqe);

        } finally {
            _conprov.releaseConnection(_ident, true, conn);
        }
    }

    /**
     * Invokes a modifying query and returns the number of rows modified.
     */
    public int invoke (Modifier modifier)
        throws PersistenceException
    {
        // TODO: invalidate the cache using the modifier's key

        // TODO: retry query on transient failure
        Connection conn = _conprov.getConnection(_ident, false);
        try {
            int result = modifier.invoke(conn);
            // TODO: (optionally) cache the results of the modifier
            return result;

        } catch (SQLException sqe) {
            // convert this exception to a DuplicateKeyException if appropriate
            String msg = sqe.getMessage();
            if (msg != null && msg.indexOf("Duplicate entry") != -1) {
                throw new DuplicateKeyException(msg);
            } else {
                throw new PersistenceException(
                    "Modifier failure " + modifier, sqe);
            }

        } finally {
            _conprov.releaseConnection(_ident, false, conn);
        }
    }

    protected String _ident;
    protected ConnectionProvider _conprov;

    protected HashMap<Class<?>, DepotMarshaller<?>> _marshallers =
        new HashMap<Class<?>, DepotMarshaller<?>>();
}
