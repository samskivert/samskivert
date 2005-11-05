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

        // create our tables
        createTables(_session);
    }

    /**
     * Inserts the supplied object into the specified table. The table
     * must be configured to store items of the supplied type.
     */
    protected int insert (final Table table, final Object object)
        throws PersistenceException
    {
        Integer iid = (Integer)execute(new Operation() {
            public Object invoke (Connection conn, DatabaseLiaison liaison)
                throws SQLException, PersistenceException
            {
                table.insert(object);
                return new Integer(liaison.lastInsertedId(conn));
            }
        });
        return iid.intValue();
    }

    /**
     * Updates the supplied object in the specified table. The table must
     * be configured to store items of the supplied type.
     */
    protected void update (final Table table, final Object object)
        throws PersistenceException
    {
        execute(new Operation() {
            public Object invoke (Connection conn, DatabaseLiaison liaison)
                throws SQLException, PersistenceException
            {
                table.update(object);
                return null;
            }
        });
    }

    /**
     * Loads a single object from the specified table that matches the
     * supplied query. <em>Note:</em> the query should match one or zero
     * records, not more.
     */
    protected Object load (final Table table, final String query)
        throws PersistenceException
    {
        return execute(new Operation() {
            public Object invoke (Connection conn, DatabaseLiaison liaison)
                throws SQLException, PersistenceException
            {
                return table.select(query).next();
            }
        });
    }

    /**
     * Loads a single object from the specified table that matches the
     * supplied example. <em>Note:</em> the query should match one or zero
     * records, not more.
     */
    protected Object loadByExample (final Table table, final Object example)
        throws PersistenceException
    {
        return execute(new Operation() {
            public Object invoke (Connection conn, DatabaseLiaison liaison)
                throws SQLException, PersistenceException
            {
                return table.queryByExample(example).next();
            }
        });
    }

    /**
     * Loads a single object from the specified table that matches the
     * supplied example. <em>Note:</em> the query should match one or zero
     * records, not more.
     */
    protected Object loadByExample (
        final Table table, final FieldMask mask, final Object example)
        throws PersistenceException
    {
        return execute(new Operation() {
            public Object invoke (Connection conn, DatabaseLiaison liaison)
                throws SQLException, PersistenceException
            {
                return table.queryByExample(example, mask).next();
            }
        });
    }

    /**
     * First attempts to update the supplied object and if that modifies
     * zero rows, inserts the object into the specified table. The table
     * must be configured to store items of the supplied type.
     */
    protected void store (final Table table, final Object object)
        throws PersistenceException
    {
        execute(new Operation() {
            public Object invoke (Connection conn, DatabaseLiaison liaison)
                throws SQLException, PersistenceException
            {
                if (table.update(object) == 0) {
                    table.insert(object);
                }
                return null;
            }
        });
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

    protected void delete (final Table table, final Object object)
        throws PersistenceException
    {
        execute(new Operation() {
            public Object invoke (Connection conn, DatabaseLiaison liaison)
                throws SQLException, PersistenceException
            {
                table.delete(object);
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
        // create or update our JORA session
        if (_session == null) {
            _session = new Session(conn);
        } else {
            _session.setConnection(conn);
        }
    }

    protected Session _session;
}
