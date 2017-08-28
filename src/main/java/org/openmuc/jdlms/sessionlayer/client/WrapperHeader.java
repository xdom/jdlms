package org.openmuc.jdlms.sessionlayer.client;

import static java.lang.String.format;
import static org.openmuc.jdlms.JDlmsException.Fault.SYSTEM;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.openmuc.jdlms.FatalJDlmsException;
import org.openmuc.jdlms.JDlmsException.ExceptionId;
import org.openmuc.jdlms.transportlayer.StreamAccessor;

public class WrapperHeader {
    public static final int HEADER_LENGTH = 8;
    private final int version;
    private final int sourceWPort;
    private final int destinationWPort;
    private final int length;

    private WrapperHeader(WrapperHeaderBuilder builder) {
        this.version = builder.version;
        this.sourceWPort = builder.sourceWPort;
        this.destinationWPort = builder.destinationWPort;
        this.length = builder.length;
    }

    private WrapperHeader(int version, int sourceWPort, int destinationWPort, int length) {
        this.version = version;
        this.sourceWPort = sourceWPort;
        this.destinationWPort = destinationWPort;
        this.length = length;
    }

    public int getVersion() {
        return version;
    }

    public int getSourceWPort() {
        return sourceWPort;
    }

    public int getDestinationWPort() {
        return destinationWPort;
    }

    public int getPayloadLength() {
        return length;
    }

    public byte[] encode() {
        return ByteBuffer.allocate(HEADER_LENGTH)
                .putShort((short) version)
                .putShort((short) sourceWPort)
                .putShort((short) destinationWPort)
                .putShort((short) length)
                .array();
    }

    public static WrapperHeader decode(StreamAccessor streamAccessor, int timeout) throws IOException {

        DataInputStream iStream = streamAccessor.getInputStream();
        if (iStream.readByte() != 0x00) {
            throw new FatalJDlmsException(ExceptionId.WRAPPER_HEADER_INVALID_VERSION, SYSTEM,
                    "Message does not start with 0x00 as expected in by the wrapper header.");
        }

        streamAccessor.setTimeout(timeout);

        byte version = iStream.readByte();

        if (version != 1) {
            throw new FatalJDlmsException(ExceptionId.WRAPPER_HEADER_INVALID_VERSION, SYSTEM,
                    format("Header version was %d, this stack is only compatible to version 1.", version));
        }

        int sourceWPort = iStream.readUnsignedShort();

        int destinationWPort = iStream.readUnsignedShort();

        int length = iStream.readUnsignedShort();

        return new WrapperHeader(version, sourceWPort, destinationWPort, length);
    }

    public static WrapperHeaderBuilder builder(int sourceWPort, int destinationWPort) {
        return new WrapperHeaderBuilder(sourceWPort, destinationWPort);
    }

    public static class WrapperHeaderBuilder {
        private final int version;
        private final int sourceWPort;
        private final int destinationWPort;
        private int length;

        private WrapperHeaderBuilder(int sourceWPort, int destinationWPort) {
            this.version = 1;
            this.sourceWPort = sourceWPort;
            this.destinationWPort = destinationWPort;
        }

        public WrapperHeaderBuilder setLength(int length) {
            this.length = length;

            return this;
        }

        public WrapperHeader build() {
            return new WrapperHeader(this);
        }
    }
}
