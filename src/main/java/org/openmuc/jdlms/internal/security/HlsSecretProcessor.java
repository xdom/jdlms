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

import org.openmuc.jdlms.AuthenticationMechanism;
import org.openmuc.jdlms.SecuritySuite;

/**
 * Interface used to provide a manufacturer specific way for processing the secret with a given salt according to the
 * high level authentication.
 * 
 * An example for processing the secret is appending the secret to the salt and generating a GMAC digest, which is
 * returned as the result. Note that you do not have to implement this specific implementation, as it is one of the two
 * standard methods provided by jDLMS by using {@link AuthenticationMechanism#HLS5_GMAC}.
 */
public interface HlsSecretProcessor {

    /**
     * Method to provide an algorithm for processing a secret byte sequence with a salt byte sequence
     * 
     * @param challenge
     *            The generated salt
     * @param securitySuite
     *            the security suite
     * @param systemTitle
     *            the system title.
     * @param frameCounter
     *            frame counter
     * @return The processed byte sequence
     * @throws IOException
     *             throws IOException
     * @throws UnsupportedOperationException
     *             throws UnsupportedOperationException
     */
    byte[] process(byte[] challenge, SecuritySuite securitySuite, byte[] systemTitle, int frameCounter)
            throws IOException, UnsupportedOperationException;
}
