package org.openmuc.jdlms.internal.systemclasses;

import org.openmuc.jdlms.AttributeAccessMode;
import org.openmuc.jdlms.CosemAttribute;
import org.openmuc.jdlms.CosemClass;
import org.openmuc.jdlms.CosemSnInterfaceObject;
import org.openmuc.jdlms.datatypes.DataObject;
import org.openmuc.jdlms.datatypes.DataObject.Type;

@CosemClass(id = 1, version = 0)
public class ReadOnlyOctetStrData extends CosemSnInterfaceObject {

    @CosemAttribute(id = 2, type = Type.OCTET_STRING, accessMode = AttributeAccessMode.READ_ONLY)
    private final DataObject value;

    public ReadOnlyOctetStrData(DataObject value, String instanceId, int baseName) {
        super(baseName, instanceId);
        this.value = value;
    }

    public DataObject getValue() {
        return this.value;
    }

}
