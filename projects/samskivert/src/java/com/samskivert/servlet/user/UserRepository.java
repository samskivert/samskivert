//
// $Id: UserRepository.java,v 1.19 2001/11/01 00:07:18 mdb Exp $
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

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Properties;

import org.apache.regexp.*;

import com.samskivert.Log;
import com.samskivert.io.PersistenceException;
import com.samskivert.jdbc.*;
import com.samskivert.jdbc.jora.*;
import com.samskivert.servlet.SiteIdentifier;
import com.samskivert.util.HashIntMap;

/**
 * Interfaces with the RDBMS in which the user information is stored. The
 * user repository encapsulates the creating, loading and management of
 * users and sessions.
 */
public class UserRepository extends JORARepository
{
    /**
     * The database identifier used to obtain a connection from our
     * connection provider. The value is <code>userdb</code> which you'll
     * probably need to know to provide the proper configuration to your
     * connection provider.
     */
    public static final String USER_REPOSITORY_IDENT = "userdb";

    /**
     * Creates the repository and opens the user database. The database
     * identifier used to fetch our database connection is documented by
     * {@link #USER_REPOSITORY_IDENT}.
     *
     * @param provider the database connection provider.
     */
    public UserRepository (ConnectionProvider provider)
    {
	super(provider, USER_REPOSITORY_IDENT);
    }

    protected void createTables (Session session)
    {
	// create our table object
	_utable = new Table(User.class.getName(), "users", session,
			    "userId");
    }

    /**
     * Requests that a new user be created in the repository.
     *
     * @param username the username of the new user to create. Usernames
     * must consist only of characters that match the following regular
     * expression: <code>[_A-Za-z0-9]+</code> and be longer than two
     * characters.
     * @param password the (unencrypted) password for the new user.
     * @param realname the user's real name.
     * @param email the user's email address.
     * @param siteId the unique identifier of the site through which this
     * account is being created. The resulting user will be tracked as
     * originating from this site for accounting purposes ({@link
     * SiteIdentifier#DEFAULT_SITE_ID} can be used by systems that don't
     * desire to perform site tracking.
     *
     * @return The userid of the newly created user.
     */
    public int createUser (String username, String password,
			   String realname, String email, int siteId)
	throws InvalidUsernameException, UserExistsException,
        PersistenceException
    {
	// check minimum length
	if (username.length() < MINIMUM_USERNAME_LENGTH) {
	    throw new InvalidUsernameException("error.username_too_short");
	}

	// check that it's only valid characters
	if (!_userre.match(username)) {
	    throw new InvalidUsernameException("error.invalid_username");
	}

	// create a new user object and stick it into the database
	final User user = new User();
	user.username = username;
	user.password = UserUtil.encryptPassword(username, password);
	user.realname = realname;
	user.email = email;
	user.created = new Date(System.currentTimeMillis());
        user.siteId = siteId;

        execute(new Operation () {
            public Object invoke (Connection conn, DatabaseLiaison liaison)
                throws PersistenceException, SQLException
            {
                try {
		    _utable.insert(user);
		    // update the userid now that it's known
		    user.userId = liaison.lastInsertedId(conn);
                    // nothing to return
                    return null;

                } catch (SQLException sqe) {
                    if (liaison.isDuplicateRowException(sqe)) {
                        throw new UserExistsException("error.user_exists");
                    } else {
                        throw sqe;
                    }
                }
            }
        });

	return user.userId;
    }

    /**
     * Looks up a user by username.
     *
     * @return the user with the specified user id or null if no user with
     * that id exists.
     */
    public User loadUser (final String username)
	throws PersistenceException
    {
        return (User)execute(new Operation () {
            public Object invoke (Connection conn, DatabaseLiaison liaison)
                throws PersistenceException, SQLException
            {
                // look up the user
                String query = "where username = '" + username + "'";
                Cursor ec = _utable.select(query);

                // fetch the user from the cursor
                User user = (User)ec.next();
                if (user != null) {
                    // call next() again to cause the cursor to close itself
                    ec.next();
                }

                return user;
            }
        });
    }

    /**
     * Looks up a user by userid.
     *
     * @return the user with the specified user id or null if no user with
     * that id exists.
     */
    public User loadUser (final int userId)
	throws PersistenceException
    {
        return (User)execute(new Operation () {
            public Object invoke (Connection conn, DatabaseLiaison liaison)
                throws PersistenceException, SQLException
            {
                // look up the user
                Cursor ec = _utable.select("where userId = " + userId);

                // fetch the user from the cursor
                User user = (User)ec.next();
                if (user != null) {
                    // call next() again to cause the cursor to close itself
                    ec.next();
                }

                return user;
            }
        });
    }

    /**
     * Looks up a user by a session identifier.
     *
     * @return the user associated with the specified session or null of
     * no session exists with the supplied identifier.
     */
    public User loadUserBySession (final String sessionKey)
	throws PersistenceException
    {
        return (User)execute(new Operation () {
            public Object invoke (Connection conn, DatabaseLiaison liaison)
                throws PersistenceException, SQLException
            {
                String query = "where authcode = '" + sessionKey +
                    "' AND sessions.userId = users.userId";
                // look up the user
                Cursor ec = _utable.select("sessions", query);

                // fetch the user from the cursor
                User user = (User)ec.next();
                if (user != null) {
                    // call next() again to cause the cursor to close itself
                    ec.next();
                }

                return user;
            }
        });
    }

    /**
     * Updates a user that was previously fetched from the repository.
     */
    public void updateUser (final User user)
	throws PersistenceException
    {
	execute(new Operation () {
            public Object invoke (Connection conn, DatabaseLiaison liaison)
                throws PersistenceException, SQLException
	    {
		_utable.update(user);
                // nothing to return
                return null;
	    }
	});
    }

    /**
     * Removes the user from the repository.
     */
    public void deleteUser (final User user)
	throws PersistenceException
    {
	execute(new Operation () {
            public Object invoke (Connection conn, DatabaseLiaison liaison)
                throws PersistenceException, SQLException
	    {
		_utable.delete(user);
                // nothing to return
                return null;
	    }
	});
    }

    /**
     * Creates a new session for the specified user and returns the
     * randomly generated session identifier for that session. Temporary
     * sessions are set to expire in two days which prevents someone from
     * being screwed if they log in at 11:59pm, but also prevents them
     * from leaving their browser authenticated for too long. Persistent
     * sessions expire after one month.
     */
    public String createNewSession (final User user, boolean persist)
	throws PersistenceException
    {
	// generate a random session identifier
	final String authcode = UserUtil.genAuthCode(user);

	// figure out when to expire the session
	Calendar cal = Calendar.getInstance();
	cal.add(Calendar.DATE, persist ? 30 : 2);
	final Date expires = new Date(cal.getTime().getTime());

	// insert the session into the database
	execute(new Operation () {
            public Object invoke (Connection conn, DatabaseLiaison liaison)
                throws PersistenceException, SQLException
	    {
		_session.execute("insert into sessions " +
				 "(authcode, userId, expires) values('" +
				 authcode + "', " + user.userId + ", '" +
				 expires + "')");
                // nothing to return
                return null;
	    }
	});

	// and let the user know what the session identifier is
	return authcode;
    }

    /**
     * Prunes any expired sessions from the sessions table.
     */
    public void pruneSessions ()
	throws PersistenceException
    {
	execute(new Operation () {
            public Object invoke (Connection conn, DatabaseLiaison liaison)
                throws PersistenceException, SQLException
	    {
		_session.execute("delete from sessions where " +
				 "expires <= CURRENT_DATE()");
                // nothing to return
                return null;
	    }
	});
    }

    /**
     * Loads the usernames of the users identified by the supplied user
     * ids and returns them in an array. If any users do not exist, their
     * slot in the array will contain a null.
     */
    public String[] loadUserNames (int[] userIds)
	throws PersistenceException
    {
	return loadNames(userIds, "username");
    }

    /**
     * Loads the real names of the users identified by the supplied user
     * ids and returns them in an array. If any users do not exist, their
     * slot in the array will contain a null.
     */
    public String[] loadRealNames (int[] userIds)
	throws PersistenceException
    {
	return loadNames(userIds, "realname");
    }

    protected String[] loadNames (int[] userIds, final String column)
	throws PersistenceException
    {
        // if userids is zero length, we've got no work to do
        if (userIds.length == 0) {
            return new String[0];
        }

	// build up the string we need for the query
	final StringBuffer ids = new StringBuffer();
	for (int i = 0; i < userIds.length; i++) {
	    if (ids.length() > 0) {
		ids.append(", ");
	    }
	    ids.append(userIds[i]);
	}

	final HashIntMap map = new HashIntMap();

	// do the query
        execute(new Operation () {
            public Object invoke (Connection conn, DatabaseLiaison liaison)
                throws PersistenceException, SQLException
            {
                Statement stmt = _session.connection.createStatement();
                try {
                    String query = "select userId, " + column +
                        " from users " +
                        "where userId in (" + ids + ")";
                    ResultSet rs = stmt.executeQuery(query);
                    while (rs.next()) {
                        int userId = rs.getInt(1);
                        String name = rs.getString(2);
                        map.put(userId, name);
                    }

                    // nothing to return
                    return null;

                } finally {
                    JDBCUtil.close(stmt);
                }
            }
        });

	// finally construct our result
	String[] result = new String[userIds.length];
	for (int i = 0; i < userIds.length; i++) {
	    result[i] = (String)map.get(userIds[i]);
	}

	return result;
    }

    /**
     * Returns an array with the real names of every user in the system.
     * This is for Paul who whined about not knowing who was using Who,
     * Where, When because he didn't feel like emailing anyone that wasn't
     * already using it to link up.
     */
    public String[] loadAllRealNames ()
	throws PersistenceException
    {
        final ArrayList names = new ArrayList();

	// do the query
        execute(new Operation () {
            public Object invoke (Connection conn, DatabaseLiaison liaison)
                throws PersistenceException, SQLException
            {
                Statement stmt = _session.connection.createStatement();
                try {
                    String query = "select realname from users";
                    ResultSet rs = stmt.executeQuery(query);
                    while (rs.next()) {
                        names.add(rs.getString(1));
                    }

                    // nothing to return
                    return null;

                } finally {
                    JDBCUtil.close(stmt);
                }
            }
        });

	// finally construct our result
	String[] result = new String[names.size()];
        names.toArray(result);
	return result;
    }

    public static void main (String[] args)
    {
	Properties props = new Properties();
	props.put(USER_REPOSITORY_IDENT + ".driver",
                  "org.gjt.mm.mysql.Driver");
	props.put(USER_REPOSITORY_IDENT + ".url",
                  "jdbc:mysql://localhost:3306/samskivert");
	props.put(USER_REPOSITORY_IDENT + ".username", "www");
	props.put(USER_REPOSITORY_IDENT + ".password", "Il0ve2PL@Y");

	try {
            StaticConnectionProvider scp =
                new StaticConnectionProvider(props);
	    UserRepository rep = new UserRepository(scp);

	    System.out.println(rep.loadUser("mdb"));
	    System.out.println(rep.loadUserBySession("auth"));

	    rep.createUser("samskivert", "foobar", "Michael Bayne",
			   "mdb@samskivert.com",
                           SiteIdentifier.DEFAULT_SITE_ID);
	    rep.createUser("mdb", "foobar", "Michael Bayne",
			   "mdb@samskivert.com",
                           SiteIdentifier.DEFAULT_SITE_ID);

	    scp.shutdown();

	} catch (Throwable t) {
	    t.printStackTrace(System.err);
	}
    }

    protected Table _utable;

    /** Used to check usernames for invalid characteres. */
    protected static RE _userre;
    static {
	try {
	    _userre = new RE("^[_A-Za-z0-9]+$");
	} catch (RESyntaxException rese) {
	    Log.warning("Unable to initialize user regexp?! " + rese);
	}
    }

    /** The minimum allowable length of a username. */
    protected static final int MINIMUM_USERNAME_LENGTH = 3;
}
