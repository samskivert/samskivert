//
// $Id: JORARepository.java,v 1.2 2004/02/25 13:16:05 mdb Exp $
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001 Michael Bayne
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

import java.sql.*;

import com.samskivert.io.PersistenceException;
import com.samskivert.jdbc.jora.*;

/**
 * The JORA repository simplifies the process of building persistence
 * services that make use of the JORA object relational mapping package.
 *
 * @see com.samskivert.jdbc.jora.Session
 */
public abstract class JORARepository extends SimpleRepository
{
    /**
     * Creates and initializes a JORA repository which will access the
     * database identified by the supplied database identifier.
     *
     * @param provider the connection provider which will be used to
     * obtain our database connection.
     * @param dbident the identifier of the database that will be accessed
     * by this repository.
     */
    public JORARepository (ConnectionProvider provider, String dbident)
    {
        super(provider, dbident);

        // create our JORA session
        _session = new Session((Connection)null);

        // create our tables
        createTables(_session);
    }

    /**
     * Updates the specified field in the supplied object (which must
     * correspond to the supplied table).
     */
    protected void updateField (
        final Table table, final Object object, String field)
        throws PersistenceException
    {
        final FieldMask mask = table.getFieldMask();
        mask.setModified(field);
        execute(new Operation() {
            public Object invoke (Connection conn, DatabaseLiaison liaison)
                throws SQLException, PersistenceException
            {
                table.update(object, mask);
                return null;
            }
        });
    }

    /**
     * Updates the specified fields in the supplied object (which must
     * correspond to the supplied table).
     */
    protected void updateFields (
        final Table table, final Object object, String[] fields)
        throws PersistenceException
    {
        final FieldMask mask = table.getFieldMask();
        for (int ii = 0; ii < fields.length; ii++) {
            mask.setModified(fields[ii]);
        }
        execute(new Operation() {
            public Object invoke (Connection conn, DatabaseLiaison liaison)
                throws SQLException, PersistenceException
            {
                table.update(object, mask);
                return null;
            }
        });
    }

    /**
     * After the database session is begun, this function will be called
     * to give the repository implementation the opportunity to create its
     * table objects.
     *
     * @param session the session instance to use when creating your table
     * instances.
     */
    protected abstract void createTables (Session session);

    protected void gotConnection (Connection conn)
    {
        // let our session know about this connection
        _session.setConnection(conn);
    }

    protected Session _session;
}
