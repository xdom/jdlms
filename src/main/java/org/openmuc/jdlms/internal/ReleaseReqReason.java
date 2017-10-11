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

import java.util.Map;

import org.openmuc.jdlms.datatypes.DlmsEnumeration;

public enum ReleaseReqReason implements DlmsEnumeration {
    NORMAL(0),
    URGENT(1),
    USER_DEFINED(2),
    UNKNOWN(-1);

    private static final Map<Long, ReleaseReqReason> mapping;
    private long code;

    static {
        mapping = DlmsEnumFunctions.generateEnumMap(ReleaseReqReason.class);
    }

    private ReleaseReqReason(long code) {
        this.code = code;
    }

    public static ReleaseReqReason reasonFor(long code) {

        ReleaseReqReason releaseReqReason = mapping.get(code);

        if (releaseReqReason != null) {
            return releaseReqReason;
        }
        else {
            return UNKNOWN;
        }

    }

    @Override
    public long getCode() {
        return code;
    }
}
