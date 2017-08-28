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
import java.util.regex.Pattern;

import org.openmuc.jdlms.sessionlayer.client.HdlcLayer;
import org.openmuc.jdlms.sessionlayer.client.SessionLayer;
import org.openmuc.jdlms.sessionlayer.hdlc.HdlcAddress;
import org.openmuc.jdlms.sessionlayer.hdlc.HdlcAddressPair;
import org.openmuc.jdlms.settings.client.SerialSettings;
import org.openmuc.jdlms.transportlayer.client.Iec21Layer.DataFlowControl;

/**
 * This connection Builder is used, to build an serial connection to a meter, optionally using the IEC 61056-21
 * protocol, or directly HDLC.
 */
public class SerialConnectionBuilder extends ConnectionBuilder<SerialConnectionBuilder> {

    private static final Pattern IEC_21_PATTERN = Pattern.compile("^[A-Za-z0-9]{0,32}$");

    private String serialPortName;
    private int baudrate;
    private DataFlowControl iec21Handshake;
    private long baudrateChangeTime;
    private String iec21Address;

    /**
     * Constructs a SerialConnectionBuilder.
     * 
     * @param serialPortName
     *            the serial port name.
     */
    public SerialConnectionBuilder(String serialPortName) {
        this.serialPortName = serialPortName;

        this.iec21Handshake = DataFlowControl.DISABLED;
        this.baudrate = 9600;
        this.baudrateChangeTime = 150;
        this.iec21Address = "";
    }

    /**
     * Sets the serial port name.
     * 
     * @param serialPortName
     *            the serial port name.
     * @return the {@link SerialConnectionBuilder} reference.
     */
    public SerialConnectionBuilder setSerialPortName(String serialPortName) {
        this.serialPortName = serialPortName;
        return this;
    }

    /**
     * Specifies the optional field in the IEC 62056-21 request message. The address consists of max 32 characters with
     * values {@code 0-9}, {@code A-Z} or {@code a-z}. Leading zeros in the tariff device address are ignored (i.e.
     * {@code 10203 == 010203 == 00000010203})
     * 
     * <p>
     * This method call enables the handshake.
     * </p>
     * 
     * @param iec21Address
     *            the optional IEC 62056-21 address.
     * 
     * @return the {@link SerialConnectionBuilder} reference.
     * @throws IllegalArgumentException
     *             if the specified address does not match the address character requirements or is {@code null}.
     */
    public SerialConnectionBuilder setIec21Address(String iec21Address) {
        if (iec21Address == null) {
            throw new IllegalArgumentException("IEC 21 address may not be null!");
        }

        if (!IEC_21_PATTERN.matcher(iec21Address).matches()) {
            throw new IllegalArgumentException("The supplied String does not match the IEC 21 address pattern.");
        }

        this.iec21Address = iec21Address;
        enableHandshake();
        return this;
    }

    /**
     * Disables a IEC 62056-21 mode E handshake with the meter. Start directly with HDLC.
     * 
     * @return the {@link SerialConnectionBuilder} reference.
     */
    public SerialConnectionBuilder disableHandshake() {
        this.iec21Handshake = DataFlowControl.DISABLED;

        return this;
    }

    /**
     * Enables a IEC <a href="https://en.wikipedia.org/wiki/IEC_62056#IEC_62056-21">62056-21</a> mode E handshake with
     * the meter.
     * 
     * @return the {@link SerialConnectionBuilder} reference.
     */
    public SerialConnectionBuilder enableHandshake() {
        this.iec21Handshake = DataFlowControl.ENABLED;

        return this;
    }

    /**
     * Sets the baudrate. Default value is 9600.
     * 
     * <p>
     * This method call disables the handshake.
     * </p>
     * 
     * <p>
     * <b>NOTE:</b> this option can't be used with {@link #setBaudRateChangeTime(long)} or
     * {@link #setIec21Address(String)}.
     * </p>
     * 
     * @param baudrate
     *            a positive integer. E.g. 9600.
     * @return the {@link SerialConnectionBuilder} reference.
     * 
     * @see #disableHandshake()
     */
    public SerialConnectionBuilder setBaudRate(int baudrate) {
        this.baudrate = baudrate;
        disableHandshake();
        return this;
    }

    /**
     * Sets the baud rate change delay in <u>ms</u> in the handshake process. Default value is <code>150</code>.
     * 
     * <p>
     * This method call enables the handshake.
     * </p>
     * 
     * <p>
     * <b>NOTE:</b> this option can't be used with {@link #setBaudRate(int)}.
     * </p>
     * 
     * @param baudrateChangeTime
     *            sets the baud rate change time in the IEC 62056-21 handshake sequence.
     * @return the {@link SerialConnectionBuilder} reference.
     * 
     * @see #enableHandshake()
     */
    public SerialConnectionBuilder setBaudRateChangeTime(long baudrateChangeTime) {
        this.baudrateChangeTime = baudrateChangeTime;
        enableHandshake();
        return this;
    }

    @Override
    public DlmsConnection build() throws IOException {
        SerialSettings settings = new HdlcSettingsImpl(this);

        SessionLayer sessionLayer = new HdlcLayer(settings);

        return buildConnection(settings, sessionLayer);
    }

    public class HdlcSettingsImpl extends SettingsImpl implements SerialSettings {
        private final String serialPortName;
        private final int baudrate;
        private final long baudrateChangeTime;
        private final HdlcAddressPair addressPair;
        private final DataFlowControl iec21Handshake;
        private final String iec21Address;

        public HdlcSettingsImpl(SerialConnectionBuilder builder) {
            super(builder);

            this.serialPortName = builder.serialPortName;

            HdlcAddress clientAddress = new HdlcAddress(clientId());
            HdlcAddress serverAddress;
            if (physicalDeviceId() != 0) {
                serverAddress = new HdlcAddress(logicalDeviceId(), physicalDeviceId());
            }
            else {
                serverAddress = new HdlcAddress(logicalDeviceId());
            }
            this.addressPair = new HdlcAddressPair(clientAddress, serverAddress);
            this.baudrate = builder.baudrate;
            this.baudrateChangeTime = builder.baudrateChangeTime;
            this.iec21Handshake = builder.iec21Handshake;
            this.iec21Address = builder.iec21Address;
        }

        @Override
        public String serialPortName() {
            return this.serialPortName;
        }

        @Override
        public HdlcAddressPair addressPair() {
            return this.addressPair;
        }

        @Override
        public int baudrate() {
            return this.baudrate;
        }

        @Override
        public long baudrateChangeDelay() {
            return this.baudrateChangeTime;
        }

        @Override
        public DataFlowControl iec21Handshake() {
            return this.iec21Handshake;
        }

        @Override
        public String iec21Address() {
            return this.iec21Address;
        }

    }
}
