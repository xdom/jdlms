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
