//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2006-2007 Michael Bayne, Pär Winzell
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import com.samskivert.io.PersistenceException;
import com.samskivert.jdbc.depot.annotation.Computed;
import com.samskivert.jdbc.depot.annotation.Entity;
import com.samskivert.jdbc.depot.annotation.FullTextIndex;
import com.samskivert.jdbc.depot.annotation.GeneratedValue;
import com.samskivert.jdbc.depot.annotation.Id;
import com.samskivert.jdbc.depot.annotation.Index;
import com.samskivert.jdbc.depot.annotation.Table;
import com.samskivert.jdbc.depot.annotation.TableGenerator;
import com.samskivert.jdbc.depot.annotation.Transient;
import com.samskivert.jdbc.depot.annotation.UniqueConstraint;

import com.samskivert.jdbc.DatabaseLiaison;
import com.samskivert.util.ArrayUtil;

import static com.samskivert.jdbc.depot.Log.log;

/**
 * Handles the marshalling and unmarshalling of persistent instances to JDBC primitives ({@link
 * PreparedStatement} and {@link ResultSet}).
 */
public class DepotMarshaller<T extends PersistentRecord>
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

        Entity entity = pclass.getAnnotation(Entity.class);

        // see if this is a computed entity
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
            }
        }

        // if the entity defines a new TableGenerator, map that in our static table as those are
        // shared across all entities
        TableGenerator generator = pclass.getAnnotation(TableGenerator.class);
        if (generator != null) {
            context.tableGenerators.put(generator.name(), generator);
        }

        // if there are FTS indexes in the Table, map those out here for future use
        Table table = pclass.getAnnotation(Table.class);
        if (table != null) {
            for (FullTextIndex fts : table.fullTextIndexes()) {
                _fullTextIndexes.put(fts.name(), fts);
            }
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
            _fields.put(field.getName(), fm);
            fields.add(field.getName());

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
                    _keyGenerator = new IdentityKeyGenerator(
                        gv, getTableName(), keyField.getColumnName());
                    break;

                case TABLE:
                    String name = gv.generator();
                    generator = context.tableGenerators.get(name);
                    if (generator == null) {
                        throw new IllegalArgumentException(
                            "Unknown generator [generator=" + name + "]");
                    }
                    _keyGenerator = new TableKeyGenerator(
                        generator, gv, getTableName(), keyField.getColumnName());
                    break;
                }
            }
        }

        // generate our full list of fields/columns for use in queries
        _allFields = fields.toArray(new String[fields.size()]);
    }

    /**
     * Returns the persistent class this is object is a marshaller for.
     */
    public Class<T> getPersistentClass ()
    {
       return _pclass;
    }

    /**
     * Returns the name of the table in which persistent instances of our class are stored. By
     * default this is the classname of the persistent object without the package.
     */
    public String getTableName ()
    {
        return _tableName;
    }

    /**
     * Returns all the persistent fields of our class, in definition order.
     */
    public String[] getFieldNames ()
    {
        return _allFields;
    }

    /**
     * Returns all the persistent fields that correspond to concrete table columns.
     */
    public String[] getColumnFieldNames ()
    {
        return _columnFields;
    }

    /**
     * Return the {@link FullTextIndex} registered under the given name, or null if none.
     */
    public FullTextIndex getFullTextIndex (String name)
    {
        return _fullTextIndexes.get(name);
    }

    /**
     * Returns the {@link FieldMarshaller} for a named field on our persistent class.
     */
    public FieldMarshaller getFieldMarshaller (String fieldName)
    {
        return _fields.get(fieldName);
    }

    /**
     * Returns true if our persistent object defines a primary key.
     */
    public boolean hasPrimaryKey ()
    {
        return (_pkColumns != null);
    }

    /**
     * Returns the {@link KeyGenerator} used to generate primary keys for this persistent object,
     * or null if it does not use a key generator.
     */
    public KeyGenerator getKeyGenerator ()
    {
        return _keyGenerator;
    }

    /**
     * Return the names of the columns that constitute the primary key of our associated persistent
     * record.
     */
    public String[] getPrimaryKeyFields ()
    {
        String[] pkcols = new String[_pkColumns.size()];
        for (int ii = 0; ii < pkcols.length; ii ++) {
            pkcols[ii] = _pkColumns.get(ii).getField().getName();
        }
        return pkcols;
    }

    /**
     * Returns a key configured with the primary key of the supplied object. If all the fields are
     * null, this method returns null. An exception is thrown if some of the fields are null and
     * some are not, or if the object does not declare a primary key.
     */
    public Key<T> getPrimaryKey (Object object)
    {
        return getPrimaryKey(object, true);
    }

    /**
     * Returns a key configured with the primary key of the supplied object. If all the fields are
     * null, this method returns null. If some of the fields are null and some are not, an
     * exception is thrown. If the object does not declare a primary key and the second argument is
     * true, this method throws an exception; if it's false, the method returns null.
     */
    public Key<T> getPrimaryKey (Object object, boolean requireKey)
    {
        if (!hasPrimaryKey()) {
            if (requireKey) {
                throw new UnsupportedOperationException(
                    _pclass.getName() + " does not define a primary key");
            }
            return null;
        }

        try {
            Comparable[] values = new Comparable[_pkColumns.size()];
            boolean hasNulls = false;
            for (int ii = 0; ii < _pkColumns.size(); ii++) {
                FieldMarshaller field = _pkColumns.get(ii);
                values[ii] = (Comparable) field.getField().get(object);
                if (values[ii] == null || Integer.valueOf(0).equals(values[ii])) {
                    // if this is the first null we see but not the first field, freak out
                    if (!hasNulls && ii > 0) {
                        throw new IllegalArgumentException(
                            "Persistent object's primary key fields are mixed null and non-null.");
                    }
                    hasNulls = true;
                } else if (hasNulls) {
                    // if this is a non-null field and we've previously seen nulls, also freak
                    throw new IllegalArgumentException(
                        "Persistent object's primary key fields are mixed null and non-null.");
                }
            }

            // if all the fields were null, return null, else build a key
            return hasNulls ? null : makePrimaryKey(values);

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
        String[] fields = new String[_pkColumns.size()];
        for (int ii = 0; ii < _pkColumns.size(); ii++) {
            fields[ii] = _pkColumns.get(ii).getField().getName();
        }
        return new Key<T>(_pclass, fields, values);
    }

    /**
     * Creates a primary key record for the type of object handled by this marshaller, using the
     * supplied result set.
     */
    public Key<T> makePrimaryKey (ResultSet rs)
        throws SQLException
    {
        if (!hasPrimaryKey()) {
            throw new UnsupportedOperationException(
                getClass().getName() + " does not define a primary key");
        }
        Comparable[] values = new Comparable[_pkColumns.size()];
        for (int ii = 0; ii < _pkColumns.size(); ii++) {
            Object keyValue = _pkColumns.get(ii).getFromSet(rs);
            if (!(keyValue instanceof Comparable)) {
                throw new IllegalArgumentException("Key field must be Comparable [field=" +
                                                   _pkColumns.get(ii).getColumnName() + "]");
            }
            values[ii] = (Comparable) keyValue;
        }
        return makePrimaryKey(values);
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
                if (!fields.contains(fm.getColumnName())) {
                    // this field was not in the result set, make sure that's OK
                    if (fm.getComputed() != null && !fm.getComputed().required()) {
                        continue;
                    }
                    throw new SQLException("ResultSet missing field: " + fm.getField().getName());
                }
                fm.writeToObject(rs, po);
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
     * Fills in the primary key just assigned to the supplied persistence object by the execution
     * of the results of {@link #createInsert}.
     *
     * @return the newly assigned primary key or null if the object does not use primary keys or
     * this is not the right time to assign the key.
     */
    public Key assignPrimaryKey (
        Connection conn, DatabaseLiaison liaison, Object po, boolean postFactum)
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
            int nextValue = _keyGenerator.nextGeneratedValue(conn, liaison);
            _pkColumns.get(0).getField().set(po, nextValue);
            return makePrimaryKey(nextValue);
        } catch (Exception e) {
            String errmsg = "Failed to assign primary key [type=" + _pclass + "]";
            throw (SQLException) new SQLException(errmsg).initCause(e);
        }
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
    protected void init (final PersistenceContext ctx)
        throws PersistenceException
    {
        if (_initialized) { // sanity check
            throw new IllegalStateException(
                "Cannot re-initialize marshaller [type=" + _pclass + "].");
        }
        _initialized = true;

        final SQLBuilder builder = ctx.getSQLBuilder(new DepotTypes(ctx, _pclass));

        // perform the context-sensitive initialization of the field marshallers
        for (FieldMarshaller fm : _fields.values()) {
            fm.init(builder);
        }

        // figure out the list of fields that correspond to actual table columns and generate the
        // SQL used to create and migrate our table (unless we're a computed entity)
        _columnFields = new String[_allFields.length];
        String[] declarations = new String[_allFields.length];
        int jj = 0;
        for (int ii = 0; ii < _allFields.length; ii++) {
            @SuppressWarnings("unchecked") FieldMarshaller<T> fm = _fields.get(_allFields[ii]);
            // include all persistent non-computed fields
            String colDef = fm.getColumnDefinition();
            if (colDef != null) {
                _columnFields[jj] = _allFields[ii];
                declarations[jj] = colDef;
                jj ++;
            }
        }
        _columnFields = ArrayUtil.splice(_columnFields, jj);
        declarations = ArrayUtil.splice(declarations, jj);

        // if we have no table (i.e. we're a computed entity), we have nothing to create
        if (getTableName() == null) {
            return;
        }

        // add any additional unique constraints
        String[][] uniqueConstraintColumns = null;
        Table table = _pclass.getAnnotation(Table.class);
        if (table != null) {
            UniqueConstraint[] uCons = table.uniqueConstraints();
            uniqueConstraintColumns = new String[uCons.length][];
            for (int kk = 0; kk < uCons.length; kk ++) {
                String[] columns = uCons[kk].fieldNames();
                for (int ii = 0; ii < columns.length; ii ++) {
                    FieldMarshaller fm = getFieldMarshaller(columns[ii]);
                    if (fm == null) {
                        throw new IllegalArgumentException(
                            "Unknown field in @UniqueConstraint: " + columns[ii]);
                    }
                    columns[ii] = fm.getColumnName();
                }
                uniqueConstraintColumns[kk] = columns;
            }
        }

        // if we did not find a schema version field, complain
        if (_schemaVersion < 0) {
            log.warning("Unable to read " + _pclass.getName() + "." + SCHEMA_VERSION_FIELD +
                        ". Schema migration disabled.");
        }

        // check to see if our schema version table exists, create it if not
        ctx.invoke(new Modifier() {
            public int invoke (Connection conn, DatabaseLiaison liaison) throws SQLException {
                liaison.createTableIfMissing(
                    conn, SCHEMA_VERSION_TABLE,
                    new String[] { "persistentClass", "version" },
                    new String[] { "VARCHAR(255) NOT NULL", "INTEGER NOT NULL" },
                    null,
                    new String[] { "persistentClass" });
                return 0;
            }
        });

        // fetch all relevant information regarding our table from the database
        final TableMetaData metaData = ctx.invoke(new Query.TrivialQuery<TableMetaData>() {
            public TableMetaData invoke (Connection conn, DatabaseLiaison dl) throws SQLException {
                return new TableMetaData(conn.getMetaData());
            }
        });

        // if the table does not exist, create it
        if (!metaData.tableExists) {
            final Entity entity = _pclass.getAnnotation(Entity.class);
            final String[] fDeclarations = declarations;
            final String[][] fUniqueConstraintColumns = uniqueConstraintColumns;

            ctx.invoke(new Modifier() {
                public int invoke (Connection conn, DatabaseLiaison liaison) throws SQLException {
                    // create the table
                    String[] columns = new String[_columnFields.length];
                    for (int ii = 0; ii < columns.length; ii ++) {
                        columns[ii] = _fields.get(_columnFields[ii]).getColumnName();
                    }
                    String[] primaryKeyColumns = null;
                    if (_pkColumns != null) {
                        primaryKeyColumns = new String[_pkColumns.size()];
                        for (int ii = 0; ii < primaryKeyColumns.length; ii ++) {
                            primaryKeyColumns[ii] = _pkColumns.get(ii).getColumnName();
                        }
                    }
                    liaison.createTableIfMissing(conn, getTableName(), columns, fDeclarations,
                        fUniqueConstraintColumns, primaryKeyColumns);

                    // add its indexen
                    for (Index idx : entity.indices()) {
                        liaison.addIndexToTable(
                            conn, getTableName(), idx.columns(), idx.name(), idx.unique());
                    }
                    if (_keyGenerator != null) {
                        _keyGenerator.init(conn, liaison);
                    }
                    for (FullTextIndex fti : _fullTextIndexes.values()) {
                        builder.addFullTextSearch(conn, DepotMarshaller.this, fti);
                    }

                    updateVersion(conn, liaison, _schemaVersion);
                    return 0;
                }
            });

            // and we're done
            return;
        }

        // if the table exists, see if should attempt automatic schema migration
        if (_schemaVersion < 0) {
            // nope, versioning disabled
            verifySchemasMatch(metaData, ctx);
            return;
        }

        // make sure the versions match
        int currentVersion = ctx.invoke(new Modifier() {
            public int invoke (Connection conn, DatabaseLiaison liaison) throws SQLException {
                String query =
                    " select " + liaison.columnSQL("version") +
                    "   from " + liaison.tableSQL(SCHEMA_VERSION_TABLE) +
                    "  where " + liaison.columnSQL("persistentClass") +
                    " = '" + getTableName() + "'";
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
            verifySchemasMatch(metaData, ctx);
            return;
        }

        // otherwise try to migrate the schema
        log.info("Migrating " + getTableName() + " from " + currentVersion + " to " +
                 _schemaVersion + "...");

        // run our pre-default-migrations
        for (EntityMigration migration : _migrations) {
            if (migration.runBeforeDefault() &&
                migration.shouldRunMigration(currentVersion, _schemaVersion)) {
                migration.init(getTableName(), _fields);
                ctx.invoke(migration);
            }
        }

        // this is a little silly, but we need a copy for name disambiguation later
        Set<String> indicesCopy = new HashSet<String>(metaData.indexColumns.keySet());

        // add any missing columns
        for (String fname : _columnFields) {
            @SuppressWarnings("unchecked") final FieldMarshaller<T> fmarsh = _fields.get(fname);
            if (metaData.tableColumns.remove(fmarsh.getColumnName())) {
                continue;
            }

            // otherwise add the column
            final String coldef = fmarsh.getColumnDefinition();
            log.info("Adding column to " + getTableName() + ": " +
                     fmarsh.getColumnName() + " " + coldef);
            ctx.invoke(new Modifier.Simple() {
                protected String createQuery (DatabaseLiaison liaison) {
                    return "alter table " + liaison.tableSQL(getTableName()) +
                        " add column " + liaison.columnSQL(fmarsh.getColumnName()) + " " + coldef;
                }
            });

            // if the column is a TIMESTAMP or DATETIME column, we need to run a special query to
            // update all existing rows to the current time because MySQL annoyingly assigns
            // TIMESTAMP columns a value of "0000-00-00 00:00:00" regardless of whether we
            // explicitly provide a "DEFAULT" value for the column or not, and DATETIME columns
            // cannot accept CURRENT_TIME or NOW() defaults at all.
            if (coldef.toLowerCase().indexOf(" timestamp") != -1 ||
                coldef.toLowerCase().indexOf(" datetime") != -1) {
                log.info("Assigning current time to " + fmarsh.getColumnName() + ".");
                ctx.invoke(new Modifier.Simple() {
                    protected String createQuery (DatabaseLiaison liaison) {
                        // TODO: is NOW() standard SQL?
                        return "update " + liaison.tableSQL(getTableName()) +
                            " set " + liaison.columnSQL(fmarsh.getColumnName()) + " = NOW()";
                    }
                });
            }
        }

        // add or remove the primary key as needed
        if (hasPrimaryKey() && metaData.pkName == null) {
            log.info("Adding primary key.");
            ctx.invoke(new Modifier() {
                public int invoke (Connection conn, DatabaseLiaison liaison) throws SQLException {
                    liaison.addPrimaryKey(conn, getTableName(), getPrimaryKeyFields());
                    return 0;
                }
            });

        } else if (!hasPrimaryKey() && metaData.pkName != null) {
            log.info("Dropping primary key.");
            ctx.invoke(new Modifier() {
                public int invoke (Connection conn, DatabaseLiaison liaison) throws SQLException {
                    liaison.dropPrimaryKey(conn, getTableName(), metaData.pkName);
                    return 0;
                }
            });
        }

        Entity entity = _pclass.getAnnotation(Entity.class);
        // add any missing indices
        for (final Index index : (entity == null ? new Index[0] : entity.indices())) {
            if (metaData.indexColumns.containsKey(index.name())) {
                metaData.indexColumns.remove(index.name());
                continue;
            }
            ctx.invoke(new Modifier() {
                public int invoke (Connection conn, DatabaseLiaison liaison) throws SQLException {
                    liaison.addIndexToTable(
                        conn, getTableName(), index.columns(), index.name(), index.unique());
                    return 0;
                }
            });
        }

        // to get the @Table(uniqueIndices...) indices, we use our clever set of column name sets
        Set<Set<String>> uniqueIndices = new HashSet<Set<String>>(metaData.indexColumns.values());

        if (getTableName() != null && table != null) {
            for (UniqueConstraint constraint : table.uniqueConstraints()) {
                // for each given UniqueConstraint, build a new column set
                Set<String> colSet = new HashSet<String>(Arrays.asList(constraint.fieldNames()));

                // and check if the table contained this set
                if (uniqueIndices.contains(colSet)) {
                    continue; // good, carry on
                }

                // else build the index; we'll use mysql's convention of naming it after a column,
                // with possible _N disambiguation; luckily we made a copy of the index names!
                String indexName = colSet.iterator().next();
                if (indicesCopy.contains(indexName)) {
                    int num = 1;
                    indexName += "_";
                    while (indicesCopy.contains(indexName + num)) {
                        num ++;
                    }
                    indexName += num;
                }

                final String[] colArr = colSet.toArray(new String[colSet.size()]);
                final String fName = indexName;
                ctx.invoke(new Modifier() {
                    public int invoke (Connection conn, DatabaseLiaison dl) throws SQLException {
                        dl.addIndexToTable(conn, getTableName(), colArr, fName, true);
                        return 0;
                    }
                });
            }
        }

        // we do not auto-remove columns but rather require that EntityMigration.Drop records be
        // registered by hand to avoid accidentally causing the loss of data

        // we don't auto-remove indices either because we'd have to sort out the potentially
        // complex origins of an index (which might be because of a @Unique column or maybe the
        // index was hand defined in a @Column clause)

        // run our post-default-migrations
        for (EntityMigration migration : _migrations) {
            if (!migration.runBeforeDefault() &&
                migration.shouldRunMigration(currentVersion, _schemaVersion)) {
                migration.init(getTableName(), _fields);
                ctx.invoke(migration);
            }
        }

        // record our new version in the database
        ctx.invoke(new Modifier() {
            public int invoke (Connection conn, DatabaseLiaison liaison) throws SQLException {
                updateVersion(conn, liaison, _schemaVersion);
                return 0;
            }
        });
    }

    protected class TableMetaData
    {
        public boolean tableExists;
        public Set<String> tableColumns = new HashSet<String>();
        public Map<String, Set<String>> indexColumns = new HashMap<String, Set<String>>();
        public String pkName;
        public Set<String> pkColumns = new HashSet<String>();

        public TableMetaData (DatabaseMetaData meta)
            throws SQLException
        {
            tableExists = meta.getTables("", "", getTableName(), null).next();
            if (!tableExists) {
                return;
            }

            ResultSet rs = meta.getColumns(null, null, getTableName(), "%");
            while (rs.next()) {
                tableColumns.add(rs.getString("COLUMN_NAME"));
            }

            rs = meta.getIndexInfo(null, null, getTableName(), false, false);
            while (rs.next()) {
                String indexName = rs.getString("INDEX_NAME");
                Set<String> set = indexColumns.get(indexName);
                if (rs.getBoolean("NON_UNIQUE")) {
                    // not a unique index: just make sure there's an entry in the keyset
                    if (set == null) {
                        indexColumns.put(indexName, null);
                    }

                } else {
                    // for unique indices we collect the column names
                    if (set == null) {
                        set = new HashSet<String>();
                        indexColumns.put(indexName, set);
                    }
                    set.add(rs.getString("COLUMN_NAME"));
                }
            }

            rs = meta.getPrimaryKeys(null, null, getTableName());
            while (rs.next()) {
                pkName = rs.getString("PK_NAME");
                pkColumns.add(rs.getString("COLUMN_NAME"));
            }
        }
    }

    /**
     * Checks that there are no database columns for which we no longer have Java fields.
     */
    protected void verifySchemasMatch (TableMetaData meta, PersistenceContext ctx)
        throws PersistenceException
    {
        for (String fname : _columnFields) {
            FieldMarshaller fmarsh = _fields.get(fname);
            meta.tableColumns.remove(fmarsh.getColumnName());
        }
        for (String column : meta.tableColumns) {
            log.warning(getTableName() + " contains stale column '" + column + "'.");
        }
    }

    protected void updateVersion (Connection conn, DatabaseLiaison liaison, int version)
        throws SQLException
    {
        String update =
            "update " + liaison.tableSQL(SCHEMA_VERSION_TABLE) +
            "   set " + liaison.columnSQL("version") + " = " + version +
            " where " + liaison.columnSQL("persistentClass") + " = '" + getTableName() + "'";
        Statement stmt = conn.createStatement();
        try {
            if (stmt.executeUpdate(update) == 0) {
                String insert =
                    "insert into " + liaison.tableSQL(SCHEMA_VERSION_TABLE) +
                    " values('" + getTableName() + "', " + version + ")";
                stmt.executeUpdate(insert);
            }
        } finally {
            stmt.close();
        }
    }

    /** The persistent object class that we manage. */
    protected Class<T> _pclass;

    /** The name of our persistent object table. */
    protected String _tableName;

    /** A field marshaller for each persistent field in our object. */
    protected Map<String, FieldMarshaller> _fields = new HashMap<String, FieldMarshaller>();

    /** The field marshallers for our persistent object's primary key columns or null if it did not
     * define a primary key. */
    protected ArrayList<FieldMarshaller> _pkColumns;

    /** The generator to use for auto-generating primary key values, or null. */
    protected KeyGenerator _keyGenerator;

    /** The persisent fields of our object, in definition order. */
    protected String[] _allFields;

    /** The fields of our object with directly corresponding table columns. */
    protected String[] _columnFields;

    /** A mapping of all of the full text index annotations for our persistent record. */
    protected Map<String, FullTextIndex> _fullTextIndexes = new HashMap<String, FullTextIndex>();

    /** The version of our persistent object schema as specified in the class definition. */
    protected int _schemaVersion = -1;

    /** Indicates that we have been initialized (created or migrated our tables). */
    protected boolean _initialized;

    /** A list of hand registered entity migrations to run prior to doing the default migration. */
    protected ArrayList<EntityMigration> _migrations = new ArrayList<EntityMigration>();

    /** The name of the table we use to track schema versions. */
    protected static final String SCHEMA_VERSION_TABLE = "DepotSchemaVersion";
}
