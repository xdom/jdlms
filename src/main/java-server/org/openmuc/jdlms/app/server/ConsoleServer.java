package org.openmuc.jdlms.app.server;

import static javax.xml.bind.DatatypeConverter.parseHexBinary;
import static org.openmuc.jdlms.sessionlayer.server.ServerSessionLayerFactories.newHdlcSessionLayerFactory;
import static org.openmuc.jdlms.sessionlayer.server.ServerSessionLayerFactories.newWrapperSessionLayerFactory;
import static org.openmuc.jdlms.settings.client.ReferencingMethod.LOGICAL;
import static org.openmuc.jdlms.settings.client.ReferencingMethod.SHORT;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.openmuc.jdlms.AuthenticationMechanism;
import org.openmuc.jdlms.DlmsAccessException;
import org.openmuc.jdlms.DlmsInterceptor;
import org.openmuc.jdlms.DlmsInvocationContext;
import org.openmuc.jdlms.DlmsServer;
import org.openmuc.jdlms.LogicalDevice;
import org.openmuc.jdlms.SecuritySuite;
import org.openmuc.jdlms.ServerConnectionInfo;
import org.openmuc.jdlms.ServerConnectionListener;
import org.openmuc.jdlms.datatypes.DataObject;
import org.openmuc.jdlms.internal.cli.CliParameterBuilder;
import org.openmuc.jdlms.internal.cli.CliParseException;
import org.openmuc.jdlms.internal.cli.CliParser;
import org.openmuc.jdlms.internal.cli.FlagCliParameter;
import org.openmuc.jdlms.internal.cli.IntCliParameter;
import org.openmuc.jdlms.internal.cli.StringCliParameter;
import org.openmuc.jdlms.sessionlayer.server.ServerSessionLayerFactory;
import org.openmuc.jdlms.settings.client.ReferencingMethod;

public class ConsoleServer {

    public static void main(String[] args) throws IOException {
        CliParser cliParser = new CliParser("jdlms-console-sever", "DLMS/COSEM console demo server application");
        IntCliParameter port = new CliParameterBuilder("-p")
                .setDescription("The port to listen on. DLMS/COSEM servers usually listen on port 4059.")
                .setMandatory()
                .buildIntParameter("port");

        FlagCliParameter useSn = new CliParameterBuilder("-sn")
                .setDescription("Use short name referencing instead of logical name referencing.").buildFlagParameter();

        FlagCliParameter useHdlc = new CliParameterBuilder("-hdlc")
                .setDescription("Use HDCL layer instead of wrapper layer.").buildFlagParameter();

        StringCliParameter password = new CliParameterBuilder("-pass")
                .setDescription(
                        "A password that is used with authentication mechanism 1 (LOW) (e.g. an ASCII string: \"pass\" or a hex number \"0xada4d2\".")
                .buildStringParameter("password");

        IntCliParameter deviceId = new CliParameterBuilder("-did").setDescription("The device ID.")
                .buildIntParameter("device_id", 123456);

        StringCliParameter manId = new CliParameterBuilder("-mid")
                .setDescription("A three ASCII letters manufacturer ID.")
                .buildStringParameter("manufacturer_id", "ISE");

        IntCliParameter ldId = new CliParameterBuilder("-ld").setDescription("The logical device ID.")
                .buildIntParameter("logical_device_id", 1);

        StringCliParameter ldName = new CliParameterBuilder("-ldn").setDescription("The name of the logical device.")
                .buildStringParameter("logical_device_name", "ISE_LD_1");

        IntCliParameter inactivityTimeout = new CliParameterBuilder("-it")
                .setDescription("The inactivity timeout in ms.").buildIntParameter("inactivity_timeout", 0);
        cliParser.addParameters(
                Arrays.asList(port, useSn, useHdlc, password, deviceId, manId, ldId, ldName, inactivityTimeout));
        try {
            cliParser.parseArguments(args);
        } catch (CliParseException e) {
            System.out.println(cliParser.getUsageString());
            return;
        }
        LogicalDevice logicalDevice = new LogicalDevice(ldId.getValue(), ldName.getName(), manId.getValue(),
                deviceId.getValue());

        SecuritySuite securitySuite = setSecSuite(password);

        DlmsInterceptor interc = new DlmsInterceptor() {

            @Override
            public DataObject intercept(DlmsInvocationContext ctx) throws DlmsAccessException {

                System.out.println("-----------------------new req-----------------------");
                System.out.println(ctx.getXDlmsServiceType());
                System.out.println("-----------------------------------------------------");

                return ctx.proceed();
            }
        };
        ServerConnectionListener connectionListener = new ServerConnectionListener() {

            @Override
            public void connectionChanged(ServerConnectionInfo connectionInfo) {
                System.out.println(connectionInfo.getConnectionStatus() + " " + connectionInfo.getClientInetAddress());
            }
        };

        logicalDevice.addRestriction(16, securitySuite);
        logicalDevice.registerCosemObject(new SampleClass(interc));

        printServer("starting..");
        ServerSessionLayerFactory sessionLayerFactory = useHdlc.isSelected() ? newHdlcSessionLayerFactory()
                : newWrapperSessionLayerFactory();
        ReferencingMethod referencingMethod = useSn.isSelected() ? SHORT : LOGICAL;

        int portVal = port.getValue();
        try (DlmsServer dlmsServer = DlmsServer.tcpServerBuilder(portVal)
                .registerLogicalDevice(logicalDevice)
                .setConnectionListener(connectionListener)
                .setMaxClients(3)
                .setRefernceingMethod(referencingMethod)
                .setSessionLayerFactory(sessionLayerFactory)
                .setInactivityTimeout(inactivityTimeout.getValue())
                .build()) {

            printServer("Server started on port " + portVal);
            printServer("Press any key to exit.");
            System.in.read();

            dlmsServer.close();
        } catch (IOException e) {
            throw new IOException("DemoServer: ", e);
        }

    }

    private static SecuritySuite setSecSuite(StringCliParameter password) {
        SecuritySuite securitySuite;
        if (password.isSelected()) {
            byte[] pwBytes = convertStrToByteArray(password);
            securitySuite = SecuritySuite.builder()
                    .setAuthenticationMechanism(AuthenticationMechanism.LOW)
                    .setPassword(pwBytes)
                    .build();

        }
        else {
            securitySuite = SecuritySuite.builder().build();
        }
        return securitySuite;
    }

    private static byte[] convertStrToByteArray(StringCliParameter password) {
        String pwVal = password.getValue();
        if (pwVal.startsWith("0x")) {
            return parseHexBinary(pwVal.substring(2));
        }
        else {
            return pwVal.getBytes(StandardCharsets.US_ASCII);
        }
    }

    private static void printServer(String message) {
        System.out.println("DemoServer: " + message);
    }
}
