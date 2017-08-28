package org.openmuc.jdlms.internal;

import org.openmuc.jdlms.internal.asn1.cosem.Enum;

public enum StateError {

    SERVICE_NOT_ALLOWED(1),
    SERVICE_UNKNOWN(2);

    private long value;

    private StateError(long value) {
        this.value = value;
    }

    public Enum asEnum() {
        return new Enum(this.value);
    }
}
