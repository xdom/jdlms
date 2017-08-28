package org.openmuc.jdlms;

import static javax.xml.bind.DatatypeConverter.parseHexBinary;
import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.openmuc.jdlms.internal.security.HlsProcessorGmac;
import org.openmuc.jdlms.internal.security.HlsSecretProcessor;

public class GmacTest {

    @Test
    public void doGmacTestFromStandardExample() throws Exception {

        byte[] challenge = parseHexBinary("503677524A323146");
        byte[] encryptionKey = parseHexBinary("000102030405060708090A0B0C0D0E0F");
        byte[] authenticationKey = parseHexBinary("D0D1D2D3D4D5D6D7D8D9DADBDCDDDEDF");
        byte[] systemTitle = parseHexBinary("4d4d4d0000000001");
        int frameCounter = 1;

        SecuritySuite securitySuite = newMockedSecSuite(encryptionKey, authenticationKey);

        HlsSecretProcessor hlsSecretProcessor = new HlsProcessorGmac();
        byte[] resultStoC = hlsSecretProcessor.process(challenge, securitySuite, systemTitle, frameCounter);

        byte[] fStoCFromStandard = parseHexBinary("10000000011A52FE7DD3E72748973C1E28");
        assertArrayEquals(fStoCFromStandard, resultStoC);
    }

    private SecuritySuite newMockedSecSuite(byte[] encryptionKey, byte[] authenticationKey) {
        SecuritySuite securitySuite = mock(SecuritySuite.class);
        when(securitySuite.getGlobalUnicastEncryptionKey()).thenReturn(encryptionKey);
        when(securitySuite.getAuthenticationKey()).thenReturn(authenticationKey);
        return securitySuite;
    }

}
