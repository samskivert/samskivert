//
// $Id$

package robodj.server;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationException;
import com.threerings.presents.server.InvocationProvider;

import robodj.client.DJService;

/**
 * Implements the main RoboDJ services.
 */
public class DJProvider implements InvocationProvider
{
    /** Used to obtain the oid of the DJ object. */
    public void getDJOid (ClientObject caller,
                          DJService.ResultListener listener)
        throws InvocationException
    {
        listener.requestProcessed(new Integer(MusicDaemon.djobj.getOid()));
    }
}
