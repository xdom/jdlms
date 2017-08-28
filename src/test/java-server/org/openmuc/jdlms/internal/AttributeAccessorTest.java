package org.openmuc.jdlms.internal;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openmuc.jdlms.datatypes.DataObject.newOctetStringData;

import java.lang.reflect.Field;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmuc.jdlms.AccessResultCode;
import org.openmuc.jdlms.CosemAttribute;
import org.openmuc.jdlms.CosemClass;
import org.openmuc.jdlms.CosemInterfaceObject;
import org.openmuc.jdlms.DlmsAccessException;
import org.openmuc.jdlms.DlmsInterceptor;
import org.openmuc.jdlms.DlmsInvocationContext;
import org.openmuc.jdlms.IllegalAttributeAccessException;
import org.openmuc.jdlms.SecuritySuite.SecurityPolicy;
import org.openmuc.jdlms.SecurityUtils;
import org.openmuc.jdlms.SelectiveAccessDescription;
import org.openmuc.jdlms.datatypes.DataObject;
import org.openmuc.jdlms.internal.AttributeAccessor.FieldAccessor;
import org.openmuc.jdlms.internal.DataDirectoryImpl.CosemClassInstance;
import org.powermock.reflect.Whitebox;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

@RunWith(JUnitParamsRunner.class)
public class AttributeAccessorTest {

    @Test
    public void testGetFieldAccessor() throws Exception {

        DlmsInterceptor interceptor = spy(new DlmsInterceptor() {
            @Override
            public DataObject intercept(DlmsInvocationContext ctx) throws DlmsAccessException {
                return ctx.proceed();
            }
        });

        T1 t1 = new T1(interceptor);

        CosemClassInstance cosemClassInstance = setupCosemClassInstance(t1);

        SelectiveAccessDescription selectiveAccessDescription = null;
        Long connectionId = null;
        SecurityPolicy securityPolicy = SecurityPolicy.AUTHENTICATED;

        t1.f1 = DataObject.newOctetStringData(SecurityUtils.generateAES128Key());
        DataObject res = setupFieldAccessor("f1").get(cosemClassInstance, selectiveAccessDescription, connectionId,
                securityPolicy);

        verify(interceptor, only()).intercept(any(DlmsInvocationContext.class));

        assertEquals(t1.f1.getType(), res.getType());
        assertArrayEquals((byte[]) t1.f1.getValue(), (byte[]) res.getValue());
    }

    @Test
    public void testSet() throws Exception {

        DlmsInterceptor interceptor = spy(new DlmsInterceptor() {
            @Override
            public DataObject intercept(DlmsInvocationContext ctx) throws DlmsAccessException {
                return ctx.proceed();
            }
        });

        T1 t1 = new T1(interceptor);

        CosemClassInstance cosemClassInstance = setupCosemClassInstance(t1);

        SelectiveAccessDescription selectiveAccessDescription = null;
        Long connectionId = null;
        SecurityPolicy securityPolicy = SecurityPolicy.AUTHENTICATED;

        DataObject newVal = newOctetStringData(SecurityUtils.generateAES128Key());
        setupFieldAccessor("f1").set(newVal, cosemClassInstance, selectiveAccessDescription, connectionId,
                securityPolicy);

        verify(interceptor, only()).intercept(any(DlmsInvocationContext.class));

        assertEquals(newVal.getType(), t1.f1.getType());
        assertArrayEquals((byte[]) newVal.getValue(), (byte[]) t1.f1.getValue());
    }

    @Test
    @Parameters(method = "cuases")
    public void testFailInvocationGet(final AccessResultCode cause) throws Exception {
        DlmsInterceptor interceptor = new DlmsInterceptor() {
            @Override
            public DataObject intercept(DlmsInvocationContext ctx) throws DlmsAccessException {
                throw new IllegalAttributeAccessException(cause);
            }
        };

        T1 t1 = new T1(interceptor);

        CosemClassInstance cosemClassInstance = setupCosemClassInstance(t1);

        SelectiveAccessDescription selectiveAccessDescription = null;
        Long connectionId = null;
        SecurityPolicy securityPolicy = SecurityPolicy.AUTHENTICATED;

        try {
            setupFieldAccessor("f1").get(cosemClassInstance, selectiveAccessDescription, connectionId, securityPolicy);
            fail("Expected an exception-");
        } catch (IllegalAttributeAccessException e) {
            assertEquals(cause, e.getAccessResultCode());
        }
    }

    @Test
    @Parameters(method = "cuases")
    public void testFailInvocationSet(final AccessResultCode cause) throws Exception {
        DlmsInterceptor interceptor = new DlmsInterceptor() {
            @Override
            public DataObject intercept(DlmsInvocationContext ctx) throws DlmsAccessException {
                throw new IllegalAttributeAccessException(cause);
            }
        };

        T1 t1 = new T1(interceptor);

        CosemClassInstance cosemClassInstance = setupCosemClassInstance(t1);

        SelectiveAccessDescription selectiveAccessDescription = null;
        Long connectionId = null;
        SecurityPolicy securityPolicy = SecurityPolicy.AUTHENTICATED;

        try {
            setupFieldAccessor("f1").set(DataObject.newNullData(), cosemClassInstance, selectiveAccessDescription,
                    connectionId, securityPolicy);
            fail("Expected an exception-");
        } catch (IllegalAttributeAccessException e) {
            assertEquals(cause, e.getAccessResultCode());
        }
    }

    public Object cuases() {
        AccessResultCode[] possibleCodes = AccessResultCode.values();
        Object[][] objects = new Object[possibleCodes.length][1];

        for (int i = 0; i < objects.length; i++) {
            objects[i][0] = possibleCodes[i];
        }

        return objects;
    }

    private static CosemClassInstance setupCosemClassInstance(T1 t1) {
        CosemClassInstance cosemClassInstance = mock(CosemClassInstance.class);
        when(cosemClassInstance.getCosemClass()).thenReturn(T1.class.getAnnotation(CosemClass.class));
        when(cosemClassInstance.getInstance()).thenReturn(t1);
        return cosemClassInstance;
    }

    private static FieldAccessor setupFieldAccessor(String filedName) {
        Field field = Whitebox.getField(T1.class, filedName);
        return new FieldAccessor(field, field.getAnnotation(CosemAttribute.class));
    }

    @CosemClass(id = 99)
    private class T1 extends CosemInterfaceObject {

        @CosemAttribute(id = 2)
        private DataObject f1;

        public T1(DlmsInterceptor interceptor) {
            super("0.0.0.0.0.0", interceptor);
        }

    }

}
