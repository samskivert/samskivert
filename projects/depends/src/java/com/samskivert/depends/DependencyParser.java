//
// $Id: DependencyParser.java,v 1.1 2002/04/11 07:23:59 mdb Exp $

package com.samskivert.depends;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.File;
import java.io.IOException;

import java.net.URL;
import java.net.MalformedURLException;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses XML dependency definitions.
 */
public class DependencyParser
{
    /**
     * Parses the specified dependency definition file into a list of
     * {@link Dependency} instances.
     */
    public static List parseDependencies (File definition)
        throws FormatException, IOException
    {
        try {
            ArrayList list = new ArrayList();
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            DependencyHandler handler = new DependencyHandler(list);
            parser.parse(definition, handler);
            return list;

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
            parseDependencies(new File(args[0]));
        } catch (Exception e) {
            Log.warning("Chokey choke-opolis", e);
        }
    }

    /**
     * Used to parse the dependency specification.
     */
    protected static class DependencyHandler extends DefaultHandler
    {
        public DependencyHandler (List list)
        {
            _list = list;
        }

        public void startElement (String namespaceURI, String localName,
                                  String qName, Attributes attrs)
        {
            if (qName.equals("library")) {
                String name = null;
                String version = null;
                String url = null;

                for (int i = 0; i < attrs.getLength(); i++) {
                    String attrName = attrs.getQName(i);
                    if (attrName.equals("name")) {
                        name = attrs.getValue(i);
                    } else if (attrName.equals("version")) {
                        version = attrs.getValue(i);
                    } else if (attrName.equals("url")) {
                        url = attrs.getValue(i);
                    }
                }

                if (!validateAttribute("name", name) ||
                    !validateAttribute("version", version) ||
                    !validateAttribute("url", url)) {
                    return;
                }

                try {
                    Dependency dep = new Dependency(
                        name, new Version(version), new URL(url));
                    Log.info("Parsed dependency " + dep + ".");
                    _list.add(dep);

                } catch (MalformedURLException mue) {
                    Log.warning("Skipping dependency with invalid 'url' " +
                                "specification [url=" + url + "]: " + mue);
                }
            }
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

        protected List _list;
    }
}
