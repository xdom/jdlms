package org.openmuc.jdlms;

import java.util.EventListener;

/**
 * Listen to transmitted data between jDLMS client and the remote meter.
 *
 * @see TcpConnectionBuilder#setRawMessageListener(RawMessageListener)
 */
public interface RawMessageListener extends EventListener {

    /**
     * Invoked when a message from either jDLMS client or remote meter is captured.
     * 
     * @param rawMessageData
     *            the captured raw message data.
     */
    void messageCaptured(RawMessageData rawMessageData);
}
