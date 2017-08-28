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

import java.io.IOException;
import java.util.List;

import org.openmuc.jdlms.AccessResultCode;
import org.openmuc.jdlms.AttributeAddress;
import org.openmuc.jdlms.DlmsConnection;
import org.openmuc.jdlms.GetResult;
import org.openmuc.jdlms.ObisCode;
import org.openmuc.jdlms.ResponseTimeoutException;
import org.openmuc.jdlms.SetParameter;
import org.openmuc.jdlms.datatypes.DataObject;
import org.openmuc.jdlms.interfaceclass.InterfaceClass;
import org.openmuc.jdlms.interfaceclass.attribute.AssociationLnAttribute;
import org.openmuc.jdlms.interfaceclass.attribute.AttributeClass;
import org.openmuc.jdlms.interfaceclass.attribute.AttributeDirectory;
import org.openmuc.jdlms.interfaceclass.attribute.AttributeDirectory.AttributeNotFoundException;
import org.openmuc.jdlms.interfaceclass.method.MethodDirectory;
import org.openmuc.jdlms.interfaceclass.method.MethodDirectory.MethodNotFoundException;

class LnActionProcessor extends GenActionProcessor {

    private static final String SCAN_FORMAT = "%-30s%-40s%-25s%s%n";

    private static final String LOGICAL_NAME_FORMAT = "<Interface_Class_ID>/<OBIS_Code>/<Object_Attribute_ID>";

    private final DlmsConnection connection;

    private boolean closed;

    public LnActionProcessor(DlmsConnection connection) {
        this.connection = connection;
        this.closed = false;
    }

    @Override
    public synchronized void close() {
        try {
            if (!closed) {
                this.connection.close();
            }
        } catch (IOException e) {
            System.err.println("Error occurred, while closing.");
            e.printStackTrace();
        } finally {
            super.close();
            this.closed = true;
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
            throw e;
        }
        return connection.get(attributeAddress);
    }

    private AttributeAddress buidlAttributeAddress(String requestParameter) throws IllegalArgumentException {
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
    protected AccessResultCode callSet(String requestParameter, DataObject dataToWrite) throws IOException {
        AttributeAddress attributeAddress = buidlAttributeAddress(requestParameter);

        return connection.set(new SetParameter(attributeAddress, dataToWrite));
    }

    @Override
    public void processScanObjects() throws IOException {
        AttributeAddress scanChannel = new AttributeAddress(AssociationLnAttribute.OBJECT_LIST, "0.0.40.0.0.255");

        GetResult scanResult;
        try {
            scanResult = connection.get(scanChannel);
        } catch (ResponseTimeoutException e) {
            System.err.println("Failed to scan: timeout");
            return;
        }

        if (scanResult.getResultCode() != AccessResultCode.SUCCESS) {
            System.err.println("Device sent error code " + scanResult.getResultCode().name());
            return;
        }

        DataObject root = scanResult.getResultData();
        List<DataObject> objectArray = root.getValue();
        System.out.println("Scanned addresses:");

        System.out.printf(SCAN_FORMAT, "Address", "Description", "Access Mode", "Access Selector");

        for (DataObject objectDef : objectArray) {
            List<DataObject> defItems = objectDef.getValue();
            Integer classId = defItems.get(0).getValue();
            classId &= 0xFF;
            // Number version = defItems.get(1)
            // .getValue();
            byte[] logicalName = defItems.get(2).getValue();
            ObisCode obisCode = new ObisCode(logicalName);
            List<DataObject> accessRight = defItems.get(3).getValue();
            List<DataObject> attributes = accessRight.get(0).getValue();

            List<DataObject> methods = accessRight.get(1).getValue();

            InterfaceClass interfaceClass = InterfaceClass.interfaceClassFor(classId);

            String classIdStr = interfaceClass.name();

            System.out.printf("%-13s %s%n", obisCode.medium(), classIdStr);

            printAttributes(classId, interfaceClass, obisCode, attributes);

            if (!methods.isEmpty()) {
                printMethods(classId, interfaceClass, obisCode, methods);
            }

            System.out.println();
        }

    }

    private void printAttributes(int classId, InterfaceClass interfaceClass, ObisCode obisCode,
            List<DataObject> attributes) {
        System.out.println("Attributes:");
        for (DataObject attributeAccess : attributes) {
            List<DataObject> value = attributeAccess.getValue();
            Number attributeId = value.get(0).getValue();

            Number accessModeI = value.get(1).getValue();
            AttributeAccessMode accessMode = AttributeAccessMode.accessModeFor(accessModeI.intValue() & 0xFF);

            DataObject accessSelectors = value.get(2);

            String attributeIdStr;

            Number intValue = attributeId.intValue();
            int attributeId2 = intValue.intValue() & 0xFF;
            try {
                AttributeClass attributeClass = AttributeDirectory.attributeClassFor(interfaceClass, attributeId2);

                attributeIdStr = String.format("%s(%d)", attributeClass.attributeName(), attributeClass.attributeId());
            } catch (AttributeNotFoundException e) {
                attributeIdStr = String.valueOf(attributeId2);
            }

            StringBuilder selectiveAccessB = new StringBuilder();
            if (accessSelectors.isNull()) {
                selectiveAccessB.append('-');
            }
            else {
                List<DataObject> slectors = accessSelectors.getValue();
                for (DataObject selector : slectors) {
                    Number sNumber = selector.getValue();
                    selectiveAccessB.append(String.format("%d, ", sNumber.intValue()));
                }
            }

            String attributeAddress = String.format("%d/%s/%d", classId, obisCode.toString(),
                    attributeId.intValue() & 0xFF);
            System.out.printf(SCAN_FORMAT, attributeAddress, attributeIdStr, accessMode, selectiveAccessB.toString());
        }
    }

    private void printMethods(int classId, InterfaceClass interfaceClass, ObisCode obisCode, List<DataObject> methods) {
        System.out.println("Methods:");

        for (DataObject dataObject : methods) {
            List<DataObject> methodAccessItem = dataObject.getValue();
            Number methodId = methodAccessItem.get(0).getValue();

            DataObject methodAccess = methodAccessItem.get(1);
            MethodAccessMode methodAccessMode;
            if (methodAccess.isBoolean()) {
                Boolean accessMethod = methodAccess.getValue();
                methodAccessMode = MethodAccessMode.accessModeFor(accessMethod);
            }
            else {
                Number accessMethod = methodAccess.getValue();
                methodAccessMode = MethodAccessMode.accessModeFor(accessMethod.intValue());
            }
            String methodAddress = String.format("%d/%s/%d", classId, obisCode.toString(), methodId.intValue() & 0xFF);

            String methoIdStr;

            try {
                methoIdStr = MethodDirectory.methodClassFor(interfaceClass, methodId.intValue()).getMethodName();
            } catch (MethodNotFoundException e) {
                methoIdStr = "";
            }
            System.out.printf(SCAN_FORMAT, methodAddress, methoIdStr, methodAccessMode, "");

        }
    }

    private enum AttributeAccessMode {
        NO_ACCESS(0),
        READ_ONLY(1),
        WRITE_ONLY(2),
        READ_AND_WRITE(3),
        AUTHENTICATED_READ_ONLY(4),
        AUTHENTICATED_WRITE_ONLY(5),
        AUTHENTICATED_READ_AND_WRITE(6),

        UNKNOWN_ACCESS_MODE(-1);

        private int code;

        private AttributeAccessMode(int code) {
            this.code = code;
        }

        public static AttributeAccessMode accessModeFor(int code) {
            for (AttributeAccessMode accessMode : values()) {
                if (accessMode.code == code) {
                    return accessMode;
                }
            }

            return UNKNOWN_ACCESS_MODE;
        }

    }

    private enum MethodAccessMode {
        NO_ACCESS(0),
        ACCESS(1),
        AUTHENTICATED_ACCESS(2),

        UNKNOWN_ACCESS_MODE(-1);

        private int code;

        private MethodAccessMode(int code) {
            this.code = code;
        }

        public static MethodAccessMode accessModeFor(boolean value) {
            return accessModeFor(value ? 1 : 0);
        }

        public static MethodAccessMode accessModeFor(int code) {
            for (MethodAccessMode accessMode : values()) {
                if (accessMode.code == code) {
                    return accessMode;
                }
            }

            return UNKNOWN_ACCESS_MODE;
        }

    }

}
