package org.openmuc.jdlms.internal.sessionlayer.client;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.powermock.api.mockito.PowerMockito.when;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.junit.Test;
import org.openmuc.jdlms.FatalJDlmsException;
import org.openmuc.jdlms.HexConverter;
import org.openmuc.jdlms.sessionlayer.client.WrapperHeader;
import org.openmuc.jdlms.sessionlayer.client.WrapperHeader.WrapperHeaderBuilder;
import org.openmuc.jdlms.transportlayer.StreamAccessor;
import org.powermock.api.mockito.PowerMockito;

public class WrapperHeaderTest {

    @Test(expected = IOException.class)
    public void illegalVersionTest() throws Exception {
        byte[] bytes = HexConverter.fromShortHexString(
                "1001001000110022602080020780a109060760857405080102be0f040d01000000065f0400181a200000");

        StreamAccessor streamAccessor = dataToStreamAccessor(bytes);

        WrapperHeader.decode(streamAccessor, 0);
    }

    @Test
    public void decode1() throws Exception {
        byte[] bytes = HexConverter.fromShortHexString(
                "0001001000110022602080020780a109060760857405080102be0f040d01000000065f0400181a200000");

        StreamAccessor streamAccessor = dataToStreamAccessor(bytes);
        InputStream inputStream = streamAccessor.getInputStream();

        WrapperHeader wrapperHeader = WrapperHeader.decode(streamAccessor, 0);

        byte[] dlmsData = new byte[wrapperHeader.getPayloadLength()];
        int length = inputStream.read(dlmsData);

        assertEquals(wrapperHeader.getPayloadLength(), length);

        byte[] array = ByteBuffer.allocate(bytes.length).put(wrapperHeader.encode()).put(dlmsData).array();

        assertArrayEquals(bytes, array);
    }

    @Test(expected = FatalJDlmsException.class)
    public void decode2() throws Exception {
        byte[] bytes = HexConverter.fromShortHexString(
                "0002001000110022602080020780a109060760857405080102be0f040d01000000065f0400181a200000");

        StreamAccessor streamAccessor = dataToStreamAccessor(bytes);

        WrapperHeader.decode(streamAccessor, 0);
    }

    private StreamAccessor dataToStreamAccessor(byte[] data) throws IOException {
        StreamAccessor streamAccessor = PowerMockito.mock(StreamAccessor.class);

        when(streamAccessor.getInputStream()).thenReturn(new DataInputStream(new ByteArrayInputStream(data)));
        return streamAccessor;
    }

    @Test
    public void builderTest() throws Exception {
        int sourceWPort = 10;
        int destinationWPort = 12;
        WrapperHeaderBuilder builder = WrapperHeader.builder(sourceWPort, destinationWPort);

        int length = 100;
        builder.setLength(length);

        WrapperHeader wrapperHeader = builder.build();

        assertEquals(sourceWPort, wrapperHeader.getSourceWPort());
        assertEquals(destinationWPort, wrapperHeader.getDestinationWPort());
        assertEquals(length, wrapperHeader.getPayloadLength());
    }

}
