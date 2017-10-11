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
package org.openmuc.jdlms.app.server;

import static org.openmuc.jdlms.AttributeAccessMode.READ_ONLY;
import static org.openmuc.jdlms.MethodResultCode.OTHER_REASON;
import static org.openmuc.jdlms.datatypes.DataObject.Type.OCTET_STRING;

import org.openmuc.jdlms.CosemAttribute;
import org.openmuc.jdlms.CosemClass;
import org.openmuc.jdlms.CosemInterfaceObject;
import org.openmuc.jdlms.CosemMethod;
import org.openmuc.jdlms.DlmsInterceptor;
import org.openmuc.jdlms.IllegalMethodAccessException;
import org.openmuc.jdlms.SelectiveAccessDescription;
import org.openmuc.jdlms.datatypes.DataObject;

@CosemClass(id = 99)
public class SampleClass extends CosemInterfaceObject {

    @CosemAttribute(id = 2, type = OCTET_STRING, selector = 1, accessMode = READ_ONLY)
    private DataObject d1;

    public SampleClass(DlmsInterceptor interceptor) {
        super("0.0.0.2.1.255", interceptor);
    }

    @CosemMethod(id = 1)
    public void hello(Long conId) throws IllegalMethodAccessException {
        System.out.println(conId);
        System.out.println("Has been called");
        return;
    }

    @CosemMethod(id = 2, consumes = OCTET_STRING)
    public DataObject hello2(DataObject datO) throws IllegalMethodAccessException {
        throw new IllegalMethodAccessException(OTHER_REASON);
    }

    public DataObject getD1(SelectiveAccessDescription sel) {
        return DataObject.newOctetStringData("HELLO WORLD".getBytes());
    }

    public void setD1(DataObject d1, SelectiveAccessDescription sel, Long connId) {
        byte[] value = d1.getValue();
        System.out.println(new String(value));
        this.d1 = d1;
    }
}
