//
// $Id: User.java,v 1.1 2001/03/02 01:21:06 mdb Exp $

package com.samskivert.servlet.user;

import java.sql.Date;

/**
 * A user object contains information about a registered user in our web
 * application environment. Users are stored in the user repository (a
 * database table) and loaded during a request based on authentication
 * information provided with the request headers.
 *
 * <p><b>Note:</b> Do not modify any of the fields of this object
 * diretly. Use the <code>set</code> methods to make updates. If no set
 * methods exist, you shouldn't be modifying that field.
 */
public class User
{
    /** The user's assigned integer userid. */
    public int userid;

    /** The user's chosen username. */
    public String username;

    /** The date this record was created. */
    public Date created;

    /**
     * The user's real name (first, last and whatever else they opt to
     * provide).
     */
    public String realname;

    /** The user's chosen password (encrypted). */
    public String password;

    /** The user's email address. */
    public String email;

    /**
     * Updates the user's real name.
     */
    public void setRealName (String realname)
    {
	this.realname = realname;
    }

    /**
     * Updates the user's password.
     *
     * @param password The user's new (unencrypted) password.
     */
    public void setPassword (String password)
    {
	this.password = UserRepository.encryptPassword(username, password);
    }

    /**
     * Updates the user's email address.
     */
    public void setEmail (String email)
    {
	this.email = email;
    }
}
