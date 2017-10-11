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

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.openmuc.jdlms.internal.APdu;

class APduBlockingQueue {

    private IOException ioException;

    private final BlockingQueue<APdu> queue;

    public APduBlockingQueue() {
        this.queue = new LinkedBlockingQueue<>();
    }

    public void putError(IOException e) {
        this.ioException = e;
        put(new APdu(null, null));
    }

    public void put(APdu aPdu) {
        try {
            this.queue.put(aPdu);
        } catch (InterruptedException e) {
            // ignore this here
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        }
    }

    public APdu poll(long timeout, TimeUnit unit) throws InterruptedException, IOException {
        throwExIfExists();
        APdu poll = this.queue.poll(timeout, unit);
        throwExIfExists();
        return poll;
    }

    private void throwExIfExists() throws IOException {
        if (this.ioException != null) {
            IOException ex = this.ioException;
            this.ioException = null;

            throw ex;
        }
    }

    public APdu take() throws InterruptedException, IOException {
        throwExIfExists();
        APdu aPdu = this.queue.take();
        throwExIfExists();
        return aPdu;
    }

    public void clear() {
        this.queue.clear();
    }

}
