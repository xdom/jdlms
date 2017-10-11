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
