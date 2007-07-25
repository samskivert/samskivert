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

import com.samskivert.jdbc.DatabaseLiaison;
import com.samskivert.jdbc.JDBCUtil;
import com.samskivert.jdbc.LiaisonRegistry;

import static com.samskivert.jdbc.depot.Log.log;

/**
 * These can be registered with the {@link PersistenceContext} to effect hand-coded migrations
 * between entity versions. The modifier should override {@link #invoke} to perform its
 * migrations. See {@link PersistenceContext#registerPreMigration} and {@link
 * PersistenceContext#registerPostMigration}.
 */
public abstract class EntityMigration extends Modifier
{
    /**
     * A convenient migration for dropping a column from an entity.
     */
    public static class Drop extends EntityMigration
    {
        public Drop (int targetVersion, String columnName) {
            super(targetVersion);
            _columnName = columnName;
        }

        public int invoke (Connection conn, DatabaseLiaison liaison) throws SQLException {
            if (!JDBCUtil.tableContainsColumn(conn, _tableName, _columnName)) {
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
    public static class Rename extends EntityMigration
    {
        public Rename (int targetVersion, String oldColumnName, String newColumnName) {
            super(targetVersion);
            _oldColumnName = oldColumnName;
            _newColumnName = newColumnName;
        }

        public int invoke (Connection conn, DatabaseLiaison liaison) throws SQLException {
            if (!JDBCUtil.tableContainsColumn(conn, _tableName, _oldColumnName)) {
                if (JDBCUtil.tableContainsColumn(conn, _tableName, _newColumnName)) {
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
            if (JDBCUtil.tableContainsColumn(conn, _tableName, _newColumnName)) {
                throw new IllegalArgumentException(
                    _tableName + " already contains '" + _newColumnName + "'");
            }

            log.info("Renaming '" + _oldColumnName + "' to '" + _newColumnName + "' in: " +
                _tableName);
            return liaison.renameColumn(conn, _tableName, _oldColumnName, _newColumnName) ? 1 : 0;
        }

        public boolean runBeforeDefault () {
            return false;
        }

        protected String _oldColumnName,  _newColumnName;
    }

    /**
     * A convenient migration for changing the type of an existing field. NOTE: This object is
     * instantiated with the name of a persistent field, not the name of a database column. These
     * can be very different things for classes that use @Column annotations.
     */
    public static class Retype extends EntityMigration
    {
        public Retype (int targetVersion, String fieldName) {
            super(targetVersion);
            _fieldName = fieldName;
        }

        public int invoke (Connection conn, DatabaseLiaison liaison) throws SQLException {
            log.info("Updating type of '" + _fieldName + "' in " + _tableName);
            return liaison.changeColumn(conn, _tableName, _fieldName, _newColumnDef) ? 1 : 0;
        }

        public boolean runBeforeDefault () {
            return false;
        }

        protected void init (String tableName, Map<String,FieldMarshaller> marshallers) {
            super.init(tableName, marshallers);
            _columnName = marshallers.get(_fieldName).getColumnName();
            _newColumnDef = marshallers.get(_fieldName).getColumnDefinition();
        }

        protected String _fieldName, _columnName, _newColumnDef;
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

    protected EntityMigration (int targetVersion)
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
    protected void init (String tableName, Map<String,FieldMarshaller> marshallers)
    {
        _tableName = tableName;
    }

    protected int _targetVersion;
    protected String _tableName;
}
