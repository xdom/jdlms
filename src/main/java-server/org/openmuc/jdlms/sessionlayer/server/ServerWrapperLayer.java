package org.openmuc.jdlms.sessionlayer.server;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.openmuc.jdlms.sessionlayer.client.WrapperHeader;
import org.openmuc.jdlms.settings.server.ServerSettings;
import org.openmuc.jdlms.transportlayer.StreamAccessor;

public class ServerWrapperLayer implements ServerSessionLayer {

    private final StreamAccessor streamAccessor;

    private int logicalDevice;
    private int clientId;

    private WrapperHeader.WrapperHeaderBuilder headerBuilder;

    private final ServerSettings settings;

    public ServerWrapperLayer(StreamAccessor streamAccessor, ServerSettings settings) {
        this.streamAccessor = streamAccessor;
        this.settings = settings;

        this.logicalDevice = -1;
        this.clientId = -1;
    }

    @Override
    public void send(byte[] data) throws IOException {
        byte[] header = this.headerBuilder.setLength(data.length).build().encode();

        byte[] wpdu = ByteBuffer.allocate(data.length + WrapperHeader.HEADER_LENGTH).put(header).put(data).array();

        this.streamAccessor.getOutpuStream().write(wpdu);
        this.streamAccessor.getOutpuStream().flush();
    }

    @Override
    public byte[] readNextMessage() throws IOException {
        streamAccessor.setTimeout(this.settings.getInactivityTimeout());
        WrapperHeader header = WrapperHeader.decode(streamAccessor, this.settings.getResponseTimeout());

        if (this.logicalDevice == -1 || this.clientId == -1) {
            this.logicalDevice = header.getDestinationWPort();
            this.clientId = header.getSourceWPort();
            this.headerBuilder = WrapperHeader.builder(this.logicalDevice, this.clientId);
        }
        validateHeader(header);

        byte[] data = new byte[header.getPayloadLength()];
        this.streamAccessor.getInputStream().readFully(data);

        return data;
    }

    private void validateHeader(WrapperHeader header) throws IOException {
        if (this.logicalDevice != header.getDestinationWPort() || this.clientId != header.getSourceWPort()) {
            throw new IOException("Illegal message.");
        }
    }

    @Override
    public int getClientId() {
        return this.clientId;
    }

    @Override
    public int getLogicalDeviceId() {
        return this.logicalDevice;
    }

    @Override
    public void close() throws IOException {
        this.streamAccessor.close();
    }

    @Override
    public void initialize() {
        // nothing to do here..
    }

}
