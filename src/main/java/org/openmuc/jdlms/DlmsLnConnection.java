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

import static java.util.Collections.emptyList;
import static org.openmuc.jdlms.ConformanceSetting.ACTION;
import static org.openmuc.jdlms.ConformanceSetting.ATTRIBUTE0_SUPPORTED_WITH_GET;
import static org.openmuc.jdlms.ConformanceSetting.ATTRIBUTE0_SUPPORTED_WITH_SET;
import static org.openmuc.jdlms.ConformanceSetting.BLOCK_TRANSFER_WITH_ACTION;
import static org.openmuc.jdlms.ConformanceSetting.BLOCK_TRANSFER_WITH_GET_OR_READ;
import static org.openmuc.jdlms.ConformanceSetting.BLOCK_TRANSFER_WITH_SET_OR_WRITE;
import static org.openmuc.jdlms.ConformanceSetting.GET;
import static org.openmuc.jdlms.ConformanceSetting.MULTIPLE_REFERENCES;
import static org.openmuc.jdlms.ConformanceSetting.PRIORITY_MGMT_SUPPORTED;
import static org.openmuc.jdlms.ConformanceSetting.SELECTIVE_ACCESS;
import static org.openmuc.jdlms.ConformanceSetting.SET;
import static org.openmuc.jdlms.internal.DataConverter.convertDataObjectToData;
import static org.openmuc.jdlms.internal.DlmsEnumFunctions.enumValueFrom;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.openmuc.jdlms.JDlmsException.ExceptionId;
import org.openmuc.jdlms.JDlmsException.Fault;
import org.openmuc.jdlms.SecuritySuite.EncryptionMechanism;
import org.openmuc.jdlms.datatypes.DataObject;
import org.openmuc.jdlms.interfaceclass.method.AssociationLnMethod;
import org.openmuc.jdlms.internal.ContextId;
import org.openmuc.jdlms.internal.DataConverter;
import org.openmuc.jdlms.internal.WellKnownInstanceIds;
import org.openmuc.jdlms.internal.asn1.axdr.AxdrType;
import org.openmuc.jdlms.internal.asn1.axdr.NullOutputStream;
import org.openmuc.jdlms.internal.asn1.axdr.types.AxdrEnum;
import org.openmuc.jdlms.internal.asn1.axdr.types.AxdrOptional;
import org.openmuc.jdlms.internal.asn1.cosem.ACTION_Request;
import org.openmuc.jdlms.internal.asn1.cosem.ACTION_Response;
import org.openmuc.jdlms.internal.asn1.cosem.Action_Request_Next_Pblock;
import org.openmuc.jdlms.internal.asn1.cosem.Action_Request_Normal;
import org.openmuc.jdlms.internal.asn1.cosem.Action_Request_With_List;
import org.openmuc.jdlms.internal.asn1.cosem.Action_Response_With_List.SubSeqOf_list_of_responses;
import org.openmuc.jdlms.internal.asn1.cosem.Action_Response_With_Optional_Data;
import org.openmuc.jdlms.internal.asn1.cosem.COSEMpdu;
import org.openmuc.jdlms.internal.asn1.cosem.Cosem_Attribute_Descriptor;
import org.openmuc.jdlms.internal.asn1.cosem.Cosem_Attribute_Descriptor_With_Selection;
import org.openmuc.jdlms.internal.asn1.cosem.Data;
import org.openmuc.jdlms.internal.asn1.cosem.GET_Request;
import org.openmuc.jdlms.internal.asn1.cosem.GET_Response;
import org.openmuc.jdlms.internal.asn1.cosem.Get_Data_Result;
import org.openmuc.jdlms.internal.asn1.cosem.Get_Request_Next;
import org.openmuc.jdlms.internal.asn1.cosem.Get_Request_Normal;
import org.openmuc.jdlms.internal.asn1.cosem.Get_Request_With_List;
import org.openmuc.jdlms.internal.asn1.cosem.Get_Response_With_List.SubSeqOf_result;
import org.openmuc.jdlms.internal.asn1.cosem.Invoke_Id_And_Priority;
import org.openmuc.jdlms.internal.asn1.cosem.SET_Request;
import org.openmuc.jdlms.internal.asn1.cosem.SET_Response;
import org.openmuc.jdlms.internal.asn1.cosem.Selective_Access_Descriptor;
import org.openmuc.jdlms.internal.asn1.cosem.Set_Request_Normal;
import org.openmuc.jdlms.internal.asn1.cosem.Set_Request_With_List;
import org.openmuc.jdlms.internal.asn1.cosem.Unsigned8;
import org.openmuc.jdlms.sessionlayer.client.SessionLayer;
import org.openmuc.jdlms.settings.client.Settings;

class DlmsLnConnection extends DlmsConnection {

    DlmsLnConnection(Settings settings, SessionLayer sessionlayer) throws IOException {
        super(settings, sessionlayer);
    }

    @Override
    public synchronized List<GetResult> get(boolean priority, List<AttributeAddress> params) throws IOException {
        if (params.isEmpty()) {
            return emptyList();
        }

        Invoke_Id_And_Priority id = invokeIdAndPriorityFor(priority);
        COSEMpdu pdu = createGetPdu(id, params);

        int pduSize = pduSizeOf(pdu);
        if (maxSendPduSize() != 0 && pduSize > maxSendPduSize()) {

            if (params.size() > 1) {
                return callEachGetIndividullay(params);
            }
            else {
                // IEC 62056-5-3 2013, Section 6.6 The GET service, Page 52:
                // A GET.request service primitive shall always fit in a single APDU
                throw new NonFatalJDlmsException(ExceptionId.GET_REQUEST_TOO_LARGE, Fault.USER,
                        MessageFormat.format(
                                "PDU ({0} byte) is too long for single GET.request. Max send PDU size is {1} byte.",
                                pduSize, maxSendPduSize()));
            }
        }

        GET_Response response = send(pdu);

        switch (response.getChoiceIndex()) {
        case GET_RESPONSE_NORMAL:
            return Arrays.asList(convertPduToGetResult(response.get_response_normal.result));
        case GET_RESPONSE_WITH_DATABLOCK:
            return readDataBlockG(response, params);
        case GET_RESPONSE_WITH_LIST:
            return convertListToDataObject(response.get_response_with_list.result.list());
        default:
            String msg = String.format(
                    "Unknown response type with Choice Index %s. Please report to developer of the stack.",
                    response.getChoiceIndex());
            throw new IllegalStateException(msg);
        }
    }

    private List<GetResult> callEachGetIndividullay(List<AttributeAddress> params) throws IOException {
        List<GetResult> res = new ArrayList<>(params.size());
        for (AttributeAddress param : params) {
            res.add(get(param));
        }
        return res;
    }

    private List<GetResult> readDataBlockG(GET_Response response, List<AttributeAddress> params) throws IOException {
        byte[] byteArray = readBlocksGet(response);
        InputStream dataByteStream = new ByteArrayInputStream(byteArray);

        if (params.size() == 1) {
            Data resultPduData = new Data();
            resultPduData.decode(dataByteStream);

            Get_Data_Result getResult = new Get_Data_Result();
            getResult.setdata(resultPduData);

            return Arrays.asList(convertPduToGetResult(getResult));
        }
        else {
            SubSeqOf_result subSeqOfResult = new SubSeqOf_result();
            subSeqOfResult.decode(dataByteStream);
            return convertListToDataObject(subSeqOfResult.list());
        }
    }

    private byte[] readBlocksGet(GET_Response response) throws IOException {
        final Invoke_Id_And_Priority invokeIdAndPriority = response.get_response_with_datablock.invoke_id_and_priority;

        ByteArrayOutputStream datablocks = new ByteArrayOutputStream();
        GET_Request getRequest = new GET_Request();
        COSEMpdu pdu = new COSEMpdu();

        Get_Request_Next nextBlock = new Get_Request_Next();
        GET_Response newRes = response;
        while (!newRes.get_response_with_datablock.result.last_block.getValue()) {
            datablocks.write(newRes.get_response_with_datablock.result.result.raw_data.getValue());

            nextBlock.block_number = newRes.get_response_with_datablock.result.block_number;
            nextBlock.invoke_id_and_priority = invokeIdAndPriority;

            getRequest.setget_request_next(nextBlock);
            pdu.setget_request(getRequest);

            try {
                newRes = send(pdu);
            } catch (ResponseTimeoutException e) {
                // Send PDU with wrong block number to indicate the
                // device that the block transfer is
                // aborted.
                // This is the well defined behavior to abort a block
                // transfer as in IEC 62056-53 section
                // 7.4.1.8.2
                // receiveTimedOut(pdu);
                send(pdu);

                throw e;
            }
        }
        // if (response.getChoiceIndex().equals(Choices.GET_RESPONSE_NORMAL)) {
        // throw new IOException("Meter response with error, access result code: "
        // + response.get_response_normal.result.data_access_result);
        // }
        // if (response.get_response_with_datablock.result.result.raw_data == null) {
        // AccessResultCode accessResultCode = AccessResultCode
        // .forValue(response.get_response_with_datablock.result.result.data_access_result.getValue());
        // }

        datablocks.write(newRes.get_response_with_datablock.result.result.raw_data.getValue());

        return datablocks.toByteArray();
    }

    @Override
    public synchronized List<AccessResultCode> set(boolean priority, List<SetParameter> params) throws IOException {
        if (params.isEmpty()) {
            return emptyList();
        }

        Invoke_Id_And_Priority invokeIdAndPriority = invokeIdAndPriorityFor(priority);
        SET_Response response = createAndSendSetPdu(invokeIdAndPriority, params);

        switch (response.getChoiceIndex()) {
        case SET_RESPONSE_NORMAL:
            return axdrEnumToAccessResultCode(response.set_response_normal.result);

        case SET_RESPONSE_WITH_LIST:
            return axdrEnumsToAccessResultCodes(response.set_response_with_list.result.list());

        case SET_RESPONSE_LAST_DATABLOCK:
            return axdrEnumToAccessResultCode(response.set_response_last_datablock.result);

        case SET_RESPONSE_LAST_DATABLOCK_WITH_LIST:
            return axdrEnumsToAccessResultCodes(response.set_response_last_datablock_with_list.result.list());

        default:
            throw new IllegalStateException("Unknown response type");
        }

    }

    private List<AccessResultCode> axdrEnumToAccessResultCode(AxdrEnum axdrEnum) {
        return Arrays.asList(enumValueFrom(axdrEnum, AccessResultCode.class));
    }

    private List<AccessResultCode> axdrEnumsToAccessResultCodes(List<AxdrEnum> enums) {
        List<AccessResultCode> result = new ArrayList<>(enums.size());
        for (AxdrEnum res : enums) {
            result.add(enumValueFrom(res, AccessResultCode.class));
        }
        return result;
    }

    @Override
    public synchronized List<MethodResult> action(boolean priority, List<MethodParameter> params) throws IOException {
        if (params.isEmpty()) {
            return emptyList();
        }

        final Invoke_Id_And_Priority id = invokeIdAndPriorityFor(priority);

        ACTION_Response response = createAndSendActionPdu(id, params);

        switch (response.getChoiceIndex()) {
        case ACTION_RESPONSE_NORMAL:
            return processActionNormal(response);
        case ACTION_RESPONSE_WITH_LIST:
            return processActionWithList(response);
        case ACTION_RESPONSE_WITH_PBLOCK:
            return processActionWithPblock(id, response);

        default:
        case ACTION_RESPONSE_NEXT_PBLOCK:
        case _ERR_NONE_SELECTED:
            throw new IllegalStateException("Server answered with an illegal response.");
        }

    }

    private List<MethodResult> processActionNormal(ACTION_Response response) {
        Action_Response_With_Optional_Data resWithOpt = response.action_response_normal.single_response;
        MethodResult methodResult = convertActionResponseToMethodResult(resWithOpt);
        return Arrays.asList(methodResult);
    }

    private List<MethodResult> processActionWithList(ACTION_Response response) {
        SubSeqOf_list_of_responses listOfResponses = response.action_response_with_list.list_of_responses;
        List<MethodResult> result = new ArrayList<>(listOfResponses.size());

        Iterator<Action_Response_With_Optional_Data> iter = listOfResponses.iterator();

        while (iter.hasNext()) {
            MethodResult methodResult = convertActionResponseToMethodResult(iter.next());
            result.add(methodResult);
        }
        return result;
    }

    private List<MethodResult> processActionWithPblock(final Invoke_Id_And_Priority id, ACTION_Response response)
            throws IOException {
        ByteArrayOutputStream datablocks = new ByteArrayOutputStream();
        COSEMpdu pdu = new COSEMpdu();
        ACTION_Request request = new ACTION_Request();
        Action_Request_Next_Pblock nextBlock = new Action_Request_Next_Pblock();
        nextBlock.invoke_id_and_priority = response.action_response_with_pblock.invoke_id_and_priority;

        while (!response.action_response_with_pblock.pblock.last_block.getValue()) {
            datablocks.write(response.action_response_with_pblock.pblock.raw_data.getValue());

            nextBlock.block_number = response.action_response_with_pblock.pblock.block_number;
            request.setaction_request_next_pblock(nextBlock);
            pdu.setaction_request(request);

            response = send(pdu);
        }
        datablocks.write(response.action_response_with_pblock.pblock.raw_data.getValue());
        InputStream dataByteStream = new ByteArrayInputStream(datablocks.toByteArray());

        return decodeAndConvertActionStream(dataByteStream);
    }

    private static List<MethodResult> decodeAndConvertActionStream(InputStream is) throws IOException {
        List<MethodResult> result = new LinkedList<>();
        while (is.available() > 0) {
            Get_Data_Result dataResult = new Get_Data_Result();
            dataResult.decode(is);
            // If remote Method call returns a pdu that must be
            // segmented into blocks of data, the assumption, that
            // the result was successful is always correct.
            DataObject resultData = DataConverter.convertDataToDataObject(dataResult.data);
            result.add(new MethodResult(MethodResultCode.SUCCESS, resultData));
        }
        return result;
    }

    /*
     * Creates a PDU to read all attributes listed in params
     */
    private COSEMpdu createGetPdu(Invoke_Id_And_Priority id, List<AttributeAddress> params) {
        if (!negotiatedFeatures().contains(ATTRIBUTE0_SUPPORTED_WITH_GET)) {
            checkAttributeIdValidty(params);
        }
        if (!negotiatedFeatures().contains(ConformanceSetting.SELECTIVE_ACCESS)) {
            for (AttributeAddress param : params) {
                if (param.getAccessSelection() != null) {
                    throw new IllegalArgumentException("Selective Access not supported on this connection");
                }
            }
        }

        GET_Request getRequest = new GET_Request();
        if (params.size() == 1) {
            Get_Request_Normal requestNormal = new Get_Request_Normal();
            requestNormal.invoke_id_and_priority = id;
            AttributeAddress attributeAddress = params.get(0);
            requestNormal.cosem_attribute_descriptor = attributeAddress.toDescriptor();
            SelectiveAccessDescription accessSelection = attributeAddress.getAccessSelection();

            Selective_Access_Descriptor access = selToSelectivAccessDesciptor(accessSelection);

            requestNormal.access_selection.setValue(access);
            getRequest.setget_request_normal(requestNormal);
        }
        else {
            Get_Request_With_List requestList = new Get_Request_With_List();
            requestList.invoke_id_and_priority = id;
            requestList.attribute_descriptor_list = new Get_Request_With_List.SubSeqOf_attribute_descriptor_list();
            for (AttributeAddress param : params) {
                SelectiveAccessDescription accessSelection = param.getAccessSelection();
                Selective_Access_Descriptor access = selToSelectivAccessDesciptor(accessSelection);

                Cosem_Attribute_Descriptor_With_Selection element = new Cosem_Attribute_Descriptor_With_Selection(
                        param.toDescriptor(), access);
                requestList.attribute_descriptor_list.add(element);
            }

            getRequest.setget_request_with_list(requestList);
        }

        COSEMpdu pdu = new COSEMpdu();
        pdu.setget_request(getRequest);

        return pdu;
    }

    private SET_Response createAndSendSetPdu(Invoke_Id_And_Priority id, List<SetParameter> params) throws IOException {
        if (!negotiatedFeatures().contains(ATTRIBUTE0_SUPPORTED_WITH_SET)) {
            for (SetParameter param : params) {
                if (param.getAttributeAddress().getId() == 0) {
                    throw new IllegalArgumentException("No Attribute 0 on set allowed");
                }
            }
        }

        SET_Request request = new SET_Request();

        if (params.size() == 1) {
            Set_Request_Normal requestNormal = new Set_Request_Normal();
            requestNormal.invoke_id_and_priority = id;
            SetParameter setParameter = params.get(0);
            AttributeAddress attributeAddress = setParameter.getAttributeAddress();
            SelectiveAccessDescription accessSelection = attributeAddress.getAccessSelection();
            Selective_Access_Descriptor access = selToSelectivAccessDesciptor(accessSelection);

            requestNormal.cosem_attribute_descriptor = attributeAddress.toDescriptor();
            requestNormal.value = DataConverter.convertDataObjectToData(setParameter.getData());
            requestNormal.access_selection.setValue(access);
            request.setset_request_normal(requestNormal);
        }
        else {
            Set_Request_With_List requestList = new Set_Request_With_List();
            requestList.invoke_id_and_priority = id;
            requestList.attribute_descriptor_list = new Set_Request_With_List.SubSeqOf_attribute_descriptor_list();
            requestList.value_list = new Set_Request_With_List.SubSeqOf_value_list();
            for (SetParameter param : params) {
                AttributeAddress attributeAddress = param.getAttributeAddress();
                SelectiveAccessDescription accessSelection = attributeAddress.getAccessSelection();
                Selective_Access_Descriptor access = selToSelectivAccessDesciptor(accessSelection);
                Cosem_Attribute_Descriptor desc = attributeAddress.toDescriptor();

                requestList.attribute_descriptor_list.add(new Cosem_Attribute_Descriptor_With_Selection(desc, access));
                requestList.value_list.add(DataConverter.convertDataObjectToData(param.getData()));
            }
            request.setset_request_with_list(requestList);
        }

        if (maxSendPduSize() == 0 || pduSizeOf(request) <= maxSendPduSize()) {
            COSEMpdu pdu = new COSEMpdu();
            pdu.setset_request(request);

            return send(pdu);
        }
        else {
            // send fragments - implements this
            // TODO
            throw new IOException("recieving fragments not yet implemented..");
        }

    }

    private static Selective_Access_Descriptor selToSelectivAccessDesciptor(
            SelectiveAccessDescription accessSelection) {
        if (accessSelection != null) {
            return new Selective_Access_Descriptor(new Unsigned8(accessSelection.getAccessSelector()),
                    DataConverter.convertDataObjectToData(accessSelection.getAccessParameter()));
        }

        return null;
    }

    private ACTION_Response createAndSendActionPdu(Invoke_Id_And_Priority invokeIdAndPrio, List<MethodParameter> params)
            throws IOException {
        for (MethodParameter param : params) {
            if (param.getId() == 0) {
                throw new IllegalArgumentException("MethodID 0 not allowed on action");
            }
        }

        ACTION_Request request = new ACTION_Request();

        if (params.size() == 1) {
            MethodParameter methodParameter = params.get(0);

            Action_Request_Normal requestNormal = new Action_Request_Normal();

            boolean paramIsUsed = !methodParameter.getParameter().isNull();

            requestNormal.invoke_id_and_priority = invokeIdAndPrio;
            requestNormal.cosem_method_descriptor = methodParameter.toDescriptor();

            AxdrOptional<Data> invocationParam = requestNormal.method_invocation_parameters;
            invocationParam.setUsed(paramIsUsed);
            if (paramIsUsed) {
                Data convertedData = convertDataObjectToData(methodParameter.getParameter());
                invocationParam.setValue(convertedData);
            }

            request.setaction_request_normal(requestNormal);
        }
        else {
            Action_Request_With_List requestList = new Action_Request_With_List();
            requestList.invoke_id_and_priority = invokeIdAndPrio;
            requestList.cosem_method_descriptor_list = new Action_Request_With_List.SubSeqOf_cosem_method_descriptor_list();
            requestList.method_invocation_parameters = new Action_Request_With_List.SubSeqOf_method_invocation_parameters();
            for (MethodParameter param : params) {
                requestList.cosem_method_descriptor_list.add(param.toDescriptor());
                requestList.method_invocation_parameters.add(convertDataObjectToData(param.getParameter()));
            }
            request.setaction_request_with_list(requestList);
        }

        if (maxSendPduSize() == 0 || pduSizeOf(request) <= maxSendPduSize()) {
            COSEMpdu pdu = new COSEMpdu();
            pdu.setaction_request(request);

            return send(pdu);
        }
        else {
            // send fragments
            // TODO
            throw new IOException("this is not yet implemented..");
        }
    }

    private static void checkAttributeIdValidty(List<AttributeAddress> params) {
        for (AttributeAddress param : params) {
            if (param.getId() == 0) {
                throw new IllegalArgumentException("No Attribute 0 on get allowed");
            }
        }
    }

    private static int pduSizeOf(AxdrType pdu) throws IOException {
        return pdu.encode(new NullOutputStream());
    }

    private static MethodResult convertActionResponseToMethodResult(Action_Response_With_Optional_Data resp) {
        DataObject resultData = null;
        if (resp.return_parameters.isUsed()) {
            resultData = DataConverter.convertDataToDataObject(resp.return_parameters.getValue().data);
        }
        MethodResultCode methodResultCode = enumValueFrom(resp.result, MethodResultCode.class);
        return new MethodResult(methodResultCode, resultData);
    }

    private static List<GetResult> convertListToDataObject(List<Get_Data_Result> resultList) {
        List<GetResult> result = new ArrayList<>(resultList.size());
        for (Get_Data_Result resultPdu : resultList) {
            GetResult res = convertPduToGetResult(resultPdu);
            result.add(res);
        }

        return result;
    }

    private static GetResult convertPduToGetResult(Get_Data_Result pdu) {
        if (pdu.getChoiceIndex() == Get_Data_Result.Choices.DATA) {
            return new GetResult(DataConverter.convertDataToDataObject(pdu.data));
        }
        else {
            AccessResultCode resultCode = enumValueFrom(pdu.data_access_result, AccessResultCode.class);
            return new GetResult(resultCode);
        }
    }

    @Override
    void processEventPdu(COSEMpdu pdu) {
        // implement event listening
    }

    @Override
    Set<ConformanceSetting> proposedConformance() {
        return new HashSet<>(Arrays.asList(GET, SET, ACTION, /* EVENT_NOTIFICATION, */ SELECTIVE_ACCESS,
                PRIORITY_MGMT_SUPPORTED, MULTIPLE_REFERENCES, BLOCK_TRANSFER_WITH_ACTION,
                BLOCK_TRANSFER_WITH_GET_OR_READ, BLOCK_TRANSFER_WITH_SET_OR_WRITE, ATTRIBUTE0_SUPPORTED_WITH_GET,
                ATTRIBUTE0_SUPPORTED_WITH_SET));
    }

    @Override
    MethodResult hlsAuthentication(byte[] processedChallenge) throws IOException {
        DataObject param = DataObject.newOctetStringData(processedChallenge);

        MethodParameter authenticate = new MethodParameter(AssociationLnMethod.REPLY_TO_HLS_AUTHENTICATION,
                WellKnownInstanceIds.CURRENT_ASSOCIATION_ID, param);

        return action(true, authenticate);
    }

    @Override
    void validateReferencingMethod() throws IOException {
        if (!(negotiatedFeatures().contains(SET) || negotiatedFeatures().contains(ConformanceSetting.GET))) {
            close();
            throw new FatalJDlmsException(ExceptionId.WRONG_REFERENCING_METHOD, Fault.USER,
                    "Wrong referencing method. Remote smart meter can't use LN referencing.");
        }
    }

    @Override
    ContextId getContextId() {

        if (connectionSettings().securitySuite().getEncryptionMechanism() != EncryptionMechanism.NONE) {
            return ContextId.LOGICAL_NAME_REFERENCING_WITH_CIPHERING;
        }
        else {
            return ContextId.LOGICAL_NAME_REFERENCING_NO_CIPHERING;
        }
    }

}
