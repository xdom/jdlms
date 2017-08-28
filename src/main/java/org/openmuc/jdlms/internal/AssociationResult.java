package org.openmuc.jdlms.internal;

import java.text.MessageFormat;

import org.openmuc.jdlms.FatalJDlmsException;
import org.openmuc.jdlms.JDlmsException.ExceptionId;
import org.openmuc.jdlms.JDlmsException.Fault;

public enum AssociationResult {
    ACCEPTED(0),
    REJECTED_PERMANENT(1),
    REJECTED_TRANSIENT(2);

    private static final AssociationResult[] VALUES = values();
    private long value;

    private AssociationResult(int value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    public static AssociationResult associationResultFor(long value) throws FatalJDlmsException {
        for (AssociationResult associationResult : VALUES) {
            if (associationResult.value == value) {
                return associationResult;
            }
        }

        String msg = MessageFormat.format(
                "The Server answered an association result {0} which is unknown/incompatible by/with the jDLMS stack.",
                value);
        throw new FatalJDlmsException(ExceptionId.UNKNOWN_ASSOCIATION_RESULT, Fault.SYSTEM, msg);
    }
}
