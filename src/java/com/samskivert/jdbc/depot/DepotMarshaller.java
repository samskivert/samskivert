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
                    log.warning("Failed to read schema version [class=" + _pClass + "].", e);
                }
            }

            // the field must be public, non-static and non-transient
            if (!java.lang.reflect.Modifier.isPublic(mods) ||
                java.lang.reflect.Modifier.isStatic(mods) ||
                field.getAnnotation(Transient.class) != null) {
                continue;
            }

            FieldMarshaller<?> fm = FieldMarshaller.createMarshaller(field);
            _fields.put(field.getName(), fm);
            fields.add(field.getName());

            // check to see if this is our primary key
            if (field.getAnnotation(Id.class) != null) {
                if (_pkColumns == null) {
                    _pkColumns = new ArrayList<FieldMarshaller<?>>();
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

                case SEQUENCE: // TODO
                    throw new IllegalArgumentException(
                        "SEQUENCE key generation strategy not yet supported.");
                }
            }

        }

        // if we did not find a schema version field, freak out (but not for computed records, for
        // whom there is no table)
        if (_tableName != null && _schemaVersion <= 0) {
            throw new IllegalStateException(
                pClass.getName() + "." + SCHEMA_VERSION_FIELD + " must be greater than zero.");
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
                        FieldMarshaller<?> fm = _fields.get(conFields[ii]);
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
                for (FullTextIndex fti : entity.fullTextIndices()) {
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
    public FieldMarshaller<?> getFieldMarshaller (String fieldName)
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
            Comparable<?>[] values = new Comparable<?>[_pkColumns.size()];
            int nulls = 0, zeros = 0;
            for (int ii = 0; ii < _pkColumns.size(); ii++) {
                FieldMarshaller<?> field = _pkColumns.get(ii);
                values[ii] = (Comparable<?>)field.getField().get(object);
                if (values[ii] == null) {
                    nulls++;
                } else if (values[ii] instanceof Number && ((Number)values[ii]).intValue() == 0) {
                    nulls++; // zeros are considered nulls; see below
                    zeros++;
                }
            }

            // make sure the keys are all null or all non-null
            if (nulls == 0) {
                return makePrimaryKey(values);
            } else if (nulls == values.length) {
                return null;
            } else if (nulls == zeros) {
                // we also allow primary keys where there are zero-valued primitive primary key
                // columns as along as there is at least one non-zero valued additional key column;
                // this is a compromise that allows sensible things like (id=99, type=0) but
                // unfortunately also allows less sensible things like (id=0, type=5) while
                // continuing to disallow the dangerous (id=0)
                return makePrimaryKey(values);
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
    public Key<T> makePrimaryKey (Comparable<?>... values)
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
        Comparable<?>[] values = new Comparable<?>[_pkColumns.size()];
        for (int ii = 0; ii < _pkColumns.size(); ii++) {
            Object keyValue = _pkColumns.get(ii).getFromSet(rs);
            if (!(keyValue instanceof Comparable<?>)) {
                throw new IllegalArgumentException("Key field must be Comparable<?> [field=" +
                                                   _pkColumns.get(ii).getColumnName() + "]");
            }
            values[ii] = (Comparable<?>) keyValue;
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
     * a properly constructed query (see {@link BuildVisitor}).
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
            for (FieldMarshaller<?> fm : _fields.values()) {
                if (!fields.contains(fm.getColumnName())) {
                    // this field was not in the result set, make sure that's OK
                    if (fm.getComputed() != null && !fm.getComputed().required()) {
                        continue;
                    }
                    throw new SQLException("ResultSet missing field: " + fm.getField().getName());
                }
                fm.getAndWriteToObject(rs, po);
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
     * Go through the registered {@link ValueGenerator}s for our persistent object and run the ones
     * that match the current postFactum phase, filling in the fields on the supplied object while
     * we go.
     *
     * The return value is only non-empty for the !postFactum phase, in which case it is a set of
     * field names that are associated with {@link IdentityValueGenerator}, because these need
     * special handling in the INSERT (specifically, 'DEFAULT' must be supplied as a value in the
     * eventual SQL).
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
    protected void registerMigration (SchemaMigration migration)
    {
        _schemaMigs.add(migration);
    }

    /**
     * Initializes the table used by this marshaller. This is called automatically by the {@link
     * PersistenceContext} the first time an entity is used. If the table does not exist, it will
     * be created. If the schema version specified by the persistent object is newer than the
     * database schema, it will be migrated.
     */
    protected void init (PersistenceContext ctx)
        throws DatabaseException
    {
        if (_initialized) { // sanity check
            throw new IllegalStateException(
                "Cannot re-initialize marshaller [type=" + _pClass + "].");
        }
        _initialized = true;

        final SQLBuilder builder = ctx.getSQLBuilder(new DepotTypes(ctx, _pClass));

        // perform the context-sensitive initialization of the field marshallers
        for (FieldMarshaller<?> fm : _fields.values()) {
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
            FieldMarshaller<?> fm = _fields.get(_allFields[ii]);
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

        // check to see if our schema version table exists, create it if not
        ctx.invoke(new Modifier() {
            @Override
            public Integer invoke (Connection conn, DatabaseLiaison liaison) throws SQLException {
                liaison.createTableIfMissing(
                    conn, SCHEMA_VERSION_TABLE,
                    new String[] { P_COLUMN, V_COLUMN, MV_COLUMN },
                    new ColumnDefinition[] {
                        new ColumnDefinition("VARCHAR(255)", false, true, null),
                        new ColumnDefinition("INTEGER", false, false, null),
                        new ColumnDefinition("INTEGER", false, false, null)
                    },
                    null,
                    new String[] { P_COLUMN });
                // add our new "migratingVersion" column if it's not already there
                liaison.addColumn(conn, SCHEMA_VERSION_TABLE, MV_COLUMN,
                                  "integer not null default 0", true);
                return 0;
            }
        });

        // fetch all relevant information regarding our table from the database
        TableMetaData metaData = TableMetaData.load(ctx, getTableName());

        // determine whether or not this record has ever been seen
        int currentVersion = ctx.invoke(new ReadVersion());
        if (currentVersion == -1) {
            log.info("Creating initial version record for " + _pClass.getName() + ".");
            // if not, create a version entry with version zero
            ctx.invoke(new SimpleModifier() {
                protected int invoke (DatabaseLiaison liaison, Statement stmt) throws SQLException {
                    try {
                        return stmt.executeUpdate(
                            "insert into " + liaison.tableSQL(SCHEMA_VERSION_TABLE) +
                            " values('" + getTableName() + "', 0 , 0)");
                    } catch (SQLException e) {
                        // someone else might be doing this at the exact same time which is OK,
                        // we'll coordinate with that other process in the next phase
                        if (liaison.isDuplicateRowException(e)) {
                            return 0;
                        } else {
                            throw e;
                        }
                    }
                }
            });
        }

        // now check whether we need to migrate our database schema
        boolean gotMigrationLock = false;
        while (!gotMigrationLock) {
            currentVersion = ctx.invoke(new ReadVersion());
            if (currentVersion >= _schemaVersion) {
                checkForStaleness(metaData, ctx, builder);
                return;
            }

            // try to update migratingVersion to the new version to indicate to other processes
            // that we are handling the migration and that they should wait
            if (ctx.invoke(new UpdateMigratingVersion(_schemaVersion, 0)) > 0) {
                break; // we got the lock, let's go
            }

            // we didn't get the lock, so wait 5 seconds and then check to see if the other process
            // finished the update or failed in which case we'll try to grab the lock ourselves
            try {
                log.info("Waiting on migration lock for " + _pClass.getName() + ".");
                Thread.sleep(5000);
            } catch (InterruptedException ie) {
                throw new DatabaseException("Interrupted while waiting on migration lock.");
            }
        }

        try {
            if (!metaData.tableExists) {
                // if the table does not exist, create it
                createTable(ctx, builder, declarations);
                metaData = TableMetaData.load(ctx, getTableName());
            } else {
                // if it does exist, run our migrations
                metaData = runMigrations(ctx, metaData, builder, currentVersion);
            }

            // check for stale columns now that the table is up to date
            checkForStaleness(metaData, ctx, builder);

            // and update our version in the schema version table
            ctx.invoke(new SimpleModifier() {
                protected int invoke (DatabaseLiaison liaison, Statement stmt) throws SQLException {
                    return stmt.executeUpdate(
                        "update " + liaison.tableSQL(SCHEMA_VERSION_TABLE) +
                        "   set " + liaison.columnSQL(V_COLUMN) + " = " + _schemaVersion +
                        " where " + liaison.columnSQL(P_COLUMN) + " = '" + getTableName() + "'");
                }
            });

        } finally {
            // set our migrating version back to zero
            try {
                if (ctx.invoke(new UpdateMigratingVersion(0, _schemaVersion)) == 0) {
                    log.warning("Failed to restore migrating version to zero!", "record", _pClass);
                }
            } catch (Exception e) {
                log.warning("Failure restoring migrating version! Bad bad!", "record", _pClass, e);
            }
        }
    }

    protected void createTable (PersistenceContext ctx, final SQLBuilder builder,
                                final ColumnDefinition[] declarations)
        throws DatabaseException
    {
        log.info("Creating initial table '" + getTableName() + "'.");

        final String[][] uniqueConCols = new String[_uniqueConstraints.size()][];
        int kk = 0;
        for (Set<String> colSet : _uniqueConstraints) {
            uniqueConCols[kk++] = colSet.toArray(new String[colSet.size()]);
        }
        final Iterable<Index> indexen = _indexes.values();
        ctx.invoke(new Modifier() {
            @Override
            public Integer invoke (Connection conn, DatabaseLiaison liaison) throws SQLException {
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
                    declarations, uniqueConCols, primaryKeyColumns);

                // add its indexen
                for (Index idx : indexen) {
                    liaison.addIndexToTable(
                        conn, getTableName(), fieldsToColumns(idx.fields()),
                        getTableName() + "_" + idx.name(), idx.unique());
                }

                // create our value generators
                for (ValueGenerator vg : _valueGenerators.values()) {
                    vg.create(conn, liaison);
                }

                // and its full text search indexes
                for (FullTextIndex fti : _fullTextIndexes.values()) {
                    builder.addFullTextSearch(conn, DepotMarshaller.this, fti);
                }

                return 0;
            }
        });
    }

    protected TableMetaData runMigrations (PersistenceContext ctx, TableMetaData metaData,
                                           final SQLBuilder builder, int currentVersion)
        throws DatabaseException
    {
        log.info("Migrating " + getTableName() + " from " + currentVersion + " to " +
                 _schemaVersion + "...");

        if (_schemaMigs.size() > 0) {
            // run our pre-default-migrations
            for (SchemaMigration migration : _schemaMigs) {
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

        // figure out which columns we have in the table now, so that when all is said and done we
        // can see what new columns we have in the table and run the creation code for any value
        // generators that are defined on those columns (we can't just track the columns we add in
        // our automatic migrations because someone might register custom migrations that add
        // columns specially)
        Set<String> preMigrateColumns = new HashSet<String>(metaData.tableColumns);

        // add any missing columns
        for (String fname : _columnFields) {
            final FieldMarshaller<?> fmarsh = _fields.get(fname);
            if (metaData.tableColumns.remove(fmarsh.getColumnName())) {
                continue;
            }

            // otherwise add the column
            final ColumnDefinition coldef = fmarsh.getColumnDefinition();
            log.info("Adding column to " + getTableName() + ": " + fmarsh.getColumnName());
            ctx.invoke(new Modifier.Simple() {
                @Override protected String createQuery (DatabaseLiaison liaison) {
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
            if (!coldef.isNullable() && (coldef.getType().equalsIgnoreCase("timestamp") ||
                                         coldef.getType().equalsIgnoreCase("datetime"))) {
                log.info("Assigning current time to " + fmarsh.getColumnName() + ".");
                ctx.invoke(new Modifier.Simple() {
                    @Override protected String createQuery (DatabaseLiaison liaison) {
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
                @Override public Integer invoke (Connection conn, DatabaseLiaison liaison)
                    throws SQLException {
                    liaison.addPrimaryKey(
                        conn, getTableName(), fieldsToColumns(getPrimaryKeyFields()));
                    return 0;
                }
            });

        } else if (!hasPrimaryKey() && metaData.pkName != null) {
            final String pkName = metaData.pkName;
            log.info("Dropping primary key: " + pkName);
            ctx.invoke(new Modifier() {
                @Override public Integer invoke (Connection conn, DatabaseLiaison liaison)
                    throws SQLException {
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
                @Override public Integer invoke (Connection conn, DatabaseLiaison liaison)
                    throws SQLException {
                    liaison.addIndexToTable(
                        conn, getTableName(), fieldsToColumns(index.fields()),
                        ixName, index.unique());
                    return 0;
                }
            });
        }

        // now check if there are any @Entity(uniqueConstraints) that need to be created
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
                @Override public Integer invoke (Connection conn, DatabaseLiaison liaison)
                    throws SQLException {
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
                @Override public Integer invoke (Connection conn, DatabaseLiaison liaison)
                    throws SQLException {
                    builder.addFullTextSearch(conn, DepotMarshaller.this, recordFts);
                    return 0;
                }
            });
        }

        // we do not auto-remove columns but rather require that SchemaMigration.Drop records be
        // registered by hand to avoid accidentally causing the loss of data

        // we don't auto-remove indices either because we'd have to sort out the potentially
        // complex origins of an index (which might be because of a @Unique column or maybe the
        // index was hand defined in a @Column clause)

        // run our post-default-migrations
        for (SchemaMigration migration : _schemaMigs) {
            if (!migration.runBeforeDefault() &&
                migration.shouldRunMigration(currentVersion, _schemaVersion)) {
                migration.init(getTableName(), _fields);
                ctx.invoke(migration);
            }
        }

        // now reload our table metadata so that we can see what columns we have now
        metaData = TableMetaData.load(ctx, getTableName());

        // initialize value generators for any columns that have been newly added
        for (String column : metaData.tableColumns) {
            if (preMigrateColumns.contains(column)) {
                continue;
            }

            // see if we have a value generator for this new column
            final ValueGenerator valgen = _valueGenerators.get(column);
            if (valgen == null) {
                continue;
            }

            // note: if someone renames a column that has an identity value generator, things will
            // break because Postgres automatically creates a table_column_seq sequence that is
            // used to generate values for that column and god knows what happens when that is
            // renamed; plus we're potentially going to try to reinitialize it if it has a non-zero
            // initialValue which will use the new column name to obtain the sequence name which
            // ain't going to work either; we punt!
            ctx.invoke(new Modifier() {
                @Override public Integer invoke (Connection conn, DatabaseLiaison liaison)
                    throws SQLException {
                    valgen.create(conn, liaison);
                    return 0;
                }
            });
        }

        return metaData;
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
    protected void checkForStaleness (TableMetaData meta, PersistenceContext ctx, SQLBuilder builder)
        throws DatabaseException
    {
        for (String fname : _columnFields) {
            FieldMarshaller<?> fmarsh = _fields.get(fname);
            meta.tableColumns.remove(fmarsh.getColumnName());
        }
        for (String column : meta.tableColumns) {
            if (builder.isPrivateColumn(column)) {
                continue;
            }
            log.warning(getTableName() + " contains stale column '" + column + "'.");
        }
    }

    protected abstract class SimpleModifier extends Modifier {
        @Override
        public Integer invoke (Connection conn, DatabaseLiaison liaison) throws SQLException {
            Statement stmt = conn.createStatement();
            try {
                return invoke(liaison, stmt);
            } finally {
                stmt.close();
            }
        }

        protected abstract int invoke (DatabaseLiaison liaison, Statement stmt) throws SQLException;
    }

    // this is a Modifier not a Query because we want to be sure we're talking to the database
    // server to whom we would talk if we were doing a modification (ie. the master, not a
    // read-only slave)
    protected class ReadVersion extends SimpleModifier {
        protected int invoke (DatabaseLiaison liaison, Statement stmt) throws SQLException {
            ResultSet rs = stmt.executeQuery(
                " select " + liaison.columnSQL(V_COLUMN) +
                "   from " + liaison.tableSQL(SCHEMA_VERSION_TABLE) +
                "  where " + liaison.columnSQL(P_COLUMN) + " = '" + getTableName() + "'");
            return (rs.next()) ? rs.getInt(1) : -1;
        }
    }

    protected class UpdateMigratingVersion extends SimpleModifier {
        public UpdateMigratingVersion (int newMigratingVersion, int guardVersion) {
            _newMigratingVersion = newMigratingVersion;
            _guardVersion = guardVersion;
        }
        protected int invoke (DatabaseLiaison liaison, Statement stmt) throws SQLException {
            return stmt.executeUpdate(
                "update " + liaison.tableSQL(SCHEMA_VERSION_TABLE) +
                "   set " + liaison.columnSQL(MV_COLUMN) + " = " + _newMigratingVersion +
                " where " + liaison.columnSQL(P_COLUMN) + " = '" + getTableName() + "'" +
                " and " + liaison.columnSQL(MV_COLUMN) + " = " + _guardVersion);
        }
        protected int _newMigratingVersion, _guardVersion;
    }

    protected static class TableMetaData
    {
        public boolean tableExists;
        public Set<String> tableColumns = new HashSet<String>();
        public Map<String, Set<String>> indexColumns = new HashMap<String, Set<String>>();
        public String pkName;
        public Set<String> pkColumns = new HashSet<String>();

        public static TableMetaData load (PersistenceContext ctx, final String tableName)
            throws DatabaseException
        {
            return ctx.invoke(new Query.Trivial<TableMetaData>() {
                @Override public TableMetaData invoke (Connection conn, DatabaseLiaison dl)
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
    protected Map<String, FieldMarshaller<?>> _fields = new HashMap<String, FieldMarshaller<?>>();

    /** The field marshallers for our persistent object's primary key columns or null if it did not
     * define a primary key. */
    protected ArrayList<FieldMarshaller<?>> _pkColumns;

    /** The persisent fields of our object, in definition order. */
    protected String[] _allFields;

    /** The fields of our object with directly corresponding table columns. */
    protected String[] _columnFields;

    /** The indexes defined in @Entity annotations for this record. */
    protected Map<String, Index> _indexes = new HashMap<String, Index>();

    /** The unique constraints defined in @Entity annotations for this record. */
    protected Set<Set<String>> _uniqueConstraints = new HashSet<Set<String>>();

    protected Map<String, FullTextIndex> _fullTextIndexes = new HashMap<String, FullTextIndex>();

    /** The version of our persistent object schema as specified in the class definition. */
    protected int _schemaVersion = -1;

    /** Indicates that we have been initialized (created or migrated our tables). */
    protected boolean _initialized;

    /** A list of hand registered schema migrations to run prior to doing the default migration. */
    protected ArrayList<SchemaMigration> _schemaMigs = new ArrayList<SchemaMigration>();

    /** The name of the table we use to track schema versions. */
    protected static final String SCHEMA_VERSION_TABLE = "DepotSchemaVersion";

    /** The name of the 'persistentClass' column in the {@link #SCHEMA_VERSION_TABLE}. */
    protected static final String P_COLUMN = "persistentClass";

    /** The name of the 'version' column in the {@link #SCHEMA_VERSION_TABLE}. */
    protected static final String V_COLUMN = "version";

    /** The name of the 'migratingVersion' column in the {@link #SCHEMA_VERSION_TABLE}. */
    protected static final String MV_COLUMN = "migratingVersion";
}
