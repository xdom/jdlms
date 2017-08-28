package org.openmuc.jdlms.internal;

import java.util.Map;

import org.openmuc.jdlms.datatypes.DlmsEnumeration;

public enum ReleaseReqReason implements DlmsEnumeration {
    NORMAL(0),
    URGENT(1),
    USER_DEFINED(2),
    UNKNOWN(-1);

    private static final Map<Long, ReleaseReqReason> mapping;
    private long code;

    static {
        mapping = DlmsEnumFunctions.generateEnumMap(ReleaseReqReason.class);
    }

    private ReleaseReqReason(long code) {
        this.code = code;
    }

    public static ReleaseReqReason reasonFor(long code) {

        ReleaseReqReason releaseReqReason = mapping.get(code);

        if (releaseReqReason != null) {
            return releaseReqReason;
        }
        else {
            return UNKNOWN;
        }

    }

    @Override
    public long getCode() {
        return code;
    }
}
