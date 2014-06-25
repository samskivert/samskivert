//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.jdbc;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.samskivert.io.PersistenceException;
import com.samskivert.util.StringUtil;
import static com.samskivert.jdbc.Log.log;

/**
 * A repository for JDBC related utility functions.
 */
public class JDBCUtil
{
    /** Used for {@link #batchQuery}. */
    public interface BatchProcessor
    {
        /**
         * Called for each row returned during our batch query. Do not advance the result set,
         * simply query its values with the get methods.
         */
        public void process (ResultSet row)
            throws SQLException;
    }

    /**
     * Closes the supplied JDBC statement and gracefully handles being passed null (by doing
     * nothing).
     */
    public static void close (Statement stmt)
        throws SQLException
    {
        if (stmt != null) {
            stmt.close();
        }
    }

    /**
     * Closes the supplied JDBC connection and gracefully handles being passed null (by doing
     * nothing).
     */
    public static void close (Connection conn)
        throws SQLException
    {
        if (conn != null) {
            conn.close();
        }
    }

    /**
     * Wraps the given connection in a proxied instance that will add all statements returned by
     * methods called on the proxy (such as {@link Connection#createStatement}) to the supplied
     * list. Thus you can create the proxy, pass the proxy to code that creates and uses statements
     * and then close any statements created by the code that operated on that Connection before
     * returning it to a pool, for example.
     */
    public static Connection makeCollector (final Connection conn, final List<Statement> stmts)
    {
        return (Connection)Proxy.newProxyInstance(
            Connection.class.getClassLoader(), PROXY_IFACES, new InvocationHandler() {
            public Object invoke (Object proxy, Method method, Object[] args) throws Throwable {
                Object result = method.invoke(conn, args);
                if (result instanceof Statement) {
                    stmts.add((Statement)result);
                }
                return result;
            }
        });
    }

    /**
     * Calls <code>stmt.executeUpdate()</code> on the supplied statement, checking to see that it
     * returns the expected update count and throwing a persistence exception if it does not.
     */
    public static void checkedUpdate (PreparedStatement stmt, int expectedCount)
        throws SQLException, PersistenceException
    {
        int modified = stmt.executeUpdate();
        if (modified != expectedCount) {
            String err = "Statement did not modify expected number of rows [stmt=" + stmt +
                ", expected=" + expectedCount + ", modified=" + modified + "]";
            throw new PersistenceException(err);
        }
    }

    /**
     * Calls <code>stmt.executeUpdate()</code> on the supplied statement with the supplied query,
     * checking to see that it returns the expected update count and throwing a persistence
     * exception if it does not.
     */
    public static void checkedUpdate (
        Statement stmt, String query, int expectedCount)
        throws SQLException, PersistenceException
    {
        int modified = stmt.executeUpdate(query);
        if (modified != expectedCount) {
            String err = "Statement did not modify expected number of rows [stmt=" + stmt +
                ", expected=" + expectedCount + ", modified=" + modified + "]";
            throw new PersistenceException(err);
        }
    }

    /**
     * Calls <code>stmt.executeUpdate()</code> on the supplied statement, checking to see that it
     * returns the expected update count and logging a warning if it does not.
     */
    public static void warnedUpdate (PreparedStatement stmt, int expectedCount)
        throws SQLException
    {
        int modified = stmt.executeUpdate();
        if (modified != expectedCount) {
            log.warning("Statement did not modify expected number of rows", "stmt", stmt,
                        "expected", expectedCount, "modified", modified);
        }
    }

    /**
     * Issues a query with a potentially large number of keys in batches.  For example, you might
     * have 10,000 ids that you wish to use in an "in" clause, but don't trust the database to be
     * smart about optimizing that many keys, so instead you use batchQuery like so:
     * <pre>{@code
     *    Collection<Integer> keys = ...;
     *    String query = "select NAME from USERS where USER_ID in (#KEYS)";
     *    JDBCUtil.BatchProcessor proc = new JDBCUtil.BatchProcessor() {
     *        public void process (ResultSet row) {
     *            String name = rs.getString(1);
     *            // do whatever with name
     *        }
     *    };
     *    JDBCUtil.batchQuery(conn, query, keys, false, 500, proc);
     * }</pre>
     *
     * @param query the SQL query to run for each batch with the string <code>#KEYS#</code> in the
     * place where the batch of keys should be substituted.
     * @param escapeKeys if true, {@link #escape} will be called on each key to escape any
     * dangerous characters and wrap the key in quotes.
     * @param batchSize the number of keys at a time to substitute in for <code>#KEYS#</code>.
     */
    public static void batchQuery (Connection conn, String query, Collection<?> keys,
                                   boolean escapeKeys, int batchSize, BatchProcessor processor)
        throws SQLException
    {
        Statement stmt = conn.createStatement();
        try {
            Iterator<?> itr = keys.iterator();
            while (itr.hasNext()) {
                // group one batch of keys together
                StringBuilder buf = new StringBuilder();
                for (int ii = 0; ii < batchSize && itr.hasNext(); ii++) {
                    if (ii > 0) {
                        buf.append(",");
                    }
                    String key = String.valueOf(itr.next());
                    buf.append(escapeKeys ? escape(key) : key);
                }

                // issue the query with that batch
                String squery = query.replace("#KEYS#", buf.toString());
                ResultSet rs = stmt.executeQuery(squery);
                while (rs.next()) {
                    processor.process(rs);
                }
            }

        } finally {
            close(stmt);
        }
    }

    /**
     * Calls <code>stmt.executeUpdate()</code> on the supplied statement with the supplied query,
     * checking to see that it returns the expected update count and logging a warning if it does
     * not.
     */
    public static void warnedUpdate (
        Statement stmt, String query, int expectedCount)
        throws SQLException
    {
        int modified = stmt.executeUpdate(query);
        if (modified != expectedCount) {
            log.warning("Statement did not modify expected number of rows", "stmt", stmt,
                        "expected", expectedCount, "modified", modified);
        }
    }

    /**
     * Converts the date to a string and surrounds it in single-quotes via the escape method. If the
     * date is null, returns null.
     */
    public String quote (Date date)
    {
        return (date == null) ? null : escape(String.valueOf(date));
    }

    /**
     * Escapes any single quotes in the supplied text and wraps it in single quotes to make it safe
     * for embedding into a database query.
     */
    public static String escape (String text)
    {
        text = text.replace("\\", "\\\\");
        return "'" + text.replace("'", "\\'") + "'";
    }

    /**
     * Escapes a list of values, separating the escaped values by commas. See {@link
     * #escape(String)}.
     */
    public static String escape (Object[] values)
    {
        StringBuilder buf = new StringBuilder();
        for (int ii = 0; ii < values.length; ii++) {
            if (ii > 0) {
                buf.append(", ");
            }
            buf.append(escape(String.valueOf(values[ii])));
        }
        return buf.toString();
    }

    /**
     * Many databases simply fail to handle Unicode text properly and this routine provides a
     * common workaround which is to represent a UTF-8 string as an ISO-8859-1 string. If you don't
     * need to use the database's collation routines, this allows you to do pretty much exactly
     * what you want at the expense of having to jigger and dejigger every goddamned string that
     * might contain multibyte characters every time you access the database. Three cheers for
     * progress!
     */
    public static String jigger (String text)
    {
        if (text == null) {
            return null;
        }
        try {
            return new String(text.getBytes("UTF8"), "8859_1");
        } catch (UnsupportedEncodingException uee) {
            log.warning("Jigger failed", uee);
            return text;
        }
    }

    /**
     * Reverses {@link #jigger}.
     */
    public static String unjigger (String text)
    {
        if (text == null) {
            return null;
        }
        try {
            return new String(text.getBytes("8859_1"), "UTF8");
        } catch (UnsupportedEncodingException uee) {
            log.warning("Unjigger failed", uee);
            return text;
        }
    }

    /**
     * Utility method to jigger the specified string so that it's safe to use in a regular
     * Statement.
     */
    public static String safeJigger (String text)
    {
        return jigger(text).replace("'", "\\'");
    }

    /**
     * Used to programatically create a database table. Does nothing if the table already exists.
     *
     * @return true if the table was created, false if it already existed.
     */
    public static boolean createTableIfMissing (
        Connection conn, String table, String[] definition, String postamble)
        throws SQLException
    {
        if (tableExists(conn, table)) {
            return false;
        }

        Statement stmt = conn.createStatement();
        try {
            stmt.executeUpdate("create table " + table +
                               "(" + StringUtil.join(definition, ", ") + ") " + postamble);
        } finally {
            close(stmt);
        }

        log.info("Database table '" + table + "' created.");
        return true;
    }

    /**
     * Returns true if the table with the specified name exists, false if it does
     * not. <em>Note:</em> the table name is case sensitive.
     */
    public static boolean tableExists (Connection conn, String name)
        throws SQLException
    {
        boolean matched = false;
        ResultSet rs = conn.getMetaData().getTables("", "", name, null);
        while (rs.next()) {
            String tname = rs.getString("TABLE_NAME");
            if (name.equals(tname)) {
                matched = true;
            }
        }
        return matched;
    }

    /**
     * Returns true if the table with the specified name exists and contains a column with the
     * specified name, false if either condition does not hold true. <em>Note:</em> the names are
     * case sensitive.
     */
    public static boolean tableContainsColumn (Connection conn, String table, String column)
        throws SQLException
    {
        boolean matched = false;
        ResultSet rs = conn.getMetaData().getColumns("", "", table, column);
        while (rs.next()) {
            String tname = rs.getString("TABLE_NAME");
            String cname = rs.getString("COLUMN_NAME");
            if (tname.equals(table) && cname.equals(column)) {
                matched = true;
            }
        }
        return matched;
    }

    /**
     * Returns true if the index on the specified column exists for the specified table, false if
     * it does not. Optionally you can specifiy a non null index name, and the table will be
     * checked to see if it contains that specifically named index. <em>Note:</em> the names are
     * case sensitive.
     */
    public static boolean tableContainsIndex (
        Connection conn, String table, String column, String index)
        throws SQLException
    {
        boolean matched = false;
        ResultSet rs = conn.getMetaData().getIndexInfo("", "", table, false, true);
        while (rs.next()) {
            String tname = rs.getString("TABLE_NAME");
            String cname = rs.getString("COLUMN_NAME");
            String iname = rs.getString("INDEX_NAME");
            if (index == null) {
                if (tname.equals(table) && cname.equals(column)) {
                    matched = true;
                }
            } else if (index.equals(iname)) {
                matched = true;
            }
        }
        return matched;
    }

    /**
     * Returns true if the specified table contains a primary key on the specified column.
     */
    public static boolean tableContainsPrimaryKey (Connection conn, String table, String column)
        throws SQLException
    {
        boolean matched = false;
        ResultSet rs = conn.getMetaData().getPrimaryKeys("", "", table);
        while (rs.next()) {
            String tname = rs.getString("TABLE_NAME");
            String cname = rs.getString("COLUMN_NAME");
            if (tname.equals(table) && cname.equals(column)) {
                matched = true;
            }
        }
        return matched;
    }

    /**
     * Returns the name of the index for the specified column in the specified table.
     */
    public static String getIndexName (Connection conn, String table,
                                       String column)
        throws SQLException
    {
        ResultSet rs = conn.getMetaData().getIndexInfo("", "", table, false, true);
        while (rs.next()) {
            String tname = rs.getString("TABLE_NAME");
            String cname = rs.getString("COLUMN_NAME");
            String iname = rs.getString("INDEX_NAME");
            if (tname.equals(table) && cname.equals(column)) {
                return iname;
            }
        }
        return null;
    }

    /**
     * Returns the type (as specified in {@link java.sql.Types} for the specified column in the
     * specified table.
     */
    public static int getColumnType (Connection conn, String table,
                                     String column)
        throws SQLException
    {
        ResultSet rs = getColumnMetaData(conn, table, column);
        try {
            return rs.getInt("DATA_TYPE");
        } finally {
            rs.close();
        }
    }

    /**
     * Determines whether or not the specified column accepts null values.
     *
     * @return true if the column accepts null values, false if it does not (or its nullability is
     * unknown)
     */
    public static boolean isColumnNullable (Connection conn, String table,
                                            String column)
        throws SQLException
    {
        ResultSet rs = getColumnMetaData(conn, table, column);
        try {
            return rs.getString("IS_NULLABLE").equals("YES");
        } finally {
            rs.close();
        }
    }

    /**
     * Returns the size for the specified column in the specified table. For char or date types
     * this is the maximum number of characters, for numeric or decimal types this is the
     * precision.
     */
    public static int getColumnSize (Connection conn, String table, String column)
        throws SQLException
    {
        ResultSet rs = getColumnMetaData(conn, table, column);
        try {
            return rs.getInt("COLUMN_SIZE");
        } finally {
            rs.close();
        }
    }

    /**
     * Returns a string representation of the default value for the specified column in the
     * specified table. This may be null.
     */
    public static String getColumnDefaultValue (Connection conn, String table, String column)
        throws SQLException
    {
        ResultSet rs = getColumnMetaData(conn, table, column);
        try {
            return rs.getString("COLUMN_DEF");
        } finally {
            rs.close();
        }
    }

    /**
     * Adds a column (with name 'cname' and definition 'cdef') to the specified table.
     *
     * @param afterCname (optional) the name of the column after which to add the new column.
     *
     * @return true if the column was added, false if it already existed.
     */
    public static boolean addColumn (
        Connection conn, String table, String cname, String cdef, String afterCname)
        throws SQLException
    {
        if (tableContainsColumn(conn, table, cname)) {
//             Log.info("Database table '" + table + "' already has column '" + cname + "'.");
            return false;
        }

        String update = "ALTER TABLE " + table + " ADD COLUMN " + cname + " " + cdef;
        if (afterCname != null) {
            update += " AFTER " + afterCname;
        }
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(update);
            stmt.executeUpdate();
        } finally {
            close(stmt);
        }
        log.info("Database column '" + cname + "' added to table '" + table + "'.");
        return true;
    }

    /**
     * Changes a column's definition. Takes a full column definition 'cdef' (including the name of
     * the column) with which to replace the specified column 'cname'.
     *
     * NOTE: A handy thing you can do with this is to rename a column by providing a column
     * definition that has a different name, but the same column type.
     */
    public static void changeColumn (Connection conn, String table, String cname, String cdef)
        throws SQLException
    {
        String update = "ALTER TABLE " + table + " CHANGE " + cname + " " + cdef;
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(update);
            stmt.executeUpdate();
        } finally {
            close(stmt);
        }
        log.info("Database column '" + cname + "' of table '" + table +
                 "' modified to have this def '" + cdef + "'.");
    }

    /**
     * Removes a column from the specified table.
     *
     * @return true if the column was dropped, false if it did not exist in the first place.
     */
    public static boolean dropColumn (Connection conn, String table, String cname)
        throws SQLException
    {
        if (!tableContainsColumn(conn, table, cname)) {
            return false;
        }

        String update = "ALTER TABLE " + table + " DROP COLUMN " + cname;
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(update);
            if (stmt.executeUpdate() == 1) {
                log.info("Database index '" + cname + "' removed from table '" + table + "'.");
            }
        } finally {
            close(stmt);
        }
        return true;
    }

    /**
     * Removes a named index from the specified table.
     *
     * @return true if the index was dropped, false if it did not exist in the first place.
     */
    public static boolean dropIndex (Connection conn, String table, String cname, String iname)
        throws SQLException
    {
        if (!tableContainsIndex(conn, table, cname, iname)) {
            return false;
        }

        String update = "ALTER TABLE " + table + " DROP INDEX " + iname;
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(update);
            if (stmt.executeUpdate() == 1) {
                log.info("Database index '" + iname + "' removed from table '" + table + "'.");
            }
        } finally {
            close(stmt);
        }
        return true;
    }

    /**
     * Removes the primary key from the specified table.
     */
    public static void dropPrimaryKey (Connection conn, String table)
        throws SQLException
    {
        String update = "ALTER TABLE " + table + " DROP PRIMARY KEY";
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(update);
            if (stmt.executeUpdate() == 1) {
                log.info("Database primary key removed from '" + table + "'.");
            }
        } finally {
            close(stmt);
        }
    }

    /**
     * Adds an index on the specified column (cname) to the specified table.  Optionally supply an
     * index name, otherwise the index is named after the column.
     *
     * @return true if the index was added, false if it already existed.
     */
    public static boolean addIndexToTable (
        Connection conn, String table, String cname, String iname)
        throws SQLException
    {
        if (tableContainsIndex(conn, table, cname, iname)) {
//             Log.info("Database table '" + table + "' already has an index " +
//                      "on column '" + cname + "'" +
//                      (iname != null ? " named '" + iname + "'." : "."));
            return false;
        }

        String idx_name = (iname != null ? iname : cname);
        String update = "CREATE INDEX " + idx_name + " on " + table + "(" + cname + ")";
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement(update);
            stmt.executeUpdate();
        } finally {
            close(stmt);
        }
        log.info("Database index '" + idx_name + "' added to table '" + table + "'");
        return true;
    }

    /**
     * Helper function for {@link #getColumnType}, etc.
     */
    protected static ResultSet getColumnMetaData (Connection conn, String table, String column)
        throws SQLException
    {
        ResultSet rs = conn.getMetaData().getColumns("", "", table, column);
        while (rs.next()) {
            String tname = rs.getString("TABLE_NAME");
            String cname = rs.getString("COLUMN_NAME");
            if (tname.equals(table) && cname.equals(column)) {
                return rs;
            }
        }
        throw new SQLException("Table or Column not defined. [table=" + table +
                               ", col=" + column + "].");
    }

    /** Used by {@link #makeCollector}. */
    protected static final Class<?>[] PROXY_IFACES = { Connection.class };
}
