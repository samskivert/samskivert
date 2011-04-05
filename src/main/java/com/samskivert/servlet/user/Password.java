//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2011 Michael Bayne, et al.
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
