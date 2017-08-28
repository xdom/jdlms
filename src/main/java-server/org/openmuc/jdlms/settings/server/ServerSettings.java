package org.openmuc.jdlms.settings.server;

import org.openmuc.jdlms.ServerConnectionListener;
import org.openmuc.jdlms.settings.client.ReferencingMethod;

public interface ServerSettings {

    int getInactivityTimeout();

    int getResponseTimeout();

    int getMaxClients();

    ServerConnectionListener getConnectionListener();

    ReferencingMethod getReferencingMethod();

}
