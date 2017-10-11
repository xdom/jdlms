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
