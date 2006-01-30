//-< DataTransferError.java >----------------------------------------*--------*
// JORA                       Version 2.0        (c) 1998  GARRET    *     ?  *
// (Java Object Relational Adapter)                                  *   /\|  *
//                                                                   *  /  \  *
//                          Created:     10-Jun-98    K.A. Knizhnik  * / [] \ *
//                          Last update: 19-Jun-98    K.A. Knizhnik  * GARRET *
//-------------------------------------------------------------------*--------*
// Exception raised when error is happed during data transfer between 
// program and database server 
//-------------------------------------------------------------------*--------*

package com.samskivert.jdbc.jora;

/** This error is raised when error is happened during data transfer 
 *  between program and database server (for example IOException was thrown 
 *  while operation with InputStream field)
 */ 
public class DataTransferError extends java.lang.Error { 
    DataTransferError() { 
        super("Database data transfer error");
    } 
    DataTransferError(Exception ex) { 
        super(ex.getMessage());
    } 
}

