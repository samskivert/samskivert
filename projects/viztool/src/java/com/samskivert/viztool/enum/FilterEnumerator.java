//
// $Id: FilterEnumerator.java,v 1.1 2001/07/04 18:22:26 mdb Exp $

package com.samskivert.viztool.enum;

/**
 * The filter enumerator provides the framework by which a particular
 * subset of classes can be filtered from the total enumeration of classes
 * in a classpath. This is useful for doing things like only considering
 * classes in a particular package or only considering interfaces, and so
 * on.
 */
public abstract class FilterEnumerator implements Enumerator
{
    /**
     * Constructs a filter enumerator with the supplied class enumerator
     * as the source of classes.
     */
    public FilterEnumerator (ClassEnumerator source)
    {
        _source = source;
        // we'd love to call scanToNextClass() here but that calls
        // filterClass() and it's extremely bad form to call a function
        // provided by a derived class in the super class's constructor
        // because the derived class's constructor hasn't yet been
        // executed. sigh.
    }

    public boolean hasMoreClasses ()
    {
        if (_nextClass == null) {
            scanToNextClass();
        }
        return _nextClass != null;
    }

    public String nextClass ()
    {
        if (_nextClass == null) {
            scanToNextClass();
        }
        String clazz = _nextClass;
        _nextClass = null;
        scanToNextClass();
        return clazz;
    }

    protected void scanToNextClass ()
    {
        while (_source.hasMoreClasses()) {
            String clazz = _source.nextClass();
            if (!filterClass(clazz)) {
                _nextClass = clazz;
                break;
            }
        }
    }

    /**
     * Derived classes should override this method and return true if the
     * specified class should be filtered (meaning it should be excluded
     * from the classes returned) or false if it should be included.
     */
    protected abstract boolean filterClass (String clazz);

    protected ClassEnumerator _source;
    protected String _nextClass;
}
