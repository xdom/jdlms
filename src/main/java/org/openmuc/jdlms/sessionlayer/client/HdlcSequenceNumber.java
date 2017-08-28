package org.openmuc.jdlms.sessionlayer.client;

public class HdlcSequenceNumber {

    private int value;

    public HdlcSequenceNumber() {
        this.value = 0;
    }

    public int getValue() {
        return this.value;
    }

    public int increment() {
        int inc = this.value;
        this.value = (this.value + 1) % 8;
        return inc;
    }

}
