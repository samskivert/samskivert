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
import com.samskivert.jdbc.depot.annotation.TableGenerator;
import com.samskivert.jdbc.depot.annotation.Transient;
import com.samskivert.jdbc.depot.annotation.UniqueConstraint;

import com.samskivert.jdbc.ColumnDefinition;
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
    public DepotMarshaller (Class<T> pClass, PersistenceContext context)
    {
        _pClass = pClass;

        Entity entity = pClass.getAnnotation(Entity.class);

        // see if this is a computed entity
        _computed = pClass.getAnnotation(Computed.class);
        if (_computed == null) {
            // if not, this class has a corresponding SQL table
            _tableName = _pClass.getName();
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
        TableGenerator generator = pClass.getAnnotation(TableGenerator.class);
        if (generator != null) {
            context.tableGenerators.put(generator.name(), generator);
        }

        boolean seenIdentityGenerator = false;

        // introspect on the class and create marshallers for persistent fields
        ArrayList<String> fields = new ArrayList<String>();
        for (Field field : _pClass.getFields()) {
            int mods = field.getModifiers();

            // check for a static constant schema version
            if (java.lang.reflect.Modifier.isStatic(mods) &&
                field.getName().equals(SCHEMA_VERSION_FIELD)) {
                try {
                    _schemaVersion = (Integer)field.get(null);
                } catch (Exception e) {
                    log.log(Level.WARNING, "Failed to read schema version " +
                        "[class=" + _pClass + "].", e);
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
            }

            // check if this field defines a new TableGenerator
            generator = field.getAnnotation(TableGenerator.class);
            if (generator != null) {
                context.tableGenerators.put(generator.name(), generator);
            }

            // check if this field is auto-generated
            GeneratedValue gv = fm.getGeneratedValue();
            if (gv != null) {
                // we can only do this on numeric fields
                Class<?> ftype = field.getType();
                boolean isNumeric = (
                    ftype.equals(Byte.TYPE) || ftype.equals(Byte.class) ||
                    ftype.equals(Short.TYPE) || ftype.equals(Short.class) ||
                    ftype.equals(Integer.TYPE) || ftype.equals(Integer.class) ||
                    ftype.equals(Long.TYPE) || ftype.equals(Long.class));
                if (!isNumeric) {
                    throw new IllegalArgumentException(
                        "Cannot use @GeneratedValue on non-numeric column: " + field.getName());
                }
                switch(gv.strategy()) {
                case AUTO:
                case IDENTITY:
                    if (seenIdentityGenerator) {
                        throw new IllegalArgumentException(
                            "Persistent records can have at most one AUTO/IDENTITY generator.");
                    }
                    _valueGenerators.put(field.getName(), new IdentityValueGenerator(gv, this, fm));
                    seenIdentityGenerator = true;
                    break;

                case TABLE:
                    String name = gv.generator();
                    generator = context.tableGenerators.get(name);
                    if (generator == null) {
                        throw new IllegalArgumentException(
                            "Unknown generator [generator=" + name + "]");
                    }
                    _valueGenerators.put(
                        field.getName(), new TableValueGenerator(generator, gv, this, fm));
                    break;
                }
            }

        }

        // generate our full list of fields/columns for use in queries
        _allFields = fields.toArray(new String[fields.size()]);

        // now check for @Entity annotations on the entire superclass chain
        Class<?> iterClass = pClass;
        do {
            entity = iterClass.getAnnotation(Entity.class);
            if (entity != null) {
                for (UniqueConstraint constraint : entity.uniqueConstraints()) {
                    String[] conFields = constraint.fieldNames();
                    Set<String> colSet = new HashSet<String>();
                    for (int ii = 0; ii < conFields.length; ii ++) {
                        FieldMarshaller fm = _fields.get(conFields[ii]);
                        if (fm == null) {
                            throw new IllegalArgumentException(
                                "Unknown unique constraint field: " + conFields[ii]);
                        }
                        colSet.add(fm.getColumnName());
                    }
                    _uniqueConstraints.add(colSet);
                }

                for (Index index : entity.indices()) {
                    if (_indexes.containsKey(index.name())) {
                        continue;
                    }
                    _indexes.put(index.name(), index);
                }

                // if there are FTS indexes in the Table, map those out here for future use
                for (FullTextIndex fti : entity.fullTextIndexes()) {
                    if (_fullTextIndexes.containsKey(fti.name())) {
                        continue;
                    }
                    _fullTextIndexes.put(fti.name(), fti);
                }
            }

            iterClass = iterClass.getSuperclass();

        } while (PersistentRecord.class.isAssignableFrom(iterClass) &&
                 !PersistentRecord.class.equals(iterClass));
    }

    /**
     * Returns the persistent class this is object is a marshaller for.
     */
    public Class<T> getPersistentClass ()
    {
       return _pClass;
    }

    /**
     * Returns the @Computed annotation definition of this entity, or null if none.
     */

    public Computed getComputed ()
    {
        return _computed;
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
     * Return the {@link FullTextIndex} registered under the given name.
     *
     * @exception IllegalArgumentException thrown if the requested full text index does not exist
     * on this record.
     */
    public FullTextIndex getFullTextIndex (String name)
    {
        FullTextIndex fti = _fullTextIndexes.get(name);
        if (fti == null) {
            throw new IllegalStateException("Persistent class missing full text index " +
                                            "[class=" + _pClass + ", index=" + name + "]");
        }
        return fti;
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
     * Returns the {@link ValueGenerator} objects used to automatically generate field values for
     * us when a new record is inserted.
     */
    public Iterable<ValueGenerator> getValueGenerators ()
    {
        return _valueGenerators.values();
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
                    _pClass.getName() + " does not define a primary key");
            }
            return null;
        }

        try {
            Comparable[] values = new Comparable[_pkColumns.size()];
            int nulls = 0;
            for (int ii = 0; ii < _pkColumns.size(); ii++) {
                FieldMarshaller field = _pkColumns.get(ii);
                if ((values[ii] = (Comparable)field.getField().get(object)) == null) {
                    nulls++;
                }
            }

            // make sure the keys are all null or all non-null
            if (nulls == 0) {
                return makePrimaryKey(values);
            } else if (nulls == values.length) {
                return null;
            }

            // throw an informative error message
            StringBuilder keys = new StringBuilder();
            for (int ii = 0; ii < _pkColumns.size(); ii++) {
                keys.append(", ").append(_pkColumns.get(ii).getField().getName());
                keys.append("=").append(values[ii]);
            }
            throw new IllegalArgumentException("Primary key fields are mixed null and non-null " +
                                               "[class=" + _pClass.getName() + keys + "].");

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
        return new Key<T>(_pClass, fields, values);
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
            T po = _pClass.newInstance();
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
            String errmsg = "Failed to unmarshall persistent object [class=" +
                _pClass.getName() + "]";
            throw (SQLException)new SQLException(errmsg).initCause(e);
        }
    }

    /**
     * Go through the registered {@link ValueGenerator}s for our persistent object and run the
     * ones that match the current postFactum phase, filling in the fields on the supplied object
     * while we go. This method used to generate a key; that is now a separate step.
     *
     * The return value is only non-empty for the !postFactum phase, in which case it is a set
     * of field names that are associated with {@link IdentityValueGenerator}, because these need
     * special handling in the INSERT (specifically, 'DEFAULT' must be supplied as a value in
     * the eventual SQL).
     */
    public Set<String> generateFieldValues (
        Connection conn, DatabaseLiaison liaison, Object po, boolean postFactum)
    {
        Set<String> idFields = new HashSet<String>();

        for (ValueGenerator vg : _valueGenerators.values()) {
            if (!postFactum && vg instanceof IdentityValueGenerator) {
                idFields.add(vg.getFieldMarshaller().getField().getName());
            }
            if (vg.isPostFactum() != postFactum) {
                continue;
            }

            try {
                int nextValue = vg.nextGeneratedValue(conn, liaison);
                vg.getFieldMarshaller().getField().set(po, nextValue);

            } catch (Exception e) {
                throw new IllegalStateException(
                    "Failed to assign primary key [type=" + _pClass + "]", e);
            }
        }
        return idFields;
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
                "Cannot re-initialize marshaller [type=" + _pClass + "].");
        }
        _initialized = true;

        final SQLBuilder builder = ctx.getSQLBuilder(new DepotTypes(ctx, _pClass));

        // perform the context-sensitive initialization of the field marshallers
        for (FieldMarshaller fm : _fields.values()) {
            fm.init(builder);
        }

        // if we have no table (i.e. we're a computed entity), we have nothing to create
        if (getTableName() == null) {
            return;
        }

        // figure out the list of fields that correspond to actual table columns and generate the
        // SQL used to create and migrate our table (unless we're a computed entity)
        _columnFields = new String[_allFields.length];
        ColumnDefinition[] declarations = new ColumnDefinition[_allFields.length];
        int jj = 0;
        for (int ii = 0; ii < _allFields.length; ii++) {
            FieldMarshaller fm = _fields.get(_allFields[ii]);
            // include all persistent non-computed fields
            ColumnDefinition colDef = fm.getColumnDefinition();
            if (colDef != null) {
                _columnFields[jj] = _allFields[ii];
                declarations[jj] = colDef;
                jj ++;
            }
        }
        _columnFields = ArrayUtil.splice(_columnFields, jj);
        declarations = ArrayUtil.splice(declarations, jj);

        // if we did not find a schema version field, complain
        if (_schemaVersion < 0) {
            log.warning("Unable to read " + _pClass.getName() + "." + SCHEMA_VERSION_FIELD +
                        ". Schema migration disabled.");
        }

        // check to see if our schema version table exists, create it if not
        ctx.invoke(new Modifier() {
            public int invoke (Connection conn, DatabaseLiaison liaison) throws SQLException {
                liaison.createTableIfMissing(
                    conn, SCHEMA_VERSION_TABLE,
                    new String[] { "persistentClass", "version" },
                    new ColumnDefinition[] {
                        new ColumnDefinition("VARCHAR(255)", false, true, null),
                        new ColumnDefinition("INTEGER", false, false, null)
                    },
                    null,
                    new String[] { "persistentClass" });
                return 0;
            }
        });

        // fetch all relevant information regarding our table from the database
        TableMetaData metaData = TableMetaData.load(ctx, getTableName());

        // if the table does not exist, create it
        if (!metaData.tableExists) {
            final ColumnDefinition[] fDeclarations = declarations;
            final String[][] uniqueConCols = new String[_uniqueConstraints.size()][];
            int kk = 0;
            for (Set<String> colSet : _uniqueConstraints) {
                uniqueConCols[kk++] = colSet.toArray(new String[colSet.size()]);
            }
            final Iterable<Index> indexen = _indexes.values();
            ctx.invoke(new Modifier() {
                public int invoke (Connection conn, DatabaseLiaison liaison) throws SQLException {
                    // create the table
                    String[] primaryKeyColumns = null;
                    if (_pkColumns != null) {
                        primaryKeyColumns = new String[_pkColumns.size()];
                        for (int ii = 0; ii < primaryKeyColumns.length; ii ++) {
                            primaryKeyColumns[ii] = _pkColumns.get(ii).getColumnName();
                        }
                    }
                    liaison.createTableIfMissing(
                        conn, getTableName(), fieldsToColumns(_columnFields),
                        fDeclarations, uniqueConCols, primaryKeyColumns);

                    // add its indexen
                    for (Index idx : indexen) {
                        liaison.addIndexToTable(
                            conn, getTableName(), fieldsToColumns(idx.fields()),
                            getTableName() + "_" + idx.name(), idx.unique());
                    }

                    // initialize our value generators
                    for (ValueGenerator vg : _valueGenerators.values()) {
                        vg.init(conn, liaison);
                    }

                    // and its full text search indexes
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
            verifySchemasMatch(metaData, ctx, builder);
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

        if (currentVersion >= _schemaVersion) {
            verifySchemasMatch(metaData, ctx, builder);
            return;
        }

        // otherwise try to migrate the schema
        log.info("Migrating " + getTableName() + " from " + currentVersion + " to " +
                 _schemaVersion + "...");

        if (_migrations.size() > 0) {
            // run our pre-default-migrations
            for (EntityMigration migration : _migrations) {
                if (migration.runBeforeDefault() &&
                        migration.shouldRunMigration(currentVersion, _schemaVersion)) {
                    migration.init(getTableName(), _fields);
                    ctx.invoke(migration);
                }
            }

            // we don't know what the pre-migrations did so we have to re-read metadata
            metaData = TableMetaData.load(ctx, getTableName());
        }

        // this is a little silly, but we need a copy for name disambiguation later
        Set<String> indicesCopy = new HashSet<String>(metaData.indexColumns.keySet());

        // add any missing columns
        for (String fname : _columnFields) {
            final FieldMarshaller fmarsh = _fields.get(fname);
            if (metaData.tableColumns.remove(fmarsh.getColumnName())) {
                continue;
            }

            // otherwise add the column
            final ColumnDefinition coldef = fmarsh.getColumnDefinition();
            log.info("Adding column to " + getTableName() + ": " + fmarsh.getColumnName());
            ctx.invoke(new Modifier.Simple() {
                protected String createQuery (DatabaseLiaison liaison) {
                    return "alter table " + liaison.tableSQL(getTableName()) +
                        " add column " + liaison.columnSQL(fmarsh.getColumnName()) + " " +
                        liaison.expandDefinition(coldef);
                }
            });

            // if the column is a TIMESTAMP or DATETIME column, we need to run a special query to
            // update all existing rows to the current time because MySQL annoyingly assigns
            // TIMESTAMP columns a value of "0000-00-00 00:00:00" regardless of whether we
            // explicitly provide a "DEFAULT" value for the column or not, and DATETIME columns
            // cannot accept CURRENT_TIME or NOW() defaults at all.
            if (coldef.getType().equalsIgnoreCase("timestamp") ||
                coldef.getType().equalsIgnoreCase("datetime")) {
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
                    liaison.addPrimaryKey(
                        conn, getTableName(), fieldsToColumns(getPrimaryKeyFields()));
                    return 0;
                }
            });

        } else if (!hasPrimaryKey() && metaData.pkName != null) {
            final String pkName = metaData.pkName;
            log.info("Dropping primary key: " + pkName);
            ctx.invoke(new Modifier() {
                public int invoke (Connection conn, DatabaseLiaison liaison) throws SQLException {
                    liaison.dropPrimaryKey(conn, getTableName(), pkName);
                    return 0;
                }
            });
        }

        // add any named indices that exist on the record but not yet on the table
        for (final Index index : _indexes.values()) {
            final String ixName = getTableName() + "_" + index.name();
            if (metaData.indexColumns.containsKey(ixName)) {
                // this index already exists
                metaData.indexColumns.remove(ixName);
                continue;
            }
            // but this is a new, named index, so we create it
            ctx.invoke(new Modifier() {
                public int invoke (Connection conn, DatabaseLiaison liaison) throws SQLException {
                    liaison.addIndexToTable(
                        conn, getTableName(), fieldsToColumns(index.fields()),
                        ixName, index.unique());
                    return 0;
                }
            });
        }

        // now check if there are any @Table(uniqueConstraints) that need to be created
        Set<Set<String>> uniqueIndices = new HashSet<Set<String>>(metaData.indexColumns.values());

        // unique constraints are unordered and may be unnamed, so we view them only as column sets
        for (Set<String> colSet : _uniqueConstraints) {
            if (uniqueIndices.contains(colSet)) {
                // the table already contains precisely this column set
                continue;
            }

            // else build the new constraint; we'll name it after one of its columns, adding _N
            // to resolve any possible ambiguities, because using all the column names in the
            // index name may exceed the maximum length of an SQL identifier
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
                public int invoke (Connection conn, DatabaseLiaison liaison) throws SQLException {
                    liaison.addIndexToTable(conn, getTableName(), colArr, fName, true);
                    return 0;
                }
            });
        }

        // next we create any full text search indexes that exist on the record but not in the
        // table, first step being to do a dialect-sensitive enumeration of existing indexes
        Set<String> tableFts = new HashSet<String>();
        builder.getFtsIndexes(metaData.tableColumns, metaData.indexColumns.keySet(), tableFts);

        // then iterate over what should be there
        for (final FullTextIndex recordFts : _fullTextIndexes.values()) {
            if (tableFts.contains(recordFts.name())) {
                // the table already contains this one
                continue;
            }

            // but not this one, so let's create it
            ctx.invoke(new Modifier() {
                public int invoke (Connection conn, DatabaseLiaison liaison) throws SQLException {
                    builder.addFullTextSearch(conn, DepotMarshaller.this, recordFts);
                    return 0;
                }
            });
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

        // last of all (re-)initialize our value generators, since one might've been added
        if (_valueGenerators.size() > 0) {
            ctx.invoke(new Modifier() {
                public int invoke (Connection conn, DatabaseLiaison liaison) throws SQLException {
                    for (ValueGenerator vg : _valueGenerators.values()) {
                        vg.init(conn, liaison);
                    }
                    return 0;
                }
            });
        }

        // record our new version in the database
        ctx.invoke(new Modifier() {
            public int invoke (Connection conn, DatabaseLiaison liaison) throws SQLException {
                updateVersion(conn, liaison, _schemaVersion);
                return 0;
            }
        });
    }

    // translate an array of field names to an array of column names
    protected String[] fieldsToColumns (String[] fields)
    {
        String[] columns = new String[fields.length];
        for (int ii = 0; ii < columns.length; ii ++) {
            FieldMarshaller<?> fm = _fields.get(fields[ii]);
            if (fm == null) {
                throw new IllegalArgumentException(
                    "Unknown field on record [field=" + fields[ii] + ", class=" + _pClass + "]");
            }
            columns[ii] = fm.getColumnName();
        }
        return columns;
    }

    /**
     * Checks that there are no database columns for which we no longer have Java fields.
     */
    protected void verifySchemasMatch (
        TableMetaData meta, PersistenceContext ctx, SQLBuilder builder)
        throws PersistenceException
    {
        for (String fname : _columnFields) {
            FieldMarshaller fmarsh = _fields.get(fname);
            meta.tableColumns.remove(fmarsh.getColumnName());
        }
        for (String column : meta.tableColumns) {
            if (builder.isPrivateColumn(column)) {
                continue;
            }
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

    protected static class TableMetaData
    {
        public boolean tableExists;
        public Set<String> tableColumns = new HashSet<String>();
        public Map<String, Set<String>> indexColumns = new HashMap<String, Set<String>>();
        public String pkName;
        public Set<String> pkColumns = new HashSet<String>();

        public static TableMetaData load (PersistenceContext ctx, final String tableName)
            throws PersistenceException
        {
            return ctx.invoke(new Query.TrivialQuery<TableMetaData>() {
                public TableMetaData invoke (Connection conn, DatabaseLiaison dl)
                    throws SQLException {
                    return new TableMetaData(conn.getMetaData(), tableName);
                }
            });
        }

        public TableMetaData (DatabaseMetaData meta, String tableName)
            throws SQLException
        {
            tableExists = meta.getTables("", "", tableName, null).next();
            if (!tableExists) {
                return;
            }

            ResultSet rs = meta.getColumns(null, null, tableName, "%");
            while (rs.next()) {
                tableColumns.add(rs.getString("COLUMN_NAME"));
            }

            rs = meta.getIndexInfo(null, null, tableName, false, false);
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

            rs = meta.getPrimaryKeys(null, null, tableName);
            while (rs.next()) {
                pkName = rs.getString("PK_NAME");
                pkColumns.add(rs.getString("COLUMN_NAME"));
            }
        }
    }

    /** The persistent object class that we manage. */
    protected Class<T> _pClass;

    /** The name of our persistent object table. */
    protected String _tableName;

    /** The @Computed annotation of this entity, or null. */
    protected Computed _computed;

    /** A mapping of field names to value generators for that field. */
    protected Map<String, ValueGenerator> _valueGenerators = new HashMap<String, ValueGenerator>();

    /** A field marshaller for each persistent field in our object. */
    protected Map<String, FieldMarshaller> _fields = new HashMap<String, FieldMarshaller>();

    /** The field marshallers for our persistent object's primary key columns or null if it did not
     * define a primary key. */
    protected ArrayList<FieldMarshaller> _pkColumns;

    /** The persisent fields of our object, in definition order. */
    protected String[] _allFields;

    /** The fields of our object with directly corresponding table columns. */
    protected String[] _columnFields;

    /** The indexes defined in @Entity annotations for this record. */
    protected Map<String, Index> _indexes = new HashMap<String, Index>();

    /** The unique constraints defined in @Table annotations for this record. */
    protected Set<Set<String>> _uniqueConstraints = new HashSet<Set<String>>();

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
