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
