package org.openmuc.jdlms;

/**
 * Signals that a timeout has occurred on a {@link DlmsConnection} get, set, or action request. Connection my still be
 * active.
 */
public class ResponseTimeoutException extends NonFatalJDlmsException {

    public ResponseTimeoutException(String message) {
        super(ExceptionId.RESPONSE_TIMEOUT, Fault.SYSTEM, message);
    }

}
