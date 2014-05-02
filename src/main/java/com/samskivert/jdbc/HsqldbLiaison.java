//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.jdbc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

/**
 * Handles liaison for HSQLDB.
 */
public class HsqldbLiaison extends BaseLiaison
{
    @Override // from DatabaseLiaison
    public boolean matchesURL (String url)
    {
        return url.startsWith("jdbc:hsqldb");
    }

    @Override // from DatabaseLiaison
    public String columnSQL (String column)
    {
        return "\"" + column + "\"";
    }

    @Override // from DatabaseLiaison
    public String tableSQL (String table)
    {
        return "\"" + table + "\"";
    }

    @Override // from DatabaseLiaison
    public String indexSQL (String index)
    {
        return "\"" + index + "\"";
    }

    @Override // from DatabaseLiaison
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
        log("Initial value of " + tableName + ":" + columnName + " set to " + initValue + ".");
    }

    @Override // from DatabaseLiaison
    public void deleteGenerator (Connection conn, String tableName, String columnName)
        throws SQLException
    {
        // HSQL's IDENTITY() does not create any database entities that we need to delete
    }

    @Override
    protected int fetchLastInsertedId (Connection conn, String table, String column)
        throws SQLException
    {
        // HSQL does not keep track of per-table-and-column insertion data, so we are pretty much
        // going on blind faith here that we're fetching the right ID. In the overwhelming number
        // of cases that will be so, but it's still not pretty.
         Statement stmt = null;
         try {
             stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("call IDENTITY()");
             return rs.next() ? rs.getInt(1) : super.fetchLastInsertedId(conn, table, column);
         } finally {
             JDBCUtil.close(stmt);
         }
    }

    @Override // from DatabaseLiaison
    public boolean isTransientException (SQLException sqe)
    {
        return false; // no known transient exceptions for HSQLDB
    }

    @Override // from DatabaseLiaison
    public boolean isDuplicateRowException (SQLException sqe)
    {
        // Violation of unique constraint SYS_PK_51: duplicate value(s) for column(s) FOO
        // integrity constraint violation: unique constraint or index violation; SYS_CT_10057
        String msg = sqe.getMessage();
        return (msg != null && (msg.contains("duplicate value(s)") ||
                                msg.contains("unique constraint or index violation")));
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
    public boolean createTableIfMissing (Connection conn, String table,
                                         List<String> columns, List<ColumnDefinition> defns,
                                         List<List<String>> uniqueColumns, List<String> pkColumns)
        throws SQLException
    {
        if (columns.size() != defns.size()) {
            throw new IllegalArgumentException("Column name and definition number mismatch");
        }

        // note the set of single column unique constraints already provided (see method comment)
        Set<String> seenUniques = new HashSet<String>();
        if (uniqueColumns != null) {
            for (List<String> udef : uniqueColumns) {
                if (udef.size() == 1) {
                    seenUniques.addAll(udef);
                }
            }
        }
        // primary key columns are also considered implicitly unique as of HSQL 2.2.4, and it will
        // freak out if we also try to include them in the UNIQUE clause, so add those too
        seenUniques.addAll(pkColumns);

        // lazily create a copy of uniqueColumns, if needed
        List<List<String>> newUniques = uniqueColumns;

        // go through the columns and find any that are unique; these we replace with a non-unique
        // variant, and instead add a new entry to the table unique constraint
        List<ColumnDefinition> newDefns = new ArrayList<ColumnDefinition>(defns.size());
        for (int ii = 0; ii < defns.size(); ii ++) {
            ColumnDefinition def = defns.get(ii);
            if (!def.unique) {
                newDefns.add(def);
                continue;
            }

            // let's be nice and not mutate the caller's object
            newDefns.add(
                new ColumnDefinition(def.type, def.nullable, false, def.defaultValue));
            // if a uniqueness constraint for this column was not in the primaryKeys or
            // uniqueColumns parameters, add the column to uniqueCsts
            if (!seenUniques.contains(columns.get(ii))) {
                if (newUniques == uniqueColumns) {
                    newUniques = new ArrayList<List<String>>(uniqueColumns);
                }
                newUniques.add(Collections.singletonList(columns.get(ii)));
            }
        }

        // now call the real implementation with our modified data
        return super.createTableIfMissing(conn, table, columns, newDefns, newUniques, pkColumns);
    }

    @Override // from DatabaseLiaison
    protected String expandDefinition (String type, boolean nullable, boolean unique,
                                       String defaultValue)
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

    @Override
    protected void log (String message) {
        // HSQL is generally used as a test database, so we don't generally want to hear a bunch of
        // spam about table creation, etc. every time we start up/run tests
        Log.log.debug(message);
    }
}
