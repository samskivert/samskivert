//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2007 Michael Bayne
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
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import java.util.regex.PatternSyntaxException;
import java.util.regex.Pattern;

import com.samskivert.util.StringUtil;

import static com.samskivert.Log.log;

/**
 * The mail util class encapsulates some utility functions related to
 * sending emails.
 */
public class MailUtil
{
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

    /**
     * @return a normalized form of the specified email address
     * (everything after the @ sign is lowercased).
     */
    public static String normalizeAddress (String address)
    {
        // this algorithm is tolerant of bogus input: if address has no
        // @ symbol it will cope.
        int afterAt = address.indexOf('@') + 1;
        return address.substring(0, afterAt) +
            address.substring(afterAt).toLowerCase();
    }

    /**
     * Delivers the supplied mail message using the machine's local mail
     * SMTP server which must be listening on port 25.
     *
     * @exception IOException thrown if an error occurs delivering the
     * email. See {@link Transport#send} for exceptions that can be thrown
     * due to response from the SMTP server.
     */
    public static void deliverMail (String recipient, String sender,
                                    String subject, String body)
        throws IOException
    {
        deliverMail(new String[] { recipient }, sender, subject, body);
    }

    /**
     * Delivers the supplied mail message using the machine's local mail
     * SMTP server which must be listening on port 25. If the
     * <code>mail.smtp.host</code> system property is set, that will be
     * used instead of localhost.
     *
     * @exception IOException thrown if an error occurs delivering the
     * email. See {@link Transport#send} for exceptions that can be thrown
     * due to response from the SMTP server.
     */
    public static void deliverMail (String[] recipients, String sender,
                                    String subject, String body)
        throws IOException
    {
        deliverMail(recipients, sender, subject, body, null, null);
    }

    /**
     * Delivers the supplied mail, with the specified additional headers.
     */
    public static void deliverMail (String[] recipients, String sender,
                                    String subject, String body,
                                    String[] headers, String[] values)
        throws IOException
    {
        if (recipients == null || recipients.length < 1) {
            throw new IOException("Must specify one or more recipients.");
        }

        try {
            MimeMessage message = createEmptyMessage();
            int hcount = (headers == null) ? 0 : headers.length;
            for (int ii = 0; ii < hcount; ii++) {
                message.addHeader(headers[ii], values[ii]);
            }
            message.setText(body);
            deliverMail(recipients, sender, subject, message);

        } catch (Exception e) {
            String errmsg = "Failure sending mail [from=" + sender +
                ", to=" + StringUtil.toString(recipients) +
                ", subject=" + subject + "]";
            IOException ioe = new IOException(errmsg);
            ioe.initCause(e);
            throw ioe;
        }
    }

    /**
     * Delivers an already-formed message to the specified recipients.  This
     *  message can be any mime type including multipart.
     */
    public static void deliverMail (String[] recipients, String sender,
                                    String subject, MimeMessage message)
        throws IOException
    {
        try {
            message.setFrom(new InternetAddress(sender));
            for (int ii = 0; ii < recipients.length; ii++) {
                message.addRecipient(Message.RecipientType.TO,
                                     new InternetAddress(recipients[ii]));
            }
            if (subject != null) {
                message.setSubject(subject);
            }
            Transport.send(message);

        } catch (Exception e) {
            String errmsg = "Failure sending mail [from=" + sender +
                ", to=" + StringUtil.toString(recipients) +
                ", subject=" + subject + "]";
            IOException ioe = new IOException(errmsg);
            ioe.initCause(e);
            throw ioe;
        }
    }

    /**
     * Returns an initialized, but empty message.
     */
    public static final MimeMessage createEmptyMessage ()
    {
        Properties props = System.getProperties();
        if (props.getProperty("mail.smtp.host") == null) {
            props.put("mail.smtp.host", "localhost");
        }
        return new MimeMessage(Session.getDefaultInstance(props, null));
    }

    /** Originally formulated by lambert@nas.nasa.gov. */
    protected static final String EMAIL_REGEX = "^([-A-Za-z0-9_.!%+]+@" +
        "[-a-zA-Z0-9]+(\\.[-a-zA-Z0-9]+)*\\.[-a-zA-Z0-9]+)$";

    /** A compiled version of our email regular expression. */
    protected static Pattern _emailre;
    static {
        try {
            _emailre = Pattern.compile(EMAIL_REGEX);
        } catch (PatternSyntaxException pse) {
            log.warning("Unable to initialize email regexp?!", pse);
        }
    }
}
