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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;

import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;

import com.samskivert.jdbc.JDBCUtil;
import com.samskivert.jdbc.depot.expression.ColumnExpression;
import com.samskivert.util.ArrayUtil;
import com.samskivert.util.ListUtil;
import com.samskivert.util.StringUtil;

import static com.samskivert.jdbc.depot.Log.log;

/**
 * Handles the marshalling and unmarshalling of persistent instances to JDBC primitives ({@link
 * PreparedStatement} and {@link ResultSet}).
 */
public class DepotMarshaller<T>
{
    /** The name of a private static field that must be defined for all persistent object classes.
     * It is used to handle schema migration. If automatic schema migration is not desired, define
     * this field and set its value to -1. */
    public static final String SCHEMA_VERSION_FIELD = "SCHEMA_VERSION";

    /**
     * Creates a marshaller for the specified persistent object class.
     */
    public DepotMarshaller (Class<T> pclass, PersistenceContext context)
    {
        _pclass = pclass;

        // determine our table name
        _tableName = _pclass.getName();
        _tableName = _tableName.substring(_tableName.lastIndexOf(".")+1);

        // if the entity defines a new TableGenerator, map that in our static table as those are
        // shared across all entities
        TableGenerator generator = pclass.getAnnotation(TableGenerator.class);
        if (generator != null) {
            context.tableGenerators.put(generator.name(), generator);
        }

        // introspect on the class and create marshallers for persistent fields
        ArrayList<String> fields = new ArrayList<String>();
        for (Field field : _pclass.getFields()) {
            int mods = field.getModifiers();

            // check for a static constant schema version
            if ((mods & Modifier.STATIC) != 0 && field.getName().equals(SCHEMA_VERSION_FIELD)) {
                try {
                    _schemaVersion = (Integer)field.get(null);
                } catch (Exception e) {
                    log.log(Level.WARNING, "Failed to read schema version " +
                        "[class=" + _pclass + "].", e);
                }
            }

            // the field must be public, non-static and non-transient
            if (((mods & Modifier.PUBLIC) == 0) || ((mods & Modifier.STATIC) != 0) ||
                field.getAnnotation(Transient.class) != null) {
                continue;
            }

            FieldMarshaller fm = FieldMarshaller.createMarshaller(field);
            _fields.put(fm.getColumnName(), fm);
            fields.add(fm.getColumnName());

            // check to see if this is our primary key
            if (field.getAnnotation(Id.class) != null) {
                if (_pkColumns == null) {
                    _pkColumns = new ArrayList<FieldMarshaller>();
                }
                _pkColumns.add(fm);

                // check if this field defines a new TableGenerator
                generator = field.getAnnotation(TableGenerator.class);
                if (generator != null) {
                    context.tableGenerators.put(generator.name(), generator);
                }
            }
        }

        // if the entity defines a single-columnar primary key, figure out if we will be generating
        // values for it
        if (_pkColumns != null) {
            GeneratedValue gv = null;
            FieldMarshaller keyField = null;
            // loop over fields to see if there's a @GeneratedValue at all
            for (FieldMarshaller field : _pkColumns) {
                gv = field.getGeneratedValue();
                if (gv != null) {
                    keyField = field;
                    break;
                }
            }

            if (keyField != null) {
                // and if there is, make sure we've a single-column id
                if (_pkColumns.size() > 1) {
                    throw new IllegalArgumentException(
                        "Cannot use @GeneratedValue on multiple-column @Id's");
                }

                // the primary key must be numeric if we are to auto-assign it
                Class<?> ftype = keyField.getField().getType();
                boolean isNumeric = (
                    ftype.equals(Byte.TYPE) || ftype.equals(Byte.class) ||
                    ftype.equals(Short.TYPE) || ftype.equals(Short.class) ||
                    ftype.equals(Integer.TYPE) || ftype.equals(Integer.class) ||
                    ftype.equals(Long.TYPE) || ftype.equals(Long.class));
                if (!isNumeric) {
                    throw new IllegalArgumentException(
                        "Cannot use @GeneratedValue on non-numeric column");
                }

                switch(gv.strategy()) {
                case AUTO:
                case IDENTITY:
                    _keyGenerator = new IdentityKeyGenerator();
                    break;

                case TABLE:
                    String name = gv.generator();
                    generator = context.tableGenerators.get(name);
                    if (generator == null) {
                        throw new IllegalArgumentException(
                            "Unknown generator [generator=" + name + "]");
                    }
                    _keyGenerator = new TableKeyGenerator(generator);
                    break;
                }
            }
        }

        // generate our full list of fields/columns for use in queries
        _allFields = fields.toArray(new String[fields.size()]);

        // figure out the list of fields that correspond to actual table columns and create the SQL
        // used to create and migrate our table
        _columnFields = new String[_allFields.length];
        _columnDefinitions = new String[_allFields.length];
        int jj = 0;
        for (int ii = 0; ii < _allFields.length; ii++) {
            // include all persistent non-computed fields
            String colDef = _fields.get(_allFields[ii]).getColumnDefinition();
            if (colDef != null) {
                _columnFields[jj] = _allFields[ii];
                _columnDefinitions[jj] = colDef;
                jj ++;
            }
        }
        _columnFields = ArrayUtil.splice(_columnFields, jj);
        _columnDefinitions = ArrayUtil.splice(_columnDefinitions, jj);

        // add the primary key, if we have one
        if (hasPrimaryKey()) {
            String[] indices = new String[_pkColumns.size()];
            for (int ii = 0; ii < indices.length; ii ++) {
                indices[ii] = _pkColumns.get(ii).getColumnName();
            }
            _columnDefinitions = ArrayUtil.append(
                _columnDefinitions, "PRIMARY KEY (" + StringUtil.join(indices, ", ") + ")");
        }
        _postamble = ""; // TODO: add annotations for the postamble

        // if we did not find a schema version field, complain
        if (_schemaVersion < 0) {
            log.warning("Unable to read " + _pclass.getName() + "." + SCHEMA_VERSION_FIELD +
                        ". Schema migration disabled.");
        }
    }

    /**
     * Returns the name of the table in which persistence instances of this class are stored. By
     * default this is the classname of the persistent object without the package.
     */
    public String getTableName ()
    {
        return _tableName;
    }

    /**
     * Returns true if our persistent object defines a primary key.
     */
    public boolean hasPrimaryKey ()
    {
        return (_pkColumns != null);
    }

    /**
     * Returns a key configured with the primary key of the supplied object.  Throws an exception
     * if the persistent object did not declare a primary key.
     */
    public Key getPrimaryKey (Object object)
    {
        if (!hasPrimaryKey()) {
            throw new UnsupportedOperationException(
                getClass().getName() + " does not define a primary key");
        }
        try {
            Comparable[] values = new Comparable[_pkColumns.size()];
            for (int ii = 0; ii < _pkColumns.size(); ii++) {
                FieldMarshaller field = _pkColumns.get(ii);
                values[ii] = (Comparable) field.getField().get(object);
            }
            return makePrimaryKey(values);
        } catch (IllegalAccessException iae) {
            throw new RuntimeException(iae);
        }
    }

    /**
     * Creates a primary key record for the type of object handled by this marshaller, using the
     * supplied primary key value.
     */
    public Key makePrimaryKey (Comparable... values)
    {
        if (!hasPrimaryKey()) {
            throw new UnsupportedOperationException(
                getClass().getName() + " does not define a primary key");
        }
        if (values.length != _pkColumns.size()) {
            throw new IllegalArgumentException(
                "Argument count (" + values.length + ") must match primary key size (" +
                _pkColumns.size() + ")");
        }
        ColumnExpression[] columns = new ColumnExpression[_pkColumns.size()];
        for (int ii = 0; ii < _pkColumns.size(); ii++) {
            FieldMarshaller field = _pkColumns.get(ii);
            columns[ii] = new ColumnExpression(_pclass, field.getColumnName());
        }
        return new Key(columns, values);
    }

    /**
     * Initializes the table used by this marshaller. If the table does not exist, it will be
     * created. If the schema version specified by the persistent object is newer than the database
     * schema, it will be migrated.
     */
    public void init (Connection conn)
        throws SQLException
    {
        // check to see if our schema version table exists, create it if not
        JDBCUtil.createTableIfMissing(conn, SCHEMA_VERSION_TABLE,
                                      new String[] { "persistentClass VARCHAR(255) NOT NULL",
                                                     "version INTEGER NOT NULL" }, "");

        // now create the table for our persistent class if it does not exist
        if (!JDBCUtil.tableExists(conn, getTableName())) {
            log.fine("Creating table " + getTableName() + " (" +
                     StringUtil.join(_columnDefinitions, ", ") + ") " + _postamble);
            JDBCUtil.createTableIfMissing(conn, getTableName(), _columnDefinitions, _postamble);
            updateVersion(conn, 1);
        }

        // if we have a key generator, initialize that too
        if (_keyGenerator != null) {
            _keyGenerator.init(conn);
        }

        // if schema versioning is disabled, stop now
        if (_schemaVersion < 0) {
            return;
        }

        // make sure the versions match
        int currentVersion = readVersion(conn);
        if (currentVersion == _schemaVersion) {
            return;
        }

        log.info("Migrating " + getTableName() + " from " +
                 currentVersion + " to " + _schemaVersion + "...");

        // otherwise try to migrate the schema; doing column additions magically and running any
        // registered hand-migrations
        DatabaseMetaData meta = conn.getMetaData();
        ResultSet rs = meta.getColumns(null, null, getTableName(), "%");
        HashSet<String> columns = new HashSet<String>();
        while (rs.next()) {
            columns.add(rs.getString("COLUMN_NAME"));
        }

        for (String fname : _columnFields) {
            FieldMarshaller fmarsh = _fields.get(fname);
            if (columns.contains(fmarsh.getColumnName())) {
                continue;
            }

            // otherwise add the column
            String coldef = fmarsh.getColumnDefinition();
            String query = "alter table " + getTableName() + " add column " + coldef;

            // try to add it to the appropriate spot
            int fidx = ListUtil.indexOf(_allFields, fmarsh.getColumnName());
            if (fidx == 0) {
                query += " first";
            } else {
                query += " after " + _allFields[fidx-1];
            }

            log.info("Adding column to " + getTableName() + ": " + coldef);
            Statement stmt = conn.createStatement();
            try {
                stmt.executeUpdate(query);
            } finally {
                stmt.close();
            }
        }

        // TODO: run any registered hand migrations

        updateVersion(conn, _schemaVersion);
    }

    /**
     * Creates a persistent object from the supplied result set. The result set must have come from
     * a query provided by {@link #createQuery}.
     */
    public T createObject (ResultSet rs)
        throws SQLException
    {
        try {
            T po = (T)_pclass.newInstance();
            for (FieldMarshaller fm : _fields.values()) {
                fm.getValue(rs, po);
            }
            return po;

        } catch (SQLException sqe) {
            // pass this on through
            throw sqe;

        } catch (Exception e) {
            String errmsg = "Failed to unmarshall persistent object [pclass=" +
                _pclass.getName() + "]";
            throw (SQLException)new SQLException(errmsg).initCause(e);
        }
    }

    /**
     * Creates a statement that will insert the supplied persistent object into the database.
     */
    public PreparedStatement createInsert (Connection conn, Object po)
        throws SQLException
    {
        try {
            StringBuilder insert = new StringBuilder();
            insert.append("insert into ").append(getTableName());
            insert.append(" (").append(StringUtil.join(_columnFields, ","));
            insert.append(")").append(" values(");
            for (int ii = 0; ii < _columnFields.length; ii++) {
                if (ii > 0) {
                    insert.append(", ");
                }
                insert.append("?");
            }
            insert.append(")");

            // TODO: handle primary key, nullable fields specially?
            PreparedStatement pstmt = conn.prepareStatement(insert.toString());
            int idx = 0;
            for (String field : _columnFields) {
                _fields.get(field).setValue(po, pstmt, ++idx);
            }
            return pstmt;

        } catch (SQLException sqe) {
            // pass this on through
            throw sqe;

        } catch (Exception e) {
            String errmsg = "Failed to marshall persistent object [pclass=" +
                _pclass.getName() + "]";
            throw (SQLException)new SQLException(errmsg).initCause(e);
        }
    }

    /**
     * Fills in the primary key just assigned to the supplied persistence object by the execution
     * of the results of {@link #createInsert}.
     *
     * @return the newly assigned primary key or null if the object does not use primary keys or
     * this is not the right time to assign the key.
     */
    public Key assignPrimaryKey (Connection conn, Object po, boolean postFactum)
        throws SQLException
    {
        // if we have no primary key or no generator, then we're done
        if (!hasPrimaryKey() || _keyGenerator == null) {
            return null;
        }

        // run this generator either before or after the actual insertion
        if (_keyGenerator.isPostFactum() != postFactum) {
            return null;
        }

        try {
            int nextValue = _keyGenerator.nextGeneratedValue(conn);
            _pkColumns.get(0).getField().set(po, nextValue);
            return makePrimaryKey(nextValue);
        } catch (Exception e) {
            String errmsg = "Failed to assign primary key [type=" + _pclass + "]";
            throw (SQLException) new SQLException(errmsg).initCause(e);
        }
    }

    /**
     * Creates a statement that will update the supplied persistent object using the supplied key.
     */
    public PreparedStatement createUpdate (Connection conn, Object po, Key key)
        throws SQLException
    {
        return createUpdate(conn, po, key, _columnFields);
    }

    /**
     * Creates a statement that will update the supplied persistent object
     * using the supplied key.
     */
    public PreparedStatement createUpdate (
        Connection conn, Object po, Key key, String[] modifiedFields)
        throws SQLException
    {
        StringBuilder update = new StringBuilder();
        update.append("update ").append(getTableName()).append(" set ");
        int idx = 0;
        for (String field : modifiedFields) {
            if (idx++ > 0) {
                update.append(", ");
            }
            update.append(field).append(" = ?");
        }
        update.append(" where "); key.appendClause(null, update);

        try {
            PreparedStatement pstmt = conn.prepareStatement(update.toString());
            idx = 0;
            // bind the update arguments
            for (String field : modifiedFields) {
                _fields.get(field).setValue(po, pstmt, ++idx);
            }
            // now bind the key arguments
            key.bindArguments(pstmt, ++idx);
            return pstmt;

        } catch (SQLException sqe) {
            // pass this on through
            throw sqe;

        } catch (Exception e) {
            String errmsg = "Failed to marshall persistent object " +
                "[pclass=" + _pclass.getName() + "]";
            throw (SQLException)new SQLException(errmsg).initCause(e);
        }
    }

    /**
     * Creates a statement that will update the specified set of fields for all persistent objects
     * that match the supplied key.
     */
    public PreparedStatement createPartialUpdate (
        Connection conn, Key key, String[] modifiedFields, Object[] modifiedValues)
        throws SQLException
    {
        StringBuilder update = new StringBuilder();
        update.append("update ").append(getTableName()).append(" set ");
        int idx = 0;
        for (String field : modifiedFields) {
            if (idx++ > 0) {
                update.append(", ");
            }
            update.append(field).append(" = ?");
        }
        update.append(" where "); key.appendClause(null, update);

        PreparedStatement pstmt = conn.prepareStatement(update.toString());
        idx = 0;
        // bind the update arguments
        for (Object value : modifiedValues) {
            // TODO: use the field marshaller?
            pstmt.setObject(++idx, value);
        }
        // now bind the key arguments
        key.bindArguments(pstmt, ++idx);
        return pstmt;
    }

    /**
     * Creates a statement that will delete all rows matching the supplied key.
     */
    public PreparedStatement createDelete (Connection conn, Key key)
        throws SQLException
    {
        StringBuilder query = new StringBuilder("delete from " + getTableName() + " where ");
        key.appendClause(null, query);
        PreparedStatement pstmt = conn.prepareStatement(query.toString());
        key.bindArguments(pstmt, 1);
        return pstmt;
    }

    /**
     * Creates a statement that will update the specified set of fields, using the supplied literal
     * SQL values, for all persistent objects that match the supplied key.
     */
    public PreparedStatement createLiteralUpdate (
        Connection conn, Key key, String[] modifiedFields, Object[] modifiedValues)
        throws SQLException
    {
        StringBuilder update = new StringBuilder();
        update.append("update ").append(getTableName()).append(" set ");
        for (int ii = 0; ii < modifiedFields.length; ii++) {
            if (ii > 0) {
                update.append(", ");
            }
            update.append(modifiedFields[ii]).append(" = ");
            update.append(modifiedValues[ii]);
        }
        update.append(" where "); key.appendClause(null, update);

        PreparedStatement pstmt = conn.prepareStatement(update.toString());
        key.bindArguments(pstmt, 1);
        return pstmt;
    }

    protected void updateVersion (Connection conn, int version)
        throws SQLException
    {
        String update = "update " + SCHEMA_VERSION_TABLE +
            " set version = " + version + " where persistentClass = '" + getTableName() + "'";
        String insert = "insert into " + SCHEMA_VERSION_TABLE +
            " values('" + getTableName() + "', " + version + ")";
        Statement stmt = conn.createStatement();
        try {
            if (stmt.executeUpdate(update) == 0) {
                stmt.executeUpdate(insert);
            }
        } finally {
            stmt.close();
        }
    }

    protected int readVersion (Connection conn)
        throws SQLException
    {
        String query = "select version from " + SCHEMA_VERSION_TABLE +
            " where persistentClass = '" + getTableName() + "'";
        Statement stmt = conn.createStatement();
        try {
            ResultSet rs = stmt.executeQuery(query);
            return (rs.next()) ?  rs.getInt(1) : 1;
        } finally {
            stmt.close();
        }
    }

    /** The persistent object class that we manage. */
    protected Class<T> _pclass;

    /** The name of our persistent object table. */
    protected String _tableName;

    /** A field marshaller for each persistent field in our object. */
    protected HashMap<String, FieldMarshaller> _fields = new HashMap<String, FieldMarshaller>();

    /** The field marshallers for our persistent object's primary key columns
     * or null if it did not define a primary key. */
    protected ArrayList<FieldMarshaller> _pkColumns;

    /** The generator to use for auto-generating primary key values, or null. */
    protected KeyGenerator _keyGenerator;

    /** The persisent fields of our object, in definition order. */
    protected String[] _allFields;

    /** The fields of our object with directly corresponding table columns. */
    protected String[] _columnFields;

    /** The version of our persistent object schema as specified in the class
     * definition. */
    protected int _schemaVersion = -1;

    /** Used when creating and migrating our table schema. */
    protected String[] _columnDefinitions;

    /** Used when creating and migrating our table schema. */
    protected String _postamble;

    /** The name of the table we use to track schema versions. */
    protected static final String SCHEMA_VERSION_TABLE = "DepotSchemaVersion";
}
