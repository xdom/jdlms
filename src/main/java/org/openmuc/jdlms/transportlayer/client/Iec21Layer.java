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

import static org.openmuc.jdlms.JDlmsException.Fault.SYSTEM;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;

import javax.xml.bind.DatatypeConverter;

import org.openmuc.jdlms.FatalJDlmsException;
import org.openmuc.jdlms.JDlmsException.ExceptionId;
import org.openmuc.jdlms.JDlmsException.Fault;
import org.openmuc.jdlms.settings.client.SerialSettings;
import org.openmuc.jrxtx.DataBits;
import org.openmuc.jrxtx.Parity;
import org.openmuc.jrxtx.SerialPort;
import org.openmuc.jrxtx.SerialPortBuilder;
import org.openmuc.jrxtx.StopBits;

/**
 * This class represents a connection on the physical layer according to IEC 62056-21 in protocol mode E.
 */
public class Iec21Layer implements TransportLayer {

    /**
     * Carriage Return.
     */
    private static final int CR = 0x0D;
    /**
     * Line Feed.
     */
    private static final int LF = 0x0A;

    /**
     * {@code / ? }
     */
    private static final byte[] REQUEST_MSG_1 = new byte[] { 0x2F, 0x3F };
    /**
     * {@code ! CR LF}
     */
    private static final byte[] REQUEST_MSG_3 = new byte[] { 0x21, CR, LF };

    /**
     * {@code ACK 2 0 2 CR LF}
     * <p>
     * ACK [HDLC protocol procedure] [initial bd 300] [binary mode]
     * <p>
     */
    private static final byte[] ACKNOWLEDGE = new byte[] { 0x06, 0x32, 0x30, 0x32, CR, LF };

    private SerialPort serialPort;

    private final SerialSettings settings;

    private boolean closed;
    private DataInputStream is;
    private DataOutputStream os;

    public Iec21Layer(SerialSettings settings) {
        this.settings = settings;

        this.closed = true;
    }

    @Override
    public void setTimeout(int timeout) throws IOException {
        try {
            this.serialPort.setSerialPortTimeout(timeout);
        } catch (IOException e) {
            throw new FatalJDlmsException(ExceptionId.JRXTX_INCOMPATIBLE_TO_OS, SYSTEM,
                    "RXTX is not compatible to your OS.", e);
        }
    }

    @Override
    public DataInputStream getInputStream() throws IOException {
        return is;
    }

    @Override
    public DataOutputStream getOutpuStream() throws IOException {
        return this.os;
    }

    @Override
    public boolean isClosed() {
        return this.closed;
    }

    @Override
    public void open() throws IOException {
        if (isClosed()) {
            try {
                if (settings.iec21Handshake() == DataFlowControl.ENABLED) {
                    connectWithHandshake();
                }
                else {
                    this.serialPort = connectWithoutHandshake(this.settings);
                    setStreams();
                }
            } catch (IOException e) {
                if (serialPort != null) {
                    serialPort.close();
                }
                throw e;
            }

            this.closed = false;
        }
    }

    private void setStreams() throws IOException {
        this.is = new DataInputStream(this.serialPort.getInputStream());
        this.os = new DataOutputStream(this.serialPort.getOutputStream());
    }

    private void connectWithHandshake() throws IOException {

        byte[] iec21AddressBytes = settings.iec21Address().trim().getBytes(StandardCharsets.US_ASCII);
        this.serialPort = SerialPortBuilder.newBuilder(settings.serialPortName())
                .setBaudRate(300)
                .setDataBits(DataBits.DATABITS_7)
                .setStopBits(StopBits.STOPBITS_1)
                .setParity(Parity.EVEN)
                .build();

        setStreams();

        serialPort.setSerialPortTimeout(2000);

        byte[] requestMsg = ByteBuffer.allocate(REQUEST_MSG_1.length + iec21AddressBytes.length + REQUEST_MSG_3.length)
                .put(REQUEST_MSG_1)
                .put(iec21AddressBytes)
                .put(REQUEST_MSG_3)
                .array();

        write(requestMsg);

        char baudRateSetting;
        try {
            baudRateSetting = listenForIdentificationMessage();
        } catch (FatalJDlmsException e) {
            throw e;
        } catch (InterruptedIOException e) {
            throw new FatalJDlmsException(ExceptionId.IEC_21_CONNECTION_ESTABLISH_ERROR, SYSTEM, MessageFormat.format(
                    "Send request message: {0}. Request timed out.", DatatypeConverter.printHexBinary(requestMsg)), e);

        } catch (IOException e) {
            throw new FatalJDlmsException(ExceptionId.IEC_21_CONNECTION_ESTABLISH_ERROR, SYSTEM,
                    MessageFormat.format("Send request message: {0}.", DatatypeConverter.printHexBinary(requestMsg)),
                    e);
        }

        int baudRate = baudRateFor(baudRateSetting);

        byte[] ackClone = ACKNOWLEDGE;
        ackClone[2] = (byte) baudRateSetting;

        write(ackClone);

        // Sleep for about 250 milliseconds to make sure, that the
        // acknowledge message has been completely transmitted prior
        // to changing the baud rate
        try {
            Thread.sleep(settings.baudrateChangeDelay());
        } catch (InterruptedException e) {
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        }

        // change mode to Z baud, 7,1,E
        serialPort.setBaudRate(baudRate);
        serialPort.setDataBits(DataBits.DATABITS_7);
        serialPort.setStopBits(StopBits.STOPBITS_1);
        serialPort.setParity(Parity.EVEN);

        listenForAck();

        // change mode to Z baud, 8,1,N
        serialPort.setDataBits(DataBits.DATABITS_8);
        serialPort.setParity(Parity.NONE);
    }

    private static SerialPort connectWithoutHandshake(SerialSettings settings) throws IOException {
        return SerialPortBuilder.newBuilder(settings.serialPortName())
                .setBaudRate(settings.baudrate())
                .setDataBits(DataBits.DATABITS_8)
                .setStopBits(StopBits.STOPBITS_1)
                .setParity(Parity.NONE)
                .build();
    }

    private void write(byte[] data) throws IOException {
        getOutpuStream().write(data);
        getOutpuStream().flush();
    }

    @Override
    public void close() {
        try {
            serialPort.close();
        } catch (IOException e) {
            // ignore
        }
        closed = true;
    }

    private static int baudRateFor(char baudCharacter) throws IOException {
        // Encoded baud rate (see IEC 62056-21 6.3.14 13c).
        switch (baudCharacter) {
        case '0':
            return 300;
        case '1':
            return 600;
        case '2':
            return 1200;
        case '3':
            return 2400;
        case '4':
            return 4800;
        case '5':
            return 9600;
        case '6':
            return 19200;
        default:
            throw new FatalJDlmsException(ExceptionId.IEC_21_CONNECTION_ESTABLISH_ERROR, SYSTEM, String.format(
                    "Syntax error in identification message received: unknown baud rate received. Baud character was 0x%02X or char '%s'.",
                    (byte) baudCharacter, String.valueOf(baudCharacter)));
        }
    }

    private void listenForAck() throws IOException {
        byte[] ackMsg = new byte[ACKNOWLEDGE.length];

        int ackLength;
        try {
            ackLength = getInputStream().read(ackMsg);
        } catch (IOException e) {
            throw new FatalJDlmsException(ExceptionId.IEC_21_WRONG_BAUD_RATE_CHANGE_DELAY, Fault.SYSTEM,
                    "Failed to read ACK message.", e);
        }

        if (ackLength != ackMsg.length) {
            throw new FatalJDlmsException(ExceptionId.IEC_21_UNKNOWN_ACK_MSG, SYSTEM, "Received unknown ACK answer.");
        }

    }

    private char listenForIdentificationMessage() throws IOException {
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream()) {
            int b;
            while ((b = getInputStream().read()) != CR) {
                byteStream.write(b);
            }

            // read CR LF
            byteStream.write(b);
            b = getInputStream().read();
            byteStream.write(b);

            // / X X X \ W Ident CR LF
            byte[] response = byteStream.toByteArray();

            return (char) response[4];
        }
    }

    public enum DataFlowControl {
        ENABLED,
        DISABLED
    }

}
