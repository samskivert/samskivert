//
// $Id: MailUtil.java,v 1.1 2001/05/26 07:08:00 mdb Exp $

package com.samskivert.net;

import java.io.IOException;
import java.io.PrintWriter;

import org.apache.regexp.*;
import com.samskivert.Log;

/**
 * The mail util class encapsulates some utility functions related to
 * sending emails. The mail delivery mechanism depends on being able to
 * invoke <code>sendmail</code> which probably prevents it from working on
 * anything other than Unix, but maybe some day someone will care enough
 * to make the code do whatever one does on Windows to deliver mail. I
 * don't have the foggiest what that is, so I can't do that currently.
 */
public class MailUtil
{
    /**
     * Delivers the supplied mail message. The message should already
     * contain the mail headers and sendmail will figure everything out
     * from those. At a minimum, these headers should be included:
     *
     * <pre>
     * From: (sender)
     * To: (recipient)
     * Subject: (subject)
     * </pre>
     */
    public void deliverMail (String message)
        throws IOException
    {
        Process p = Runtime.getRuntime().exec("sendmail -t");
        PrintWriter writer = new PrintWriter(p.getOutputStream());
        writer.print(message);
        writer.flush();
        writer.close();

        // some day we should read the response from the mailer to check
        // for errors and also check the Process object for a valid return
        // code. for now we're flying blind!
    }

    /**
     * Checks the supplied address against a regular expression that (for
     * the most part) matches only valid email addresses. It's not
     * perfect, but the ommissions and inclusions are so obscure that we
     * can't be bothered to worry about them.
     */
    public static boolean isValidAddress (String address)
    {
        return _emailre.match(address);
    }

    /** Originally formulated by lambert@nas.nasa.gov. */
    protected static final String EMAIL_REGEX = "^([-A-Za-z0-9_.!%+]+@" +
        "[-a-zA-Z0-9]+(\\.[-a-zA-Z0-9]+)*\\.[-a-zA-Z0-9]+)$";

    /** Used to check email addresses for validity. */
    protected static RE _emailre;
    static {
	try {
	    _emailre = new RE(EMAIL_REGEX);
	} catch (RESyntaxException rese) {
	    Log.warning("Unable to initialize email regexp?! " + rese);
	}
    }
}
