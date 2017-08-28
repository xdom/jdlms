package org.openmuc.jdlms;

/**
 * Base exception for a illegal attribute of illegal method access exception.
 * 
 * @see DlmsInterceptor#intercept(DlmsInvocationContext)
 */
public abstract class DlmsAccessException extends Exception {

    protected DlmsAccessException() {
        super();
    }

    protected DlmsAccessException(Throwable cause) {
        super(cause);
    }
}
