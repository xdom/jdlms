package org.openmuc.jdlms.sessionlayer.server;

import org.openmuc.jdlms.settings.server.ServerSettings;
import org.openmuc.jdlms.transportlayer.StreamAccessor;

public class ServerSessionLayerFactories {

    public static ServerSessionLayerFactory newHdlcSessionLayerFactory() {
        return new ServerSessionLayerFactory() {

            @Override
            public ServerSessionLayer newSesssionLayer(StreamAccessor streamAccessor, ServerSettings settings) {
                return new ServerHdlcSessionLayer(streamAccessor, settings);
            }
        };
    }

    public static ServerSessionLayerFactory newWrapperSessionLayerFactory() {
        return new ServerSessionLayerFactory() {

            @Override
            public ServerSessionLayer newSesssionLayer(StreamAccessor streamAccessor, ServerSettings settings) {
                return new ServerWrapperLayer(streamAccessor, settings);
            }
        };
    }

    private ServerSessionLayerFactories() {
    }
}
