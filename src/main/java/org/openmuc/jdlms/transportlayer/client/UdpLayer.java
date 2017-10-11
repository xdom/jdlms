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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import org.openmuc.jdlms.settings.client.TcpSettings;

/**
 * The transport layer used to communicate via the User Datagram Protocol (UDP).
 */
public class UdpLayer implements TransportLayer {

    public static final int MAX_UDP_PAYLOAD_SIZE = 65507;

    private TcpSettings settings;
    private DatagramSocket socket;
    private boolean closed;

    private IStream inputStream;
    private OStream outputStream;

    public UdpLayer(TcpSettings settings) {
        this.settings = settings;
        this.closed = true;
    }

    @Override
    public void open() throws IOException {
        this.socket = new DatagramSocket();
        this.closed = false;
        this.inputStream = new IStream();
        this.outputStream = new OStream();
    }

    @Override
    public void setTimeout(int timeout) throws IOException {
        this.socket.setSoTimeout(timeout);
    }

    @Override
    public DataInputStream getInputStream() throws IOException {
        return new DataInputStream(inputStream);
    }

    @Override
    public DataOutputStream getOutpuStream() throws IOException {
        return new DataOutputStream(outputStream);
    }

    @Override
    public void close() throws IOException {
        try {
            this.socket.close();

            this.inputStream.closeStream();
            this.outputStream.closeStream();
        } finally {
            this.closed = true;
        }
    }

    @Override
    public boolean isClosed() {
        return this.closed;
    }

    private class OStream extends OutputStream {

        private ByteArrayOutputStream os;

        public OStream() {
            this.os = new ByteArrayOutputStream(MAX_UDP_PAYLOAD_SIZE);
        }

        public void closeStream() throws IOException {
            this.os.close();
        }

        @Override
        public void close() throws IOException {
            UdpLayer.this.close();
        }

        @Override
        public void write(int b) throws IOException {
            this.os.write(b);

            if (this.os.size() == MAX_UDP_PAYLOAD_SIZE) {
                flush();
            }
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            int remaining = len;
            while (true) {
                int newLen = Math.min(len, MAX_UDP_PAYLOAD_SIZE - this.os.size());

                if (newLen != 0) {
                    this.os.write(b, off + (len - remaining), newLen);
                    remaining -= newLen;
                }

                if (remaining == 0) {
                    break;
                }

                flush();
            }

        }

        @Override
        public void write(byte[] b) throws IOException {
            write(b, 0, b.length);
        }

        @Override
        public synchronized void flush() throws IOException {
            if (this.os.size() == 0) {
                return;
            }

            byte[] data = this.os.toByteArray();
            this.os.reset();

            DatagramPacket packet = new DatagramPacket(data, 0, data.length, settings.inetAddress(), settings.port());
            socket.send(packet);
        }

    }

    private class IStream extends InputStream {

        private InputStream is;
        private Object lock;

        public IStream() {
            this.is = new ByteArrayInputStream(new byte[0]);
            this.lock = new Object();
        }

        public void closeStream() throws IOException {
            this.is.close();
        }

        @Override
        public void close() throws IOException {
            UdpLayer.this.close();
        }

        @Override
        public int read() throws IOException {
            readIfEmpty();
            synchronized (lock) {
                return is.read();
            }

        }

        private void readIfEmpty() throws IOException {
            synchronized (lock) {
                if (is.available() == 0) {
                    readNextPacket();
                }
            }
        }

        @Override
        public int available() throws IOException {
            readIfEmpty();

            return is.available();
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            try {
                readIfEmpty();
            } catch (IOException e) {
                return 0;
            }

            int remaining = len;
            while (true) {
                synchronized (socket) {
                    remaining -= is.read(b, len - remaining, Math.min(is.available(), remaining));
                }

                if (remaining == 0) {
                    return len;
                }

                try {
                    readIfEmpty();
                } catch (IOException e) {
                    return len - remaining;
                }
            }
        }

        @Override
        public int read(byte[] b) throws IOException {
            return read(b, 0, b.length);
        }

        private synchronized void readNextPacket() throws IOException {

            byte[] buf = new byte[MAX_UDP_PAYLOAD_SIZE];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);

            InetAddress address = packet.getAddress();
            int port = packet.getPort();
            if (!address.equals(settings.inetAddress()) || port != settings.port()) {
                System.out.println("Sure??");
                System.out.println(address.equals(settings.inetAddress()));
                System.out.println(address);
                System.out.println(settings.inetAddress());
                System.out.println(port == settings.port());

                readNextPacket();
                return;
            }

            int len = packet.getLength();
            byte[] data = packet.getData();

            synchronized (lock) {
                this.is = new ByteArrayInputStream(data, 0, len);
            }
        }
    }

}
