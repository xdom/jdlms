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
package org.openmuc.jdlms.sample.client;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

import org.openmuc.jdlms.AccessResultCode;
import org.openmuc.jdlms.AttributeAddress;
import org.openmuc.jdlms.AuthenticationMechanism;
import org.openmuc.jdlms.DlmsConnection;
import org.openmuc.jdlms.GetResult;
import org.openmuc.jdlms.RawMessageData;
import org.openmuc.jdlms.RawMessageListener;
import org.openmuc.jdlms.SecuritySuite;
import org.openmuc.jdlms.SecuritySuite.EncryptionMechanism;
import org.openmuc.jdlms.TcpConnectionBuilder;
import org.openmuc.jdlms.datatypes.DataObject;
import org.openmuc.jdlms.interfaceclass.attribute.AssociationLnAttribute;

public class SampleClient {

    public static void main(String[] args) throws IOException {

        // tag::todoc[]
        InetAddress inetAddress = InetAddress.getByName("127.0.0.1");
        SecuritySuite securitySuite = SecuritySuite.builder()
                .setPassword("Password".getBytes(StandardCharsets.US_ASCII))
                .setAuthenticationMechanism(AuthenticationMechanism.LOW)
                .setEncryptionMechanism(EncryptionMechanism.NONE)
                .build();

        TcpConnectionBuilder connectionBuilder = new TcpConnectionBuilder(inetAddress).setPort(6789)
                .setSecuritySuite(securitySuite)
                .setRawMessageListener(new RawMessageListener() {

                    @Override
                    public void messageCaptured(RawMessageData rawMessageData) {
                        // TODO: log data
                        // logger.debug(.. rawMessageData.getMessageSource() ..
                    }
                });

        try (DlmsConnection dlmsConnection = connectionBuilder.build()) {

            GetResult result = dlmsConnection
                    .get(new AttributeAddress(AssociationLnAttribute.OBJECT_LIST, "0.0.40.0.0.255"));

            if (result.getResultCode() == AccessResultCode.SUCCESS) {
                DataObject resultData = result.getResultData();
                System.out.println(resultData.toString());
            }
        } // closes the connection automatically at the end of this block
          // end::todoc[]
    }

}
