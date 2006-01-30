//-< NoPrimaryKeyError.java >----------------------------------------*--------*
// JORA                       Version 2.0        (c) 1998  GARRET    *     ?  *
// (Java Object Relational Adapter)                                  *   /\|  *
//                                                                   *  /  \  *
//                          Created:     10-Jun-98    K.A. Knizhnik  * / [] \ *
//                          Last update: 16-Jun-98    K.A. Knizhnik  * GARRET *
//-------------------------------------------------------------------*--------*
// Exception raised when UPDATE/REMOVE operation is appllied to Table with
// no primary key
//-------------------------------------------------------------------*--------*

package com.samskivert.jdbc.jora;

/** Error raised when unpdate/remove operation is invoked for the table
 *  with no correct primary key defined (key was not specified or 
 *  type of the key component is not atomic).
 */
public class NoPrimaryKeyError extends java.lang.Error { 
    NoPrimaryKeyError(Table table) { 
        super("Table " + table.name + " has no atomic primary key");
    } 
}
