//
// $Id: UserUtil.java,v 1.3 2001/05/26 03:22:49 mdb Exp $

package com.samskivert.servlet.user;

import java.security.*;

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
	StringBuffer buf = new StringBuffer();
	buf.append(user.password);
	buf.append(System.currentTimeMillis());
	buf.append(Math.random());

	// and MD5 hash it
	try {
	    MessageDigest digest = MessageDigest.getInstance("MD5");
	    byte[] enc = digest.digest(buf.toString().getBytes());
	    return StringUtil.hexlate(enc);

	} catch (NoSuchAlgorithmException nsae) {
	    throw new RuntimeException("JVM missing MD5 message digest " +
				       "algorithm implementation. User " +
				       "management facilities require MD5 " +
				       "encoding capabilities.");
	}
    }

    /**
     * Encrypts the user's password according to our preferred scheme.
     */
    public static String encryptPassword (String username, String password)
    {
	return Crypt.crypt(username.substring(0, 2), password);
    }

    public static void main (String[] args)
    {
        if (args.length < 2) {
            System.err.println("Usage: UserUtil username password");
            System.exit(-1);
        }

        System.out.println("Encrypted password: " +
                           encryptPassword(args[0], args[1]));
    }
}
