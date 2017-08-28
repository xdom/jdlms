package org.openmuc.jdlms;

/**
 * Any COSEM object must inherit this abstract class.
 * 
 * @see CosemClass
 * @see CosemSnInterfaceObject
 */
public abstract class CosemInterfaceObject {

    private final ObisCode instanceId;

    private final DlmsInterceptor interceptor;

    /**
     * Creates a COSEM object with the corresponding instance ID.
     * 
     * @param instanceId
     *            the string instance ID.
     * @param interceptor
     *            the interceptor intercepting xDLMS GET/SET/ACTION services.
     */
    public CosemInterfaceObject(String instanceId, DlmsInterceptor interceptor) {
        this(new ObisCode(instanceId), interceptor);
    }

    /**
     * Creates a COSEM object with the corresponding instance ID.
     * 
     * @param instanceId
     *            the string instance ID.
     */
    public CosemInterfaceObject(String instanceId) {
        this(instanceId, null);
    }

    /**
     * Creates a COSEM object with the corresponding instance ID.
     * 
     * @param instanceId
     *            the OBIS code instance ID.
     * @param interceptor
     *            the interceptor intercepting xDLMS GET/SET/ACTION services.
     */
    public CosemInterfaceObject(ObisCode instanceId, DlmsInterceptor interceptor) {
        this.instanceId = instanceId;
        this.interceptor = interceptor;
    }

    /**
     * Creates a COSEM object with the corresponding instance ID.
     * 
     * @param instanceId
     *            the OBIS code instance ID.
     */
    public CosemInterfaceObject(ObisCode instanceId) {
        this(instanceId, null);
    }

    /**
     * Get the list of DLMS interceptor of the current {@linkplain CosemInterfaceObject}
     * 
     * @return the interceptor intercepting xDLMS GET/SET/ACTION services.
     */
    public final DlmsInterceptor getInterceptor() {
        return this.interceptor;
    }

    /**
     * Get the instance ID of the COSEM class.
     * 
     * @return the instance ID as OBIS code.
     */
    public final ObisCode getInstanceId() {
        return instanceId;
    }

}
