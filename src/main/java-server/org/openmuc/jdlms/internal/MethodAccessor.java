package org.openmuc.jdlms.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import org.openmuc.jdlms.CosemInterfaceObject;
import org.openmuc.jdlms.CosemMethod;
import org.openmuc.jdlms.CosemResourceDescriptor;
import org.openmuc.jdlms.DlmsAccessException;
import org.openmuc.jdlms.DlmsInterceptor;
import org.openmuc.jdlms.DlmsInvocationContext;
import org.openmuc.jdlms.IllegalMethodAccessException;
import org.openmuc.jdlms.MethodParameter;
import org.openmuc.jdlms.MethodResultCode;
import org.openmuc.jdlms.SecuritySuite.SecurityPolicy;
import org.openmuc.jdlms.datatypes.DataObject;
import org.openmuc.jdlms.datatypes.DataObject.Type;
import org.openmuc.jdlms.internal.DataDirectoryImpl.CosemClassInstance;

public class MethodAccessor {

    private final Method method;
    private final CosemMethod cosemMethod;
    private final Type parameterType;
    private final Type returnType;
    private final int numOfParams;

    public MethodAccessor(Method method, CosemMethod cosemMethod, Type parameterType, Type returnType) {
        this.method = method;
        this.cosemMethod = cosemMethod;
        this.parameterType = parameterType;
        this.returnType = returnType;
        this.numOfParams = this.method.getParameterTypes().length;
    }

    public CosemMethod getCosemMethod() {
        return cosemMethod;
    }

    public DataObject invoke(final CosemClassInstance dlmsClassInstance, DataObject argument, Long connectionId,
            final SecurityPolicy securityPolicy) throws IllegalMethodAccessException {

        final Object[] parameters = buildMethodArguments(argument, connectionId);

        CosemInterfaceObject instance = dlmsClassInstance.getInstance();
        DlmsInterceptor interceptor = instance.getInterceptor();

        DataObject result;
        if (interceptor != null) {
            result = invokeInterceptor(dlmsClassInstance, securityPolicy, parameters, instance, interceptor);
        }
        else {
            result = saveInvoke(instance, parameters);
        }

        if (result == null && this.returnType != null) {
            result = DataObject.newNullData();
        }

        return result;
    }

    private DataObject invokeInterceptor(final CosemClassInstance dlmsClassInstance,
            final SecurityPolicy securityPolicy, final Object[] parameters, CosemInterfaceObject instance,
            DlmsInterceptor interceptor) throws IllegalMethodAccessException {
        try {
            DlmsInvocationContext ctx = new MethodInvocationCtx(parameters, instance, securityPolicy) {
                @Override
                public CosemResourceDescriptor getCosemResourceDescriptor() {
                    return new MethodParameter(dlmsClassInstance.getCosemClass().id(), getTarget().getInstanceId(),
                            cosemMethod.id());
                }

                @Override
                public DataObject proceed() throws DlmsAccessException {
                    return saveInvoke(getTarget(), getParameters());
                }

            };

            return interceptor.intercept(ctx);

        } catch (DlmsAccessException e) {
            if (e instanceof IllegalMethodAccessException) {
                throw (IllegalMethodAccessException) e;
            }
            else {
                throw new IllegalMethodAccessException(MethodResultCode.OTHER_REASON);
            }
        }

    }

    private DataObject saveInvoke(final CosemInterfaceObject instance, final Object[] methodArguments)
            throws IllegalMethodAccessException {
        try {
            return (DataObject) this.method.invoke(instance, methodArguments);
        } catch (IllegalAccessException | IllegalArgumentException e) {
            throw new IllegalMethodAccessException(MethodResultCode.OTHER_REASON);
        } catch (InvocationTargetException e) {
            if (!(e.getTargetException() instanceof IllegalMethodAccessException)) {
                throw new IllegalMethodAccessException(MethodResultCode.OTHER_REASON);
            }
            throw (IllegalMethodAccessException) e.getTargetException();
        }
    }

    private Object[] buildMethodArguments(DataObject argument, Long connectionId) throws IllegalMethodAccessException {
        Object[] methodArguments = new Object[numOfParams];

        if ((argument == null || argument.isNull()) && this.parameterType == null) {
            if (this.numOfParams == 1) {
                methodArguments[0] = connectionId;
            }
        }
        else if (argument != null
                && (argument.getType() == this.parameterType || this.parameterType == Type.DONT_CARE)) {
            methodArguments[0] = argument;

            if (this.numOfParams == 2) {
                methodArguments[1] = connectionId;
            }
        }
        else {
            throw new IllegalMethodAccessException(MethodResultCode.TYPE_UNMATCHED);
        }
        return methodArguments;
    }

    private abstract class MethodInvocationCtx extends AbstarctInvocationCtx {

        public MethodInvocationCtx(Object[] parameters, CosemInterfaceObject target, SecurityPolicy securityPolicy) {
            super(target, parameters, securityPolicy, MethodAccessor.this.parameterType);
        }

        @Override
        public Member getMember() {
            return MethodAccessor.this.method;
        }

        @Override
        public XDlmsServiceType getXDlmsServiceType() {
            return XDlmsServiceType.ACTION;
        }

    }

}
