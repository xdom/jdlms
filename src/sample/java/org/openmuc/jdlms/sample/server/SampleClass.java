package org.openmuc.jdlms.sample.server;

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

@CosemClass(id = 99)
public class SampleClass extends CosemInterfaceObject {

    @CosemAttribute(id = 2, type = Type.OCTET_STRING)
    private DataObject d1;

    public SampleClass(DlmsInterceptor interceptor) {
        super("0.0.0.2.1.255", interceptor);
    }

    @CosemMethod(id = 1)
    public void hello() throws IllegalMethodAccessException {
        System.out.println();
        System.out.println("From Cosem Class: Has been called");
        System.out.println();
        return;
    }

    @CosemMethod(id = 2, consumes = Type.OCTET_STRING)
    public DataObject hello2(DataObject datO) throws IllegalMethodAccessException {
        throw new IllegalMethodAccessException(MethodResultCode.OTHER_REASON);
    }

    public DataObject getD1() {
        return DataObject.newOctetStringData("HELLO WORLD".getBytes(StandardCharsets.UTF_8));
    }

    public void setD1(DataObject d1) {
        byte[] value = d1.getValue();
        System.out.println(new String(value));
        this.d1 = d1;
    }
}
