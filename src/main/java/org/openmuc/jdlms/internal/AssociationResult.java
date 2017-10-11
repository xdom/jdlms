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

import org.openmuc.jdlms.FatalJDlmsException;
import org.openmuc.jdlms.JDlmsException.ExceptionId;
import org.openmuc.jdlms.JDlmsException.Fault;

public enum AssociationResult {
    ACCEPTED(0),
    REJECTED_PERMANENT(1),
    REJECTED_TRANSIENT(2);

    private static final AssociationResult[] VALUES = values();
    private long value;

    private AssociationResult(int value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    public static AssociationResult associationResultFor(long value) throws FatalJDlmsException {
        for (AssociationResult associationResult : VALUES) {
            if (associationResult.value == value) {
                return associationResult;
            }
        }

        String msg = MessageFormat.format(
                "The Server answered an association result {0} which is unknown/incompatible by/with the jDLMS stack.",
                value);
        throw new FatalJDlmsException(ExceptionId.UNKNOWN_ASSOCIATION_RESULT, Fault.SYSTEM, msg);
    }
}
