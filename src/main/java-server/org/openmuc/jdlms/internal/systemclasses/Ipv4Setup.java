package org.openmuc.jdlms.internal.systemclasses;

import org.openmuc.jdlms.CosemAttribute;
import org.openmuc.jdlms.CosemClass;
import org.openmuc.jdlms.CosemInterfaceObject;
import org.openmuc.jdlms.datatypes.DataObject;

@CosemClass(id = 42, version = 0)
public class Ipv4Setup extends CosemInterfaceObject {

    @CosemAttribute(id = 2)
    private DataObject tcpUdpPort;

    @CosemAttribute(id = 3)
    private DataObject ipReference;

    @CosemAttribute(id = 4)
    private DataObject mss;

    @CosemAttribute(id = 5)
    private DataObject nbOfSimConn;

    @CosemAttribute(id = 6)
    private DataObject inactivityTimeOut;

    public Ipv4Setup(String instanceId) {
        super(instanceId);
    }

}
