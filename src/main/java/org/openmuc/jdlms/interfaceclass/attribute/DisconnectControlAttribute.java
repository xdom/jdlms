package org.openmuc.jdlms.interfaceclass.attribute;

import org.openmuc.jdlms.interfaceclass.InterfaceClass;

public enum DisconnectControlAttribute implements AttributeClass {
    LOGICAL_NAME(1),
    OUTPUT_STATE(2),
    CONTROL_STATE(3),
    CONTROL_MODE(4);

    static final InterfaceClass INTERFACE_CLASS = InterfaceClass.DISCONNECT_CONTROL;

    private final int id;

    private DisconnectControlAttribute(int id) {
        this.id = id;
    }

    @Override
    public InterfaceClass interfaceClass() {
        return INTERFACE_CLASS;
    }

    @Override
    public int attributeId() {
        return id;
    }

    @Override
    public String attributeName() {
        return name();
    }

}
