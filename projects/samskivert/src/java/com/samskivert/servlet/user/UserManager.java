//
// $Id: UserManager.java,v 1.1 2001/03/02 01:21:06 mdb Exp $

package com.samskivert.servlet.user;

import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.Properties;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpUtils;

import com.samskivert.Log;
import com.samskivert.servlet.RedirectException;
import com.samskivert.util.Interval;
import com.samskivert.util.IntervalManager;
import com.samskivert.util.StringUtil;

/**
 * The user manager provides easy access to user objects for servlets. It
 * takes care of cookie management involved in login, logout and loading a
 * user record during an authenticated session.
 */
public class UserManager
{
    /**
     * A user manager creates a user repository through which to load and
     * save user records. The properties needed to configure the user
     * repository must be provided to the user manager at construct time.
     *
     * <p> Presently the user manager requires the following configuration
     * information:
     * <ul>
     * <li><code>login_url</code>: Should be set to the URL to which to
     * redirect a requester if they are required to login before accessing
     * the requested page. For example:
     *
     * <pre>
     * login_url = /usermgmt/login.ajsp?return=%R
     * </pre>
     *
     * The <code>%R</code> will be replaced with the URL encoded URL the
     * user is currently requesting (complete with query parameters) so
     * that the login code can redirect the user back to this request once
     * they are authenticated.
     * </ul>
     *
     * @see UserRepository#UserRepository
     */
    public UserManager (Properties props)
	throws SQLException
    {
	// open up the user repository
	_repository = new UserRepository(props);

	// fetch the login URL from the properties
	_loginURL = props.getProperty("login_url");
	if (_loginURL == null) {
	    Log.warning("No login_url supplied in user manager config. " +
			"Authentication won't work.");
	}

	// register a cron job to prune the session table every hour
	Interval pruner = new Interval() {
	    public void intervalExpired (int id, Object arg)
	    {
		try {
		    _repository.pruneSessions();
		} catch (SQLException sqe) {
		    Log.warning("Error pruning session table: " + sqe);
		}
	    }
	};
	_prunerid = IntervalManager.register(pruner, SESSION_PRUNE_INTERVAL,
					     null, true);
    }

    public void shutdown ()
    {
	// shut down the user repository
	try {
	    _repository.shutdown();
	} catch (SQLException sqe) {
	    Log.warning("Error shutting down user repository: " + sqe);
	}

	// cancel our session table pruning thread
	IntervalManager.remove(_prunerid);
    }

    /**
     * Fetches the necessary authentication information from the http
     * request and loads the user identified by that information.
     *
     * @return the user associated with the request or null if no user was
     * associated with the request or if the authentication information is
     * bogus.
     */
    public User loadUser (HttpServletRequest req)
	throws SQLException
    {
	String authcode = getUserAuthCookie(req);
	if (authcode != null) {
	    return _repository.getUserBySession(authcode);
	} else {
	    return null;
	}
    }

    /**
     * Fetches the necessary authentication information from the http
     * request and loads the user identified by that information. If no
     * user could be loaded (because the requester is not authenticated),
     * a redirect exception will be thrown to redirect the user to the
     * login page specified in the user manager configuration.
     *
     * @return the user associated with the request.
     */
    public User requireUser (HttpServletRequest req)
	throws SQLException, RedirectException
    {
	User user = loadUser(req);
	// if no user was loaded, we need to redirect these fine people to
	// the login page
	if (user == null) {
	    // first construct the redirect URL
	    StringBuffer rurl = HttpUtils.getRequestURL(req);
	    String qs = req.getQueryString();
	    if (qs != null) {
		rurl.append("?").append(qs);
	    }
	    String eurl = URLEncoder.encode(rurl.toString());
	    String target = StringUtil.replace(_loginURL, "%R", eurl);
	    throw new RedirectException(target);
	}
	return user;
    }

    protected static String getUserAuthCookie (HttpServletRequest req)
    {
	Cookie[] cookies = req.getCookies();
	if (cookies == null) {
	    return null;
	}
	for (int i = 0; i < cookies.length; i++) {
	    if (cookies[i].getName().equals(USERAUTH_COOKIE)) {
		return cookies[i].getValue();
	    }
	}
	return null;
    }

    protected UserRepository _repository;
    protected int _prunerid = -1;

    protected String _loginURL;

    protected static final String USERAUTH_COOKIE = "id_";

    /** Prune the session table every hour. */
    protected static final long SESSION_PRUNE_INTERVAL = 60L * 60L * 1000L;
}
