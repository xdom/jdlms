package org.openmuc.jdlms;

/**
 * This exception signals that a COSEM method has been accessed in a wrong way.
 * 
 * @see CosemMethod
 */
public class IllegalMethodAccessException extends DlmsAccessException {

    private final MethodResultCode methodResultCode;

    /**
     * Construct a new Exception.
     * 
     * @param methodResultCode
     *            the reason of the exception.
     */
    public IllegalMethodAccessException(MethodResultCode methodResultCode) {
        this.methodResultCode = methodResultCode;
    }

    /**
     * Get the access result code of the illegal method access.
     * 
     * @return the access result code.
     */
    public MethodResultCode getMethodResultCode() {
        return methodResultCode;
    }

}
