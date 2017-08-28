package org.openmuc.jdlms.internal;

import java.text.MessageFormat;

public class Range<T extends Comparable<T>> implements Comparable<Range<T>> {

    private final T upperBound;
    private final T lowerBound;

    public Range(T lowerBound, T upperBound) {
        if (upperBound.compareTo(lowerBound) < 0) {
            throw new IllegalArgumentException("Lower bound is greater than upper bound.");
        }
        this.upperBound = upperBound;
        this.lowerBound = lowerBound;

    }

    /**
     * Checks if this range intersects with an other range.
     * 
     * @param other
     *            the other range.
     * @return true if the ranges intersect.
     */
    public boolean intersects(Range<T> other) {

        if (this.lowerBound.compareTo(other.lowerBound) > 0) {
            return other.upperBound.compareTo(this.lowerBound) >= 0;
        }
        else if (this.lowerBound.compareTo(other.lowerBound) < 0) {

            return this.upperBound.compareTo(other.lowerBound) >= 0;
        }
        else {
            return true;
        }
    }

    public boolean intersectsWith(T elem) {
        return this.lowerBound.compareTo(elem) <= 0 && this.upperBound.compareTo(elem) >= 0;
    }

    @Override
    public int compareTo(Range<T> o) {
        return this.lowerBound.compareTo(o.lowerBound);
    }

    public T getLowerBound() {
        return lowerBound;
    }

    public T getUpperBound() {
        return upperBound;
    }

    @Override
    public String toString() {
        return MessageFormat.format("'{'\"lowerBound: \"{0}\", \"upperBound\": \"{1}\"'}'", lowerBound, upperBound);
    }
}
