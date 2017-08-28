package org.openmuc.jdlms.internal;

import static org.openmuc.jdlms.AccessResultCode.SCOPE_OF_ACCESS_VIOLATED;
import static org.openmuc.jdlms.datatypes.DataObject.newNullData;
import static org.openmuc.jdlms.internal.AttributeInvokationCtx.saveCallInterceptIntercept;
import static org.openmuc.jdlms.internal.AttributeInvokationCtx.toAttributeDesctiptor;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import org.openmuc.jdlms.AccessResultCode;
import org.openmuc.jdlms.CosemAttribute;
import org.openmuc.jdlms.CosemInterfaceObject;
import org.openmuc.jdlms.CosemResourceDescriptor;
import org.openmuc.jdlms.DlmsAccessException;
import org.openmuc.jdlms.DlmsInterceptor;
import org.openmuc.jdlms.DlmsInvocationContext;
import org.openmuc.jdlms.DlmsInvocationContext.XDlmsServiceType;
import org.openmuc.jdlms.IllegalAttributeAccessException;
import org.openmuc.jdlms.ObisCode;
import org.openmuc.jdlms.SecuritySuite.SecurityPolicy;
import org.openmuc.jdlms.SelectiveAccessDescription;
import org.openmuc.jdlms.datatypes.DataObject;
import org.openmuc.jdlms.internal.DataDirectoryImpl.CosemClassInstance;

public interface AttributeAccessor {

    DataObject get(CosemClassInstance cosemClassInstance, SelectiveAccessDescription selectiveAccessDescription,
            Long connectionId, SecurityPolicy securityPolicy) throws IllegalAttributeAccessException;

    void set(DataObject newVal, CosemClassInstance dlmsClassInstance,
            SelectiveAccessDescription selectiveAccessDescription, Long connectionId, SecurityPolicy securityPolicy)
            throws IllegalAttributeAccessException;

    static class LogicalNameFakeAccessor implements AttributeAccessor {

        private ObisCode instanceId;

        public LogicalNameFakeAccessor(ObisCode instanceId) {
            this.instanceId = instanceId;
        }

        @Override
        public DataObject get(CosemClassInstance cosemClassInstance,
                SelectiveAccessDescription selectiveAccessDescription, Long connectionId, SecurityPolicy securityPolicy)
                throws IllegalAttributeAccessException {
            return DataObject.newOctetStringData(this.instanceId.bytes());
        }

        @Override
        public void set(DataObject newVal, CosemClassInstance dlmsClassInstanc,
                SelectiveAccessDescription selectiveAccessDescription, Long connectionId, SecurityPolicy securityPolicy)
                throws IllegalAttributeAccessException {
            throw new IllegalAttributeAccessException(AccessResultCode.READ_WRITE_DENIED);
        }

    }

    static class FieldAccessor implements AttributeAccessor {

        private final Field field;
        private final CosemAttribute cosemAttribute;

        public FieldAccessor(Field field, CosemAttribute cosemAttribute) {
            this.field = field;
            this.cosemAttribute = cosemAttribute;
        }

        @Override
        public DataObject get(final CosemClassInstance cosemClassInstance,
                SelectiveAccessDescription selectiveAccessDescription, Long connectionId, SecurityPolicy securityPolicy)
                throws IllegalAttributeAccessException {
            CosemInterfaceObject instance = cosemClassInstance.getInstance();
            DlmsInterceptor interceptor = instance.getInterceptor();

            DataObject result;
            if (interceptor != null) {
                result = callGetViaInterceptor(cosemClassInstance, securityPolicy, instance, interceptor);
            }
            else {
                result = saveGet(instance);
            }

            return result == null ? newNullData() : result;
        }

        private DataObject callGetViaInterceptor(final CosemClassInstance cosemClassInstance,
                SecurityPolicy securityPolicy, CosemInterfaceObject instance, DlmsInterceptor interceptor)
                throws IllegalAttributeAccessException {
            CosemResourceDescriptor attributeDesc = toAttributeDesctiptor(cosemClassInstance, this.cosemAttribute,
                    instance);

            DlmsInvocationContext ctx = new AttributeInvokationCtx(securityPolicy, XDlmsServiceType.GET, attributeDesc,
                    instance, this.field, this.cosemAttribute.type()) {

                @Override
                public DataObject proceed() throws DlmsAccessException {
                    return saveGet(getTarget());
                }
            };
            return saveCallInterceptIntercept(interceptor, ctx);
        }

        private DataObject saveGet(CosemInterfaceObject instance) throws IllegalAttributeAccessException {
            try {
                return (DataObject) this.field.get(instance);
            } catch (IllegalAccessException e) {
                throw new IllegalAttributeAccessException(AccessResultCode.OTHER_REASON, e);
            }
        }

        @Override
        public void set(final DataObject newVal, final CosemClassInstance cosemClassInstanc,
                SelectiveAccessDescription selectiveAccessDescription, Long connectionId, SecurityPolicy securityPolicy)
                throws IllegalAttributeAccessException {
            CosemInterfaceObject instance = cosemClassInstanc.getInstance();
            DlmsInterceptor interceptor = instance.getInterceptor();
            if (interceptor != null) {
                CosemResourceDescriptor attributeDesc = toAttributeDesctiptor(cosemClassInstanc, this.cosemAttribute,
                        instance);

                DlmsInvocationContext ctx = new AttributeInvokationCtx(securityPolicy, XDlmsServiceType.SET,
                        attributeDesc, instance, field, this.cosemAttribute.type(), newVal) {

                    @Override
                    public DataObject proceed() throws DlmsAccessException {
                        saveSet((DataObject) (getParameters()[0]), getTarget());
                        return null;
                    }

                };

                saveCallInterceptIntercept(interceptor, ctx);
            }
            else {
                saveSet(newVal, instance);
            }

        }

        private void saveSet(DataObject newVal, CosemInterfaceObject instance) throws IllegalAttributeAccessException {
            try {
                this.field.set(instance, newVal);
            } catch (IllegalAccessException e) {
                throw new IllegalAttributeAccessException(AccessResultCode.OTHER_REASON, e);
            }
        }
    }

    static class MethodSetFieldGetAccessor implements AttributeAccessor {

        private final FieldAccessor fieldAccessor;
        private final MethodAttributeAccessor methodAccessor;

        public MethodSetFieldGetAccessor(Field field, Method setMethod, CosemAttribute cosemAttribute,
                Set<Integer> accessSelectors) {
            this.fieldAccessor = new FieldAccessor(field, cosemAttribute);
            this.methodAccessor = new MethodAttributeAccessor(null, setMethod, cosemAttribute, accessSelectors);
        }

        @Override
        public DataObject get(CosemClassInstance cosemClassInstance,
                SelectiveAccessDescription selectiveAccessDescription, Long connectionId, SecurityPolicy securityPolicy)
                throws IllegalAttributeAccessException {
            return this.fieldAccessor.get(cosemClassInstance, selectiveAccessDescription, connectionId, securityPolicy);
        }

        @Override
        public void set(DataObject newVal, CosemClassInstance dlmsClassInstance,
                SelectiveAccessDescription selectiveAccessDescription, Long connectionId, SecurityPolicy securityPolicy)
                throws IllegalAttributeAccessException {
            this.methodAccessor.set(newVal, dlmsClassInstance, selectiveAccessDescription, connectionId,
                    securityPolicy);
        }

    }

    static class FieldSetMethodGetAccessor implements AttributeAccessor {

        private final FieldAccessor fieldAccessor;
        private final MethodAttributeAccessor methodAccessor;

        public FieldSetMethodGetAccessor(Field field, Method getMethod, CosemAttribute cosemAttribute,
                Set<Integer> accessSelectors) {
            this.fieldAccessor = new FieldAccessor(field, cosemAttribute);
            this.methodAccessor = new MethodAttributeAccessor(getMethod, null, cosemAttribute, accessSelectors);
        }

        @Override
        public DataObject get(CosemClassInstance cosemClassInstance,
                SelectiveAccessDescription selectiveAccessDescription, Long connectionId, SecurityPolicy securityPolicy)
                throws IllegalAttributeAccessException {
            return this.methodAccessor.get(cosemClassInstance, selectiveAccessDescription, connectionId,
                    securityPolicy);
        }

        @Override
        public void set(DataObject newVal, CosemClassInstance cosemClassInstanc,
                SelectiveAccessDescription selectiveAccessDescription, Long connectionId, SecurityPolicy securityPolicy)
                throws IllegalAttributeAccessException {
            this.fieldAccessor.set(newVal, cosemClassInstanc, selectiveAccessDescription, connectionId, securityPolicy);
        }

    }

    public static class MethodAttributeAccessor implements AttributeAccessor {

        private final Method getMethod;
        private final Method setMethod;
        private final Set<Integer> accessSelectors;
        private final boolean containsGetId;
        private final boolean containsSetId;
        private final CosemAttribute cosemAttribute;

        public MethodAttributeAccessor(Method getMethod, Method setMethod, CosemAttribute cosemAttribute,
                Set<Integer> accessSelectors) {
            this.getMethod = getMethod;
            this.setMethod = setMethod;

            this.cosemAttribute = cosemAttribute;

            this.accessSelectors = accessSelectors;

            this.containsGetId = methodHasConnectionIdParam(getMethod);
            this.containsSetId = methodHasConnectionIdParam(setMethod);
        }

        private boolean methodHasConnectionIdParam(Method method) {
            if (method == null) {
                return false;
            }

            Class<?>[] parameterTypes = method.getParameterTypes();

            if (parameterTypes.length == 0) {
                return false;
            }

            int lastIndex = parameterTypes.length - 1;

            return Long.class.isAssignableFrom(parameterTypes[lastIndex]);
        }

        @Override
        public DataObject get(CosemClassInstance classInstance, SelectiveAccessDescription selectiveAccessDescription,
                Long connectionId, SecurityPolicy securityPolicy) throws IllegalAttributeAccessException {
            Object[] parameter = buildGetParameter(selectiveAccessDescription, connectionId);
            CosemInterfaceObject instance = classInstance.getInstance();
            DataObject result;

            DlmsInterceptor interceptor = instance.getInterceptor();
            if (interceptor != null) {
                CosemResourceDescriptor address = toAttributeDesctiptor(classInstance, this.cosemAttribute, instance);
                DlmsInvocationContext ctx = new AttributeInvokationCtx(securityPolicy, XDlmsServiceType.GET, address,
                        instance, this.getMethod, this.cosemAttribute.type(), parameter) {
                    @Override
                    public DataObject proceed() throws DlmsAccessException {
                        return saveGet(getParameters(), getTarget());
                    }
                };
                result = saveCallInterceptIntercept(interceptor, ctx);
            }
            else {
                result = saveGet(parameter, instance);
            }

            if (result == null) {
                return DataObject.newNullData();
            }

            return result;
        }

        private DataObject saveGet(Object[] parameter, CosemInterfaceObject instance)
                throws IllegalAttributeAccessException {
            try {
                return (DataObject) this.getMethod.invoke(instance, parameter);

            } catch (IllegalAccessException e) {
                throw new IllegalAttributeAccessException(AccessResultCode.OTHER_REASON);
            } catch (InvocationTargetException e) {
                throw convert(e);
            }
        }

        private Object[] buildGetParameter(SelectiveAccessDescription selectiveAccessDescription, Long connectionId)
                throws IllegalAttributeAccessException {
            Object[] parameter = new Object[this.getMethod.getParameterTypes().length];
            setSelectiveAcccessDescription(selectiveAccessDescription, parameter, 0);

            if (this.containsGetId) {
                setConnectionId(connectionId, parameter);
            }

            return parameter;
        }

        private void setConnectionId(Long connectionId, Object[] parameter) {
            parameter[parameter.length - 1] = connectionId;
        }

        @Override
        public void set(DataObject newVal, CosemClassInstance classInstance,
                SelectiveAccessDescription selectiveAccessDescription, Long connectionId, SecurityPolicy securityPolicy)
                throws IllegalAttributeAccessException {
            Object[] parameter = buildSetParameter(newVal, selectiveAccessDescription, connectionId);

            CosemInterfaceObject instance = classInstance.getInstance();

            DlmsInterceptor interceptor = instance.getInterceptor();
            if (interceptor != null) {
                DlmsInvocationContext ctx = new AttributeInvokationCtx(securityPolicy, XDlmsServiceType.SET, null,
                        instance, this.setMethod, this.cosemAttribute.type(), parameter) {

                    @Override
                    public DataObject proceed() throws DlmsAccessException {
                        saveSet(getTarget(), getParameters());
                        return null;
                    }
                };
                saveCallInterceptIntercept(interceptor, ctx);
            }
            else {
                saveSet(instance, parameter);
            }

        }

        private void saveSet(CosemInterfaceObject instance, Object[] parameter) throws IllegalAttributeAccessException {
            try {
                this.setMethod.invoke(instance, parameter);
            } catch (IllegalAccessException e) {
                throw new IllegalAttributeAccessException(AccessResultCode.OTHER_REASON);
            } catch (InvocationTargetException e) {
                throw convert(e);
            }
        }

        private Object[] buildSetParameter(DataObject newVal, SelectiveAccessDescription selectiveAccessDescription,
                Long connectionId) throws IllegalAttributeAccessException {
            Object[] parameter = new Object[this.setMethod.getParameterTypes().length];

            parameter[0] = newVal;
            setSelectiveAcccessDescription(selectiveAccessDescription, parameter, 1);
            if (this.containsSetId) {
                setConnectionId(connectionId, parameter);
            }
            return parameter;
        }

        private void setSelectiveAcccessDescription(SelectiveAccessDescription selectiveAccessDescription,
                Object[] parameter, int indexOfAccess) throws IllegalAttributeAccessException {

            if (selectiveAccessDescription == null) {
                return;
            }

            if (this.accessSelectors.isEmpty()) {
                throw new IllegalAttributeAccessException(SCOPE_OF_ACCESS_VIOLATED);
            }

            parameter[indexOfAccess] = selectiveAccessDescription;
        }

        private IllegalAttributeAccessException convert(InvocationTargetException e)
                throws IllegalAttributeAccessException {
            Throwable targetException = e.getTargetException();
            if (!(targetException instanceof IllegalAttributeAccessException)) {
                return new IllegalAttributeAccessException(AccessResultCode.OTHER_REASON);
            }
            return (IllegalAttributeAccessException) targetException;
        }
    }

}
