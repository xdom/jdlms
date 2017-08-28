package org.openmuc.jdlms.interfaceclass.attribute;

import org.openmuc.jdlms.interfaceclass.InterfaceClass;

public enum MbusClientAttribute implements AttributeClass {
    LOGICAL_NAME(1),
    MBUS_PORT_REFERENCE(2),
    CAPTURE_DEFINITION(3),
    CAPTURE_PERIOD(4),
    PRIMARY_ADDRESS(5),
    IDENTIFICATION_NUMBER(6),
    MANUFACTURER_ID(7),
    VERSION(8),
    DEVICE_TYPE(9),
    ACCESS_NUMBER(10),
    STATUS(11),
    ALARM(12),
    CONFIGURATION(13),
    ENCRYPTION_KEY_STATUS(14);

    static final InterfaceClass INTERFACE_CLASS = InterfaceClass.MBUS_CLIENT;

    private MbusClientAttribute(int id) {
        this.id = id;
    }

    private int id;

    @Override
    public int attributeId() {
        return id;
    }

    @Override
    public String attributeName() {
        return name();
    }

    @Override
    public InterfaceClass interfaceClass() {
        return INTERFACE_CLASS;
    }

}
