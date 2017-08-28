package org.openmuc.jdlms;

import org.openmuc.jdlms.internal.asn1.axdr.AxdrType;

/**
 * The COSEM resource descriptor. The base class for an attribute address or a method address.
 */
public abstract class CosemResourceDescriptor {

    private final int classId;
    private final ObisCode instanceId;
    private final int id;

    CosemResourceDescriptor(int classId, ObisCode instanceId, int id) {
        this.classId = classId;
        this.instanceId = instanceId;
        this.id = id;
    }

    /**
     * Get the class ID.
     * 
     * @return the int class ID.
     */
    public int getClassId() {
        return classId;
    }

    /**
     * The ID/index of the resource. Method or attribute index.
     * 
     * @return the index.
     */
    public int getId() {
        return id;
    }

    /**
     * Get the instance ID of the interface class.
     * 
     * @return the instance ID.
     */
    public ObisCode getInstanceId() {
        return instanceId;
    }

    abstract AxdrType toDescriptor();
}
