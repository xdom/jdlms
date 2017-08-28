package org.openmuc.jdlms.sessionlayer.client;

import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.openmuc.jdlms.RawMessageData;
import org.openmuc.jdlms.RawMessageData.MessageSource;
import org.openmuc.jdlms.RawMessageData.RawMessageDataBuilder;
import org.openmuc.jdlms.RawMessageListener;
import org.openmuc.jdlms.sessionlayer.client.WrapperHeader.WrapperHeaderBuilder;
import org.openmuc.jdlms.settings.client.Settings;
import org.openmuc.jdlms.transportlayer.client.TransportLayer;

public class WrapperLayer implements SessionLayer {
    private SessionLayerListener eventListener;

    private final WrapperHeaderBuilder headerBuilder;
    private final TransportLayer transportLayer;

    private final Settings settings;
    private boolean closed;
    private final ExecutorService exec;

    private DataOutputStream outpuStream;

    public WrapperLayer(Settings settings, TransportLayer transportLayer) throws IOException {
        this.settings = settings;
        this.headerBuilder = WrapperHeader.builder(settings.clientId(), settings.logicalDeviceId());
        this.transportLayer = transportLayer;

        this.closed = true;

        this.exec = Executors.newSingleThreadExecutor();
    }

    @Override
    public void startListening(SessionLayerListener eventListener) throws IOException {
        if (!closed) {
            return;
        }
        this.eventListener = eventListener;

        this.transportLayer.open();
        this.closed = false;
        this.outpuStream = this.transportLayer.getOutpuStream();

        this.exec.execute(new ConnectionReader());
    }

    @Override
    public void send(byte[] tSdu, int off, int len, RawMessageDataBuilder rawMessageDataBuilder) throws IOException {
        byte[] headerBytes = this.headerBuilder.setLength(len).build().encode();

        byte[] wpdu = ByteBuffer.allocate(len + WrapperHeader.HEADER_LENGTH)
                .put(headerBytes)
                .put(tSdu, off, len)
                .array();

        try {
            outpuStream.write(wpdu);
            outpuStream.flush();
        } finally {
            RawMessageListener rawMessageListener = this.settings.rawMessageListener();
            if (rawMessageListener != null) {
                rawMessageListener.messageCaptured(
                        rawMessageDataBuilder.setMessage(wpdu).setMessageSource(MessageSource.CLIENT).build());
            }
        }
    }

    @Override
    public void close() throws IOException {
        if (this.closed) {
            return;
        }

        this.exec.shutdown();
        this.transportLayer.close();
        this.closed = true;
    }

    private class ConnectionReader implements Runnable {

        @Override
        public void run() {
            Thread.currentThread().setName("jDLMS - WRAPPER/INET-CONNECTION-READER thread");

            try {
                while (!transportLayer.isClosed()) {
                    WrapperPdu wrapperPdu = WrapperPdu.decode(transportLayer, settings);

                    RawMessageDataBuilder rawMessageBuilder = createMsgBuilder(wrapperPdu);
                    eventListener.dataReceived(wrapperPdu.getData(), rawMessageBuilder);

                    Thread.yield();
                }
            } catch (EOFException e) {
                if (!closed) {
                    EOFException ex = new EOFException("Socket was closed by remote host.");
                    eventListener.connectionInterrupted(ex);
                }
            } catch (IOException e) {
                eventListener.connectionInterrupted(e);
            } finally {
                unsaveClose();
            }
        }

        private RawMessageDataBuilder createMsgBuilder(WrapperPdu wrapperPdu) {
            if (settings.rawMessageListener() == null) {
                return null;
            }
            WrapperHeader header = wrapperPdu.getheader();
            byte[] message = ByteBuffer.allocate(WrapperHeader.HEADER_LENGTH + header.getPayloadLength())
                    .put(header.encode())
                    .put(wrapperPdu.getData())
                    .array();

            return RawMessageData.builder().setMessage(message);
        }

    }

    private void unsaveClose() {
        if (closed) {
            return;
        }

        try {
            close();
        } catch (IOException e) {
            // ignore
        }
    }

}
