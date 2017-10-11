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
        return MessageFormat.format("{0} {1}", getFirstMethodId(), getFirstMethodOffset());
    }
}
