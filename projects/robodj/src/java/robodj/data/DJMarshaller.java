//
// $Id$

package robodj.data;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService.ResultListener;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.dobj.InvocationResponseEvent;
import robodj.client.DJService;

/**
 * Provides the implementation of the {@link DJService} interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 */
public class DJMarshaller extends InvocationMarshaller
    implements DJService
{
    /** The method id used to dispatch {@link #getDJOid} requests. */
    public static final int GET_DJOID = 1;

    // documentation inherited from interface
    public void getDJOid (Client arg1, ResultListener arg2)
    {
        ResultMarshaller listener2 = new ResultMarshaller();
        listener2.listener = arg2;
        sendRequest(arg1, GET_DJOID, new Object[] {
            listener2
        });
    }

}
