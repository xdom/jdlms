package org.openmuc.jdlms.internal.systemclasses;

import static org.openmuc.jdlms.AccessResultCode.OBJECT_UNDEFINED;
import static org.openmuc.jdlms.AccessResultCode.TYPE_UNMATCHED;
import static org.openmuc.jdlms.AttributeAccessMode.READ_ONLY;
import static org.openmuc.jdlms.MethodAccessMode.ACCESS;
import static org.openmuc.jdlms.datatypes.DataObject.newArrayData;
import static org.openmuc.jdlms.datatypes.DataObject.newStructureData;
import static org.openmuc.jdlms.datatypes.DataObject.Type.ARRAY;
import static org.openmuc.jdlms.datatypes.DataObject.Type.LONG_INTEGER;
import static org.openmuc.jdlms.datatypes.DataObject.Type.OCTET_STRING;
import static org.openmuc.jdlms.datatypes.DataObject.Type.STRUCTURE;
import static org.openmuc.jdlms.internal.WellKnownInstanceIds.CURRENT_ASSOCIATION_ID;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.openmuc.jdlms.AccessResultCode;
import org.openmuc.jdlms.AttributeAddress;
import org.openmuc.jdlms.CosemAttribute;
import org.openmuc.jdlms.CosemClass;
import org.openmuc.jdlms.CosemMethod;
import org.openmuc.jdlms.CosemSnInterfaceObject;
import org.openmuc.jdlms.IllegalAttributeAccessException;
import org.openmuc.jdlms.IllegalMethodAccessException;
import org.openmuc.jdlms.MethodResultCode;
import org.openmuc.jdlms.ObisCode;
import org.openmuc.jdlms.SelectiveAccessDescription;
import org.openmuc.jdlms.datatypes.DataObject;
import org.openmuc.jdlms.datatypes.DataObject.Type;
import org.openmuc.jdlms.internal.BaseNameRange;
import org.openmuc.jdlms.internal.BaseNameRangeSet;
import org.openmuc.jdlms.internal.DataDirectoryImpl;
import org.openmuc.jdlms.internal.DataDirectoryImpl.Attribute;
import org.openmuc.jdlms.internal.DataDirectoryImpl.CosemClassInstance;
import org.openmuc.jdlms.internal.DataDirectoryImpl.CosemLogicalDevice;
import org.openmuc.jdlms.internal.MethodAccessor;

@CosemClass(id = 12, version = 2)
public final class AssociationSnClass extends CosemSnInterfaceObject {
    private static final int ASSOCIATION_SN_BASE_NAME = 0xFA00;

    private static final int CLASS_ID_ACCESS = 1;
    private static final int CLASS_ID_BASE_NANE_ACCESS = 2;
    private static final int BASE_NAME_ACCESS = 3;

    @CosemAttribute(id = 2, accessMode = READ_ONLY, type = ARRAY, selector = { CLASS_ID_ACCESS,
            CLASS_ID_BASE_NANE_ACCESS, BASE_NAME_ACCESS })
    private DataObject objectList;

    @CosemAttribute(id = 3, accessMode = READ_ONLY, type = ARRAY, selector = { BASE_NAME_ACCESS })
    private DataObject accessRightsList;

    @CosemAttribute(id = 4, accessMode = READ_ONLY, type = OCTET_STRING)
    private DataObject securitySetupReference;

    @CosemDataDirectory
    private DataDirectoryImpl directory;

    private final int logicalDeviceId;

    public AssociationSnClass(int logicalDeviceId) {
        super(ASSOCIATION_SN_BASE_NAME, CURRENT_ASSOCIATION_ID);
        this.logicalDeviceId = logicalDeviceId;
    }

    public DataObject getObjectList(SelectiveAccessDescription selectiveAccessDescription)
            throws IllegalAttributeAccessException {
        if (selectiveAccessDescription == null) {
            if (this.objectList == null) {
                initializeObjectList();
            }
            return this.objectList;
        }

        return getSelectedObjectListElement(selectiveAccessDescription);
    }

    public DataObject getAccessRightsList(SelectiveAccessDescription selectiveAccessDescription)
            throws IllegalAttributeAccessException {
        if (selectiveAccessDescription == null) {

            if (this.accessRightsList == null) {
                initializeAccessRightList();
            }

            return this.accessRightsList;
        }

        return getSelectedAccessRightElement(selectiveAccessDescription);
    }

    @CosemMethod(id = 3, accessMode = ACCESS)
    public DataObject readByLogicalName(DataObject data, Long connectionId) throws IllegalMethodAccessException {

        if (data.getType() != Type.ARRAY) {
            throw new IllegalMethodAccessException(MethodResultCode.TYPE_UNMATCHED);
        }

        List<DataObject> attributeIds = data.getValue();

        if (attributeIds.isEmpty()) {
            return DataObject.newNullData();
        }

        if (attributeIds.get(0).getType() != STRUCTURE) {
            throw new IllegalMethodAccessException(MethodResultCode.TYPE_UNMATCHED);
        }

        List<DataObject> res = new LinkedList<>();
        for (DataObject attributeId : attributeIds) {
            List<DataObject> struct = attributeId.getValue();

            if (struct.size() != 3) {
                throw new IllegalMethodAccessException(MethodResultCode.TYPE_UNMATCHED);
            }

            DataObject classIdDo = struct.get(0);
            DataObject logicalNameDo = struct.get(1);
            DataObject attributeIndexDo = struct.get(2);

            boolean typesDontMatch = classIdDo.getType() != Type.LONG_UNSIGNED
                    || logicalNameDo.getType() != Type.OCTET_STRING || attributeIndexDo.getType() != Type.INTEGER;
            if (typesDontMatch) {
                throw new IllegalMethodAccessException(MethodResultCode.TYPE_UNMATCHED);
            }

            int classId = classIdDo.getValue();
            byte[] obis = logicalNameDo.getValue();
            ObisCode instanceId = new ObisCode(obis);
            byte attId = attributeIndexDo.getValue();
            attId = (byte) (attId & 0xff);

            if (attId == 0) {
                CosemLogicalDevice logicalDevice = this.directory.getLogicalDeviceFor(this.logicalDeviceId);
                CosemClassInstance cosemClassInstance = logicalDevice.get(instanceId);
                for (Attribute a : cosemClassInstance.getSortedAttributes()) {
                    AttributeAddress attributeAddress = new AttributeAddress(classId, instanceId,
                            a.getAttributeProperties().id());

                    res.add(callGet(connectionId, attributeAddress));
                }

            }
            else {
                AttributeAddress attributeAddress = new AttributeAddress(classId, instanceId, attId);

                res.add(callGet(connectionId, attributeAddress));
            }

        }

        return DataObject.newStructureData(res);
    }

    private DataObject callGet(Long connectionId, AttributeAddress attributeAddress)
            throws IllegalMethodAccessException {
        try {
            return this.directory.get(this.logicalDeviceId, attributeAddress, connectionId);
        } catch (IllegalAttributeAccessException e) {
            throw new IllegalMethodAccessException(MethodResultCode.SCOPE_OF_ACCESS_VIOLATION);
        }
    }

    private DataObject getSelectedObjectListElement(final SelectiveAccessDescription selectiveAccessDescription)
            throws IllegalAttributeAccessException {
        final DataObject accessParameter = selectiveAccessDescription.getAccessParameter();

        switch (selectiveAccessDescription.getAccessSelector()) {
        case CLASS_ID_ACCESS:
            return findClassesForId(accessParameter);

        case CLASS_ID_BASE_NANE_ACCESS:
            return findClassForIdAndBaseName(accessParameter);

        case BASE_NAME_ACCESS:
            return findClassForBaseName(accessParameter);

        default:
            throw new IllegalAttributeAccessException(TYPE_UNMATCHED);
        }

    }

    private DataObject findClassForIdAndBaseName(DataObject accessParameter) throws IllegalAttributeAccessException {
        if (accessParameter.getType() != STRUCTURE) {
            throw new IllegalAttributeAccessException(TYPE_UNMATCHED);
        }

        final int numberOfParameters = 2;

        List<DataObject> struct = accessParameter.getValue();
        if (struct.size() != numberOfParameters) {
            throw new IllegalAttributeAccessException(TYPE_UNMATCHED);
        }

        DataObject classIdDo = struct.get(0);
        DataObject instanceIdDo = struct.get(1);
        if (instanceIdDo.getType() != Type.OCTET_STRING) {
            throw new IllegalAttributeAccessException(TYPE_UNMATCHED);
        }

        byte[] bytes = instanceIdDo.getValue();
        final ObisCode exInstanceId = new ObisCode(bytes);

        if (classIdDo.getType() != Type.LONG_UNSIGNED) {
            throw new IllegalAttributeAccessException(TYPE_UNMATCHED);
        }

        final Number exClassId = classIdDo.getValue();
        DataObject objects = findObjects(new Filter() {

            @Override
            public boolean passes(BaseNameRange baseNameRange) {

                CosemClassInstance classInstance = baseNameRange.getClassInstance();
                int classId = classInstance.getCosemClass().id();

                return classId == exClassId.intValue()
                        && exInstanceId.equals(classInstance.getInstance().getInstanceId());
            }
        });

        List<DataObject> value = objects.getValue();
        if (value.isEmpty()) {
            throw new IllegalAttributeAccessException(OBJECT_UNDEFINED);
        }
        else {
            return value.get(0);
        }
    }

    private DataObject findClassForBaseName(DataObject accessParameter) throws IllegalAttributeAccessException {
        BaseNameRange baseNameRange = baseNameRangeFor(accessParameter);

        if (baseNameRange == null) {
            throw new IllegalAttributeAccessException(OBJECT_UNDEFINED);
        }

        return objectListElementFor(baseNameRange);
    }

    private DataObject findClassesForId(final DataObject accessParameter) throws IllegalAttributeAccessException {
        if (accessParameter.getType() != Type.LONG_UNSIGNED) {
            throw new IllegalAttributeAccessException(TYPE_UNMATCHED);
        }

        Number value = accessParameter.getValue();
        final int exptClassId = value.intValue();

        return findObjects(new Filter() {
            @Override
            public boolean passes(BaseNameRange baseNameRange) {
                return baseNameRange.getClassInstance().getCosemClass().id() == exptClassId;
            }
        });
    }

    private DataObject getSelectedAccessRightElement(SelectiveAccessDescription selectiveAccessDescription)
            throws IllegalAttributeAccessException {
        int accessSelector = selectiveAccessDescription.getAccessSelector();
        if (accessSelector != BASE_NAME_ACCESS) {
            // TODO correct?
            throw new IllegalAttributeAccessException(TYPE_UNMATCHED);
        }

        BaseNameRange baseNameRange = baseNameRangeFor(selectiveAccessDescription.getAccessParameter());

        if (baseNameRange == null) {
            throw new IllegalAttributeAccessException(AccessResultCode.OBJECT_UNDEFINED);

        }
        return accessRightFor(baseNameRange);
    }

    private BaseNameRange baseNameRangeFor(DataObject accessParameter) throws IllegalAttributeAccessException {
        if (accessParameter.getType() != LONG_INTEGER) {
            throw new IllegalAttributeAccessException(TYPE_UNMATCHED);
        }

        BaseNameRangeSet baseNameRangeSet = this.directory.baseNameRangesFor(this.logicalDeviceId);

        Number basename = accessParameter.getValue();

        return baseNameRangeSet.getIntersectingRange(basename.shortValue() & 0xFFFF);
    }

    private void initializeAccessRightList() {
        List<BaseNameRange> baseNameRanges = this.directory.baseNameRangesFor(this.logicalDeviceId).toList();

        List<DataObject> accessRightListElements = new ArrayList<>(baseNameRanges.size());

        for (BaseNameRange baseNameRange : baseNameRanges) {
            DataObject accessRightListElement = accessRightFor(baseNameRange);
            accessRightListElements.add(accessRightListElement);
        }

        this.accessRightsList = newArrayData(accessRightListElements);
    }

    private DataObject accessRightFor(BaseNameRange baseNameRange) {
        CosemClassInstance classInstance = baseNameRange.getClassInstance();

        DataObject baseName = DataObject.newInteger16Data((short) baseNameRange.getBaseName());

        DataObject attribueAccess = attribueAccessFrom(classInstance);
        DataObject methodAccess = methodAccessFrom(classInstance);

        return newStructureData(baseName, attribueAccess, methodAccess);
    }

    private static DataObject methodAccessFrom(CosemClassInstance classInstance) {
        Collection<MethodAccessor> sortedMethods = classInstance.getSortedMethods();
        List<DataObject> methodAccessDes = new ArrayList<>(sortedMethods.size());

        for (MethodAccessor methodAccessor : sortedMethods) {
            CosemMethod cosemMethod = methodAccessor.getCosemMethod();

            DataObject methodId = DataObject.newInteger8Data(cosemMethod.id());
            DataObject accessMode = DataObject.newEnumerateData((int) cosemMethod.accessMode().getCode());

            DataObject methodAccessItem = newStructureData(methodId, accessMode);

            methodAccessDes.add(methodAccessItem);
        }

        return newArrayData(methodAccessDes);
    }

    private static DataObject attribueAccessFrom(CosemClassInstance classInstance) {
        Collection<Attribute> sortedAttributes = classInstance.getSortedAttributes();

        List<DataObject> attributeAccessDes = new ArrayList<>(sortedAttributes.size());

        for (Attribute attribute : sortedAttributes) {
            CosemAttribute attributeProperties = attribute.getAttributeProperties();

            DataObject attributeId = DataObject.newInteger16Data(attributeProperties.id());
            DataObject accessMode = DataObject.newEnumerateData((int) attributeProperties.accessMode().getCode());

            DataObject accessSelector = createAccessSelector(attributeProperties);

            DataObject attributeAccessItem = DataObject.newStructureData(attributeId, accessMode, accessSelector);
            attributeAccessDes.add(attributeAccessItem);
        }

        return DataObject.newArrayData(attributeAccessDes);
    }

    private static DataObject createAccessSelector(CosemAttribute attributeProperties) {
        int[] selectors = attributeProperties.selector();
        if (selectors.length == 0) {
            return DataObject.newNullData();
        }
        else {
            List<DataObject> selData = new ArrayList<>(selectors.length);

            for (int selector : selectors) {
                selData.add(DataObject.newInteger8Data((byte) selector));
            }

            return DataObject.newArrayData(selData);
        }
    }

    private void initializeObjectList() {
        this.objectList = findObjects(new Filter() {
            @Override
            public boolean passes(BaseNameRange baseNameRange) {
                return true; // don't filter
            }
        });
    }

    private DataObject findObjects(Filter filter) {
        List<BaseNameRange> baseNameRanges = this.directory.baseNameRangesFor(this.logicalDeviceId).toList();

        List<DataObject> objectListelements = new ArrayList<>(baseNameRanges.size());

        for (BaseNameRange baseNameRange : baseNameRanges) {
            if (!filter.passes(baseNameRange)) {
                continue;
            }

            DataObject objectListElement = objectListElementFor(baseNameRange);
            objectListelements.add(objectListElement);
        }

        return newArrayData(objectListelements);
    }

    private DataObject objectListElementFor(BaseNameRange baseNameRange) {
        CosemClassInstance classInstance = baseNameRange.getClassInstance();
        CosemClass cosemClass = classInstance.getCosemClass();

        DataObject baseName = DataObject.newInteger16Data((short) baseNameRange.getBaseName());
        DataObject classId = DataObject.newUInteger16Data(cosemClass.id());
        DataObject version = DataObject.newUInteger8Data((short) cosemClass.version());
        DataObject logicalName = DataObject.newOctetStringData(classInstance.getInstance().getInstanceId().bytes());

        List<DataObject> element = Arrays.asList(baseName, classId, version, logicalName);

        return newStructureData(element);
    }

    private interface Filter {
        boolean passes(BaseNameRange baseNameRange);
    }

}
