package org.openmuc.jdlms.internal;

import org.openmuc.jdlms.internal.asn1.iso.acse.ReleaseResponseReason;

public enum ReleaseRespReason {
    NORMAL(0),
    NOT_FINISHED(1),
    USER_DEFINED(2);

    private long code;

    private ReleaseRespReason(long code) {
        this.code = code;
    }

    public ReleaseResponseReason toDlmsReason() {
        return new ReleaseResponseReason(code);
    }
}
