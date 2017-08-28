package org.openmuc.jdlms.internal.association;

public abstract class RequestProcessorBase implements RequestProcessor {

    protected final AssociationMessenger associationMessenger;
    protected final RequestProcessorData requestProcessorData;

    public RequestProcessorBase(AssociationMessenger associationMessenger, RequestProcessorData requestProcessorData) {
        this.associationMessenger = associationMessenger;
        this.requestProcessorData = requestProcessorData;

    }

    protected int logicalDeviceId() {
        return this.requestProcessorData.logicalDeviceId;
    }

    protected Long connectionId() {
        return this.requestProcessorData.connectionData.connectionId;
    }

}
