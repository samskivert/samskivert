//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.servlet.user;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Calendar;

import com.samskivert.io.PersistenceException;
import com.samskivert.jdbc.ConnectionProvider;
import com.samskivert.jdbc.DatabaseLiaison;
import com.samskivert.jdbc.JDBCUtil;
import com.samskivert.jdbc.JORARepository;
import com.samskivert.jdbc.jora.FieldMask;
import com.samskivert.jdbc.jora.Table;
import com.samskivert.util.HashIntMap;
import com.samskivert.util.StringUtil;

import com.samskivert.servlet.SiteIdentifier;

/**
 * Interfaces with the RDBMS in which the user information is stored. The user repository
 * encapsulates the creating, loading and management of users and sessions.
 */
public class UserRepository extends JORARepository
{
    /**
     * The database identifier used to obtain a connection from our connection provider. The value
     * is <code>userdb</code> which you'll probably need to know to provide the proper
     * configuration to your connection provider.
     */
    public static final String USER_REPOSITORY_IDENT = "userdb";

    /**
     * Creates the repository and opens the user database. The database identifier used to fetch
     * our database connection is documented by {@link #USER_REPOSITORY_IDENT}.
     *
     * @param provider the database connection provider.
     */
    public UserRepository (ConnectionProvider provider)
    {
        super(provider, USER_REPOSITORY_IDENT);
    }

    /**
     * Requests that a new user be created in the repository.
     *
     * @param username the username of the new user to create.
     * @param password the password for the new user.
     * @param realname the user's real name.
     * @param email the user's email address.
     * @param siteId the unique identifier of the site through which this account is being
     * created. The resulting user will be tracked as originating from this site for accounting
     * purposes ({@link SiteIdentifier#DEFAULT_SITE_ID} can be used by systems that don't desire to
     * perform site tracking.
     *
     * @return The userid of the newly created user.
     */
    public int createUser (
        Username username, Password password, String realname, String email, int siteId)
        throws UserExistsException, PersistenceException
    {
        // create a new user object...
        User user = new User();
        user.setDirtyMask(_utable.getFieldMask());

        // ...configure it...
        populateUser(user, username, password, realname, email, siteId);
        // ...and stick it into the database
        return insertUser(user);
    }

    /**
     * Looks up a user by username.
     *
     * @return the user with the specified user id or null if no user with that id exists.
     */
    public User loadUser (String username)
        throws PersistenceException
    {
        return loadUserWhere("where username = " + JDBCUtil.escape(username));
    }

    /**
     * Looks up a user by userid.
     *
     * @return the user with the specified user id or null if no user with that id exists.
     */
    public User loadUser (int userId)
        throws PersistenceException
    {
        return loadUserWhere("where userId = " + userId);
    }

    /**
     * Looks up a user by a session identifier.
     *
     * @return the user associated with the specified session or null of no session exists with the
     * supplied identifier.
     */
    public User loadUserBySession (String sessionKey)
        throws PersistenceException
    {
        User user = load(_utable, "sessions", "where authcode = '" + sessionKey + "' " +
                         "AND sessions.userId = users.userId");
        if (user != null) {
            user.setDirtyMask(_utable.getFieldMask());
        }
        return user;
    }

    /**
     * Looks up users by userid
     *
     * @return the users whom have a user id in the userIds array.
     */
    public HashIntMap<User> loadUsersFromId (int[] userIds)
        throws PersistenceException
    {
        HashIntMap<User> data = new HashIntMap<User>();
        if (userIds.length > 0) {
            String query = "where userId in (" + genIdString(userIds) + ")";
            for (User user : loadAll(_utable, query)) {
                user.setDirtyMask(_utable.getFieldMask());
                data.put(user.userId, user);
            }
        }
        return data;
    }

    /**
     * Lookup a user by email address, something that is not efficient and should really only be
     * done by site admins attempting to look up a user record.
     *
     * @return the user with the specified user id or null if no user with that id exists.
     */
    public ArrayList<User> lookupUsersByEmail (String email)
        throws PersistenceException
    {
        return loadAll(_utable, "where email = " + JDBCUtil.escape(email));
    }

    /**
     * Looks up a list of users that match an arbitrary query. Care should be taken in constructing
     * these queries as the user table is likely to be large and a query that does not make use of
     * indices could be very slow.
     *
     * @return the users matching the specified query or an empty list if there are no matches.
     */
    public ArrayList<User> lookupUsersWhere (final String where)
        throws PersistenceException
    {
        ArrayList<User> users = loadAll(_utable, where);
        for (User user : users) {
            // configure the user record with its field mask
            user.setDirtyMask(_utable.getFieldMask());
        }
        return users;
    }

    /**
     * Updates a user that was previously fetched from the repository.  Only fields that have been
     * modified since it was loaded will be written to the database and those fields will
     * subsequently be marked clean once again.
     *
     * @return true if the record was updated, false if the update was skipped because no fields in
     * the user record were modified.
     */
    public boolean updateUser (final User user)
        throws PersistenceException
    {
        if (!user.getDirtyMask().isModified()) {
            // nothing doing!
            return false;
        }
        update(_utable, user, user.getDirtyMask());
        return true;
    }

    /**
     * 'Delete' the users account such that they can no longer access it, however we do not delete
     * the record from the db.  The name is changed such that the original name has XX=FOO if the
     * name were FOO originally.  If we have to lop off any of the name to get our prefix to fit we
     * use a minus sign instead of a equals side.  The password field is set to be the empty string
     * so that no one can log in (since nothing hashes to the empty string.  We also make sure
     * their email address no longer works, so in case we don't ignore 'deleted' users when we do
     * the sql to get emailaddresses for the mass mailings we still won't spam delete folk.  We
     * leave the emailaddress intact exect for the @ sign which gets turned to a #, so that we can
     * see what their email was incase it was an accidently deletion and we have to verify through
     * email.
     */
    public void deleteUser (final User user)
        throws PersistenceException
    {
        if (user.isDeleted()) {
            return;
        }

        executeUpdate(new Operation<Object>() {
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
                        user.username = StringUtil.truncate(ii + "=" + oldName, 24);
                        _utable.update(conn, user, mask);
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
     * Creates a new session for the specified user and returns the randomly generated session
     * identifier for that session. If a session entry already exists for the specified user it
     * will be reused.
     *
     * @param expireDays the number of days in which the session token should expire.
     */
    public String registerSession (User user, int expireDays)
        throws PersistenceException
    {
        // look for an existing session for this user
        final String query = "select authcode from sessions where userId = " + user.userId;
        String authcode = execute(new Operation<String>() {
            public String invoke (Connection conn, DatabaseLiaison liaison)
                throws PersistenceException, SQLException
            {
                Statement stmt = conn.createStatement();
                try {
                    ResultSet rs = stmt.executeQuery(query);
                    while (rs.next()) {
                        return rs.getString(1);
                    }
                    return null;
                } finally {
                    JDBCUtil.close(stmt);
                }
            }
        });

        // figure out when to expire the session
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, expireDays);
        Date expires = new Date(cal.getTime().getTime());

        // if we found one, update its expires time and reuse it
        if (authcode != null) {
            update("update sessions set expires = '" + expires + "' where " +
                   "authcode = '" + authcode + "'");
        } else {
            // otherwise create a new one and insert it into the table
            authcode = UserUtil.genAuthCode(user);
            update("insert into sessions (authcode, userId, expires) values('" + authcode + "', " +
                   user.userId + ", '" + expires + "')");
        }

        return authcode;
    }

    /**
     * Validates that the supplied session key is still valid and if so, refreshes it for the
     * specified number of days.
     *
     * @return true if the session was located and refreshed, false if it no longer exists.
     */
    public boolean refreshSession (String sessionKey, int expireDays)
        throws PersistenceException
    {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, expireDays);
        Date expires = new Date(cal.getTime().getTime());

        // attempt to update an existing session row, returning true if we found and updated it
        return (update("update sessions set expires = '" + expires + "' " +
                       "where authcode = " + JDBCUtil.escape(sessionKey)) == 1);
    }

    /**
     * Prunes any expired sessions from the sessions table.
     */
    public void pruneSessions ()
        throws PersistenceException
    {
        update("delete from sessions where expires <= CURRENT_DATE()");
    }

    /**
     * Loads the usernames of the users identified by the supplied user ids and returns them in an
     * array. If any users do not exist, their slot in the array will contain a null.
     */
    public String[] loadUserNames (int[] userIds)
        throws PersistenceException
    {
        return loadNames(userIds, "username");
    }

    /**
     * Loads the real names of the users identified by the supplied user ids and returns them in an
     * array. If any users do not exist, their slot in the array will contain a null.
     */
    public String[] loadRealNames (int[] userIds)
        throws PersistenceException
    {
        return loadNames(userIds, "realname");
    }

    /**
     * Returns an array with the real names of every user in the system.
     */
    public String[] loadAllRealNames ()
        throws PersistenceException
    {
        final ArrayList<String> names = new ArrayList<String>();

        // do the query
        execute(new Operation<Object>() {
            public Object invoke (Connection conn, DatabaseLiaison liaison)
                throws PersistenceException, SQLException
            {
                Statement stmt = conn.createStatement();
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
        return names.toArray(new String[names.size()]);
    }

    /**
     * Configures the supplied user record with the provided information in preparation for
     * inserting the record into the database for the first time.
     */
    protected void populateUser (
        User user, Username name, Password pass, String realname, String email, int siteId)
    {
        user.username = name.getUsername();
        user.setPassword(pass);
        user.setRealName(realname);
        user.setEmail(email);
        user.created = new Date(System.currentTimeMillis());
        user.setSiteId(siteId);
    }

    /**
     * Inserts the supplied user record into the user database, assigning it a userid in the
     * process, which is returned.
     */
    protected int insertUser (final User user)
        throws UserExistsException, PersistenceException
    {
        executeUpdate(new Operation<Object>() {
            public Object invoke (Connection conn, DatabaseLiaison liaison)
                throws PersistenceException, SQLException
            {
                try {
                    _utable.insert(conn, user);
                    // update the userid now that it's known
                    user.userId = liaison.lastInsertedId(conn, null, _utable.getName(), "userId");
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
     * Loads up a user record that matches the specified where clause.  Returns null if no record
     * matches.
     */
    protected User loadUserWhere (String where)
        throws PersistenceException
    {
        User user = load(_utable, where);
        if (user != null) {
            user.setDirtyMask(_utable.getFieldMask());
        }
        return user;
    }

    protected String[] loadNames (int[] userIds, final String column)
        throws PersistenceException
    {
        // if userids is zero length, we've got no work to do
        if (userIds.length == 0) {
            return new String[0];
        }

        // do the query
        final String ids = genIdString(userIds);
        final HashIntMap<String> map = new HashIntMap<String>();
        execute(new Operation<Object>() {
            public Object invoke (Connection conn, DatabaseLiaison liaison)
                throws PersistenceException, SQLException
            {
                Statement stmt = conn.createStatement();
                try {
                    String query = "select userId, " + column + " from users " +
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
            result[i] = map.get(userIds[i]);
        }
        return result;
    }

    /**
     * Take the passed in int array and create the a string suitable for using in a SQL set query
     * (I.e., "select foo, from bar where userId in (genIdString(userIds))"; )
     */
    protected String genIdString (int[] userIds)
    {
        // build up the string we need for the query
        StringBuilder ids = new StringBuilder();
        for (int i = 0; i < userIds.length; i++) {
            if (ids.length() > 0) {
                ids.append(",");
            }
            ids.append(userIds[i]);
        }

        return ids.toString();
    }

    @Override
    protected void createTables ()
    {
        // create our table object
        _utable = new Table<User>(User.class, "users", "userId");
    }

    /** A wrapper that provides access to the userstable. */
    protected Table<User> _utable;
}
