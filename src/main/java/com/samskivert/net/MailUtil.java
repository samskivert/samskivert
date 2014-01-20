//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.net;

import java.io.IOException;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.event.TransportEvent;
import javax.mail.event.TransportListener;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import java.util.regex.Pattern;

import com.samskivert.util.StringUtil;

import static com.samskivert.net.Log.log;

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
        checkCreateSession();
        try {
            message.setFrom(new InternetAddress(sender));
            for (String recipient : recipients) {
                message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
            }
            if (subject != null) {
                message.setSubject(subject);
            }
            message.saveChanges();
            Address[] recips = message.getAllRecipients();
            if (recips == null || recips.length == 0) {
                log.info("Not sending mail to zero recipients",
                    "subject", subject, "message", message);
                return;
            }
            Transport t = _defaultSession.getTransport(recips[0]);
            try {
                t.addTransportListener(_listener);
                t.connect();
                t.sendMessage(message, recips);

            } finally {
                t.close();
            }

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
        checkCreateSession();
        return new MimeMessage(_defaultSession);
    }

    /**
     * Create our default session if not already created.
     */
    protected static void checkCreateSession ()
    {
        if (_defaultSession == null) { // no need to sync
            Properties props = System.getProperties();
            if (props.getProperty("mail.smtp.host") == null) {
                props.put("mail.smtp.host", "localhost");
            }
            _defaultSession = Session.getDefaultInstance(props, null);
        }
    }

    /** The session for sending our messages. */
    protected static Session _defaultSession;

    /** Listens to mail sending transport events. */
    protected static TransportListener _listener = new TransportListener() {
        public void messageDelivered (TransportEvent event) {
            log.debug("messageDelivered: " + event);
        }
        public void messageNotDelivered (TransportEvent event) {
            logDeliveryError("messageNotDelivered", event);
        }
        public void messagePartiallyDelivered (TransportEvent event) {
            logDeliveryError("messagePartiallyDelivered", event);
        }
        protected void logDeliveryError (String what, TransportEvent event) {
            log.warning(what + ": " + event,
                        "invalidAddresses", event.getInvalidAddresses(),
                        "validUnsentAddresses", event.getValidUnsentAddresses(),
                        "message", event.getMessage());
        }
    };

    /** Originally formulated by lambert@nas.nasa.gov. */
    protected static final String EMAIL_REGEX = "^([-A-Za-z0-9_.!%+]+@" +
        "[-a-zA-Z0-9]+(\\.[-a-zA-Z0-9]+)*\\.[-a-zA-Z0-9]+)$";

    /** A compiled version of our email regular expression. */
    protected static final Pattern _emailre = Pattern.compile(EMAIL_REGEX);
}
