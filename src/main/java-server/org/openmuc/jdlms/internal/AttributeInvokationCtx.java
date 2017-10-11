/**
 * Copyright 2012-17 Fraunhofer ISE
 *
 * This file is part of jDLMS.
 * For more information visit http://www.openmuc.org
 *
 * jDLMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jDLMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with jDLMS.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.openmuc.jdlms.internal;

import java.lang.reflect.Member;

import org.openmuc.jdlms.AccessResultCode;
import org.openmuc.jdlms.AttributeAddress;
import org.openmuc.jdlms.CosemAttribute;
import org.openmuc.jdlms.CosemInterfaceObject;
import org.openmuc.jdlms.CosemResourceDescriptor;
import org.openmuc.jdlms.DlmsAccessException;
import org.openmuc.jdlms.DlmsInterceptor;
import org.openmuc.jdlms.DlmsInvocationContext;
import org.openmuc.jdlms.IllegalAttributeAccessException;
import org.openmuc.jdlms.ObisCode;
import org.openmuc.jdlms.SecuritySuite.SecurityPolicy;
import org.openmuc.jdlms.datatypes.DataObject;
import org.openmuc.jdlms.datatypes.DataObject.Type;
import org.openmuc.jdlms.internal.DataDirectoryImpl.CosemClassInstance;

abstract class AttributeInvokationCtx extends AbstarctInvocationCtx {

    private final XDlmsServiceType invocationType;
    private final CosemResourceDescriptor cosemResourceDescriptor;
    private final Member member;

    public AttributeInvokationCtx(SecurityPolicy securityPolicy, XDlmsServiceType invocationType,
            CosemResourceDescriptor cosemResourceDescriptor, CosemInterfaceObject target, Member member,
            Type parameterType, Object... parameters) {
        super(target, parameters, securityPolicy, parameterType);

        this.invocationType = invocationType;
        this.cosemResourceDescriptor = cosemResourceDescriptor;
        this.member = member;
    }

    @Override
    public CosemResourceDescriptor getCosemResourceDescriptor() {
        return this.cosemResourceDescriptor;
    }

    @Override
    public XDlmsServiceType getXDlmsServiceType() {
        return this.invocationType;
    }

    @Override
    public Member getMember() {
        return this.member;
    }

    static DataObject saveCallInterceptIntercept(DlmsInterceptor interceptor, DlmsInvocationContext ctx)
            throws IllegalAttributeAccessException {
        try {
            return interceptor.intercept(ctx);
        } catch (DlmsAccessException e) {
            if (e instanceof IllegalAttributeAccessException) {
                throw (IllegalAttributeAccessException) e;
            }
            else {
                throw new IllegalAttributeAccessException(AccessResultCode.OTHER_REASON);
            }
        }
    }

    static CosemResourceDescriptor toAttributeDesctiptor(final CosemClassInstance cosemClassInstanc,
            CosemAttribute cosemAttribute, CosemInterfaceObject instance) {
        int classId = cosemClassInstanc.getCosemClass().id();
        ObisCode instanceId = instance.getInstanceId();
        byte attributeId = cosemAttribute.id();
        return new AttributeAddress(classId, instanceId, attributeId);
    }

}
