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
package org.openmuc.jdlms.settings.client;

import org.openmuc.jdlms.RawMessageListener;
import org.openmuc.jdlms.SecuritySuite;

public interface Settings {

    byte[] systemTitle();

    int challengeLength();

    int responseTimeout();

    int clientId();

    int logicalDeviceId();

    int physicalDeviceId();

    void updateAuthenticationKey(byte[] authenticationKey);

    void updateGlobalEncryptionKey(byte[] globalEncryptionKey);

    SecuritySuite securitySuite();

    ReferencingMethod referencingMethod();

    RawMessageListener rawMessageListener();

    int hdlcMaxInformationLength();

    boolean selectiveAccessValidationDisabled();
}
