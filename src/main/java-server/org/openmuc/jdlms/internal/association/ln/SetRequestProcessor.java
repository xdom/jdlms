package org.openmuc.jdlms.internal.association.ln;

import java.io.IOException;
import java.util.Iterator;

import org.openmuc.jdlms.AccessResultCode;
import org.openmuc.jdlms.AttributeAddress;
import org.openmuc.jdlms.ObisCode;
import org.openmuc.jdlms.SelectiveAccessDescription;
import org.openmuc.jdlms.SetParameter;
import org.openmuc.jdlms.datatypes.DataObject;
import org.openmuc.jdlms.internal.APdu;
import org.openmuc.jdlms.internal.DataConverter;
import org.openmuc.jdlms.internal.asn1.axdr.types.AxdrEnum;
import org.openmuc.jdlms.internal.asn1.axdr.types.AxdrOptional;
import org.openmuc.jdlms.internal.asn1.cosem.COSEMpdu;
import org.openmuc.jdlms.internal.asn1.cosem.Cosem_Attribute_Descriptor;
import org.openmuc.jdlms.internal.asn1.cosem.Cosem_Attribute_Descriptor_With_Selection;
import org.openmuc.jdlms.internal.asn1.cosem.Data;
import org.openmuc.jdlms.internal.asn1.cosem.Invoke_Id_And_Priority;
import org.openmuc.jdlms.internal.asn1.cosem.SET_Request;
import org.openmuc.jdlms.internal.asn1.cosem.SET_Response;
import org.openmuc.jdlms.internal.asn1.cosem.Selective_Access_Descriptor;
import org.openmuc.jdlms.internal.asn1.cosem.Set_Request_Normal;
import org.openmuc.jdlms.internal.asn1.cosem.Set_Request_With_List;
import org.openmuc.jdlms.internal.asn1.cosem.Set_Response_Normal;
import org.openmuc.jdlms.internal.asn1.cosem.Set_Response_With_List;
import org.openmuc.jdlms.internal.association.AssociationMessenger;
import org.openmuc.jdlms.internal.association.RequestProcessorBase;
import org.openmuc.jdlms.internal.association.RequestProcessorData;

public class SetRequestProcessor extends RequestProcessorBase {

    public SetRequestProcessor(AssociationMessenger associationMessenger, RequestProcessorData requestProcessorData) {
        super(associationMessenger, requestProcessorData);
    }

    @Override
    public void processRequest(COSEMpdu request) throws IOException {
        SET_Request setRequest = request.set_request;
        SET_Response setResponse;
        switch (setRequest.getChoiceIndex()) {
        case SET_REQUEST_NORMAL:
            setResponse = processSetRequestNormal(setRequest.set_request_normal);
            break;
        case SET_REQUEST_WITH_LIST:
            setResponse = processSetRequestWithList(setRequest.set_request_with_list);
            break;
        case SET_REQUEST_WITH_DATABLOCK:
        default:
            throw new IOException("Not yet implemented");
        }

        COSEMpdu cosemPdu = new COSEMpdu();
        cosemPdu.setset_response(setResponse);
        APdu aPdu = new APdu(null, cosemPdu);
        this.associationMessenger.encodeAndSend(aPdu);

    }

    private SET_Response processSetRequestWithList(Set_Request_With_List requestWithList) {
        Invoke_Id_And_Priority invokeIdAndPriority = requestWithList.invoke_id_and_priority;

        Iterator<Cosem_Attribute_Descriptor_With_Selection> descriptorListIter = requestWithList.attribute_descriptor_list
                .list().iterator();
        Iterator<Data> valueListIter = requestWithList.value_list.list().iterator();

        Set_Response_With_List.SubSeqOf_result result = new Set_Response_With_List.SubSeqOf_result();
        while (descriptorListIter.hasNext() && valueListIter.hasNext()) {
            Cosem_Attribute_Descriptor_With_Selection descriptor = descriptorListIter.next();

            AccessResultCode resultCode = convertAndSet(valueListIter.next(), descriptor.cosem_attribute_descriptor,
                    descriptor.access_selection);
            result.add(new AxdrEnum(resultCode.getCode()));
        }
        SET_Response setResponse = new SET_Response();
        Set_Response_With_List responseWithList = new Set_Response_With_List(invokeIdAndPriority, result);
        setResponse.setset_response_with_list(responseWithList);

        return setResponse;
    }

    private SET_Response processSetRequestNormal(Set_Request_Normal normalRequest) {
        Invoke_Id_And_Priority invokeIdAndPriority = normalRequest.invoke_id_and_priority;

        AccessResultCode accessResultCode = convertAndSet(normalRequest.value, normalRequest.cosem_attribute_descriptor,
                normalRequest.access_selection);

        Set_Response_Normal setResponseNormal = new Set_Response_Normal(invokeIdAndPriority,
                new AxdrEnum(accessResultCode.getCode()));

        SET_Response setResponse = new SET_Response();
        setResponse.setset_response_normal(setResponseNormal);
        return setResponse;
    }

    private AccessResultCode convertAndSet(Data newValue, Cosem_Attribute_Descriptor cosemAttributeAescriptor,
            AxdrOptional<Selective_Access_Descriptor> accessSelection) {
        DataObject dataObject = DataConverter.convertDataToDataObject(newValue);

        ObisCode instanceId = new ObisCode(cosemAttributeAescriptor.instance_id.getValue());
        SelectiveAccessDescription selectiveAccessDescription = null;

        if (accessSelection.isUsed()) {
            Selective_Access_Descriptor accessDescriptor = accessSelection.getValue();
            int accessSelector = (int) accessDescriptor.access_selector.getValue();
            DataObject accessParameter = DataConverter.convertDataToDataObject(accessDescriptor.access_parameters);
            selectiveAccessDescription = new SelectiveAccessDescription(accessSelector, accessParameter);
        }

        int classId = (int) cosemAttributeAescriptor.class_id.getValue();
        int attributeId = (int) cosemAttributeAescriptor.attribute_id.getValue();
        AttributeAddress attributeAddress = new AttributeAddress(classId, instanceId, attributeId,
                selectiveAccessDescription);
        SetParameter setParam = new SetParameter(attributeAddress, dataObject);

        return this.requestProcessorData.directory.set(this.requestProcessorData.logicalDeviceId, setParam,
                connectionId());
    }

}
