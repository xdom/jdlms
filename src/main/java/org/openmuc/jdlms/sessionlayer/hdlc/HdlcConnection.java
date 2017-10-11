package org.openmuc.jdlms.sessionlayer.hdlc;

import org.openmuc.jdlms.FatalJDlmsException;
import org.openmuc.jdlms.RawMessageData;
import org.openmuc.jdlms.RawMessageListener;
import org.openmuc.jdlms.settings.client.HdlcSettings;
import org.openmuc.jdlms.settings.client.Settings;
import org.openmuc.jdlms.transportlayer.client.TransportLayer;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static org.openmuc.jdlms.JDlmsException.ExceptionId.HDLC_CONNECTION_CLOSE_ERROR;
import static org.openmuc.jdlms.JDlmsException.ExceptionId.HDLC_CONNECTION_ESTABLISH_ERROR;
import static org.openmuc.jdlms.JDlmsException.Fault.SYSTEM;
import static org.openmuc.jdlms.RawMessageData.MessageSource.CLIENT;
import static org.openmuc.jdlms.sessionlayer.hdlc.HdlcFrame.newDisconnectFrame;
import static org.openmuc.jdlms.sessionlayer.hdlc.HdlcFrame.newSetNormalResponseModeFrame;

/**
 * @author Dominik Matta
 */
public class HdlcConnection {
    private final TransportLayer transportLayer;
    private final Settings settings;
    private final Map<HdlcAddressPair, Listener> listeners;

    private HdlcAddressPair connectionKey;
    private final BlockingQueue<HdlcFrame> incommingQueue;
    private ExecutorService connectionreaderExecutor;

    protected HdlcConnection(Settings settings, TransportLayer transportLayer) {
        this.settings = settings;
        this.transportLayer = transportLayer;
        this.listeners = new LinkedHashMap<>();

        this.incommingQueue = new ArrayBlockingQueue<>(1);
    }

    public synchronized void send(byte[] data) throws IOException {
        this.transportLayer.getOutpuStream().write(data);
        this.transportLayer.getOutpuStream().flush();
    }

    public synchronized HdlcParameters open(HdlcSettings settings) throws IOException {
        if (this.transportLayer.isClosed()) {
            this.transportLayer.open();
            onTransportLayerOpen();
        }

        this.connectionKey = settings.addressPair();
        try {
            return connectSequence(settings);
        } catch (IOException ex) {
            closeAndShutdown(settings);
            throw ex;
        } finally {
            this.connectionKey = null;
        }
    }

    protected void onTransportLayerOpen() {
        this.connectionreaderExecutor = Executors.newSingleThreadExecutor();
        this.connectionreaderExecutor.execute(new ConnectionReader());
    }

    public synchronized void disconnect(HdlcSettings settings) throws IOException {
        this.connectionKey = settings.addressPair();

        try {
            sendDisconnectSequence(settings);
        } finally {
            closeAndShutdown(settings);
        }
    }

    private void closeAndShutdown(HdlcSettings settings) throws IOException {
        removeListenerAndTryClosePhysicalLayer(settings);

        this.connectionKey = null;
        this.connectionreaderExecutor.shutdown();
    }

    private void removeListenerAndTryClosePhysicalLayer(HdlcSettings settings) throws IOException {
        synchronized (this.listeners) {
            this.listeners.remove(settings.addressPair());

            if (this.listeners.isEmpty()) {
                this.transportLayer.close();
            }
        }
    }

    private void sendDisconnectSequence(HdlcSettings settings) throws IOException {
        boolean poll = true;
        byte[] dfData = newDisconnectFrame(settings.addressPair(), poll).encode();

        send(dfData);

        RawMessageListener rawMessageListener = settings.rawMessageListener();
        notifyListener(dfData, CLIENT, rawMessageListener);

        HdlcFrame disconnectAckFrame = waitForFrame(settings.responseTimeout());

        if (disconnectAckFrame == null) {
            throw new FatalJDlmsException(HDLC_CONNECTION_CLOSE_ERROR, SYSTEM,
                    "Didn't receive answer on connection close.");
        }
        notifyListener(disconnectAckFrame, CLIENT, rawMessageListener);

        if (disconnectAckFrame.getFrameType() == FrameType.UNNUMBERED_ACKNOWLEDGE
                || disconnectAckFrame.getFrameType() == FrameType.DISCONNECT_MODE) {
            // do something with this information
        }
    }

    private HdlcFrame waitForFrame(long responseTimeout) {
        HdlcFrame receivedFrame = null;
        try {
            receivedFrame = this.incommingQueue.poll(responseTimeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            // ignore, since this should't occur
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        }
        return receivedFrame;
    }

    protected HdlcParameters connectSequence(HdlcSettings settings) throws IOException {
        int informationLength = settings.hdlcMaxInformationLength();
        int windowSize = HdlcParameters.MIN_WINDOW_SIZE;
        HdlcParameters dNegotiation = new HdlcParameters(informationLength, windowSize, informationLength,
                windowSize);

        boolean pollFinalBit = true;

        HdlcFrame answerFrame = sendNRMF(settings, dNegotiation, pollFinalBit);

        if (answerFrame.getFrameType() == FrameType.DISCONNECT_MODE) {
            answerFrame = sendNRMF(settings, null, pollFinalBit);
        }

        switch (answerFrame.getFrameType()) {
            case UNNUMBERED_ACKNOWLEDGE:
                return handleUnnumberedAckResponse(settings, answerFrame);

            default:
            case DISCONNECT_MODE:
                throw handleDisconnectResponse(settings, answerFrame);
        }
    }

    private HdlcFrame sendNRMF(HdlcSettings settings, HdlcParameters dNegotiation, boolean pollFinalBit)
            throws IOException {
        byte[] snrmData = newSetNormalResponseModeFrame(settings.addressPair(), dNegotiation, pollFinalBit)
                .encode();

        RawMessageListener rawMessageListener = settings.rawMessageListener();
        notifyListener(snrmData, CLIENT, rawMessageListener);

        send(snrmData);
        HdlcFrame answerFrame = waitForFrame(settings.responseTimeout());

        if (answerFrame == null) {
            throw new FatalJDlmsException(HDLC_CONNECTION_ESTABLISH_ERROR, SYSTEM,
                    "Didn't receive answer in connection establish process.");
        }

        notifyListener(answerFrame, RawMessageData.MessageSource.SERVER, rawMessageListener);
        return answerFrame;
    }

    private void notifyListener(byte[] data, RawMessageData.MessageSource client, RawMessageListener rawMessageListener) {
        if (rawMessageListener == null) {
            return;
        }

        RawMessageData rawMessageData = RawMessageData.builder().setMessageSource(client).setMessage(data).build();
        rawMessageListener.messageCaptured(rawMessageData);
    }

    private void notifyListener(HdlcFrame frame, RawMessageData.MessageSource client, RawMessageListener rawMessageListener) {
        if (rawMessageListener == null) {
            return;
        }

        notifyListener(frame.encode(), client, rawMessageListener);
    }

    private FatalJDlmsException handleDisconnectResponse(HdlcSettings settings, HdlcFrame receiveFrame)
            throws IOException {
        closeAndShutdown(settings);
        return new FatalJDlmsException(HDLC_CONNECTION_ESTABLISH_ERROR, SYSTEM,
                MessageFormat.format("Received a {0} frame, while connecting. Connection has been shot down.",
                        receiveFrame.getFrameType()));
    }

    private HdlcParameters handleUnnumberedAckResponse(HdlcSettings settings, HdlcFrame receiveFrame)
            throws IOException {
        if (receiveFrame.getInformationField() == null) {
            throw new FatalJDlmsException(HDLC_CONNECTION_ESTABLISH_ERROR, SYSTEM,
                    "Remote meter didn't return a parameter negotioation.");
        }
        try {
            return HdlcParameters.decode(receiveFrame.getInformationField());
        } catch (FrameInvalidException e) {
            throw new FatalJDlmsException(HDLC_CONNECTION_ESTABLISH_ERROR, SYSTEM,
                    "Received parameter negotiation, contains errors. Evaluate cause for details.", e);
        }
    }

    void registerNewListener(HdlcAddressPair key, Listener listener) {
        synchronized (this.listeners) {
            this.listeners.put(key, listener);
        }
    }

    public interface Listener {
        void dataReceived(RawMessageData.RawMessageDataBuilder rawMessageBuilder, HdlcFrame frame);

        void connectionInterrupted(IOException e);
    }

    private class ConnectionReader implements Runnable {

        @Override
        public void run() {
            Thread.currentThread().setName("HDLC CONNECTION READER");

            try {
                mainLoop();
            } catch (InterruptedIOException e) {
                // ignore
            } catch (IOException e) {
                notifyAllListners(e);
            } finally {
                closeAll();
            }
        }

        private void closeAll() {
            try {
                transportLayer.close();
                listeners.clear();
            } catch (IOException e) {
                // ignore
            }
        }

        private void notifyAllListners(IOException e) {
            for (Listener listener : listeners.values()) {
                listener.connectionInterrupted(e);
            }
        }

        private void mainLoop() throws IOException {
            while (!transportLayer.isClosed()) {
                RawMessageData.RawMessageDataBuilder rawMessageBuilder = null;
                if (settings.rawMessageListener() != null) {
                    rawMessageBuilder = RawMessageData.builder();
                }

                List<HdlcFrame> frames = HdlcMessageDecoder.decode(rawMessageBuilder, transportLayer,
                        settings.responseTimeout());

                for (HdlcFrame hdlcFrame : frames) {
                    HdlcAddressPair switchedPair = hdlcFrame.getAddressPair().switchedPair();
                    if (connectionKey != null && connectionKey.equals(switchedPair)) {
                        try {
                            incommingQueue.put(hdlcFrame);
                        } catch (InterruptedException e) {
                            // ignore this
                            // Restore interrupted state...
                            Thread.currentThread().interrupt();
                        }
                    } else {
                        synchronized (listeners) {
                            Listener listener = listeners.get(switchedPair);

                            if (listener != null) {
                                listener.dataReceived(rawMessageBuilder, hdlcFrame);
                            }
                            // else {
                            // // ignore
                            // }

                        }
                    }

                }
            }
        }

    }

}
