package org.openmuc.jdlms.internal.systemclasses;

import org.openmuc.jdlms.CosemAttribute;
import org.openmuc.jdlms.CosemClass;
import org.openmuc.jdlms.CosemSnInterfaceObject;
import org.openmuc.jdlms.datatypes.DataObject;
import org.openmuc.jdlms.datatypes.DataObject.Type;

@CosemClass(id = 8, version = 0)
public class Clock extends CosemSnInterfaceObject {

    @CosemAttribute(id = 2, type = Type.OCTET_STRING)
    private DataObject time;

    @CosemAttribute(id = 3, type = Type.LONG64) // TODO
    private DataObject timeZone;

    @CosemAttribute(id = 4, type = Type.UNSIGNED)
    private DataObject status;

    @CosemAttribute(id = 5, type = Type.OCTET_STRING)
    private DataObject daylightSavingBegin;

    @CosemAttribute(id = 6, type = Type.OCTET_STRING)
    private DataObject daylightSavingEnd;

    @CosemAttribute(id = 7, type = Type.INTEGER)
    private DataObject daylightSavingDevication;

    @CosemAttribute(id = 8, type = Type.BOOLEAN)
    private DataObject daylightSavingEnabled;

    @CosemAttribute(id = 9, type = Type.ENUMERATE)
    private DataObject clockBase;

    public Clock() {
        super(0x2BC0, "0.0.1.0.0.255");
    }

}
