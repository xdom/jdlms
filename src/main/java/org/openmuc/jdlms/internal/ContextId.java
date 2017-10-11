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

import org.openmuc.jdlms.settings.client.ReferencingMethod;

public enum ContextId {
    LOGICAL_NAME_REFERENCING_NO_CIPHERING(1),
    SHORT_NAME_REFERENCING_NO_CIPHERING(2),
    LOGICAL_NAME_REFERENCING_WITH_CIPHERING(3),
    SHORT_NAME_REFERENCING_WITH_CIPHERING(4);

    private static final ContextId[] VALUES = values();
    private int code;

    private ContextId(int code) {
        this.code = code;
    }

    public static ContextId contextIdFor(int code) {
        for (ContextId value : VALUES) {
            if (value.code == code) {
                return value;
            }
        }
        throw new IllegalArgumentException(MessageFormat.format("Unknown code {0}.", code));
    }

    public int getCode() {
        return code;
    }

    public ReferencingMethod getReferencingMethod() {
        if (this == SHORT_NAME_REFERENCING_WITH_CIPHERING || this == ContextId.SHORT_NAME_REFERENCING_NO_CIPHERING) {
            return ReferencingMethod.SHORT;
        }
        else {
            return ReferencingMethod.LOGICAL;
        }
    }

    public boolean isCiphered() {
        return this == LOGICAL_NAME_REFERENCING_WITH_CIPHERING
                || this == ContextId.SHORT_NAME_REFERENCING_WITH_CIPHERING;
    }
}
