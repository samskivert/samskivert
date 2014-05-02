//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.jdbc;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Despite good intentions, JDBC and SQL do not provide a unified interface to all databases. There
 * remain idiosyncrasies that must be worked around when making code interact with different
 * database servers. The database liaison encapsulates the code needed to straighten out the curves
 * and curve out the straights.
 */
public interface DatabaseLiaison
{
    /**
     * Indicates whether or not this database liaison is the proper liaison for the specified
     * database URL.
     *
     * @return true if we should use this liaison for connections created with the supplied URL,
     * false if we should not.
     */
    public boolean matchesURL (String url);

    /**
     * Determines whether or not the supplied SQL exception was caused by a duplicate row being
     * inserted into a table with a unique key.
     *
     * @return true if the exception was caused by the insertion of a duplicate row, false if not.
     */
    public boolean isDuplicateRowException (SQLException sqe);

    /**
     * Determines whether or not the supplied SQL exception is a transient failure, meaning one
     * that is not related to the SQL being executed, but instead to the environment at the time of
     * execution, like the connection to the database having been lost.
     *
     * @return true if the exception was thrown due to a transient failure, false if not.
     */
    public boolean isTransientException (SQLException sqe);

    /** @deprecated Use version that takes the insert statement. */
    @Deprecated
    public int lastInsertedId (Connection conn, String table, String column) throws SQLException;

    /**
     * Attempts as dialect-agnostic an interface as possible to the ability of certain databases to
     * auto-generated numerical values for i.e. key columns; there is MySQL's AUTO_INCREMENT and
     * PostgreSQL's DEFAULT nextval(sequence), for example.
     *
     * @param istmt the insert statement that generated the keys. May be null if the ORM doesn't
     * have the statement handy.
     * @return the requested inserted id.
     * @throws SQLException if we are unable to obtain the last inserted id.
     */
    public int lastInsertedId (Connection conn, Statement istmt, String table, String column)
        throws SQLException;

    /**
     * Initializes the column value auto-generator described in {@link #lastInsertedId}. This
     * should be idempotent (meaning the generator may already exist in which case this method
     * should have no negative effect like resetting it).
     */
    public void createGenerator (Connection conn, String tableName, String columnName,
                                 int initialValue)
        throws SQLException;

    /**
     * Deletes the column value auto-generator described in {@link #lastInsertedId}.
     */
    public void deleteGenerator (Connection conn, String tableName, String columnName)
        throws SQLException;

    /**
     * Drops the given column from the given table. Returns true or false if the database did or
     * did not report a schema modification.
     */
    public boolean dropColumn (Connection conn, String table, String column)
        throws SQLException;

    /**
     * Adds a named index to a table on the given columns. Returns true or false if the database
     * did or did not report a schema modification.
     */
    public boolean addIndexToTable (Connection conn, String table, List<String> columns,
                                    String index, boolean unique)
        throws SQLException;

    /**
     * Drops the named index from the given table.
     */
    public void dropIndex (Connection conn, String table, String index)
        throws SQLException;

    /**
     * Adds a primary key to a table of the given name and on the given columns. Returns true or
     * false if the database did nor did not report a schema modification.
     */
    public void addPrimaryKey (Connection conn, String table, List<String> columns)
        throws SQLException;

    /**
     * Deletes the primary key from a table, if it exists.
     */
    public void dropPrimaryKey (Connection conn, String table, String pkName)
        throws SQLException;

    /**
     * Adds a column to a table with the given definition. Tests for the previous existence of
     * the column iff 'check' is true.
     */
    public boolean addColumn (Connection conn, String table, String column, String definition,
                              boolean check)
        throws SQLException;

    /**
     * Adds a column to a table with the given definition. Tests for the previous existence of
     * the column iff 'check' is true.
     */
    public boolean addColumn (Connection conn, String table, String column,
                              ColumnDefinition columnDef, boolean check)
        throws SQLException;

    /**
     * Alter the definition, but not the name, of a given column on a given table. Returns true or
     * false if the database did or did not report a schema modification. Any of the nullable,
     * unique and defaultValue arguments may be null, in which case the implementation will attempt
     * to leave that aspect of the column unchanged.
     */
    public boolean changeColumn (Connection conn, String table, String column, String type,
                                 Boolean nullable, Boolean unique, String defaultValue)
            throws SQLException;

    /**
     * Alter the name, but not the definition, of a given column on a given table. Returns true or
     * false if the database did or did not report a schema modification.
     *
     * @param columnDef the full definition of the new column, including its new name (MySQL
     * requires this for a column rename).
     */
    public boolean renameColumn (Connection conn, String table, String oldColumn, String newColumn,
                                 ColumnDefinition columnDef)
        throws SQLException;

    /**
     * Created a new table of the given name with the given column names and column definitions;
     * the given set of unique constraints (or null) and the given primary key columns (or null).
     * Returns true if the table was successfully created, false if it already existed.
     */
    public boolean createTableIfMissing (Connection conn, String table, List<String> columns,
                                         List<ColumnDefinition> declarations,
                                         List<String> primaryKeyColumns)
        throws SQLException;

    /**
     * Created a new table of the given name with the given column names and column definitions;
     * the given set of unique constraints (or null) and the given primary key columns (or null).
     * Returns true if the table was successfully created, false if it already existed.
     */
    public boolean createTableIfMissing (Connection conn, String table, List<String> columns,
                                         List<ColumnDefinition> declarations,
                                         List<List<String>> uniqueConstraintColumns,
                                         List<String> primaryKeyColumns)
        throws SQLException;

    /**
     * Create an SQL string that summarizes a column definition in that format generally accepted
     * in table creation and column addition statements, e.g. {@code INTEGER UNIQUE NOT NULL
     * DEFAULT 100}
     */
    public String expandDefinition (ColumnDefinition coldef);

    /**
     * Returns true if the specified table exists and contains an index of the specified name;
     * false if either conditions does not hold true. <em>Note:</em> the names are case sensitive.
     */
    public boolean tableContainsIndex (Connection conn, String table, String index)
        throws SQLException;

    /**
     * Returns true if the specified table exists and contains a column with the specified name;
     * false if either condition does not hold true. <em>Note:</em> the names are case sensitive.
     */
    public boolean tableContainsColumn (Connection conn, String table, String column)
        throws SQLException;

    /**
     * Returns true if the table with the specified name exists, false if it does not.
     * <em>Note:</em> the table name is case sensitive.
     */
    public boolean tableExists (Connection conn, String name)
        throws SQLException;

    /**
     * Drops the given table and returns true if the table exists, else returns false.
     * <em>Note:</em> the table name is case sensitive.
     */
    public boolean dropTable (Connection conn, String name)
        throws SQLException;

    /**
     * Returns the proper SQL to identify a table. Some databases require table names to be quoted.
     */
    public String tableSQL (String table);

    /**
     * Returns the proper SQL to identify a column. Some databases require columns names to be
     * quoted.
     */
    public String columnSQL (String column);

    /**
     * Returns the proper SQL to identify an index. Some databases require index names to be
     * quoted.
     */
    public String indexSQL (String index);
}
