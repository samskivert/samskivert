//
// $Id: Password.java,v 1.1 2004/01/31 06:24:01 mdb Exp $

package com.samskivert.servlet.user;

/**
 * Represents an encrypted password. Currently only used when creating
 * user accounts.
 */
public class Password
{
    /**
     * Creates a password from the supplied username and (unencrypted)
     * password.
     */
    public Password (String username, String password)
    {
        _encrypted = UserUtil.encryptPassword(username, password);
    }

    /**
     * Creates a password directly from the encrypted text. This text
     * <em>must</em> have been created using {@link
     * UserUtil#encryptPassword} or via the same algorithm.
     */
    public Password (String encrypted)
    {
        _encrypted = encrypted;
    }

    /**
     * Returns the encrypted password text.
     */
    public String getEncrypted ()
    {
        return _encrypted;
    }

    protected String _encrypted;
}
