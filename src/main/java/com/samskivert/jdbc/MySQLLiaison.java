//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.jdbc;

import java.sql.*;
import java.util.List;

/**
 * A database liaison for the MySQL database.
 */
public class MySQLLiaison extends BaseLiaison
{
    @Override // from DatabaseLiaison
    public boolean matchesURL (String url)
    {
        return url.startsWith("jdbc:mysql");
    }

    @Override // from DatabaseLiaison
    public boolean isDuplicateRowException (SQLException sqe)
    {
        String msg = sqe.getMessage();
        return (msg != null && msg.indexOf("Duplicate entry") != -1);
    }

    @Override // from DatabaseLiaison
    public boolean isTransientException (SQLException sqe)
    {
        String msg = sqe.getMessage();
        return (msg != null && (msg.indexOf("Lost connection") != -1 ||
                                msg.indexOf("link failure") != -1 ||
                                msg.indexOf("Broken pipe") != -1));
    }

    @Override // from DatabaseLiaison
    public void createGenerator (Connection conn, String tableName, String columnName, int initValue)
        throws SQLException
    {
        // TODO: is there any way we can set the initial AUTO_INCREMENT value?
    }

    @Override // from DatabaseLiaison
    public void deleteGenerator (Connection conn, String tableName, String columnName)
        throws SQLException
    {
        // AUTO_INCREMENT does not create any database entities that we need to delete
    }

    @Override // from BaseLiaison
    public int lastInsertedId (Connection conn, Statement istmt, String table, String column)
        throws SQLException
    {
        // MySQL uses "GENERATED_KEY" as the column name for the last inserted key, so we have to
        // hackily pass that to our super method to get things to work
        return super.lastInsertedId(conn, istmt, table, "GENERATED_KEY");
    }

    @Override
    protected int fetchLastInsertedId (Connection conn, String table, String column)
        throws SQLException
    {
        // MySQL does not keep track of per-table-and-column insertion data, so we are pretty much
        // going on blind faith here that we're fetching the right ID. In the overwhelming number
        // of cases that will be so, but it's still not pretty.
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("select LAST_INSERT_ID()");
            return rs.next() ? rs.getInt(1) : super.fetchLastInsertedId(conn, table, column);
        } finally {
            JDBCUtil.close(stmt);
        }
    }

    @Override // from BaseLiaison
    public boolean addIndexToTable (Connection conn, String table, List<String> columns,
                                    String ixName, boolean unique) throws SQLException
    {
        if (tableContainsIndex(conn, table, ixName)) {
            return false;
        }

        // MySQL's "CREATE INDEX" is buggy, it actually changes the case of the table names you're
        // working with. Luckily (?) ALTER TABLE ADD INDEX works, so we do that here.
        StringBuilder update = new StringBuilder("ALTER TABLE ").append(table).append(" ADD ");
        if (unique) {
            update.append("UNIQUE ");
        }
        update.append("INDEX ").append(indexSQL(ixName)).append(" (");
        appendColumns(columns, update);
        update.append(")");

        executeQuery(conn, update.toString());
        log("Database index '" + ixName + "' added to table '" + table + "'");
        return true;
    }

    @Override // from BaseLiaison
    public void dropIndex (Connection conn, String table, String index) throws SQLException
    {
        executeQuery(conn, "ALTER TABLE " + tableSQL(table) + " DROP INDEX " + columnSQL(index));
    }

    @Override // from BaseLiaison
    public void dropPrimaryKey (Connection conn, String table, String pkName) throws SQLException
    {
        executeQuery(conn, "ALTER TABLE " + tableSQL(table) + " DROP PRIMARY KEY");
    }

    @Override // from BaseLiaison
    public boolean renameColumn (Connection conn, String table, String oldColumnName,
                                 String newColumnName, ColumnDefinition newColumnDef)
        throws SQLException
    {
        if (!tableContainsColumn(conn, table, oldColumnName)) {
            return false;
        }
        executeQuery(conn, "ALTER TABLE " + table + " CHANGE " + oldColumnName + " " +
                     newColumnName + " " + expandDefinition(newColumnDef));
        log("Renamed column '" + oldColumnName + "' on table '" + table + "' to '" +
            newColumnName + "'");
        return true;
    }

    @Override // from DatabaseLiaison
    public String columnSQL (String column)
    {
        return "`" + column + "`";
    }

    @Override // from DatabaseLiaison
    public String tableSQL (String table)
    {
        return "`" + table + "`";
    }

    @Override // from DatabaseLiaison
    public String indexSQL (String index)
    {
        return "`" + index + "`";
    }
}
