//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import com.samskivert.util.StringUtil;

/**
 * A superclass to help with the shrinking subset of SQL our supported dialects can agree on,
 * or when there is disagreement, implement the most standard-compliant version and let the
 * dialectal sub-classes override.
 */
public abstract class BaseLiaison implements DatabaseLiaison
{
    public BaseLiaison () {
        super();
    }

    // we override all the interface methods here so that our subclasses can use @Override without
    // incurring the wrath of the Eclipse Java 1.5 compiler; Jesus Fuck, why do we support 1.5?

    // from DatabaseLiaison
    public abstract boolean matchesURL (String url);

    // from DatabaseLiaison
    public abstract boolean isDuplicateRowException (SQLException sqe);

    // from DatabaseLiaison
    public abstract boolean isTransientException (SQLException sqe);

    @Deprecated
    public int lastInsertedId (Connection conn, String table, String column) throws SQLException {
        return lastInsertedId(conn, null, table, column);
    }

    // from DatabaseLiaison
    public int lastInsertedId (Connection conn, Statement istmt, String table, String column)
        throws SQLException
    {
        // if this JDBC driver supports getGeneratedKeys, use it!
        if (istmt != null && conn.getMetaData().supportsGetGeneratedKeys()) {
            ResultSet rs = istmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(column);
            }
        }
        return fetchLastInsertedId(conn, table, column);
    }

    /**
     * Requests the last inserted id for the specified table and column. This is used if a JDBC
     * driver does not support {@code getGeneratedKeys} or an attempt to use that failed.
     */
    protected int fetchLastInsertedId (Connection conn, String table, String column)
        throws SQLException
    {
        throw new SQLException(
            "Unable to obtain last inserted id [table=" + table + ", column=" + column + "]");
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
    public boolean addIndexToTable (Connection conn, String table, List<String> columns,
                                    String ixName, boolean unique) throws SQLException
    {
        if (tableContainsIndex(conn, table, ixName)) {
            return false;
        }
        ixName = (ixName != null ? ixName :
                  StringUtil.join(columns.toArray(new String[columns.size()]), "_"));

        StringBuilder update = new StringBuilder("CREATE ");
        if (unique) {
            update.append("UNIQUE ");
        }
        update.append("INDEX ").append(indexSQL(ixName)).append(" ON ").
            append(tableSQL(table)).append(" (");
        appendColumns(columns, update);
        update.append(")");

        executeQuery(conn, update.toString());
        log("Database index '" + ixName + "' added to table '" + table + "'");
        return true;
    }

    // from DatabaseLiaison
    public void addPrimaryKey (Connection conn, String table, List<String> columns)
        throws SQLException
    {
        StringBuilder fields = new StringBuilder("(");
        appendColumns(columns, fields);
        fields.append(")");
        String update = "ALTER TABLE " + tableSQL(table) + " ADD PRIMARY KEY " + fields.toString();

        executeQuery(conn, update);
        log("Primary key " + fields + " added to table '" + table + "'");
    }

    // from DatabaseLiaison
    public void dropIndex (Connection conn, String table, String index) throws SQLException
    {
        executeQuery(conn, "DROP INDEX " + columnSQL(index));
    }

    // from DatabaseLiaison
    public void dropPrimaryKey (Connection conn, String table, String pkName) throws SQLException
    {
        executeQuery(conn, "ALTER TABLE " + tableSQL(table) +
                     " DROP CONSTRAINT " + columnSQL(pkName));
    }

    // from DatabaseLiaison
    public boolean addColumn (Connection conn, String table, String column, String definition,
                              boolean check) throws SQLException
    {
        if (check && tableContainsColumn(conn, table, column)) {
            return false;
        }

        executeQuery(conn, "ALTER TABLE " + tableSQL(table) + " ADD COLUMN " +
                     columnSQL(column) + " " + definition);
        log("Database column '" + column + "' added to table '" + table + "'.");
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
        log("Database column '" + column + "' added to table '" + table + "'.");
        return true;
    }

    // from DatabaseLiaison
    public boolean changeColumn (Connection conn, String table, String column, String type,
                                 Boolean nullable, Boolean unique, String defaultValue)
        throws SQLException
    {
        String defStr = expandDefinition(type, nullable != null ? nullable : false,
                                         unique != null ? unique : false, defaultValue);

        executeQuery(conn, "ALTER TABLE " + tableSQL(table) + " CHANGE " +
                     columnSQL(column) + " " + columnSQL(column) + " " + defStr);
        log("Database column '" + column + "' of table '" + table + "' modified to have " +
            "definition '" + defStr + "'.");
        return true;
    }

    // from DatabaseLiaison
    public boolean renameColumn (Connection conn, String table, String from, String to,
                                 ColumnDefinition newColumnDef) throws SQLException
    {
        executeQuery(conn, "ALTER TABLE " + tableSQL(table) + " RENAME COLUMN " +
                     columnSQL(from) + " TO " + columnSQL(to));
        log("Renamed column '" + from + "' on table '" + table + "' to '" + to + "'");
        return true;
    }

    // from DatabaseLiaison
    public abstract void createGenerator (Connection conn, String tableName, String columnName,
                                          int initialValue)
        throws SQLException;

    // from DatabaseLiaison
    public abstract void deleteGenerator (Connection conn, String tableName, String columnName)
        throws SQLException;

    // from DatabaseLiaison
    public boolean dropColumn (Connection conn, String table, String column) throws SQLException
    {
        if (!tableContainsColumn(conn, table, column)) {
            return false;
        }
        executeQuery(conn, "ALTER TABLE " + tableSQL(table) + " DROP COLUMN " + columnSQL(column));
        log("Database column '" + column + "' removed from table '" + table + "'.");
        return true;
    }

    // from DatabaseLiaison
    public boolean createTableIfMissing (Connection conn, String table, List<String> columns,
                                         List<ColumnDefinition> declarations,
                                         List<String> primaryKeyColumns) throws SQLException {
        return createTableIfMissing(conn, table, columns, declarations, null, primaryKeyColumns);
    }

    // from DatabaseLiaison
    public boolean createTableIfMissing (Connection conn, String table, List<String> columns,
                                         List<ColumnDefinition> definitions,
                                         List<List<String>> uniqueConstraintColumns,
                                         List<String> primaryKeyColumns)
        throws SQLException
    {
        if (tableExists(conn, table)) {
            return false;
        }
        if (columns.size() != definitions.size()) {
            throw new IllegalArgumentException("Column name and definition number mismatch");
        }

        StringBuilder builder = new StringBuilder("CREATE TABLE ").
            append(tableSQL(table)).append(" (");
        for (int ii = 0; ii < columns.size(); ii ++) {
            if (ii > 0) {
                builder.append(", ");
            }
            builder.append(columnSQL(columns.get(ii))).append(" ");
            builder.append(expandDefinition(definitions.get(ii)));
        }

        if (uniqueConstraintColumns != null) {
            for (List<String> uCols : uniqueConstraintColumns) {
                builder.append(", UNIQUE (");
                appendColumns(uCols, builder);
                builder.append(")");
            }
        }

        if (primaryKeyColumns != null && !primaryKeyColumns.isEmpty()) {
            builder.append(", PRIMARY KEY (");
            appendColumns(primaryKeyColumns, builder);
            builder.append(")");
        }

        builder.append(")");

        executeQuery(conn, builder.toString());
        log("Database table '" + table + "' created.");
        return true;
    }

    // from DatabaseLiaison
    public boolean dropTable (Connection conn, String name) throws SQLException
    {
        if (!tableExists(conn, name)) {
            return false;
        }
        executeQuery(conn, "DROP TABLE " + tableSQL(name));
        log("Table '" + name + "' dropped.");
        return true;
    }

    // from DatabaseLiaison
    public abstract String tableSQL (String table);

    // from DatabaseLiaison
    public abstract String columnSQL (String column);

    // from DatabaseLiaison
    public abstract String indexSQL (String index);

    /**
     * Create an SQL string that summarizes a column definition in that format generally accepted
     * in table creation and column addition statements, e.g. {@code INTEGER UNIQUE NOT NULL
     * DEFAULT 100}.
     */
    public String expandDefinition (ColumnDefinition def)
    {
        return expandDefinition(def.type, def.nullable, def.unique, def.defaultValue);
    }

    protected int executeQuery (Connection conn, String query) throws SQLException
    {
        Statement stmt = conn.createStatement();
        try {
            return stmt.executeUpdate(query);
        } finally {
            JDBCUtil.close(stmt);
        }
    }

    protected String expandDefinition (String type, boolean nullable, boolean unique,
                                       String defaultValue)
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

    /** Escapes {@code columns} with {@link #columnSQL}, appends (comma-sepped) to {@code buf}. */
    protected void appendColumns (Iterable<String> columns, StringBuilder buf) {
        int ii = 0;
        for (String column : columns) {
            if (ii++ > 0) {
                buf.append(", ");
            }
            buf.append(columnSQL(column));
        }
    }

    protected void log (String message) {
        Log.log.info(message);
    }
}
