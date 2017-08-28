package org.openmuc.jdlms;

/**
 * This Exception denotes, that a non fatal exception has occurred. Connection may not be broken.
 */
public class NonFatalJDlmsException extends JDlmsException {

    public NonFatalJDlmsException(ExceptionId exceptionId, Fault assumedFault, String message) {
        super(exceptionId, assumedFault, message);
    }

    public NonFatalJDlmsException(ExceptionId exceptionId, Fault assumedFault, String message, Throwable cause) {
        super(exceptionId, assumedFault, message, cause);
    }
}
