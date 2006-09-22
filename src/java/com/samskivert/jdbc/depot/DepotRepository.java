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
import com.samskivert.jdbc.DuplicateKeyException;

/**
 * Provides a base for classes that provide access to persistent objects. Also
 * defines the mechanism by which all persistent queries and updates are routed
 * through the distributed cache.
 */
public class DepotRepository
{
    /**
     * Creates a repository with the supplied connection provider and its own
     * private persistence context.
     */
    protected DepotRepository (ConnectionProvider conprov)
    {
        this(conprov, new PersistenceContext());
    }

    /**
     * Creates a repository with the supplied connection provider and
     * persistence context.
     */
    protected DepotRepository (
        ConnectionProvider conprov, PersistenceContext context)
    {
        _conprov = conprov;
        _context = context;
    }

    /**
     * Loads the persistent object that matches the specified primary key.
     */
    protected <T> T load (Class<T> type, Comparable primaryKey)
        throws PersistenceException
    {
        return load(type, getMarshaller(type).makePrimaryKey(primaryKey));
    }

    /**
     * Loads the first persistent object that matches the supplied key.
     */
    protected <T> T load (Class<T> type, Key key)
        throws PersistenceException
    {
        final DepotMarshaller<T> marsh = getMarshaller(type);
        return invoke(new Query<T>(key) {
            public T invoke (Connection conn) throws SQLException {
                PreparedStatement stmt = marsh.createQuery(conn, _key);
                try {
                    T result = null;
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        result = marsh.createObject(rs);
                    }
                    // TODO: if (rs.next()) issue warning?
                    rs.close();
                    return result;

                } finally {
                    stmt.close();
                }
            }
        });
    }

    /**
     * Loads all persistent objects of the specified type.
     */
    protected <T,C extends Collection<T>> Collection<T> findAll (Class<T> type)
        throws PersistenceException
    {
        return findAll(type, null);
    }

    /**
     * Loads all persistent objects that match the specified key.
     */
    protected <T,C extends Collection<T>> Collection<T> findAll (
        Class<T> type, Key key)
        throws PersistenceException
    {
        final DepotMarshaller<T> marsh = getMarshaller(type);
        return invoke(new Query<ArrayList<T>>(key) {
            public ArrayList<T> invoke (Connection conn) throws SQLException {
                PreparedStatement stmt = marsh.createQuery(conn, _key);
                try {
                    ArrayList<T> results = new ArrayList<T>();
                    ResultSet rs = stmt.executeQuery();
                    while (rs.next()) {
                        results.add(marsh.createObject(rs));
                    }
                    return results;

                } finally {
                    stmt.close();
                }
            }
        });
    }

    /**
     * Inserts the supplied persistent object into the database, assigning its
     * primary key (if it has one) in the process.
     *
     * @return the number of rows modified by this action, this should always
     * be one.
     */
    protected int insert (final Object record)
        throws PersistenceException
    {
        final DepotMarshaller marsh = getMarshaller(record.getClass());
        return invoke(new Modifier(marsh.getPrimaryKey(record)) {
            public int invoke (Connection conn) throws SQLException {
                marsh.assignPrimaryKey(conn, record, false);
                PreparedStatement stmt = marsh.createInsert(conn, record);
                try {
                    int mods = stmt.executeUpdate();
                    marsh.assignPrimaryKey(conn, record, true);
                    return mods;
                } finally {
                    stmt.close();
                }
            }
        });
    }

    /**
     * Updates all fields of the supplied persistent object, using its primary
     * key to identify the row to be updated.
     *
     * @return the number of rows modified by this action.
     */
    protected int update (final Object record)
        throws PersistenceException
    {
        final DepotMarshaller marsh = getMarshaller(record.getClass());
        return invoke(new Modifier(marsh.getPrimaryKey(record)) {
            public int invoke (Connection conn) throws SQLException {
                PreparedStatement stmt = marsh.createUpdate(conn, record, _key);
                try {
                    return stmt.executeUpdate();
                } finally {
                    stmt.close();
                }
            }
        });
    }

    /**
     * Updates just the specified fields of the supplied persistent object,
     * using its primary key to identify the row to be updated.
     *
     * @return the number of rows modified by this action.
     */
    protected int update (final Object record, final String ... modifiedFields)
        throws PersistenceException
    {
        final DepotMarshaller marsh = getMarshaller(record.getClass());
        return invoke(new Modifier(marsh.getPrimaryKey(record)) {
            public int invoke (Connection conn) throws SQLException {
                PreparedStatement stmt = marsh.createUpdate(
                    conn, record, _key, modifiedFields);
                try {
                    return stmt.executeUpdate();
                } finally {
                    stmt.close();
                }
            }
        });
    }

    /**
     * Updates the specified columns for all persistent objects matching the
     * supplied primary key.
     *
     * @param type the type of the persistent object to be modified.
     * @param primaryKey the primary key to match in the update.
     * @param fieldsValues an array containing the names of the fields/columns
     * and the values to be assigned, in key, value, key, value, etc. order.
     *
     * @return the number of rows modified by this action.
     */
    protected <T> int updatePartial (
        Class<T> type, Comparable primaryKey, Object ... fieldsValues)
        throws PersistenceException
    {
        return updatePartial(
            type, getMarshaller(type).makePrimaryKey(primaryKey), fieldsValues);
    }

    /**
     * Updates the specified columns for all persistent objects matching the
     * supplied key.
     *
     * @param type the type of the persistent object to be modified.
     * @param key the key to match in the update.
     * @param fieldsValues an array containing the names of the fields/columns
     * and the values to be assigned, in key, value, key, value, etc. order.
     *
     * @return the number of rows modified by this action.
     */
    protected <T> int updatePartial (
        Class<T> type, Key key, Object ... fieldsValues)
        throws PersistenceException
    {
        // separate the arguments into keys and values
        final String[] fields = new String[fieldsValues.length/2];
        final Object[] values = new Object[fields.length];
        for (int ii = 0, idx = 0; ii < fields.length; ii++) {
            fields[ii] = (String)fieldsValues[idx++];
            values[ii] = fieldsValues[idx++];
        }

        final DepotMarshaller marsh = getMarshaller(type);
        return invoke(new Modifier(key) {
            public int invoke (Connection conn) throws SQLException {
                PreparedStatement stmt = marsh.createPartialUpdate(
                    conn, _key, fields, values);
                try {
                    return stmt.executeUpdate();
                } finally {
                    stmt.close();
                }
            }
        });
    }

    /**
     * Updates the specified columns for all persistent objects matching the
     * supplied primary key. The values in this case must be literal SQL to be
     * inserted into the update statement. In general this is used when you
     * want to do something like the following:
     *
     * <pre>
     * update FOO set BAR = BAR + 1;
     * update BAZ set BIF = NOW();
     * </pre>
     *
     * @param type the type of the persistent object to be modified.
     * @param primaryKey the key to match in the update.
     * @param fieldsValues an array containing the names of the fields/columns
     * and the values to be assigned, in key, literal value, key, literal
     * value, etc. order.
     *
     * @return the number of rows modified by this action.
     */
    protected <T> int updateLiteral (
        Class<T> type, Comparable primaryKey, String ... fieldsValues)
        throws PersistenceException
    {
        return updateLiteral(
            type, getMarshaller(type).makePrimaryKey(primaryKey), fieldsValues);
    }

    /**
     * Updates the specified columns for all persistent objects matching the
     * supplied primary key. The values in this case must be literal SQL to be
     * inserted into the update statement. In general this is used when you
     * want to do something like the following:
     *
     * <pre>
     * update FOO set BAR = BAR + 1;
     * update BAZ set BIF = NOW();
     * </pre>
     *
     * @param type the type of the persistent object to be modified.
     * @param key the key to match in the update.
     * @param fieldsValues an array containing the names of the fields/columns
     * and the values to be assigned, in key, literal value, key, literal
     * value, etc. order.
     *
     * @return the number of rows modified by this action.
     */
    protected <T> int updateLiteral (
        Class<T> type, Key key, String ... fieldsValues)
        throws PersistenceException
    {
        // separate the arguments into keys and values
        final String[] fields = new String[fieldsValues.length/2];
        final String[] values = new String[fields.length];
        for (int ii = 0, idx = 0; ii < fields.length; ii++) {
            fields[ii] = fieldsValues[idx++];
            values[ii] = fieldsValues[idx++];
        }

        final DepotMarshaller marsh = getMarshaller(type);
        return invoke(new Modifier(key) {
            public int invoke (Connection conn) throws SQLException {
                PreparedStatement stmt = marsh.createLiteralUpdate(
                    conn, _key, fields, values);
                try {
                    return stmt.executeUpdate();
                } finally {
                    stmt.close();
                }
            }
        });
    }

    /**
     * Stores the supplied persisent object in the database. If it has no
     * primary key assigned (it is null or zero), it will be inserted
     * directly. Otherwise an update will first be attempted and if that
     * matches zero rows, the object will be inserted.
     *
     * @return the number of rows modified by this action, this should always
     * be one.
     */
    protected int store (final Object record)
        throws PersistenceException
    {
        final DepotMarshaller marsh = getMarshaller(record.getClass());
        return invoke(new Modifier(marsh.getPrimaryKey(record)) {
            public int invoke (Connection conn) throws SQLException {
                PreparedStatement stmt = null;
                try {
                    // if our primary key is null or is the integer 0, assume
                    // the record has never before been persisted and insert
                    if (_key != null && !Integer.valueOf(0).equals(_key)) {
                        stmt = marsh.createUpdate(conn, record, _key);
                        int mods = stmt.executeUpdate();
                        if (mods > 0) {
                            return mods;
                        }
                        stmt.close();
                    }

                    // if the update modified zero rows or the primary key was
                    // obviously unset, do an insertion
                    marsh.assignPrimaryKey(conn, record, false);
                    stmt = marsh.createInsert(conn, record);
                    int mods = stmt.executeUpdate();
                    marsh.assignPrimaryKey(conn, record, true);
                    return mods;

                } finally {
                    stmt.close();
                }
            }
        });
    }

    /**
     * Deletes all persistent objects from the database with a primary key
     * matching the primary key of the supplied object.
     *
     * @return the number of rows deleted by this action.
     */
    protected <T> int delete (T record)
        throws PersistenceException
    {
        @SuppressWarnings("unchecked") Class<T> type =
            (Class<T>)record.getClass();
        DepotMarshaller<T> marsh = getMarshaller(type);
        return deleteAll(type, marsh.getPrimaryKey(record));
    }

    /**
     * Deletes all persistent objects from the database with a primary key
     * matching the supplied primary key.
     *
     * @return the number of rows deleted by this action.
     */
    protected <T> int delete (Class<T> type, Comparable primaryKey)
        throws PersistenceException
    {
        return deleteAll(type, getMarshaller(type).makePrimaryKey(primaryKey));
    }

    /**
     * Deletes all persistent objects from the database that match the supplied
     * key.
     *
     * @return the number of rows deleted by this action.
     */
    protected <T> int deleteAll (Class<T> type, Key key)
        throws PersistenceException
    {
        final DepotMarshaller marsh = getMarshaller(type);
        return invoke(new Modifier(key) {
            public int invoke (Connection conn) throws SQLException {
                PreparedStatement stmt = marsh.createDelete(conn, _key);
                try {
                    return stmt.executeUpdate();
                } finally {
                    stmt.close();
                }
            }
        });
    }

    protected <T> DepotMarshaller<T> getMarshaller (Class<T> type)
        throws PersistenceException
    {
        @SuppressWarnings("unchecked")DepotMarshaller<T> marshaller =
            (DepotMarshaller<T>)_marshallers.get(type);
        if (marshaller == null) {
            _marshallers.put(
                type, marshaller = new DepotMarshaller<T>(type, _context));
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

    protected int invoke (Modifier modifier)
        throws PersistenceException
    {
        // TODO: invalidate the cache using the modifier's key

        // TODO: retry query on transient failure
        Connection conn = _conprov.getConnection(getIdent(), false);
        try {
            return modifier.invoke(conn);

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
            _conprov.releaseConnection(getIdent(), false, conn);
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

        public void bindArguments (PreparedStatement stmt, int startIdx)
            throws SQLException
        {
            for (int ii = 0; ii < indices.length; ii++) {
                stmt.setObject(startIdx++, values[ii]);
            }
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

        public abstract T invoke (Connection conn) throws SQLException;

        protected Query (Key key)
        {
            _key = key;
        }

        protected Key _key;
    }

    protected static abstract class CollectionQuery<T extends Collection>
        extends Query<T>
    {
        public CollectionQuery (Key key) {
            super(key);
        }

        public abstract T invoke (Connection conn) throws SQLException;
    }

    protected static abstract class Modifier
    {
        public Key getKey ()
        {
            return _key;
        }

        public abstract int invoke (Connection conn) throws SQLException;

        protected Modifier (Key key)
        {
            _key = key;
        }

        protected Key _key;
    }

    protected ConnectionProvider _conprov;
    protected PersistenceContext _context;
    protected HashMap<Class<?>, DepotMarshaller<?>> _marshallers =
        new HashMap<Class<?>, DepotMarshaller<?>>();
}
