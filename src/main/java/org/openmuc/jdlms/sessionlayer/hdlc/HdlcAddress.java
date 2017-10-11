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

import static java.lang.String.format;

import java.text.MessageFormat;

public class HdlcAddress {
    /**
     * see EN 62056-46:2002 6.4.2.2.
     */
    private static final int ONE_BYTE_UPPER_BOUND = 0x7F;
    private static final int TWO_BYTE_UPPER_BOUND = 0x3FFF;

    private final int byteLength;
    private final int logicalId;
    private int physicalId;

    public HdlcAddress(int logicalId) {
        if (logicalId > ONE_BYTE_UPPER_BOUND) {
            throw new IllegalArgumentException(
                    format("One byte address exceeded upper bound of 0x%02x.", ONE_BYTE_UPPER_BOUND));
        }

        this.byteLength = 1;
        this.logicalId = logicalId;
    }

    public HdlcAddress(int logicalId, int physicalId) {
        int byteLength = Math.max(addressSizeOf(logicalId), addressSizeOf(physicalId));
        this.byteLength = physicalId == 0 ? byteLength : byteLength * 2;

        this.logicalId = logicalId;
        this.physicalId = physicalId;
    }

    private static int addressSizeOf(int address) throws IllegalArgumentException {
        if (address <= ONE_BYTE_UPPER_BOUND) {
            return 1;
        }
        else if (address <= TWO_BYTE_UPPER_BOUND) {
            return 2;
        }
        else {
            throw new IllegalArgumentException(
                    format("Address 0x%x is out of upper bound 0x%x.", address, TWO_BYTE_UPPER_BOUND));
        }
    }

    public int getLogicalId() {
        return logicalId;
    }

    public int getPhysicalId() {
        return physicalId;
    }

    public int getLength() {
        return byteLength;
    }

    public byte[] encode() throws IllegalArgumentException {
        validateAddress();

        int upperLength = (byteLength + 1) / 2;
        int lowerLength = byteLength / 2;

        byte[] result = new byte[byteLength];

        for (int i = 0; i < upperLength; i++) {
            int shift = 7 * (upperLength - i - 1);
            result[i] = (byte) ((logicalId & (0x7F << shift)) >> (shift) << 1);
        }
        for (int i = 0; i < lowerLength; i++) {
            int shift = 7 * (upperLength - i - 1);
            result[upperLength + i] = (byte) ((physicalId & (0x7F << shift)) >> (shift) << 1);
        }
        // Setting stop bit
        result[byteLength - 1] |= 1;

        return result;
    }

    public static HdlcAddress decode(byte[] data, int length) throws FrameInvalidException {
        int logicalDeviceAddr = 0;
        int physicalDevAddr = 0;

        switch (length) {
        case 1:
            logicalDeviceAddr = (data[0] & 0xFF) >> 1;
            break;
        case 2:
            logicalDeviceAddr = (data[0] & 0xFF) >> 1;
            physicalDevAddr = (data[1] & 0xFF) >> 1;
            break;
        case 4:
            logicalDeviceAddr = ((data[0] & 0xFF) >> 1) << 7;
            logicalDeviceAddr |= ((data[1] & 0xFF) >> 1);
            physicalDevAddr = ((data[2] & 0xFF) >> 1) << 7;
            physicalDevAddr |= (data[3] & 0xFF) >> 1;
            break;
        default:
            throw new FrameInvalidException("Received HdlcAddress has a invalid bytelength of " + length);
        }

        return new HdlcAddress(logicalDeviceAddr, physicalDevAddr);
    }

    private void validateAddress() throws IllegalArgumentException {
        // According to IEC 62056-46, addresses with a byteLength, that are
        // neither 1, 2 or 4, are illegal
        if (byteLength == 1 || byteLength == 2 || byteLength == 4) {

            int upperLength = (byteLength + 1) / 2;
            int lowerLength = byteLength / 2;

            if (!(logicalId >= Math.pow(2d, 7d * upperLength) || physicalId >= Math.pow(2d, 7d * lowerLength)
                    || logicalId < 0 || physicalId < 0)) {
                return;
            }
        }

        throw new IllegalArgumentException("HdlcAddress has a invalid bytelength");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        int ldLength = ((byteLength + 1) / 2) * 2;
        int phLength = (byteLength / 2) * 2;

        String formatStr = MessageFormat.format("%0{0}X", ldLength);
        sb.append(String.format(formatStr, logicalId));

        if (phLength > 0) {
            sb.append(String.format(MessageFormat.format("-%0{0}X", phLength), physicalId));
        }
        return sb.toString();
    }

    /**
     * Checks if the HdlcAddress is a reserved broadcast address Reserved broadcast addresses may never be the source of
     * a message
     * 
     * @return true if the address is a broadcast address
     */
    public boolean isAllStation() {
        if (this.byteLength == 1 || this.byteLength == 2) {
            return this.logicalId == ReservedAddresses.SERVER_UPPER_ALL_STATIONS_1BYTE;

        }
        else if (this.byteLength == 4) {
            return this.logicalId == ReservedAddresses.SERVER_UPPER_ALL_STATIONS_2BYTE;

        }
        else {
            return false;
        }
    }

    /**
     * Checks if the HdlcAddress is a reserved no station address Reserved no station addresses may never be the source
     * of a message.
     * 
     * @return true if the address is a no station address
     */
    public boolean isNoStation() {
        return this.logicalId == ReservedAddresses.NO_STATION && this.physicalId == ReservedAddresses.NO_STATION;
    }

    /**
     * Checks if the HdlcAddress is a reserved calling station address Reserved calling station addresses may only be
     * sent from the server to send an event to the client
     * 
     * @return true if the address is a calling station address
     */
    public boolean isCalling() {
        if (this.byteLength == 2) {

            return this.physicalId == ReservedAddresses.SERVER_LOWER_CALLING_1BYTE;
        }
        else if (this.byteLength == 4) {

            return this.physicalId == ReservedAddresses.SERVER_LOWER_CALLING_2BYTE;
        }
        else {
            return false;

        }

    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof HdlcAddress)) {
            return false;
        }

        HdlcAddress other = (HdlcAddress) o;

        return byteLength == other.byteLength && logicalId == other.logicalId && physicalId == other.physicalId;
    }

    @Override
    public int hashCode() {
        return logicalId << 16 | physicalId;
    }

    /**
     * HdlcAddresses with special meanings.
     */
    public static class ReservedAddresses {
        /**
         * Guaranteed to be received by no one
         */
        public static final int NO_STATION = 0x00;

        /**
         * Guaranteed to be received by no client
         */
        public static final HdlcAddress CLIENT_NO_STATION = new HdlcAddress(NO_STATION);
        /**
         * Identifies client as management process.
         * <p>
         * Not supported by all remote stations
         * </p>
         */
        public static final HdlcAddress CLIENT_MANAGEMENT_PROCESS = new HdlcAddress(0x01);
        /**
         * Identifies client as public client.
         * <p>
         * No password is needed to access remote station with public client. On the other hand public clients have the
         * fewest rights.
         * </p>
         */
        public static final HdlcAddress CLIENT_PUBLIC_CLIENT = new HdlcAddress(0x10);
        /**
         * Client address used by remote stations to send a broadcast message.
         */
        public static final HdlcAddress CLIENT_ALL_STATION = new HdlcAddress(0x7F);

        /**
         * Logical address of the management logical device. This logical device should always be accessible.
         */
        public static final int SERVER_UPPER_MANAGEMENT_LOGICAL_DEVICE = 0x01;
        /**
         * Logical address to send a message to all logical devices of a remote station. One byte version
         */
        public static final int SERVER_UPPER_ALL_STATIONS_1BYTE = 0x7F;
        /**
         * Logical address to send a message to all logical devices of a remote station. Two byte version
         */
        public static final int SERVER_UPPER_ALL_STATIONS_2BYTE = 0x3FFF;

        /**
         * Physical address used by remote stations as source for event messages. One byte version
         */
        public static final int SERVER_LOWER_CALLING_1BYTE = 0x7E;
        /**
         * Physical address used by remote stations as source for event messages. Two byte version
         */
        public static final int SERVER_LOWER_CALLING_2BYTE = 0x3FFE;
    }
}
