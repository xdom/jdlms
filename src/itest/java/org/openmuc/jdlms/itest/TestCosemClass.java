/**
 * Copyright 2012-17 Fraunhofer ISE
 * 
 * This file is part of jDLMS.
 * For more information visit http://www.openmuc.org
 * 
 * jDLMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * jDLMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with jDLMS.  If not, see <http://www.gnu.org/licenses/>.
 */
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

    @CosemAttribute(id = 3, type = Type.FLOAT32)
    private DataObject d2;

    @CosemAttribute(id = 4, type = Type.FLOAT64)
    private DataObject d3;

    public TestCosemClass(DlmsInterceptor interceptor) {
        super(ID, interceptor);
    }

    public DataObject getD2() {
        return DataObject.newFloat32Data(99f);
    }

    public DataObject getD3() {
        return DataObject.newFloat64Data(1620d);
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
