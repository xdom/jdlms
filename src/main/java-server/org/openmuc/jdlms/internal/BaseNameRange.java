package org.openmuc.jdlms.internal;

import static org.openmuc.jdlms.AccessResultCode.OBJECT_UNDEFINED;

import java.util.Map;

import org.openmuc.jdlms.IllegalAttributeAccessException;
import org.openmuc.jdlms.internal.DataDirectoryImpl.CosemClassInstance;

public class BaseNameRange extends Range<Integer> {

    private static final int OFFSET = 0x08;
    private final int baseName;
    @SuppressWarnings("unused")
    private final int lastSnIndex;
    private final CosemClassInstance classInstance;
    private final Map<Integer, Integer> snMethodIdMap;
    private final int numOfFields;

    public BaseNameRange(int baseName, int lastSnIndex, CosemClassInstance classInstance,
            Map<Integer, Integer> snMethodIdMap, int numOfFields) {
        super(baseName, lastSnIndex);
        this.baseName = baseName;
        this.lastSnIndex = lastSnIndex;
        this.classInstance = classInstance;
        this.snMethodIdMap = snMethodIdMap;
        this.numOfFields = numOfFields;

    }

    public CosemClassInstance getClassInstance() {
        return classInstance;
    }

    public int getBaseName() {
        return baseName;
    }

    public Access accessFor(int varName) throws IllegalAttributeAccessException {
        int baseI = varName + OFFSET - this.baseName;

        // must be a multiple of 0x08!
        if (baseI % OFFSET != 0) {
            throw new IllegalAttributeAccessException(OBJECT_UNDEFINED);
        }

        int index = baseI / OFFSET;

        AccessType accessType;
        Integer memberId;
        if (index > this.numOfFields) {

            memberId = this.snMethodIdMap.get(varName);
            if (memberId == null) {
                throw new IllegalAttributeAccessException(OBJECT_UNDEFINED);
            }
            accessType = AccessType.METHOD;
        }
        else {
            memberId = index;
            accessType = AccessType.ATTRIBUTE;
        }

        return new Access(accessType, memberId);
    }

    public enum AccessType {
        ATTRIBUTE,
        METHOD
    }

    public class Access {
        private final AccessType accessType;
        private final int memberId;

        private Access(AccessType accessType, int memberId) {
            this.accessType = accessType;
            this.memberId = memberId;
        }

        public AccessType getAccessType() {
            return accessType;
        }

        public int getMemberId() {
            return memberId;
        }

    }

}
