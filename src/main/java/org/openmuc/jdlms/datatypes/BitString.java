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
package org.openmuc.jdlms.datatypes;

/**
 * Arbitrary string of bits (zeros and ones). A bit string value can have any length including zero.
 */
public class BitString {
    private final byte[] bytes;
    private final int numBits;

    /**
     * Construct a new bit string object.
     * 
     * @param bitString
     *            as a a byte array.
     * @param numBits
     *            the number of bits.
     * @throws IllegalArgumentException
     *             if the passed number of is is out of range.<br>
     *             <code>numBits &lt;= (bitString.length - 1) * 8 + 1 || numBits &gt; bitString.length * 8</code>
     */
    public BitString(byte[] bitString, int numBits) throws IllegalArgumentException {
        if (bitString == null) {
            throw new NullPointerException("bitString cannot be null");
        }
        if (numBits < 0) {
            throw new IllegalArgumentException("numBits cannot be negative.");
        }
        if (numBits > (bitString.length * 8)) {
            throw new IllegalArgumentException("'bitString' is too short to hold all bits.");
        }

        this.bytes = bitString;
        this.numBits = numBits;
    }

    /**
     * Copy Constructor.
     * 
     * @param other
     *            the other bit string,
     */
    public BitString(BitString other) {
        this(other.bytes.clone(), other.numBits);
    }

    /**
     * Get the bit string as byte array.
     * 
     * @return the bit string.
     */
    public byte[] getBitString() {
        return this.bytes;
    }

    /**
     * The number of bits in the byte array.
     * 
     * @return the number of bits.
     * @see #getBitString()
     */
    public int getNumBits() {
        return this.numBits;
    }
}
