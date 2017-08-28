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

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.GCMBlockCipher;
import org.bouncycastle.crypto.modes.gcm.BasicGCMMultiplier;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.openmuc.jdlms.SecuritySuite;
import org.openmuc.jdlms.SecuritySuite.EncryptionMechanism;
import org.openmuc.jdlms.internal.asn1.axdr.AxdrLength;

public class GcmModule {

    private static final int FC_LENGTH = 4;
    private static final int IV_LENGTH = 12;
    private static final int IV_LENGTH_BITS = IV_LENGTH * 8;
    private static final int LENGTH_FC_TAG_SC = 6;

    public static byte[] processPlain(byte[] plaintext, int off, int len, byte[] systemTitle, int frameCounter,
            SecuritySuite securitySuite, byte tag) throws IOException {
        byte[] frameCounterBytes = ByteBuffer.allocate(4).putInt(frameCounter).array();

        byte[] iv = ByteBuffer.allocate(systemTitle.length + 4).put(systemTitle).putInt(frameCounter).array();

        byte[] additionalAssociationData = createAadIfExists(securitySuite);

        AEADParameters parameters = new AEADParameters(new KeyParameter(securitySuite.getGlobalUnicastEncryptionKey()),
                IV_LENGTH_BITS, iv, additionalAssociationData);

        GCMBlockCipher encCipher = createBlockCipher(true, parameters);

        byte[] chipherText = new byte[encCipher.getOutputSize(len)];
        int length = encCipher.processBytes(plaintext, off, len, chipherText, 0);
        try {
            encCipher.doFinal(chipherText, length);
        } catch (IllegalStateException | InvalidCipherTextException e) {
            throw new IOException("Unable to cipher/encrypt xDLMS APDU", e);
        }

        int chipherTextLength = chipherText.length;

        if (!securitySuite.getSecurityPolicy().isAuthenticated()) {
            chipherTextLength -= 12;
        }
        byte[] lengthBytes = AxdrLength.encodeLength(chipherTextLength + 5);

        final int cipheredApduLength = chipherTextLength + LENGTH_FC_TAG_SC + lengthBytes.length;

        byte controlByte = securityControlByteFrom(securitySuite);

        return ByteBuffer.allocate(cipheredApduLength)
                .put(tag)
                .put(lengthBytes)
                .put(controlByte)
                .put(frameCounterBytes)
                .put(chipherText, 0, chipherTextLength)
                .array();
    }

    private static byte[] createAadIfExists(SecuritySuite securitySuite) {
        if (!securitySuite.getSecurityPolicy().isAuthenticated()) {
            return new byte[0];
        }

        byte[] authenticationKey = securitySuite.getAuthenticationKey();
        byte securityControlByte = securityControlByteFrom(securitySuite);
        return ByteBuffer.allocate(authenticationKey.length + 1)
                .put(securityControlByte)
                .put(authenticationKey)
                .array();
    }

    public static byte[] decrypt(byte[] cipheredApdu, byte[] systemTitle, SecuritySuite securitySuite)
            throws IOException {
        byte[] iv = ByteBuffer.allocate(IV_LENGTH).put(systemTitle).put(cipheredApdu, 1, FC_LENGTH).array();

        byte[] additionalAssociationData = createAadIfExists(securitySuite);
        AEADParameters parameters = new AEADParameters(new KeyParameter(securitySuite.getGlobalUnicastEncryptionKey()),
                IV_LENGTH_BITS, iv, additionalAssociationData);

        GCMBlockCipher decCipher = createBlockCipher(false, parameters);

        byte[] dec = new byte[decCipher.getOutputSize(cipheredApdu.length - 5)];
        int length = decCipher.processBytes(cipheredApdu, 5, cipheredApdu.length - 5, dec, 0);
        try {
            decCipher.doFinal(dec, length);
        } catch (IllegalStateException | InvalidCipherTextException e) {
            throw new IOException("Unable to decipher/decrypt xDLMS pdu", e);
        }

        return dec;
    }

    private static GCMBlockCipher createBlockCipher(boolean forEncryption, AEADParameters parameters) {
        GCMBlockCipher decCipher = new GCMBlockCipher(new AESEngine(), new BasicGCMMultiplier());
        decCipher.init(forEncryption, parameters);
        return decCipher;
    }

    private static byte securityControlByteFrom(SecuritySuite securitySuite) {
        if (securitySuite.getEncryptionMechanism() == EncryptionMechanism.NONE) {
            // TODO error!
        }
        byte sc = 0;
        sc = setBit(sc, 5, securitySuite.getSecurityPolicy().isEncrypted());
        sc = setBit(sc, 4, securitySuite.getSecurityPolicy().isAuthenticated());

        sc = (byte) (sc | securitySuite.getEncryptionMechanism().getCode() & 0x07);
        return sc;
    }

    private static byte setBit(byte data, int index, boolean flag) {
        if (!flag) {
            return data;
        }

        return (byte) (data | (1 << index));
    }

    /**
     * Don't let anyone instantiate this class.
     */
    private GcmModule() {
    }
}
