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

import org.openmuc.jdlms.SecuritySuite;

/**
 * Implementation of HIGH Level authentication using SHA-256 hashing as described in IEC 62056-6-2
 */
public class HlsProcessorSha256 implements HlsSecretProcessor {

    @Override
    public byte[] process(byte[] challenge, SecuritySuite securitySuite, byte[] systemTitle, int frameCounter)
            throws IOException, UnsupportedOperationException {
        return MdShaProcessor.process(challenge, securitySuite, "SHA256");
    }

}
