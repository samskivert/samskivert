//
// $Id$

package robodj.server;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService.ResultListener;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.server.InvocationDispatcher;
import com.threerings.presents.server.InvocationException;
import robodj.client.DJService;
import robodj.data.DJMarshaller;

/**
 * Dispatches requests to the {@link DJProvider}.
 */
public class DJDispatcher extends InvocationDispatcher
{
    /**
     * Creates a dispatcher that may be registered to dispatch invocation
     * service requests for the specified provider.
     */
    public DJDispatcher (DJProvider provider)
    {
        this.provider = provider;
    }

    // documentation inherited
    public InvocationMarshaller createMarshaller ()
    {
        return new DJMarshaller();
    }

    // documentation inherited
    public void dispatchRequest (
        ClientObject source, int methodId, Object[] args)
        throws InvocationException
    {
        switch (methodId) {
        case DJMarshaller.GET_DJOID:
            ((DJProvider)provider).getDJOid(
                source,
                (ResultListener)args[0]
            );
            return;

        default:
            super.dispatchRequest(source, methodId, args);
        }
    }
}
