//
// $Id$
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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import com.samskivert.jdbc.JDBCUtil;

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

        public int invoke (Connection conn) throws SQLException {
            if (!JDBCUtil.tableContainsColumn(conn, _tableName, _columnName)) {
                // we'll accept this inconsistency
                log.warning(_tableName + "." + _columnName + " already dropped.");
                return 0;
            }

            Statement stmt = conn.createStatement();
            try {
                log.info("Dropping '" + _columnName + "' from " + _tableName);
                return stmt.executeUpdate(
                    "alter table " + _tableName + " drop column " + _columnName);
            } finally {
                stmt.close();
            }
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

        public int invoke (Connection conn) throws SQLException {
            Statement stmt = conn.createStatement();
            try {
                log.info("Changing '" + _oldColumnName + "' to '" + _newColumnDef + "' in " +
                         _tableName);

                if (!JDBCUtil.tableContainsColumn(conn, _tableName, _oldColumnName)) {
                    if (JDBCUtil.tableContainsColumn(conn, _tableName, _newColumnName)) {
                        // we'll accept this inconsistency
                        log.warning("Column rename appears already performed.");
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

                return stmt.executeUpdate("alter table " + _tableName + " change column " +
                                          _oldColumnName + " " + _newColumnDef);

            } finally {
                stmt.close();
            }
        }

        protected void init (String tableName, HashMap<String,FieldMarshaller> marshallers)
        {
            super.init(tableName, marshallers);
            _newColumnDef = marshallers.get(_newColumnName).getColumnDefinition();
        }

        protected String _oldColumnName,  _newColumnName, _newColumnDef;
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
    protected void init (String tableName, HashMap<String,FieldMarshaller> marshallers)
    {
        _tableName = tableName;
    }

    protected int _targetVersion;
    protected String _tableName;
}
