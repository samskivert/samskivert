//
// $Id$
//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2010 Michael Bayne, et al.
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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import java.net.HttpURLConnection;
import java.net.URL;

import com.samskivert.util.ServiceWaiter;

/**
 * Contains utility methods for doing a form post.
 */
public class HttpPostUtil
{
    /**
     * Return the results of a form post. Note that the http request takes
     * place on another thread, but this thread blocks until the results
     * are returned or it times out.
     *
     * @param url from which to make the request.
     * @param submission the entire submission eg "foo=bar&baz=boo&futz=foo".
     * @param timeout time to wait for the response, in seconds, or -1
     * for forever.
     */
    public static String httpPost (final URL url, final String submission,
                                   int timeout)
        throws IOException, ServiceWaiter.TimeoutException
    {
        final ServiceWaiter<String> waiter = new ServiceWaiter<String>(
            (timeout < 0) ? ServiceWaiter.NO_TIMEOUT : timeout);
        Thread tt = new Thread() {
            @Override public void run () {
                try {
                    HttpURLConnection conn =
                        (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true);
                    conn.setDoOutput(true);
                    conn.setUseCaches(false);
                    conn.setRequestProperty(
                        "Content-Type", "application/x-www-form-urlencoded");

                    DataOutputStream out = new DataOutputStream(
                        conn.getOutputStream());
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
