//
// $Id: XMLUtil.java,v 1.3 2001/08/13 14:33:52 shaper Exp $
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001 Walter Korman
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

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.*;
import javax.xml.parsers.*;

/**
 * The XMLUtil class provides a simplified interface for XML parsing.
 *
 * <p> Classes wishing to parse XML data should extend the
 * <code>org.xml.sax.helpers.DefaultHandler</code> class, override the
 * desired SAX event handler methods, and call
 * <code>XMLUtil.parse()</code>.
 */
public class XMLUtil
{
    /**
     * Parse the XML data in the given input stream, using the
     * specified handler object as both the content and error handler.
     *
     * @param handler the SAX event handler
     * @param in the input stream containing the XML to be parsed
     */
    public static void parse (DefaultHandler handler, InputStream in)
	throws IOException, ParserConfigurationException, SAXException
    {
	XMLReader xr = _pfactory.newSAXParser().getXMLReader();

	xr.setContentHandler(handler);
	xr.setErrorHandler(handler);

	xr.parse(new InputSource(in));
    }

    /** The factory from whence we obtain XMLReader objects */
    protected static SAXParserFactory _pfactory;

    static {
	_pfactory = SAXParserFactory.newInstance();
	_pfactory.setValidating(false);
    }
}
