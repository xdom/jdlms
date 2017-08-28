package org.openmuc.jdlms.interfaceclass.attribute;

import org.openmuc.jdlms.interfaceclass.InterfaceClass;

public enum ImageTransferAttribute implements AttributeClass {
    LOGICAL_NAME(1),
    IMAGE_BLOCK_SIZE(2),
    IMAGE_TRANSFERRED_BLOCKS_STATUS(3),
    IMAGE_FIRST_NOT_TRANSFERRED_BLOCK_NUMBER(4),
    IMAGE_TRANSFER_ENABLED(5),
    IMAGE_TRANSFER_STATUS(6),
    IMAGE_TO_ACTIVATE_INFO(7);

    static final InterfaceClass INTERFACE_CLASS = InterfaceClass.IMAGE_TRANSFER;

    private final int id;

    private ImageTransferAttribute(int id) {
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
