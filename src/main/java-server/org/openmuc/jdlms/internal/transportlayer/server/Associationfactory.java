package org.openmuc.jdlms.internal.transportlayer.server;

import org.openmuc.jdlms.internal.association.Association;
import org.openmuc.jdlms.sessionlayer.server.ServerSessionLayer;
import org.openmuc.jdlms.settings.server.ServerSettings;

public interface Associationfactory {
    Association newAssociation(ServerSessionLayer sessionLayer, long connectionId, ServerSettings settings,
            ServerConnectionInformationImpl connInfo);
}
