//
// $Id: UserManager.java,v 1.11 2002/05/01 01:50:05 shaper Exp $
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

package com.samskivert.servlet.user;

import java.net.URLEncoder;
import java.util.Properties;
import javax.servlet.http.*;

import com.samskivert.Log;
import com.samskivert.io.PersistenceException;
import com.samskivert.jdbc.ConnectionProvider;
import com.samskivert.servlet.RedirectException;
import com.samskivert.servlet.util.RequestUtils;
import com.samskivert.util.*;

/**
 * The user manager provides easy access to user objects for servlets. It
 * takes care of cookie management involved in login, logout and loading a
 * user record during an authenticated session.
 */
public class UserManager
{
    /**
     * A user manager must be supplied with a {@link UserRepository}
     * through which it loads and saves user records.
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
     * @param config the user manager configuration properties.
     * @param repository the user repository through which user records
     * are loaded and saved.
     */
    public UserManager (Properties config, UserRepository repository)
	throws PersistenceException
    {
	// save off the user repository
	_repository = repository;

	// fetch the login URL from the properties
	_loginURL = config.getProperty("login_url");
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
		} catch (PersistenceException pe) {
		    Log.warning("Error pruning session table: " + pe);
		}
	    }
	};
	_prunerid = IntervalManager.register(pruner, SESSION_PRUNE_INTERVAL,
					     null, true);
    }

    public void shutdown ()
    {
	// cancel our session table pruning thread
	IntervalManager.remove(_prunerid);
    }

    /**
     * Returns a reference to the repository in use by this user manager.
     */
    public UserRepository getRepository ()
    {
	return _repository;
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
	throws PersistenceException
    {
	String authcode = getAuthCode(req);
	if (authcode != null) {
	    return _repository.loadUserBySession(authcode);
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
	throws PersistenceException, RedirectException
    {
	User user = loadUser(req);
	// if no user was loaded, we need to redirect these fine people to
	// the login page
	if (user == null) {
	    // first construct the redirect URL
            String eurl = RequestUtils.getLocationEncoded(req);
	    String target = StringUtil.replace(_loginURL, "%R", eurl);
	    throw new RedirectException(target);
	}
	return user;
    }

    /**
     * Attempts to authenticate the requester and initiate an
     * authenticated session for them. An authenticated session involves
     * their receiving a cookie that provides them to be authenticated and
     * an entry in the session database being created that maps their
     * information to their userid. If this call completes, the session
     * was established and the proper cookies were set in the supplied
     * response object. If invalid authentication information is provided
     * or some other error occurs, an exception will be thrown.
     *
     * @param username The username supplied by the user.
     * @param password The plaintext password supplied by the user.
     * @param persist If true, the cookie will expire in one month, if
     * false, the cookie will expire in 24 hours.
     *
     * @return the user object of the authenticated user.
     */
    public User login (String username, String password, boolean persist,
		       HttpServletResponse rsp)
	throws PersistenceException, NoSuchUserException,
        InvalidPasswordException
    {
	// load up the requested user
	User user = _repository.loadUser(username);
	if (user == null) {
	    throw new NoSuchUserException("error.no_such_user");
	}
	if (!user.passwordsMatch(password)) {
	    throw new InvalidPasswordException("error.invalid_password");
	}

	// generate a new session for this user
	String authcode = _repository.createNewSession(user, persist);
	// stick it into a cookie for their browsing convenience
	Cookie acookie = new Cookie(USERAUTH_COOKIE, authcode);
	acookie.setPath("/");
        if (persist) {
            acookie.setMaxAge(30*24*60*60); // expire in one month
        } else {
            acookie.setMaxAge(24*60*60); // expire in 24 hours
        }
	rsp.addCookie(acookie);

	return user;
    }

    public void logout (HttpServletRequest req, HttpServletResponse rsp)
    {
	String authcode = getAuthCode(req);

	// nothing to do if they don't already have an auth cookie
	if (authcode == null) {
	    return;
	}

	// set them up the bomb
	Cookie rmcookie = new Cookie(USERAUTH_COOKIE, authcode);
	rmcookie.setPath("/");
	rmcookie.setMaxAge(0);
	rsp.addCookie(rmcookie);
    }

    protected static String getAuthCode (HttpServletRequest req)
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
