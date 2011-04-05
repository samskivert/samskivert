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
