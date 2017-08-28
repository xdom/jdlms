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

public class AxdrNull implements AxdrType {

    public AxdrNull() {
    }

    @Override
    public int encode(BerByteArrayOutputStream axdrOStream) throws IOException {
        return 0;
    }

    @Override
    public int decode(InputStream iStream) throws IOException {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof AxdrNull) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
