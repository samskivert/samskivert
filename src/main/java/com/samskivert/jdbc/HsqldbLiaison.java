//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2011 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.jdbc;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

import com.samskivert.util.ArrayUtil;

import static com.samskivert.Log.log;

/**
 * Handles liaison for HSQLDB.
 */
public class HsqldbLiaison extends BaseLiaison
{
    // from DatabaseLiaison
    public boolean matchesURL (String url)
    {
        return url.startsWith("jdbc:hsqldb");
    }

    // from DatabaseLiaison
    public String columnSQL (String column)
    {
        return "\"" + column + "\"";
    }

    // from DatabaseLiaison
    public String tableSQL (String table)
    {
        return "\"" + table + "\"";
    }

    // from DatabaseLiaison
    public String indexSQL (String index)
    {
        return "\"" + index + "\"";
    }

    // from DatabaseLiaison
    public void createGenerator (Connection conn, String tableName,
                                 String columnName, int initValue)
        throws SQLException
    {
        if (initValue == 1) {
            return; // that's the default! yay, do nothing
        }

        Statement stmt = conn.createStatement();
        try {
            stmt.execute("alter table " + tableSQL(tableName) +
                " alter column " + columnSQL(columnName) +
                " restart with " + initValue);
        } finally {
            JDBCUtil.close(stmt);
        }
        log.info("Initial value of " + tableName + ":" + columnName + " set to " + initValue + ".");
    }

    // from DatabaseLiaison
    public void deleteGenerator (Connection conn, String tableName, String columnName)
        throws SQLException
    {
        // HSQL's IDENTITY() does not create any database entities that we need to delete
    }

    // from DatabaseLiaison
    public int lastInsertedId (Connection conn, String table, String column) throws SQLException
    {
        // HSQL does not keep track of per-table-and-column insertion data, so we are pretty much
        // going on blind faith here that we're fetching the right ID. In the overwhelming number
        // of cases that will be so, but it's still not pretty.
         Statement stmt = null;
         try {
             stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("call IDENTITY()");
             return rs.next() ? rs.getInt(1) : -1;
         } finally {
             JDBCUtil.close(stmt);
         }
    }

    // from DatabaseLiaison
    public boolean isTransientException (SQLException sqe)
    {
        return false; // no known transient exceptions for HSQLDB
    }

    // from DatabaseLiaison
    public boolean isDuplicateRowException (SQLException sqe)
    {
        // Violation of unique constraint SYS_PK_51: duplicate value(s) for column(s) FOO
        String msg = sqe.getMessage();
        return (msg != null && msg.indexOf("duplicate value(s)") != -1);
    }

    // BaseLiaison's implementation of table creation accepts unique constraints both as
    // part of the column definition and as a separate argument, and merrily passes this
    // duality onto the database. Postgres and MySQL both handle this fine but HSQL seems
    // to simply not allow uniqueness in the column definitions. So, for HSQL, we transfer
    // uniqueness from the ColumnDefinitions to the uniqueConstraintColumns before we pass
    // it in to the super implementation.
    //
    // TODO: Consider making this the general MO instead of a subclass override. In fact
    // it may be that uniqueness should be removed from ColumnDefinition.
    @Override // from DatabaseLiaison
    public boolean createTableIfMissing (
        Connection conn, String table, String[] columns, ColumnDefinition[] definitions,
        String[][] uniqueConstraintColumns, String[] primaryKeyColumns)
        throws SQLException
    {
        if (columns.length != definitions.length) {
            throw new IllegalArgumentException("Column name and definition number mismatch");
        }

        // make a set of unique constraints already provided
        Set<List<String>> uColSet = new HashSet<List<String>>();
        if (uniqueConstraintColumns != null) {
            for (String[] uCols : uniqueConstraintColumns) {
                uColSet.add(Arrays.asList(uCols));
            }
        }

        // go through the columns and find any that are unique; these we replace with a
        // non-unique variant, and instead add a new entry to the table unique constraint
        ColumnDefinition[] newDefinitions = new ColumnDefinition[definitions.length];
        for (int ii = 0; ii < definitions.length; ii ++) {
            ColumnDefinition def = definitions[ii];
            if (def.unique) {
                // let's be nice and not mutate the caller's object
                newDefinitions[ii] = new ColumnDefinition(
                    def.type, def.nullable, false, def.defaultValue);
                // if a uniqueness constraint for this column was not in the
                // uniqueConstraintColumns parameter, add such an entry
                if (!uColSet.contains(Collections.singletonList(columns[ii]))) {
                    String[] newConstraint = new String[] { columns[ii] };
                    uniqueConstraintColumns = (uniqueConstraintColumns == null) ?
                        new String[][] { newConstraint } :
                        ArrayUtil.append(uniqueConstraintColumns, newConstraint);
                }
            } else {
                newDefinitions[ii] = def;
            }
        }

        // now call the real implementation with our modified data
        return super.createTableIfMissing(
            conn, table, columns, newDefinitions, uniqueConstraintColumns, primaryKeyColumns);
    }

    @Override // from DatabaseLiaison
    protected String expandDefinition (
        String type, boolean nullable, boolean unique, String defaultValue)
    {
        StringBuilder builder = new StringBuilder(type);

        // append the default value if one was specified
        if (defaultValue != null) {
            if ("IDENTITY".equals(defaultValue)) {
                // this is a blatant hack, we need this method to join Depot's SQLBuilder
                builder.append(" GENERATED BY DEFAULT AS IDENTITY (START WITH 1)");
            } else {
                builder.append(" DEFAULT ").append(defaultValue);
            }
        }

        if (!nullable) {
            builder.append(" NOT NULL");
        }
        if (unique) {
            throw new IllegalArgumentException("HSQL can't deal with column uniqueness here");
        }

        return builder.toString();
    }
}
