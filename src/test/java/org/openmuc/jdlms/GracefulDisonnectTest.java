package org.openmuc.jdlms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.powermock.api.mockito.PowerMockito.doAnswer;
import static org.powermock.api.mockito.PowerMockito.doCallRealMethod;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.reflect.Whitebox.setInternalState;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openmuc.jdlms.RawMessageData.RawMessageDataBuilder;
import org.openmuc.jdlms.SecuritySuite.EncryptionMechanism;
import org.openmuc.jdlms.internal.APdu;
import org.openmuc.jdlms.internal.ReleaseRespReason;
import org.openmuc.jdlms.internal.asn1.iso.acse.ACSEApdu;
import org.openmuc.jdlms.internal.asn1.iso.acse.RLREApdu;
import org.openmuc.jdlms.internal.asn1.iso.acse.RLRQApdu;
import org.openmuc.jdlms.internal.asn1.iso.acse.ReleaseResponseReason;
import org.openmuc.jdlms.sessionlayer.client.SessionLayer;
import org.openmuc.jdlms.settings.client.Settings;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@PrepareForTest(DlmsConnection.class)
@RunWith(PowerMockRunner.class)
public class GracefulDisonnectTest {

    @Test
    public void test1() throws Exception {
        DlmsConnection connection = mock(DlmsConnection.class);

        setInternalState(connection, "buffer", new byte[200]);
        Settings settings = mock(Settings.class);

        SecuritySuite secSuite = mock(SecuritySuite.class);
        when(secSuite.getEncryptionMechanism()).thenReturn(EncryptionMechanism.NONE);

        when(settings.securitySuite()).thenReturn(secSuite);

        setInternalState(connection, "settings", settings);
        SessionLayer sessionLayer = mock(SessionLayer.class);

        final APduBlockingQueue blockingQueue = new APduBlockingQueue();

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                byte[] dataBytes = invocation.getArgumentAt(0, byte[].class);
                int off = invocation.getArgumentAt(1, int.class);
                int len = invocation.getArgumentAt(2, int.class);
                dataBytes = Arrays.copyOfRange(dataBytes, off, off + len);

                // Arrays.copyOfRange(dataBytes, off, off + len);

                APdu data = APdu.decode(dataBytes, null);
                ACSEApdu acseAPdu2 = data.getAcseAPdu();

                assertNotNull(acseAPdu2);
                RLRQApdu rlrq = acseAPdu2.getRlrq();
                assertNotNull(rlrq);
                assertEquals(0, rlrq.getReason().value.intValue());

                ReleaseRespReason responseReason = ReleaseRespReason.NORMAL;
                ReleaseResponseReason reason = responseReason.toDlmsReason();
                RLREApdu rlre = new RLREApdu();
                rlre.setReason(reason);
                ACSEApdu acseAPdu = new ACSEApdu();
                acseAPdu.setRlre(rlre);
                APdu aPdu = new APdu(acseAPdu, null);
                blockingQueue.put(aPdu);
                return null;
            }
        }).when(sessionLayer).send(any(byte[].class), anyInt(), anyInt(), any(RawMessageDataBuilder.class));

        doReturn(100).when(settings).responseTimeout();

        setInternalState(connection, Settings.class, settings);
        setInternalState(connection, SessionLayer.class, sessionLayer);
        setInternalState(connection, APduBlockingQueue.class, blockingQueue);

        doCallRealMethod().when(connection).disconnect();
        doCallRealMethod().when(connection, "encodeAPdu", any(APdu.class), any(RawMessageDataBuilder.class));
        doCallRealMethod().when(connection, "unencryptedEncode", any(APdu.class), any(RawMessageDataBuilder.class));
        connection.disconnect();
    }

    @Test()
    public void test2() throws Exception {
        // bytes from E350:
        // 0001 0001 0010 0005 6303800100
        byte[] releaseBytes = HexConverter.fromShortHexString("6303800100");

        APdu aPdu = APdu.decode(releaseBytes, RawMessageData.builder());

        ACSEApdu acseAPdu = aPdu.getAcseAPdu();
        assertNotNull(acseAPdu);

        RLREApdu rlre = acseAPdu.getRlre();
        assertNotNull(rlre);

        assertEquals(0, rlre.getReason().value.intValue());
    }
}
