package org.openmuc.jdlms.internal.association.ln;

import static org.openmuc.jdlms.internal.DataConverter.convertDataToDataObject;
import static org.openmuc.jdlms.internal.WellKnownInstanceIds.CURRENT_ASSOCIATION_ID;

import java.io.IOException;
import java.util.Iterator;

import org.openmuc.jasn1.ber.BerByteArrayOutputStream;
import org.openmuc.jdlms.IllegalMethodAccessException;
import org.openmuc.jdlms.MethodParameter;
import org.openmuc.jdlms.MethodResultCode;
import org.openmuc.jdlms.ObisCode;
import org.openmuc.jdlms.datatypes.DataObject;
import org.openmuc.jdlms.internal.APdu;
import org.openmuc.jdlms.internal.DataConverter;
import org.openmuc.jdlms.internal.MessageFragment;
import org.openmuc.jdlms.internal.PduHelper;
import org.openmuc.jdlms.internal.ServiceError;
import org.openmuc.jdlms.internal.StateError;
import org.openmuc.jdlms.internal.asn1.axdr.AxdrType;
import org.openmuc.jdlms.internal.asn1.axdr.types.AxdrBoolean;
import org.openmuc.jdlms.internal.asn1.axdr.types.AxdrEnum;
import org.openmuc.jdlms.internal.asn1.axdr.types.AxdrOctetString;
import org.openmuc.jdlms.internal.asn1.axdr.types.AxdrOptional;
import org.openmuc.jdlms.internal.asn1.cosem.ACTION_Request;
import org.openmuc.jdlms.internal.asn1.cosem.ACTION_Response;
import org.openmuc.jdlms.internal.asn1.cosem.Action_Request_Next_Pblock;
import org.openmuc.jdlms.internal.asn1.cosem.Action_Request_Normal;
import org.openmuc.jdlms.internal.asn1.cosem.Action_Request_With_List;
import org.openmuc.jdlms.internal.asn1.cosem.Action_Response_Normal;
import org.openmuc.jdlms.internal.asn1.cosem.Action_Response_With_List;
import org.openmuc.jdlms.internal.asn1.cosem.Action_Response_With_Optional_Data;
import org.openmuc.jdlms.internal.asn1.cosem.Action_Response_With_Pblock;
import org.openmuc.jdlms.internal.asn1.cosem.COSEMpdu;
import org.openmuc.jdlms.internal.asn1.cosem.Cosem_Method_Descriptor;
import org.openmuc.jdlms.internal.asn1.cosem.Data;
import org.openmuc.jdlms.internal.asn1.cosem.DataBlock_SA;
import org.openmuc.jdlms.internal.asn1.cosem.Get_Data_Result;
import org.openmuc.jdlms.internal.asn1.cosem.Invoke_Id_And_Priority;
import org.openmuc.jdlms.internal.asn1.cosem.Unsigned32;
import org.openmuc.jdlms.internal.association.AssociationException;
import org.openmuc.jdlms.internal.association.AssociationMessenger;
import org.openmuc.jdlms.internal.association.RequestProcessorBase;
import org.openmuc.jdlms.internal.association.RequestProcessorData;

public class ActionRequestProcessor extends RequestProcessorBase {

    private static final ObisCode ASSOCIATION_LN_INSTANCE_ID = new ObisCode(CURRENT_ASSOCIATION_ID);

    public ActionRequestProcessor(AssociationMessenger associationMessenger,
            RequestProcessorData requestProcessorData) {
        super(associationMessenger, requestProcessorData);
    }

    @Override
    public void processRequest(COSEMpdu request) throws IOException {
        ACTION_Request actionRequest = request.action_request;

        switch (actionRequest.getChoiceIndex()) {
        case ACTION_REQUEST_NORMAL:
            processActionRequestNormal(actionRequest.action_request_normal);
            break;

        case ACTION_REQUEST_WITH_LIST:
            processActionRequestWithList(actionRequest.action_request_with_list);
            break;

        default:
            throw new IOException("Not yet implemented");
        }
    }

    private Action_Response_With_Optional_Data invokeMethod(Cosem_Method_Descriptor methodDescriptor, DataObject param)
            throws AssociationException {

        Get_Data_Result getDataResult = new Get_Data_Result();

        ObisCode instanceId = new ObisCode(methodDescriptor.instance_id.getValue());
        long classId = methodDescriptor.class_id.getValue();
        long methodId = methodDescriptor.method_id.getValue();

        if (!this.requestProcessorData.connectionData.authenticated
                && !(ASSOCIATION_LN_INSTANCE_ID.equals(instanceId) && classId == 15 && methodId == 1)) {
            throw new AssociationException(StateError.SERVICE_NOT_ALLOWED, ServiceError.OPERATION_NOT_POSSIBLE);
        }

        DataObject result = null;
        MethodResultCode resultCode = MethodResultCode.SUCCESS;
        try {
            MethodParameter methodParameter = new MethodParameter((int) classId, instanceId, (int) methodId, param);

            result = this.requestProcessorData.directory.invokeMethod(requestProcessorData.logicalDeviceId,
                    methodParameter, connectionId());
        } catch (IllegalMethodAccessException e) {
            resultCode = e.getMethodResultCode();
            getDataResult = null;
        }

        if (result != null) {
            getDataResult.setdata(DataConverter.convertDataObjectToData(result));
        }
        else {
            getDataResult = null;
        }
        return new Action_Response_With_Optional_Data(new AxdrEnum(resultCode.getCode()), getDataResult);
    }

    private void processActionRequestNormal(Action_Request_Normal normalRequest) throws IOException {
        Invoke_Id_And_Priority invokeIdAndPriority = normalRequest.invoke_id_and_priority;
        Cosem_Method_Descriptor cosemMethodDescriptor = normalRequest.cosem_method_descriptor;
        AxdrOptional<Data> methodInvocationParameters = normalRequest.method_invocation_parameters;

        DataObject param;
        if (methodInvocationParameters.isUsed()) {
            param = convertDataToDataObject(methodInvocationParameters.getValue());
        }
        else {

            param = null;
        }
        Action_Response_With_Optional_Data singleResult = invokeMethod(cosemMethodDescriptor, param);

        ACTION_Response actionResponse = new ACTION_Response();
        Action_Response_Normal normalResponse = new Action_Response_Normal(invokeIdAndPriority, singleResult);
        actionResponse.setaction_response_normal(normalResponse);

        if (!associationMessenger.pduSizeTooLarge(actionResponse)) {
            sendActionResponse(actionResponse);

            return;
        }

        byte[] rawData = encodePduRawDataBlockData(singleResult);
        sendActionResponseAsFragments(invokeIdAndPriority, rawData);
    }

    private void sendActionResponseAsFragments(Invoke_Id_And_Priority invokeIdAndPriority, byte[] rawData)
            throws IOException {
        ACTION_Response actionResponse = new ACTION_Response();

        final int fragmentSize = (int) this.requestProcessorData.connectionData.clientMaxReceivePduSize - 10;
        MessageFragment messageFragment = new MessageFragment(rawData, fragmentSize);

        long blockNumber = 1;
        boolean lastBlock = false;
        byte[] octetString = messageFragment.next();

        DataBlock_SA pblock = pBlockFrom(blockNumber, lastBlock, octetString);
        Action_Response_With_Pblock responseWithPblock = new Action_Response_With_Pblock(invokeIdAndPriority, pblock);
        actionResponse.setaction_response_with_pblock(responseWithPblock);
        sendActionResponse(actionResponse);

        while (messageFragment.hasNext()) {
            COSEMpdu cosemPdu = associationMessenger.readNextApdu().getCosemPdu();
            if (cosemPdu.getChoiceIndex() != COSEMpdu.Choices.ACTION_REQUEST) {
                // TODO error
                throw new IOException("wrong request type.");
            }
            ACTION_Request actionRequest = cosemPdu.action_request;

            if (actionRequest.getChoiceIndex() != ACTION_Request.Choices.ACTION_REQUEST_NEXT_PBLOCK) {
                // TODO error
                throw new IOException("Wrong action type.");
            }

            Action_Request_Next_Pblock requestNextPblock = actionRequest.action_request_next_pblock;
            Invoke_Id_And_Priority invokeIdAndPriorityRpl = requestNextPblock.invoke_id_and_priority;
            if (PduHelper.invokeIdFrom(invokeIdAndPriorityRpl) != PduHelper.invokeIdFrom(invokeIdAndPriority)) {
                throw new IOException("Wrong invoke id");
            }

            if (blockNumber++ != requestNextPblock.block_number.getValue()) {
                // TODO: error
                throw new IOException("Wrong pblock confimation.");
            }

            pblock = pBlockFrom(blockNumber, !messageFragment.hasNext(), messageFragment.next());
            responseWithPblock = new Action_Response_With_Pblock(invokeIdAndPriority, pblock);

            actionResponse.setaction_response_with_pblock(responseWithPblock);
            sendActionResponse(actionResponse);
        }
    }

    private void processActionRequestWithList(Action_Request_With_List requestWithList) throws IOException {
        Invoke_Id_And_Priority invokeIdAndPriority = requestWithList.invoke_id_and_priority;

        Iterator<Data> invocationParametersIter = requestWithList.method_invocation_parameters.list().iterator();
        Iterator<Cosem_Method_Descriptor> methodDescriptorIter = requestWithList.cosem_method_descriptor_list.list()
                .iterator();

        Action_Response_With_List.SubSeqOf_list_of_responses listOfResponses = new Action_Response_With_List.SubSeqOf_list_of_responses();
        while (invocationParametersIter.hasNext() && methodDescriptorIter.hasNext()) {
            Data invokationParameter = invocationParametersIter.next();
            Cosem_Method_Descriptor methodDescriptor = methodDescriptorIter.next();

            DataObject param = convertDataToDataObject(invokationParameter);
            if (param.isNull()) {
                param = null;
            }

            Action_Response_With_Optional_Data actionResult = invokeMethod(methodDescriptor, param);
            listOfResponses.add(actionResult);
        }

        Action_Response_With_List responseWithList = new Action_Response_With_List(invokeIdAndPriority,
                listOfResponses);

        ACTION_Response actionResponse = new ACTION_Response();

        actionResponse.setaction_response_with_list(responseWithList);
        if (!associationMessenger.pduSizeTooLarge(actionResponse)) {
            sendActionResponse(actionResponse);

            return;
        }

        byte[] rawData = encodePduRawDataBlockData(listOfResponses);

        sendActionResponseAsFragments(invokeIdAndPriority, rawData);
    }

    private static byte[] encodePduRawDataBlockData(AxdrType axdrType) throws IOException {
        // TODO set correct buffer size
        BerByteArrayOutputStream axdrOStream = new BerByteArrayOutputStream(0xffff);
        axdrType.encode(axdrOStream);
        return axdrOStream.getArray();
    }

    private static DataBlock_SA pBlockFrom(long blockNumber, boolean lastBlock, byte[] octetString) {
        AxdrBoolean lastBlockAxdr = new AxdrBoolean(lastBlock);
        Unsigned32 blockNumberU32 = new Unsigned32(blockNumber);
        AxdrOctetString rawData = new AxdrOctetString(octetString);

        return new DataBlock_SA(lastBlockAxdr, blockNumberU32, rawData);
    }

    private void sendActionResponse(ACTION_Response actionResponse) throws IOException {
        COSEMpdu cosemPdu = new COSEMpdu();
        cosemPdu.setaction_response(actionResponse);

        APdu aPdu = new APdu(null, cosemPdu);

        associationMessenger.encodeAndSend(aPdu);
    }
}
