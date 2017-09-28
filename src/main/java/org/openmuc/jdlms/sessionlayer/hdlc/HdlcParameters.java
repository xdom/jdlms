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

import java.nio.ByteBuffer;
import java.text.MessageFormat;

/**
 * This class represents optional parameter that are negotiated during the connection phase between client and server on
 * the HDLC layer.
 * 
 * For more information, see IEC 62056-46 section 6.4.4.4.3.2 and ISO 13239 section 5.5.3.2.2
 */
public class HdlcParameters {
    private static final byte NUM_WINDOW_SIZE_BYTES = 4;

    public static final int MAX_INFORMATION_LENGTH = 2030;
    public static final int MIN_INFORMATION_LENGTH = 128;
    public static final int MIN_WINDOW_SIZE = 1;
    public static final int MAX_WINDOW_SIZE = 7;

    private static final byte MAX_TRANS_INFO_LENGTH_ID = 0x05;
    private static final byte MAX_REC_INFO_LENGTH_ID = 0x06;
    private static final byte TRANS_WINDOW_SIZE_ID = 0x07;
    private static final byte REC_WINDOW_SIZE_ID = 0x08;

    private static final byte FORMAT_IDENTIFIER = (byte) 0x81;
    private static final byte HDLC_PARAM_IDENTIFIER = (byte) 0x80;
    private static final byte USER_PARAM_IDENTIFIER = (byte) 0xF0;

    private int maxTransmitInformationLength;
    private int maxReceiveInformationLength;
    private int transmitWindowSize;
    private int receiveWindowSize;

    public HdlcParameters() {
        this.maxTransmitInformationLength = MAX_INFORMATION_LENGTH;
        this.maxReceiveInformationLength = MAX_INFORMATION_LENGTH;

        this.transmitWindowSize = MIN_WINDOW_SIZE;
        this.receiveWindowSize = MIN_WINDOW_SIZE;
    }

    public HdlcParameters(int transmitInformationLength, int transmitWindowSize) {
        this(MIN_INFORMATION_LENGTH, MIN_WINDOW_SIZE, transmitInformationLength, transmitWindowSize);
    }

    public HdlcParameters(int receiveInformationLength, int receiveWindowSize, int transmitInformationLength,
            int transmitWindowSize) {
        this.maxReceiveInformationLength = 16;
        this.receiveWindowSize = valueConsiderWindowSize(receiveWindowSize);

        this.maxTransmitInformationLength = 60;
        this.transmitWindowSize = valueConsiderWindowSize(transmitWindowSize);
    }

    public int getTransmitInformationLength() {
        return maxTransmitInformationLength;
    }

    public int getTransmitWindowSize() {
        return transmitWindowSize;
    }

    public int getReceiveInformationLength() {
        return maxReceiveInformationLength;
    }

    public int getReceiveWindowSize() {
        return receiveWindowSize;
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "'{'\"maxTransmitInformationLength\": {0}, \"transmitWindowSize\": {1}, \"maxReceiveInformationLength\": {2}, \"receiveWindowSize\": {3}'}'",
                this.maxTransmitInformationLength, this.transmitWindowSize, this.maxReceiveInformationLength,
                this.receiveWindowSize);
    }

    private static int valueConsiderWindowSize(int value) {
        return valueConsiderRange(MIN_WINDOW_SIZE, MAX_WINDOW_SIZE, value);
    }

    private static int valueConsiderInformationLength(int value) {
        return valueConsiderRange(MIN_INFORMATION_LENGTH, MAX_INFORMATION_LENGTH, value);
    }

    private static int valueConsiderRange(int lowerBound, int upperBound, int value) {
        int maxLowValue = Math.max(lowerBound, value);

        return Math.min(maxLowValue, upperBound);
    }

    public static HdlcParameters decode(byte[] data) throws FrameInvalidException {
        ByteBuffer bBuf = ByteBuffer.wrap(data);

        HdlcParameters parameterNegotiation = new HdlcParameters();

        byte nextByte = bBuf.get();

        if (nextByte != FORMAT_IDENTIFIER) {
            throw new FrameInvalidException("Information field is no HDLC parameter negotiation");
        }

        nextByte = bBuf.get();
        while (bBuf.hasRemaining()) {
            if (nextByte == USER_PARAM_IDENTIFIER) {
                readUserId(bBuf);
            }
            else if (nextByte == HDLC_PARAM_IDENTIFIER) {
                readParamId(bBuf, parameterNegotiation);
            }
            else {
                // error?
            }
        }

        return parameterNegotiation;
    }

    private static void readUserId(ByteBuffer bBuf) {
        byte length = bBuf.get();
        bBuf.position(bBuf.position() + length);
    }

    private static void readParamId(ByteBuffer bBuf, HdlcParameters parameterNegotiation) throws FrameInvalidException {
        int numOfRemainingBytes = bBuf.get();
        while (numOfRemainingBytes > 0) {
            int paramIdent = bBuf.get();
            int paramLength = bBuf.get();

            int val = readData(bBuf, paramLength);
            switch (paramIdent) {
            case MAX_TRANS_INFO_LENGTH_ID:
                checkLength(val);
                parameterNegotiation.maxTransmitInformationLength = val;
                break;
            case MAX_REC_INFO_LENGTH_ID:
                checkLength(val);
                parameterNegotiation.maxReceiveInformationLength = val;
                break;
            case TRANS_WINDOW_SIZE_ID:
                checkWindowSize(val);
                parameterNegotiation.transmitWindowSize = val;
                break;
            case REC_WINDOW_SIZE_ID:
                checkWindowSize(val);
                parameterNegotiation.receiveWindowSize = val;
                break;
            default:
                throw new FrameInvalidException("Hdlc parameter unknown");
            }

            numOfRemainingBytes -= (2 + paramLength);
        }
    }

    private static void checkWindowSize(int val) throws FrameInvalidException {
        if (val < MIN_WINDOW_SIZE || val > MAX_WINDOW_SIZE) {
            throw new FrameInvalidException("Window size is out of range: " + val);
        }
    }

    private static void checkLength(int val) throws FrameInvalidException {
        if (val < 32 || val > MAX_INFORMATION_LENGTH) {
            throw new FrameInvalidException("Max info length is out of range: " + val);
        }
    }

    private static int readData(ByteBuffer bBuf, final int length) {
        int result = 0;

        for (int i = 0; i < length; i++) {
            result = (result << 8) | (bBuf.get() & 0xFF);
        }

        return result;
    }

    public byte[] encode() {
        byte numInfoLengthBytes;
        byte[] maxTransmitInformationData;
        byte[] maxReceiveInformationData;
        if (this.maxTransmitInformationLength <= 0xFF && this.maxReceiveInformationLength <= 0xFF) {
            numInfoLengthBytes = 1;
            maxTransmitInformationData = ByteBuffer.allocate(numInfoLengthBytes)
                    .put((byte) this.maxTransmitInformationLength)
                    .array();
            maxReceiveInformationData = ByteBuffer.allocate(numInfoLengthBytes)
                    .put((byte) this.maxReceiveInformationLength)
                    .array();
        }
        else {
            numInfoLengthBytes = 2;
            maxTransmitInformationData = ByteBuffer.allocate(numInfoLengthBytes)
                    .putShort((short) this.maxTransmitInformationLength)
                    .array();
            maxReceiveInformationData = ByteBuffer.allocate(numInfoLengthBytes)
                    .putShort((short) this.maxReceiveInformationLength)
                    .array();
        }

        int numOfBytes = 11 + numInfoLengthBytes * 2 + NUM_WINDOW_SIZE_BYTES * 2;

        byte infoLength = (byte) (numOfBytes - 3);

        return ByteBuffer.allocate(numOfBytes)
                .put(FORMAT_IDENTIFIER)
                .put(HDLC_PARAM_IDENTIFIER)
                .put(infoLength)
                .put(MAX_TRANS_INFO_LENGTH_ID)
                .put(numInfoLengthBytes)
                .put(maxTransmitInformationData)
                .put(MAX_REC_INFO_LENGTH_ID)
                .put(numInfoLengthBytes)
                .put(maxReceiveInformationData)
                .put(TRANS_WINDOW_SIZE_ID)
                .put(NUM_WINDOW_SIZE_BYTES)
                .putInt(this.transmitWindowSize)
                .put(REC_WINDOW_SIZE_ID)
                .put(NUM_WINDOW_SIZE_BYTES)
                .putInt(this.receiveWindowSize)
                .array();

    }

}
