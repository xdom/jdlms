package org.openmuc.jdlms.sessionlayer.hdlc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class HdlcFrameSegmentBuffer {

    private final List<HdlcFrame> segments;

    public HdlcFrameSegmentBuffer() {
        this.segments = new LinkedList<>();
    }

    public void buffer(HdlcFrame segment) {
        this.segments.add(segment);
    }

    public boolean isEmpty() {
        return this.segments.isEmpty();
    }

    public byte[] toByteArray() {
        Iterator<HdlcFrame> iter = this.segments.iterator();

        if (!iter.hasNext()) {
            return new byte[0];
        }

        ByteArrayOutputStream bios = new ByteArrayOutputStream();

        try {
            HdlcFrame next = iter.next();
            bios.write(next.getInformationFieldWithoutLlc());

            while (iter.hasNext()) {
                next = iter.next();
                bios.write(next.getInformationField());
            }

        } catch (IOException e) {
            // should not occur
        }

        return bios.toByteArray();
    }

    public void clear() {
        this.segments.clear();
    }

    public byte[] concatFramesBytes() {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.write(0x7E);
        for (HdlcFrame hdlcFrame : segments) {

            try {
                byteArrayOutputStream.write(hdlcFrame.encodeWithoutFlags());
            } catch (IOException e) {
                // should not occur
            }

            byteArrayOutputStream.write(0x7E);
        }

        return byteArrayOutputStream.toByteArray();
    }

}
