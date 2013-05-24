//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.xml;

import java.io.*;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import static com.samskivert.xml.Log.log;

/**
 * The simple parser class provides an extensible object that is
 * intended to simplify the task of writing an object to parse a
 * particular XML file.
 */
public class SimpleParser extends DefaultHandler
{
    @Override
    public void characters (char ch[], int start, int length)
    {
          _chars.append(ch, start, length);
    }

    @Override
    public void endElement (String uri, String localName, String qName)
    {
        finishElement(uri, localName, qName, _chars.toString().trim());
        _chars = new StringBuilder();
    }

    /**
     * Parse the given file.
     *
     * @param path the full path to the file to parse.
     *
     * @exception IOException thrown if an error occurs while parsing
     * the file.
     */
    public void parseFile (String path)
        throws IOException
    {
        parseStream(getInputStream(path));
    }

    /**
     * Parse the given input stream.
     *
     * @param stream the input stream from which the XML source to be
     * parsed can be loaded.
     *
     * @exception IOException thrown if an error occurs while parsing
     * the stream.
     */
    public void parseStream (InputStream stream)
        throws IOException
    {
        try {
            // read the XML input stream and construct the scene object
            _chars = new StringBuilder();
            XMLUtil.parse(this, stream);

        } catch (ParserConfigurationException pce) {
            throw (IOException) new IOException().initCause(pce);

        } catch (SAXException saxe) {
            throw (IOException) new IOException().initCause(saxe);
        }
    }

    /**
     * Called when parsing an element is finished and its data is
     * fully available.  Essentially the same as {@link #endElement}
     * excepting that the final complete character data is provided as
     * a parameter and is conveniently trimmed of any extraneous white
     * space.
     */
    protected void finishElement (
        String uri, String localName, String qName, String data)
    {
        // nothing for now
    }

    /**
     * Returns an input stream to read data from the given file name.
     */
    protected InputStream getInputStream (String path)
        throws IOException
    {
        FileInputStream fis = new FileInputStream(path);
        return new BufferedInputStream(fis);
    }

    /**
     * Parse the given string as an integer and return the integer
     * value, or -1 if the string is malformed.
     */
    protected int parseInt (String val)
    {
        try {
            return (val == null) ? -1 : Integer.parseInt(val);
        } catch (NumberFormatException nfe) {
            log.warning("Malformed integer value", "val", val);
            return -1;
        }
    }

    /** The character data gathered while parsing. */
    protected StringBuilder _chars;
}
