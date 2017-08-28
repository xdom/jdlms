/*
 * Copyright Fraunhofer ISE, 2012
 * Author(s): Karsten Mueller-Bier
 * 
 * This file is part of jASN1.
 * For more information visit http://www.openmuc.org
 * 
 * jASN1 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * jASN1 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with jASN1.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package org.openmuc.jdlms.internal.asn1.axdr.types;

import java.io.IOException;
import java.io.InputStream;

import org.openmuc.jasn1.ber.BerByteArrayOutputStream;
import org.openmuc.jdlms.internal.asn1.axdr.AxdrType;

public class AxdrOptional<T extends AxdrType> {

    private T value;
    private boolean usage;

    public AxdrOptional(T value, boolean usage) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }
        this.value = value;
        this.usage = usage;
    }

    public int encode(BerByteArrayOutputStream axdrOStream) throws IOException {
        int codeLength = 0;

        if (usage) {
            codeLength += value.encode(axdrOStream);
        }

        codeLength += new AxdrBoolean(usage).encode(axdrOStream);

        return codeLength;
    }

    public int decode(InputStream iStream) throws IOException {
        int codeLength = 0;

        AxdrBoolean axdrUsage = new AxdrBoolean();
        codeLength += axdrUsage.decode(iStream);

        usage = axdrUsage.getValue();

        if (usage) {
            codeLength += value.decode(iStream);
        }

        return codeLength;
    }

    public void setUsed(boolean use) {
        usage = use;
    }

    public boolean isUsed() {
        return usage;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        usage = value != null;

        if (value != null) {
            this.value = value;
        }
    }

    @Override
    public String toString() {
        if (usage) {
            return value.toString();
        }
        else {
            return "not set";
        }
    }
}
