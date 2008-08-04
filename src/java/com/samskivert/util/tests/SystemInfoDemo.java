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

package com.samskivert.util.tests;

import com.samskivert.util.SystemInfo;

public class SystemInfoDemo
{
    public static void main (String[] args)
    {
        // output initial system information
        SystemInfo info = new SystemInfo();
        System.out.println("Initial system info:");
        System.out.println(info.toString());

        // allocate a bit of data
        System.out.println("\nAllocating test data.");
        for (int ii = 0; ii < 10000; ii++) {
            @SuppressWarnings("unused") int[] data = new int[100];
        }

        // update and output the latest system information
        info.update();
        System.out.println("\nUpdated system info:");
        System.out.println(info.toString());
    }
}
