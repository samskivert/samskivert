//
// samskivert library - useful routines for java programs
// Copyright (C) 2001-2011 Michael Bayne, et al.
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

package com.samskivert.util;

/**
 * Provides access to an integer result, or the exception associated with
 * failure. In the trying course of implementing a method, one is often
 * left with no choice but to foist control off onto another thread or
 * generally postpone things beyond the lifetime of the current call. In
 * such circumstances, it is handy to have a general purpose mechanism for
 * leting the caller know when things are done and whether or not the
 * succeeded; this class serves that purpose.
 *
 * <p> The following contrived example will hopefully communicate its use
 * more clearly than the previous paragraph of flowery prose:
 *
 * <pre>
 * public void doSomeStuff (IntResultListener listener)
 * {
 *     Runnable run = new Runnable () {
 *         public void run () {
 *             try {
 *                 // do our thing
 *                 listener.requestCompleted(42);
 *             } catch (Exception e) {
 *                 listener.requestFailed(e);
 *             }
 *         }
 *     };
 *     new Thread(run).start();
 * }
 * </pre>
 *
 * This interface is a convenience-variation on the plain old {@link
 * ResultListener}, which communicates the result back in the form of an
 * {@link Object} rather than an integer.
 */
public interface IntResultListener
{
    /**
     * Called to communicate that the request succeeded and that the
     * result is available.
     */
    public void requestCompleted (int result);

    /**
     * Called to communicate that the request failed and to provide the
     * reason for failure.
     */
    public void requestFailed (Exception cause);
}
