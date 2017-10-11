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
package org.openmuc.jdlms.internal.association.ln;

import static org.openmuc.jdlms.internal.DataConverter.convertDataObjectToData;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;

import org.openmuc.jasn1.ber.BerByteArrayOutputStream;
import org.openmuc.jdlms.AttributeAddress;
import org.openmuc.jdlms.IllegalAttributeAccessException;
import org.openmuc.jdlms.ObisCode;
import org.openmuc.jdlms.SelectiveAccessDescription;
import org.openmuc.jdlms.datatypes.DataObject;
import org.openmuc.jdlms.internal.APdu;
import org.openmuc.jdlms.internal.DataConverter;
import org.openmuc.jdlms.internal.asn1.axdr.AxdrType;
import org.openmuc.jdlms.internal.asn1.axdr.types.AxdrBoolean;
import org.openmuc.jdlms.internal.asn1.axdr.types.AxdrEnum;
import org.openmuc.jdlms.internal.asn1.axdr.types.AxdrOctetString;
import org.openmuc.jdlms.internal.asn1.axdr.types.AxdrOptional;
import org.openmuc.jdlms.internal.asn1.cosem.COSEMpdu;
import org.openmuc.jdlms.internal.asn1.cosem.Cosem_Attribute_Descriptor;
import org.openmuc.jdlms.internal.asn1.cosem.Cosem_Attribute_Descriptor_With_Selection;
import org.openmuc.jdlms.internal.asn1.cosem.Data;
import org.openmuc.jdlms.internal.asn1.cosem.DataBlock_G;
import org.openmuc.jdlms.internal.asn1.cosem.DataBlock_G.SubChoice_result;
import org.openmuc.jdlms.internal.asn1.cosem.GET_Request;
import org.openmuc.jdlms.internal.asn1.cosem.GET_Response;
import org.openmuc.jdlms.internal.asn1.cosem.Get_Data_Result;
import org.openmuc.jdlms.internal.asn1.cosem.Get_Request_Next;
import org.openmuc.jdlms.internal.asn1.cosem.Get_Request_Normal;
import org.openmuc.jdlms.internal.asn1.cosem.Get_Request_With_List;
import org.openmuc.jdlms.internal.asn1.cosem.Get_Response_Normal;
import org.openmuc.jdlms.internal.asn1.cosem.Get_Response_With_Datablock;
import org.openmuc.jdlms.internal.asn1.cosem.Get_Response_With_List;
import org.openmuc.jdlms.internal.asn1.cosem.Get_Response_With_List.SubSeqOf_result;
import org.openmuc.jdlms.internal.asn1.cosem.Invoke_Id_And_Priority;
import org.openmuc.jdlms.internal.asn1.cosem.Selective_Access_Descriptor;
import org.openmuc.jdlms.internal.asn1.cosem.Unsigned32;
import org.openmuc.jdlms.internal.asn1.iso.acse.ACSEApdu;
import org.openmuc.jdlms.internal.association.AssociationMessenger;
import org.openmuc.jdlms.internal.association.RequestProcessorBase;
import org.openmuc.jdlms.internal.association.RequestProcessorData;

public class GetRequestProcessor extends RequestProcessorBase {

    /**
     * Overhead of the APdu is ~ 35 bytes.
     */
    private static final int OVERHEAD = 35;

    public GetRequestProcessor(AssociationMessenger associationMessenger, RequestProcessorData requestProcessorData) {
        super(associationMessenger, requestProcessorData);
    }

    @Override
    public void processRequest(COSEMpdu request) throws IOException {
        final int clientMaxReceivePduSize = this.associationMessenger.getMaxMessageLength();

        GET_Request getRequest = request.get_request;
        GET_Response getResponse;

        Invoke_Id_And_Priority invokeIdPrio = null;

        byte[] bytesToSend = null;
        switch (getRequest.getChoiceIndex()) {
        case GET_REQUEST_NORMAL:
            invokeIdPrio = getRequest.get_request_normal.invoke_id_and_priority;
            getResponse = processGetRequestNormal(getRequest.get_request_normal, invokeIdPrio);

            bytesToSend = encodeData(getResponse);

            encodeAndSend(clientMaxReceivePduSize, getResponse, invokeIdPrio, bytesToSend,
                    getResponse.get_response_normal.result.data);
            break;

        case GET_REQUEST_WITH_LIST:
            invokeIdPrio = getRequest.get_request_with_list.invoke_id_and_priority;
            getResponse = processGetRequestWithList(getRequest.get_request_with_list, invokeIdPrio);

            bytesToSend = encodeData(getResponse);

            encodeAndSend(clientMaxReceivePduSize, getResponse, invokeIdPrio, bytesToSend,
                    getResponse.get_response_with_list.result);
            break;

        case GET_REQUEST_NEXT:
        case _ERR_NONE_SELECTED:
        default:
            // should not occur
            // TODO answer with illegal request response
            throw new IOException();
        }

    }

    private void encodeAndSend(final long clientMaxReceivePduSize, GET_Response getResponse,
            Invoke_Id_And_Priority invokeIdPrio, byte[] bytesToSend, AxdrType axdrData) throws IOException {
        if (clientMaxReceivePduSize != 0 && bytesToSend.length > clientMaxReceivePduSize) {
            sendAsDataBlocks(clientMaxReceivePduSize, getResponse, invokeIdPrio, axdrData, bytesToSend.length);
        }
        else {
            this.associationMessenger.send(bytesToSend);
        }
    }

    private void sendAsDataBlocks(final long clientMaxReceivePduSize, GET_Response getResponse,
            Invoke_Id_And_Priority invokeIdPrio, AxdrType data, int size) throws IOException {
        BerByteArrayOutputStream axdrOStream = new BerByteArrayOutputStream(size);
        data.encode(axdrOStream);
        byte[] completeBlock = axdrOStream.getArray();

        final int blockSize = (int) clientMaxReceivePduSize - OVERHEAD;
        byte[] block = new byte[blockSize];
        long blockNumber = 1L;

        try (DataInputStream ds = new DataInputStream(new ByteArrayInputStream(completeBlock))) {
            while (true) {
                if (blockSize > ds.available()) {
                    block = new byte[ds.available()];
                }
                ds.readFully(block);

                boolean lastBlock = ds.available() == 0;

                SubChoice_result result = new SubChoice_result();
                result.setraw_data(new AxdrOctetString(block));
                DataBlock_G dataBlockG = new DataBlock_G(new AxdrBoolean(lastBlock), new Unsigned32(blockNumber++),
                        result);
                Get_Response_With_Datablock gdata = new Get_Response_With_Datablock(invokeIdPrio, dataBlockG);
                getResponse.setget_response_with_datablock(gdata);

                ACSEApdu acseAPdu = null;
                COSEMpdu coseMpdu = new COSEMpdu();
                coseMpdu.setget_response(getResponse);
                APdu blockApdu = new APdu(acseAPdu, coseMpdu);

                this.associationMessenger.encodeAndSend(blockApdu);

                if (lastBlock) {
                    break;
                }

                APdu nextApdu = this.associationMessenger.readNextApdu();

                if (!isGetNext(nextApdu)) {
                    // error
                    System.err.println("Err");
                }

                Get_Request_Next nextGetRequest = nextApdu.getCosemPdu().get_request.get_request_next;
                boolean resposneNumEquals = nextGetRequest.block_number.getValue() == blockNumber - 1;
                if (!resposneNumEquals) {
                    // handle this case
                    System.err.println("Err");
                }

                invokeIdPrio = nextGetRequest.invoke_id_and_priority;
            }
        }

    }

    private byte[] encodeData(GET_Response getResponse) throws IOException {
        COSEMpdu coseMpdu = new COSEMpdu();
        coseMpdu.setget_response(getResponse);

        APdu aPdu = new APdu(null, coseMpdu);

        return this.associationMessenger.encode(aPdu);
    }

    private static boolean isGetNext(APdu nextApdu) {
        return nextApdu.getCosemPdu().getChoiceIndex() == COSEMpdu.Choices.GET_REQUEST
                && nextApdu.getCosemPdu().get_request.getChoiceIndex() == GET_Request.Choices.GET_REQUEST_NEXT;
    }

    private GET_Response processGetRequestWithList(Get_Request_With_List requestWithList,
            Invoke_Id_And_Priority invokeIdPrio) {

        List<Cosem_Attribute_Descriptor_With_Selection> list = requestWithList.attribute_descriptor_list.list();

        SubSeqOf_result result = new SubSeqOf_result();
        for (Cosem_Attribute_Descriptor_With_Selection attributeDescriptor : list) {

            Get_Data_Result element = tryGet(attributeDescriptor.cosem_attribute_descriptor,
                    attributeDescriptor.access_selection);
            result.add(element);
        }

        GET_Response getResponse = new GET_Response();
        Get_Response_With_List responseWithList = new Get_Response_With_List(invokeIdPrio, result);
        getResponse.setget_response_with_list(responseWithList);

        return getResponse;
    }

    private GET_Response processGetRequestNormal(Get_Request_Normal normalRequest,
            Invoke_Id_And_Priority invokeIdAndPriority) {
        Get_Data_Result result = tryGet(normalRequest.cosem_attribute_descriptor, normalRequest.access_selection);
        GET_Response getResponse = new GET_Response();
        getResponse.setget_response_normal(new Get_Response_Normal(invokeIdAndPriority, result));

        return getResponse;
    }

    private Get_Data_Result tryGet(Cosem_Attribute_Descriptor cosemAttributeDescriptor,
            AxdrOptional<Selective_Access_Descriptor> accessSelection) {
        Get_Data_Result result = new Get_Data_Result();
        try {
            ObisCode instanceId = new ObisCode(cosemAttributeDescriptor.instance_id.getValue());

            SelectiveAccessDescription selectiveAccessDescription = null;
            if (accessSelection.isUsed()) {
                Selective_Access_Descriptor accessDescriptor = accessSelection.getValue();

                int accessSelector = (int) accessDescriptor.access_selector.getValue();
                DataObject accessParameter = DataConverter.convertDataToDataObject(accessDescriptor.access_parameters);
                selectiveAccessDescription = new SelectiveAccessDescription(accessSelector, accessParameter);
            }

            int logicalDeviceId = requestProcessorData.logicalDeviceId;

            long classId = cosemAttributeDescriptor.class_id.getValue();
            long attributeId = cosemAttributeDescriptor.attribute_id.getValue();

            AttributeAddress attributeAddress = new AttributeAddress((int) classId, instanceId, (int) attributeId,
                    selectiveAccessDescription);

            DataObject attributeData = this.requestProcessorData.directory.get(logicalDeviceId, attributeAddress,
                    connectionId());

            Data convertedData = convertDataObjectToData(attributeData);
            result.setdata(convertedData);
        } catch (IllegalAttributeAccessException e) {
            result.setdata_access_result(new AxdrEnum(e.getAccessResultCode().getCode()));
        }
        return result;
    }

}
