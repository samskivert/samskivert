//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

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
