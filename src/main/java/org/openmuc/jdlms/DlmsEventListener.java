/*
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
 *
 */
package org.openmuc.jdlms;

import java.io.IOException;
import java.util.EventListener;

/**
 * The listener interface for receiving events from a remote smart meter.
 */
public interface DlmsEventListener extends EventListener {
    /**
     * Invoked when the remote station has sent an event.
     * 
     * @param event
     *            Data of the event
     */
    void onEventReceived(EventNotification event);

    /**
     * Invoked when an IOException occurred while listening for incoming messages. An IOException implies that the
     * ClientConnection that feeds this listener was automatically closed and can no longer be used to send commands or
     * receive messages.
     *
     * @param e
     *            the exception that occurred.
     */
    void connectionClosed(IOException e);
}
