package org.openmuc.jdlms.sessionlayer.server;

import java.io.IOException;

public interface ServerSessionLayer extends AutoCloseable {

    void initialize() throws IOException;

    byte[] readNextMessage() throws IOException;

    void send(byte[] data) throws IOException;

    int getClientId();

    int getLogicalDeviceId();

    @Override
    void close() throws IOException;
}
