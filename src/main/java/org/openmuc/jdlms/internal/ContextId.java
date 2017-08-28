package org.openmuc.jdlms.internal;

import java.text.MessageFormat;

import org.openmuc.jdlms.settings.client.ReferencingMethod;

public enum ContextId {
    LOGICAL_NAME_REFERENCING_NO_CIPHERING(1),
    SHORT_NAME_REFERENCING_NO_CIPHERING(2),
    LOGICAL_NAME_REFERENCING_WITH_CIPHERING(3),
    SHORT_NAME_REFERENCING_WITH_CIPHERING(4);

    private static final ContextId[] VALUES = values();
    private int code;

    private ContextId(int code) {
        this.code = code;
    }

    public static ContextId contextIdFor(int code) {
        for (ContextId value : VALUES) {
            if (value.code == code) {
                return value;
            }
        }
        throw new IllegalArgumentException(MessageFormat.format("Unknown code {0}.", code));
    }

    public int getCode() {
        return code;
    }

    public ReferencingMethod getReferencingMethod() {
        if (this == SHORT_NAME_REFERENCING_WITH_CIPHERING || this == ContextId.SHORT_NAME_REFERENCING_NO_CIPHERING) {
            return ReferencingMethod.SHORT;
        }
        else {
            return ReferencingMethod.LOGICAL;
        }
    }

    public boolean isCiphered() {
        return this == LOGICAL_NAME_REFERENCING_WITH_CIPHERING
                || this == ContextId.SHORT_NAME_REFERENCING_WITH_CIPHERING;
    }
}
