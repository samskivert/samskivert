//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2012 Michael Bayne, et al.
// http://github.com/samskivert/samskivert/blob/master/COPYING

package com.samskivert.net;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import java.net.HttpURLConnection;
import java.net.URL;

import java.util.Collections;
import java.util.Map;

import com.samskivert.util.ServiceWaiter;

/**
 * Contains utility methods for doing a form post.
 */
public class HttpPostUtil
{
    /**
     * Return the results of a form post. Note that the http request takes place on another
     * thread, but this thread blocks until the results are returned or it times out. This
     * overload sets a single request property of
     * <code>"Content-Type" = "application/x-www-form-urlencoded"</code>.
     *
     * @param url from which to make the request.
     * @param submission the entire submission eg {@code foo=bar&baz=boo&futz=foo}.
     * @param timeout time to wait for the response, in seconds, or -1 for forever.
     */
    public static String httpPost (URL url, String submission, int timeout)
        throws IOException, ServiceWaiter.TimeoutException
    {
        return httpPost(url, submission, timeout,
            Collections.singletonMap("Content-Type", "application/x-www-form-urlencoded"));
    }

    /**
     * Return the results of a form post. Note that the http request takes place on another
     * thread, but this thread blocks until the results are returned or it times out.
     *
     * @param url from which to make the request.
     * @param submission the entire submission eg {@code foo=bar&baz=boo&futz=foo}.
     * @param timeout time to wait for the response, in seconds, or -1 for forever.
     * @param requestProps additional request properties.
     */
    public static String httpPost (
        final URL url, final String submission, int timeout, final Map<String, String> requestProps)
        throws IOException, ServiceWaiter.TimeoutException
    {
        final ServiceWaiter<String> waiter = new ServiceWaiter<String>(
            (timeout < 0) ? ServiceWaiter.NO_TIMEOUT : timeout);
        Thread tt = new Thread() {
            @Override public void run () {
                try {
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setUseCaches(false);
                    for (Map.Entry<String, String> entry : requestProps.entrySet()) {
                        conn.setRequestProperty(entry.getKey(), entry.getValue());
                    }

                    DataOutputStream out = new DataOutputStream(conn.getOutputStream());
                    out.writeBytes(submission);
                    out.flush();
                    out.close();

                    BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));

                    StringBuilder buf = new StringBuilder();
                    for (String s; null != (s = reader.readLine()); ) {
                        buf.append(s);
                    }
                    reader.close();

                    waiter.postSuccess(buf.toString()); // yay

                } catch (IOException e) {
                    waiter.postFailure(e); // boo
                }
            }
        };

        tt.start();

        if (waiter.waitForResponse()) {
            return waiter.getArgument();
        } else {
            throw (IOException) waiter.getError();
        }
    }
}
