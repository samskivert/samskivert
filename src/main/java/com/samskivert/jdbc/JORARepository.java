//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.jdbc;

import java.sql.*;
import java.util.ArrayList;

import com.samskivert.io.PersistenceException;
import com.samskivert.jdbc.jora.*;

/**
 * The JORA repository simplifies the process of building persistence
 * services that make use of the JORA object relational mapping package.
 *
 * @see com.samskivert.jdbc.jora.Table
 */
public abstract class JORARepository extends SimpleRepository
{
    /**
     * Creates and initializes a JORA repository which will access the
     * database identified by the supplied database identifier.
     *
     * @param provider the connection provider which will be used to
     * obtain our database connection.
     * @param dbident the identifier of the database that will be accessed
     * by this repository.
     */
    public JORARepository (ConnectionProvider provider, String dbident)
    {
        super(provider, dbident);

        // create our tables
        createTables();
    }

    /**
     * Inserts the supplied object into the specified table.
     *
     * @return a call to {@link DatabaseLiaison#lastInsertedId} made
     * immediately following the insert.
     */
    protected <T> int insert (final Table<T> table, final T object)
        throws PersistenceException
    {
        return executeUpdate(new Operation<Integer>() {
            public Integer invoke (Connection conn, DatabaseLiaison liaison)
                throws SQLException, PersistenceException
            {
                table.insert(conn, object);
                return liaison.lastInsertedId(conn, null, table.getName(), "TODO");
            }
        });
    }

    /**
     * Updates the supplied object in the specified table.
     *
     * @return the number of rows modified by the update.
     */
    protected <T> int update (final Table<T> table, final T object)
        throws PersistenceException
    {
        return executeUpdate(new Operation<Integer>() {
            public Integer invoke (Connection conn, DatabaseLiaison liaison)
                throws SQLException, PersistenceException
            {
                return table.update(conn, object);
            }
        });
    }

    /**
     * Updates fields specified by the supplied field mask in the supplied
     * object in the specified table.
     *
     * @return the number of rows modified by the update.
     */
    protected <T> int update (final Table<T> table, final T object,
                              final FieldMask mask)
        throws PersistenceException
    {
        return executeUpdate(new Operation<Integer>() {
            public Integer invoke (Connection conn, DatabaseLiaison liaison)
                throws SQLException, PersistenceException
            {
                return table.update(conn, object, mask);
            }
        });
    }

    /**
     * Loads all objects from the specified table that match the supplied
     * query.
     */
    protected <T> ArrayList<T> loadAll (final Table<T> table,
                                        final String query)
        throws PersistenceException
    {
        return execute(new Operation<ArrayList<T>>() {
            public ArrayList<T> invoke (
                Connection conn, DatabaseLiaison liaison)
                throws SQLException, PersistenceException
            {
                return table.select(conn, query).toArrayList();
            }
        });
    }

    /**
     * Loads all objects from the specified table that match the supplied
     * query, joining with the supplied auxiliary table(s).
     */
    protected <T> ArrayList<T> loadAll (
        final Table<T> table, final String auxtable, final String query)
        throws PersistenceException
    {
        return execute(new Operation<ArrayList<T>>() {
            public ArrayList<T> invoke (
                Connection conn, DatabaseLiaison liaison)
                throws SQLException, PersistenceException
            {
                return table.select(conn, auxtable, query).toArrayList();
            }
        });
    }

    /**
     * Loads all objects from the specified table that match the supplied
     * example.
     */
    protected <T> ArrayList<T> loadAllByExample (
        final Table<T> table, final T example)
        throws PersistenceException
    {
        return execute(new Operation<ArrayList<T>>() {
            public ArrayList<T> invoke (
                Connection conn, DatabaseLiaison liaison)
                throws SQLException, PersistenceException
            {
                return table.queryByExample(conn, example).toArrayList();
            }
        });
    }

    /**
     * Loads all objects from the specified table that match the supplied
     * example.
     */
    protected <T> ArrayList<T> loadAllByExample (
        final Table<T> table, final T example, final FieldMask mask)
        throws PersistenceException
    {
        return execute(new Operation<ArrayList<T>>() {
            public ArrayList<T> invoke (
                Connection conn, DatabaseLiaison liaison)
                throws SQLException, PersistenceException
                {
                return table.queryByExample(conn, example, mask).toArrayList();
            }
        });
    }

    /**
     * Loads a single object from the specified table that matches the
     * supplied query. <em>Note:</em> the query should match one or zero
     * records, not more.
     */
    protected <T> T load (final Table<T> table, final String query)
        throws PersistenceException
    {
        return execute(new Operation<T>() {
            public T invoke (Connection conn, DatabaseLiaison liaison)
                throws SQLException, PersistenceException
            {
                return table.select(conn, query).get();
            }
        });
    }

    /**
     * Loads a single object from the specified table that matches the supplied
     * query, joining with the supplied auxiliary table(s). <em>Note:</em> the
     * query should match one or zero records, not more.
     */
    protected <T> T load (
        final Table<T> table, final String auxtable, final String query)
        throws PersistenceException
    {
        return execute(new Operation<T>() {
            public T invoke (Connection conn, DatabaseLiaison liaison)
                throws SQLException, PersistenceException
            {
                return table.select(conn, auxtable, query).get();
            }
        });
    }

    /**
     * Loads a single object from the specified table that matches the
     * supplied example. <em>Note:</em> the query should match one or zero
     * records, not more.
     */
    protected <T> T loadByExample (final Table<T> table, final T example)
        throws PersistenceException
    {
        return execute(new Operation<T>() {
            public T invoke (Connection conn, DatabaseLiaison liaison)
                throws SQLException, PersistenceException
            {
                return table.queryByExample(conn, example).get();
            }
        });
    }

    /**
     * Loads a single object from the specified table that matches the
     * supplied example. <em>Note:</em> the query should match one or zero
     * records, not more.
     */
    protected <T> T loadByExample (
        final Table<T> table, final T example, final FieldMask mask)
        throws PersistenceException
    {
        return execute(new Operation<T>() {
            public T invoke (Connection conn, DatabaseLiaison liaison)
                throws SQLException, PersistenceException
            {
                return table.queryByExample(conn, example, mask).get();
            }
        });
    }

    /**
     * First attempts to update the supplied object and if that modifies
     * zero rows, inserts the object into the specified table. The table
     * must be configured to store items of the supplied type.
     *
     * @return -1 if the object was updated, the last inserted id if it was
     * inserted.
     */
    protected <T> int store (final Table<T> table, final T object)
        throws PersistenceException
    {
        return executeUpdate(new Operation<Integer>() {
            public Integer invoke (Connection conn, DatabaseLiaison liaison)
                throws SQLException, PersistenceException
            {
                if (table.update(conn, object) == 0) {
                    table.insert(conn, object);
                    return liaison.lastInsertedId(conn, null, table.getName(), "TODO");
                }
                return -1;
            }
        });
    }

    /**
     * Updates the specified field in the supplied object (which must
     * correspond to the supplied table).
     *
     * @return the number of rows modified by the update.
     */
    protected <T> int updateField (
        final Table<T> table, final T object, String field)
        throws PersistenceException
    {
        final FieldMask mask = table.getFieldMask();
        mask.setModified(field);
        return executeUpdate(new Operation<Integer>() {
            public Integer invoke (Connection conn, DatabaseLiaison liaison)
                throws SQLException, PersistenceException
            {
                return table.update(conn, object, mask);
            }
        });
    }

    /**
     * Updates the specified fields in the supplied object (which must
     * correspond to the supplied table).
     *
     * @return the number of rows modified by the update.
     */
    protected <T> int updateFields (
        final Table<T> table, final T object, String[] fields)
        throws PersistenceException
    {
        final FieldMask mask = table.getFieldMask();
        for (int ii = 0; ii < fields.length; ii++) {
            mask.setModified(fields[ii]);
        }
        return executeUpdate(new Operation<Integer>() {
            public Integer invoke (Connection conn, DatabaseLiaison liaison)
                throws SQLException, PersistenceException
            {
                return table.update(conn, object, mask);
            }
        });
    }

    /**
     * Deletes the specified object from the table.
     *
     * @return the number of rows deleted.
     */
    protected <T> int delete (final Table<T> table, final T object)
        throws PersistenceException
    {
        return executeUpdate(new Operation<Integer>() {
            public Integer invoke (Connection conn, DatabaseLiaison liaison)
                throws SQLException, PersistenceException
            {
                return table.delete(conn, object);
            }
        });
    }

    /**
     * During construction, this function will be called to give the repository
     * implementation the opportunity to create its table objects.
     */
    protected abstract void createTables ();
}
