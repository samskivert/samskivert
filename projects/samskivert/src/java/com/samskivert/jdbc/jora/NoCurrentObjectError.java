//-< NoCurrentObjectError.java >-------------------------------------*--------*
// JORA                       Version 2.0        (c) 1998  GARRET    *     ?  *
// (Java Object Relational Adapter)                                  *   /\|  *
//                                                                   *  /  \  *
//                          Created:     10-Jun-98    K.A. Knizhnik  * / [] \ *
//                          Last update: 16-Jun-98    K.A. Knizhnik  * GARRET *
//-------------------------------------------------------------------*--------*
// Exception raised when UPDATE/REMOVE operation is applied to Cursor
// with no current object
//-------------------------------------------------------------------*--------*

package com.samskivert.jdbc.jora;

/** Error raised when update/remove operation is applied to 
 *  cursor with no current object (before first call of <TT>next()</TT>
 *  method).
 */
public class NoCurrentObjectError extends java.lang.Error { 
    NoCurrentObjectError() { 
       super("Cursor doesn't specify current object");
    } 
}

