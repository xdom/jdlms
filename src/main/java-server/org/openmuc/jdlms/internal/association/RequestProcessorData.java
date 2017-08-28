package org.openmuc.jdlms.internal.association;

import org.openmuc.jdlms.internal.DataDirectoryImpl;
import org.openmuc.jdlms.internal.ServerConnectionData;

public class RequestProcessorData {
    public final int logicalDeviceId;
    public final DataDirectoryImpl directory;
    public final ServerConnectionData connectionData;

    public RequestProcessorData(int logicalDeviceId, DataDirectoryImpl dataDirectory, ServerConnectionData connectionData) {
        this.logicalDeviceId = logicalDeviceId;
        this.directory = dataDirectory;
        this.connectionData = connectionData;
    }
}
