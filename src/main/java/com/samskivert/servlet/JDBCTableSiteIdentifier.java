//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.servlet;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;

import com.samskivert.io.PersistenceException;

import com.samskivert.jdbc.ConnectionProvider;
import com.samskivert.jdbc.DatabaseLiaison;
import com.samskivert.jdbc.JDBCUtil;
import com.samskivert.jdbc.SimpleRepository;

import com.samskivert.util.ArrayUtil;
import com.samskivert.util.HashIntMap;

import static com.samskivert.servlet.Log.log;

/**
 * Accomplishes the process of site identification based on a mapping from domains (e.g.
 * samskivert.com) to site identifiers that is maintained in a database table, accessible via JDBC
 * (hence the name).
 *
 * <p> There are two tables, one that maps domains to site identifiers and another that maps site
 * identifiers to site strings. These are both loaded at construct time and refreshed periodically
 * in the course of normal operation.
 *
 * <p> Note that any of the calls to identify, lookup or enumerate site information can result in
 * the sites table being refreshed from the database which will take relatively much longer than
 * the simple hashtable lookup that the operations normally require. However, this happens only
 * once every 15 minutes and the circumstances in which the site identifier are normally used can
 * generally accomodate the extra 100 milliseconds or so that it is likely to take to reload the
 * (tiny) sites and domains tables from the database.
 */
public class JDBCTableSiteIdentifier implements SiteIdentifier
{
    /** The database identifier used to obtain a connection from our connection provider. The value
     * is <code>sitedb</code> which you'll probably need to know to provide the proper
     * configuration to your connection provider. */
    public static final String SITE_IDENTIFIER_IDENT = "sitedb";

    /**
     * Constructs a JDBC table site identifier with the supplied connection provider from which to
     * obtain its database connection.
     *
     * @see #SITE_IDENTIFIER_IDENT
     */
    public JDBCTableSiteIdentifier (ConnectionProvider conprov)
        throws PersistenceException
    {
        this(conprov, DEFAULT_SITE_ID);
    }

    /**
     * Creates an identifier that will load data from the supplied connection provider and which
     * will use the supplied default site id instead of {@link #DEFAULT_SITE_ID}.
     */
    public JDBCTableSiteIdentifier (ConnectionProvider conprov, int defaultSiteId)
        throws PersistenceException
    {
        _repo = new SiteIdentifierRepository(conprov);
        _repo.refreshSiteData();
        _defaultSiteId = defaultSiteId;
    }

    // documentation inherited
    public int identifySite (HttpServletRequest req)
    {
        checkReloadSites();
        String serverName = req.getServerName();

        // scan for the mapping that matches the specified domain
        int msize = _mappings.size();
        for (int i = 0; i < msize; i++) {
            SiteMapping mapping = _mappings.get(i);
            if (serverName.endsWith(mapping.domain)) {
                return mapping.siteId;
            }
        }

        // if we matched nothing, return the default id
        return _defaultSiteId;
    }

    // documentation inherited
    public String getSiteString (int siteId)
    {
        checkReloadSites();
        Site site = _sitesById.get(siteId);
        if (site == null) {
            site = _sitesById.get(_defaultSiteId);
        }
        return (site == null) ? DEFAULT_SITE_STRING : site.siteString;
    }

    // documentation inherited
    public int getSiteId (String siteString)
    {
        checkReloadSites();
        Site site = _sitesByString.get(siteString);
        return (site == null) ? _defaultSiteId : site.siteId;
    }

    // documentation inherited from interface
    public Iterator<Site> enumerateSites ()
    {
        checkReloadSites();
        return _sitesById.values().iterator();
    }

    /**
     * Insert a new site into the site table and into this mapping.
     */
    public Site insertNewSite (String siteString)
        throws PersistenceException
    {
        if (_sitesByString.containsKey(siteString)) {
            return null;
        }

        // add it to the db
        Site site = new Site();
        site.siteString = siteString;
        _repo.insertNewSite(site);

        // add it to our two mapping tables, taking care to avoid causing enumerateSites() to choke
        @SuppressWarnings("unchecked") HashMap<String,Site> newStrings =
            (HashMap<String,Site>)_sitesByString.clone();
        HashIntMap<Site> newIds = _sitesById.clone();
        newIds.put(site.siteId, site);
        newStrings.put(site.siteString, site);
        _sitesByString = newStrings;
        _sitesById = newIds;

        return site;
    }

    /**
     * Checks to see if we should reload our sites information from the sites table.
     */
    protected void checkReloadSites ()
    {
        long now = System.currentTimeMillis();
        boolean reload = false;
        synchronized (this) {
            reload = (now - _lastReload > RELOAD_INTERVAL);
            if (reload) {
                _lastReload = now;
            }
        }
        if (reload) {
            try {
                _repo.refreshSiteData();
            } catch (PersistenceException pe) {
                log.warning("Error refreshing site data.", pe);
            }
        }
    }

    /**
     * Used to load information from the site database.
     */
    protected class SiteIdentifierRepository extends SimpleRepository
        implements SimpleRepository.Operation<Object>
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
                String query = "select siteId, siteString from sites";
                ResultSet rs = stmt.executeQuery(query);
                HashIntMap<Site> sites = new HashIntMap<Site>();
                HashMap<String,Site> strings = new HashMap<String,Site>();
                while (rs.next()) {
                    Site site = new Site(rs.getInt(1), rs.getString(2));
                    sites.put(site.siteId, site);
                    strings.put(site.siteString, site);
                }
                _sitesById = sites;
                _sitesByString = strings;

                // now load up the domain mappings
                query = "select domain, siteId from domains";
                rs = stmt.executeQuery(query);
                ArrayList<SiteMapping> mappings = new ArrayList<SiteMapping>();
                while (rs.next()) {
                    mappings.add(new SiteMapping(rs.getInt(2), rs.getString(1)));
                }

                // sort the mappings in order of specificity
                Collections.sort(mappings, SiteMapping.BY_SPECIFICITY);
                _mappings = mappings;
//                 Log.info("Loaded site mappings " + StringUtil.toString(_mappings) + ".");

                // nothing to return
                return null;

            } finally {
                JDBCUtil.close(stmt);
            }
        }

        /**
         * Add a new site to the database.
         */
        public void insertNewSite (final Site site)
            throws PersistenceException
        {
            executeUpdate(new Operation<Object>() {
                public Object invoke (Connection conn, DatabaseLiaison liaison)
                    throws PersistenceException, SQLException
                {
                    PreparedStatement stmt = null;
                    try {
                        stmt = conn.prepareStatement("insert into sites (siteString) VALUES (?)");
                        stmt.setString(1, site.siteString);
                        if (1 != stmt.executeUpdate()) {
                            throw new PersistenceException("Not inserted " + site);
                        }
                        site.siteId = liaison.lastInsertedId(conn, stmt, "sites", "siteId");

                    } finally {
                        JDBCUtil.close(stmt);
                    }

                    return null;
                }
            });
        }
    }

    /**
     * Used to track domain to site identifier mappings.
     */
    protected static class SiteMapping
    {
        /**
         * Sorts site mappings from most specific (www.yahoo.com) to least specific (yahoo.com).
         */
        public static final Comparator<SiteMapping> BY_SPECIFICITY = new Comparator<SiteMapping>() {
            public int compare (SiteMapping one, SiteMapping two) {
                return one._rdomain.compareTo(two._rdomain);
            }
        };

        /** The domain to match. */
        public String domain;

        /** The site identifier for the associated domain. */
        public int siteId;

        public SiteMapping (int siteId, String domain) {
            this.siteId = siteId;
            this.domain = domain;
            byte[] bytes = domain.getBytes();
            ArrayUtil.reverse(bytes);
            _rdomain = new String(bytes);
        }

        @Override // from Object
        public String toString () {
            return "[" + domain + " => " + siteId + "]";
        }

        protected String _rdomain;
    }

    /** The repository through which we load up site identifier information. */
    protected SiteIdentifierRepository _repo;

    /** The site id to return if we cannot identify the site from our table data. */
    protected int _defaultSiteId;

    /** The list of domain to site identifier mappings ordered from most specific domain to least
     * specific. */
    protected volatile ArrayList<SiteMapping> _mappings = new ArrayList<SiteMapping>();

    /** The mapping from integer site identifiers to string site identifiers. */
    protected volatile HashIntMap<Site> _sitesById = new HashIntMap<Site>();

    /** The mapping from string site identifiers to integer site identifiers. */
    protected volatile HashMap<String,Site> _sitesByString = new HashMap<String,Site>();

    /** Used to periodically reload our site data. */
    protected long _lastReload;

    /** Reload our site data every 15 minutes. */
    protected static final long RELOAD_INTERVAL = 15 * 60 * 1000L;
}
