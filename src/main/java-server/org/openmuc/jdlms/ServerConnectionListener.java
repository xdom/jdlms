package org.openmuc.jdlms;

import java.util.EventListener;

/**
 * A connection listener to listen to all new connections and to closing connections.
 */
public interface ServerConnectionListener extends EventListener {

    /**
     * Callback function, which is invoked, when a connection is being opened or closed.
     * 
     * @param connectionInfo
     *            the info about the client connecting to the DLMS server.
     */
    void connectionChanged(ServerConnectionInfo connectionInfo);
}
