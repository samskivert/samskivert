//
// $Id: TwoDueApp.java,v 1.2 2003/11/15 22:55:32 mdb Exp $

package com.samskivert.twodue;

import java.util.Properties;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import com.samskivert.io.PersistenceException;
import com.samskivert.jdbc.ConnectionProvider;
import com.samskivert.jdbc.StaticConnectionProvider;

import com.samskivert.servlet.JDBCTableSiteIdentifier;
import com.samskivert.servlet.SiteIdentifier;
import com.samskivert.servlet.user.UserManager;
import com.samskivert.util.ConfigUtil;
import com.samskivert.util.ServiceUnavailableException;
import com.samskivert.velocity.Application;

import com.samskivert.twodue.data.TaskRepository;

/**
 * Contains references to application-wide resources (like the database
 * repository) and handles initialization and cleanup for those resources.
 */
public class TwoDueApp extends Application
{
    /** Returns the connection provider in use by this application. */
    public final ConnectionProvider getConnectionProvider ()
    {
        return _conprov;
    }

    /** Returns the task repository in use by the application. */
    public TaskRepository getRepository ()
    {
        return _taskrep;
    }

    /** Returns the user manager in use by the application. */
    public UserManager getUserManager ()
    {
        return _usermgr;
    }

    protected void willInit (ServletConfig config)
    {
        super.willInit(config);

	try {
            // create a static connection provider
            _conprov = new StaticConnectionProvider(CONN_CONFIG);

            // load up our configuration properties
            Properties props = ConfigUtil.loadProperties("user.properties");
	    _usermgr = new UserManager(props, _conprov);

	    // initialize the task repository
	    _taskrep = new TaskRepository(_conprov);

	    Log.info("TwoDue application initialized.");

	} catch (Throwable t) {
	    Log.warning("Error initializing application: " + t);
	}
    }

    public void shutdown ()
    {
	try {
	    _usermgr.shutdown();
	    Log.info("TwoDue application shutdown.");

	} catch (Throwable t) {
	    Log.warning("Error shutting down repository: " + t);
	}
    }

    /** We want a special site identifier. */
    protected SiteIdentifier createSiteIdentifier (ServletContext ctx)
    {
        try {
            return new JDBCTableSiteIdentifier(_conprov);
        } catch (PersistenceException pe) {
            throw new ServiceUnavailableException(
                "Can't access site database.", pe);
        }
    }

    /** A reference to our connection provider. */
    protected ConnectionProvider _conprov;

    /** A reference to our user manager. */
    protected UserManager _usermgr;

    /** A reference to our task repository. */
    protected TaskRepository _taskrep;

    /** The path to our database configuration file. */
    protected static final String CONN_CONFIG = "repository.properties";
}
