//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.net;

import java.net.InetSocketAddress;

import com.samskivert.util.StringUtil;

/**
 * Address related utilities.
 */
public class AddressUtil
{
    /**
     * Creates a address to the given host, or the wildcard host if the hostname is
     * {@link StringUtil#blank}.
     */
    public static InetSocketAddress getAddress (String hostname, int port)
    {
        return StringUtil.isBlank(hostname) ?
            new InetSocketAddress(port) : new InetSocketAddress(hostname, port);
    }
}
