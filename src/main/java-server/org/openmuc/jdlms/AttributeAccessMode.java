package org.openmuc.jdlms;

import org.openmuc.jdlms.datatypes.DlmsEnumeration;

/**
 * The access restrictions for COSEM attributes.
 */
public enum AttributeAccessMode implements DlmsEnumeration {
    NO_ACCESS(0),
    READ_ONLY(1),
    WRITE_ONLY(2),
    READ_AND_WRITE(3),
    AUTHENTICATED_READ_ONLY(4),
    AUTHENTICATED_WRITE_ONLY(5),
    AUTHENTICATED_READ_AND_WRITE(6);

    private int code;

    private AttributeAccessMode(int code) {
        this.code = code;
    }

    @Override
    public long getCode() {
        return code;
    }
}
