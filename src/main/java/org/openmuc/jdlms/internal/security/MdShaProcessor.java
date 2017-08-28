package org.openmuc.jdlms.internal.security;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.MessageFormat;

import org.openmuc.jdlms.SecuritySuite;

class MdShaProcessor {

    public static byte[] process(byte[] challenge, SecuritySuite securitySuite, String algorithm) throws IOException {

        MessageDigest messageDigest = messageDigestFor(algorithm);

        byte[] authenticationKey = securitySuite.getAuthenticationKey();
        byte[] input = ByteBuffer.allocate(authenticationKey.length + challenge.length)
                .put(challenge)
                .put(authenticationKey)
                .array();

        return messageDigest.digest(input);
    }

    private static MessageDigest messageDigestFor(String algorithm) throws IOException {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            String msg = MessageFormat.format("Could not process secret. Algorithm {0} not installed.", algorithm);
            throw new IOException(msg, e);
        }
    }

    private MdShaProcessor() {
    }

}
