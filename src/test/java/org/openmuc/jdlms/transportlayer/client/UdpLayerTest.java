package org.openmuc.jdlms.transportlayer.client;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.reflect.Whitebox.setInternalState;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InterruptedIOException;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.security.SecureRandom;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openmuc.jdlms.settings.client.TcpSettings;
import org.powermock.reflect.Whitebox;

public class UdpLayerTest {

    private DataOutputStream clientOStream;
    private DataInputStream serverIStream;
    private UdpLayer client;
    private UdpLayer server;

    @Before
    public void setUp() throws Exception {
        final int port = 9999;
        TcpSettings clientconf = confFor(port);

        client = new UdpLayer(clientconf);
        client.open();
        DatagramSocket ds = Whitebox.getInternalState(client, "socket");

        TcpSettings serverInetConf = confFor(ds.getLocalPort());

        server = spy(new UdpLayer(serverInetConf));
        server.open();
        ((DatagramSocket) Whitebox.getInternalState(server, "socket")).close();
        setInternalState(server, "socket", new DatagramSocket(port));

        this.clientOStream = client.getOutpuStream();

        this.serverIStream = server.getInputStream();
    }

    @After
    public void tearDown() throws Exception {
        try {
            this.clientOStream.close();
        } finally {
            this.serverIStream.close();
        }

    }

    @Test
    public void test_send_receive_data() throws Exception {

        SecureRandom random = new SecureRandom();
        final byte[] bytes = new byte[20];
        random.nextBytes(bytes);

        clientOStream.write(bytes);
        clientOStream.flush();

        assertEquals(bytes.length, serverIStream.available());
        byte[] b2 = new byte[bytes.length];
        serverIStream.readFully(b2);

        assertArrayEquals(bytes, b2);

    }

    @Test
    public void test_send_receive_data_fragments() throws Exception {

        SecureRandom random = new SecureRandom();
        final byte[] bytes = new byte[20];
        random.nextBytes(bytes);

        clientOStream.write(bytes, 0, 10);
        clientOStream.flush();
        clientOStream.write(bytes, 10, 10);
        clientOStream.flush();

        byte[] b2 = new byte[bytes.length];
        serverIStream.readFully(b2);

        assertArrayEquals(bytes, b2);

    }

    @Test
    public void test_send_receive_byte() throws Exception {

        SecureRandom random = new SecureRandom();
        boolean nextBoolean = random.nextBoolean();

        clientOStream.writeBoolean(nextBoolean);
        clientOStream.flush();

        assertEquals(1, serverIStream.available());
        assertEquals(nextBoolean, serverIStream.readBoolean());
    }

    @Test
    public void test_send_receive_max_msg() throws Exception {

        SecureRandom random = new SecureRandom();
        byte[] data = new byte[65507 * 2];
        random.nextBytes(data);

        clientOStream.write(data);
        clientOStream.flush();

        byte[] rData = new byte[data.length];
        this.serverIStream.readFully(rData);

        assertArrayEquals(data, rData);
    }

    @Test(expected = InterruptedIOException.class)
    public void test_timeout1() throws Exception {
        server.setTimeout(100);

        serverIStream.read();
    }

    @Test
    public void test_timeout2() throws Exception {
        final int timeOut = 100;
        server.setTimeout(timeOut);
        long t0 = System.currentTimeMillis();
        try {
            serverIStream.read();
            fail("Read did not time out: should hav timed out.");
        } catch (InterruptedIOException e) {
            // ignore
        }
        long actualTime = System.currentTimeMillis() - t0;
        double delta = 1.0;
        assertEquals(timeOut, actualTime, delta);

    }

    private static TcpSettings confFor(final int port) throws UnknownHostException {
        TcpSettings clientConf = mock(TcpSettings.class);

        when(clientConf.port()).thenReturn(port);
        when(clientConf.inetAddress()).thenReturn(Inet4Address.getByName("127.0.0.1"));
        return clientConf;
    }

}
