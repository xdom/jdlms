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
