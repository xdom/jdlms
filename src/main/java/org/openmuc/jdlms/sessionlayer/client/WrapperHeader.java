package org.openmuc.jdlms.sessionlayer.client;

import org.openmuc.jdlms.FatalJDlmsException;
import org.openmuc.jdlms.JDlmsException.ExceptionId;
import org.openmuc.jdlms.transportlayer.StreamAccessor;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static java.lang.String.format;
import static org.openmuc.jdlms.JDlmsException.Fault.SYSTEM;

public class WrapperHeader {
    public static final int HEADER_LENGTH = 8;
    private final int version;
    private final int sourceWPort;
    private final int destinationWPort;
    private final int length;
    private final ByteOrder byteOrder;

    private WrapperHeader(WrapperHeaderBuilder builder) {
        this.version = builder.version;
        this.sourceWPort = builder.sourceWPort;
        this.destinationWPort = builder.destinationWPort;
        this.length = builder.length;
        this.byteOrder = builder.byteOrder;
    }

    private WrapperHeader(int version, int sourceWPort, int destinationWPort, int length, ByteOrder byteOrder) {
        this.version = version;
        this.sourceWPort = sourceWPort;
        this.destinationWPort = destinationWPort;
        this.length = length;
        this.byteOrder = byteOrder;
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

    public ByteOrder getByteOrder() {
        return byteOrder;
    }

    public byte[] encode() {
        return ByteBuffer.allocate(HEADER_LENGTH)
                .order(byteOrder)
                .putShort((short) version)
                .putShort((short) sourceWPort)
                .putShort((short) destinationWPort)
                .putShort((short) length)
                .array();
    }

    public static WrapperHeader decode(StreamAccessor streamAccessor, int timeout) throws IOException {
        DataInputStream iStream = streamAccessor.getInputStream();
        byte firstByte = iStream.readByte();
        streamAccessor.setTimeout(timeout);
        if (firstByte == 0x00) {
            return decodeBE(iStream);
        } else if (firstByte == 0x01) {
            return decodeLE(iStream, firstByte);
        } else {
            throw new FatalJDlmsException(ExceptionId.WRAPPER_HEADER_INVALID_VERSION, SYSTEM,
                    "Message does not start with 0x00 or 0x01 as expected in by the wrapper header." +
                            " Starts with: " + Integer.toHexString(firstByte));
        }
    }

    private static WrapperHeader decodeBE(DataInputStream iStream) throws IOException {
        byte version = iStream.readByte();

        if (version != 1) {
            throw new FatalJDlmsException(ExceptionId.WRAPPER_HEADER_INVALID_VERSION, SYSTEM,
                    format("Header version was %d, this stack is only compatible to version 1.", version));
        }

        int sourceWPort = iStream.readUnsignedShort();

        int destinationWPort = iStream.readUnsignedShort();

        int length = iStream.readUnsignedShort();

        return new WrapperHeader(version, sourceWPort, destinationWPort, length, ByteOrder.BIG_ENDIAN);
    }

    private static WrapperHeader decodeLE(DataInputStream iStream, byte version) throws IOException {
        byte secondByte = iStream.readByte();

        if (secondByte != 0x00) {
            throw new FatalJDlmsException(ExceptionId.WRAPPER_HEADER_INVALID_VERSION, SYSTEM,
                    format("Header version was %d, this stack is only compatible to version 1.",
                            (secondByte << 8) + version));
        }

        int sourceWPort = flipShort(iStream.readUnsignedShort());

        int destinationWPort = flipShort(iStream.readUnsignedShort());

        int length = flipShort(iStream.readUnsignedShort());

        return new WrapperHeader(version, sourceWPort, destinationWPort, length, ByteOrder.LITTLE_ENDIAN);
    }

    private static int flipShort(int shortNum) {
        return (shortNum >> 8) + ((shortNum & 0xFF) << 8);
    }

    public static WrapperHeaderBuilder builder(int sourceWPort, int destinationWPort) {
        return builder(sourceWPort, destinationWPort, ByteOrder.BIG_ENDIAN);
    }

    public static WrapperHeaderBuilder builder(int sourceWPort, int destinationWPort, ByteOrder byteOrder) {
        return new WrapperHeaderBuilder(sourceWPort, destinationWPort, byteOrder);
    }

    public static class WrapperHeaderBuilder {
        private final int version;
        private final int sourceWPort;
        private final int destinationWPort;
        private final ByteOrder byteOrder;
        private int length;

        private WrapperHeaderBuilder(int sourceWPort, int destinationWPort, ByteOrder byteOrder) {
            this.version = 1;
            this.sourceWPort = sourceWPort;
            this.destinationWPort = destinationWPort;
            this.byteOrder = byteOrder;
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
