//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.xml;

import org.apache.commons.digester.Rule;

import static com.samskivert.xml.Log.log;

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
