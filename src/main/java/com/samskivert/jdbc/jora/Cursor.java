//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.jdbc.jora;

import java.util.*;
import java.sql.*;

import static com.samskivert.jdbc.Log.log;

/**
 * Cursor is used for successive access to records fetched by SELECT statement.
 * As far as records can be retrived from several derived tables (polymorphic
 * form of select), this class can issue several requests to database. Cursor
 * also provides methods for updating/deleting current record.
 */
public class Cursor<V>
{
    /**
     * A cursor is initially positioned before its first row; the first call to
     * next makes the first row the current row; the second call makes the
     * second row the current row, etc.
     *
     * <P> If an input stream from the previous row is open, it is implicitly
     * closed. The ResultSet's warning chain is cleared when a new row is read.
     *
     * @return object constructed from fetched record or null if there are no
     * more rows
     */
    public V next ()
        throws SQLException
    {
        // if we closed everything up after the last call to next(),
        // table will be null here and we should bail immediately
        if (_table == null) {
            return null;
        }

        if (_result == null) {
            if (_qbeObject != null) {
                PreparedStatement qbeStmt = _conn.prepareStatement(_query);
                _table.bindQueryVariables(qbeStmt, _qbeObject, _qbeMask);
                _result = qbeStmt.executeQuery();
                _stmt = qbeStmt;
            } else {
                if (_stmt == null) {
                    _stmt = _conn.createStatement();
                }
                _result = _stmt.executeQuery(_query);
            }
        }
        if (_result.next()) {
            return _currObject = _table.load(_result);
        }

        _result.close();
        _result = null;
        _currObject = null;
        _table = null;

        if (_stmt != null) {
            _stmt.close();
        }
        return null;
    }

    /**
     * Returns the first element matched by this cursor or null if no elements
     * were matched. Checks to ensure that no subsequent elements were matched
     * by the query, logs a warning if there were spurious additional matches.
     */
    public V get ()
        throws SQLException
    {
        V result = next();
        if (result != null) {
            int spurious = 0;
            while (next() != null) {
                spurious++;
            }
            if (spurious > 0) {
                log.warning("Cursor.get() quietly tossed " + spurious + " spurious additional " +
                            "records.", "query", _query);
            }
        }
        return result;
    }

    /**
     * Update current record pointed by cursor. This method can be called only
     * after next() method, which returns non-null object. This objects is used
     * to update current record fields.
     *
     * <P> If you are going to update or delete selected records, you should
     * add "for update" clause to select statement. So parameter of
     * <CODE>jora.Table.select()</CODE> statement should contain "for update"
     * clause: <CODE>record.table.Select("where name='xyz' for
     * update");</CODE><P>
     *
     * <I><B>Attention!</B></I> Not all database drivers support update
     * operation with cursor. This method will not work with such database
     * drivers.
     */
    public void update ()
        throws SQLException
    {
        if (_currObject == null) {
            throw new IllegalStateException("No current object");
        }
        _table.updateVariables(_result, _currObject);
    }

    /**
     * Delete current record pointed by cursor. This method can be called only
     * after next() method, which returns non-null object.
     *
     * <P> If you are going to update or delete selected records, you should
     * add "for update" clause to select statement. So parameter of
     * <CODE>jora.Table.select()</CODE> statement should contain "for update"
     * clause: <CODE>record.table.Select("where name='xyz' for
     * update");</CODE><P>
     *
     * <I><B>Attention!</B></I> Not all database drivers support delete
     * operation with cursor.  This method will not work with such database
     * drivers.
     */
    public void delete ()
        throws SQLException
    {
        if (_currObject == null) {
            throw new IllegalStateException("No current object");
        }
        _result.deleteRow();
    }

    /**
     * Close the Cursor, even if we haven't read all the possible objects.
     */
    public void close ()
        throws SQLException
    {
        if (_result != null) {
            _result.close();
            _result = null;
        }
        if (_stmt != null) {
            _stmt.close();
            _stmt = null;
        }
    }

    /**
     * Extracts no more than <I>maxElements</I> records from database and store
     * them into array. It is possible to extract rest records by successive
     * next() or toArray() calls. Selected objects should have now components
     * of InputStream, Blob or Clob type, because their data will be not
     * available after fetching next record.
     *
     * @param maxElements limitation for result array size (and also for number
     *  of fetched records)
     * @return List with objects constructed from fetched records.
     */
    public ArrayList<V> toArrayList (int maxElements)
        throws SQLException
    {
        ArrayList<V> al = new ArrayList<V>(Math.min(maxElements, 100));
        V o;
        while (--maxElements >= 0 && (o = next()) != null) {
            al.add(o);
        }
        return al;
    }

    /**
     * Store all objects returned by SELECT query into a list of Object.
     * Selected objects should have now components of InputStream, Blob or Clob
     * type, because their data will be not available after fetching next
     * record.
     *
     * @return Array with objects constructed from fetched records.
     */
    public ArrayList<V> toArrayList ()
        throws SQLException
    {
        return toArrayList(Integer.MAX_VALUE);
    }

    protected Cursor (Table<V> table, Connection conn, String query)
    {
        _table = table;
        _conn = conn;
        _query = query;
    }

    protected Cursor (Table<V> table, Connection conn, V obj,
                      FieldMask mask, boolean like)
    {
        _table = table;
        _conn = conn;
        _like = like;
        _qbeObject = obj;
        _qbeMask = mask;
        _query = table.buildQueryList(obj, mask, like);
        _stmt = null;
    }

    protected Table<V> _table;
    protected Connection _conn;
    protected ResultSet _result;
    protected String _query;
    protected Statement _stmt;
    protected V _currObject, _qbeObject;
    protected FieldMask _qbeMask;
    protected boolean _like;
}

