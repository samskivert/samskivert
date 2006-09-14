//
// samskivert library - useful routines for java programs
// Copyright (C) 2006 Michael Bayne
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import com.samskivert.io.PersistenceException;
import com.samskivert.jdbc.ConnectionProvider;

/**
 * Provides a base for classes that provide access to persistent objects. Also
 * defines the mechanism by which all persistent queries and updates are routed
 * through the distributed cache.
 */
public class DepotRepository
{
    protected DepotRepository (ConnectionProvider conprov)
    {
        _conprov = conprov;
    }

    protected <T> T load (Class<T> type, Comparable primaryKey)
        throws PersistenceException
    {
        DepotMarshaller<T> marsh = getMarshaller(type);
        Key key = new Key(marsh.getPrimaryKey(), primaryKey);
        return invoke(new ObjectQuery<T>(marsh, key));
    }

    protected <T> T load (Class<T> type, Key key)
        throws PersistenceException
    {
        DepotMarshaller<T> marsh = getMarshaller(type);
        return invoke(new ObjectQuery<T>(marsh, key));
    }

    protected <T,C extends Collection<T>> Collection<T> findAll (
        Class<T> type, Key key)
        throws PersistenceException
    {
        DepotMarshaller<T> marsh = getMarshaller(type);
        return invoke(new ObjectCollectionQuery<T>(marsh, key));
    }

    protected <T extends Collection> T findAll (
        String index, Comparable key, CollectionQuery<T> query)
        throws PersistenceException
    {
        return null;
    }

    protected void insert (Object record)
        throws PersistenceException
    {
    }

    protected int update (Object record)
        throws PersistenceException
    {
        return 0;
    }

    protected int update (Object record, String ... modifiedFields)
        throws PersistenceException
    {
        return 0;
    }

    protected <T> int updatePartial (Class<T> type,
        Comparable primaryKey, Object ... fieldsValues)
        throws PersistenceException
    {
        return 0;
    }

    protected <T> int updatePartial (Class<T> type,
        Key key, Object ... fieldsValues)
        throws PersistenceException
    {
        return 0;
    }

    protected <T> int updateLiteral (Class<T> type,
        Comparable primaryKey, Object ... fieldsValues)
    {
        return 0;
    }

    protected <T> int updateLiteral (Class<T> type,
        Key key, Object ... fieldsValues)
    {
        return 0;
    }

    protected int store (Object record)
        throws PersistenceException
    {
        return 0;
    }

    protected void delete (Object record)
        throws PersistenceException
    {
    }

    protected <T> void delete (Class<T> type, Comparable primaryKey)
        throws PersistenceException
    {
    }

    protected <T> void deleteAll (Class<T> type, Key key)
        throws PersistenceException
    {
    }

    protected <T> DepotMarshaller<T> getMarshaller (Class<T> type)
    {
        @SuppressWarnings("unchecked")DepotMarshaller<T> marshaller =
            (DepotMarshaller<T>)_marshallers.get(type);
        if (marshaller == null) {
            _marshallers.put(type, marshaller = new DepotMarshaller<T>(type));
        }
        return marshaller;
    }

    protected <T> T invoke (Query<T> query)
        throws PersistenceException
    {
        // TODO: check the cache using query.getKey()

        // TODO: retry query on transient failure
        Connection conn = _conprov.getConnection(getIdent(), true);
        try {
            return query.invoke(conn);

        } catch (SQLException sqe) {
            throw new PersistenceException("Query failure " + query, sqe);

        } finally {
            _conprov.releaseConnection(getIdent(), true, conn);
        }
    }

    /**
     * Returns the identifier to be used when obtaining JDBC connections from
     * our connection provider. The default implementation uses the class name
     * of this repository.
     */
    protected String getIdent ()
    {
        return getClass().getName();
    }

    protected static class Key
    {
        public String[] indices;
        public Comparable[] values;

        public Key (String[] indices, Comparable[] values)
        {
            this.indices = indices;
            this.values = values;
        }

        public Key (String index, Comparable value)
        {
            this(new String[] { index }, new Comparable[] { value });
        }

        public String toWhereClause ()
        {
            StringBuilder where = new StringBuilder();
            for (int ii = 0; ii < indices.length; ii++) {
                if (ii > 0) {
                    where.append(" and ");
                }
                where.append(indices[ii]).append(" = ?");
            }
            return where.toString();
        }
    }

    protected static class Key2 extends Key
    {
        public Key2 (String index1, Comparable value1,
            String index2, Comparable value2)
        {
            super(new String[] { index1, index2 },
                new Comparable[] { value1, value2 });
        }
    }

    protected static abstract class Query<T>
    {
        public Key getKey ()
        {
            return _key;
        }

        public abstract T invoke (Connection conn)
            throws SQLException, PersistenceException;

        protected Query (Key key)
        {
            _key = key;
        }

        protected Key _key;
    }

    protected static abstract class CollectionQuery<T extends Collection>
    {
        public abstract T invoke (Connection conn)
            throws SQLException, PersistenceException;
    }

    protected static class ObjectQuery<T> extends Query<T>
    {
        public ObjectQuery (DepotMarshaller<T> marsh, Key key) {
            super(key);
            _marsh = marsh;
        }

        public T invoke (Connection conn)
            throws SQLException, PersistenceException
        {
            PreparedStatement stmt = _marsh.createQuery(conn, _key);
            try {
                T result = null;
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    result = _marsh.createObject(rs);
                }
                // TODO: if (rs.next()) issue warning?
                rs.close();
                return result;

            } finally {
                stmt.close();
            }
        }

        protected DepotMarshaller<T> _marsh;
    }

    protected static class ObjectCollectionQuery<T>
        extends Query<ArrayList<T>>
    {
        public ObjectCollectionQuery (DepotMarshaller<T> marsh, Key key) {
            super(key);
            _marsh = marsh;
        }

        public ArrayList<T> invoke (Connection conn)
            throws SQLException, PersistenceException
        {
            PreparedStatement stmt = _marsh.createQuery(conn, _key);
            try {
                ArrayList<T> results = new ArrayList<T>();
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    results.add(_marsh.createObject(rs));
                }
                return results;

            } finally {
                stmt.close();
            }
        }

        protected DepotMarshaller<T> _marsh;
    }

//     protected static abstract class InstanceUpdate<T>
//     {
//         public void invoke (T object)
//             throws PersistenceException;
//     }

//     protected static abstract class CollectionUpdate<T>
//     {
//         public void invoke (Collection<T> collection)
//             throws PersistenceException;
//     }

    protected ConnectionProvider _conprov;

    protected HashMap<Class<?>, DepotMarshaller<?>> _marshallers =
        new HashMap<Class<?>, DepotMarshaller<?>>();
}
