/*
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
 *
 */
package org.openmuc.jdlms.app.client;

import static org.openmuc.jdlms.internal.WellKnownInstanceIds.CURRENT_ASSOCIATION_ID;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.openmuc.jdlms.AccessResultCode;
import org.openmuc.jdlms.AttributeAddress;
import org.openmuc.jdlms.DlmsConnection;
import org.openmuc.jdlms.GetResult;
import org.openmuc.jdlms.ObisCode;
import org.openmuc.jdlms.ResponseTimeoutException;
import org.openmuc.jdlms.datatypes.DataObject;
import org.openmuc.jdlms.interfaceclass.InterfaceClass;
import org.openmuc.jdlms.interfaceclass.attribute.AssociationSnAttribute;

class SnActionProcessor extends GenActionProcessor {
    private static final String LOGICAL_NAME_FORMAT = "<Interface_Class_ID>/<OBIS_Code>/<Object_Attribute_ID>";

    private final DlmsConnection connection;

    private boolean closed;

    public SnActionProcessor(DlmsConnection connection) {
        this.connection = connection;
        this.closed = false;
    }

    @Override
    public void close() {
        try {
            if (!closed) {
                connection.close();
                closed = true;
            }
        } catch (IOException e) {
            System.out.println("Error occured while closing.");
        }
    }

    @Override
    protected String nameFormat() {
        return LOGICAL_NAME_FORMAT;
    }

    @Override
    protected GetResult callGet(String requestParameter) throws IOException, IllegalArgumentException {
        AttributeAddress attributeAddress;

        try {
            attributeAddress = buidlAttributeAddress(requestParameter);
        } catch (IllegalArgumentException e) {
            // TODO: handle this better
            // System.err.println(e.getMessage());
            // return;
            throw e;
        }
        return connection.get(attributeAddress);
    }

    private AttributeAddress buidlAttributeAddress(String requestParameter)
            throws IllegalArgumentException, NumberFormatException {
        String[] arguments = requestParameter.split("/");

        if (arguments.length != 3) {
            throw new IllegalArgumentException(String.format("Wrong number of arguments. %s", LOGICAL_NAME_FORMAT));
        }

        int classId = Integer.parseInt(arguments[0]);

        ObisCode obisCode = new ObisCode(arguments[1]);

        int attributeId = Integer.parseInt(arguments[2]);

        return new AttributeAddress(classId, obisCode, attributeId);
    }

    @Override
    public void processScanObjects() throws IOException {
        AttributeAddress snObjects = new AttributeAddress(AssociationSnAttribute.OBJECT_LIST,
                new ObisCode(CURRENT_ASSOCIATION_ID));
        AttributeAddress snAccessRights = new AttributeAddress(AssociationSnAttribute.ACCESS_RIGHTS_LIST,
                new ObisCode(CURRENT_ASSOCIATION_ID));

        GetResult objectsResult;
        GetResult accessRightsResult;
        try {
            List<GetResult> list = connection.get(Arrays.asList(snObjects, snAccessRights));
            objectsResult = list.get(0);
            accessRightsResult = list.get(1);
        } catch (ResponseTimeoutException e) {
            System.err.println("Failed to scan: Timeout");
            return;
        }

        if (objectsResult.getResultCode() != AccessResultCode.SUCCESS) {
            System.err.printf("Failed to scan with following result code %s.\n", objectsResult.getResultCode().name());
            return;
        }
        System.out.println(accessRightsResult.getResultCode());

        List<DataObject> objectList = objectsResult.getResultData().getValue();

        final String formatStr = "%-30s %-10s %-20s %s\n";
        System.out.printf(formatStr, "Class_ID", "Short Name", "Obis Code", "Version");
        for (DataObject objectListElement : objectList) {
            List<DataObject> struct = objectListElement.getValue();

            Number baseName = struct.get(0).getValue();
            Number classId = struct.get(1).getValue();
            classId = classId.intValue() & 0xFFFFFFFFL;
            Number version = struct.get(2).getValue();

            byte[] logNameBytes = struct.get(3).getValue();

            ObisCode obisCode = new ObisCode(logNameBytes);

            InterfaceClass interfaceClass = InterfaceClass.interfaceClassFor(classId.intValue());

            String interfaceClassStr;
            if (interfaceClass != InterfaceClass.UNKNOWN) {
                interfaceClassStr = String.format("%s(%d)", interfaceClass.name(), classId);
            }
            else {
                interfaceClassStr = classId.toString();
            }

            System.out.printf(formatStr, interfaceClassStr, String.format("%04X", baseName.intValue() & 0xFFFF),
                    obisCode.toString(), version);

        }
        // DataObject mParam = newArrayData(Arrays.asList(
        // newStructureData(newUInteger16Data(12), newOctetStringData(new ObisCode("0.0.40.0.0.255").bytes()))));
        //
        // MethodParameter params = new MethodParameter(12, "0.0.40.0.0.255", 4, mParam);
        //
        // MethodResult action = this.connection.action(params);
        //
        // if (action.getResultCode() == MethodResultCode.SUCCESS) {
        // System.out.println(action.getResultData());
        // }
    }

    @Override
    protected AccessResultCode callSet(String requestParameter, DataObject dataToWrite) throws IOException {
        throw new UnsupportedOperationException("Set operation is not yet implemented for SN console APP..");
    }

}
