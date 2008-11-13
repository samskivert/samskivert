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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import com.samskivert.jdbc.ColumnDefinition;
import com.samskivert.jdbc.DatabaseLiaison;
import com.samskivert.jdbc.depot.annotation.Column;

import static com.samskivert.jdbc.depot.Log.log;

/**
 * Encapsulates the migration of a persistent record's database schema. These can be registered
 * with the {@link PersistenceContext} to effect hand-coded migrations between entity versions. The
 * modifier should override {@link #invoke} to perform its migrations. See {@link
 * PersistenceContext#registerMigration} for details on the migration process.
 *
 * <p> Note: these should only be used for actual schema changes (column additions, removals,
 * renames, retypes, etc.). It should not be used for data migration, use {@link DataMigration} for
 * that.
 */
public abstract class SchemaMigration extends Modifier
{
    /**
     * A convenient migration for dropping a column from an entity.
     */
    public static class Drop extends SchemaMigration
    {
        public Drop (int targetVersion, String columnName) {
            super(targetVersion);
            _columnName = columnName;
        }

        @Override
        public Integer invoke (Connection conn, DatabaseLiaison liaison) throws SQLException {
            if (!liaison.tableContainsColumn(conn, _tableName, _columnName)) {
                // we'll accept this inconsistency
                log.warning(_tableName + "." + _columnName + " already dropped.");
                return 0;
            }

            log.info("Dropping '" + _columnName + "' from " + _tableName);
            return liaison.dropColumn(conn, _tableName, _columnName) ? 1 : 0;
        }

        protected String _columnName;
    }

    /**
     * A convenient migration for renaming a column in an entity.
     */
    public static class Rename extends SchemaMigration
    {
        public Rename (int targetVersion, String oldColumnName, String newColumnName) {
            super(targetVersion);
            _oldColumnName = oldColumnName;
            _newColumnName = newColumnName;
        }

        @Override
        public Integer invoke (Connection conn, DatabaseLiaison liaison) throws SQLException {
            if (!liaison.tableContainsColumn(conn, _tableName, _oldColumnName)) {
                if (liaison.tableContainsColumn(conn, _tableName, _newColumnName)) {
                    // we'll accept this inconsistency
                    log.warning(_tableName + "." + _oldColumnName + " already renamed to " +
                                _newColumnName + ".");
                    return 0;
                }
                // but this is not OK
                throw new IllegalArgumentException(
                    _tableName + " does not contain '" + _oldColumnName + "'");
            }

            // nor is this
            if (liaison.tableContainsColumn(conn, _tableName, _newColumnName)) {
                throw new IllegalArgumentException(
                    _tableName + " already contains '" + _newColumnName + "'");
            }

            log.info("Renaming '" + _oldColumnName + "' to '" + _newColumnName + "' in: " +
                _tableName);
            return liaison.renameColumn(
                conn, _tableName, _oldColumnName, _newColumnName, _newColumnDef) ? 1 : 0;
        }

        @Override public boolean runBeforeDefault () {
            return true;
        }

        @Override
        protected void init (String tableName, Map<String, FieldMarshaller<?>> marshallers) {
            super.init(tableName, marshallers);
            FieldMarshaller<?> fm = marshallers.get(_newColumnName);
            if (fm == null) {
                throw new IllegalArgumentException(
                    _tableName + " does not contain '" + _newColumnName + "' field.");
            }
            _newColumnDef = fm.getColumnDefinition();
        }

        protected String _oldColumnName,  _newColumnName;
        protected ColumnDefinition _newColumnDef;
    }

    /**
     * A convenient migration for changing the type of an existing field. NOTE: This object is
     * instantiated with the name of a persistent field, not the name of a database column. These
     * can be very different things for classes that use @Column annotations.
     */
    public static class Retype extends SchemaMigration
    {
        public Retype (int targetVersion, String fieldName) {
            super(targetVersion);
            _fieldName = fieldName;
        }

        @Override
        public Integer invoke (Connection conn, DatabaseLiaison liaison) throws SQLException {
            log.info("Updating type of '" + _fieldName + "' in " + _tableName);
            return liaison.changeColumn(conn, _tableName, _fieldName, _newColumnDef.getType(),
                _newColumnDef.isNullable(), _newColumnDef.isUnique(),
                _newColumnDef.getDefaultValue()) ? 1 : 0;
        }

        @Override public boolean runBeforeDefault () {
            return false;
        }

        @Override
        protected void init (String tableName, Map<String, FieldMarshaller<?>> marshallers) {
            super.init(tableName, marshallers);
            _columnName = marshallers.get(_fieldName).getColumnName();
            _newColumnDef = marshallers.get(_fieldName).getColumnDefinition();
        }

        protected String _fieldName, _columnName;
        protected ColumnDefinition _newColumnDef;
    }

    /**
     * A convenient migration for adding a new column that requires a default value to be specified
     * during the addition. Normally Depot will automatically handle column addition, but if you
     * have a column that normally does not have a default value but needs one when it is added to
     * a table with existing rows, you can use this migration.
     *
     * @see Column#defaultValue
     */
    public static class Add extends SchemaMigration
    {
        public Add (int targetVersion, String fieldName, String defaultValue) {
            super(targetVersion);
            _fieldName = fieldName;
            _defaultValue = defaultValue;
        }

        @Override
        public Integer invoke (Connection conn, DatabaseLiaison liaison) throws SQLException {
            // override the default value in the column definition with the one provided
            ColumnDefinition defColumnDef = new ColumnDefinition(
                _newColumnDef.getType(), _newColumnDef.isNullable(),
                _newColumnDef.isUnique(), _defaultValue);
            // first add the column with the overridden default value
            if (liaison.addColumn(conn, _tableName, _fieldName, defColumnDef, true)) {
                // then change the column to the permanent default value
                liaison.changeColumn(conn, _tableName, _fieldName, _newColumnDef.getType(),
                                     null, null, _newColumnDef.getDefaultValue());
                return 1;
            }
            return 0;
        }

        @Override public boolean runBeforeDefault () {
            return true;
        }

        @Override
        protected void init (String tableName, Map<String, FieldMarshaller<?>> marshallers) {
            super.init(tableName, marshallers);
            _columnName = marshallers.get(_fieldName).getColumnName();
            _newColumnDef = marshallers.get(_fieldName).getColumnDefinition();
        }

        protected String _fieldName, _columnName, _defaultValue;
        protected ColumnDefinition _newColumnDef;
    }

    /**
     * If this method returns true, this migration will be run <b>before</b> the default
     * migrations, if false it will be run after.
     */
    public boolean runBeforeDefault ()
    {
        return true;
    }

    /**
     * When an Entity is being migrated, this method will be called to check whether this migration
     * should be run. The default implementation runs as long as the currentVersion is less than
     * the target version supplied to the migration at construct time.
     */
    public boolean shouldRunMigration (int currentVersion, int targetVersion)
    {
        return (currentVersion < _targetVersion);
    }

    protected SchemaMigration (int targetVersion)
    {
        super();
        _targetVersion = targetVersion;
    }

    /**
     * This is called to provide the migration with the name of the entity table and access to its
     * field marshallers prior to being invoked. This will <em>only</em> be called after this
     * migration has been determined to be runnable so one cannot rely on this method having been
     * called in {@link #shouldRunMigration}.
     */
    protected void init (String tableName, Map<String, FieldMarshaller<?>> marshallers)
    {
        _tableName = tableName;
    }

    protected int _targetVersion;
    protected String _tableName;
}
