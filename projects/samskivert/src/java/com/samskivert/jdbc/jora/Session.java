//-< Session.java >--------------------------------------------------*--------*
// JORA                       Version 2.0        (c) 1998  GARRET    *     ?  *
// (Java Object Relational Adapter)                                  *   /\|  *
//                                                                   *  /  \  *
//                          Created:     10-Jun-98    K.A. Knizhnik  * / [] \ *
//                          Last update: 20-Jun-98    K.A. Knizhnik  * GARRET *
//-------------------------------------------------------------------*--------*
// Database session abstraction  
//-------------------------------------------------------------------*--------*

package com.samskivert.jdbc.jora;

import java.util.*;
import java.sql.*;

import com.samskivert.Log;

/**
 * This class is reposnsible for establishing connection with database
 * and handling database errors.
 */
public class Session { 
    public Connection connection;

    /** Session consructor
     * 
     * @param driverClass class of database driver
     */
    public Session(String driverClass)
    {
        driver = driverClass;
        preparedStmtHash = new Hashtable();
	connectionID = 0;
    }

    /** Construct a session with a pre-existing connection instance. In
     * this case, {@link #open} should not be called on this session.
     *
     * @param connection the connection to use.
     */
    public Session(Connection connection)
    {
        connectionID = 0;
        preparedStmtHash = new Hashtable();
        setConnection(connection);
    }

    /** Session consructor for ODBC bridge driver
     */
    public Session() { this("sun.jdbc.odbc.JdbcOdbcDriver"); }
     
    /** Sets the connection that should be used by this session.
     */
    public void setConnection(Connection conn)
    {
        // only up the connection id if this is a new connection
        if (connection != conn) {
            // clear out our prepared statement hash because we've got a
            // new connection
	    Enumeration items = preparedStmtHash.elements();
	    while (items.hasMoreElements()) { 
                try {
                    ((PreparedStatement)items.nextElement()).close();
                } catch (SQLException sqe) {
                    Log.warning("Error closing cached prepared statement " +
                                "[error=" + sqe + "].");
                }
	    }
	    preparedStmtHash.clear();

            // switch to our new connection
            connection = conn;
            connectionID += 1;
        }
    }

    /** Handler of database session errors. Programmer should override
     *  this method in derived class in order to provide application
     *  dependent error handling.
     * 
     * @param ex exception thrown by some of JDBC methods
     */
    public void handleSQLException(SQLException ex) {
        // A SQLException was generated.  Catch it and
        // display the error information.  Note that there
   	// could be multiple error objects chained
	// together
	System.out.println ("*** SQLException caught ***");
	SQLException x = ex; 
	while (ex != null) {
	    System.out.println("SQLState: " + ex.getSQLState ());
	    System.out.println("Message:  " + ex.getMessage ());
	    System.out.println("Vendor:   " + ex.getErrorCode ());
	    ex = ex.getNextException();
	    System.out.println ("");
	}
	throw new SQLError(x); // terminate program execution
    }

    /** Open database session. 
     *  Attempt to establish a connection to the given database URL.
     *  The DriverManager attempts to select an appropriate driver from
     *  the set of registered JDBC drivers.
     *
     * @param dataSource a database url of the form 
     *  jdbc:<em>subprotocol</em>:<em>subname</em>
     * @param user the database user on whose behalf the Connection is
     * being made
     * @param password the user's password
     * @return true if session is succesfully openned, false otherwise
     */      
    public boolean open(String dataSource, String user, String password)
	throws SQLException
    {
        try {
	     Class.forName(driver);

	     connection = 
	       DriverManager.getConnection(dataSource, user, password);	
	     connectionID += 1;
	} 
//  	catch(SQLException ex) { 
//  	    handleSQLException(ex); 
//  	    return false;
//  	}
	catch(ClassNotFoundException ex) { 
	    return false;
	}
	return true;
    }

    /** Close database session and release all resources holded by session.
     */
    public void close()
	throws SQLException
    { 
//          try { 
	    Enumeration items = preparedStmtHash.elements();
	    while (items.hasMoreElements()) { 
	        ((PreparedStatement)items.nextElement()).close();
	    }
	    preparedStmtHash.clear();
            if (connection != null) {
                connection.close();
            }
//  	} 
//  	catch (SQLException ex) { handleSQLException(ex); }
    }
  
    /**
     * Execute a SQL INSERT, UPDATE or DELETE statement. In addition,
     * SQL statements that return nothing such as SQL DDL statements
     * can be executed.
     *
     * @param sql a SQL INSERT, UPDATE or DELETE statement or a SQL
     * statement that returns nothing
     * @return either the row count for INSERT, UPDATE or DELETE or 0
     * for SQL statements that return nothing
     */
    public int execute(String sql)
	throws SQLException
    {
//        try { 
	    Statement stmt = connection.createStatement();
	    int result = stmt.executeUpdate(sql);
	    stmt.close();
	    return result;
//	} catch(SQLException ex) { handleSQLException(ex); }
//	return -1;
    }

    /**
     * Commit makes all changes made since the previous
     * commit/rollback permanent and releases any database locks
     * currently held by the Connection. This method should only be
     * used when auto commit has been disabled.
     */
    public void commit()
	throws SQLException
    { 
//        try { 
	    connection.commit();
//	} catch(SQLException ex) { handleSQLException(ex); }
    }

    /**
     * Rollback drops all changes made since the previous
     * commit/rollback and releases any database locks currently held
     * by the Connection. This method should only be used when auto
     * commit has been disabled.
     */
    public void rollback()
	throws SQLException
    { 
//        try { 
	    connection.rollback();
//	} catch(SQLException ex) { handleSQLException(ex); }
    }

    protected String    driver;   // driver class name
    protected Hashtable preparedStmtHash;
    protected int       connectionID;
}
