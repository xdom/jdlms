package org.openmuc.jdlms.sample.server;

import static org.openmuc.jdlms.AttributeAccessMode.READ_AND_WRITE;
import static org.openmuc.jdlms.datatypes.DataObject.newOctetStringData;

import org.openmuc.jdlms.CosemAttribute;
import org.openmuc.jdlms.CosemClass;
import org.openmuc.jdlms.CosemMethod;
import org.openmuc.jdlms.CosemSnInterfaceObject;
import org.openmuc.jdlms.ObisCode;
import org.openmuc.jdlms.datatypes.DataObject;
import org.openmuc.jdlms.datatypes.DataObject.Type;

@CosemClass(id = 99, version = 12)
public class SnSampleClass extends CosemSnInterfaceObject {

    public static final ObisCode INSTANCE_ID = new ObisCode("1.11.123.55.1.13");

    @CosemAttribute(id = 2, type = Type.OCTET_STRING, accessMode = READ_AND_WRITE)
    private DataObject d1;

    public SnSampleClass() {
        super(0xA0, INSTANCE_ID.toString());

        byte[] string = new byte[0xFFFF * 3];
        string[0] = 1;
        string[string.length - 1] = 1;
        this.d1 = DataObject.newOctetStringData(string);
    }

    public void setD1(DataObject d1) {
        this.d1 = d1;
        byte[] value = d1.getValue();
        System.out.println(value.length);
        System.out.println(value[0]);
        System.out.println(value[value.length - 1]);
    }

    @CosemMethod(id = 3)
    public DataObject hello() {
        return newOctetStringData("Hello World".getBytes());
    }
}
