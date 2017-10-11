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
package org.openmuc.jdlms.internal;

import static java.util.Collections.sort;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.openmuc.jdlms.AccessResultCode;
import org.openmuc.jdlms.AttributeAddress;
import org.openmuc.jdlms.AuthenticationMechanism;
import org.openmuc.jdlms.CosemAttribute;
import org.openmuc.jdlms.CosemClass;
import org.openmuc.jdlms.CosemInterfaceObject;
import org.openmuc.jdlms.CosemResourceDescriptor;
import org.openmuc.jdlms.DataDirectory;
import org.openmuc.jdlms.IllegalAttributeAccessException;
import org.openmuc.jdlms.IllegalMethodAccessException;
import org.openmuc.jdlms.LogicalDevice;
import org.openmuc.jdlms.MethodAccessMode;
import org.openmuc.jdlms.MethodParameter;
import org.openmuc.jdlms.MethodResultCode;
import org.openmuc.jdlms.ObisCode;
import org.openmuc.jdlms.SecuritySuite;
import org.openmuc.jdlms.SecuritySuite.SecurityPolicy;
import org.openmuc.jdlms.SetParameter;
import org.openmuc.jdlms.datatypes.DataObject;
import org.openmuc.jdlms.datatypes.DataObject.Type;

public class DataDirectoryImpl implements DataDirectory {

    private final Map<Integer, CosemLogicalDevice> logicalDeviceMap;

    private final Map<Long, ServerConnectionData> connectionsData;

    public DataDirectoryImpl() {
        this.logicalDeviceMap = new HashMap<>();
        this.connectionsData = new HashMap<>();
    }

    public synchronized DataObject invokeMethod(int logicalDeviceId, MethodParameter params, Long connectionId)
            throws IllegalMethodAccessException {
        CosemClassInstance dlmsClassInstance = retrieveDlmsClassInstance(logicalDeviceId, params);

        if (dlmsClassInstance == null) {
            throw new IllegalMethodAccessException(MethodResultCode.OBJECT_UNDEFINED);
        }

        MethodAccessor method = findMethod(params.getId(), dlmsClassInstance);
        SecuritySuite sec = getConnectionData(connectionId).securitySuite;
        SecurityPolicy securityPolicy = sec.getSecurityPolicy();

        MethodAccessMode accessMode = method.getCosemMethod().accessMode();

        if (accessMode == MethodAccessMode.NO_ACCESS) {
            throw new IllegalMethodAccessException(MethodResultCode.READ_WRITE_DENIED);
        }

        if (accessMode == MethodAccessMode.AUTHENTICATED_ACCESS && !securityPolicy.isAuthenticated()) {
            throw new IllegalMethodAccessException(MethodResultCode.READ_WRITE_DENIED);
        }

        return method.invoke(dlmsClassInstance, params.getParameter(), connectionId, securityPolicy);
    }

    private MethodAccessor findMethod(long methodId, CosemClassInstance dlmsClassInstance)
            throws IllegalMethodAccessException {
        if (dlmsClassInstance == null) {
            throw new IllegalMethodAccessException(MethodResultCode.OBJECT_UNDEFINED);
        }
        return dlmsClassInstance.getMethod((byte) methodId);
    }

    public Set<Integer> getLogicalDeviceIds() {
        return this.logicalDeviceMap.keySet();
    }

    public boolean doesLogicalDeviceExists(int logicalDeviceId) {
        return this.logicalDeviceMap.containsKey(logicalDeviceId);
    }

    public CosemLogicalDevice addLogicalDevice(int logicalDeviceId, CosemLogicalDevice logicalDevice) {
        return this.logicalDeviceMap.put(logicalDeviceId, logicalDevice);
    }

    public synchronized AccessResultCode set(int logicalDeviceId, SetParameter setParameter, Long connectionId) {
        Attribute entry;
        CosemClassInstance dlmsClassInstance;
        AttributeAddress attributeAddress = setParameter.getAttributeAddress();
        try {
            dlmsClassInstance = retrieveDlmsClassInstance(logicalDeviceId, attributeAddress);
            if (dlmsClassInstance == null) {
                return AccessResultCode.OBJECT_UNDEFINED;
            }

            entry = retrieveAttribute(dlmsClassInstance, attributeAddress.getId());
        } catch (IllegalAttributeAccessException e) {
            return e.getAccessResultCode();
        }

        CosemAttribute attributeProperties = entry.attributeProperties;

        DataObject data = setParameter.getData();
        try {
            checkSetAccess(attributeProperties, data.getType());
        } catch (IllegalAttributeAccessException e) {
            return e.getAccessResultCode();
        }

        ServerConnectionData connectionData = getConnectionData(connectionId);
        try {
            entry.accessor.set(data, dlmsClassInstance, attributeAddress.getAccessSelection(), connectionId,
                    connectionData.securitySuite.getSecurityPolicy());
        } catch (IllegalAttributeAccessException e) {
            return e.getAccessResultCode();
        }

        return AccessResultCode.SUCCESS;
    }

    private Attribute retrieveAttribute(CosemClassInstance dlmsClassInstance, int attributeId)
            throws IllegalAttributeAccessException {

        Attribute entry = dlmsClassInstance.getAttribute((byte) attributeId);

        if (entry == null) {
            throw new IllegalAttributeAccessException(AccessResultCode.READ_WRITE_DENIED);
        }

        return entry;
    }

    private CosemClassInstance retrieveDlmsClassInstance(int logicalDeviceId,
            CosemResourceDescriptor cosemResourceDescriptor) {
        CosemLogicalDevice logicalDevice = this.logicalDeviceMap.get(logicalDeviceId);
        CosemClassInstance dlmsClassInstance = logicalDevice.get(cosemResourceDescriptor.getInstanceId());

        if (dlmsClassInstance == null
                || cosemResourceDescriptor.getClassId() != dlmsClassInstance.getCosemClass().id()) {
            return null;
        }

        return dlmsClassInstance;
    }

    private void checkSetAccess(CosemAttribute attributeProperties, Type type) throws IllegalAttributeAccessException {
        switch (attributeProperties.accessMode()) {
        case READ_ONLY:
        case NO_ACCESS:
            throw new IllegalAttributeAccessException(AccessResultCode.READ_WRITE_DENIED);
        default:
            // TODO: Handle other cases!
            break;
        }

        if (attributeProperties.type() != Type.DONT_CARE && attributeProperties.type() != type) {
            // TODO: error wrong dataobject provided!
        }
    }

    public synchronized DataObject get(int logicalDeviceId, AttributeAddress attributeAddress, Long connectionId)
            throws IllegalAttributeAccessException {
        CosemClassInstance dlmsClassInstance = retrieveDlmsClassInstance(logicalDeviceId, attributeAddress);

        ServerConnectionData connectionData = this.connectionsData.get(connectionId);
        if (dlmsClassInstance == null) {
            throw new IllegalAttributeAccessException(AccessResultCode.OBJECT_UNDEFINED);
        }

        Attribute entry = retrieveAttribute(dlmsClassInstance, attributeAddress.getId());

        CosemAttribute attributeProperties = entry.attributeProperties;
        checkGetAccess(attributeProperties, connectionData);

        return entry.accessor.get(dlmsClassInstance, attributeAddress.getAccessSelection(), connectionId,
                connectionData.securitySuite.getSecurityPolicy());
    }

    private void checkGetAccess(CosemAttribute attributeProperties, ServerConnectionData connectionData)
            throws IllegalAttributeAccessException {
        switch (attributeProperties.accessMode()) {
        case AUTHENTICATED_WRITE_ONLY:
        case WRITE_ONLY:
        case NO_ACCESS:
            throw new IllegalAttributeAccessException(AccessResultCode.READ_WRITE_DENIED);

        case AUTHENTICATED_READ_ONLY:
        case AUTHENTICATED_READ_AND_WRITE:
            SecuritySuite securitySuite = connectionData.securitySuite;
            if (securitySuite.getAuthenticationMechanism() == AuthenticationMechanism.NONE) {
                throw new IllegalAttributeAccessException(AccessResultCode.READ_WRITE_DENIED);
            }
            break;

        case READ_AND_WRITE:
        case READ_ONLY:
        default:
            break;
        }
    }

    public static class CosemLogicalDevice {

        private final Map<ObisCode, CosemClassInstance> classes;
        private final LogicalDevice logicalDevice;
        private final BaseNameRangeSet baseNameRanges;

        public CosemLogicalDevice(LogicalDevice logicalDevice, BaseNameRangeSet baseNameRanges) {
            this.baseNameRanges = baseNameRanges;
            this.classes = new HashMap<>();
            this.logicalDevice = logicalDevice;
        }

        public CosemClassInstance put(ObisCode key, CosemClassInstance classInstance) {
            return this.classes.put(key, classInstance);
        }

        public LogicalDevice getLogicalDevice() {
            return logicalDevice;
        }

        public BaseNameRangeSet getBaseNameRanges() {
            return baseNameRanges;
        }

        public Set<ObisCode> getInstanceIds() {
            return this.classes.keySet();
        }

        public CosemClassInstance get(ObisCode key) {
            return this.classes.get(key);
        }
    }

    public static class CosemClassInstance {
        private final Map<Byte, Attribute> attributesMap;
        private final Map<Byte, MethodAccessor> methodsMap;

        private final CosemInterfaceObject instance;
        private final CosemClass cosemClass;

        public CosemClassInstance(CosemClass cosemClass, CosemInterfaceObject instance) {
            this.attributesMap = new HashMap<>();
            this.methodsMap = new HashMap<>();

            this.instance = instance;
            this.cosemClass = cosemClass;
        }

        public CosemClass getCosemClass() {
            return cosemClass;
        }

        public CosemInterfaceObject getInstance() {
            return instance;
        }

        public Attribute putAttribute(Byte attributeId, Attribute value) {
            return this.attributesMap.put(attributeId, value);
        }

        private Attribute getAttribute(Byte attributeId) {
            return this.attributesMap.get(attributeId);
        }

        public MethodAccessor putMethod(Byte methodId, MethodAccessor value) {
            return this.methodsMap.put(methodId, value);
        }

        private MethodAccessor getMethod(Byte methodId) {
            return this.methodsMap.get(methodId);
        }

        public Collection<MethodAccessor> getSortedMethods() {
            List<MethodAccessor> sortedEntries = new ArrayList<>(getMethods());
            sort(sortedEntries, new Comparator<MethodAccessor>() {

                @Override
                public int compare(MethodAccessor o1, MethodAccessor o2) {
                    return Integer.compare(o1.getCosemMethod().id(), o2.getCosemMethod().id());
                }
            });
            return sortedEntries;
        }

        public Collection<Attribute> getAttributes() {
            return this.attributesMap.values();
        }

        public Collection<Attribute> getSortedAttributes() {
            List<Attribute> sortedEntries = new ArrayList<>(getAttributes());
            sort(sortedEntries, new Comparator<Attribute>() {

                @Override
                public int compare(Attribute o1, Attribute o2) {
                    return Integer.compare(o1.getAttributeProperties().id(), o2.getAttributeProperties().id());
                }
            });
            return sortedEntries;
        }

        public Collection<MethodAccessor> getMethods() {
            return this.methodsMap.values();
        }

        public List<Entry<Byte, MethodAccessor>> getMethodPairs() {
            List<Entry<Byte, MethodAccessor>> retList = new ArrayList<>(this.methodsMap.entrySet());
            Collections.sort(retList, new Comparator<Entry<Byte, MethodAccessor>>() {
                @Override
                public int compare(Entry<Byte, MethodAccessor> o1, Entry<Byte, MethodAccessor> o2) {
                    return Byte.compare(o1.getKey(), o2.getKey());
                }
            });
            return retList;
        }

    }

    public static class Attribute {
        private final AttributeAccessor accessor;
        private final CosemAttribute attributeProperties;

        public Attribute(AttributeAccessor accessor, CosemAttribute attributeProperties) {
            this.accessor = accessor;
            this.attributeProperties = attributeProperties;
        }

        public CosemAttribute getAttributeProperties() {
            return attributeProperties;
        }

    }

    public CosemLogicalDevice getLogicalDeviceFor(Integer logicalDeviceId) {
        return this.logicalDeviceMap.get(logicalDeviceId);
    }

    public ServerConnectionData addConnection(Long connectionId, ServerConnectionData connectionData) {
        return this.connectionsData.put(connectionId, connectionData);
    }

    public ServerConnectionData getConnectionData(Long connectionId) {
        return connectionsData.get(connectionId);
    }

    public ServerConnectionData removeConnection(Long connectionId) {
        return this.connectionsData.remove(connectionId);
    }

    public BaseNameRangeSet baseNameRangesFor(Integer logicalDeviceId) {
        return this.logicalDeviceMap.get(logicalDeviceId).getBaseNameRanges();
    }

}
