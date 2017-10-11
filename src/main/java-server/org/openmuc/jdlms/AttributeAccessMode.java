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

import org.openmuc.jdlms.datatypes.DlmsEnumeration;

/**
 * The access restrictions for COSEM attributes.
 */
public enum AttributeAccessMode implements DlmsEnumeration {
    NO_ACCESS(0),
    READ_ONLY(1),
    WRITE_ONLY(2),
    READ_AND_WRITE(3),
    AUTHENTICATED_READ_ONLY(4),
    AUTHENTICATED_WRITE_ONLY(5),
    AUTHENTICATED_READ_AND_WRITE(6);

    private int code;

    private AttributeAccessMode(int code) {
        this.code = code;
    }

    @Override
    public long getCode() {
        return code;
    }
}
