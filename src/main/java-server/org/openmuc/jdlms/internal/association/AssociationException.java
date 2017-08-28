package org.openmuc.jdlms.internal.association;

import org.openmuc.jdlms.internal.APdu;
import org.openmuc.jdlms.internal.ServiceError;
import org.openmuc.jdlms.internal.StateError;
import org.openmuc.jdlms.internal.asn1.cosem.COSEMpdu;
import org.openmuc.jdlms.internal.asn1.cosem.EXCEPTION_Response;
import org.openmuc.jdlms.internal.asn1.cosem.Enum;

public class AssociationException extends GenericAssociationException {

    private final StateError stateError;
    private final ServiceError serviceError;

    public AssociationException(StateError stateError, ServiceError serviceErorr) {
        this.stateError = stateError;
        this.serviceError = serviceErorr;
    }

    @Override
    public APdu getErrorMessageApdu() {
        Enum stateErrorEnum = this.stateError != null ? this.stateError.asEnum() : null;

        Enum serviceErorrEnum = this.serviceError != null ? this.serviceError.asEnum() : null;
        EXCEPTION_Response ex = new EXCEPTION_Response(stateErrorEnum, serviceErorrEnum);

        COSEMpdu cosemPdu = new COSEMpdu();
        cosemPdu.setexception_response(ex);
        return new APdu(null, cosemPdu);
    }

}
