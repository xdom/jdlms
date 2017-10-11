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
package org.openmuc.jdlms.sessionlayer.server;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.reflect.Whitebox.setInternalState;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.Test;
import org.openmuc.jdlms.sessionlayer.client.WrapperHeader;
import org.openmuc.jdlms.settings.server.ServerSettings;
import org.openmuc.jdlms.transportlayer.StreamAccessor;

public class ServerWrapperLayerTest {

    @Test
    public void testNextMessage() throws Exception {
        ServerSettings settings = mock(ServerSettings.class);
        when(settings.getInactivityTimeout()).thenReturn(0);
        when(settings.getResponseTimeout()).thenReturn(0);

        final int clientId = 16;
        final int ldId = 1;

        final byte[] data = "Hello World!".getBytes();
        byte[] headerBytes = WrapperHeader.builder(clientId, ldId).setLength(data.length).build().encode();

        byte[] msg = ByteBuffer.allocate(headerBytes.length + data.length).put(headerBytes).put(data).array();
        StreamAccessor streamAccessor = streamAccessorFor(msg);

        ServerWrapperLayer wrapperLayer = new ServerWrapperLayer(streamAccessor, settings);

        assertEquals(-1, wrapperLayer.getClientId());
        assertEquals(-1, wrapperLayer.getLogicalDeviceId());

        byte[] nextMessage = wrapperLayer.readNextMessage();

        assertArrayEquals(data, nextMessage);
        assertEquals(clientId, wrapperLayer.getClientId());
        assertEquals(ldId, wrapperLayer.getLogicalDeviceId());
        wrapperLayer.close();
    }

    private static StreamAccessor streamAccessorFor(byte[] msg) throws IOException {
        StreamAccessor streamAccessor = mock(StreamAccessor.class);
        when(streamAccessor.getInputStream()).thenReturn(new DataInputStream(new ByteArrayInputStream(msg)));
        return streamAccessor;
    }

    @Test
    public void testSendMessage() throws Exception {
        StreamAccessor streamAccessor = mock(StreamAccessor.class);
        ServerSettings settings = null;

        ServerWrapperLayer wrapperLayer = spy(new ServerWrapperLayer(streamAccessor, settings));

        final int clientId = 16;
        final int ldId = 1;
        setInternalState(wrapperLayer, "headerBuilder", WrapperHeader.builder(ldId, clientId));

        assertEquals(-1, wrapperLayer.getClientId());
        assertEquals(-1, wrapperLayer.getLogicalDeviceId());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(streamAccessor.getOutpuStream()).thenReturn(new DataOutputStream(out));

        final byte[] bytes = "Hello World!".getBytes();
        wrapperLayer.send(bytes);

        StreamAccessor clientAccssor = streamAccessorFor(out.toByteArray());

        DataInputStream clientIs = clientAccssor.getInputStream();
        assertEquals(WrapperHeader.HEADER_LENGTH + bytes.length, clientIs.available());

        WrapperHeader header = WrapperHeader.decode(clientAccssor, 0);

        assertEquals(ldId, header.getSourceWPort());
        assertEquals(clientId, header.getDestinationWPort());

        byte[] resBytes = new byte[header.getPayloadLength()];
        clientIs.readFully(resBytes);

        assertArrayEquals(bytes, resBytes);
        wrapperLayer.close();
    }

}
