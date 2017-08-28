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

/*
 * Collection of bit patterns that define the frame type from a control byte
 */
public enum FrameType {

    INFORMATION(0x00, 0x01),
    RECEIVE_READY(0x01, 0x0F),
    RECEIVE_NOT_READY(0x05, 0x0F),
    SET_NORMAL_RESPONSEMODE(0x83, 0xEF),
    DISCONNECT(0x43, 0xEF),
    UNNUMBERED_ACKNOWLEDGE(0x63, 0xEF),
    DISCONNECT_MODE(0x0F, 0xEF),
    FRAME_REJECT(0x87, 0xEF),
    UNNUMBERED_INFORMATION(0xC0, 0xEF),

    ERR_INVALID_TYPE(0xFF, 0xFF);

    private byte value;
    private int mask;

    private FrameType(int value, int mask) {
        this.value = (byte) value;
        this.mask = mask;
    }

    public byte value() {
        return value;
    }

    public static FrameType frameTypeFor(int controlByte) {

        for (FrameType frameType : FrameType.values()) {
            if ((byte) (controlByte & frameType.mask) == frameType.value) {
                return frameType;
            }
        }

        return ERR_INVALID_TYPE;
    }
}
