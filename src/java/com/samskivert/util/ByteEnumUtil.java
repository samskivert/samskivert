//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2010 Michael Bayne, et al.
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

/**
 * {@link ByteEnum} utility methods.
 */
public class ByteEnumUtil
{
    /**
     * Returns the enum value with the specified code in the supplied enum class.
     *
     * @exception IllegalArgumentException thrown if the enum lacks a value that maps to the
     * supplied code.
     */
    public static <E extends Enum<E> & ByteEnum> E fromByte (Class<E> eclass, byte code)
    {
        for (E value : eclass.getEnumConstants()) {
            if (value.toByte() == code) {
                return value;
            }
        }
        throw new IllegalArgumentException(eclass + " has no value with code " + code);
    }
}
