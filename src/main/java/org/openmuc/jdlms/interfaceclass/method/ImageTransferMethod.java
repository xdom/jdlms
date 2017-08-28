package org.openmuc.jdlms.interfaceclass.method;

import org.openmuc.jdlms.interfaceclass.InterfaceClass;

public enum ImageTransferMethod implements MethodClass {
    IMAGE_TRANSFER_INITIATE(1, true),
    IMAGE_BLOCK_TRANSFER(2, true),
    IMAGE_VERIFY(3, true),
    IMAGE_ACTIVATE(4, true);

    static final InterfaceClass INTERFACE_CLASS = InterfaceClass.IMAGE_TRANSFER;

    private final int id;
    private final boolean mandatory;

    private ImageTransferMethod(int id, boolean mandatory) {
        this.id = id;
        this.mandatory = mandatory;
    }

    @Override
    public int getMethodId() {
        return id;
    }

    @Override
    public InterfaceClass getInterfaceClass() {
        return INTERFACE_CLASS;
    }

    @Override
    public boolean isMandatory() {
        return mandatory;
    }

    @Override
    public String getMethodName() {
        return name();
    }

}
