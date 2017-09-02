/*
 * Copyright 2012-15 Fraunhofer ISE
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
package org.openmuc.jdlms.sessionlayer.client;

import static org.openmuc.jdlms.JDlmsException.ExceptionId.CONNECTION_ALREADY_CLOSED;
import static org.openmuc.jdlms.sessionlayer.hdlc.HdlcFrame.newInformationFrame;
import static org.openmuc.jdlms.sessionlayer.hdlc.HdlcFrame.newReceiveReadyFrame;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.openmuc.jdlms.FatalJDlmsException;
import org.openmuc.jdlms.JDlmsException.Fault;
import org.openmuc.jdlms.RawMessageData;
import org.openmuc.jdlms.RawMessageData.MessageSource;
import org.openmuc.jdlms.RawMessageData.RawMessageDataBuilder;
import org.openmuc.jdlms.RawMessageListener;
import org.openmuc.jdlms.sessionlayer.hdlc.*;
import org.openmuc.jdlms.sessionlayer.hdlc.HdlcConnection.Listener;
import org.openmuc.jdlms.settings.client.HdlcSettings;

/**
 * SessionLayer implementing the HDLC protocol IEC 62056-46 for further details.
 */
public class HdlcLayer implements SessionLayer {

    private SessionLayerListener connectionListener;

    private final HdlcSettings settings;

    private HdlcSequenceNumber sendSeqNum;
    private HdlcSequenceNumber recSeqNum;

    private final HdlcMessageQueue sendQueue;

    private int sendWindowSize;
    private int sendInformationLength;

    private boolean closed;

    private final HdlcConnection hdlcConnection;

    public HdlcLayer(HdlcSettings settings) {
        this(settings, HdlcDispatcher.instance());
    }

    public HdlcLayer(HdlcSettings settings, HdlcConnectionFactory factory) {
        this.settings = settings;

        this.sendSeqNum = new HdlcSequenceNumber();
        this.recSeqNum = new HdlcSequenceNumber();

        this.sendQueue = new HdlcMessageQueue(1);

        this.closed = true;

        this.hdlcConnection = factory.getHdlcConnection(settings, new HdlcConnectionListenerImpl());
    }

    @Override
    public synchronized void startListening(SessionLayerListener listener) throws IOException {
        connectionListener = listener;

        HdlcParameters parameterNegotiation = this.hdlcConnection.open(this.settings);
        this.sendInformationLength = parameterNegotiation.getReceiveInformationLength();
        this.sendWindowSize = parameterNegotiation.getReceiveWindowSize();

        // if (this.sendQueue.getCapacity() < sendWindowSize) {
        // this.sendQueue.reszize(sendWindowSize);
        // }
        this.closed = false;
    }

    @Override
    public synchronized void send(byte[] tSdu, int off, int len, RawMessageDataBuilder rawMessageDataBuilder)
            throws IOException {
        int toIndex = off + len;
        final byte[] data = Arrays.copyOfRange(tSdu, off, toIndex);

        if (len > maxMessageLength()) {
            sendAsSegments(rawMessageDataBuilder, data);
        }
        else {
            boolean segmented = false;
            boolean addLlc = true;
            sendInfoFrame(rawMessageDataBuilder, data, segmented, addLlc);
        }
    }

    private void sendAsSegments(RawMessageDataBuilder rawMessageDataBuilder, byte[] data) throws IOException {
        ByteBuffer segmentBufferBuffer = ByteBuffer.wrap(data);
        byte[] segment = new byte[this.sendInformationLength - 12];

        boolean addLlc = true;
        sendSegment(rawMessageDataBuilder, segmentBufferBuffer, segment, addLlc);

        addLlc = false;
        while (true) {
            sendSegment(rawMessageDataBuilder, segmentBufferBuffer, segment, addLlc);

            if (segmentBufferBuffer.remaining() < segment.length) {
                if (!segmentBufferBuffer.hasRemaining()) {
                    return;
                }

                segment = new byte[segmentBufferBuffer.remaining()];
            }

        }

    }

    private void sendSegment(RawMessageDataBuilder rawMessageDataBuilder, ByteBuffer segmentBufferBuffer,
            byte[] segment, boolean addLlc) throws IOException {
        segmentBufferBuffer.get(segment);
        boolean segmented = segmentBufferBuffer.hasRemaining();

        sendInfoFrame(rawMessageDataBuilder, segment, segmented, addLlc);
    }

    private int maxMessageLength() {
        return this.sendInformationLength * this.sendWindowSize;
    }

    @Override
    public synchronized void close() throws IOException {
        if (this.closed) {
            throw new FatalJDlmsException(CONNECTION_ALREADY_CLOSED, Fault.USER, "Connection has been already closed.");
        }
        try {
            this.hdlcConnection.disconnect(settings);
        } finally {
            closed = true;
            sendSeqNum = null;
            recSeqNum = null;
            sendQueue.clear();
        }
    }

    private void closeUnsafe() {
        try {
            close();
        } catch (IOException e) {
            // ignore
        }
    }

    private void sendInfoFrame(RawMessageDataBuilder rawMessageDataBuilder, byte[] data, boolean segmented,
            boolean addLlc) throws IOException {
        HdlcAddressPair addressPair = settings.addressPair();
        HdlcFrame frame = newInformationFrame(addressPair, this.sendSeqNum.increment(), this.recSeqNum.getValue(), data,
                segmented, addLlc);

        sendAndBufferFrame(rawMessageDataBuilder, frame);
    }

    private void sendAndBufferFrame(RawMessageDataBuilder rawMessageDataBuilder, HdlcFrame infoFrame)
            throws IOException {
        byte[] dataToSend = infoFrame.encode();

        sendQueue.offerMessage(dataToSend, infoFrame.getSendSequence());

        RawMessageListener rawMessageListener = this.settings.rawMessageListener();

        if (rawMessageListener != null) {
            RawMessageData rawMessageData = rawMessageDataBuilder.setMessageSource(MessageSource.CLIENT)
                    .setMessage(dataToSend)
                    .build();
            rawMessageListener.messageCaptured(rawMessageData);
        }

        this.hdlcConnection.send(dataToSend);
    }

    private class HdlcConnectionListenerImpl implements Listener {

        private final HdlcFrameSegmentBuffer segmentBuffer;

        public HdlcConnectionListenerImpl() {
            this.segmentBuffer = new HdlcFrameSegmentBuffer();
        }

        private void sendAcknowledge() throws IOException {
            boolean poll = true;
            byte[] ackFrame = newReceiveReadyFrame(settings.addressPair(), recSeqNum.getValue(), poll).encode();

            MessageSource messageSource = MessageSource.CLIENT;
            notifyRawMessageListener(ackFrame, messageSource);

            try {
                hdlcConnection.send(ackFrame);
            } catch (InterruptedIOException e) {
                // ignore this
            }
        }

        @Override
        public void dataReceived(RawMessageDataBuilder rawMessageDataBuilder, HdlcFrame frame) {
            recSeqNum.increment();

            if (frame.isSegmented()) {
                segmentBuffer.buffer(frame);

                notifyListener(rawMessageDataBuilder);

                try {
                    sendAcknowledge();
                } catch (IOException e) {
                    closeUnsafe();
                    connectionInterrupted(e);
                    return;
                }
            }
            else if (frame.getFrameType() == FrameType.INFORMATION) {
                acknowledgeSendFramesTil(frame.getReceiveSequence());
                byte[] cosemFrame;
                if (!segmentBuffer.isEmpty()) {

                    segmentBuffer.buffer(frame);
                    notifyListener(rawMessageDataBuilder);

                    if (settings.rawMessageListener() != null) {
                        rawMessageDataBuilder.setMessage(this.segmentBuffer.concatFramesBytes());
                    }

                    cosemFrame = segmentBuffer.toByteArray();
                    segmentBuffer.clear();
                }
                else {
                    cosemFrame = frame.getInformationFieldWithoutLlc();
                }

                connectionListener.dataReceived(cosemFrame, rawMessageDataBuilder);
            }
            else if (frame.getFrameType() == FrameType.RECEIVE_READY) {
                acknowledgeSendFramesTil(frame.getReceiveSequence());
                sendQueuedFrames();
            }
        }

        private void notifyListener(RawMessageDataBuilder rawMessageDataBuilder) {
            if (settings.rawMessageListener() != null) {
                RawMessageData rawMessageData = rawMessageDataBuilder.build();
                settings.rawMessageListener().messageCaptured(rawMessageData);
            }
        }

        private void acknowledgeSendFramesTil(int sendSeq) {
            if (sendSeq == 0) {
                sendSeq = 8;
            }

            sendQueue.clearTil(sendSeq);
        }

        @Override
        public void connectionInterrupted(IOException e) {
            connectionListener.connectionInterrupted(e);
        }

        private void sendQueuedFrames() {
            try {
                for (byte[] message : sendQueue) {
                    hdlcConnection.send(message);
                }
            } catch (IOException e) {
                closeUnsafe();
                connectionListener.connectionInterrupted(e);
            }

        }

    }

    private void notifyRawMessageListener(byte[] data, MessageSource messageSource) {
        if (settings.rawMessageListener() == null) {
            return;
        }

        RawMessageData rawMessageData = RawMessageData.builder()
                .setMessageSource(messageSource)
                .setMessage(data)
                .build();
        settings.rawMessageListener().messageCaptured(rawMessageData);
    }

}
