//
// $Id: MailUtil.java,v 1.7 2004/01/15 16:56:02 eric Exp $
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001 Michael Bayne
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

package com.samskivert.net;

import java.io.IOException;
import java.io.PrintWriter;

import java.util.regex.PatternSyntaxException;
import java.util.regex.Pattern;

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
     * Delivers the supplied mail message and returns whether the mail
     * delivery attempt was successful. The message should already contain
     * the mail headers and sendmail will figure everything out from
     * those. At a minimum, these headers should be included:
     *
     * <pre>
     * From: (sender)
     * To: (recipient)
     * Subject: (subject)
     * </pre>
     *
     * Note that this method will block until the sendmail process has
     * completed all of its business.
     *
     * @return true if the mail was delivered successfully, false if not.
     */
    public static boolean deliverMail (String message)
        throws IOException
    {
        Process p;
        try {
            p = Runtime.getRuntime().exec(SENDMAIL_COMMAND);
        } catch (Exception e) {
            // try try again, with the full path. Environment be damned!
            p = Runtime.getRuntime().exec(FULL_SENDMAIL_COMMAND);
        }
        
        PrintWriter writer = new PrintWriter(p.getOutputStream());
        writer.print(message);
        writer.flush();
        writer.close();

        // make sure sendmail and the process both exit cleanly
        try {
            return (p.waitFor() == 0);
        } catch (InterruptedException ie) {
            return false;
        }
    }

    /**
     * Checks the supplied address against a regular expression that (for
     * the most part) matches only valid email addresses. It's not
     * perfect, but the omissions and inclusions are so obscure that we
     * can't be bothered to worry about them.
     */
    public static boolean isValidAddress (String address)
    {
        return _emailre.matcher(address).matches();
    }

    /** Command used to execute the program with which mail is sent. */
    protected static final String SENDMAIL_COMMAND = "sendmail -t";

    /** Full path to the command to execute the program to send mail. */
    protected static final String FULL_SENDMAIL_COMMAND =
        "/usr/sbin/sendmail -t";
        
    /** Originally formulated by lambert@nas.nasa.gov. */
    protected static final String EMAIL_REGEX = "^([-A-Za-z0-9_.!%+]+@" +
        "[-a-zA-Z0-9]+(\\.[-a-zA-Z0-9]+)*\\.[-a-zA-Z0-9]+)$";

    /** A compiled version of our email regular expression. */
    protected static Pattern _emailre;
    static {
        try {
            _emailre = Pattern.compile(EMAIL_REGEX);
        } catch (PatternSyntaxException pse) {
            Log.warning("Unable to initialize email regexp?!");
            Log.logStackTrace(pse);
        }
    }

    public static void main (String[] args)
    {
        String msg = "To: eric@threerings.com, eric@radiantenergy.org\n" +
            "Subject: test\n\n" +
            "Body of message is here. Yiza!\n";
        try {
            MailUtil.deliverMail(msg);
        } catch (Exception e) {
            System.err.println("Le Booch Sending Mail [exception=" + e + "].");
        }
    }
}
