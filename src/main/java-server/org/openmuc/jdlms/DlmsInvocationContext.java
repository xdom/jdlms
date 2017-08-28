package org.openmuc.jdlms;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import org.openmuc.jdlms.SecuritySuite.SecurityPolicy;
import org.openmuc.jdlms.datatypes.DataObject;

/**
 * Exposes context information about the intercepted invocation and operations that enable interceptor methods to
 * control the behavior of the invocation of an xDLMS action/ COSEM attribute or method access.
 * 
 * <pre>
 * 
 *  public DataObject intercept(DlmsInvocationContext ctx) throws DlmsAccessException {
 *     ctx.get..
 *     ...
 * 
 *     DataObject res = ctx.proceed();
 * 
 *     ...
 *     return res;
 *  }
 * </pre>
 * 
 * @see DlmsInterceptor
 */
public interface DlmsInvocationContext {
    /**
     * Get the target object of the xDLMS service invokation.
     * 
     * @return the target class.
     */
    CosemInterfaceObject getTarget();

    /**
     * Get the parameters of the method which is going to be invoked. This may be empty in the case of a GET invocation.
     * 
     * @return the parameters of the access.
     */
    Object[] getParameters();

    /**
     * Change the parameter of the invocation.
     * 
     * @param params
     *            the new parameters.
     * @throws IllegalArgumentException
     *             if the new params don't match the type of the original method/field.
     */
    void setParameters(Object[] params) throws IllegalArgumentException;

    /**
     * Get the COSEM resource descriptor.
     * 
     * @return the resource descriptor.
     */
    CosemResourceDescriptor getCosemResourceDescriptor();

    /**
     * The member either {@link Method} or {@link Field} which is being accessed.
     * 
     * <p>
     * <b>NOTE:</b> if {@linkplain #getXDlmsServiceType()} is {@linkplain XDlmsServiceType#ACTION} the member must be an
     * {@linkplain Method}.
     * </p>
     * 
     * @return the member.
     */
    Member getMember();

    /**
     * The type of invocation.
     * 
     * @return the xDLMS service type.
     */
    XDlmsServiceType getXDlmsServiceType();

    /**
     * The policy in which the client is invoking the xDLMS service.
     * 
     * @return the security policy.
     */
    SecurityPolicy getSecurityPolicy();

    /**
     * Invoke the actual COSEM resource. This may be null.
     * 
     * <p>
     * <b>NOTE:</b> The result of the invocation must be returned at
     * {@linkplain DlmsInterceptor#intercept(DlmsInvocationContext)}
     * </p>
     * 
     * @return the return value of the next method in the chain.
     * @throws DlmsAccessException
     *             if the call of the method throws a {@link DlmsAccessException}.
     */
    DataObject proceed() throws DlmsAccessException;

    /**
     * The invocation type (service).
     */
    enum XDlmsServiceType {
        /**
         * A COSEM attribute is being read.
         */
        GET(),
        /**
         * A COSEM attribute is being set.
         */
        SET(),
        /**
         * A COSEM method is being invoked.
         */
        ACTION();
    }
}
