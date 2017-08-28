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
