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
package org.openmuc.jdlms.sessionlayer.client;

import java.io.IOException;

import org.openmuc.jdlms.RawMessageData.RawMessageDataBuilder;

/**
 * Interface to implement a DLMS SessionLayer.
 * 
 * @see HdlcLayer
 * @see WrapperLayer
 */
public interface SessionLayer extends AutoCloseable {

    /**
     * Function to start the connection.
     * 
     * @param eventListener
     *            the listener.
     * @throws IOException
     *             if an error occurs opening the connection to the remote meter.
     */
    void startListening(SessionLayerListener eventListener) throws IOException;

    /**
     * Writes <code>len</code> bytes from the specified byte array starting at offset <code>off</code> to this output
     * stream.
     * 
     * @param tSdu
     *            the data.
     * @param off
     *            the start offset in the data.
     * @param len
     *            the number of bytes to write.
     * @param rawMessageBuilder
     *            the raw byte array message builder for logging purposes.
     * 
     * @throws IOException
     *             if an I/O error occurs. In particular, an IOException is thrown if the output stream is closed.
     */
    void send(byte[] tSdu, int off, int len, RawMessageDataBuilder rawMessageBuilder) throws IOException;

    @Override
    void close() throws IOException;
}
