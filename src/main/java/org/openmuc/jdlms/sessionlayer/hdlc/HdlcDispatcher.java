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
package org.openmuc.jdlms.sessionlayer.hdlc;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.openmuc.jdlms.TcpConnectionBuilder.InetTransportProtocol;
import org.openmuc.jdlms.settings.client.HdlcTcpSettings;
import org.openmuc.jdlms.settings.client.HdlcSettings;
import org.openmuc.jdlms.settings.client.SerialSettings;
import org.openmuc.jdlms.transportlayer.client.Iec21Layer;
import org.openmuc.jdlms.transportlayer.client.TcpLayer;
import org.openmuc.jdlms.transportlayer.client.TransportLayer;
import org.openmuc.jdlms.transportlayer.client.UdpLayer;

public class HdlcDispatcher extends AbstractHdlcConnectionFactory {
    private static HdlcDispatcher instance;

    private final Map<Object, HdlcConnection> hdlcConnectionMap;

    protected HdlcDispatcher() {
        this.hdlcConnectionMap = new HashMap<>();
    }

    public static synchronized HdlcDispatcher instance() {
        if (instance == null) {
            instance = new HdlcDispatcher();
        }
        return instance;
    }

    @Override
    protected synchronized HdlcConnection createHdlcConnection(HdlcSettings settings) {
        HdlcConnection hdlcConnection;

        if (settings instanceof HdlcTcpSettings) {
            HdlcTcpSettings inetSettings = (HdlcTcpSettings) settings;

            hdlcConnection = getCachedConnection(new InetEntry(inetSettings.inetAddress(), inetSettings.port()));

            if (hdlcConnection == null) {
                TransportLayer transportLayer;

                if (inetSettings.tranportProtocol() == InetTransportProtocol.TCP) {
                    transportLayer = new TcpLayer(inetSettings);
                } else {
                    transportLayer = new UdpLayer(inetSettings);
                }

                hdlcConnection = createNewConnection(settings, transportLayer);
            }
        } else if (settings instanceof SerialSettings) {
            SerialSettings serialSettings = (SerialSettings) settings;

            hdlcConnection = getCachedConnection(serialSettings.serialPortName());

            if (hdlcConnection == null) {
                TransportLayer transportLayer = new Iec21Layer(serialSettings);
                hdlcConnection = createNewConnection(settings, transportLayer);
            }
        } else {
            // TODO: handle this properly.
            throw new UnsupportedOperationException();
        }

        hdlcConnection.registerNewListener(settings.addressPair(), listener);
        return hdlcConnection;
    }

    protected HdlcConnection getCachedConnection(Object key) {
        return hdlcConnectionMap.get(key);
    }

    protected HdlcConnection createNewConnection(HdlcSettings settings,
                                                 TransportLayer transportLayer) {
        return new HdlcConnection(settings, transportLayer);
    }

    private class InetEntry {

        private final int port;
        private final InetAddress inetAddress;

        private InetEntry(InetAddress inetAddress, int port) {
            this.inetAddress = inetAddress;
            this.port = port;
        }

        @Override
        public int hashCode() {
            return this.inetAddress.hashCode() ^ this.port;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof InetEntry)) {
                return false;
            }

            InetEntry other = (InetEntry) obj;
            return port == other.port && inetAddress.equals(other.inetAddress);
        }
    }

}
