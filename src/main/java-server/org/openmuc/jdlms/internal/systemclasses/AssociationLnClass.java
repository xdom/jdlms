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
package org.openmuc.jdlms.internal.systemclasses;

import static java.util.Arrays.asList;
import static org.openmuc.jdlms.AccessResultCode.TYPE_UNMATCHED;
import static org.openmuc.jdlms.AttributeAccessMode.AUTHENTICATED_READ_AND_WRITE;
import static org.openmuc.jdlms.AttributeAccessMode.READ_ONLY;
import static org.openmuc.jdlms.datatypes.DataObject.newArrayData;
import static org.openmuc.jdlms.datatypes.DataObject.newInteger8Data;
import static org.openmuc.jdlms.datatypes.DataObject.newNullData;
import static org.openmuc.jdlms.datatypes.DataObject.newOctetStringData;
import static org.openmuc.jdlms.datatypes.DataObject.newStructureData;
import static org.openmuc.jdlms.datatypes.DataObject.newUInteger16Data;
import static org.openmuc.jdlms.datatypes.DataObject.newUInteger8Data;
import static org.openmuc.jdlms.datatypes.DataObject.Type.ARRAY;
import static org.openmuc.jdlms.datatypes.DataObject.Type.ENUMERATE;
import static org.openmuc.jdlms.datatypes.DataObject.Type.LONG_UNSIGNED;
import static org.openmuc.jdlms.datatypes.DataObject.Type.OCTET_STRING;
import static org.openmuc.jdlms.datatypes.DataObject.Type.STRUCTURE;
import static org.openmuc.jdlms.internal.WellKnownInstanceIds.SECURITY_SETUP_ID;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openmuc.jdlms.AuthenticationMechanism;
import org.openmuc.jdlms.ConformanceSetting;
import org.openmuc.jdlms.CosemAttribute;
import org.openmuc.jdlms.CosemClass;
import org.openmuc.jdlms.CosemInterfaceObject;
import org.openmuc.jdlms.CosemMethod;
import org.openmuc.jdlms.IllegalAttributeAccessException;
import org.openmuc.jdlms.IllegalMethodAccessException;
import org.openmuc.jdlms.LogicalDevice;
import org.openmuc.jdlms.MethodAccessMode;
import org.openmuc.jdlms.MethodResultCode;
import org.openmuc.jdlms.ObisCode;
import org.openmuc.jdlms.SecuritySuite;
import org.openmuc.jdlms.SelectiveAccessDescription;
import org.openmuc.jdlms.datatypes.DataObject;
import org.openmuc.jdlms.datatypes.DataObject.Type;
import org.openmuc.jdlms.internal.ConformanceSettingConverter;
import org.openmuc.jdlms.internal.DataDirectoryImpl;
import org.openmuc.jdlms.internal.DataDirectoryImpl.Attribute;
import org.openmuc.jdlms.internal.DataDirectoryImpl.CosemClassInstance;
import org.openmuc.jdlms.internal.DataDirectoryImpl.CosemLogicalDevice;
import org.openmuc.jdlms.internal.MethodAccessor;
import org.openmuc.jdlms.internal.ServerConnectionData;
import org.openmuc.jdlms.internal.WellKnownInstanceIds;
import org.openmuc.jdlms.internal.asn1.cosem.Conformance;
import org.openmuc.jdlms.internal.security.HlsProcessorGmac;
import org.openmuc.jdlms.internal.security.HlsSecretProcessor;

@CosemClass(id = 15, version = 1)
public class AssociationLnClass extends CosemInterfaceObject {

    private static final int FILTER_ACCESS_RIGHTS = 1;
    private static final int FILTER_CLASS_LIST = 2;
    private static final int FILTER_OBJECT_ID_LIST = 3;
    private static final int FILTER_OBJECT_ID = 4;

    @CosemAttribute(id = 2, accessMode = READ_ONLY, type = ARRAY, selector = { FILTER_ACCESS_RIGHTS, FILTER_CLASS_LIST,
            FILTER_OBJECT_ID_LIST, FILTER_OBJECT_ID })
    private DataObject objectList;

    @CosemAttribute(id = 3, accessMode = READ_ONLY, type = STRUCTURE)
    private DataObject associationPartners;

    @CosemAttribute(id = 4, accessMode = READ_ONLY)
    private DataObject applicationContextName;

    @CosemAttribute(id = 5, accessMode = READ_ONLY)
    private DataObject authenticationMechanism;

    @CosemAttribute(id = 6, accessMode = READ_ONLY, type = STRUCTURE)
    private DataObject xDlmsContextInfo;

    @CosemAttribute(id = 7, accessMode = AUTHENTICATED_READ_AND_WRITE, type = OCTET_STRING)
    private DataObject secret;

    @CosemAttribute(id = 8, accessMode = READ_ONLY, type = ENUMERATE)
    private DataObject assotiationStatus;

    @CosemAttribute(id = 9, accessMode = READ_ONLY, type = OCTET_STRING)
    private final DataObject securitySetupReference;

    @CosemDataDirectory
    private DataDirectoryImpl dataDirectory;

    private final int logicalDeviceId;

    public AssociationLnClass(int logicalDeviceId) {
        super(WellKnownInstanceIds.CURRENT_ASSOCIATION_ID);
        this.logicalDeviceId = logicalDeviceId;

        this.securitySetupReference = DataObject.newOctetStringData(new ObisCode(SECURITY_SETUP_ID).bytes());
    }

    public DataObject getObjectList(SelectiveAccessDescription sel) throws IllegalAttributeAccessException {
        if (sel == null) {
            if (this.objectList != null) {
                return objectList;
            }

            boolean excludeAccessRights = false;
            this.objectList = buildFullObjectList(excludeAccessRights);
            return this.objectList;
        }

        return filterObjectListBy(sel);

    }

    private DataObject buildFullObjectList(boolean excludeAccessRights) {
        return buildObjectsList(excludeAccessRights, new Filter() {
            @Override
            public boolean passes(CosemClassInstance cosemClassInstance) {
                return true;
            }

        });
    }

    private DataObject filterObjectListBy(SelectiveAccessDescription sel) throws IllegalAttributeAccessException {
        switch (sel.getAccessSelector()) {
        case FILTER_ACCESS_RIGHTS:
            boolean excludeAccessRights = true;
            return buildFullObjectList(excludeAccessRights);
        case FILTER_CLASS_LIST:
            return filterForClassList(sel.getAccessParameter());
        case FILTER_OBJECT_ID_LIST:
            return filterForObjectIdList(sel.getAccessParameter());
        case FILTER_OBJECT_ID:
            return filterForObjectId(sel.getAccessParameter());

        default:
            throw new IllegalAttributeAccessException(TYPE_UNMATCHED);
        }
    }

    private DataObject filterForObjectId(DataObject accessParameter) throws IllegalAttributeAccessException {
        List<DataObject> struct = extractObjectIdStruct(accessParameter);

        DataObject classIdDo = struct.get(0);
        DataObject logicalNameDo = struct.get(1);

        if (classIdDo.getType() != LONG_UNSIGNED || logicalNameDo.getType() != OCTET_STRING) {
            throw new IllegalAttributeAccessException(TYPE_UNMATCHED);
        }
        CosemLogicalDevice logicalDevice = this.dataDirectory.getLogicalDeviceFor(this.logicalDeviceId);

        byte[] logicalName = logicalNameDo.getValue();
        CosemClassInstance cosemClassInstance = logicalDevice.get(new ObisCode(logicalName));

        Integer classId = classIdDo.getValue();

        if (cosemClassInstance.getCosemClass().id() == classId) {
            return newNullData();
        }

        boolean excludeAccessRights = false;
        return buildObjectListElement(excludeAccessRights, cosemClassInstance);
    }

    private static List<DataObject> extractObjectIdStruct(DataObject dataObject)
            throws IllegalAttributeAccessException {
        if (dataObject.getType() != Type.STRUCTURE) {
            throw new IllegalAttributeAccessException(TYPE_UNMATCHED);
        }
        List<DataObject> struct = dataObject.getValue();

        if (struct.size() != 2) {
            throw new IllegalAttributeAccessException(TYPE_UNMATCHED);
        }
        return struct;
    }

    private DataObject filterForObjectIdList(DataObject accessParameter) throws IllegalAttributeAccessException {
        if (accessParameter.getType() != Type.ARRAY) {
            throw new IllegalAttributeAccessException(TYPE_UNMATCHED);
        }

        List<DataObject> objectIds = accessParameter.getValue();

        if (objectIds.isEmpty()) {
            return newNullData();
        }

        final Map<ObisCode, Integer> instanceIdClassIdMap = new HashMap<>(objectIds.size());
        for (DataObject structDo : objectIds) {
            List<DataObject> struct = extractObjectIdStruct(structDo);

            DataObject classIdDo = struct.get(0);
            DataObject logicalNameDo = struct.get(1);

            if (classIdDo.getType() != LONG_UNSIGNED || logicalNameDo.getType() != OCTET_STRING) {
                throw new IllegalAttributeAccessException(TYPE_UNMATCHED);
            }

            Integer classId = classIdDo.getValue();
            byte[] instanceId = logicalNameDo.getValue();
            instanceIdClassIdMap.put(new ObisCode(instanceId), classId);
        }

        return buildObjectsList(new Filter() {

            @Override
            public boolean passes(CosemClassInstance cosemClassInstance) {
                Integer classId = instanceIdClassIdMap.get(cosemClassInstance.getInstance().getInstanceId());

                if (classId == null) {
                    return false;
                }

                return classId == cosemClassInstance.getCosemClass().id();
            }
        });
    }

    private DataObject filterForClassList(DataObject accessParameter) throws IllegalAttributeAccessException {
        if (accessParameter.getType() != Type.ARRAY) {
            throw new IllegalAttributeAccessException(TYPE_UNMATCHED);
        }

        List<DataObject> classIds = accessParameter.getValue();

        if (classIds.isEmpty()) {
            return newNullData();
        }

        if (classIds.get(0).getType() != Type.LONG_UNSIGNED) {
            throw new IllegalAttributeAccessException(TYPE_UNMATCHED);
        }

        final Set<Integer> classIdSet = new HashSet<>(classIds.size());
        for (DataObject classIdDo : classIds) {
            Integer classId = classIdDo.getValue();
            classIdSet.add(classId);
        }

        return buildObjectsList(new Filter() {
            @Override
            public boolean passes(CosemClassInstance cosemClassInstance) {
                return classIdSet.contains(cosemClassInstance.getCosemClass().id());
            }
        });
    }

    private DataObject buildObjectsList(Filter filter) {
        return buildObjectsList(false, filter);
    }

    private DataObject buildObjectsList(boolean excludeAccessRights, Filter filter) {
        CosemLogicalDevice logicalDevice = this.dataDirectory.getLogicalDeviceFor(this.logicalDeviceId);
        List<DataObject> objectListElements = new LinkedList<>();

        for (ObisCode instanceId : logicalDevice.getInstanceIds()) {
            CosemClassInstance cosemClassInstance = logicalDevice.get(instanceId);

            if (!filter.passes(cosemClassInstance)) {
                continue;
            }

            DataObject listElement = buildObjectListElement(excludeAccessRights, cosemClassInstance);
            objectListElements.add(listElement);

        }

        return DataObject.newArrayData(objectListElements);
    }

    private static DataObject buildObjectListElement(boolean excludeAccessRights,
            CosemClassInstance cosemClassInstance) {

        CosemClass cosemClass = cosemClassInstance.getCosemClass();

        ObisCode instanceId = cosemClassInstance.getInstance().getInstanceId();
        DataObject instanceIdDo = newOctetStringData(instanceId.bytes());

        DataObject classId = newUInteger16Data((short) cosemClass.id());
        DataObject version = newUInteger8Data((short) cosemClass.version());

        Collection<Attribute> attributes = cosemClassInstance.getSortedAttributes();

        Collection<MethodAccessor> methods = cosemClassInstance.getSortedMethods();

        if (excludeAccessRights) {
            return newStructureData(asList(classId, version, instanceIdDo));
        }

        DataObject attributAccesItems = buildAttributeAccessElementsStruct(attributes);
        DataObject methodAccesItems = buildMethodAccessElementsStruct(methods);

        DataObject accessRightsStruct = newStructureData(asList(attributAccesItems, methodAccesItems));

        return newStructureData(asList(classId, version, instanceIdDo, accessRightsStruct));
    }

    private static DataObject buildMethodAccessElementsStruct(Collection<MethodAccessor> methods) {

        List<DataObject> attributeAccessItems = new ArrayList<>(methods.size());
        for (MethodAccessor actionMethod : methods) {

            CosemMethod methodProperties = actionMethod.getCosemMethod();

            DataObject methodId = DataObject.newInteger8Data(methodProperties.id());
            DataObject accessMod = DataObject.newEnumerateData((int) methodProperties.accessMode().getCode());

            DataObject attributeAccessItem = newStructureData(Arrays.asList(methodId, accessMod));

            attributeAccessItems.add(attributeAccessItem);
        }
        return newArrayData(attributeAccessItems);
    }

    private static DataObject buildAttributeAccessElementsStruct(Collection<Attribute> entries) {

        List<DataObject> attributeAccessItems = new ArrayList<>(entries.size());
        for (Attribute entry : entries) {

            CosemAttribute attributeProperties = entry.getAttributeProperties();

            DataObject attributeId = DataObject.newInteger8Data(attributeProperties.id());
            DataObject accessMode = DataObject.newEnumerateData((int) attributeProperties.accessMode().getCode());

            DataObject accessSelectors = buildSelector(attributeProperties);

            List<DataObject> attributeAccessItem = asList(attributeId, accessMode, accessSelectors);

            attributeAccessItems.add(DataObject.newStructureData(attributeAccessItem));
        }

        return DataObject.newArrayData(attributeAccessItems);
    }

    private static DataObject buildSelector(CosemAttribute attributeProperties) {
        DataObject accessSelectors;

        int[] selectors = attributeProperties.selector();
        if (selectors.length == 0) {
            accessSelectors = DataObject.newNullData();
        }
        else {
            List<DataObject> array = new ArrayList<>(selectors.length);
            for (int selector : selectors) {
                array.add(newInteger8Data((byte) selector));
            }
            accessSelectors = newArrayData(array);
        }
        return accessSelectors;
    }

    public DataObject getApplicationContextName() {
        if (applicationContextName == null) {
            Set<ConformanceSetting> conformanceSettings = this.dataDirectory.getLogicalDeviceFor(this.logicalDeviceId)
                    .getLogicalDevice()
                    .getConformance();

            Conformance conformance = ConformanceSettingConverter.conformanceFor(conformanceSettings);

            this.applicationContextName = DataObject.newOctetStringData(conformance.value);
        }

        return this.applicationContextName;
    }

    @CosemMethod(id = 1, consumes = Type.OCTET_STRING, accessMode = MethodAccessMode.ACCESS)
    public DataObject replyToHlsAuthentication(DataObject dataObject, Long connectionId)
            throws IllegalMethodAccessException {
        LogicalDevice logicalDevice = this.dataDirectory.getLogicalDeviceFor(this.logicalDeviceId).getLogicalDevice();

        ServerConnectionData connectionData = this.dataDirectory.getConnectionData(connectionId);

        SecuritySuite sec = logicalDevice.getRestrictions().get(connectionData.clientId);

        checkChallengeEquality(dataObject, connectionData);

        HlsSecretProcessor secretProcessor = hlsSecretProcessorFor(sec.getAuthenticationMechanism());

        byte[] clientToServerChallenge = connectionData.clientToServerChallenge;

        byte[] processedClientToServerChallenge;
        try {
            processedClientToServerChallenge = secretProcessor.process(clientToServerChallenge, sec,
                    logicalDevice.getSystemTitle(), connectionData.frameCounter);
        } catch (UnsupportedOperationException | IOException e) {
            throw new IllegalMethodAccessException(MethodResultCode.OTHER_REASON);
        }
        connectionData.authenticated = true;

        return DataObject.newOctetStringData(processedClientToServerChallenge);
    }

    private HlsSecretProcessor hlsSecretProcessorFor(AuthenticationMechanism authenticationMechanismId)
            throws IllegalMethodAccessException {

        HlsSecretProcessor secretProcessor;
        switch (authenticationMechanismId) {
        case HLS5_GMAC:
            secretProcessor = new HlsProcessorGmac();
            break;

        case LOW:
        case NONE:
        default:
            // TODO set correct error flag
            throw new IllegalMethodAccessException(MethodResultCode.OTHER_REASON);
        }

        return secretProcessor;
    }

    private static void checkChallengeEquality(DataObject dataObject, ServerConnectionData connectionData)
            throws IllegalMethodAccessException {
        byte[] receivedServerToClientChallenge = dataObject.getValue();

        byte[] processedServerToClientChallenge = connectionData.processedServerToClientChallenge;

        if (!Arrays.equals(receivedServerToClientChallenge, processedServerToClientChallenge)) {
            throw new IllegalMethodAccessException(MethodResultCode.OTHER_REASON);
        }
    }

    private interface Filter {
        boolean passes(CosemClassInstance cosemClassInstance);
    }
}
