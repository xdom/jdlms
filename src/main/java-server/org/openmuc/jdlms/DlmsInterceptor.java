package org.openmuc.jdlms;

import org.openmuc.jdlms.datatypes.DataObject;

/**
 * A interceptor interface used to intercept xDLMS services of a COSEM object.
 * 
 * @see CosemInterfaceObject#CosemInterfaceObject(ObisCode, DlmsInterceptor)
 * @see CosemInterfaceObject#CosemInterfaceObject(String, DlmsInterceptor)
 */
public interface DlmsInterceptor {
    /**
     * Intercept all xDLMS GET, SET and ACTION services, except the get for attribute ID 1 (logical name/ instance ID).
     * 
     * @param ctx
     *            the invocation context.
     * @return the result of the invocation. Return the result of {@linkplain DlmsInvocationContext#proceed()}
     * @throws DlmsAccessException
     *             if {@linkplain DlmsInvocationContext#proceed()} throws an exception (meaning the actual method), this
     *             can be forwarded.
     */
    DataObject intercept(DlmsInvocationContext ctx) throws DlmsAccessException;
}
