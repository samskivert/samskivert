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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import com.samskivert.io.PersistenceException;
import com.samskivert.jdbc.depot.annotation.Computed;
import com.samskivert.jdbc.depot.annotation.Entity;
import com.samskivert.jdbc.depot.annotation.GeneratedValue;
import com.samskivert.jdbc.depot.annotation.Id;
import com.samskivert.jdbc.depot.annotation.Index;
import com.samskivert.jdbc.depot.annotation.TableGenerator;
import com.samskivert.jdbc.depot.annotation.Transient;

import com.samskivert.jdbc.JDBCUtil;
import com.samskivert.jdbc.depot.clause.Where;
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

        // see if this is a computed entity
        Entity entity = pclass.getAnnotation(Entity.class);
        Computed computed = pclass.getAnnotation(Computed.class);
        if (computed == null) {
            // if not, this class has a corresponding SQL table
            _tableName = _pclass.getName();
            _tableName = _tableName.substring(_tableName.lastIndexOf(".")+1);

            // see if there are Entity values specified
            if (entity != null) {
                if (entity.name().length() > 0) {
                    _tableName = entity.name();
                }
                _postamble = entity.postamble();
            }
        }

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
            if (java.lang.reflect.Modifier.isStatic(mods) &&
                field.getName().equals(SCHEMA_VERSION_FIELD)) {
                try {
                    _schemaVersion = (Integer)field.get(null);
                } catch (Exception e) {
                    log.log(Level.WARNING, "Failed to read schema version " +
                        "[class=" + _pclass + "].", e);
                }
            }

            // the field must be public, non-static and non-transient
            if (!java.lang.reflect.Modifier.isPublic(mods) ||
                java.lang.reflect.Modifier.isStatic(mods) ||
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

        // if we're a computed entity, stop here
        if (_tableName == null) {
            return;
        }

        // figure out the list of fields that correspond to actual table columns and generate the
        // SQL used to create and migrate our table (unless we're a computed entity)
        _columnFields = new String[_allFields.length];
        int jj = 0;
        for (int ii = 0; ii < _allFields.length; ii++) {
            // include all persistent non-computed fields
            String colDef = _fields.get(_allFields[ii]).getColumnDefinition();
            if (colDef != null) {
                _columnFields[jj] = _allFields[ii];
                _declarations.add(colDef);
                jj ++;
            }
        }
        _columnFields = ArrayUtil.splice(_columnFields, jj);

        // determine whether we have any index definitions
        if (entity != null) {
            for (Index index : entity.indices()) {
                // TODO: delegate this to a database specific SQL generator
                _declarations.add(index.type() + " index " + index.name() +
                                  " (" + StringUtil.join(index.columns(), ", ") + ")");
            }
        }

        // add the primary key, if we have one
        if (hasPrimaryKey()) {
            _declarations.add("PRIMARY KEY (" + getPrimaryKeyColumns() + ")");
        }

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
    public Key<T> getPrimaryKey (Object object)
    {
        if (!hasPrimaryKey()) {
            throw new UnsupportedOperationException(
                _pclass.getName() + " does not define a primary key");
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
    public Key<T> makePrimaryKey (Comparable... values)
    {
        if (!hasPrimaryKey()) {
            throw new UnsupportedOperationException(
                getClass().getName() + " does not define a primary key");
        }
        String[] columns = new String[_pkColumns.size()];
        for (int ii = 0; ii < _pkColumns.size(); ii++) {
            columns[ii] = _pkColumns.get(ii).getColumnName();
        }
        return new Key<T>(_pclass, columns, values);
    }

    /**
     * Returns true if this marshaller has been initialized ({@link #init} has been called), its
     * migrations run and it is ready for operation. False otherwise.
     */
    public boolean isInitialized ()
    {
        return _initialized;
    }

    /**
     * Creates a persistent object from the supplied result set. The result set must have come from
     * a query provided by {@link #createQuery}.
     */
    public T createObject (ResultSet rs)
        throws SQLException
    {
        try {
            // first, build a set of the fields that we actually received
            Set<String> fields = new HashSet<String>();
            ResultSetMetaData metadata = rs.getMetaData();
            for (int ii = 1; ii <= metadata.getColumnCount(); ii ++) {
               fields.add(metadata.getColumnName(ii));
            }

            // then create and populate the persistent object
            T po = _pclass.newInstance();
            for (FieldMarshaller fm : _fields.values()) {
                if (!fields.contains(fm.getField().getName())) {
                    // this field was not in the result set, make sure that's OK
                    if (fm.getComputed() != null && !fm.getComputed().required()) {
                        continue;
                    }
                    throw new SQLException("ResultSet missing field: " + fm.getField().getName());
                }
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
        requireNotComputed("insert rows into");

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
    public PreparedStatement createUpdate (Connection conn, Object po, Where key)
        throws SQLException
    {
        return createUpdate(conn, po, key, _columnFields);
    }

    /**
     * Creates a statement that will update the supplied persistent object
     * using the supplied key.
     */
    public PreparedStatement createUpdate (
        Connection conn, Object po, Where key, String[] modifiedFields)
        throws SQLException
    {
        requireNotComputed("update rows in");

        StringBuilder update = new StringBuilder();
        update.append("update ").append(getTableName()).append(" set ");
        int idx = 0;
        for (String field : modifiedFields) {
            if (idx++ > 0) {
                update.append(", ");
            }
            update.append(field).append(" = ?");
        }
        key.appendClause(null, update);

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
        Connection conn, Where key, String[] modifiedFields, Object[] modifiedValues)
        throws SQLException
    {
        requireNotComputed("update rows in");

        StringBuilder update = new StringBuilder();
        update.append("update ").append(getTableName()).append(" set ");
        int idx = 0;
        for (String field : modifiedFields) {
            if (idx++ > 0) {
                update.append(", ");
            }
            update.append(field).append(" = ?");
        }
        key.appendClause(null, update);

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
    public PreparedStatement createDelete (Connection conn, Where key)
        throws SQLException
    {
        requireNotComputed("delete rows from");

        StringBuilder query = new StringBuilder("delete from " + getTableName());
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
        Connection conn, Where key, String[] modifiedFields, Object[] modifiedValues)
        throws SQLException
    {
        requireNotComputed("update rows in");

        StringBuilder update = new StringBuilder();
        update.append("update ").append(getTableName()).append(" set ");
        for (int ii = 0; ii < modifiedFields.length; ii++) {
            if (ii > 0) {
                update.append(", ");
            }
            update.append(modifiedFields[ii]).append(" = ");
            update.append(modifiedValues[ii]);
        }
        key.appendClause(null, update);

        PreparedStatement pstmt = conn.prepareStatement(update.toString());
        key.bindArguments(pstmt, 1);
        return pstmt;
    }

    /**
     * This is called by the persistence context to register a migration for the entity managed by
     * this marshaller.
     */
    protected void registerMigration (EntityMigration migration)
    {
        _migrations.add(migration);
    }

    /**
     * Initializes the table used by this marshaller. This is called automatically by the {@link
     * PersistenceContext} the first time an entity is used. If the table does not exist, it will
     * be created. If the schema version specified by the persistent object is newer than the
     * database schema, it will be migrated.
     */
    protected void init (PersistenceContext ctx)
        throws PersistenceException
    {
        if (_initialized) { // sanity check
            throw new IllegalStateException(
                "Cannot re-initialize marshaller [type=" + _pclass + "].");
        }
        _initialized = true;

        // if we have no table (i.e. we're a computed entity), we have nothing to create
        if (getTableName() == null) {
            return;
        }

        // check to see if our schema version table exists, create it if not
        ctx.invoke(new Modifier() {
            public int invoke (Connection conn) throws SQLException {
                JDBCUtil.createTableIfMissing(
                    conn, SCHEMA_VERSION_TABLE,
                    new String[] { "persistentClass VARCHAR(255) NOT NULL",
                                   "version INTEGER NOT NULL" }, "");
                return 0;
            }
        });

        // now create the table for our persistent class if it does not exist
        ctx.invoke(new Modifier() {
            public int invoke (Connection conn) throws SQLException {
                if (!JDBCUtil.tableExists(conn, getTableName())) {
                    log.info("Creating table " + getTableName() + " (" + _declarations + ") " +
                             _postamble);
                    String[] definition = _declarations.toArray(new String[_declarations.size()]);
                    JDBCUtil.createTableIfMissing(conn, getTableName(), definition, _postamble);
                    updateVersion(conn, 1);
                }
                return 0;
            }
        });

        // if we have a key generator, initialize that too
        if (_keyGenerator != null) {
            ctx.invoke(new Modifier() {
                public int invoke (Connection conn) throws SQLException {
                    _keyGenerator.init(conn);
                    return 0;
                }
            });
        }

        // if schema versioning is disabled, stop now
        if (_schemaVersion < 0) {
            return;
        }

        // make sure the versions match
        int currentVersion = ctx.invoke(new Modifier() {
            public int invoke (Connection conn) throws SQLException {
                String query = "select version from " + SCHEMA_VERSION_TABLE +
                    " where persistentClass = '" + getTableName() + "'";
                Statement stmt = conn.createStatement();
                try {
                    ResultSet rs = stmt.executeQuery(query);
                    return (rs.next()) ? rs.getInt(1) : 1;
                } finally {
                    stmt.close();
                }
            }
        });
        if (currentVersion == _schemaVersion) {
            return;
        }

        // otherwise try to migrate the schema
        log.info("Migrating " + getTableName() + " from " +
                 currentVersion + " to " + _schemaVersion + "...");

        // run our pre-default-migrations
        for (EntityMigration migration : _migrations) {
            if (migration.runBeforeDefault() &&
                migration.shouldRunMigration(currentVersion, _schemaVersion)) {
                ctx.invoke(migration);
            }
        }

        // enumerate all of the columns now that we've run our pre-migrations
        final HashSet<String> columns = new HashSet<String>();
        final HashSet<String> indices = new HashSet<String>();
        ctx.invoke(new Modifier() {
            public int invoke (Connection conn) throws SQLException {
                DatabaseMetaData meta = conn.getMetaData();
                ResultSet rs = meta.getColumns(null, null, getTableName(), "%");
                while (rs.next()) {
                    columns.add(rs.getString("COLUMN_NAME"));
                }
                rs = meta.getIndexInfo(null, null, getTableName(), false, false);
                while (rs.next()) {
                    indices.add(rs.getString("INDEX_NAME"));
                }
                return 0;
            }
        });

        // add any missing columns
        for (String fname : _columnFields) {
            FieldMarshaller fmarsh = _fields.get(fname);
            if (columns.remove(fmarsh.getColumnName())) {
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
            ctx.invoke(new Modifier.Simple(query));

            // if the column is a TIMESTAMP column, we need to run a special query to update all
            // existing rows to the current time because MySQL annoyingly assigns them a default
            // value of "0000-00-00 00:00:00" regardless of whether we explicitly provide a
            // "DEFAULT" value for the column or not
            if (coldef.toLowerCase().indexOf(" timestamp") != -1) {
                query = "update " + getTableName() + " set " + fmarsh.getColumnName() + " = NOW()";
                log.info("Assigning current time to TIMESTAMP column: " + query);
                ctx.invoke(new Modifier.Simple(query));
            }
        }

        // add or remove the primary key as needed
        if (hasPrimaryKey() && !indices.remove("PRIMARY")) {
            String pkdef = "primary key (" + getPrimaryKeyColumns() + ")";
            log.info("Adding primary key to " + getTableName() + ": " + pkdef);
            ctx.invoke(new Modifier.Simple("alter table " + getTableName() + " add " + pkdef));

        } else if (!hasPrimaryKey() && indices.remove("PRIMARY")) {
            log.info("Dropping primary from " + getTableName());
            ctx.invoke(new Modifier.Simple("alter table " + getTableName() + " drop primary key"));
        }

        // add any missing indices
        Entity entity = _pclass.getAnnotation(Entity.class);
        for (Index index : (entity == null ? new Index[0] : entity.indices())) {
            if (indices.remove(index.name())) {
                continue;
            }
            String indexdef = "create " + index.type() + " index " + index.name() +
                " on " + getTableName() + " (" + StringUtil.join(index.columns(), ", ") + ")";
            log.info("Adding index: " + indexdef);
            ctx.invoke(new Modifier.Simple(indexdef));
        }

        // we do not auto-remove columns but rather require that EntityMigration.Drop records be
        // registered by hand to avoid accidentally causin the loss of data

        // we don't auto-remove indices either because we'd have to sort out the potentially
        // complex origins of an index (which might be because of a @Unique column or maybe the
        // index was hand defined in a @Column clause)

        // run our post-default-migrations
        for (EntityMigration migration : _migrations) {
            if (!migration.runBeforeDefault() &&
                migration.shouldRunMigration(currentVersion, _schemaVersion)) {
                ctx.invoke(migration);
            }
        }

        // record our new version in the database
        ctx.invoke(new Modifier() {
            public int invoke (Connection conn) throws SQLException {
                updateVersion(conn, _schemaVersion);
                return 0;
            }
        });
    }

    protected String getPrimaryKeyColumns ()
    {
        String[] pkcols = new String[_pkColumns.size()];
        for (int ii = 0; ii < pkcols.length; ii ++) {
            pkcols[ii] = _pkColumns.get(ii).getColumnName();
        }
        return StringUtil.join(pkcols, ", ");
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

    protected void requireNotComputed (String action)
        throws SQLException
    {
        if (getTableName() == null) {
            throw new IllegalArgumentException(
                "Can't " + action + " computed entities [class=" + _pclass + "]");
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
    protected ArrayList<String> _declarations = new ArrayList<String>();

    /** Used when creating and migrating our table schema. */
    protected String _postamble = "";

    /** Indicates that we have been initialized (created or migrated our tables). */
    protected boolean _initialized;

    /** A list of hand registered entity migrations to run prior to doing the default migration. */
    protected ArrayList<EntityMigration> _migrations = new ArrayList<EntityMigration>();

    /** The name of the table we use to track schema versions. */
    protected static final String SCHEMA_VERSION_TABLE = "DepotSchemaVersion";
}
