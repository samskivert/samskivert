//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.jdbc;

import java.sql.SQLException;

import org.junit.*;
import static org.junit.Assert.*;

/**
 * Tests which errors the {@link MySQLLiaison} treats as transient, as Connector/J reports them.
 */
public class MySQLLiaisonTest
{
    @Test
    public void testIdleDisconnectIsTransient ()
    {
        // Connector/J 8+ against MySQL 8.0.24+, on a connection idle past wait_timeout
        assertTrue(_liaison.isTransientException(new SQLException(
            "The client was disconnected by the server because of inactivity. See wait_timeout " +
            "and interactive_timeout for configuring this behavior.", "08S01", 0)));
    }

    @Test
    public void testLostLinksAreTransient ()
    {
        assertTrue(_liaison.isTransientException(new SQLException(
            "Communications link failure\n\nThe last packet successfully received from the " +
            "server was 28,800,123 milliseconds ago.", "08S01", 0)));
        assertTrue(_liaison.isTransientException(new SQLException(
            "Lost connection to MySQL server during query", "HY000", 2013)));
    }

    @Test
    public void testOtherErrorsAreNot ()
    {
        assertFalse(_liaison.isTransientException(new SQLException(
            "Duplicate entry '1' for key 'PRIMARY'", "23000", 1062)));
        assertFalse(_liaison.isTransientException(new SQLException(null, "08S01", 0)));
    }

    protected MySQLLiaison _liaison = new MySQLLiaison();
}
