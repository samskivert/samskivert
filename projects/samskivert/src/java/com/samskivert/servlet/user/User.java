//
// $Id: User.java,v 1.7 2002/09/18 01:18:51 shaper Exp $
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

import java.sql.Date;

import com.samskivert.jdbc.jora.FieldMask;
import com.samskivert.util.StringUtil;

/**
 * A user object contains information about a registered user in our web
 * application environment. Users are stored in the user repository (a
 * database table) and loaded during a request based on authentication
 * information provided with the request headers.
 *
 * <p><b>Note:</b> Do not modify any of the fields of this object
 * directly. Use the <code>set</code> methods to make updates. If no set
 * methods exist, you shouldn't be modifying that field.
 */
public class User
{
    /** The user's assigned integer userid. */
    public int userId;

    /** The user's chosen username. */
    public String username;

    /** The date this record was created. */
    public Date created;

    /** The user's real name (first, last and whatever else they opt to
     * provide). */
    public String realname;

    /** The user's chosen password (encrypted). */
    public String password;

    /** The user's email address. */
    public String email;

    /** The site identifier of the site through which the user created
     * their account. (Their affiliation, if you will.) */
    public int siteId;

    /**
     * Updates the user's real name.
     */
    public void setRealName (String realname)
    {
	this.realname = realname;
        _dirty.setModified("realname");
    }

    /**
     * Updates the user's password.
     *
     * @param password The user's new (unencrypted) password.
     */
    public void setPassword (String password)
    {
	this.password = UserUtil.encryptPassword(username, password);
        _dirty.setModified("password");
    }

    /**
     * Updates the user's email address.
     */
    public void setEmail (String email)
    {
	this.email = email;
        _dirty.setModified("email");
    }

    /**
     * Compares the supplied password with the password associated with
     * this user record.
     *
     * @return true if the passwords match, false if they do not.
     */
    public boolean passwordsMatch (String password)
    {
	String epasswd = UserUtil.encryptPassword(username, password);
	return this.password.equals(epasswd);
    }

    /**
     * Called by the repository to find out which fields have been
     * modified since the object was loaded.
     */
    protected FieldMask getDirtyMask ()
    {
        return _dirty;
    }

    /**
     * Called by the repository to configure this user record with a field
     * mask that it can use to track modified fields.
     */
    protected void setDirtyMask (FieldMask dirty)
    {
        _dirty = dirty;
    }

    /** Returns a string representation of this instance. */
    public String toString ()
    {
        return StringUtil.fieldsToString(this);
    }

    /** Our dirty field mask. */
    protected transient FieldMask _dirty;
}
