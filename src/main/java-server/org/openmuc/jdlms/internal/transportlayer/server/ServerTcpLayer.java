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

        public InetAddress clienInetAddress;

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
                while (this.run) {
                    Socket socket = this.serverSocket.accept();
                    if (this.threadPool.getActiveCount() >= this.maxPermits) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            // ignore
                        }
                        continue;
                    }

                    ServerSessionLayer sessionLayer = sessionLayerFactory
                            .newSesssionLayer(new TcpSocketAccessor(socket), settings);

                    Long connectionId = ++connections;

                    Association association = new Association(dataDirectory, sessionLayer, connectionId, settings,
                            new TcpServerConnectionInformation(socket.getInetAddress()));

                    this.threadPool.execute(association);
                }
            } catch (IOException e) {
                // ignore here, connection will be closed
            }

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
