//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2007 Michael Bayne
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

import com.samskivert.util.Crypt;
import com.samskivert.util.StringUtil;

/**
 * User related utility functions.
 */
public class UserUtil
{
    /**
     * Generates a new random session identifier for the supplied user.
     */
    public static String genAuthCode (User user)
    {
        // concatenate a bunch of secret stuff together
        StringBuilder buf = new StringBuilder();
        buf.append(user.password);
        buf.append(System.currentTimeMillis());
        buf.append(Math.random());

        // and MD5 hash it
        return StringUtil.md5hex(buf.toString());
    }

    /**
     * Encrypts the supplied username and password and returns the value
     * that would be stored in the user record were the password to be
     * updated via {@link User#setPassword}.
     */
    public static String encryptPassword (String password)
    {
        return StringUtil.md5hex(password);
    }

    /**
     * Encrypts passwords the way we used to.
     */
    public static String legacyEncrypt (String username, String password,
                                        boolean ignoreUserCase)
    {
        if (ignoreUserCase) {
            username = username.toLowerCase();
        }
        return Crypt.crypt(StringUtil.truncate(username, 2), password);
    }

    public static void main (String[] args)
    {
        if (args.length < 2) {
            System.err.println("Usage: UserUtil password");
            System.exit(-1);
        }
        System.out.println("Encrypted password: " + encryptPassword(args[0]));
    }
}
