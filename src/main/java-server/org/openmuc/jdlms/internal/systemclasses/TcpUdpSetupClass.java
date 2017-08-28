package org.openmuc.jdlms.internal.systemclasses;

import org.openmuc.jdlms.CosemAttribute;
import org.openmuc.jdlms.CosemClass;
import org.openmuc.jdlms.CosemInterfaceObject;
import org.openmuc.jdlms.ObisCode;
import org.openmuc.jdlms.datatypes.DataObject;

@CosemClass(id = 41, version = 0)
public class TcpUdpSetupClass extends CosemInterfaceObject {

    @CosemAttribute(id = 2)
    private DataObject dlReference;

    @CosemAttribute(id = 3)
    private DataObject ipAddress;

    @CosemAttribute(id = 4)
    private DataObject multicastIpAddress;

    @CosemAttribute(id = 5)
    private DataObject subnetMask;

    @CosemAttribute(id = 6)
    private DataObject gatewayIpAddress;

    public TcpUdpSetupClass(ObisCode instanceId) {
        super(instanceId);
    }

}
