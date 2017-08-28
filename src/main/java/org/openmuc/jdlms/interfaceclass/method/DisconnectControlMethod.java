package org.openmuc.jdlms.interfaceclass.method;

import org.openmuc.jdlms.interfaceclass.InterfaceClass;

public enum DisconnectControlMethod implements MethodClass {
    REMOTE_DISCONNECT(1, true),
    REMOTE_RECONNECT(2, true);

    static final InterfaceClass INTERFACE_CLASS = InterfaceClass.DISCONNECT_CONTROL;

    private final int id;
    private final boolean mandatory;

    private DisconnectControlMethod(int id, boolean mandatory) {
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
