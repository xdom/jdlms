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
package org.openmuc.jdlms.transportlayer.client;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import javax.net.SocketFactory;

import org.openmuc.jdlms.settings.client.TcpSettings;

/**
 * Class to handle all outgoing and incoming TCP packets over one TCP connection.
 */
public class TcpLayer implements TransportLayer {

    private Socket socket;
    private DataOutputStream os;
    private DataInputStream is;
    private boolean closed;

    private final TcpSettings settings;

    public TcpLayer(TcpSettings settings) {
        this.settings = settings;
        closed = true;
    }

    @Override
    public void setTimeout(int timeout) throws IOException {
        socket.setSoTimeout(timeout);
    }

    @Override
    public DataInputStream getInputStream() {
        return this.is;
    }

    @Override
    public boolean isClosed() {
        return this.closed;
    }

    @Override
    public DataOutputStream getOutpuStream() {
        return this.os;
    }

    @Override
    public synchronized void open() throws IOException {
        if (!closed) {
            throw new IOException("Connection has already been opened..");
        }
        this.socket = SocketFactory.getDefault().createSocket(settings.inetAddress(), settings.port());

        try {
            os = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        } catch (IOException e) {
            unsaveClose(socket);
            throw e;
        }

        try {
            is = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        } catch (IOException e) {
            // this will also close the socket
            unsaveClose(os);
            throw e;
        }
        closed = false;
    }

    /**
     * Will close the TCP connection to the server if its still open and free any resources of this connection.
     */
    @Override
    public void close() throws IOException {
        if (!closed) {
            os.close();

            is.close();
            closed = true;
        }
    }

    private static void unsaveClose(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException e) {
            // ignore
        }
    }

}
