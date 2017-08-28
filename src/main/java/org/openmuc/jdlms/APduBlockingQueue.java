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
