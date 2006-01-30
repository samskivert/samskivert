//-< SQLError.java >-------------------------------------------------*--------*
// JORA                       Version 2.0        (c) 1998  GARRET    *     ?  *
// (Java Object Relational Adapter)                                  *   /\|  *
//                                                                   *  /  \  *
//                          Created:     10-Jun-98    K.A. Knizhnik  * / [] \ *
//                          Last update: 16-Jun-98    K.A. Knizhnik  * GARRET *
//-------------------------------------------------------------------*--------*
// Database error
//-------------------------------------------------------------------*--------*

package com.samskivert.jdbc.jora;

/** Database error. Exception SQLException was catched by JORA. 
 */
public class SQLError extends java.lang.RuntimeException { 
    java.sql.SQLException ex;
    SQLError(java.sql.SQLException x) {
        super("Database session aborted due to critical error");
	ex = x;
    } 
}
