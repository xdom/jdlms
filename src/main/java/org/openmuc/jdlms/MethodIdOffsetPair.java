package org.openmuc.jdlms;

import java.io.Serializable;
import java.text.MessageFormat;

public class MethodIdOffsetPair implements Serializable {

    private final Integer firstMethodId;
    private final Integer firstMethodOffset;

    public MethodIdOffsetPair(int firstMethodId, int firstMethodOffset) {
        this.firstMethodId = firstMethodId;
        this.firstMethodOffset = firstMethodOffset;
    }

    public int getFirstMethodId() {
        return this.firstMethodId;
    }

    public int getFirstMethodOffset() {
        return this.firstMethodOffset;
    }

    @Override
    public int hashCode() {
        int h1 = this.firstMethodId.hashCode();
        int h2 = this.firstMethodOffset.hashCode();
        return (h1 + h2) * h2 + h1;
    }

    @Override
    public boolean equals(Object obj) {

        if (!(obj instanceof MethodIdOffsetPair)) {
            return false;
        }

        MethodIdOffsetPair other = (MethodIdOffsetPair) obj;

        return this.firstMethodId.equals(other.firstMethodId) && this.firstMethodOffset.equals(other.firstMethodOffset);
    }

    @Override
    public String toString() {
        return MessageFormat.format("{} {}", getFirstMethodId(), getFirstMethodOffset());
    }
}
