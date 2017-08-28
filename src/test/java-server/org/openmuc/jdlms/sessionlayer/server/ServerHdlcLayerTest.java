package org.openmuc.jdlms.sessionlayer.server;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.openmuc.jdlms.internal.association.AssociationShutdownException;
import org.openmuc.jdlms.sessionlayer.hdlc.FrameType;
import org.openmuc.jdlms.sessionlayer.hdlc.HdlcAddress;
import org.openmuc.jdlms.sessionlayer.hdlc.HdlcAddressPair;
import org.openmuc.jdlms.sessionlayer.hdlc.HdlcFrame;
import org.openmuc.jdlms.sessionlayer.hdlc.HdlcMessageDecoder;
import org.openmuc.jdlms.sessionlayer.hdlc.HdlcParameters;
import org.openmuc.jdlms.settings.server.ServerSettings;
import org.openmuc.jdlms.transportlayer.StreamAccessor;
import org.powermock.reflect.Whitebox;

public class ServerHdlcLayerTest {

    @Test
    public void testInit() throws Exception {

        HdlcAddressPair addressPair = new HdlcAddressPair(new HdlcAddress(16), new HdlcAddress(1));
        HdlcParameters negParams = new HdlcParameters(1000, 7, 1000, 7);
        boolean poll = false;
        byte[] data = HdlcFrame.newSetNormalResponseModeFrame(addressPair, negParams, poll).encode();
        StreamAccessor streamAccessor = isStreamAccessorFor(data);
        ServerSettings settings = mock(ServerSettings.class);
        ServerHdlcSessionLayer layer = new ServerHdlcSessionLayer(streamAccessor, settings);
        assertEquals(-1, layer.getClientId());
        assertEquals(-1, layer.getLogicalDeviceId());

        ByteArrayOutputStream out = setOSToStream(streamAccessor);
        layer.initialize();

        StreamAccessor clientSa = isStreamAccessorFor(out.toByteArray());
        List<HdlcFrame> resList = HdlcMessageDecoder.decode(null, clientSa, 0);

        assertEquals(1, resList.size());

        HdlcFrame response = resList.get(0);

        assertEquals(FrameType.UNNUMBERED_ACKNOWLEDGE, response.getFrameType());

        assertEquals(addressPair.switchedPair(), response.getAddressPair());

        HdlcParameters finalParams = HdlcParameters.decode(response.getInformationField());

        assertEquals(1, finalParams.getReceiveWindowSize());
        assertEquals(1, finalParams.getTransmitWindowSize());

        assertTrue(finalParams.getReceiveInformationLength() <= negParams.getTransmitInformationLength());
        assertTrue(finalParams.getTransmitInformationLength() <= negParams.getReceiveInformationLength());

        layer.close();
    }

    @Test
    public void testNextMsg() throws Exception {
        HdlcAddressPair addressPair = new HdlcAddressPair(new HdlcAddress(16), new HdlcAddress(1));
        int sendSequence = 2;
        int receiveSequence = 2;
        final byte[] bytesToSend = "HelloWorld!".getBytes();
        byte[] data = HdlcFrame
                .newInformationFrame(addressPair, sendSequence, receiveSequence, bytesToSend, false, true)
                .encode();
        StreamAccessor streamAccessor = isStreamAccessorFor(data);
        ServerSettings settings = mock(ServerSettings.class);
        ServerHdlcSessionLayer layer = spy(new ServerHdlcSessionLayer(streamAccessor, settings));

        Whitebox.setInternalState(layer, "addressPair", addressPair);

        byte[] readData = layer.readNextMessage();

        assertArrayEquals(bytesToSend, readData);

        layer.close();
    }

    @Test
    public void testDisconnectMsg() throws Exception {
        HdlcAddressPair addressPair = new HdlcAddressPair(new HdlcAddress(16), new HdlcAddress(1));
        byte[] data = HdlcFrame.newDisconnectFrame(addressPair, false).encode();
        StreamAccessor streamAccessor = isStreamAccessorFor(data);
        ByteArrayOutputStream out = setOSToStream(streamAccessor);

        ServerSettings settings = mock(ServerSettings.class);
        ServerHdlcSessionLayer layer = spy(new ServerHdlcSessionLayer(streamAccessor, settings));
        Whitebox.setInternalState(layer, "addressPair", addressPair);

        try {
            layer.readNextMessage();
            fail("Expected an exception");
        } catch (IOException e) {
            assertThat(e, instanceOf(AssociationShutdownException.class));
        }

        List<HdlcFrame> retMsg = HdlcMessageDecoder.decode(null, isStreamAccessorFor(out.toByteArray()), 0);

        assertEquals(1, retMsg.size());

        HdlcFrame discFrame = retMsg.get(0);
        assertEquals(FrameType.UNNUMBERED_ACKNOWLEDGE, discFrame.getFrameType());

        layer.close();
    }

    private static ByteArrayOutputStream setOSToStream(StreamAccessor streamAccessor) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(streamAccessor.getOutpuStream()).thenReturn(new DataOutputStream(out));
        return out;
    }

    private static StreamAccessor isStreamAccessorFor(byte[] data) throws IOException {
        StreamAccessor streamAccessor = mock(StreamAccessor.class);
        when(streamAccessor.getInputStream()).thenReturn(new DataInputStream(new ByteArrayInputStream(data)));
        return streamAccessor;
    }
}
