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
package org.openmuc.jdlms.app.client;

import static org.openmuc.jdlms.internal.Constants.DEFAULT_DLMS_PORT;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.openmuc.jdlms.AuthenticationMechanism;
import org.openmuc.jdlms.ConnectionBuilder;
import org.openmuc.jdlms.DlmsConnection;
import org.openmuc.jdlms.HexConverter;
import org.openmuc.jdlms.SecuritySuite;
import org.openmuc.jdlms.SecuritySuite.EncryptionMechanism;
import org.openmuc.jdlms.SerialConnectionBuilder;
import org.openmuc.jdlms.TcpConnectionBuilder;
import org.openmuc.jdlms.TcpConnectionBuilder.InetTransportProtocol;
import org.openmuc.jdlms.internal.cli.CliParameter;
import org.openmuc.jdlms.internal.cli.CliParameterBuilder;
import org.openmuc.jdlms.internal.cli.CliParseException;
import org.openmuc.jdlms.internal.cli.CliParser;
import org.openmuc.jdlms.internal.cli.FlagCliParameter;
import org.openmuc.jdlms.internal.cli.IntCliParameter;
import org.openmuc.jdlms.internal.cli.LongCliParameter;
import org.openmuc.jdlms.internal.cli.StringCliParameter;
import org.openmuc.jdlms.sessionlayer.hdlc.HdlcParameters;
import org.openmuc.jdlms.settings.client.ReferencingMethod;

class ConsoleClientCliParser {

    CliParser cliParser;

    // common options

    private final IntCliParameter logicalDeviceAddress = new CliParameterBuilder("-ld")
            .setDescription(
                    "The address of the logical device inside the server to connect to. This address is also referred to as the server wPort or server SAP. 1 = Management LD.")
            .buildIntParameter("logical_device_address", 1);
    private final IntCliParameter clientId = new CliParameterBuilder("-cid")
            .setDescription(
                    "The client ID which identifies the client. This ID is also referred to as the client access point or client wPort. 16 = public user.")
            .buildIntParameter("client_ID", 16);
    private final FlagCliParameter useShortNameReferencing = new CliParameterBuilder("-sn")
            .setDescription("Use short name referencing instead of logical name referencing.").buildFlagParameter();
    private final IntCliParameter authenticationMechanism = new CliParameterBuilder("-auth")
            .setDescription("Authentication mechanism: 0 = none, 1 = low, 5 = HLS5 GMAC.")
            .buildIntParameter("auth_mech", 0);
    private final IntCliParameter encryptionMechanism = new CliParameterBuilder("-enc")
            .setDescription("Encryption mechanism: -1 = none, 0 = AES-GCM-128.").buildIntParameter("enc_mech", -1);
    private final StringCliParameter encryptionKey = new CliParameterBuilder("-ekey")
            .setDescription("The encryption key. Note that this key is sometimes needed for authentication as well.")
            .buildStringParameter("encryption_key");
    private final StringCliParameter authenticationKey = new CliParameterBuilder("-akey")
            .setDescription("The authentication key in hexadecimal form (e.g. e3e3e3).")
            .buildStringParameter("authentication_key");
    private final StringCliParameter password = new CliParameterBuilder("-pass")
            .setDescription(
                    "A password that is used with authentication mechanism 1 (e.g. an ASCII string: \"pass\" or a hex number \"0xada4d2\"")
            .buildStringParameter("password");
    private final IntCliParameter challengeLength = new CliParameterBuilder("-cl")
            .setDescription("The maximum length of the authentication challenge, from 8 to 64 byte.")
            .buildIntParameter("challenge_length", 16);
    private final IntCliParameter responseTimeout = new CliParameterBuilder("-rt")
            .setDescription("Maximum time the clients waits for an answer from the remote meter.")
            .buildIntParameter("response_timeout", 20_000);
    private final StringCliParameter manufacturerId = new CliParameterBuilder("-mid")
            .setDescription("Manufacturer ID of the client.").buildStringParameter("manufacturer_id", "MMM");
    private final LongCliParameter deviceId = new CliParameterBuilder("-did").setDescription("Unique device ID.")
            .buildLongParameter("device_id", 1);

    // TCP options:

    private final StringCliParameter host = new CliParameterBuilder("-h")
            .setDescription("The address of the device you want to access.")
            .setMandatory()
            .buildStringParameter("host");

    private final IntCliParameter port = new CliParameterBuilder("-p").setDescription("The port to connect to.")
            .buildIntParameter("port", DEFAULT_DLMS_PORT);

    private final FlagCliParameter useHdlc = new CliParameterBuilder("-hdlc")
            .setDescription("Use HDCL layer instead of wrapper layer.").buildFlagParameter();

    private final FlagCliParameter useUdp = new CliParameterBuilder("-udp")
            .setDescription("Use UDP layer instead of TCP layer.").buildFlagParameter();

    // Serial options:

    private final StringCliParameter serialPort = new CliParameterBuilder("-sp")
            .setDescription(
                    "The serial port used for communication. Examples are /dev/ttyS0 (Linux) or COM1 (Windows).")
            .setMandatory()
            .buildStringParameter("serial_port");

    private final IntCliParameter baudRate = new CliParameterBuilder("-bd")
            .setDescription("Baud rate of the serial port.").buildIntParameter("baud_rate", 9600);

    private final LongCliParameter baudRateChangeDelay = new CliParameterBuilder("-d")
            .setDescription(
                    "Delay of baud rate change in ms. USB to serial converters often require a delay of up to 250ms.")
            .buildLongParameter("baud_rate_change_delay", 0);

    private final FlagCliParameter enableBaudRateHandShake = new CliParameterBuilder("-eh")
            .setDescription("Enables the baudrate handshake process.").buildFlagParameter();

    private final StringCliParameter iec21Address = new CliParameterBuilder("-iec")
            .setDescription("Device address, optional field, manufacturer-specific, 32 characters maximum.")
            .buildStringParameter("iec_21_address");

    private final IntCliParameter physicalDeviceAddress = new CliParameterBuilder("-pd")
            .setDescription("The HDLC physical device address.").buildIntParameter("physical_dev_address", 0);

    private final IntCliParameter hdlcInfoFieldLength = new CliParameterBuilder("-hil")
            .setDescription("The HDLC maximum information length in bytes.")
            .buildIntParameter("hdlc_informatin_length", HdlcParameters.MAX_INFORMATION_LENGTH);

    ConsoleClientCliParser() {
        List<CliParameter> commonParameters = new ArrayList<>();
        commonParameters.add(logicalDeviceAddress);
        commonParameters.add(clientId);
        commonParameters.add(useShortNameReferencing);
        commonParameters.add(authenticationMechanism);
        commonParameters.add(encryptionMechanism);
        commonParameters.add(encryptionKey);
        commonParameters.add(authenticationKey);
        commonParameters.add(password);
        commonParameters.add(challengeLength);
        commonParameters.add(responseTimeout);
        commonParameters.add(manufacturerId);
        commonParameters.add(deviceId);
        commonParameters.add(hdlcInfoFieldLength);

        List<CliParameter> tcpParameters = new ArrayList<>();
        tcpParameters.add(host);
        tcpParameters.add(port);
        tcpParameters.addAll(commonParameters);
        tcpParameters.add(useHdlc);
        tcpParameters.add(useUdp);
        // TODO
        // tcpParameters.add(physicalDeviceAddress);

        List<CliParameter> serialParameters = new ArrayList<>();
        serialParameters.add(serialPort);
        serialParameters.add(physicalDeviceAddress);
        serialParameters.addAll(commonParameters);
        serialParameters.add(baudRate);
        serialParameters.add(baudRateChangeDelay);
        serialParameters.add(enableBaudRateHandShake);
        serialParameters.add(iec21Address);

        cliParser = new CliParser("jdlms-console-client", "DLMS/COSEM client application to access meters");
        cliParser.addParameterGroup("tcp", tcpParameters);
        cliParser.addParameterGroup("serial", serialParameters);
    }

    public void parse(String[] args) throws CliParseException {
        cliParser.parseArguments(args);

        if (authenticationMechanism.getValue() == 1) {
            if (!password.isSelected()) {
                throw new CliParseException("The authentication mechanism was set to 1 but no password was set.");
            }
        }
    }

    public GenActionProcessor connectAndCreateConsoleApp() throws CliParseException, IOException {

        GenActionProcessor app;

        switch (cliParser.getSelectedGroup()) {
        case "tcp":
            app = tcpConnect();
            break;
        case "serial":
            app = serialConnect();
            break;
        default:
            throw new IllegalArgumentException("Unknown connection type");
        }

        System.out.print("** Successfully connected to host: \n");
        return app;
    }

    private GenActionProcessor tcpConnect() throws CliParseException, IOException {

        TcpConnectionBuilder connectionBuilder = new TcpConnectionBuilder(InetAddress.getByName(host.getValue()))
                .setPort(port.getValue());

        if (useHdlc.isSelected()) {
            connectionBuilder.useHdlc();
        }
        if (useUdp.isSelected()) {
            connectionBuilder.setTranportProtocol(InetTransportProtocol.UDP);
        }

        return setCommonParameters(connectionBuilder);

    }

    private GenActionProcessor serialConnect() throws CliParseException, IOException {
        SerialConnectionBuilder connectionBuilder = new SerialConnectionBuilder(serialPort.getValue())
                .setBaudRate(baudRate.getValue())
                .setBaudRateChangeTime(baudRateChangeDelay.getValue())
                .setPhysicalDeviceAddress(physicalDeviceAddress.getValue())
                .disableHandshake();

        if (iec21Address.isSelected()) {
            connectionBuilder.setIec21Address(iec21Address.getValue());
        }

        if (enableBaudRateHandShake.isSelected()) {
            connectionBuilder.enableHandshake();
        }

        return setCommonParameters(connectionBuilder);
    }

    private GenActionProcessor setCommonParameters(ConnectionBuilder<?> connectionBuilder)
            throws CliParseException, IOException {
        ReferencingMethod referencingMethod = useShortNameReferencing.isSelected() ? ReferencingMethod.SHORT
                : ReferencingMethod.LOGICAL;

        connectionBuilder.setLogicalDeviceId(logicalDeviceAddress.getValue())
                .setResponseTimeout(responseTimeout.getValue())
                .setClientId(clientId.getValue())
                .setChallengeLength(challengeLength.getValue())
                .setSystemTitle(manufacturerId.getValue(), deviceId.getValue())
                .setReferencingMethod(referencingMethod)
                .setHdlcMaxInformationLength(hdlcInfoFieldLength.getValue());

        setSecurityLevel(connectionBuilder);

        DlmsConnection connection = connectionBuilder.build();

        if (useShortNameReferencing.isSelected()) {
            return new SnActionProcessor(connection);
        }
        else {
            return new LnActionProcessor(connection);
        }
    }

    private void setSecurityLevel(ConnectionBuilder<?> connectionBuilder) throws CliParseException {

        EncryptionMechanism encryptionMechanismSelected = EncryptionMechanism
                .getInstance(encryptionMechanism.getValue());
        AuthenticationMechanism authenticationMechanismSelected;
        int mechanism = authenticationMechanism.getValue();
        try {
            authenticationMechanismSelected = AuthenticationMechanism.getInstance(mechanism);
        } catch (IllegalArgumentException e) {
            throw new CliParseException("Illegal argument for authentication mechanism: " + mechanism);
        }

        byte[] encryptionKeyBytes = null;
        if ((encryptionMechanismSelected == EncryptionMechanism.AES_GMC_128)
                || authenticationMechanismSelected == AuthenticationMechanism.HLS5_GMAC) {
            if (!encryptionKey.isSelected()) {
                if (encryptionMechanismSelected == EncryptionMechanism.AES_GMC_128) {
                    throw new CliParseException("Encryption mechanism \"" + EncryptionMechanism.AES_GMC_128
                            + "\" was selected but no encryption key was specified using the -ekey parameter.");
                }
                if (authenticationMechanismSelected == AuthenticationMechanism.HLS5_GMAC) {
                    throw new CliParseException("Authentication mechanism \"" + AuthenticationMechanism.HLS5_GMAC
                            + "\" was selected but no encryption key was specified using the -ekey parameter.");
                }
            }
            encryptionKeyBytes = HexConverter.fromShortHexString(encryptionKey.getValue());
        }

        byte[] authenticationKeyBytes = null;
        if ((encryptionMechanismSelected == EncryptionMechanism.AES_GMC_128)
                || authenticationMechanismSelected == AuthenticationMechanism.HLS5_GMAC) {
            if (!authenticationKey.isSelected()) {
                if (encryptionMechanismSelected == EncryptionMechanism.AES_GMC_128) {
                    // TODO according to the standard the authentication key is optional for this encryption method
                    throw new CliParseException("Encryption mechanism \"" + EncryptionMechanism.AES_GMC_128
                            + "\" was selected but no authentication key was specified using the -akey parameter.");
                }
                if (authenticationMechanismSelected == AuthenticationMechanism.HLS5_GMAC) {
                    throw new CliParseException("Authentication mechanism \"" + AuthenticationMechanism.HLS5_GMAC
                            + "\" was selected but no authentication key was specified using the -akey parameter.");
                }
            }
            authenticationKeyBytes = HexConverter.fromShortHexString(authenticationKey.getValue());
        }

        byte[] passwordBytes = null;
        if (authenticationMechanismSelected == AuthenticationMechanism.LOW) {
            String pwVal = password.getValue();
            if (pwVal.startsWith("0x")) {
                passwordBytes = HexConverter.fromShortHexString(pwVal.substring(2));
            }
            else {
                passwordBytes = pwVal.getBytes(StandardCharsets.US_ASCII);
            }
        }

        SecuritySuite securitySuite = SecuritySuite.builder()
                .setAuthenticationMechanism(authenticationMechanismSelected)
                .setEncryptionMechanism(encryptionMechanismSelected)
                .setAuthenticationKey(authenticationKeyBytes)
                .setGlobalUnicastEncryptionKey(encryptionKeyBytes)
                .setPassword(passwordBytes)
                .build();

        connectionBuilder.setSecuritySuite(securitySuite);
    }

    public void printUsage() {
        System.out.println(cliParser.getUsageString());
    }

}
