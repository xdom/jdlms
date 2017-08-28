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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * Class representing the information field of a FrameReject response frame. For more information, see ISO 13239 section
 * 5.5.3.4.2
 */
public class FrameRejectReason implements Serializable {

    private byte rejectedControlField;

    private int nextSendSequence;
    private int nextReceiveSequence;

    private List<RejectReason> rejectReasons;

    FrameRejectReason(byte controlField) {
        this.rejectedControlField = controlField;

        this.rejectReasons = RejectReason.reasonsFor(controlField);
    }

    private FrameRejectReason() {
    }

    static FrameRejectReason decode(byte[] data) throws IOException {
        FrameRejectReason frameRejectReason = new FrameRejectReason();
        ByteArrayInputStream iStream = new ByteArrayInputStream(data);

        int buffer = iStream.read();

        frameRejectReason.rejectedControlField = (byte) buffer;

        buffer = iStream.read();

        frameRejectReason.nextSendSequence = ((buffer & 0x40) >> 6) | ((buffer & 0x20) >> 4) | ((buffer & 0x10) >> 2);

        frameRejectReason.nextReceiveSequence = ((buffer & 0x04) >> 2) | (buffer & 0x02) | ((buffer & 0x01) << 2);

        buffer = iStream.read();

        frameRejectReason.rejectReasons = RejectReason.reasonsFor((byte) buffer);

        return frameRejectReason;
    }

    public List<RejectReason> rejectReasons() {
        return rejectReasons;
    }

    byte[] encode() {
        byte[] result = new byte[3];

        result[0] = rejectedControlField;

        byte encodedSendSequence = (byte) (((nextSendSequence & 0x01) << 6) | ((nextSendSequence & 0x02) << 4)
                | ((nextSendSequence & 0x04) << 2));

        byte encodedReceiveSequence = (byte) (((nextReceiveSequence & 0x01) << 2) | (nextReceiveSequence & 0x02)
                | ((nextReceiveSequence & 0x04) >> 2));

        result[1] = (byte) (encodedReceiveSequence | encodedSendSequence);
        result[1] |= rejectedControlField & 0x08;

        result[2] = 0;
        for (RejectReason reason : this.rejectReasons) {
            result[2] |= reason.mask();
        }

        return result;
    }

    public enum RejectReason {
        CONTOL_FIELD_UNDEFINED(0x80),
        INVALID_INFORMATION(0x40),
        INFORMATION_SIZE_EXCEEDED(0x20),
        INVALID_RECEIVE_SEQUENCE(0x10);

        private static final RejectReason[] VALUES = values();
        private byte mask;

        private RejectReason(int mask) {
            this.mask = (byte) mask;
        }

        public byte mask() {
            return this.mask;
        }

        public static List<RejectReason> reasonsFor(byte bits) {
            List<RejectReason> reasons = new LinkedList<>();

            for (RejectReason reason : VALUES) {
                if ((reason.mask & bits) == reason.mask) {
                    reasons.add(reason);
                }
            }

            return reasons;
        }
    }

}
