//
// $Id: MessageUtil.java,v 1.1 2003/12/11 06:32:18 mdb Exp $

package com.samskivert.text;

import com.samskivert.util.StringUtil;

/**
 * Utility functions for translation string handling.
 */
public class MessageUtil
{
    /** Text prefixed by this character will be considered tainted when
     * doing recursive translations and won't be translated. */
    public static final String TAINT_CHAR = "~";

    /** Used to mark fully qualified message keys. */
    public static final String QUAL_PREFIX = "%";

    /** Used to separate the bundle qualifier from the message key in a
     * fully qualified message key. */
    public static final String QUAL_SEP = ":";

    /**
     * Call this to "taint" any string that has been entered by an entity
     * outside the application so that the translation code knows not to
     * attempt to translate this string when doing recursive translations
     * (see {@link #xlate}).
     */
    public static String taint (String text)
    {
        return TAINT_CHAR + text;
    }

    /**
     * Composes a message key with an array of arguments. The message can
     * subsequently be translated in a single call using {@link #xlate}.
     */
    public static String compose (String key, String[] args)
    {
        StringBuffer buf = new StringBuffer();
        buf.append(key);
        buf.append('|');
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                buf.append('|');
            }
            // escape the string while adding to the buffer
            String arg = (args[i] == null) ? "" : args[i];
            int alength = arg.length();
            for (int p = 0; p < alength; p++) {
                char ch = arg.charAt(p);
                if (ch == '|') {
                    buf.append("\\!");
                } else if (ch == '\\') {
                    buf.append("\\\\");
                } else {
                    buf.append(ch);
                }
            }
        }
        return buf.toString();
    }

    /**
     * Unescapes characters that are escaped in a call to compose.
     */
    public static String unescape (String value)
    {
        int bsidx = value.indexOf('\\');
        if (bsidx == -1) {
            return value;
        }

        StringBuffer buf = new StringBuffer();
        int vlength = value.length();
        for (int i = 0; i < vlength; i++) {
            char ch = value.charAt(i);
            if (ch != '\\') {
                buf.append(ch);
            } else if (i < vlength-1) {
                // look at the next character
                ch = value.charAt(++i);
                buf.append((ch == '!') ? '|' : ch);
            } else {
                buf.append(ch);
            }
        }

        return buf.toString();
    }

    /**
     * A convenience method for calling {@link #compose(String,String[])}
     * with a single argument.
     */
    public static String compose (String key, String arg)
    {
        return compose(key, new String[] { arg });
    }

    /**
     * A convenience method for calling {@link #compose(String,String[])}
     * with two arguments.
     */
    public static String compose (String key, String arg1, String arg2)
    {
        return compose(key, new String[] { arg1, arg2 });
    }

    /**
     * A convenience method for calling {@link #compose(String,String[])}
     * with three arguments.
     */
    public static String compose (
        String key, String arg1, String arg2, String arg3)
    {
        return compose(key, new String[] { arg1, arg2, arg3 });
    }

    /**
     * A convenience method for calling {@link #compose(String,String[])}
     * with a single argument that will be automatically tainted (see
     * {@link #taint}).
     */
    public static String tcompose (String key, String arg)
    {
        return compose(key, new String[] { taint(arg) });
    }

    /**
     * A convenience method for calling {@link #compose(String,String[])}
     * with two arguments that will be automatically tainted (see {@link
     * #taint}).
     */
    public static String tcompose (String key, String arg1, String arg2)
    {
        return compose(key, new String[] { taint(arg1), taint(arg2) });
    }

    /**
     * A convenience method for calling {@link #compose(String,String[])}
     * with three arguments that will be automatically tainted (see {@link
     * #taint}).
     */
    public static String tcompose (
        String key, String arg1, String arg2, String arg3)
    {
        return compose(key, new String[] {
            taint(arg1), taint(arg2), taint(arg3) });
    }

    /**
     * A convenience method for calling {@link #compose(String,String[])}
     * with an array of arguments that will be automatically tainted (see
     * {@link #taint}).
     */
    public static String tcompose (String key, String[] args)
    {
        int acount = args.length;
        String[] targs = new String[acount];
        for (int ii = 0; ii < acount; ii++) {
            targs[ii] = taint(args[ii]);
        }
        return compose(key, targs);
    }

    /**
     * Returns a fully qualified message key which, when translated by
     * some other bundle, will know to resolve and utilize the supplied
     * bundle to translate this particular key.
     */
    public static String qualify (String bundle, String key)
    {
        // sanity check
        if (bundle.indexOf(QUAL_PREFIX) != -1 ||
            bundle.indexOf(QUAL_SEP) != -1) {
            String errmsg = "Message bundle may not contain '" + QUAL_PREFIX +
                "' or '" + QUAL_SEP + "' [bundle=" + bundle +
                ", key=" + key + "]";
            throw new IllegalArgumentException(errmsg);
        }
        return QUAL_PREFIX + bundle + QUAL_SEP + key;
    }

    /**
     * Returns the bundle name from a fully qualified message key.
     *
     * @see #qualify
     */
    public static String getBundle (String qualifiedKey)
    {
        if (!qualifiedKey.startsWith(QUAL_PREFIX)) {
            throw new IllegalArgumentException(
                qualifiedKey + " is not a fully qualified message key.");
        }

        int qsidx = qualifiedKey.indexOf(QUAL_SEP);
        if (qsidx == -1) {
            throw new IllegalArgumentException(
                qualifiedKey + " is not a valid fully qualified key.");
        }

        return qualifiedKey.substring(QUAL_PREFIX.length(), qsidx);
    }

    /**
     * Returns the unqualified portion of the key from a fully qualified
     * message key.
     *
     * @see #qualify
     */
    public static String getUnqualifiedKey (String qualifiedKey)
    {
        if (!qualifiedKey.startsWith(QUAL_PREFIX)) {
            throw new IllegalArgumentException(
                qualifiedKey + " is not a fully qualified message key.");
        }

        int qsidx = qualifiedKey.indexOf(QUAL_SEP);
        if (qsidx == -1) {
            throw new IllegalArgumentException(
                qualifiedKey + " is not a valid fully qualified key.");
        }

        return qualifiedKey.substring(qsidx+1);
    }

    /**
     * Used to escape single quotes so that they are not interpreted by
     * {@link MessageFormat}. As we assume all single quotes are to be
     * escaped, we cannot use the characters <code>{</code> and
     * <code>}</code> in our translation strings, but this is a small
     * price to pay to have to differentiate between messages that will
     * and won't eventually be parsed by a {@link MessageFormat} instance.
     */
    protected static String escape (String message)
    {
        return StringUtil.replace(message, "'", "''");
    }
}
