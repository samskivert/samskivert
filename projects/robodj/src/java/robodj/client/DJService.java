//
// $Id$

package robodj.client;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService;

/**
 * Defines the main RoboDJ services.
 */
public interface DJService extends InvocationService
{
    /** Used to obtain the oid of the DJ object. */
    public void getDJOid (Client client, ResultListener listener);
}
