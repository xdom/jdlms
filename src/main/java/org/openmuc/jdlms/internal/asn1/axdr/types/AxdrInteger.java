/*
 * Copyright Fraunhofer ISE, 2012
 * Author(s): Karsten Mueller-Bier
 * 
 * This file is part of jASN1.
 * For more information visit http://www.openmuc.org
 * 
 * jASN1 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * jASN1 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with jASN1.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package org.openmuc.jdlms.internal.asn1.axdr.types;

import java.io.IOException;
import java.io.InputStream;

import org.openmuc.jasn1.ber.BerByteArrayOutputStream;
import org.openmuc.jdlms.internal.asn1.axdr.AxdrType;

public class AxdrInteger implements AxdrType {

    private long val = 0;

    private Long maxVal = null;

    private Long minVal = null;

    protected byte[] code = null;

    private boolean isFixedLength = false;

    private boolean isUnsigned = false;

    public AxdrInteger() {
    }

    public AxdrInteger(long val) {
        setValue(val);
    }

    public AxdrInteger(byte[] code) {
        this.code = code;
    }

    protected AxdrInteger(long min, long max, long val) {
        minVal = min;
        maxVal = max;
        setValue(val);
        isFixedLength = true;
        isUnsigned = (min >= 0);
    }

    @Override
    public int encode(BerByteArrayOutputStream axdrOStream) throws IOException {
        int codeLength = 0;

        if (code != null) {
            codeLength = code.length;
            axdrOStream.write(code);
        }
        else {
            if (isFixedLength) {
                codeLength = Math.max(getByteLength(minVal), getByteLength(maxVal));

                for (int i = 0; i < codeLength; i++) {
                    axdrOStream.write(((int) (val >> 8 * (i))) & 0xff);
                }
            }
            else {
                if (val >= 0 && val <= 127) {
                    codeLength = 1;
                }
                else {
                    codeLength = getByteLength(val);
                }

                for (int i = 0; i < codeLength; i++) {
                    axdrOStream.write(((int) (val >> 8 * (i))) & 0xff);
                }

                axdrOStream.write((byte) ((codeLength & 0xff) | 0x80));
                codeLength++;
            }
        }
        return codeLength;
    }

    @Override
    public int decode(InputStream iStream) throws IOException {
        int codeLength = 0;
        byte[] byteCode;
        byte length = 0;

        if (isFixedLength) {
            length = (byte) Math.max(getByteLength(minVal), getByteLength(maxVal));
            codeLength = length;
        }
        else {
            length = (byte) iStream.read();

            if ((length & 0x80) == 0x80) {
                length = (byte) (length ^ 0x80);
                codeLength = length + 1;
            }
            else {
                val = length;
                return 1;
            }
        }

        byteCode = new byte[length];
        Util.readFully(iStream, byteCode);

        if ((byteCode[0] & 0x80) == 0x80 && !isUnsigned) {
            val = -1;
            for (int i = 0; i < length; i++) {
                int numShiftBits = 8 * (length - i - 1);
                val &= ((byteCode[i]) << numShiftBits) | ~(0xff << numShiftBits);
            }

        }
        else {
            val = 0;
            for (int i = 0; i < length; i++) {
                val |= (long) (byteCode[i] & 0xff) << (8 * (length - i - 1));
            }
        }

        return codeLength;
    }

    public void encodeAndSave(int encodingSizeGuess) throws IOException {
        BerByteArrayOutputStream berOStream = new BerByteArrayOutputStream(encodingSizeGuess);
        encode(berOStream);
        code = berOStream.getArray();
    }

    private int getByteLength(long val) {
        if (minVal != null && minVal >= 0) {
            for (int i = 1; i <= 8; i++) {
                long upperBound = (long) (Math.pow(2, (i * 8)) - 1);
                if (upperBound >= val) {
                    return i;
                }
            }
        }
        else {
            for (int i = 1; i <= 8; i++) {
                long lowerBound = (long) (Math.pow(2, (i * 8) - 1) * (-1));
                long upperBound = (long) (Math.pow(2, (i * 8) - 1) - 1);
                if (lowerBound <= val && upperBound >= val) {
                    return i;
                }
            }
        }
        // Unreachable, a long has at max 8 Byte
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof AxdrInteger) {
            AxdrInteger other = (AxdrInteger) o;
            if (other.val == val && other.maxVal.equals(maxVal) && other.minVal.equals(minVal)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        return minVal.hashCode() ^ maxVal.hashCode() ^ (int) val;
    }

    public Long getMax() {
        return maxVal;
    }

    public Long getMin() {
        return minVal;
    }

    public long getValue() {
        return val;
    }

    public void setValue(long newVal) {
        if (minVal != null && minVal > newVal) {
            throw new IllegalArgumentException("Value " + newVal + " is smaller than minimum " + minVal);
        }
        if (maxVal != null && maxVal < newVal) {
            throw new IllegalArgumentException("Value " + newVal + " is greater than maximum " + maxVal);
        }

        val = newVal;
    }

    @Override
    public String toString() {
        return "" + val;
    }

}
