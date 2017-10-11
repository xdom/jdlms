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
package org.openmuc.jdlms.internal.association;

import static org.openmuc.jdlms.SecuritySuite.newSecuritySuiteFrom;

import java.io.IOException;
import java.util.Arrays;

import org.openmuc.jdlms.SecuritySuite;
import org.openmuc.jdlms.SecuritySuite.EncryptionMechanism;
import org.openmuc.jdlms.internal.APdu;
import org.openmuc.jdlms.internal.DataDirectoryImpl;
import org.openmuc.jdlms.internal.ServerConnectionData;
import org.openmuc.jdlms.internal.asn1.axdr.AxdrType;
import org.openmuc.jdlms.internal.asn1.axdr.NullOutputStream;

public class AssociationMessenger {

    private final ServerConnectionData connectionData;
    private final DataDirectoryImpl directory;

    private SecuritySuite securitySuite;

    private final byte[] buffer = new byte[0xFFFFF * 5];

    public AssociationMessenger(ServerConnectionData connectionData, DataDirectoryImpl directory) {
        this.connectionData = connectionData;
        this.directory = directory;
    }

    public void encodeAndSend(APdu aPdu) throws IOException {
        send(encode(aPdu));
    }

    public void send(byte[] data) throws IOException {
        connectionData.sessionLayer.send(data);
    }

    public APdu readNextApdu() throws IOException {
        byte[] bytes = connectionData.sessionLayer.readNextMessage();
        APdu apdu = APdu.decode(bytes, null);

        SecuritySuite sec = connectionData.securitySuite;
        if (sec.getEncryptionMechanism() == EncryptionMechanism.NONE) {
            return apdu;
        }

        if (apdu.getCosemPdu() != null && !apdu.isEncrypted()) {
            // TODO erro
        }
        if (connectionData.clientSystemTitle == null) {
            connectionData.clientSystemTitle = systemTitle();
        }

        this.securitySuite = newSecuritySuiteFrom(sec);

        return APdu.decode(bytes, connectionData.clientSystemTitle, connectionData.frameCounter, sec, null);
    }

    public synchronized byte[] encode(APdu aPdu) throws IOException {
        int length;

        SecuritySuite sec = connectionData.securitySuite;
        if (sec.getEncryptionMechanism() != EncryptionMechanism.NONE) {

            if (this.securitySuite == null) {
                this.securitySuite = newSecuritySuiteFrom(sec);
            }

            length = aPdu.encode(buffer, connectionData.frameCounter++, systemTitle(), this.securitySuite, null);
        }
        else {
            length = aPdu.encode(buffer, null);
        }

        return Arrays.copyOfRange(buffer, buffer.length - length, buffer.length);
    }

    public byte[] systemTitle() {
        return this.directory.getLogicalDeviceFor(connectionData.sessionLayer.getLogicalDeviceId())
                .getLogicalDevice()
                .getSystemTitle();
    }

    public boolean pduSizeTooLarge(AxdrType actionResponse) throws IOException {
        int maxMessageLength = getMaxMessageLength();

        return maxMessageLength != 0 && pduSizeOf(actionResponse) >= maxMessageLength;
    }

    public int getMaxMessageLength() {
        if ((int) this.connectionData.clientMaxReceivePduSize == 0) {
            return 0xFFFF;
        }
        else {
            return (int) this.connectionData.clientMaxReceivePduSize;
        }
    }

    public boolean apduTooLarge(int apduSize) {
        int maxMessageLength = getMaxMessageLength();
        return maxMessageLength != 0 && apduSize >= maxMessageLength;
    }

    public static int pduSizeOf(AxdrType pdu) throws IOException {
        return pdu.encode(new NullOutputStream());
    }

}
