package org.openmuc.jdlms.sessionlayer.hdlc;

import static org.junit.Assert.assertEquals;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.List;

import org.junit.Test;
import org.openmuc.jdlms.HexConverter;
import org.openmuc.jdlms.RawMessageData.RawMessageDataBuilder;
import org.openmuc.jdlms.sessionlayer.hdlc.FrameType;
import org.openmuc.jdlms.sessionlayer.hdlc.HdlcAddress;
import org.openmuc.jdlms.sessionlayer.hdlc.HdlcFrame;
import org.openmuc.jdlms.sessionlayer.hdlc.HdlcMessageDecoder;
import org.openmuc.jdlms.transportlayer.client.TransportLayer;

public class HdlcFrameTest {

    @Test
    public void decode_with_decoder() throws Exception {
        final String hdlcFlag = "7E";
        String bytes1 = "A01E610373B57C81801205018006013E0704000000010804000000010722";
        String bytes2 = "A021230201737A5B81801405020074060200740704000000010804000000016A0B";
        StringBuilder message = new StringBuilder().append(hdlcFlag)
                .append(bytes1)
                .append(hdlcFlag)
                .append(bytes2)
                .append(hdlcFlag);
        byte[] data = HexConverter.fromShortHexString(message.toString());

        TransportLayer transportLayer = mock(TransportLayer.class);

        when(transportLayer.getInputStream()).thenReturn(new DataInputStream(new ByteArrayInputStream(data)));
        when(transportLayer.isClosed()).thenReturn(false);

        RawMessageDataBuilder rawMb = null;
        List<HdlcFrame> frames = HdlcMessageDecoder.decode(rawMb, transportLayer, 0);

        assertEquals(2, frames.size());
    }

    @Test
    public void decode1() throws Exception {
        /*
         * Captured in /jdlms/develop/inventar/landis+gyr/E650/51324481/E650_MAP120_Kommunikation_tcp.port_9000.pcapng
         */
        String bytes = "A01E610373B57C81801205018006013E0704000000010804000000010722";
        byte[] data = HexConverter.fromShortHexString(bytes);

        HdlcFrame frame = HdlcFrame.decode(data);

        assertEquals(FrameType.UNNUMBERED_ACKNOWLEDGE, frame.getFrameType());
    }

    @Test
    public void decode2() throws Exception {
        /**
         * Captured from the initial answer from the Kampstrup meter
         */
        String bytes = "A021230201737A5B81801405020074060200740704000000010804000000016A0B";
        byte[] data = HexConverter.fromShortHexString(bytes);

        HdlcFrame.decode(data);
        // new HdlcMessageDecoder().decode(new DataInputStream(new ByteArrayInputStream(data))).get(0);
    }

    @Test
    public void decode3() throws Exception {
        String bytes = "A0372103306C7CE6E7006128A109060760857405080102A203020100A305A103020100BE0F040D0800065F1F04001802200960FA000AA8";

        byte[] data = HexConverter.fromShortHexString(bytes);

        HdlcFrame.decode(data);
    }

    @Test
    public void decode4() throws Exception {
        /**
         * Captured from the initial answer from the Iskra meter
         */
        String bytes = "A01FC9022373B49681801205017E06017E0704000000010804000000015F75";

        byte[] data = HexConverter.fromShortHexString(bytes);

        HdlcFrame frame = HdlcFrame.decode(data);

        assertEquals(new HdlcAddress(100), frame.getDestinationAddress());
        assertEquals(new HdlcAddress(1, 17), frame.getSourceAddress());
        assertEquals(FrameType.UNNUMBERED_ACKNOWLEDGE, frame.getFrameType());

    }

    @Test
    public void decode5() throws Exception {
        byte[] bytes = HexConverter.fromShortHexString("A00B002254D36193F4E3");

        HdlcFrame frame = HdlcFrame.decode(bytes);

        final int physicalAddressE650 = 5481;
        assertEquals(physicalAddressE650, frame.getAddressPair().destination().getPhysicalId());
    }

    @Test
    public void decode6() throws Exception {
        byte[] bytes = HexConverter.fromShortHexString("A00A002254D361513F99");
        byte[] bytes2 = HexConverter.fromShortHexString("A00A002254D361513F99");

        HdlcFrame frame = HdlcFrame.decode(bytes);
        assertEquals(FrameType.RECEIVE_READY, frame.getFrameType());

        HdlcFrame frame2 = HdlcFrame.decode(bytes2);
        assertEquals(FrameType.RECEIVE_READY, frame2.getFrameType());
    }

}
