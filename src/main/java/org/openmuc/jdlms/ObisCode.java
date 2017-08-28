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
package org.openmuc.jdlms;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents the address of a remote object according to IEC 62056-6-1. An instance of ObisCode is immutable.
 */
public class ObisCode {

    private static final int NUM_OF_BYTES = 6;

    private static final Pattern OBIS_PATTERN;

    static {
        final String numberFormat = "[0-9]{1,3}";

        String a = "((" + numberFormat + ")-)?";
        String b = "((" + numberFormat + "):)?";
        String c = "(" + numberFormat + ").";
        String d = "(" + numberFormat + ")";
        String e = "(\\.(" + numberFormat + "))?";
        String f = "(\\*(" + numberFormat + "))?";

        OBIS_PATTERN = Pattern.compile("^" + a + b + c + d + e + f + "$");
    }

    private byte[] bytes;

    /**
     * Constructor
     * 
     * @param byteA
     *            First byte of the address
     * @param byteB
     *            Second byte of the address
     * @param byteC
     *            Third byte of the address
     * @param byteD
     *            Fourth byte of the address
     * @param byteE
     *            Fifth byte of the address
     * @param byteF
     *            Sixth byte of the address
     * @throws IllegalArgumentException
     *             If one of the bytes is out of range [0, 255]
     */
    public ObisCode(int byteA, int byteB, int byteC, int byteD, int byteE, int byteF) {
        this.bytes = verifyLengthAndConvertToByteArray(byteA, byteB, byteC, byteD, byteE, byteF);
    }

    /**
     * The reference-id can be written as OBIS number (e.g. 1-b:8.29.0*2) or as a series of six decimal numbers
     * separated by periods (1.1.1.8.0.255).
     * 
     * @param address
     *            Reference-ID
     */
    public ObisCode(String address) {
        String[] addressArray = address.split("\\.");

        if (addressArray.length == NUM_OF_BYTES) {
            int[] bytesInt = { parseInt(addressArray[0]), parseInt(addressArray[1]), parseInt(addressArray[2]),
                    parseInt(addressArray[3]), parseInt(addressArray[4]), parseInt(addressArray[5]) };

            this.bytes = verifyLengthAndConvertToByteArray(bytesInt);

        }
        else {
            Matcher obisMatcher = OBIS_PATTERN.matcher(address);

            if (obisMatcher.matches()) {
                this.bytes = new byte[NUM_OF_BYTES];

                this.bytes[0] = (byte) convertToByte(obisMatcher, 2);
                this.bytes[1] = (byte) convertToByte(obisMatcher, 4);
                this.bytes[2] = (byte) convertToByte(obisMatcher, 5);
                this.bytes[3] = (byte) convertToByte(obisMatcher, 6);
                this.bytes[4] = (byte) convertToByte(obisMatcher, 8);

                int fieldF = convertToByte(obisMatcher, 10);
                if (fieldF == -1) {
                    fieldF = 255;
                }
                this.bytes[5] = (byte) fieldF;
            }
            else {
                throw new IllegalArgumentException("ObisCode is not reduced obis format.");
            }
        }

    }

    public Medium medium() {
        return Medium.mediumFor(this.bytes[0] & 0xFF);
    }

    public int channel() {
        return this.bytes[1] & 0xFF;
    }

    private static int convertToByte(Matcher obisMatcher, int group) {
        String byteStr = obisMatcher.group(group);

        if (byteStr == null) {
            return -1;
        }

        return Integer.parseInt(byteStr);
    }

    public ObisCode(byte[] bytes) {
        if (bytes.length != NUM_OF_BYTES) {
            throw new IllegalArgumentException("ObisCode has the wrong length, not equal.");
        }
        this.bytes = bytes;
    }

    public String asShortObisCodeString() {
        StringBuilder sb = new StringBuilder();

        int i = 0;
        sb.append(format("%d-", bytes[i++] & 0xFF));

        sb.append(format("%d:", bytes[i++] & 0xFF));

        sb.append(format("%d.", bytes[i++] & 0xFF));
        sb.append(format("%d", bytes[i++] & 0xFF));

        sb.append(format(".%d", bytes[i++] & 0xFF));

        int f = bytes[i] & 0xFF;
        if (f != 0xFF) {
            sb.append(format("*%d", f));
        }

        return sb.toString();

    }

    public String asHexCodeString() {
        StringBuilder sb = new StringBuilder(12);
        for (int i = 0; i < 6; ++i) {
            sb.append(format("%02x", bytes[i]));
        }
        return sb.toString();
    }

    public byte[] bytes() {
        return this.bytes.clone();
    }

    public String asDecimalString() {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 5; ++i) {
            sb.append(bytes[i] & 0xFF);
            sb.append('.');
        }
        sb.append(bytes[5] & 0xFF);

        return sb.toString();
    }

    @Override
    public String toString() {
        return asDecimalString();
    }

    private static byte[] verifyLengthAndConvertToByteArray(int... bytesInt) throws IllegalArgumentException {
        for (int b : bytesInt) {
            checkLength(b & 0xFFFFFFFF);
        }

        byte[] data = new byte[NUM_OF_BYTES];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) bytesInt[i];
        }

        return data;
    }

    private static void checkLength(int number) {
        if (number < 0x00 || number > 0xFF) {
            throw new IllegalArgumentException(number + " is out of range [0, 255]");
        }
    }

    public enum Medium {
        ABSTRACT(0),
        ELECTRICITY(1),
        HEAT_COST_ALLOCATOR(4),
        COOLING(5),
        HEAT(6),
        GAS(7),
        COLD_WATER(8),
        HOT_WATER(9),
        RESERVED(-1);

        private int code;

        private Medium(int code) {
            this.code = code;
        }

        private static Medium mediumFor(int code) {
            for (Medium medium : values()) {
                if (medium.code == code) {
                    return medium;
                }
            }

            return Medium.RESERVED;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ObisCode)) {
            return false;
        }

        ObisCode other = (ObisCode) obj;
        return other == this || Arrays.equals(bytes, other.bytes);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(bytes);
    }
}
