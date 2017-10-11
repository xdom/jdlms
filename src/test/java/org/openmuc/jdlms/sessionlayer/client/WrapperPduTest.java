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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.Test;
import org.openmuc.jdlms.settings.client.Settings;
import org.openmuc.jdlms.transportlayer.StreamAccessor;
import org.powermock.api.mockito.PowerMockito;

public class WrapperPduTest {

    @Test
    public void testDecode() throws Exception {
        StreamAccessor transportLayer = mock(StreamAccessor.class);

        byte[] data = "HelloWold!".getBytes();
        final short sourceWp = 12;
        final short destWp = 10;
        byte[] msg = ByteBuffer.allocate(data.length + 8)
                .putShort((short) 1)
                .putShort(sourceWp)
                .putShort(destWp)
                .putShort((short) data.length)
                .put(data)
                .array();

        when(transportLayer.getInputStream()).thenReturn(new DataInputStream(new ByteArrayInputStream(msg)));

        Settings settings = mock(Settings.class);
        PowerMockito.when(settings.logicalDeviceId()).thenReturn((int) sourceWp);
        PowerMockito.when(settings.clientId()).thenReturn((int) destWp);

        WrapperPdu wPdu = WrapperPdu.decode(transportLayer, settings);

        WrapperHeader header = wPdu.getheader();
        assertEquals(sourceWp, header.getSourceWPort());
        assertEquals(destWp, header.getDestinationWPort());
        assertArrayEquals(data, wPdu.getData());
    }

    @Test(expected = IOException.class)
    public void illegalMessage() throws Exception {
        StreamAccessor transportLayer = mock(StreamAccessor.class);

        byte[] data = "HelloWold!".getBytes();
        final short sourceWp = 12;
        final short destWp = 10;
        byte[] msg = ByteBuffer.allocate(data.length + 8)
                .putShort((short) 1)
                .putShort(sourceWp)
                .putShort(destWp)
                .putShort((short) (data.length + 2))
                .put(data)
                .array();

        when(transportLayer.getInputStream()).thenReturn(new DataInputStream(new ByteArrayInputStream(msg)));

        Settings settings = mock(Settings.class);
        PowerMockito.when(settings.logicalDeviceId()).thenReturn((int) sourceWp);
        PowerMockito.when(settings.clientId()).thenReturn((int) destWp);

        WrapperPdu.decode(transportLayer, settings);

    }

}
