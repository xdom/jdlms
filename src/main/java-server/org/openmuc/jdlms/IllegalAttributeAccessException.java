package org.openmuc.jdlms;

/**
 * This exception signals that a COSEM attribute has been accessed in a incorrect way.
 * 
 * @see CosemAttribute
 */
public class IllegalAttributeAccessException extends DlmsAccessException {
    private final AccessResultCode accessResultCode;

    /**
     * Construct a new Exception.
     * 
     * @param accessResultCode
     *            the reason of the exception.
     */
    public IllegalAttributeAccessException(AccessResultCode accessResultCode) {
        this.accessResultCode = accessResultCode;
    }

    /**
     * Construct a new Exception.
     * 
     * @param accessResultCode
     *            the reason of the exception.
     * @param cause
     *            the cause.
     */
    public IllegalAttributeAccessException(AccessResultCode accessResultCode, Throwable cause) {
        super(cause);

        this.accessResultCode = accessResultCode;
    }

    /**
     * Get the access result code of the illegal attribute access.
     * 
     * @return the access result code.
     */
    public AccessResultCode getAccessResultCode() {
        return accessResultCode;
    }
}
