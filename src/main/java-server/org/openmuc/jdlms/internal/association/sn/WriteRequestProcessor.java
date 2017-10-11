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
package org.openmuc.jdlms.internal.association.sn;

import static org.openmuc.jdlms.internal.DlmsEnumFunctions.enumToAxdrEnum;

import java.io.IOException;
import java.util.Iterator;

import org.openmuc.jdlms.AccessResultCode;
import org.openmuc.jdlms.AttributeAddress;
import org.openmuc.jdlms.IllegalAttributeAccessException;
import org.openmuc.jdlms.IllegalMethodAccessException;
import org.openmuc.jdlms.MethodParameter;
import org.openmuc.jdlms.ObisCode;
import org.openmuc.jdlms.SetParameter;
import org.openmuc.jdlms.datatypes.DataObject;
import org.openmuc.jdlms.internal.APdu;
import org.openmuc.jdlms.internal.BaseNameRange;
import org.openmuc.jdlms.internal.BaseNameRange.Access;
import org.openmuc.jdlms.internal.DataConverter;
import org.openmuc.jdlms.internal.DataDirectoryImpl.CosemClassInstance;
import org.openmuc.jdlms.internal.DlmsEnumFunctions;
import org.openmuc.jdlms.internal.asn1.axdr.types.AxdrNull;
import org.openmuc.jdlms.internal.asn1.cosem.COSEMpdu;
import org.openmuc.jdlms.internal.asn1.cosem.Data;
import org.openmuc.jdlms.internal.asn1.cosem.Variable_Access_Specification;
import org.openmuc.jdlms.internal.asn1.cosem.WriteRequest;
import org.openmuc.jdlms.internal.asn1.cosem.WriteResponse;
import org.openmuc.jdlms.internal.asn1.cosem.WriteResponse.SubChoice;
import org.openmuc.jdlms.internal.association.AssociationMessenger;
import org.openmuc.jdlms.internal.association.RequestProcessorData;

public class WriteRequestProcessor extends SnRequestProcessorBase {

    public WriteRequestProcessor(AssociationMessenger associationMessenger, RequestProcessorData requestProcessorData) {
        super(associationMessenger, requestProcessorData);
    }

    @Override
    public void processRequest(COSEMpdu request) throws IOException {
        WriteRequest writeRequest = request.writeRequest;

        // sized should be the same
        Iterator<Variable_Access_Specification> varAccessSpecIter = writeRequest.variable_access_specification.list()
                .iterator();
        Iterator<Data> dataIter = writeRequest.list_of_data.list().iterator();

        WriteResponse writeResponse = new WriteResponse();
        while (varAccessSpecIter.hasNext() && dataIter.hasNext()) {
            Variable_Access_Specification vas = varAccessSpecIter.next();
            Data data = dataIter.next();

            switch (vas.getChoiceIndex()) {
            case VARIABLE_NAME:
                writeResponse.add(varNameReq(vas, data));
                break;
            case PARAMETERIZED_ACCESS:
                // TODO
                break;
            case WRITE_DATA_BLOCK_ACCESS:
                // also kind of illegal at this point.
                break;

            case BLOCK_NUMBER_ACCESS:
            case READ_DATA_BLOCK_ACCESS:
            case _ERR_NONE_SELECTED:
            default:
                // TODO illegal
                break;
            }

        }

        APdu aPdu = newAPdu();
        aPdu.getCosemPdu().setwriteResponse(writeResponse);

        this.associationMessenger.encodeAndSend(aPdu);

    }

    private SubChoice varNameReq(Variable_Access_Specification vas, Data data) throws IOException {
        int varName = (int) vas.variable_name.getValue() & 0xFFFF;
        BaseNameRange intersectingRange = this.nameRangeSet.getIntersectingRange(varName);

        Access access;
        try {
            access = accessFor(varName, intersectingRange);
        } catch (IllegalAttributeAccessException e) {
            SubChoice subChoice = new SubChoice();
            subChoice.setdata_access_error(enumToAxdrEnum(e.getAccessResultCode()));
            return subChoice;
        }

        CosemClassInstance classInstance = intersectingRange.getClassInstance();

        int classId = classInstance.getCosemClass().id();
        ObisCode instanceId = classInstance.getInstance().getInstanceId();
        int memberId = access.getMemberId();

        switch (access.getAccessType()) {
        case METHOD:
            return methodAction(memberId, classId, instanceId, data);

        case ATTRIBUTE:
        default:
            return variableSet(classId, instanceId, memberId, data);

        }
    }

    private SubChoice methodAction(int memberId, int classId, ObisCode instanceId, Data data) {
        DataObject dataObject;
        if (data != null) {
            dataObject = DataConverter.convertDataToDataObject(data);
        }
        else {
            dataObject = DataObject.newNullData();
        }

        MethodParameter methodParameter = new MethodParameter(classId, instanceId, memberId, dataObject);

        SubChoice res = new SubChoice();
        try {

            this.requestProcessorData.directory.invokeMethod(logicalDeviceId(), methodParameter, connectionId());
            res.setsuccess(new AxdrNull());

        } catch (IllegalMethodAccessException e) {
            res.setdata_access_error(enumToAxdrEnum(e.getMethodResultCode()));
        }
        return res;
    }

    private SubChoice variableSet(int classId, ObisCode instanceId, int memberId, Data data) {
        AttributeAddress attributeAddress = new AttributeAddress(classId, instanceId, memberId);

        SetParameter setParameter = new SetParameter(attributeAddress, DataConverter.convertDataToDataObject(data));

        AccessResultCode resultCode = this.requestProcessorData.directory.set(logicalDeviceId(), setParameter,
                connectionId());

        SubChoice res = new SubChoice();
        if (resultCode != AccessResultCode.SUCCESS) {
            res.setdata_access_error(DlmsEnumFunctions.enumToAxdrEnum(resultCode));
        }
        else {
            res.setsuccess(new AxdrNull());
        }

        return res;
    }

}
