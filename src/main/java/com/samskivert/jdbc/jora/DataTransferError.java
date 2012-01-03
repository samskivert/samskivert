//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

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
        super(ex);
    }
}
