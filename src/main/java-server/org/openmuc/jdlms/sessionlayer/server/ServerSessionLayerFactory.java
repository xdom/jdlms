package org.openmuc.jdlms.sessionlayer.server;

import org.openmuc.jdlms.settings.server.ServerSettings;
import org.openmuc.jdlms.transportlayer.StreamAccessor;

public interface ServerSessionLayerFactory {

    ServerSessionLayer newSesssionLayer(StreamAccessor streamAccessor, ServerSettings settings);

}
