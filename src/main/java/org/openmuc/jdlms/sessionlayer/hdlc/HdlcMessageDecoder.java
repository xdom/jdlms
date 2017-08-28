package org.openmuc.jdlms.sessionlayer.hdlc;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import org.openmuc.jdlms.FatalJDlmsException;
import org.openmuc.jdlms.JDlmsException.ExceptionId;
import org.openmuc.jdlms.JDlmsException.Fault;
import org.openmuc.jdlms.RawMessageData.MessageSource;
import org.openmuc.jdlms.RawMessageData.RawMessageDataBuilder;
import org.openmuc.jdlms.transportlayer.StreamAccessor;

public class HdlcMessageDecoder {

    private static final int HDLC_LENGTH_MASK = 0x07FF;
    private static final byte HDLC_FLAG = 0x7E;

    public static List<HdlcFrame> decode(RawMessageDataBuilder rawMessageBuilder, StreamAccessor streamAccessor,
            int timeout) throws IOException {
        DataInputStream iStream = streamAccessor.getInputStream();
        List<HdlcFrame> frames = new LinkedList<>();

        streamAccessor.setTimeout(0);
        validateFlag(iStream.readByte());

        do {
            byte[] frameBytes = readFrame(iStream, streamAccessor, timeout);
            if (rawMessageBuilder != null) {
                rawMessageBuilder.setMessageSource(MessageSource.SERVER);
                rawMessageBuilder.setMessage(ByteBuffer.allocate(frameBytes.length + 2)
                        .put(HDLC_FLAG)
                        .put(frameBytes)
                        .put(HDLC_FLAG)
                        .array());
            }

            validateFlag(iStream.readByte());

            try {
                frames.add(HdlcFrame.decode(frameBytes));
            } catch (FrameInvalidException e) {
                // ignore illegal frames
            }

        } while (iStream.available() > 0);

        return frames;
    }

    private static byte[] readFrame(DataInputStream iStream, StreamAccessor streamAccessor, int timeout)
            throws IOException {
        short frameFormat = iStream.readShort();
        if (streamAccessor != null) {
            streamAccessor.setTimeout(timeout);
        }

        int length = HDLC_LENGTH_MASK & frameFormat;

        byte[] data = new byte[length];

        data[0] = (byte) ((frameFormat & 0xFF00) >> 8);
        data[1] = (byte) (frameFormat & 0xFF);
        iStream.readFully(data, 2, length - 2);

        return data;
    }

    private static void validateFlag(byte flag) throws FatalJDlmsException {
        if (flag != HDLC_FLAG) {
            throw new FatalJDlmsException(ExceptionId.HDLC_MSG_INVALID_FLAG, Fault.SYSTEM,
                    String.format("Expected starting/ending Flag 0x7E, but received: 0x%02X.", flag));
        }
    }

    /*
     * Don't let anyone instantiate this class.
     */
    private HdlcMessageDecoder() {
    }

}
