//
// $Id: AttachableURLFactory.java,v 1.1 2003/07/09 18:44:11 ray Exp $

package com.samskivert.net;

import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

import java.util.HashMap;

import com.samskivert.Log;

/**
 * Allows other entities in an application to register URLStreamHandler
 * classes for protocols of their own making.
 */
public class AttachableURLFactory implements URLStreamHandlerFactory
{
    /**
     * Register a URL handler.
     *
     * @param protocol the protocol to register.
     * @param handlerClass a Class of type java.net.URLStreamHandler
     */
    public static void attachHandler (String protocol, Class handlerClass)
    {
        if (!URLStreamHandler.class.isAssignableFrom(handlerClass)) {
            throw new IllegalArgumentException(
                "Specified class is not a java.net.URLStreamHandler.");
        }

        // set up the factory.
        if (_handlers == null) {
            _handlers = new HashMap();

            // this could throw an Error if another factory is already
            // registered. We let that error bubble on back.
            URL.setURLStreamHandlerFactory(new AttachableURLFactory());
        }

        _handlers.put(protocol.toLowerCase(), handlerClass);
    }

    /**
     * Do not let others instantiate us.
     */
    private AttachableURLFactory ()
    {
    }

    // documentation inherited from interface URLStreamHandlerFactory
    public URLStreamHandler createURLStreamHandler (String protocol)
    {
        Class handler = (Class) _handlers.get(protocol.toLowerCase());
        if (handler != null) {
            try {
                return (URLStreamHandler) handler.newInstance();
            } catch (Exception e) {
                Log.warning("Unable to instantiate URLStreamHandler" +
                    " [protocol=" + protocol + ", cause=" + e + "].");
            }
        }
        return null;
    }

    /** A mapping of protocol name to handler classes. */
    protected static HashMap _handlers;
}
