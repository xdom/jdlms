package org.openmuc.jdlms.transportlayer.server;

import java.io.IOException;

public interface ServerTransportLayer extends AutoCloseable {

    void start() throws IOException;

    @Override
    void close() throws IOException;

}
