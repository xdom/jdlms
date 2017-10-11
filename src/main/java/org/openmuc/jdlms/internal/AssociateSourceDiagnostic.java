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

public class AssociateSourceDiagnostic {

    public enum AcseServiceUser {
        NULL(0),
        NO_REASON_GIVEN(1),
        APPLICATION_CONTEXT_NAME_NOT_SUPPORTED(2),
        AUTHENTICATION_MECHANISM_NAME_NOT_RECOGNISED(11),
        AUTHENTICATION_MECHANISM_NAME_REQUIRED(12),
        AUTHENTICATION_FAILURE(13),
        AUTHENTICATION_REQUIRED(14);

        private static final AcseServiceUser[] VALUES = values();
        private int code;

        private AcseServiceUser(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static AcseServiceUser acseServiceUserFor(long value) throws FatalJDlmsException {
            for (AcseServiceUser element : VALUES) {
                if (value == element.getCode()) {
                    return element;
                }
            }
            throw new FatalJDlmsException(ExceptionId.UNKNOWN, Fault.SYSTEM,
                    MessageFormat.format("Unknown value {0}.", value));
        }
    }

    public enum AcseServiceProvider {
        NULL(0),
        NO_REASON_GIVEN(1),
        NO_COMMON_ACSE_VERSION(2);

        private static final AcseServiceProvider[] VALUES = values();
        private int code;

        private AcseServiceProvider(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static AcseServiceProvider acseServiceProviderFor(long value) throws FatalJDlmsException {
            for (AcseServiceProvider element : VALUES) {
                if (value == element.getCode()) {
                    return element;
                }
            }
            throw new FatalJDlmsException(ExceptionId.UNKNOWN, Fault.USER,
                    MessageFormat.format("Unknown value {0}.", value));
        }
    }

    /**
     * Don't let anyone instantiate this class.
     */
    private AssociateSourceDiagnostic() {
    }

}
