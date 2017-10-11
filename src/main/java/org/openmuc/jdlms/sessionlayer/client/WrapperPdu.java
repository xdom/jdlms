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
package org.openmuc.jdlms.sessionlayer.client;

import static java.lang.String.format;
import static org.openmuc.jdlms.JDlmsException.Fault.SYSTEM;

import java.io.DataInputStream;
import java.io.IOException;

import org.openmuc.jdlms.FatalJDlmsException;
import org.openmuc.jdlms.JDlmsException.ExceptionId;
import org.openmuc.jdlms.settings.client.Settings;
import org.openmuc.jdlms.transportlayer.StreamAccessor;

public class WrapperPdu {

    private WrapperHeader header;
    private byte[] data;

    private WrapperPdu(WrapperHeader header, byte[] data) {
        this.header = header;
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

    public WrapperHeader getheader() {
        return header;
    }

    public static WrapperPdu decode(StreamAccessor transportLayer, Settings settings) throws IOException {
        DataInputStream inputStream = transportLayer.getInputStream();

        transportLayer.setTimeout(0);

        WrapperHeader header = WrapperHeader.decode(transportLayer, settings.responseTimeout());

        byte[] messageData = new byte[header.getPayloadLength()];
        inputStream.readFully(messageData);

        validate(header, messageData.length, settings);

        return new WrapperPdu(header, messageData);
    }

    private static void validate(WrapperHeader header, int payloadLength, Settings settings)
            throws FatalJDlmsException {
        if (header.getSourceWPort() != settings.logicalDeviceId()) {
            throw new FatalJDlmsException(ExceptionId.WRAPPER_HEADER_INVALID_SRC_DEST_ADDR, SYSTEM,
                    format("Connection was initiated with logical device address %d, but server answered with %d",
                            settings.logicalDeviceId(), header.getSourceWPort()));
        }
    }

}
