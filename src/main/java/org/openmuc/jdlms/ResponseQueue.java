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

import org.openmuc.jdlms.internal.asn1.cosem.COSEMpdu;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.BlockingQueue;

import org.openmuc.jdlms.internal.asn1.cosem.COSEMpdu;

class ResponseQueue {

    private final BlockingQueue<COSEMpdu> queue = new ArrayBlockingQueue<>(1);
    private final int timeout;
    private volatile boolean polled;
    private IOException lastError;

    public ResponseQueue() {
        this.timeout = 0;
    }

    public ResponseQueue(int timeout) {
        this.timeout = timeout;
    }

    public void put(COSEMpdu data) {
        try {
            this.queue.put(data);
        } catch (InterruptedException e) {
            // should not occur
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        }
    }

    public void putError(IOException ex) {
        this.lastError = ex;
        put(new COSEMpdu());
    }

    public synchronized COSEMpdu poll() throws IOException {
        this.polled = true;
        try {
            COSEMpdu cosemPdu = timeout == 0 ? queue.take()
                    : queue.poll(timeout, TimeUnit.MILLISECONDS);

            if (this.lastError != null) {
                IOException le = this.lastError;
                this.lastError = null;
                throw le;
            }
            return cosemPdu;
        } catch (InterruptedException e) {
            // Restore interrupted state...
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while waiting for incoming response");
        } finally {
            this.polled = false;
        }
    }

    public boolean beingPolled() {
        return this.polled;
    }

    public void clear() {
        this.queue.clear();
        this.lastError = null;
    }

}
