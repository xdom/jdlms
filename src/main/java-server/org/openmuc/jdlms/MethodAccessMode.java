package org.openmuc.jdlms;

import org.openmuc.jdlms.datatypes.DlmsEnumeration;

/**
 * Restrict the access mode of a method.
 */
public enum MethodAccessMode implements DlmsEnumeration {
    /**
     * No access allowed.
     */
    NO_ACCESS(0),
    /**
     * Allows access under any circumstances.
     */
    ACCESS(1),
    /*
     * Allows HLS authenticated clients to access.
     */
    AUTHENTICATED_ACCESS(2);

    private int code;

    private MethodAccessMode(int code) {
        this.code = code;
    }

    @Override
    public long getCode() {
        return code;
    }

}
