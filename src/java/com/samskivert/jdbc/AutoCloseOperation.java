//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2009 Michael Bayne
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.samskivert.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.List;

import com.samskivert.io.PersistenceException;

/**
 * An {@link Repository.Operation} wrapper that automatically closes all statements opened by the
 * operation.
 *
 * @author Charlie Groves
 */
public abstract class AutoCloseOperation<V>
    implements Repository.Operation<V>
{
    /**
     * See {@link Repository.Operation#invoke}. Any Statement or PreparedStatement objects created
     * during the invocation of this method will automatically be closed.
     */
    public abstract V cleanInvoke (Connection conn, DatabaseLiaison liason)
        throws SQLException, PersistenceException;

    /**
     * Invokes {@link #cleanInvoke} and then closes any statements opened by that call.
     */
    public final V invoke (Connection conn, DatabaseLiaison liaison)
        throws SQLException, PersistenceException
    {
        List<Statement> stmts = new ArrayList<Statement>(1);
        conn = JDBCUtil.makeCollector(conn, stmts);
        try {
            return cleanInvoke(conn, liaison);
        } finally {
            // if closing a statement throws an SQLException, we don't attempt to close the rest of
            // the statements -- an SQLException thrown on close is a problem with the underlying
            // connection and the SimpleRepository closes the connection when it gets an exception
            // like that; the resources for the remaining statements will be collected in that case
            for (Statement stmt : stmts) {
                JDBCUtil.close(stmt);
            }
        }
    }
}
