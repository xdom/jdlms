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
import org.openmuc.jdlms.internal.asn1.axdr.AxdrLength;
import org.openmuc.jdlms.internal.asn1.axdr.AxdrType;

public class AxdrBitString implements AxdrType {

    private byte[] bitString;
    private int numBits = 0;
    private final boolean fixedLength;

    /**
     * Use this constructor for decoding variable length bit strings.
     */
    public AxdrBitString() {
        fixedLength = false;
    }

    /**
     * Use this constructor for decoding fixed length bit strings.
     * 
     */
    public AxdrBitString(int numBits) {
        this.numBits = numBits;
        fixedLength = true;
    }

    /**
     * Use this constructor for encoding fixed length bit strings.
     */
    public AxdrBitString(byte[] value) {

        this.bitString = value;
        this.fixedLength = true;
    }

    /**
     * Use this constructor for encoding variable length bit strings.
     */
    public AxdrBitString(byte[] value, int numBits) {
        if (value == null) {
            throw new NullPointerException("value cannot be null");
        }
        if (numBits < 0) {
            throw new IllegalArgumentException("numBits cannot be negative.");
        }
        if (numBits > (value.length * 8)) {
            throw new IllegalArgumentException("'value' is too short to hold all bits.");
        }

        this.numBits = numBits;
        this.bitString = value;
        this.fixedLength = false;
    }

    @Override
    public int encode(BerByteArrayOutputStream axdrOStream) throws IOException {

        int codeLength = bitString.length;

        for (int i = bitString.length - 1; i >= 0; --i) {
            axdrOStream.write(bitString[i]);
        }

        if (!fixedLength) {
            AxdrLength length = new AxdrLength(numBits);
            codeLength += length.encode(axdrOStream);
        }

        return codeLength;
    }

    @Override
    public int decode(InputStream iStream) throws IOException {
        int codeLength = 0;

        if (!fixedLength) {
            AxdrLength l = new AxdrLength();
            codeLength += l.decode(iStream);

            numBits = l.getValue();
        }

        int byteArrayLength = numBits % 8 == 0 ? numBits / 8 : numBits / 8 + 1;

        codeLength += byteArrayLength;

        bitString = new byte[byteArrayLength];
        if (byteArrayLength != 0 && iStream.read(bitString, 0, byteArrayLength) < byteArrayLength) {
            throw new IOException("Error Decoding AxdrBitString");
        }

        return codeLength;
    }

    public byte[] getValue() {
        return bitString;
    }

    public int getNumBits() {
        return numBits;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < numBits; i++) {
            if (((bitString[i / 8] & 0xff) & (0x80 >> (i % 8))) == (0x80 >> (i % 8))) {
                sb.append('1');
            }
            else {
                sb.append('0');
            }
        }
        return sb.toString();
    }
}
