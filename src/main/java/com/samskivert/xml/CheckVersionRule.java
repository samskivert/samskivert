//
// $Id$
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

package com.samskivert.xml;

import org.apache.commons.digester.Rule;

import static com.samskivert.Log.log;

/**
 * Used to compare a file format version number in an XML file with the
 * one compiled into the parsing code.
 */
public class CheckVersionRule extends Rule
{
    /**
     * Constructs a check version rule with the specified know version
     * number. If a version newer than the specified version is parsed, a
     * big fat warning will be issued.
     *
     * @param version the version number that the compiled code expects to
     * see.
     * @param parserIdentifier the name of the parser using this rule
     * which will be reported in the event of a version mismatch.
     */
    public CheckVersionRule (int version, String parserIdentifier)
    {
        // keep this for later
        _version = version;
        _parserIdentifier = parserIdentifier;
    }

    @Override
    public void body (String namespace, String name, String bodyText)
        throws Exception
    {
        int version = Integer.parseInt(bodyText.trim());
        if (version > _version) {
            log.warning(_parserIdentifier + " only knows about version " +
                        _version + ", but is being asked to parse a file " +
                        "with version " + version + ". Wackiness may ensue.");
        }
    }

    protected int _version;
    protected String _parserIdentifier;
}
