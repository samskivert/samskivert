//
// $Id: LibraryDescriptorParser.java,v 1.1 2002/04/11 07:23:59 mdb Exp $

package com.samskivert.depends;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.net.URL;
import java.net.MalformedURLException;

/**
 * Parses XML library descriptor definitions.
 */
public class LibraryDescriptorParser
{
    /**
     * Parses the specified library descriptor file.
     */
    public static LibraryDescriptor parseLibraryDescriptor (File descriptor)
        throws FormatException, IOException
    {
        return parseLibraryDescriptor(new FileInputStream(descriptor));
    }

    /**
     * Parses the library descriptor definition provided via the supplied
     * input stream.
     */
    public static LibraryDescriptor parseLibraryDescriptor (InputStream source)
        throws FormatException, IOException
    {
        try {
            LibraryDescriptor ld = new LibraryDescriptor();
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            DescriptorHandler handler = new DescriptorHandler(ld);
            parser.parse(source, handler);
            return ld;

        } catch (SAXException saxe) {
            throw new FormatException(saxe.getMessage());

        } catch (ParserConfigurationException pce) {
            throw new FormatException(
                "Unable to load parser: " + pce.getMessage());
        }
    }

    public static void main (String[] args)
    {
        try {
            parseLibraryDescriptor(new File(args[0]));
        } catch (Exception e) {
            Log.warning("Chokey choke-opolis", e);
        }
    }

    /**
     * Used to parse a library descriptor specification.
     */
    protected static class DescriptorHandler extends DefaultHandler
    {
        public DescriptorHandler (LibraryDescriptor descriptor)
        {
            _descriptor = descriptor;
        }

        public void startElement (String namespaceURI, String localName,
                                  String qName, Attributes attrs)
        {
        }

        protected boolean validateAttribute (String name, String value)
        {
            if (value == null || value.length() == 0) {
                Log.warning("Skipping dependency declaration that is " +
                            "missing required attribute '" + name + "'.");
                return false;
            } else {
                return true;
            }
        }

        protected LibraryDescriptor _descriptor;
    }
}
