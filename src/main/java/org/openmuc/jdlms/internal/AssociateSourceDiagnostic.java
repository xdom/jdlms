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
