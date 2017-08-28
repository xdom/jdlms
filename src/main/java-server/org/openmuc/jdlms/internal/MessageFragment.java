package org.openmuc.jdlms.internal;

import java.util.Arrays;

public class MessageFragment {

    private final int fragmentSize;
    private final byte[] rawData;

    private int index;

    public MessageFragment(byte[] rawData, int fragmentSize) {
        this.rawData = rawData;
        this.fragmentSize = fragmentSize;

        this.index = 0;
    }

    public boolean hasNext() {
        return this.index < this.rawData.length;
    }

    public byte[] next() {
        if (!hasNext()) {
            throw new NoSuchFragmentException();
        }

        int endIndex = this.index + this.fragmentSize;
        if (endIndex > this.rawData.length) {
            endIndex = this.rawData.length;
        }

        byte[] fragment = Arrays.copyOfRange(this.rawData, this.index, endIndex);

        this.index += this.fragmentSize;

        return fragment;
    }

    private class NoSuchFragmentException extends RuntimeException {
        public NoSuchFragmentException() {
            super();
        }
    }
}
