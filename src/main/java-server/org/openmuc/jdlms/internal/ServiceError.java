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

import org.openmuc.jdlms.datatypes.DlmsEnumeration;
import org.openmuc.jdlms.internal.asn1.cosem.Enum;

public enum ServiceError implements DlmsEnumeration {

    OPERATION_NOT_POSSIBLE(1),
    SERVICE_NOT_SUPPORTED(2),
    OTHER_REASON(3);

    private long code;

    private ServiceError(long code) {
        this.code = code;
    }

    @Override
    public long getCode() {
        return this.code;
    }

    public Enum asEnum() {
        return new Enum(this.code);
    }
}
