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
package org.openmuc.jdlms.internal.asn1.axdr;

import java.io.IOException;
import java.io.InputStream;

import org.openmuc.jasn1.ber.BerByteArrayOutputStream;

public class AxdrLength {

    private int length;

    public AxdrLength() {
    }

    public AxdrLength(int length) {
        this.length = length;
    }

    public static int encodeLength(BerByteArrayOutputStream axdrOStream, int length) throws IOException {
        int codeLength = 0;

        if (length == 0) {
            axdrOStream.write(0);
            codeLength++;
        }
        else {
            int lengthOfLength = 0;
            for (int i = 0; (length >> 8 * (i)) != 0; i++) {
                axdrOStream.write((length >> 8 * (i)) & 0xff);
                lengthOfLength++;
                codeLength++;
            }

            if (length >= 128) {
                axdrOStream.write((byte) ((lengthOfLength & 0xff) | 0x80));
                codeLength++;
            }
        }

        return codeLength;
    }

    public static byte[] encodeLength(int length) throws IOException {

        if (length == 0) {
            return new byte[] { 0 };
        }
        else {
            int lengthOfLength = 1;
            while ((length >> (8 * lengthOfLength)) != 0) {
                lengthOfLength++;
            }

            int offset = 0;

            byte[] buffer;

            if (length >= 128) {
                buffer = new byte[lengthOfLength + 1];
                buffer[0] = (byte) (lengthOfLength | 0x80);
                offset++;
            }
            else {
                buffer = new byte[lengthOfLength];
            }

            for (int i = 0; i < lengthOfLength; i++) {
                buffer[offset + i] = (byte) (length >> (8 * (lengthOfLength - 1 - i)));
            }

            return buffer;

        }
    }

    public int encode(BerByteArrayOutputStream axdrOStream) throws IOException {
        int codeLength = 0;

        if (length == 0) {
            axdrOStream.write(0);
            codeLength++;
        }
        else {
            int lengthOfLength = 0;
            for (int i = 0; (length >> 8 * (i)) != 0; i++) {
                axdrOStream.write((length >> 8 * (i)) & 0xff);
                lengthOfLength++;
                codeLength++;
            }

            if (length >= 128) {
                axdrOStream.write((byte) ((lengthOfLength & 0xff) | 0x80));
                codeLength++;
            }
        }

        return codeLength;
    }

    public int decode(InputStream iStream) throws IOException {
        int codeLength = 0;

        length = iStream.read();
        codeLength++;

        if ((length & 0x80) == 0x80) {
            int encodedLength = length ^ 0x80;
            codeLength += encodedLength;
            length = 0;
            byte[] byteCode = new byte[encodedLength];
            if (iStream.read(byteCode, 0, encodedLength) < encodedLength) {
                throw new IOException("Error Decoding AxdrLength");
            }
            for (int i = 0; i < encodedLength; i++) {
                length |= (byteCode[i] & 0xff) << (8 * (encodedLength - i - 1));
            }
        }

        return codeLength;
    }

    public int getValue() {
        return length;
    }
}
