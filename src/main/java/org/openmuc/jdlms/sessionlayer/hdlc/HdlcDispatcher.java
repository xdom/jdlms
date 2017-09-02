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

    private HdlcDispatcher() {
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
