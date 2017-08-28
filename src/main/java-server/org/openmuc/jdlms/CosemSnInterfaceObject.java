package org.openmuc.jdlms;

/**
 * COSEM interface object which is necessary to extend, if short naming is used.
 *
 * @see CosemInterfaceObject
 */
public abstract class CosemSnInterfaceObject extends CosemInterfaceObject {

    private static int DEFAULT_FIRST_METHOD_OFFSET = 4;
    private final int objectName;
    private final int firstMethodOffset;

    public CosemSnInterfaceObject(int objectName, String instanceId, DlmsInterceptor interceptor) {
        this(objectName, instanceId, DEFAULT_FIRST_METHOD_OFFSET, interceptor);
    }

    /**
     * 
     * Create a new CosemSnInterfaceObject.
     * 
     * @param objectName
     *            the base name of the object. Addresses the instance ID.
     * @param instanceId
     *            the instance ID of the object.
     * @param firstMethodOffset
     *            the delta, between the last attribute and the first method of the class.
     * 
     *            <p>
     *            This is illustrated with Association SN (id:12, v: 2):
     *            </p>
     * 
     *            <p>
     *            The last attribute has id 4 and offset 0x18 (a). The first method id is 4 with ID is 0x30 (m). The
     *            first method offset is calculated the following way: (m-a)/8. For Association SN this results to:
     *            (0x30-0x18)/8 = (0x18)/8 = 3.
     *            </p>
     * 
     *            <p>
     *            Note: 4 is used as a default value.
     *            </p>
     * @param interceptor
     *            the interceptor for this class.
     */
    public CosemSnInterfaceObject(int objectName, String instanceId, int firstMethodOffset,
            DlmsInterceptor interceptor) {
        super(instanceId, interceptor);
        this.objectName = objectName;
        this.firstMethodOffset = firstMethodOffset;
    }

    /**
     * Create a new CosemSnInterfaceObject.
     * 
     * @param objectName
     *            the base name of the object. Addresses the instance ID.
     * @param instanceId
     *            the instance ID of the object.
     * 
     * @see #CosemSnInterfaceObject(int, String, int, DlmsInterceptor)
     */
    public CosemSnInterfaceObject(int objectName, String instanceId) {
        this(objectName, instanceId, DEFAULT_FIRST_METHOD_OFFSET);
    }

    public CosemSnInterfaceObject(int objectName, String instanceId, int firstMethodOffset) {
        this(objectName, instanceId, firstMethodOffset, null);
    }

    public int getFirstMethodOffset() {
        return firstMethodOffset;
    }

    public final int getObjectName() {
        return this.objectName;
    }

}
