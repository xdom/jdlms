package org.openmuc.jdlms.internal.transportlayer.server;

import org.openmuc.jdlms.ServerConnectionInfo;

public abstract class ServerConnectionInformationImpl implements ServerConnectionInfo {
    public Status status;
    public int logicalDeviceAddress;
    public int clientId;

    @Override
    public Status getConnectionStatus() {
        return this.status;
    }

    @Override
    public int getLogicalDeviceAddress() {
        return this.logicalDeviceAddress;
    }

    @Override
    public int getClientId() {
        return this.clientId;
    }
}
