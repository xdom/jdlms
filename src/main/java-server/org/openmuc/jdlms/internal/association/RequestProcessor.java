package org.openmuc.jdlms.internal.association;

import java.io.IOException;

import org.openmuc.jdlms.internal.asn1.cosem.COSEMpdu;

public interface RequestProcessor {

    void processRequest(COSEMpdu request) throws IOException;

    static abstract class RequestProcessorBase implements RequestProcessor {

        protected final AssociationMessenger associationMessenger;
        protected final RequestProcessorData requestProcessorData;

        public RequestProcessorBase(AssociationMessenger associationMessenger,
                RequestProcessorData requestProcessorData) {
            this.associationMessenger = associationMessenger;
            this.requestProcessorData = requestProcessorData;
        }

        protected Long connectionId() {
            return this.requestProcessorData.connectionData.connectionId;
        }

    }

}
