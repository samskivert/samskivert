//
// $Id: JDBCTableSiteIdentifier.java,v 1.3 2003/07/04 20:34:34 mdb Exp $
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

package com.samskivert.servlet;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Collections;
import javax.servlet.http.HttpServletRequest;
import com.samskivert.io.PersistenceException;

import com.samskivert.jdbc.ConnectionProvider;
import com.samskivert.jdbc.DatabaseLiaison;
import com.samskivert.jdbc.JDBCUtil;
import com.samskivert.jdbc.SimpleRepository;

import com.samskivert.util.ArrayUtil;
import com.samskivert.util.HashIntMap;
import com.samskivert.util.StringUtil;

import com.samskivert.Log;

/**
 * Accomplishes the process of site identification based on a mapping from
 * domains (e.g. samskivert.com) to site identifiers that is maintained in
 * a database table, accessible via JDBC (hence the name).
 *
 * <p> There are two tables, one that maps domains to site identifiers and
 * another that maps site identifiers to site strings. These are both
 * loaded at construct time and refreshed periodically in the course of
 * normal operation.
 */
public class JDBCTableSiteIdentifier implements SiteIdentifier
{
    /** The database identifier used to obtain a connection from our
     * connection provider. The value is <code>sitedb</code> which you'll
     * probably need to know to provide the proper configuration to your
     * connection provider. */
    public static final String SITE_IDENTIFIER_IDENT = "sitedb";

    /**
     * Constructs a JDBC table site identifier with the supplied
     * connection provider from which to obtain its database connection.
     *
     * @see #SITE_IDENTIFIER_IDENT
     */
    public JDBCTableSiteIdentifier (ConnectionProvider conprov)
        throws PersistenceException
    {
        _repo = new SiteIdentifierRepository(conprov);
        // load up our site data
        _repo.refreshSiteData();
    }

    // documentation inherited
    public int identifySite (HttpServletRequest req)
    {
        String serverName = req.getServerName();

        // scan for the mapping that matches the specified domain
        int msize = _mappings.size();
        for (int i = 0; i < msize; i++) {
            SiteMapping mapping = (SiteMapping)_mappings.get(i);
            if (serverName.endsWith(mapping.domain)) {
                return mapping.siteId;
            }
        }

        // if we matched nothing, return the default id
        return DEFAULT_SITE_ID;
    }

    // documentation inherited
    public String getSiteString (int siteId)
    {
        String stringId = (String)_sites.get(siteId);
        return (stringId == null) ? DEFAULT_SITE_STRING : stringId;
    }

    /**
     * Used to load information from the site database.
     */
    protected class SiteIdentifierRepository
        extends SimpleRepository implements SimpleRepository.Operation
    {
        public SiteIdentifierRepository (ConnectionProvider conprov)
        {
            super(conprov, SITE_IDENTIFIER_IDENT);
        }

        public void refreshSiteData ()
            throws PersistenceException
        {
            // we are the operation!
            execute(this);
        }

        public Object invoke (Connection conn, DatabaseLiaison liaison)
            throws PersistenceException, SQLException
        {
            Statement stmt = conn.createStatement();
            try {
                // first load up the list of sites
                String query = "select siteId, stringId from sites";
                ResultSet rs = stmt.executeQuery(query);
                HashIntMap sites = new HashIntMap();
                while (rs.next()) {
                    sites.put(rs.getInt(1), rs.getString(2));
                }
                _sites = sites;

                // now load up the domain mappings
                query = "select domain, siteId from domains";
                rs = stmt.executeQuery(query);
                ArrayList mappings = new ArrayList();
                while (rs.next()) {
                    mappings.add(new SiteMapping(rs.getInt(2),
                                                 rs.getString(1)));
                }
                _mappings = mappings;

                // sort the mappings in order of specificity
                Collections.sort(_mappings);
//                 Log.info("Loaded site mappings " +
//                          StringUtil.toString(_mappings) + ".");

                // nothing to return
                return null;

            } finally {
                JDBCUtil.close(stmt);
            }
        }
    }

    /**
     * Used to track domain to site identifier mappings.
     */
    protected static class SiteMapping implements Comparable
    {
        /** The domain to match. */
        public String domain;

        /** The site identifier for the associated domain. */
        public int siteId;

        public SiteMapping (int siteId, String domain)
        {
            this.siteId = siteId;
            this.domain = domain;
            byte[] bytes = domain.getBytes();
            ArrayUtil.reverse(bytes);
            _rdomain = new String(bytes);
        }

        /**
         * Site mappings sort from most specific (www.yahoo.com) to least
         * specific (yahoo.com).
         */
        public int compareTo (Object other)
        {
            if (other instanceof SiteMapping) {
                SiteMapping orec = (SiteMapping)other;
                return orec._rdomain.compareTo(_rdomain);
            } else {
                // no comparablo
                return getClass().getName().compareTo(
                    other.getClass().getName());
            }
        }

        /** Returns a string representation of this site mapping. */
        public String toString ()
        {
            return "[" + domain + " => " + siteId + "]";
        }

        protected String _rdomain;
    }

    /** The repository through which we load up site identifier
     * information. */
    protected SiteIdentifierRepository _repo;

    /** The list of domain to site identifier mappings ordered from most
     * specific domain to least specific. */
    protected ArrayList _mappings = new ArrayList();

    /** The mapping from integer site identifiers to string site
     * identifiers. */
    protected HashIntMap _sites = new HashIntMap();
}
