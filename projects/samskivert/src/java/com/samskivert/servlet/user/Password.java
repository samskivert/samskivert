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
     * Returns the clear password text. This method may return null if
     * none was provided when creating the password.
     */
    public String getCleartext ()
    {
        return _cleartext;
    }

    /**
     * Returns the encrypted password text.
     */
    public String getEncrypted ()
    {
        return _encrypted;
    }

    /**
     * Creates a password instance from the supplied plaintext.
     */
    public static Password makeFromClear (String password)
    {
        return new Password(password, UserUtil.encryptPassword(password));
    }

    /**
     * Creates a password instance from the supplied already encrypted
     * text. <em>Note:</em> the encrypted text must be obtained from
     * {@link UserUtil#encryptPassword}.
     */
    public static Password makeFromCrypto (String encrypted)
    {
        return new Password(null, encrypted);
    }

    /** Creates a password instance. */
    protected Password (String cleartext, String encrypted)
    {
        _cleartext = cleartext;
        _encrypted = encrypted;
    }

    protected String _cleartext;
    protected String _encrypted;
}
