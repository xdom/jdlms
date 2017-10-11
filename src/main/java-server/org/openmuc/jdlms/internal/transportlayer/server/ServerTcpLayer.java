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
package org.openmuc.jdlms.internal.transportlayer.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.openmuc.jdlms.DataDirectory;
import org.openmuc.jdlms.internal.association.Association;
import org.openmuc.jdlms.sessionlayer.server.ServerSessionLayer;
import org.openmuc.jdlms.sessionlayer.server.ServerSessionLayerFactory;
import org.openmuc.jdlms.settings.server.TcpServerSettings;
import org.openmuc.jdlms.transportlayer.StreamAccessor;
import org.openmuc.jdlms.transportlayer.server.ServerTransportLayer;

public class ServerTcpLayer implements ServerTransportLayer {

    private SocketListener socketListener;
    private ExecutorService serverExec;
    private final TcpServerSettings settings;
    private final DataDirectory dataDirectory;
    private final ServerSessionLayerFactory sessionLayerFactory;

    public ServerTcpLayer(TcpServerSettings settings, DataDirectory dataDirectory,
            ServerSessionLayerFactory sessionLayerFactory) {
        this.settings = settings;
        this.dataDirectory = dataDirectory;
        this.sessionLayerFactory = sessionLayerFactory;
    }

    @Override
    public void close() throws IOException {
        this.socketListener.close();
        this.serverExec.shutdown();
    }

    @Override
    public void start() throws IOException {
        this.socketListener = new SocketListener();
        serverExec = Executors.newSingleThreadExecutor();
        serverExec.submit(socketListener);
    }

    public static class TcpServerConnectionInformation extends ServerConnectionInformationImpl {

        private InetAddress clienInetAddress;

        public TcpServerConnectionInformation(InetAddress clienInetAddress) {
            this.clienInetAddress = clienInetAddress;
        }

        @Override
        public InetAddress getClientInetAddress() {
            return this.clienInetAddress;
        }
    }

    private class SocketListener implements Runnable, AutoCloseable {

        private final ServerSocket serverSocket;
        private final ThreadPoolExecutor threadPool;
        private final int maxPermits;

        private long connections;
        private boolean run;

        public SocketListener() throws IOException {
            this.serverSocket = new ServerSocket(settings.getTcpPort());
            this.serverSocket.setSoTimeout(0);

            this.maxPermits = settings.getMaxClients() == 0 ? Integer.MAX_VALUE : settings.getMaxClients();
            this.threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
            this.connections = 0L;
            this.run = true;
        }

        @Override
        public void run() {
            try {
                saveRun();
            } catch (IOException e) {
                // ignore here, connection will be closed
            }

        }

        private void saveRun() throws IOException {
            while (this.run) {
                acceptCon();
            }
        }

        private void acceptCon() throws IOException {
            Socket socket = this.serverSocket.accept();
            if (this.threadPool.getActiveCount() >= this.maxPermits) {
                try {
                    socket.close();
                } catch (IOException e) {
                    // ignore
                }
                return;
            }

            ServerSessionLayer sessionLayer = sessionLayerFactory.newSesssionLayer(new TcpSocketAccessor(socket),
                    settings);

            Long connectionId = ++connections;

            Association association = new Association(dataDirectory, sessionLayer, connectionId, settings,
                    new TcpServerConnectionInformation(socket.getInetAddress()));

            this.threadPool.execute(association);
        }

        @Override
        public void close() throws IOException {
            this.run = false;
            this.serverSocket.close();
            this.threadPool.shutdown();
        }
    }

    private class TcpSocketAccessor implements StreamAccessor {

        private final Socket socket;
        private final DataOutputStream os;
        private final DataInputStream is;

        public TcpSocketAccessor(Socket socket) throws IOException {
            this.socket = socket;

            this.is = new DataInputStream(socket.getInputStream());
            this.os = new DataOutputStream(socket.getOutputStream());

        }

        @Override
        public void setTimeout(int timeout) throws IOException {
            this.socket.setSoTimeout(timeout);
        }

        @Override
        public DataInputStream getInputStream() throws IOException {
            return this.is;
        }

        @Override
        public DataOutputStream getOutpuStream() throws IOException {
            return this.os;
        }

        @Override
        public void close() throws IOException {
            this.socket.close();
        }

    }
}
