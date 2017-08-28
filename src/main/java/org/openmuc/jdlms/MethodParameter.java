/*
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
 *
 */
package org.openmuc.jdlms;

import org.openmuc.jdlms.datatypes.DataObject;
import org.openmuc.jdlms.interfaceclass.method.MethodClass;
import org.openmuc.jdlms.internal.asn1.cosem.Cosem_Method_Descriptor;
import org.openmuc.jdlms.internal.asn1.cosem.Cosem_Object_Instance_Id;
import org.openmuc.jdlms.internal.asn1.cosem.Integer8;
import org.openmuc.jdlms.internal.asn1.cosem.Unsigned16;

/**
 * Collection of data needed for a single remote ACTION call
 */
public final class MethodParameter extends CosemResourceDescriptor {
    /**
     * Parameter transmitted to be used by the method. May be null if not needed. (Method without parameter)
     */
    private final DataObject methodParameter;

    /**
     * Creates an action parameter for that particular method with no data container
     * 
     * @param classId
     *            Class of the object to change
     * @param instanceId
     *            Identifier of the remote object to change
     * @param methodId
     *            Method of the object that shall be called
     */
    public MethodParameter(int classId, ObisCode instanceId, int methodId) {
        this(classId, instanceId, methodId, DataObject.newNullData());
    }

    public MethodParameter(int classId, String instanceId, int methodId) {
        this(classId, new ObisCode(instanceId), methodId);
    }

    /**
     * Creates an action parameter for that particular method with a copy of the given data container
     * 
     * @param classId
     *            Class of the object to change
     * @param instanceId
     *            Identifier of the remote object to change
     * @param methodId
     *            Method of the object that is to change
     * @param methodParameter
     *            Container of this parameter
     */
    public MethodParameter(int classId, ObisCode instanceId, int methodId, DataObject methodParameter) {
        super(classId, instanceId, methodId);
        this.methodParameter = methodParameter;
    }

    public MethodParameter(int classId, String instanceId, int methodId, DataObject methodParameter) {
        this(classId, new ObisCode(instanceId), methodId, methodParameter);
    }

    public MethodParameter(MethodClass methodClass, String instanceId, DataObject methodParameter) {
        this(methodClass, new ObisCode(instanceId), methodParameter);
    }

    public MethodParameter(MethodClass methodClass, ObisCode instanceId, DataObject methodParameter) {
        this(methodClass.getInterfaceClass().id(), instanceId, methodClass.getMethodId(), methodParameter);
    }

    public DataObject getParameter() {
        return methodParameter;
    }

    @Override
    Cosem_Method_Descriptor toDescriptor() {
        return new Cosem_Method_Descriptor(new Unsigned16(getClassId()),
                new Cosem_Object_Instance_Id(getInstanceId().bytes()), new Integer8(getId()));
    }
}
