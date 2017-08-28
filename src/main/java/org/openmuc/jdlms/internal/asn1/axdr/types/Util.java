package org.openmuc.jdlms.internal.asn1.axdr.types;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

class Util {

    static void readFully(InputStream is, byte[] buffer) throws IOException {

        readFully(is, buffer, 0, buffer.length);

    }

    static void readFully(InputStream is, byte[] buffer, int off, int len) throws IOException {

        do {

            int bytesRead = is.read(buffer, off, len);
            if (bytesRead == -1) {
                throw new EOFException("Unexpected end of input stream.");
            }

            len -= bytesRead;
            off += bytesRead;

        } while (len > 0);

    }

}
