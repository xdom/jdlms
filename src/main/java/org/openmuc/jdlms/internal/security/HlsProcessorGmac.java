/*
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
 *
 */
package org.openmuc.jdlms.internal.security;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.macs.GMac;
import org.bouncycastle.crypto.modes.GCMBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.openmuc.jdlms.SecuritySuite;

/**
 * High Level Security Processor implementing the Galois MAC (Message Authentication Code) Counter.
 */
public class HlsProcessorGmac implements HlsSecretProcessor {

    @Override
    public byte[] process(byte[] challenge, SecuritySuite securitySuite, byte[] systemTitle, int frameCounter)
            throws IOException, UnsupportedOperationException {
        final byte[] encryptionKey = securitySuite.getGlobalUnicastEncryptionKey();
        final byte[] authenticationKey = securitySuite.getAuthenticationKey();

        final byte sc = SecurityControl.AUTHENTICATION.getSecurityControlByte();

        byte[] frameCounterBytes = ByteBuffer.allocate(4).putInt(frameCounter).array();
        byte[] iv = ByteBuffer.allocate(systemTitle.length + frameCounterBytes.length)
                .put(systemTitle)
                .put(frameCounterBytes)
                .array();

        CipherParameters cipherParameters = new KeyParameter(encryptionKey);
        ParametersWithIV parameterWithIV = new ParametersWithIV(cipherParameters, iv);

        GMac mac = new GMac(new GCMBlockCipher(new AESEngine()), 96);

        mac.init(parameterWithIV);

        byte[] input = ByteBuffer.allocate(1 + authenticationKey.length + challenge.length)
                .put(sc)
                .put(authenticationKey)
                .put(challenge)
                .array();
        mac.update(input, 0, input.length);
        final byte[] generatedMac = new byte[mac.getMacSize()];
        mac.doFinal(generatedMac, 0);

        return ByteBuffer.allocate(1 + frameCounterBytes.length + generatedMac.length)
                .put(sc)
                .put(frameCounterBytes)
                .put(generatedMac)
                .array();
    }

    private enum SecurityControl {
        AUTHENTICATION(0x10),
        ENCRYPTION(0x20),
        AUTHENTICATION_ENCRYPTION(0x30);

        private final byte sc;

        private SecurityControl(int b) {
            this.sc = (byte) b;
        }

        public byte getSecurityControlByte() {
            return sc;
        }
    }

}
