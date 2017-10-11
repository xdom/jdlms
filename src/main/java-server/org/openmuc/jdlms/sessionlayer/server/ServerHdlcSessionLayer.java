/**
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
 */
package org.openmuc.jdlms.sessionlayer.server;

import static org.openmuc.jdlms.sessionlayer.hdlc.HdlcFrame.newInformationFrame;
import static org.openmuc.jdlms.sessionlayer.hdlc.HdlcFrame.newReceiveReadyFrame;
import static org.openmuc.jdlms.sessionlayer.hdlc.HdlcFrame.newUnnumberedAcknowledgeFrame;
import static org.openmuc.jdlms.sessionlayer.hdlc.HdlcParameters.MAX_INFORMATION_LENGTH;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import org.openmuc.jdlms.RawMessageData.RawMessageDataBuilder;
import org.openmuc.jdlms.internal.association.AssociationShutdownException;
import org.openmuc.jdlms.sessionlayer.client.HdlcSequenceNumber;
import org.openmuc.jdlms.sessionlayer.hdlc.FrameInvalidException;
import org.openmuc.jdlms.sessionlayer.hdlc.FrameType;
import org.openmuc.jdlms.sessionlayer.hdlc.HdlcAddressPair;
import org.openmuc.jdlms.sessionlayer.hdlc.HdlcFrame;
import org.openmuc.jdlms.sessionlayer.hdlc.HdlcFrameSegmentBuffer;
import org.openmuc.jdlms.sessionlayer.hdlc.HdlcMessageDecoder;
import org.openmuc.jdlms.sessionlayer.hdlc.HdlcParameters;
import org.openmuc.jdlms.settings.server.ServerSettings;
import org.openmuc.jdlms.transportlayer.StreamAccessor;

public class ServerHdlcSessionLayer implements ServerSessionLayer {

    private static final int INFORMATION_FRAME_OVERHEAD = 18;
    private final StreamAccessor streamAccessor;
    private final ServerSettings settings;
    private int logicalDeviceId;
    private int clientId;
    private HdlcAddressPair addressPair;
    private HdlcParameters negParams;
    private final HdlcSequenceNumber sendSequenceNum;
    private final HdlcSequenceNumber receiveSequenceNum;

    public ServerHdlcSessionLayer(StreamAccessor streamAccessor, ServerSettings settings) {
        this.streamAccessor = streamAccessor;
        this.settings = settings;

        this.logicalDeviceId = -1;
        this.clientId = -1;

        this.sendSequenceNum = new HdlcSequenceNumber();
        this.receiveSequenceNum = new HdlcSequenceNumber();
    }

    @Override
    public void initialize() throws IOException {
        RawMessageDataBuilder rawMessageBuilder = null;
        List<HdlcFrame> frames = HdlcMessageDecoder.decode(rawMessageBuilder, streamAccessor,
                settings.getResponseTimeout());

        if (frames.size() != 1) {
            // TODO error
        }

        HdlcFrame connectFrame = frames.get(0);

        if (connectFrame.getFrameType() != FrameType.SET_NORMAL_RESPONSEMODE) {
            // TODO error
        }

        this.addressPair = connectFrame.getAddressPair().switchedPair();
        this.clientId = addressPair.destination().getLogicalId();
        this.logicalDeviceId = addressPair.source().getLogicalId();

        HdlcParameters clientParams;
        try {
            clientParams = HdlcParameters.decode(connectFrame.getInformationField());
        } catch (FrameInvalidException e) {
            // byte[] information = null;
            // boolean poll = false;
            // HdlcFrame.newDisconnectModeFrame(addressPair, information, poll);

            // TODO
            throw new AssociationShutdownException();
        }

        int receiveInformationLength = Math.min(MAX_INFORMATION_LENGTH, clientParams.getTransmitInformationLength());
        int receiveWindowSize = HdlcParameters.MIN_WINDOW_SIZE;

        int transmitInformationLength = Math.min(MAX_INFORMATION_LENGTH, clientParams.getReceiveInformationLength());
        int transmitWindowSize = HdlcParameters.MIN_WINDOW_SIZE;

        this.negParams = new HdlcParameters(receiveInformationLength, receiveWindowSize, transmitInformationLength,
                transmitWindowSize);

        boolean finalFlag = true;
        byte[] responseFrame = newUnnumberedAcknowledgeFrame(this.addressPair, this.negParams, finalFlag).encode();

        writeToStream(responseFrame);
    }

    @Override
    public byte[] readNextMessage() throws IOException {
        RawMessageDataBuilder rawMessageBuilder = null;

        HdlcFrame incommingFrame = HdlcMessageDecoder
                .decode(rawMessageBuilder, streamAccessor, this.settings.getResponseTimeout())
                .get(0);

        incommingFrame.getAddressPair(); // TODO check address pairs..

        switch (incommingFrame.getFrameType()) {

        case INFORMATION:
            return handleIncomingInfoFrame(incommingFrame);

        case DISCONNECT:
            boolean finalFrame = true;
            HdlcFrame disconnectAck = newUnnumberedAcknowledgeFrame(this.addressPair, this.negParams, finalFrame);

            writeToStream(disconnectAck.encode());
            throw new AssociationShutdownException();

        case DISCONNECT_MODE:
        case ERR_INVALID_TYPE:
        case FRAME_REJECT:
        case RECEIVE_NOT_READY:
        case RECEIVE_READY:
        case SET_NORMAL_RESPONSEMODE:
        case UNNUMBERED_ACKNOWLEDGE:
        case UNNUMBERED_INFORMATION:
        default:
            throw new IOException();
        }

    }

    private byte[] handleIncomingInfoFrame(HdlcFrame incommingFrame) throws IOException {
        if (incommingFrame.isSegmented()) {
            return readSegmentsFromClient(incommingFrame);
        }
        else {
            return incommingFrame.getInformationFieldWithoutLlc();
        }
    }

    private byte[] readSegmentsFromClient(HdlcFrame incommingFrame) throws IOException {
        HdlcFrameSegmentBuffer segmentBuffer = new HdlcFrameSegmentBuffer();

        segmentBuffer.buffer(incommingFrame);

        do {
            sendReceiveReady(incommingFrame.getReceiveSequence());

            incommingFrame = HdlcMessageDecoder.decode(null, streamAccessor, settings.getResponseTimeout()).get(0);

            if (incommingFrame.getFrameType() != FrameType.INFORMATION) {
                // TODO error
            }
            segmentBuffer.buffer(incommingFrame);

        } while (incommingFrame.isSegmented());

        return segmentBuffer.toByteArray();
    }

    private void sendReceiveReady(int sequenceNumber) throws IOException {
        HdlcFrame receiveReadyFrame = newReceiveReadyFrame(addressPair, sequenceNumber, false);

        writeToStream(receiveReadyFrame.encode());
    }

    @Override
    public void send(byte[] data) throws IOException {

        if (data.length + INFORMATION_FRAME_OVERHEAD >= this.negParams.getTransmitInformationLength()) {
            sendAsSegments(data);
        }
        else {
            boolean segmented = false;
            writeToStream(infoFrameDataFor(data, segmented));
        }

    }

    private void writeToStream(byte[] dataToSend) throws IOException {
        this.streamAccessor.getOutpuStream().write(dataToSend);
        this.streamAccessor.getOutpuStream().flush();
    }

    private void sendAsSegments(byte[] data) throws IOException {
        byte[] segment = new byte[segmentLength()];
        ByteBuffer segmentBuffer = ByteBuffer.wrap(data);

        boolean first = true;
        sendSegment(segment, segmentBuffer, first);

        first = false;

        while (true) {
            waitForRRFrame();

            sendSegment(segment, segmentBuffer, first);

            if (segmentBuffer.remaining() < segmentLength()) {
                if (!segmentBuffer.hasRemaining()) {
                    return;
                }

                segment = new byte[segmentBuffer.remaining()];
            }

        }

    }

    private void waitForRRFrame() throws IOException {
        HdlcFrame rrFrame = HdlcMessageDecoder.decode(null, streamAccessor, this.settings.getResponseTimeout()).get(0);

        if (rrFrame.getFrameType() != FrameType.RECEIVE_READY) {
            // TODO error
            System.err.println(rrFrame.getFrameType());
        }
    }

    private void sendSegment(byte[] segment, ByteBuffer buffer, boolean first) throws IOException {
        buffer.get(segment);

        boolean segmented = buffer.hasRemaining();

        byte[] frameData = infoFrameDataFor(segment, segmented, first);

        writeToStream(frameData);
    }

    private int segmentLength() {
        return this.negParams.getTransmitInformationLength() - INFORMATION_FRAME_OVERHEAD;
    }

    private byte[] infoFrameDataFor(byte[] segment, boolean segmented) {
        return infoFrameDataFor(segment, segmented, true);
    }

    private byte[] infoFrameDataFor(byte[] segment, boolean segmented, boolean addLlc) {
        return newInformationFrame(addressPair, this.sendSequenceNum.increment(), this.receiveSequenceNum.getValue(),
                segment, segmented, addLlc).encode();
    }

    @Override
    public int getClientId() {
        return this.clientId;
    }

    @Override
    public int getLogicalDeviceId() {
        return this.logicalDeviceId;
    }

    @Override
    public void close() throws IOException {
        this.streamAccessor.close();
    }

}
