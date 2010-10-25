//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2010 Michael Bayne, et al.
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
import java.sql.DriverManager;
import java.sql.ResultSet;

import org.junit.*;
import static org.junit.Assert.*;

/**
 * Tests the behavior of the {@link HsqldbLiaison}, which also sort of tests the {@link
 * DatabaseLiaison} since that's the only implementation we can easily use in unit tests.
 */
public class HsqldbLiaisonTest
{
    @BeforeClass
    public static void setUpJDBCDriver ()
        throws Exception
    {
        // create an instance of the driver
        Class.forName("org.hsqldb.jdbcDriver");
    }

    @Test
    public void testCreateTable ()
        throws Exception
    {
        invoke(new WithConnection() {
            public void execute (Connection c, DatabaseLiaison dl) throws Exception {
                String[] cols = new String[] { "col1", "col2", "col3" };
                ColumnDefinition[] defs = new ColumnDefinition[] {
                    new ColumnDefinition("int", false, false, null),
                    new ColumnDefinition("int", false, false, "0"),
                    new ColumnDefinition("varchar(255)", false, false, "''")
                };
                boolean created = dl.createTableIfMissing(
                    c, "test_table", cols, defs, null, new String[] { "col1" });
                assertTrue(created);

                ResultSet rs = c.getMetaData().getColumns(null, null, "test_table", "%");
                while (rs.next()) {
                    if ("col1".equals(rs.getString("COLUMN_NAME"))) {
                        assertEquals("INTEGER", rs.getString("TYPE_NAME"));
                        assertEquals("NO", rs.getString("IS_NULLABLE"));
                        assertEquals(null, rs.getString("COLUMN_DEF"));
                    } else if ("col2".equals(rs.getString("COLUMN_NAME"))) {
                        assertEquals("INTEGER", rs.getString("TYPE_NAME"));
                        assertEquals("NO", rs.getString("IS_NULLABLE"));
                        assertEquals("0", rs.getString("COLUMN_DEF"));
                    } else if ("col3".equals(rs.getString("COLUMN_NAME"))) {
                        assertEquals("VARCHAR", rs.getString("TYPE_NAME"));
                        assertEquals("255", rs.getString("COLUMN_SIZE"));
                        assertEquals("NO", rs.getString("IS_NULLABLE"));
                        assertEquals("''", rs.getString("COLUMN_DEF"));
                    }
                }
                rs.close();
            }
        });
    }

    protected void invoke (WithConnection wc)
        throws Exception
    {
        Connection c = null;
        try {
            c = DriverManager.getConnection("jdbc:hsqldb:mem:test", "test", "");
            DatabaseLiaison dl = LiaisonRegistry.getLiaison(c);
            wc.execute(c, dl);
        } finally {
            try {
                if (c != null) {
                    c.close();
                }
            } catch (Exception e) {
                System.err.println("Error closing HQSLDB: " + e);
            }
        }
    }

    protected interface WithConnection {
        void execute (Connection c, DatabaseLiaison dl) throws Exception;
    }
}
