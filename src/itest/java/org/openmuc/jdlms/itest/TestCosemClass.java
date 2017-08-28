package org.openmuc.jdlms.itest;

import java.nio.charset.StandardCharsets;

import org.openmuc.jdlms.CosemAttribute;
import org.openmuc.jdlms.CosemClass;
import org.openmuc.jdlms.CosemInterfaceObject;
import org.openmuc.jdlms.CosemMethod;
import org.openmuc.jdlms.DlmsInterceptor;
import org.openmuc.jdlms.IllegalMethodAccessException;
import org.openmuc.jdlms.MethodResultCode;
import org.openmuc.jdlms.datatypes.DataObject;
import org.openmuc.jdlms.datatypes.DataObject.Type;

@CosemClass(id = TestCosemClass.CLASS_ID)
public class TestCosemClass extends CosemInterfaceObject {

    public static final byte[] D1_DATA = "HELLO WORLD".getBytes(StandardCharsets.UTF_8);
    public static final String ID = "0.0.0.2.1.255";
    public static final int CLASS_ID = 99;

    @CosemAttribute(id = 2, type = Type.OCTET_STRING)
    private DataObject d1;

    public TestCosemClass(DlmsInterceptor interceptor) {
        super(ID, interceptor);
    }

    @CosemMethod(id = 1)
    public void hello() throws IllegalMethodAccessException {
    }

    @CosemMethod(id = 2, consumes = Type.OCTET_STRING)
    public DataObject hello2(DataObject datO) throws IllegalMethodAccessException {
        throw new IllegalMethodAccessException(MethodResultCode.OTHER_REASON);
    }

    public DataObject getD1() {
        return DataObject.newOctetStringData(D1_DATA);
    }

    public void setD1(DataObject d1) {
        byte[] value = d1.getValue();
        System.out.println(new String(value));
        this.d1 = d1;
    }
}
