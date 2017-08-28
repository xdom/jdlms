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
package org.openmuc.jdlms;

import static org.openmuc.jdlms.internal.Constants.DEFAULT_DLMS_PORT;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.openmuc.jdlms.sessionlayer.client.HdlcLayer;
import org.openmuc.jdlms.sessionlayer.client.SessionLayer;
import org.openmuc.jdlms.sessionlayer.client.WrapperLayer;
import org.openmuc.jdlms.sessionlayer.hdlc.HdlcAddress;
import org.openmuc.jdlms.sessionlayer.hdlc.HdlcAddressPair;
import org.openmuc.jdlms.settings.client.HdlcTcpSettings;
import org.openmuc.jdlms.transportlayer.client.TcpLayer;
import org.openmuc.jdlms.transportlayer.client.TransportLayer;
import org.openmuc.jdlms.transportlayer.client.UdpLayer;

/**
 * Builder class to establish a DLMS connection via TCP/IP protocol suite. This includes the transport layers TCP and
 * UDP.
 */
public class TcpConnectionBuilder extends ConnectionBuilder<TcpConnectionBuilder> {

    private InetAddress inetAddress;
    private int port;
    private InetSessionLayerType sessionLayerType;
    private InetTransportProtocol tranportProtocol;

    /**
     * Construct a {@link TcpConnectionBuilder} with client ID 1, logical device address 16 and a default TCP port 4059.
     * 
     * @param inetAddress
     *            the Internet address of the remote meter.
     */
    public TcpConnectionBuilder(InetAddress inetAddress) {
        this.inetAddress = inetAddress;
        this.port = DEFAULT_DLMS_PORT;
        this.sessionLayerType = InetSessionLayerType.WRAPPER;
        this.tranportProtocol = InetTransportProtocol.TCP;

    }

    public TcpConnectionBuilder(String inetAddress) throws UnknownHostException {
        this(InetAddress.getByName(inetAddress));
    }

    /**
     * Set the Internet address of the remote meter.
     * 
     * @param inetAddress
     *            the Internet address.
     * @return the builder.
     */
    public TcpConnectionBuilder setInetAddress(InetAddress inetAddress) {
        this.inetAddress = inetAddress;
        return this;
    }

    public void setTranportProtocol(InetTransportProtocol tranportProtocol) {
        this.tranportProtocol = tranportProtocol;
    }

    /**
     * Set the port of the remote meter.
     * 
     * @param port
     *            the port.
     * @return the builder.
     */
    public TcpConnectionBuilder setPort(int port) {
        this.port = port;
        return this;
    }

    public TcpConnectionBuilder useHdlc() {
        this.sessionLayerType = InetSessionLayerType.HDLC;
        return this;
    }

    public TcpConnectionBuilder useWrapper() {
        this.sessionLayerType = InetSessionLayerType.WRAPPER;
        return this;
    }

    @Override
    public DlmsConnection build() throws IOException {
        TcpSettingsImpl settings = new TcpSettingsImpl(this);

        SessionLayer sessionLayer = buildSessionLayer(settings);

        return buildConnection(settings, sessionLayer);
    }

    private SessionLayer buildSessionLayer(TcpSettingsImpl settings) throws IOException {
        switch (sessionLayerType) {
        case HDLC:
            return new HdlcLayer(settings);

        default:
        case WRAPPER:
            TransportLayer tl;
            if (this.tranportProtocol == InetTransportProtocol.TCP) {
                tl = new TcpLayer(settings);
            }
            else {
                tl = new UdpLayer(settings);
            }

            return new WrapperLayer(settings, tl);
        }
    }

    public class TcpSettingsImpl extends SettingsImpl implements HdlcTcpSettings {

        private final InetAddress inetAddress;
        private final int port;
        private final HdlcAddressPair addressPair;
        private InetTransportProtocol tranportProtocol;

        public TcpSettingsImpl(TcpConnectionBuilder connectionBuilder) {
            super(connectionBuilder);
            this.inetAddress = connectionBuilder.inetAddress;
            this.port = connectionBuilder.port;
            this.tranportProtocol = connectionBuilder.tranportProtocol;

            HdlcAddress source = new HdlcAddress(clientId());
            HdlcAddress destination = new HdlcAddress(logicalDeviceId(), physicalDeviceId());
            this.addressPair = new HdlcAddressPair(source, destination);
        }

        @Override
        public InetAddress inetAddress() {
            return this.inetAddress;
        }

        @Override
        public InetTransportProtocol tranportProtocol() {
            return tranportProtocol;
        }

        @Override
        public int port() {
            return this.port;
        }

        @Override
        public HdlcAddressPair addressPair() {
            return this.addressPair;
        }

    }

    private enum InetSessionLayerType {
        HDLC,
        WRAPPER
    }

    public enum InetTransportProtocol {
        UDP,
        TCP
    }
}
