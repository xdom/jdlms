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
import java.nio.ByteBuffer;

import org.openmuc.jasn1.ber.BerByteArrayOutputStream;

public class NullOutputStream extends BerByteArrayOutputStream {

    private static final byte[] BUFFER = new byte[1];

    public NullOutputStream() {
        super(BUFFER, 0);
    }

    @Override
    public void write(int arg0) throws IOException {
        return;
    }

    @Override
    public void write(byte arg0) throws IOException {
        return;
    }

    @Override
    public void write(byte[] byteArray) throws IOException {
        return;
    }

    @Override
    public byte[] getArray() {
        return new byte[0];
    }

    @Override
    public ByteBuffer getByteBuffer() {
        return null;
    }
}
