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
package org.openmuc.jdlms;

import static org.openmuc.jdlms.internal.Constants.DEFAULT_DLMS_PORT;

import java.io.IOException;

import org.openmuc.jdlms.ServerBuilder.ServerSettingsImpl;
import org.openmuc.jdlms.internal.transportlayer.server.ServerTcpLayer;
import org.openmuc.jdlms.sessionlayer.server.ServerSessionLayerFactories;
import org.openmuc.jdlms.sessionlayer.server.ServerSessionLayerFactory;
import org.openmuc.jdlms.settings.server.TcpServerSettings;
import org.openmuc.jdlms.transportlayer.server.ServerTransportLayer;

/**
 * Class representing a physical device (DLMS/COSEM server).
 */
public class DlmsServer implements AutoCloseable {

    private final ServerTransportLayer serverTransportLayer;

    DlmsServer(ServerTransportLayer serverTransportLayer) {
        this.serverTransportLayer = serverTransportLayer;
    }

    /**
     * Starts the DLMS/COSEM server.
     * 
     * @throws IOException
     *             if an exception occurs during the start of the server.
     */
    void start() throws IOException {
        serverTransportLayer.start();
    }

    /**
     * Stops the server immediately. Releases all acquired resources.
     */
    @Override
    public void close() throws IOException {
        this.serverTransportLayer.close();
    }

    /**
     * Sends disconnect messages to all connected clients.
     * 
     * @throws IOException
     *             if an exception occurs, while releasing the resources.
     */
    public void shutdown() throws IOException {
        close();
    }

    /**
     * Create a new TCP server builder.
     * 
     * @param port
     *            the TCP port the server starts listening on.
     * @return a new TcpServerBuilder.
     */
    public static TcpServerBuilder tcpServerBuilder(int port) {
        return new TcpServerBuilder(port);
    }

    /**
     * Create a new TCP server builder. Opening the defaut DLMS port 4059.
     * 
     * @return a new TcpServerBuilder.
     */
    public static TcpServerBuilder tcpServerBuilder() {
        return new TcpServerBuilder(DEFAULT_DLMS_PORT);
    }

    public static class TcpServerSettingsImpl extends ServerSettingsImpl implements TcpServerSettings {

        private int port;

        public TcpServerSettingsImpl(int port) {
            this.port = port;
        }

        @Override
        public int getTcpPort() {
            return this.port;
        }

    }

    /**
     * Builder to create a TCP physical device/server.
     */
    public static class TcpServerBuilder extends ServerBuilder<TcpServerBuilder> {

        private int port;
        private ServerSessionLayerFactory sessionLayerFactory;

        private TcpServerBuilder(int port) {
            this.port = port;
            this.sessionLayerFactory = ServerSessionLayerFactories.newWrapperSessionLayerFactory();
        }

        /**
         * 
         * Set the server session layer factory.
         * 
         * @param sessionLayerFactory
         *            the session layer factory.
         * @return the current builder instance.
         * 
         * @see ServerSessionLayerFactories#newWrapperSessionLayerFactory()
         * @see ServerSessionLayerFactories#newHdlcSessionLayerFactory()
         */
        public TcpServerBuilder setSessionLayerFactory(ServerSessionLayerFactory sessionLayerFactory) {
            this.sessionLayerFactory = sessionLayerFactory;
            return this;
        }

        /**
         * The port a client may access the server.
         * 
         * @param port
         *            the TCP port the server starts listening on.
         * @return the current builder instance.
         */
        public TcpServerBuilder setTcpPort(int port) {
            this.port = port;
            return this;
        }

        @Override
        public DlmsServer build() throws IOException {
            final DataDirectory dataDirectory = parseLogicalDevices();

            final TcpServerSettingsImpl settings = new TcpServerSettingsImpl(this.port);
            setPropertiesTo(settings);

            ServerTcpLayer serverLayer = new ServerTcpLayer(settings, dataDirectory, sessionLayerFactory);
            return newServer(serverLayer);
        }

    }

}
