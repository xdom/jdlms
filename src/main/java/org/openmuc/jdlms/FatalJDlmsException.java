package org.openmuc.jdlms;

/**
 * This Exception denotes, that the a fatal exception has occurred. Connection has been closed.
 */
public class FatalJDlmsException extends JDlmsException {

    public FatalJDlmsException(ExceptionId exceptionId, Fault assumedFault, String message) {
        super(exceptionId, assumedFault, message);
    }

    public FatalJDlmsException(ExceptionId exceptionId, Fault assumedFault, String message, Throwable cause) {
        super(exceptionId, assumedFault, message, cause);
    }
}
