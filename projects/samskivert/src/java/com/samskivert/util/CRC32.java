//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2006 Michael Bayne
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

import java.math.*;

/**
 * <p> Calculates the CRC32 - 32 bit Cyclical Redundancy Check. This check is
 * used in numerous systems to verify the integrity of information.  It's also
 * used as a hashing function.  Unlike a regular checksum, it's sensitive to
 * the order of the characters.  It produces a 32 bit Java <code>int</code>.
 *
 * <p> This Java programme was translated from a C version I had written.  This
 * software is in the public domain.
 *
 * <p> When calculating the CRC32 over a number of strings or byte arrays the
 * previously calculated CRC is passed to the next call.  In this way the CRC
 * is built up over a number of items, including a mix of strings and byte
 * arrays.
 *
 * <pre>
 * int crcCalc = CRC32.crc32("Hello World");
 * crcCalc = CRC32.crc32("How are you?", crcCalc);
 * crcCalc = CRC32.crc32("I'm feeling really good, how about you?", crcCalc);
 * </pre>
 *
 * <p> The line <code>int crcCalc = CRC32.crc32("Hello World");</code> is
 * equivalent to <code>int crcCalc = CRC32.crc32("Hello World", -1);</code>.
 * When starting a new CRC calculation the "previous crc" is set to 0xFFFFFFFF
 * (or -1).
 *
 * @author Michael Lecuyer (mjl@theorem.com)
 * @author Michael Bayne (slight modifications)
 */
public class CRC32
{
    /**
     * Convenience mithod for generating a CRC from a single
     * <code>String</code>.
     *
     * @param buffer string to generate the CRC32.
     * @return 32 bit CRC.
     */
    public static int crc32 (String buffer)
    {
        return crc32(buffer, 0xFFFFFFFF);
    }

    /**
     * Convenience method for generating a CRC from a <code>byte</code> array.
     *
     * @param buffer byte array to generate the CRC32.
     * @return 32 bit CRC.
     */
    public static int crc32 (byte buffer[])
    {
        return crc32(buffer, 0xFFFFFFFF);
    }

    /**
     * Convenience method for generating a CRC from a series of
     * <code>String</code>'s.
     *
     * @param buffer string to generate the CRC32.
     * @param crc previously generated CRC32.
     * @return 32 bit CRC.
     */
    public static int crc32 (String buffer, int crc)
    {
        return crc32(buffer.getBytes(), crc);
    }

    /**
     * Convenience method for generating a CRC from a series of
     * <code>byte</code> arrays.
     *
     * @param buffer byte array to generate the CRC32
     * @param crc previously generated CRC32.
     * @return 32 bit CRC.
     */
    public static int crc32 (byte buffer[], int crc)
    {
        return crc32(buffer, 0, buffer.length, crc);
    }

    /**
     * General CRC generation function.
     *
     * @param buffer byte array to generate the CRC32.
     * @param start byte start position.
     * @param count number of byte's to include in CRC calculation.
     * @param lastcrc previously generated CRC32.
     * @return 32 bit CRC
     */
    public static int crc32 (byte buffer[], int start, int count, int lastcrc)
    {
        int crc = lastcrc;
        for (int ii = start; count-- != 0; ii++) {
            int temp1 = crc >>> 8;
            int temp2 = _crcTable[(crc ^ buffer[ii]) & 0xFF];
            crc = temp1 ^ temp2;
        }
        return crc;
    }

    protected static int[] _crcTable = new int[256];
    protected static final int CRC32_POLYNOMIAL = 0xEDB88320;

    static {
        int crc;
        for (int ii = 0; ii <= 255; ii++) {
            crc = ii;
            for (int jj = 8; jj > 0; jj--) {
                if ((crc & 1) == 1) {
                    crc = (crc >>> 1) ^ CRC32_POLYNOMIAL;
                } else {
                    crc >>>= 1;
                }
            }
            _crcTable[ii] = crc;
        }
    }
}
