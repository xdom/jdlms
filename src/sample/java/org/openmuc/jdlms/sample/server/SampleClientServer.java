package org.openmuc.jdlms.sample.server;

import static javax.xml.bind.DatatypeConverter.parseHexBinary;
import static javax.xml.bind.DatatypeConverter.printHexBinary;
import static org.openmuc.jdlms.SecuritySuite.newSecuritySuiteFrom;
import static org.openmuc.jdlms.datatypes.DataObject.newUInteger16Data;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.openmuc.jdlms.AccessResultCode;
import org.openmuc.jdlms.AttributeAddress;
import org.openmuc.jdlms.AuthenticationMechanism;
import org.openmuc.jdlms.DlmsAccessException;
import org.openmuc.jdlms.DlmsConnection;
import org.openmuc.jdlms.DlmsInterceptor;
import org.openmuc.jdlms.DlmsInvocationContext;
import org.openmuc.jdlms.DlmsServer;
import org.openmuc.jdlms.GetResult;
import org.openmuc.jdlms.LogicalDevice;
import org.openmuc.jdlms.MethodParameter;
import org.openmuc.jdlms.MethodResult;
import org.openmuc.jdlms.SecuritySuite;
import org.openmuc.jdlms.SecuritySuite.EncryptionMechanism;
import org.openmuc.jdlms.SecurityUtils;
import org.openmuc.jdlms.SecurityUtils.KeyId;
import org.openmuc.jdlms.SelectiveAccessDescription;
import org.openmuc.jdlms.TcpConnectionBuilder;
import org.openmuc.jdlms.datatypes.DataObject;
import org.openmuc.jdlms.interfaceclass.InterfaceClass;
import org.openmuc.jdlms.interfaceclass.attribute.AssociationLnAttribute;
import org.openmuc.jdlms.internal.WellKnownInstanceIds;

public class SampleClientServer {
    private final static byte[] AUTHENTICATION_KEY = parseHexBinary("5468697349734150617373776f726431");

    private final static byte[] GLOBAL_ENCRYPTION_KEY = parseHexBinary("000102030405060708090a0b0c0d0e0f");
    private static final byte[] MASTER_KEY = parseHexBinary("aa0102030405060738090a0b0c0d0eff");;

    private final static int PORT = 6789;
    private final static String MANUFACTURE_ID = "ISE";
    private final static long DEVICE_ID = 9999L;
    private final static String LOGICAL_DEVICE_ID = "L_D_I";

    private final static SecuritySuite AUTHENTICATION_C = SecuritySuite.builder()
            .setAuthenticationKey(AUTHENTICATION_KEY)
            .setGlobalUnicastEncryptionKey(GLOBAL_ENCRYPTION_KEY)
            .setAuthenticationMechanism(AuthenticationMechanism.HLS5_GMAC)
            .setEncryptionMechanism(EncryptionMechanism.AES_GMC_128)
            .build();

    private final static SecuritySuite AUTHENTICATION_S = newSecuritySuiteFrom(AUTHENTICATION_C);

    public static void main(String[] args) throws IOException {
        DlmsServer serverConnection;

        try {
            printServer("starting");
            LogicalDevice logicalDevice = new LogicalDevice(1, LOGICAL_DEVICE_ID, "HMM", DEVICE_ID);
            logicalDevice.setMasterKey(MASTER_KEY);
            logicalDevice.addRestriction(16, AUTHENTICATION_S);
            logicalDevice.registerCosemObject(new SampleClass(new DlmsInterceptor() {

                @Override
                public DataObject intercept(DlmsInvocationContext ctx) throws DlmsAccessException {
                    printServer("--------------------------------------");
                    printServer(ctx.getXDlmsServiceType());
                    printServer(ctx.getCosemResourceDescriptor().getClassId());
                    printServer(ctx.getCosemResourceDescriptor().getInstanceId());
                    printServer(ctx.getCosemResourceDescriptor().getId());
                    printServer(ctx.getSecurityPolicy());
                    printServer("--------------------------------------");

                    return ctx.proceed();
                }
            }));

            serverConnection = DlmsServer.tcpServerBuilder(PORT).registerLogicalDevice(logicalDevice).build();

            printServer("started");
        } catch (IOException e) {
            throw new IOException("DemoServer: " + e);
        }

        try {
            runDemoClient();

        } finally {
            if (serverConnection != null) {
                serverConnection.close();
            }
            printServer("closed");
        }

    }

    static void runDemoClient() throws IOException {
        TcpConnectionBuilder connectionBuilder = new TcpConnectionBuilder(InetAddress.getLocalHost())
                .setLogicalDeviceId(1)
                .setPort(PORT)
                .setSecuritySuite(AUTHENTICATION_C)
                .setSystemTitle(MANUFACTURE_ID, DEVICE_ID);

        printClient("connecting to server");

        byte[] newKey = GLOBAL_ENCRYPTION_KEY.clone();
        DlmsConnection client = connectionBuilder.build();
        printClient("connected");

        int accessSelector = 2;
        DataObject accessParameter = DataObject
                .newArrayData(Arrays.asList(newUInteger16Data(InterfaceClass.ASSOCIATION_LN.id()),
                        newUInteger16Data(InterfaceClass.SAP_ASSIGNMENT.id())));
        SelectiveAccessDescription access = new SelectiveAccessDescription(accessSelector, accessParameter);
        // access = null;
        GetResult getResult2 = client.get(new AttributeAddress(AssociationLnAttribute.OBJECT_LIST,
                WellKnownInstanceIds.CURRENT_ASSOCIATION_ID, access));

        if (getResult2.requestSuccessful()) {
            System.out.println(getResult2.getResultData());
        }

        for (int i = 0; i < newKey.length; i++) {
            newKey[i] = (byte) (newKey[i] ^ 42);
        }

        printClient("new enckey " + printHexBinary(newKey));
        MethodParameter keyChangeMethodParam = SecurityUtils.keyChangeMethodParamFor(MASTER_KEY, newKey,
                KeyId.GLOBAL_UNICAST_ENCRYPTION_KEY);

        MethodResult methodResult = client.action(keyChangeMethodParam);
        printClient("Change key: " + methodResult.getResultCode());

        client.changeClientGlobalEncryptionKey(newKey);
        final String associationLnInstancId = "0.0.40.0.0.255";

        AccessResultCode resultCode = client
                .get(new AttributeAddress(AssociationLnAttribute.OBJECT_LIST, associationLnInstancId)).getResultCode();
        printClient("Req after change key: " + resultCode);

        client.disconnect();

        SecuritySuite newAuth = SecuritySuite.builder()
                .setAuthenticationKey(AUTHENTICATION_KEY)
                .setGlobalUnicastEncryptionKey(newKey)
                .setAuthenticationMechanism(AuthenticationMechanism.HLS5_GMAC)
                .setEncryptionMechanism(EncryptionMechanism.AES_GMC_128)
                .build();
        connectionBuilder.setSecuritySuite(newAuth);
        try (DlmsConnection con = connectionBuilder.build()) {
            MethodResult result = con.action(new MethodParameter(99, "0.0.0.2.1.255", 1));
            printClient(result.getResultCode());

            AttributeAddress attributeAddress = new AttributeAddress(99, "0.0.0.2.1.255", 2);
            GetResult getResult = con.get(attributeAddress);
            DataObject resultData = getResult.getResultData();
            printClient("--------------------------------");
            printClient(resultData);
            byte[] bytes = resultData.getValue();
            printClient(new String(bytes, StandardCharsets.UTF_8));
            printClient("--------------------------------");

        }

        printClient("closed");
    }

    private static void printClient(Object message) {
        System.out.println("DemoClient: " + message);
    }

    private static void printServer(Object message) {
        System.out.println("DemoServer: " + message);
    }
}
