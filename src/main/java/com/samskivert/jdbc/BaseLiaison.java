//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2011 Michael Bayne, et al.
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

package com.samskivert.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.samskivert.util.StringUtil;

import static com.samskivert.Log.log;

/**
 * A superclass to help with the shrinking subset of SQL our supported dialects can agree on,
 * or when there is disagreement, implement the most standard-compliant version and let the
 * dialectal sub-classes override.
 */
public abstract class BaseLiaison implements DatabaseLiaison
{
    public BaseLiaison ()
    {
        super();
    }

    // from DatabaseLiaison
    public boolean tableExists (Connection conn, String name) throws SQLException
    {
        ResultSet rs = conn.getMetaData().getTables(null, null, name, null);
        while (rs.next()) {
            String tname = rs.getString("TABLE_NAME");
            if (name.equals(tname)) {
                return true;
            }
        }
        return false;
    }

    // from DatabaseLiaison
    public boolean tableContainsColumn (Connection conn, String table, String column)
        throws SQLException
    {
        ResultSet rs = conn.getMetaData().getColumns(null, null, table, column);
        while (rs.next()) {
            String tname = rs.getString("TABLE_NAME");
            String cname = rs.getString("COLUMN_NAME");
            if (tname.equals(table) && cname.equals(column)) {
                return true;
            }
        }
        return false;
    }

    // from DatabaseLiaison
    public boolean tableContainsIndex (Connection conn, String table, String index)
        throws SQLException
    {
        ResultSet rs = conn.getMetaData().getIndexInfo(null, null, table, false, true);
        while (rs.next()) {
            String tname = rs.getString("TABLE_NAME");
            String iname = rs.getString("INDEX_NAME");
            if (tname.equals(table) && index.equals(iname)) {
                return true;
            }
        }
        return false;
    }

    // from DatabaseLiaison
    public boolean addIndexToTable (
        Connection conn, String table, String[] columns, String ixName, boolean unique)
        throws SQLException
    {
        if (tableContainsIndex(conn, table, ixName)) {
            return false;
        }
        ixName = (ixName != null ? ixName : StringUtil.join(columns, "_"));

        StringBuilder update = new StringBuilder("CREATE ");
        if (unique) {
            update.append("UNIQUE ");
        }
        update.append("INDEX ").append(indexSQL(ixName)).append(" ON ").
            append(tableSQL(table)).append(" (");
        for (int ii = 0; ii < columns.length; ii ++) {
            if (ii > 0) {
                update.append(", ");
            }
            update.append(columnSQL(columns[ii]));
        }
        update.append(")");

        executeQuery(conn, update.toString());
        log.info("Database index '" + ixName + "' added to table '" + table + "'");
        return true;
    }

    // from DatabaseLiaison
    public void addPrimaryKey (Connection conn, String table, String[] columns)
        throws SQLException
    {
        StringBuilder fields = new StringBuilder("(");
        for (int ii = 0; ii < columns.length; ii ++) {
            if (ii > 0) {
                fields.append(", ");
            }
            fields.append(columnSQL(columns[ii]));
        }
        fields.append(")");
        String update = "ALTER TABLE " + tableSQL(table) + " ADD PRIMARY KEY " + fields.toString();

        executeQuery(conn, update);
        log.info("Primary key " + fields + " added to table '" + table + "'");
    }

    // from DatabaseLiaison
    public void dropIndex (Connection conn, String table, String index) throws SQLException
    {
        executeQuery(conn, "DROP INDEX " + columnSQL(index));
    }

    // from DatabaseLiaison
    public void dropPrimaryKey (Connection conn, String table, String pkName)
        throws SQLException
    {
        executeQuery(conn, "ALTER TABLE " + tableSQL(table) +
            " DROP CONSTRAINT " + columnSQL(pkName));
    }

    // from DatabaseLiaison
    public boolean addColumn (
        Connection conn, String table, String column, String definition, boolean check)
        throws SQLException
    {
        if (check && tableContainsColumn(conn, table, column)) {
            return false;
        }

        executeQuery(conn, "ALTER TABLE " + tableSQL(table) + " ADD COLUMN " +
                     columnSQL(column) + " " + definition);
        log.info("Database column '" + column + "' added to table '" + table + "'.");
        return true;
    }

    // from DatabaseLiaison
    public boolean addColumn (Connection conn, String table, String column,
                              ColumnDefinition newColumnDef, boolean check)
        throws SQLException
    {
        if (check && tableContainsColumn(conn, table, column)) {
            return false;
        }

        executeQuery(conn, "ALTER TABLE " + tableSQL(table) + " ADD COLUMN " +
                     columnSQL(column) + " " + expandDefinition(newColumnDef));
        log.info("Database column '" + column + "' added to table '" + table + "'.");
        return true;
    }

    // from DatabaseLiaison
    public boolean changeColumn (Connection conn, String table, String column, String type,
                                 Boolean nullable, Boolean unique, String defaultValue)
        throws SQLException
    {
        String defStr = expandDefinition(
            type, nullable != null ? nullable : false, unique != null ? unique : false,
            defaultValue);

        executeQuery(conn, "ALTER TABLE " + tableSQL(table) + " CHANGE " +
                     columnSQL(column) + " " + columnSQL(column) + " " + defStr);
        log.info("Database column '" + column + "' of table '" + table + "' modified to have " +
                 "definition '" + defStr + "'.");
        return true;
    }

    // from DatabaseLiaison
    public boolean renameColumn (Connection conn, String table, String from, String to,
                                 ColumnDefinition newColumnDef)
        throws SQLException
    {
        executeQuery(conn, "ALTER TABLE " + tableSQL(table) + " RENAME COLUMN " +
                     columnSQL(from) + " TO " + columnSQL(to));
        log.info("Renamed column '" + from + "' on table '" + table + "' to '" + to + "'");
        return true;
    }

    // from DatabaseLiaison
    public boolean dropColumn (Connection conn, String table, String column) throws SQLException
    {
        if (!tableContainsColumn(conn, table, column)) {
            return false;
        }
        executeQuery(conn, "ALTER TABLE " + tableSQL(table) + " DROP COLUMN " + columnSQL(column));
        log.info("Database column '" + column + "' removed from table '" + table + "'.");
        return true;
    }

    // from DatabaseLiaison
    public boolean createTableIfMissing (
        Connection conn, String table, String[] columns, ColumnDefinition[] definitions,
        String[][] uniqueConstraintColumns, String[] primaryKeyColumns)
        throws SQLException
    {
        if (tableExists(conn, table)) {
            return false;
        }
        if (columns.length != definitions.length) {
            throw new IllegalArgumentException("Column name and definition number mismatch");
        }

        StringBuilder builder =
            new StringBuilder("CREATE TABLE ").append(tableSQL(table)).append(" (");
        for (int ii = 0; ii < columns.length; ii ++) {
            if (ii > 0) {
                builder.append(", ");
            }
            builder.append(columnSQL(columns[ii])).append(" ");
            builder.append(expandDefinition(definitions[ii]));
        }

        if (uniqueConstraintColumns != null && uniqueConstraintColumns.length > 0) {
            for (String[] uCols : uniqueConstraintColumns) {
                builder.append(", UNIQUE (");
                for (int ii = 0; ii < uCols.length; ii ++) {
                    if (ii > 0) {
                        builder.append(", ");
                    }
                    builder.append(columnSQL(uCols[ii]));
                }
                builder.append(")");
            }
        }

        if (primaryKeyColumns != null && primaryKeyColumns.length > 0) {
            builder.append(", PRIMARY KEY (");
            for (int ii = 0; ii < primaryKeyColumns.length; ii ++) {
                if (ii > 0) {
                    builder.append(", ");
                }
                builder.append(columnSQL(primaryKeyColumns[ii]));
            }
            builder.append(")");
        }

        builder.append(")");

        executeQuery(conn, builder.toString());
        log.info("Database table '" + table + "' created.");
        return true;
    }

    public boolean dropTable (Connection conn, String name)
        throws SQLException
    {
        if (!tableExists(conn, name)) {
            return false;
        }
        executeQuery(conn, "DROP TABLE " + tableSQL(name));
        log.info("Table '" + name + "' dropped.");
        return true;
    }

    /**
     * Create an SQL string that summarizes a column definition in that format generally
     * accepted in table creation and column addition statements, e.g.
     *          INTEGER UNIQUE NOT NULL DEFAULT 100
     */
    public String expandDefinition (ColumnDefinition def)
    {
        return expandDefinition(def.type, def.nullable, def.unique, def.defaultValue);
    }

    protected int executeQuery (Connection conn, String query)
        throws SQLException
    {
        Statement stmt = conn.createStatement();
        try {
            return stmt.executeUpdate(query);
        } finally {
            JDBCUtil.close(stmt);
        }
    }

    protected String expandDefinition (
        String type, boolean nullable, boolean unique, String defaultValue)
    {
        StringBuilder builder = new StringBuilder(type);

        if (!nullable) {
            builder.append(" NOT NULL");
        }
        if (unique) {
            builder.append(" UNIQUE");
        }

        // append the default value if one was specified
        if (defaultValue != null) {
            builder.append(" DEFAULT ").append(defaultValue);
        }

        return builder.toString();
    }
}
