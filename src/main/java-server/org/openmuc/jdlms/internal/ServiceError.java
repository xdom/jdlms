package org.openmuc.jdlms.internal;

import org.openmuc.jdlms.datatypes.DlmsEnumeration;
import org.openmuc.jdlms.internal.asn1.cosem.Enum;

public enum ServiceError implements DlmsEnumeration {

    OPERATION_NOT_POSSIBLE(1),
    SERVICE_NOT_SUPPORTED(2),
    OTHER_REASON(3);

    private long code;

    private ServiceError(long code) {
        this.code = code;
    }

    @Override
    public long getCode() {
        return this.code;
    }

    public Enum asEnum() {
        return new Enum(this.code);
    }
}
