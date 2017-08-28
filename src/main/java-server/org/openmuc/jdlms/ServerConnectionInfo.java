package org.openmuc.jdlms;

import java.net.InetAddress;

/**
 * Information about the change of the connection.
 */
public interface ServerConnectionInfo {

    /**
     * Get the client inet address.
     * 
     * @return the client inet address.
     */
    InetAddress getClientInetAddress();

    /**
     * Get the logical device address.
     * 
     * @return the logical device address.
     */
    int getLogicalDeviceAddress();

    /**
     * Get the client ID.
     * 
     * @return the client ID.
     */
    int getClientId();

    /**
     * Get the connection status.
     * 
     * @return the connection status.
     */
    Status getConnectionStatus();

    /**
     * The status of a connection.
     */
    enum Status {
        /**
         * A connection (e.g. streams/sockets) where closed.
         */
        CLOSED,
        /**
         * A new connection was established.
         */
        OPEN;
    }
}
