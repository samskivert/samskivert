//
// $Id: ArrayIntSetTest.java,v 1.1 2002/02/03 07:10:16 mdb Exp $
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

package com.samskivert.util;

import java.util.Arrays;

import junit.framework.Test;
import junit.framework.TestCase;

import com.samskivert.Log;

public class ArrayIntSetTest extends TestCase
{
    public ArrayIntSetTest ()
    {
        super(ArrayIntSetTest.class.getName());
    }

    public void runTest ()
    {
        ArrayIntSet set = new ArrayIntSet();
        set.add(3);
        set.add(5);
        set.add(5);
        set.add(9);
        set.add(5);
        set.add(7);
        set.add(1);

        int[] values = { 1, 3, 5, 7, 9 };
        int[] setvals = set.toIntArray();

        assert("values equal", Arrays.equals(values, setvals));
    }

    public static Test suite ()
    {
        return new ArrayIntSetTest();
    }
}
