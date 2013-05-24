//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.jdbc;

import java.sql.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.samskivert.jdbc.Log.log;

/**
 * The liaison registry provides access to the appropriate database liaison implementation for a
 * particular database connection.
 */
public class LiaisonRegistry
{
    /**
     * Fetch the appropriate database liaison for the supplied URL, which should be the same string
     * that would be used to configure a connection to the database.
     */
    public static DatabaseLiaison getLiaison (String url)
    {
        if (url == null) throw new NullPointerException("URL must not be null");
        // see if we already have a liaison mapped for this connection
        DatabaseLiaison liaison = _mappings.get(url);
        if (liaison == null) {
            // scan the list looking for a matching liaison
            for (DatabaseLiaison candidate : _liaisons) {
                if (candidate.matchesURL(url)) {
                    liaison = candidate;
                    break;
                }
            }

            // if we didn't find a matching liaison, use the default
            if (liaison == null) {
                log.warning("Unable to match liaison for database. Using default.", "url", url);
                liaison = new DefaultLiaison();
            }

            // map this URL to this liaison
            _mappings.put(url, liaison);
        }

        return liaison;
    }

    /**
     * Fetch the appropriate database liaison for the supplied database connection.
     */
    public static DatabaseLiaison getLiaison (Connection conn)
        throws SQLException
    {
        return getLiaison(conn.getMetaData().getURL());
    }

    protected static void registerLiaisonClass (Class<? extends DatabaseLiaison> lclass)
    {
        // create a new instance and stick it on our list
        try {
            _liaisons.add(lclass.newInstance());
        } catch (Exception e) {
            log.warning("Unable to instantiate liaison", "class", lclass.getName(), "error", e);
        }
    }

    protected static ArrayList<DatabaseLiaison> _liaisons = new ArrayList<DatabaseLiaison>();
    protected static Map<String,DatabaseLiaison> _mappings = new HashMap<String,DatabaseLiaison>();

    // register our liaison classes
    static {
        registerLiaisonClass(MySQLLiaison.class);
        registerLiaisonClass(PostgreSQLLiaison.class);
        registerLiaisonClass(HsqldbLiaison.class);
    }
}
