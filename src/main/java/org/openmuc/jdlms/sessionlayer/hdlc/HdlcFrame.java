
/*
 * Copyright 2012-17 Fraunhofer ISE
 *
 * This file is part of jDLMS.
 * For more information visit http://www.openmuc.org
 *
 * jDLMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jDLMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with jDLMS.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openmuc.jdlms.sessionlayer.hdlc;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.util.Arrays;

public class HdlcFrame {

    /**
     * Logical Link Control (LLC) Request.
     * 
     * See IEC 62056-46:2002 + A1 2007 5.3.2
     */
    private static final byte[] LLC_REQUEST;

    static {
        byte destinationLsap = (byte) 0xE6;
        byte sourceLsap = (byte) 0xE6; // last bit z=0 means command
        byte quality = 0x00; // reserved for future use

        LLC_REQUEST = new byte[] { destinationLsap, sourceLsap, quality };
    }

    private static final byte FLAG = 0x7E;

    private final FrameType frameType;

    private final byte[] informationField;

    private int sendSequence;
    private int receiveSequence;
    private boolean segmented;

    private byte controlField;

    private final HdlcAddressPair addressPair;

    private int length;

    private HdlcFrame(FrameType frameType, byte[] informationField, int sendSequence, int receiveSequence,
            boolean segmented, byte controlField, HdlcAddressPair addressPair, int length) {
        this.frameType = frameType;
        this.informationField = informationField;
        this.sendSequence = sendSequence;
        this.receiveSequence = receiveSequence;
        this.segmented = segmented;
        this.controlField = controlField;
        this.addressPair = addressPair;
        this.length = length;
    }

    private HdlcFrame(HdlcAddressPair addressPair, FrameType frameType) {
        this(addressPair, frameType, null);
    }

    private HdlcFrame(HdlcAddressPair addressPair, FrameType frameType, byte[] informationField) {
        this.segmented = false;

        this.sendSequence = -1;
        this.receiveSequence = -1;

        this.frameType = frameType;
        this.addressPair = addressPair;

        this.informationField = informationField;

        this.length = 2 + getDestinationAddress().getLength() + getSourceAddress().getLength() + 1 + 2;
        if (informationField != null) {
            this.length += informationField.length + 2;
        }
    }

    public static HdlcFrame decode(byte[] frame) throws FrameInvalidException {
        final FcsCalc fcsCalc = new FcsCalc();
        ByteBuffer buffer = ByteBuffer.wrap(frame);

        byte frameFormatH = buffer.get();
        if ((frameFormatH & 0xF0) != 0xA0) {
            throw new FrameInvalidException("Illegal frame format");
        }
        boolean segmented = (0x08 & frameFormatH) == 0x08;

        fcsCalc.update(frameFormatH);
        byte frameFormatL = buffer.get();

        int length = frame.length;

        // content can be ignored, has been read in MessageDecoder
        fcsCalc.update(frameFormatL);

        HdlcAddress destination = readAddress(fcsCalc, buffer);
        HdlcAddress source = readAddress(fcsCalc, buffer);

        byte controlField = buffer.get();
        fcsCalc.update(controlField);

        FrameType frameType = readFrameType(controlField);

        verifyFcsCalc(fcsCalc, buffer);

        int sendSequence = 0;
        int receiveSequence = 0;

        switch (frameType) {
        case INFORMATION:
            // Send sequence number are the bits 1 to 3 of the frame type
            // field
            sendSequence = (controlField & 0x0E) >> 1;

        case RECEIVE_READY:
        case RECEIVE_NOT_READY:
            // Receive sequence number are the bits 5 to 7 of the frame type
            // field
            receiveSequence = (controlField & 0xE0) >> 5;
            break;
        default:
            // no seq no
            break;
        }

        byte[] informationField;
        if (buffer.hasRemaining()) {
            informationField = readInformationField(fcsCalc, buffer);
        }
        else {
            informationField = new byte[0];
        }

        HdlcAddressPair addressPair = new HdlcAddressPair(source, destination);
        return new HdlcFrame(frameType, informationField, sendSequence, receiveSequence, segmented, controlField,
                addressPair, length);
    }

    private static byte[] readInformationField(final FcsCalc fcsCalc, ByteBuffer buffer) throws FrameInvalidException {
        byte[] informationField;
        int infoLength = buffer.remaining() - 2;
        informationField = new byte[infoLength];

        for (int i = 0; i < infoLength; i++) {
            byte data = buffer.get();
            fcsCalc.update(data);
            informationField[i] = data;
        }
        verifyFcsCalc(fcsCalc, buffer);
        return informationField;
    }

    private static void verifyFcsCalc(final FcsCalc fcsCalc, ByteBuffer buffer) throws FrameInvalidException {
        fcsCalc.update(buffer.get());
        fcsCalc.update(buffer.get());
        fcsCalc.validateCurrentFcsValue();
    }

    private static FrameType readFrameType(byte controlField) throws FrameInvalidException {
        FrameType frameType = FrameType.frameTypeFor(controlField & 0xFF);
        if (frameType == FrameType.ERR_INVALID_TYPE) {
            FrameRejectReason reason = new FrameRejectReason(controlField);

            throw new FrameInvalidException(MessageFormat.format("Control field unknown {0}", controlField), reason);
        }
        return frameType;
    }

    private static HdlcAddress readAddress(final FcsCalc fcsCalc, ByteBuffer buffer) throws FrameInvalidException {
        byte[] data = new byte[4];
        byte currentByte = 0;
        int length = 0;
        while ((currentByte & 0x01) == 0) {
            if (length == 4) {
                throw new FrameInvalidException("HLDC address is illegal in frame.");
            }
            currentByte = buffer.get();
            fcsCalc.update(currentByte);
            data[length++] = currentByte;
        }

        return HdlcAddress.decode(data, length);
    }

    public static HdlcFrame newInformationFrame(HdlcAddressPair addressPair, int sendSequence, int receiveSequence,
            byte[] data, boolean segmented, boolean addLcc) {
        byte[] informationField = data;
        if (addLcc) {
            informationField = ByteBuffer.allocate(LLC_REQUEST.length + data.length).put(LLC_REQUEST).put(data).array();
        }
        HdlcFrame hdlcFrame = new HdlcFrame(addressPair, FrameType.INFORMATION, informationField);

        hdlcFrame.sendSequence = sendSequence;
        hdlcFrame.receiveSequence = receiveSequence;

        hdlcFrame.segmented = segmented;

        hdlcFrame.controlField = hdlcFrame.frameType.value();
        hdlcFrame.controlField |= ((sendSequence % 8) << 1);
        hdlcFrame.controlField |= ((receiveSequence % 8) << 5);
        if (!segmented) {
            hdlcFrame.controlField |= 0x10;
        }

        return hdlcFrame;
    }

    public static HdlcFrame newReceiveReadyFrame(HdlcAddressPair addressPair, int receiveSeq, boolean poll) {
        HdlcFrame hdlcFrame = new HdlcFrame(addressPair, FrameType.RECEIVE_READY);
        hdlcFrame.segmented = false;

        hdlcFrame.controlField = hdlcFrame.frameType.value();
        hdlcFrame.controlField |= ((receiveSeq % 8) << 5);
        if (poll) {
            hdlcFrame.controlField |= 0x10;
        }

        return hdlcFrame;
    }

    public static HdlcFrame newSetNormalResponseModeFrame(HdlcAddressPair addressPair, HdlcParameters negotiationParams,
            boolean poll) throws IOException {

        byte[] info = null;

        if (negotiationParams != null) {
            info = negotiationParams.encode();
        }

        HdlcFrame hdlcFrame = new HdlcFrame(addressPair, FrameType.SET_NORMAL_RESPONSEMODE, info);
        hdlcFrame.segmented = false;

        hdlcFrame.controlField = hdlcFrame.frameType.value();
        if (poll) {
            hdlcFrame.controlField |= 0x10;
        }
        return hdlcFrame;
    }

    public static HdlcFrame newUnnumberedInformationFrame(HdlcAddressPair addressPair, byte[] information,
            boolean poll) {
        HdlcFrame hdlcFrame = new HdlcFrame(addressPair, FrameType.UNNUMBERED_INFORMATION, information);

        hdlcFrame.segmented = false;

        hdlcFrame.controlField = hdlcFrame.frameType.value();
        if (poll) {
            hdlcFrame.controlField |= 0x10;
        }

        return hdlcFrame;
    }

    public static HdlcFrame newDisconnectFrame(HdlcAddressPair addressPair, boolean poll) {
        HdlcFrame hdlcFrame = new HdlcFrame(addressPair, FrameType.DISCONNECT);

        hdlcFrame.controlField = hdlcFrame.frameType.value();
        if (poll) {
            hdlcFrame.controlField |= 0x10;
        }

        return hdlcFrame;
    }

    // needed?
    public static HdlcFrame newReceiveNotReadyFrame(HdlcAddressPair addressPair, int receiveSeq, boolean poll) {
        HdlcFrame hdlcFrame = new HdlcFrame(addressPair, FrameType.RECEIVE_NOT_READY);
        hdlcFrame.segmented = false;

        hdlcFrame.controlField = hdlcFrame.frameType.value();
        hdlcFrame.controlField |= ((receiveSeq % 8) << 5);
        if (poll) {
            hdlcFrame.controlField |= 0x10;
        }

        return hdlcFrame;
    }

    // needed?
    public static HdlcFrame newUnnumberedAcknowledgeFrame(HdlcAddressPair addressPair, HdlcParameters negotiationParams,
            boolean poll) throws IOException {
        byte[] info;
        if (negotiationParams != null) {
            info = negotiationParams.encode();
        }
        else {
            info = new byte[0];
        }
        HdlcFrame hdlcFrame = new HdlcFrame(addressPair, FrameType.UNNUMBERED_ACKNOWLEDGE, info);

        hdlcFrame.segmented = false;
        hdlcFrame.controlField = hdlcFrame.frameType.value();

        if (poll) {
            hdlcFrame.controlField |= 0x10;
        }

        return hdlcFrame;
    }

    // TODO: chick if this is needed??
    public static HdlcFrame newDisconnectModeFrame(HdlcAddressPair addressPair, byte[] information, boolean poll) {
        HdlcFrame hdlcFrame = new HdlcFrame(addressPair, FrameType.DISCONNECT_MODE, information);
        hdlcFrame.segmented = false;

        hdlcFrame.controlField = hdlcFrame.frameType.value();
        if (poll) {
            hdlcFrame.controlField |= 0x10;
        }

        return hdlcFrame;
    }

    // TODO: chick if this is needed??
    public static HdlcFrame newFrameRejectFrame(HdlcAddressPair addressPair, FrameRejectReason reason, boolean poll) {
        HdlcFrame hdlcFrame = new HdlcFrame(addressPair, FrameType.FRAME_REJECT, reason.encode());
        hdlcFrame.segmented = false;

        hdlcFrame.controlField = hdlcFrame.frameType.value();
        if (poll) {
            hdlcFrame.controlField |= 0x10;
        }

        return hdlcFrame;
    }

    public HdlcAddress getDestinationAddress() {
        return addressPair.destination();
    }

    public HdlcAddress getSourceAddress() {
        return addressPair.source();
    }

    public HdlcAddressPair getAddressPair() {
        return addressPair;
    }

    public FrameType getFrameType() {
        return frameType;
    }

    public byte[] getInformationField() {
        return informationField;
    }

    public byte[] getInformationFieldWithoutLlc() {
        if (hasInformationField() && informationField.length > 0) {
            return Arrays.copyOfRange(this.informationField, LLC_REQUEST.length, this.informationField.length);
        }
        else {
            return this.informationField;
        }

    }

    public int getLength() {
        return length;
    }

    public int getSendSequence() {
        return sendSequence;
    }

    public int getReceiveSequence() {
        return receiveSequence;
    }

    public boolean isSegmented() {
        return segmented;
    }

    public byte[] encode() {
        byte[] data = encodeWithoutFlags();
        return ByteBuffer.allocate(data.length + 2).put(FLAG).put(data).put(FLAG).array();
    }

    public byte[] encodeWithoutFlags() {
        ByteBuffer codeBuffer = ByteBuffer.allocate(length);

        short frameFormat = (short) (0xA000 | length);
        if (segmented) {
            frameFormat |= 0x0800;
        }

        codeBuffer.putShort(frameFormat);
        codeBuffer.put(getDestinationAddress().encode());
        codeBuffer.put(getSourceAddress().encode());
        codeBuffer.put(controlField);

        FcsCalc fcsCalc = new FcsCalc();
        fcsCalc.update(codeBuffer.array(), codeBuffer.position());
        codeBuffer.put(fcsCalc.fcsValueInBytes());

        if (hasInformationField()) {
            fcsCalc.update(fcsCalc.fcsValueInBytes());
            codeBuffer.put(informationField);
            fcsCalc.update(informationField);
            codeBuffer.put(fcsCalc.fcsValueInBytes());
        }

        return codeBuffer.array();
    }

    private boolean hasInformationField() {
        return informationField != null;
    }
}
