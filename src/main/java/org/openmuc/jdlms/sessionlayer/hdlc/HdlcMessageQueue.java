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
package org.openmuc.jdlms.sessionlayer.hdlc;

import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class HdlcMessageQueue implements Iterable<byte[]> {

    private BlockingQueue<HdlcMessage> sendQueue;
    private int capacity;

    public HdlcMessageQueue(int capacity) {
        this.sendQueue = new ArrayBlockingQueue<>(capacity);
        this.capacity = capacity;
    }

    public void reszize(int newSize) {
        BlockingQueue<HdlcMessage> oldQueue = this.sendQueue;
        this.sendQueue = new ArrayBlockingQueue<>(newSize);
        this.sendQueue.addAll(oldQueue);

        this.capacity = newSize;
    }

    public int getCapacity() {
        return this.capacity;
    }

    public void clearTil(int sendSeq) {

        while (!sendQueue.isEmpty() && sendQueue.poll().sequenceCounter < sendSeq) {
            // do nothing, sendQueue.poll() already removed the frame
        }
    }

    public int size() {
        return this.sendQueue.size();
    }

    public void clear() {
        sendQueue.clear();
    }

    public void offerMessage(byte[] dataToSend, int sendSequence) {
        HdlcMessage newHdlcMessage = new HdlcMessage(dataToSend, sendSequence);
        try {
            if (!this.sendQueue.offer(newHdlcMessage, 10, TimeUnit.SECONDS)) {
                // TODO fatal..
            }
        } catch (InterruptedException e) {
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public Iterator<byte[]> iterator() {
        return new ByteArrayIter();
    }

    private class HdlcMessage {
        private final byte[] data;
        private final int sequenceCounter;

        public HdlcMessage(byte[] data, int sequenceCounter) {
            this.data = data;
            this.sequenceCounter = sequenceCounter;
        }

        public byte[] data() {
            return data;
        }
    }

    private class ByteArrayIter implements Iterator<byte[]> {

        private final Iterator<HdlcMessage> iterator;

        public ByteArrayIter() {
            this.iterator = sendQueue.iterator();
        }

        @Override
        public boolean hasNext() {
            return this.iterator.hasNext();
        }

        @Override
        public byte[] next() {
            return this.iterator.next().data();
        }

        @Override
        public void remove() {
            this.iterator.remove();
        }

    }

}
