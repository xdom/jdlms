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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.openmuc.jdlms.internal.SecSuiteAccessor;
import org.openmuc.jdlms.sessionlayer.client.SessionLayer;
import org.openmuc.jdlms.sessionlayer.hdlc.HdlcParameters;
import org.openmuc.jdlms.settings.client.ReferencingMethod;
import org.openmuc.jdlms.settings.client.Settings;

/**
 * Class to create a {@link DlmsConnection}.
 * 
 * @param <T>
 *            the concrete connection builder.
 * 
 * @see TcpConnectionBuilder
 * @see SerialConnectionBuilder
 */
public abstract class ConnectionBuilder<T extends ConnectionBuilder<T>> {

    private byte[] systemTitle;

    private int challengeLength;

    private int responseTimeout;

    private int logicalDeviceId;

    private int clientId;

    private SecuritySuite securitySuite;

    private int physicalDeviceId;

    private ReferencingMethod referencingMethod;

    private Map<ObisCode, SnObjectInfo> snObjectMapping;

    private RawMessageListener rawMessageListener;

    private int hdlcMaxInformationLength;

    /**
     * Create a new connection builder, with default settings.
     */
    public ConnectionBuilder() {
        this.systemTitle = new byte[] { 0x4d, 0x4d, 0x4d, 0, 0, 0, 0, 1 };

        this.challengeLength = 64;

        this.responseTimeout = 5000;

        this.clientId = 16;
        this.logicalDeviceId = 1;

        this.securitySuite = SecuritySuite.builder().build();

        this.physicalDeviceId = 0;

        this.referencingMethod = ReferencingMethod.LOGICAL;

        this.rawMessageListener = null;

        this.hdlcMaxInformationLength = HdlcParameters.MAX_INFORMATION_LENGTH;
    }

    /**
     * Set the referencing method used to address COSEM objects in the remote meter.
     * 
     * <p>
     * The default referencing method is {@link ReferencingMethod#LOGICAL}.
     * </p>
     * 
     * @param referencingMethod
     *            the referencing method.
     * @return the ConnectionBuilder
     */
    public T setReferencingMethod(ReferencingMethod referencingMethod) {
        this.referencingMethod = referencingMethod;

        return self();
    }

    /**
     * Set the maximum HDLC information length.
     * 
     * <p>
     * The information length is in the range of [128, 2030].
     * </p>
     * 
     * @param hdlcMaxInformationLength
     *            the information length in #bytes.
     */
    public void setHdlcMaxInformationLength(int hdlcMaxInformationLength) {
        this.hdlcMaxInformationLength = hdlcMaxInformationLength;
    }

    /**
     * Set the LN -&gt; SN mapping, so the connection does not have to retrieve the mapping when opening the SN
     * connection.
     * 
     * <p>
     * Access the mapping of a short name addressing connection with
     * {@linkplain SnObjectInfo#retrieveLnSnMappingFrom(DlmsConnection)}.
     * </p>
     * 
     * <p>
     * This also sets the referencing method to short.
     * </p>
     * 
     * @param snObjectMapping
     *            the OBIS code to SN object info map
     * @return the ConnectionBuilder
     * @see SnObjectInfo#retrieveLnSnMappingFrom(DlmsConnection)
     */
    public T setSnObjectMapping(Map<ObisCode, SnObjectInfo> snObjectMapping) {
        this.snObjectMapping = new HashMap<>(snObjectMapping);
        setReferencingMethod(ReferencingMethod.SHORT);
        return self();
    }

    /**
     * Set {@link SecuritySuite}.
     * 
     * <p>
     * Default authentication is NONE.
     * </p>
     * 
     * @param securitySuite
     *            authentication object
     * @return the ConnectionBuilder
     */
    public T setSecuritySuite(SecuritySuite securitySuite) {
        this.securitySuite = securitySuite;

        return self();
    }

    /**
     * Set the client ID which should be used to connect to the server.
     * 
     * <p>
     * Default client ID is 16 (Public Client).
     * </p>
     * 
     * @param clientId
     *            the client id.
     * @return the ConnectionBuilder
     */
    public T setClientId(int clientId) {
        this.clientId = clientId;

        return self();
    }

    /**
     * Set the logical device ID of the logical device in the physical server.
     * 
     * <p>
     * Default logical device ID is 1 (Management Logical Device).
     * </p>
     * 
     * @param logicalDeviceId
     *            the logical device ID.
     * @return the ConnectionBuilder
     */
    public T setLogicalDeviceId(int logicalDeviceId) {
        this.logicalDeviceId = logicalDeviceId;

        return self();
    }

    /**
     * Change the used challenge length.
     * 
     * <p>
     * The length must be in the range {@code [8, 64]}. Default is {@code 64}.
     * </p>
     * 
     * @param challengeLength
     *            challenge length
     * @return the ConnectionBuilder
     * 
     * @throws IllegalArgumentException
     *             if the integer is not in the range {@code [8, 64]}.
     */
    public T setChallengeLength(int challengeLength) {
        final int minLength = 8;
        final int maxLength = 64;

        if (challengeLength < minLength || challengeLength > maxLength) {
            String msg = MessageFormat.format("Challenge length has to be between {0} and {1}.", minLength, maxLength);
            throw new IllegalArgumentException(msg);
        }
        this.challengeLength = challengeLength;
        return self();
    }

    /**
     * Sets the physical device address.
     * 
     * <p>
     * This is only relevant if a connection over HDLC is used. In this case the physical device address is by default
     * 0.
     * </p>
     * 
     * @param physicalDeviceAddress
     *            the physical device address.
     * 
     * @return the ConnectionBuilder
     */
    public T setPhysicalDeviceAddress(int physicalDeviceAddress) {
        this.physicalDeviceId = physicalDeviceAddress;
        return self();
    }

    /**
     * Sets the time in milliseconds the client waits for a response.
     * 
     * @param responseTimeout
     *            time in milliseconds.
     * @return the ConnectionBuilder
     */
    public T setResponseTimeout(int responseTimeout) {
        this.responseTimeout = responseTimeout;

        return self();
    }

    /**
     * Set the client's system title. It consists of 8 bytes: 3 characters for the manufacturer ID and 5 bytes for the
     * device ID. The default is "MMM" (manufacturer ID) and 1 (device ID).
     * 
     * @param manufacturerId
     *            the manufacturer ID
     * @param deviceId
     *            the device ID
     * @return the ConnectionBuilder
     */
    public T setSystemTitle(String manufacturerId, long deviceId) {
        this.systemTitle = new byte[8];

        byte[] manufacturerIdBytes = manufacturerId.getBytes(StandardCharsets.US_ASCII);

        for (int i = 0; i < 3; i++) {
            systemTitle[i] = manufacturerIdBytes[i];
        }

        for (int i = 0; i < 5; i++) {
            systemTitle[3 + i] = (byte) ((deviceId >> (4 - i) * 8) & 0xff);
        }
        return self();
    }

    /**
     * Set the a RawMessageListener to log the incoming and outgoing messages as byte arrays.
     * 
     * <p>
     * NOTE: listening to the data costs extra computation time.
     * </p>
     * 
     * @param rawMessageListener
     *            the RawMessageListener.
     * @return the builder.
     * 
     * @see RawMessageData
     */
    public T setRawMessageListener(RawMessageListener rawMessageListener) {
        this.rawMessageListener = rawMessageListener;
        return self();
    }

    /**
     * Builds a new DLMS/COSEM Connection.
     * 
     * <p>
     * This method is used if a custom session layer or transport layer is used.
     * </p>
     * 
     * @param settings
     *            the settings for the DLMS/COSEM connection.
     * @param sessionLayer
     *            the session layer.
     * @return A new {@link DlmsConnection} with the given settings.
     * @throws IOException
     *             if an error occurs, while connecting to the meter.
     * 
     * @see #build()
     */
    protected final DlmsConnection buildConnection(Settings settings, SessionLayer sessionLayer) throws IOException {

        DlmsConnection connection;
        if (settings.referencingMethod() == ReferencingMethod.LOGICAL) {
            connection = new DlmsLnConnection(settings, sessionLayer);
        }
        else {
            connection = new DlmsSnConnection(settings, sessionLayer, snObjectMapping);
        }

        try {
            connection.connect();
        } catch (Exception e) {
            try {
                connection.close();
            } catch (Exception closingException) {
                System.err.println("Error occurred, while closing.");
                closingException.printStackTrace();
            }
            throw e;
        }

        return connection;
    }

    /**
     * Builds a new DLMS/COSEM Connection, which is connected to the remote meter.
     * 
     * @return A new {@link DlmsConnection} with the given settings.
     * @throws IOException
     *             if an error occurs, while connecting to the meter.
     */
    public abstract DlmsConnection build() throws IOException;

    @SuppressWarnings("unchecked")
    private T self() {
        return (T) this;
    }

    /**
     * This Settings can be overridden by an implementing Settings.
     */
    protected static abstract class SettingsImpl implements Settings {
        private final byte[] systemTitle;
        private final int challengeLength;
        private final int responseTimeout;
        private final int clientAccessPoint;
        private final int logicalDeviceAddress;
        private final SecuritySuite securitySuite;
        private final int physicalDeviceId;
        private final ReferencingMethod referencingMethod;
        private final RawMessageListener rawMessageListener;
        private final int hdlcMaxInformationLength;

        public SettingsImpl(ConnectionBuilder<?> builder) {
            this.systemTitle = builder.systemTitle;
            this.challengeLength = builder.challengeLength;
            this.responseTimeout = builder.responseTimeout;
            this.clientAccessPoint = builder.clientId;
            this.logicalDeviceAddress = builder.logicalDeviceId;
            this.securitySuite = builder.securitySuite;
            this.physicalDeviceId = builder.physicalDeviceId;
            this.referencingMethod = builder.referencingMethod;
            this.rawMessageListener = builder.rawMessageListener;
            this.hdlcMaxInformationLength = builder.hdlcMaxInformationLength;
        }

        @Override
        public ReferencingMethod referencingMethod() {
            return this.referencingMethod;
        }

        @Override
        public SecuritySuite securitySuite() {
            synchronized (this.securitySuite) {
                return this.securitySuite;
            }
        }

        @Override
        public int challengeLength() {
            return this.challengeLength;
        }

        @Override
        public byte[] systemTitle() {
            return this.systemTitle;
        }

        @Override
        public int responseTimeout() {
            return this.responseTimeout;
        }

        @Override
        public int logicalDeviceId() {
            return this.logicalDeviceAddress;
        }

        @Override
        public int physicalDeviceId() {
            return this.physicalDeviceId;
        }

        @Override
        public int clientId() {
            return this.clientAccessPoint;
        }

        @Override
        public RawMessageListener rawMessageListener() {
            return this.rawMessageListener;
        }

        @Override
        public void updateAuthenticationKey(byte[] authenticationKey) {
            synchronized (securitySuite) {
                ((SecSuiteAccessor) this.securitySuite).updateAuthentciationKey(authenticationKey);
            }
        }

        @Override
        public void updateGlobalEncryptionKey(byte[] globalEncryptionKey) {
            synchronized (securitySuite) {
                ((SecSuiteAccessor) this.securitySuite).updateGlobalUnicastEncryptionKey(globalEncryptionKey);
            }
        }

        @Override
        public int hdlcMaxInformationLength() {
            return this.hdlcMaxInformationLength;
        }

    }
}
