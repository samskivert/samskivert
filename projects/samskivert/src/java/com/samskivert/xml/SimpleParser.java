//
// $Id: SimpleParser.java,v 1.2 2001/11/08 01:17:41 mdb Exp $

package com.samskivert.xml;

import java.io.*;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import com.samskivert.Log;
import com.samskivert.io.NestableIOException;

/**
 * The simple parser class provides an extensible object that is
 * intended to simplify the task of writing an object to parse a
 * particular XML file.
 */
public class SimpleParser extends DefaultHandler
{
    // documentation inherited
    public void characters (char ch[], int start, int length)
    {
  	_chars.append(ch, start, length);
    }

    // documentation inherited
    public void endElement (String uri, String localName, String qName)
    {
	finishElement(uri, localName, qName, _chars.toString().trim());
        _chars = new StringBuffer();
    }

    /**
     * Parse the given file.
     *
     * @param fname the file to parse.
     *
     * @exception IOException thrown if an error occurs while parsing
     * the file.
     */
    public void parseFile (String fname)
        throws IOException
    {
        _fname = fname;

	try {
            InputStream is = getInputStream(fname);

            // read the XML input stream and construct the scene object
            _chars = new StringBuffer();
	    XMLUtil.parse(this, is);

        } catch (ParserConfigurationException pce) {
  	    throw new NestableIOException(pce);

	} catch (SAXException saxe) {
	    throw new NestableIOException(saxe);
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
    protected InputStream getInputStream (String fname)
        throws IOException
    {
        FileInputStream fis = new FileInputStream(fname);
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
            Log.warning("Malformed integer value [val=" + val + "].");
            return -1;
        }
    }

    /** The file being parsed. */
    protected String _fname;

    /** The character data gathered while parsing. */
    protected StringBuffer _chars;
}
