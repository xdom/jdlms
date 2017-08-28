package org.openmuc.jdlms.internal.association;

import org.openmuc.jdlms.internal.APdu;
import org.openmuc.jdlms.internal.AssociateSourceDiagnostic.AcseServiceUser;
import org.openmuc.jdlms.internal.AssociationResult;
import org.openmuc.jdlms.internal.ConformanceSettingConverter;
import org.openmuc.jdlms.internal.ContextId;
import org.openmuc.jdlms.internal.asn1.cosem.Conformance;

/**
 * This exception is thrown, if the initiate request is bad.
 */
public class AssociatRequestException extends GenericAssociationException {
    private final AcseServiceUser reason;
    private final transient Conformance conformanceSetting;

    public AssociatRequestException(AcseServiceUser reason) {
        this.reason = reason;
        this.conformanceSetting = ConformanceSettingConverter.conformanceFor();
    }

    public AcseServiceUser getReason() {
        return reason;
    }

    @Override
    public APdu getErrorMessageApdu() {
        return new InitiateResponseBuilder(this.conformanceSetting)
                .setContextId(ContextId.LOGICAL_NAME_REFERENCING_NO_CIPHERING)
                .setResult(AssociationResult.REJECTED_PERMANENT)
                .setAssociateSourceDiagnostic(this.reason)
                .build();
    }
}
