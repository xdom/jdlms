package org.openmuc.jdlms.interfaceclass.method;

import org.openmuc.jdlms.interfaceclass.InterfaceClass;

public enum MBusClientMethod implements MethodClass {
    SLAVE_INSTALL(1, false),
    SLAVE_DEINSTALL(2, false),
    CAPTURE(3, false),
    RESET_ALARM(4, false),
    SYNCHRONIZE_CLOCK(5, false),
    DATA_SEND(6, false),
    SET_ENCRYPTION_KEY(7, false),
    TRANSFER_KEY(8, false);

    static final InterfaceClass INTERFACE_CLASS = InterfaceClass.MBUS_CLIENT;
    private int methodId;
    private boolean mandatory;

    private MBusClientMethod(int methodId, boolean mandatory) {
        this.methodId = methodId;
        this.mandatory = mandatory;
    }

    @Override
    public boolean isMandatory() {
        return this.mandatory;
    }

    @Override
    public int getMethodId() {
        return this.methodId;
    }

    @Override
    public InterfaceClass getInterfaceClass() {
        return INTERFACE_CLASS;
    }

    @Override
    public String getMethodName() {
        return name();
    }

}
