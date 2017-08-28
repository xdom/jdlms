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

public class AxdrOctetString implements AxdrType {

    private byte[] octetString = new byte[0];

    private int length = 0;

    public AxdrOctetString() {
    }

    public AxdrOctetString(int length) {
        this.length = length;
        octetString = new byte[length];
    }

    public AxdrOctetString(byte[] octetString) {
        if (octetString != null) {
            this.octetString = octetString;
        }
    }

    public AxdrOctetString(int length, byte[] octetString) {
        if (length != 0 && length != octetString.length) {
            throw new IllegalArgumentException("octetString of wrong size");
        }

        this.length = length;
        this.octetString = octetString;
    }

    @Override
    public int encode(BerByteArrayOutputStream axdrOStream) throws IOException {

        int codeLength = 0;
        axdrOStream.write(octetString);
        codeLength = octetString.length;

        if (length == 0) {
            AxdrLength length = new AxdrLength(octetString.length);
            codeLength += length.encode(axdrOStream);
        }

        return codeLength;
    }

    @Override
    public int decode(InputStream iStream) throws IOException {

        int codeLength = 0;
        int length = this.length;

        if (length == 0) {
            AxdrLength l = new AxdrLength();
            codeLength += l.decode(iStream);

            length = l.getValue();
            octetString = new byte[length];
        }

        if (length != 0) {
            if (iStream.read(octetString, 0, length) < length) {
                throw new IOException("Error Decoding AxdrOctetString");
            }
            codeLength += length;
        }

        return codeLength;

    }

    public byte[] getValue() {
        return octetString;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (byte element : octetString) {
            String hexString = Integer.toHexString(element & 0xff);
            if (hexString.length() == 1) {
                builder.append('0');
            }
            builder.append(hexString);
        }
        return builder.toString();
    }
}
