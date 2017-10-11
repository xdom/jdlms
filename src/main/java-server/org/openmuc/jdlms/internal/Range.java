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
