//
// $Id: UserRepository.java,v 1.40 2004/02/25 13:17:13 mdb Exp $
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

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Properties;

import com.samskivert.io.PersistenceException;
import com.samskivert.jdbc.ConnectionProvider;
import com.samskivert.jdbc.DatabaseLiaison;
import com.samskivert.jdbc.JDBCUtil;
import com.samskivert.jdbc.JORARepository;
import com.samskivert.jdbc.StaticConnectionProvider;
import com.samskivert.jdbc.jora.Cursor;
import com.samskivert.jdbc.jora.FieldMask;
import com.samskivert.jdbc.jora.Session;
import com.samskivert.jdbc.jora.Table;
import com.samskivert.util.HashIntMap;
import com.samskivert.util.StringUtil;

import com.samskivert.servlet.SiteIdentifier;

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
        throws PersistenceException
    {
	super(provider, USER_REPOSITORY_IDENT);
    }

    /**
     * Derived classes that extend the user record with their own
     * additional information will want to override this method and return
     * their desired {@link User} derivation.
     */
    protected Class getUserClass ()
    {
        return User.class;
    }

    // documentation inherited
    protected void createTables (Session session)
    {
	// create our table object
	_utable = new Table(
            getUserClass().getName(), "users", session, "userId");
    }

    /**
     * Like {@link #createUser(String,String,String,String,int)} except
     * that the supplied password has already been encrypted.
     */
    public int createUser (Username username, Password password,
                           String realname, String email, int siteId)
	throws UserExistsException, PersistenceException
    {
	// create a new user object...
        User user = new User();
        // ...configure it...
        populateUser(user, username, password, realname, email, siteId);
        // ...and stick it into the database
        return insertUser(user);
    }

    /**
     * Configures the supplied user record with the provided information
     * in preparation for inserting the record into the database for the
     * first time.
     */
    protected void populateUser (User user, Username name, Password pass,
                                 String realname, String email, int siteId)
    {
	user.username = name.getUsername();
	user.setPassword(pass);
	user.setRealName(realname);
	user.setEmail(email);
	user.created = new Date(System.currentTimeMillis());
        user.setSiteId(siteId);
    }

    /**
     * Inserts the supplied user record into the user database, assigning
     * it a userid in the process, which is returned.
     */
    protected int insertUser (final User user)
	throws UserExistsException, PersistenceException
    {
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
    public User loadUser (String username)
	throws PersistenceException
    {
        username = StringUtil.replace(username, "'", "\\'");
        return loadUserWhere("where username = '" + username + "'");
    }

    /**
     * Looks up a user by userid.
     *
     * @return the user with the specified user id or null if no user with
     * that id exists.
     */
    public User loadUser (int userId)
	throws PersistenceException
    {
        return loadUserWhere("where userId = " + userId);
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
                    // configure the user record with its field mask
                    user.setDirtyMask(_utable.getFieldMask());
                }

                return user;
            }
        });
    }

    /**
     * Looks up users by userid
     *
     * @return the users whom have a user id in the userIds array.
     */
    public HashIntMap loadUsersFromId (int[] userIds)
	throws PersistenceException
    {
        // make sure we actually have something to do
        if (userIds.length == 0) {
            return new HashIntMap();
        }

        final String ids = genIdString(userIds);

        return (HashIntMap)execute(new Operation () {
            public Object invoke (Connection conn, DatabaseLiaison liaison)
                throws PersistenceException, SQLException
            {
                // look up the users
                Cursor ec = _utable.select("where userid in (" + ids + ")");

                User user;
                HashIntMap data = new HashIntMap();
                while ((user = (User)ec.next()) != null) {
                    user.setDirtyMask(_utable.getFieldMask());
                    data.put(user.userId, user);
                }

                return data;
            }
        });
    }

    /**
     * Lookup a user by email address, something that is not efficient and
     * should really only be done by site admins attempting to look up a
     * user record.
     *
     * @return the user with the specified user id or null if no user with
     * that id exists.
     */
    public ArrayList lookupUsersByEmail (String email)
	throws PersistenceException
    {
        final String where = "where email = '" +
            StringUtil.replace(email, "'", "\\'") + "'";
        return (ArrayList) execute(new Operation() {
            public Object invoke (Connection conn, DatabaseLiaison liaison)
                throws PersistenceException, SQLException
            {
                return _utable.select(where).toArrayList();
            }
        });
    }

    /**
     * Loads up a user record that matches the specified where clause.
     * Returns null if no record matches.
     */
    protected User loadUserWhere (final String whereClause)
	throws PersistenceException
    {
        return (User)execute(new Operation () {
            public Object invoke (Connection conn, DatabaseLiaison liaison)
                throws PersistenceException, SQLException
            {
                // look up the user
                Cursor ec = _utable.select(whereClause);

                // fetch the user from the cursor
                User user = (User)ec.next();
                if (user != null) {
                    // call next() again to cause the cursor to close itself
                    ec.next();
                    // configure the user record with its field mask
                    user.setDirtyMask(_utable.getFieldMask());
                }

                return user;
            }
        });
    }

    /**
     * Updates a user that was previously fetched from the repository.
     * Only fields that have been modified since it was loaded will be
     * written to the database and those fields will subsequently be
     * marked clean once again.
     *
     * @return true if the record was updated, false if the update was
     * skipped because no fields in the user record were modified.
     */
    public boolean updateUser (final User user)
	throws PersistenceException
    {
        if (!user.getDirtyMask().isModified()) {
            // nothing doing!
            return false;
        }

	execute(new Operation () {
            public Object invoke (Connection conn, DatabaseLiaison liaison)
                throws PersistenceException, SQLException
	    {
		_utable.update(user, user.getDirtyMask());
                // nothing to return
                return null;
	    }
	});

        return true;
    }

    /**
     * 'Delete' the users account such that they can no longer access it,
     * however we do not delete the record from the db.  The name is
     * changed such that the original name has XX=FOO if the name were FOO
     * originally.  If we have to lop off any of the name to get our
     * prefix to fit we use a minus sign instead of a equals side.  The
     * password field is set to be the empty string so that no one can log
     * in (since nothing hashes to the empty string.  We also make sure
     * their email address no longer works, so in case we don't ignore
     * 'deleted' users when we do the sql to get emailaddresses for the mass
     * mailings we still won't spam delete folk.  We leave the emailaddress
     * intact exect for the @ sign which gets turned to a #, so that we can
     * see what their email was incase it was an accidently deletion and we
     * have to verify through email.
     */
    public void deleteUser (final User user)
	throws PersistenceException
    {
        if (user.isDeleted()) {
            return;
        }

	execute(new Operation () {
            public Object invoke (Connection conn, DatabaseLiaison liaison)
                throws PersistenceException, SQLException
	    {
               // create our modified fields mask
                FieldMask mask = _utable.getFieldMask();
                mask.setModified("username");
                mask.setModified("password");
                mask.setModified("email");

                // set the password to unusable
                user.password = "";

                // 'disable' their email address
                String newEmail = user.email.replace('@','#');
                user.email = newEmail;

                String oldName = user.username;
                for (int ii = 0; ii < 100; ii++) {
                    try {
                        user.username = StringUtil.truncate(
                            ii + "=" + oldName, 24);
                        _utable.update(user, mask);
                        return null; // nothing to return
                    } catch (SQLException se) {
                        if (!liaison.isDuplicateRowException(se)) {
                            throw se;
                        }
                    }
                }

                // ok we failed to rename the user, lets bust an error
                throw new PersistenceException("Failed to 'delete' the user");
	    }
	});
    }

    /**
     * Creates a new session for the specified user and returns the
     * randomly generated session identifier for that session. Temporary
     * sessions are set to expire at the end of the user's browser
     * session. Persistent sessions expire after one month.
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

    protected String[] loadNames (int[] userIds, final String column)
	throws PersistenceException
    {
        // if userids is zero length, we've got no work to do
        if (userIds.length == 0) {
            return new String[0];
        }

        final String ids = genIdString(userIds);

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
     * Take the passed in int array and create the a string suitable for
     * using in a SQL set query (I.e., "select foo, from bar where userid
     * in (genIdString(userIds))"; )
     */
    protected String genIdString (int[] userIds)
    {
	// build up the string we need for the query
	StringBuffer ids = new StringBuffer();
	for (int i = 0; i < userIds.length; i++) {
	    if (ids.length() > 0) {
                ids.append(",");
            }
            ids.append(userIds[i]);
	}

        return ids.toString();
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

	    rep.createUser(new Username("samskivert"),
                           Password.makeFromClear("foobar"),
                           "Michael Bayne", "mdb@samskivert.com",
                           SiteIdentifier.DEFAULT_SITE_ID);
	    rep.createUser(new Username("mdb"),
                           Password.makeFromClear("foobar"), "Michael Bayne",
			   "mdb@samskivert.com",
                           SiteIdentifier.DEFAULT_SITE_ID);

	    scp.shutdown();

	} catch (Throwable t) {
	    t.printStackTrace(System.err);
	}
    }

    /** A wrapper that provides access to the userstable. */
    protected Table _utable;
}
