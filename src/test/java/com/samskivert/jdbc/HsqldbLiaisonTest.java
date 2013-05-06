//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
                boolean created = dl.createTableIfMissing(
                    c, "test_table",
                    Arrays.asList("col1", "col2", "col3"),
                    Arrays.asList(new ColumnDefinition("int", false, false, null),
                                  new ColumnDefinition("int", false, false, "0"),
                                  new ColumnDefinition("varchar(255)", false, false, "''")),
                    Collections.<List<String>>emptyList(),
                    Arrays.asList("col1"));
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
