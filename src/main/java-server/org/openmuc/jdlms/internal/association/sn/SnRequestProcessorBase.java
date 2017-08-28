package org.openmuc.jdlms.internal.association.sn;

import org.openmuc.jdlms.AccessResultCode;
import org.openmuc.jdlms.IllegalAttributeAccessException;
import org.openmuc.jdlms.internal.APdu;
import org.openmuc.jdlms.internal.BaseNameRange;
import org.openmuc.jdlms.internal.BaseNameRange.Access;
import org.openmuc.jdlms.internal.BaseNameRangeSet;
import org.openmuc.jdlms.internal.asn1.cosem.COSEMpdu;
import org.openmuc.jdlms.internal.asn1.iso.acse.ACSEApdu;
import org.openmuc.jdlms.internal.association.AssociationMessenger;
import org.openmuc.jdlms.internal.association.RequestProcessorBase;
import org.openmuc.jdlms.internal.association.RequestProcessorData;

abstract class SnRequestProcessorBase extends RequestProcessorBase {

    protected final BaseNameRangeSet nameRangeSet;

    public SnRequestProcessorBase(AssociationMessenger associationMessenger,
            RequestProcessorData requestProcessorData) {
        super(associationMessenger, requestProcessorData);

        this.nameRangeSet = requestProcessorData.directory.baseNameRangesFor(this.requestProcessorData.logicalDeviceId);
    }

    protected final APdu newAPdu() {
        ACSEApdu acseAPdu = null;
        COSEMpdu cosemPdu = new COSEMpdu();

        return new APdu(acseAPdu, cosemPdu);
    }

    protected final Access accessFor(final int variableName, BaseNameRange intersectingRange)
            throws IllegalAttributeAccessException {
        boolean baseNameExistent = intersectingRange != null;
        if (!baseNameExistent) {
            throw new IllegalAttributeAccessException(AccessResultCode.OBJECT_UNDEFINED);
        }
        return intersectingRange.accessFor(variableName);
    }

}
