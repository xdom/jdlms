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

import static org.openmuc.jdlms.AccessResultCode.SCOPE_OF_ACCESS_VIOLATED;
import static org.openmuc.jdlms.ConformanceSetting.MULTIPLE_REFERENCES;
import static org.openmuc.jdlms.ConformanceSetting.PARAMETERIZED_ACCESS;
import static org.openmuc.jdlms.ConformanceSetting.READ;
import static org.openmuc.jdlms.ConformanceSetting.WRITE;
import static org.openmuc.jdlms.MethodResultCode.OBJECT_UNDEFINED;
import static org.openmuc.jdlms.MethodResultCode.SCOPE_OF_ACCESS_VIOLATION;
import static org.openmuc.jdlms.datatypes.DataObject.newNullData;
import static org.openmuc.jdlms.internal.DataConverter.convertDataObjectToData;
import static org.openmuc.jdlms.internal.DataConverter.convertDataToDataObject;
import static org.openmuc.jdlms.internal.DlmsEnumFunctions.enumValueFrom;
import static org.openmuc.jdlms.internal.WellKnownInstanceIds.CURRENT_ASSOCIATION_ID;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.openmuc.jdlms.JDlmsException.ExceptionId;
import org.openmuc.jdlms.JDlmsException.Fault;
import org.openmuc.jdlms.SecuritySuite.EncryptionMechanism;
import org.openmuc.jdlms.datatypes.DataObject;
import org.openmuc.jdlms.interfaceclass.method.AssociationSnMethod;
import org.openmuc.jdlms.internal.ContextId;
import org.openmuc.jdlms.internal.DataConverter;
import org.openmuc.jdlms.internal.SnInterfaceClassList;
import org.openmuc.jdlms.internal.WellKnownInstanceIds;
import org.openmuc.jdlms.internal.asn1.axdr.types.AxdrOctetString;
import org.openmuc.jdlms.internal.asn1.cosem.Block_Number_Access;
import org.openmuc.jdlms.internal.asn1.cosem.COSEMpdu;
import org.openmuc.jdlms.internal.asn1.cosem.Data;
import org.openmuc.jdlms.internal.asn1.cosem.Data.SubSeqOf_structure;
import org.openmuc.jdlms.internal.asn1.cosem.Data_Block_Result;
import org.openmuc.jdlms.internal.asn1.cosem.Integer16;
import org.openmuc.jdlms.internal.asn1.cosem.Parameterized_Access;
import org.openmuc.jdlms.internal.asn1.cosem.ReadRequest;
import org.openmuc.jdlms.internal.asn1.cosem.ReadResponse;
import org.openmuc.jdlms.internal.asn1.cosem.Unsigned16;
import org.openmuc.jdlms.internal.asn1.cosem.Unsigned8;
import org.openmuc.jdlms.internal.asn1.cosem.Variable_Access_Specification;
import org.openmuc.jdlms.internal.asn1.cosem.WriteRequest;
import org.openmuc.jdlms.internal.asn1.cosem.WriteResponse;
import org.openmuc.jdlms.sessionlayer.client.SessionLayer;
import org.openmuc.jdlms.settings.client.Settings;

/**
 * Variant of the connection class using decrypt messages with short name referencing to communicate with the remote
 * smart meter
 */
class DlmsSnConnection extends DlmsConnection {

    /**
     * Short name referring to the list of all accessible Cosem Objects on the smart meter
     */
    private static final Integer16 ASSOCIATION_OBJECT_LIST = new Integer16((short) 0xFA08);

    private Map<ObisCode, SnObjectInfo> snObjectMapping;
    private volatile boolean mapIsInitialized;

    /**
     * This map is relevant for method/action calls
     * 
     * baseName -> #attributes
     */
    DlmsSnConnection(Settings settings, SessionLayer transportLayerCon, Map<ObisCode, SnObjectInfo> snObjectMapping)
            throws IOException {
        super(settings, transportLayerCon);

        this.mapIsInitialized = false;

        if (snObjectMapping != null && !snObjectMapping.isEmpty()) {
            this.snObjectMapping = snObjectMapping;
        }
        else {
            this.snObjectMapping = new LinkedHashMap<>();
        }
    }

    @Override
    public synchronized List<GetResult> get(boolean highPriority, List<AttributeAddress> params) throws IOException {
        if (saveListIsEmpty(params)) {
            return Collections.emptyList();
        }

        if (!multipleReferencesAllowed(params)) {
            return callAllGetSeperatly(params);
        }

        GetResult[] res = new GetResult[params.size()];

        ReadRequest request = new ReadRequest();

        ListIterator<AttributeAddress> addrIter = params.listIterator();

        while (addrIter.hasNext()) {
            int nextIndex = addrIter.nextIndex();
            AttributeAddress next = addrIter.next();
            try {
                request.add(buildAddressSpec(next));
            } catch (AccessNotAllowedException e) {
                res[nextIndex] = new GetResult(AccessResultCode.OBJECT_UNDEFINED);
            }
        }

        if (request.size() == 0) {
            return Arrays.asList(res);
        }

        COSEMpdu pdu = new COSEMpdu();
        pdu.setreadRequest(request);

        ReadResponse response = sendPollRead(pdu);

        Iterator<ReadResponse.SubChoice> resIter = response.iterator();
        int resIndex = 0;
        while (resIter.hasNext()) {
            ReadResponse.SubChoice data = resIter.next();

            GetResult resultItem = convertReadResponseToGetResult(data);

            resIndex = nextFreeResultIndex(res, resIndex);

            res[resIndex++] = resultItem;
        }

        return Arrays.asList(res);
    }

    private static GetResult convertReadResponseToGetResult(ReadResponse.SubChoice data) {
        GetResult resultItem;
        if (data.getChoiceIndex() == ReadResponse.SubChoice.Choices.DATA) {
            DataObject dat = DataConverter.convertDataToDataObject(data.data);
            resultItem = new GetResult(dat);
        }
        else {
            AccessResultCode resultCode = enumValueFrom(data.data_access_error, AccessResultCode.class);
            resultItem = new GetResult(resultCode);
        }
        return resultItem;
    }

    private boolean multipleReferencesAllowed(List<?> params) {
        return params.size() == 1 || negotiatedFeatures().contains(MULTIPLE_REFERENCES);
    }

    private List<GetResult> callAllGetSeperatly(List<AttributeAddress> params) throws IOException {
        List<GetResult> resultList = new ArrayList<>(params.size());
        for (AttributeAddress attributeAddress : params) {
            resultList.add(get(attributeAddress));
        }
        return resultList;
    }

    private ReadResponse sendPollRead(COSEMpdu pdu) throws IOException {
        ReadResponse readResponse = send(pdu);

        ReadResponse.SubChoice.Choices choiceIndex = readResponse.get(0).getChoiceIndex();

        if (choiceIndex != ReadResponse.SubChoice.Choices.DATA_BLOCK_RESULT) {
            return readResponse;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        while (true) {
            Data_Block_Result dataBlockResult = readResponse.get(0).data_block_result;

            baos.write(dataBlockResult.raw_data.getValue());

            if (dataBlockResult.last_block.getValue()) {
                readResponse = new ReadResponse();
                readResponse.decode(new ByteArrayInputStream(baos.toByteArray()));
                return readResponse;
            }

            Unsigned16 blockNumber = dataBlockResult.block_number;

            readResponse = requestNexBlock(blockNumber);
        }

    }

    private ReadResponse requestNexBlock(Unsigned16 blockNumber) throws IOException {
        ReadRequest readRequest = new ReadRequest();
        Variable_Access_Specification blockNumAccess = new Variable_Access_Specification();
        blockNumAccess.setblock_number_access(new Block_Number_Access(blockNumber));
        readRequest.add(blockNumAccess);

        COSEMpdu cosemPdu = new COSEMpdu();
        cosemPdu.setreadRequest(readRequest);
        return send(cosemPdu);
    }

    @Override
    public synchronized List<AccessResultCode> set(boolean highPriority, List<SetParameter> params) throws IOException {

        if (saveListIsEmpty(params)) {
            return Collections.emptyList();
        }
        if (!multipleReferencesAllowed(params)) {
            return callEachSetIndividually(params);
        }

        if (!negotiatedFeatures().contains(ConformanceSetting.WRITE)) {
            return answertWithWriteNotAllowed(params);
        }

        AccessResultCode[] result = new AccessResultCode[params.size()];

        ListIterator<SetParameter> paramsIter = params.listIterator();

        WriteRequest request = new WriteRequest();
        request.list_of_data = new WriteRequest.SubSeqOf_list_of_data();
        request.variable_access_specification = new WriteRequest.SubSeqOf_variable_access_specification();
        while (paramsIter.hasNext()) {
            int nextIndex = paramsIter.nextIndex();
            SetParameter next = paramsIter.next();
            try {
                Variable_Access_Specification variableAccessSpec = buildAddressSpec(next.getAttributeAddress());
                Data data = convertDataObjectToData(next.getData());

                request.variable_access_specification.add(variableAccessSpec);
                request.list_of_data.add(data);
            } catch (AccessNotAllowedException e) {
                result[nextIndex] = AccessResultCode.SCOPE_OF_ACCESS_VIOLATED;
            }
        }

        if (request.variable_access_specification.size() == 0) {
            return Arrays.asList(result);
        }

        COSEMpdu pdu = new COSEMpdu();
        pdu.setwriteRequest(request);

        WriteResponse writeRes = send(pdu);
        Iterator<WriteResponse.SubChoice> responseIter = writeRes.iterator();

        int resultIndex = 0;
        while (responseIter.hasNext()) {
            WriteResponse.SubChoice response = responseIter.next();
            AccessResultCode item;
            if (response.getChoiceIndex() == WriteResponse.SubChoice.Choices.SUCCESS) {
                item = AccessResultCode.SUCCESS;
            }
            else {
                item = enumValueFrom(response.data_access_error, AccessResultCode.class);
            }

            resultIndex = nextFreeResultIndex(result, resultIndex);

            result[resultIndex++] = item;
        }
        return Arrays.asList(result);

    }

    private List<AccessResultCode> answertWithWriteNotAllowed(List<SetParameter> params) {
        List<AccessResultCode> resList = new ArrayList<>(params.size());
        for (int i = 0; i < params.size(); ++i) {
            resList.add(SCOPE_OF_ACCESS_VIOLATED);
        }
        return resList;
    }

    private List<AccessResultCode> callEachSetIndividually(List<SetParameter> params) throws IOException {
        List<AccessResultCode> resList = new ArrayList<>(params.size());

        for (SetParameter param : params) {
            resList.add(set(param));
        }
        return resList;
    }

    @Override
    public synchronized List<MethodResult> action(boolean priority, List<MethodParameter> params) throws IOException {
        if (saveListIsEmpty(params)) {
            return Collections.emptyList();
        }

        Set<ConformanceSetting> negotiatedFeatures = negotiatedFeatures();
        if (!negotiatedFeatures.contains(READ) || !negotiatedFeatures.contains(PARAMETERIZED_ACCESS)) {
            // method request is not allowed.
            return setFailureResForAllParams(params);
        }

        if (!multipleReferencesAllowed(params)) {
            return callEachActionIndividually(priority, params);
        }

        return confirmedMethdoCall(params);
    }

    private List<MethodResult> setFailureResForAllParams(List<MethodParameter> params) {
        List<MethodResult> resList = new ArrayList<>(params.size());
        final MethodResult result = new MethodResult(SCOPE_OF_ACCESS_VIOLATION);
        for (int i = 0; i < params.size(); ++i) {
            resList.add(result);
        }
        return resList;
    }

    private List<MethodResult> confirmedMethdoCall(List<MethodParameter> params) throws IOException {
        ReadRequest readRequest = new ReadRequest();

        MethodResult[] result = new MethodResult[params.size()];

        ListIterator<MethodParameter> paramsIter = params.listIterator();

        while (paramsIter.hasNext()) {

            int index = paramsIter.nextIndex();
            MethodParameter param = paramsIter.next();

            SnObjectInfo snObjectInfo;
            try {
                snObjectInfo = accessSnObjectInfo(param);
            } catch (AccessNotAllowedException e) {
                result[index] = new MethodResult(OBJECT_UNDEFINED);
                continue;
            }

            MethodIdOffsetPair firstMethodIndexPair = SnInterfaceClassList.firstMethodPairFor(snObjectInfo);

            if (firstMethodIndexPair == null || firstMethodIndexPair.getFirstMethodId() < param.getId()) {
                result[index] = new MethodResult(OBJECT_UNDEFINED);
                continue;
            }
            Integer16 variableName = methodVarNameFor(param, snObjectInfo, firstMethodIndexPair);

            Variable_Access_Specification access = buildReadVarSpec(param, variableName);

            readRequest.add(access);
        }

        if (readRequest.size() == 0) {
            return Arrays.asList(result);
        }

        COSEMpdu pdu = new COSEMpdu();

        pdu.setreadRequest(readRequest);

        Iterator<ReadResponse.SubChoice> iter = sendPollRead(pdu).iterator();
        int resId = 0;
        while (iter.hasNext()) {
            ReadResponse.SubChoice retVal = iter.next();
            DataObject returnValue;
            MethodResultCode resultCode;

            switch (retVal.getChoiceIndex()) {
            case DATA:
                returnValue = convertDataToDataObject(retVal.data);
                resultCode = MethodResultCode.SUCCESS;
                break;
            case DATA_ACCESS_ERROR:
                returnValue = null;
                resultCode = enumValueFrom(retVal.data_access_error, MethodResultCode.class);
                break;

            default:
                // error case. should not occur
                returnValue = null;
                resultCode = MethodResultCode.TEMPORARY_FAILURE;
                break;
            }

            resId = nextFreeResultIndex(result, resId);

            result[resId++] = new MethodResult(resultCode, returnValue);
        }
        return Arrays.asList(result);
    }

    private static int nextFreeResultIndex(Object[] result, int resId) {
        while (resId < result.length && result[resId] != null) {
            ++resId;
        }
        return resId;
    }

    private Integer16 methodVarNameFor(MethodParameter param, SnObjectInfo snObjectInfo,
            MethodIdOffsetPair firstMethodIndexPair) {
        Integer fMethodId = firstMethodIndexPair.getFirstMethodId();
        Integer fMethodOffset = firstMethodIndexPair.getFirstMethodOffset();
        int methodIdOffset = fMethodOffset + 0x08 * (param.getId() - fMethodId);
        return new Integer16(snObjectInfo.getBaseName() + methodIdOffset);
    }

    private static Variable_Access_Specification buildReadVarSpec(MethodParameter param, Integer16 variableName) {
        DataObject parameter = param.getParameter();

        Variable_Access_Specification access = new Variable_Access_Specification();

        if (parameter == null) {
            parameter = newNullData();
        }

        Unsigned8 selector = new Unsigned8(0);
        Data data = convertDataObjectToData(parameter);
        Parameterized_Access accessParam = new Parameterized_Access(variableName, selector, data);
        access.setparameterized_access(accessParam);

        return access;
    }

    private List<MethodResult> callEachActionIndividually(boolean priority, List<MethodParameter> params)
            throws IOException {
        List<MethodResult> result = new ArrayList<>(params.size());
        for (MethodParameter methodParameter : params) {
            result.add(action(priority, methodParameter));
        }
        return result;
    }

    @Override
    void processEventPdu(COSEMpdu pdu) {
        // add event listening INFORMATIONREPORTREQUEST
    }

    @Override
    protected Set<ConformanceSetting> proposedConformance() {
        return new HashSet<>(
                Arrays.asList(READ, WRITE, MULTIPLE_REFERENCES, PARAMETERIZED_ACCESS /* , INFORMTION_REPORT */));
    }

    private Variable_Access_Specification buildAddressSpec(AttributeAddress attributeAddress)
            throws IOException, AccessNotAllowedException {
        SnObjectInfo info = accessSnObjectInfo(attributeAddress);

        Variable_Access_Specification accessSpec = new Variable_Access_Specification();

        Integer16 variableName = new Integer16(info.getBaseName() + 0x08 * (attributeAddress.getId() - 1));
        SelectiveAccessDescription accessSelection = attributeAddress.getAccessSelection();
        if (accessSelection == null) {
            accessSpec.setvariable_name(variableName);
        }
        else {
            if (!negotiatedFeatures().contains(PARAMETERIZED_ACCESS)) {
                throw new AccessNotAllowedException("Connection doesn't allow access selection");
            }

            Unsigned8 selector = new Unsigned8(accessSelection.getAccessSelector());
            Data data = convertDataObjectToData(accessSelection.getAccessParameter());

            accessSpec.setparameterized_access(new Parameterized_Access(variableName, selector, data));
        }

        return accessSpec;
    }

    private class AccessNotAllowedException extends Exception {
        public AccessNotAllowedException(String message) {
            super(message);
        }
    }

    private SnObjectInfo accessSnObjectInfo(CosemResourceDescriptor cosemResourceDescriptor)
            throws IOException, AccessNotAllowedException {
        ObisCode instanceId = cosemResourceDescriptor.getInstanceId();
        SnObjectInfo info = snObjectMapping.get(instanceId);

        if (info == null && !mapIsInitialized) {
            try {
                info = getVariableInfo(cosemResourceDescriptor);
                snObjectMapping.put(instanceId, info);
            } catch (IOException e) {
                initializeLnMap();
                info = snObjectMapping.get(instanceId);
            }
        }

        if (info == null) {
            throw new AccessNotAllowedException(
                    "Object " + instanceId + " unknown to the smart meter. Try an other address.");
        }

        return info;
    }

    private boolean saveListIsEmpty(List<?> params) {
        return params == null || params.isEmpty();
    }

    private SnObjectInfo getVariableInfo(CosemResourceDescriptor param) throws IOException {
        if (!negotiatedFeatures().contains(PARAMETERIZED_ACCESS)) {
            throw new IOException("Connection does not allow parameterized actions");
        }

        Variable_Access_Specification getBaseName = new Variable_Access_Specification();

        Unsigned8 selector = new Unsigned8(2);
        Data filter = objectListFilterFor(param);
        Parameterized_Access parametrizedAccess = new Parameterized_Access(ASSOCIATION_OBJECT_LIST, selector, filter);

        getBaseName.setparameterized_access(parametrizedAccess);

        ReadRequest request = new ReadRequest();
        request.add(getBaseName);

        COSEMpdu pdu = new COSEMpdu();
        pdu.setreadRequest(request);
        send(pdu);

        ReadResponse response = send(pdu);

        ReadResponse.SubChoice subChoice = response.get(0);
        if (subChoice.getChoiceIndex() == ReadResponse.SubChoice.Choices.DATA_ACCESS_ERROR) {
            throw new IOException("Data Access Error ..");
        }

        SubSeqOf_structure objectListElement = subChoice.data.structure;

        int baseName = (int) objectListElement.get(0).long_integer.getValue();
        int classId = (int) objectListElement.get(1).long_unsigned.getValue();
        int version = (int) objectListElement.get(2).unsigned.getValue();

        return new SnObjectInfo(baseName, classId, version);
    }

    private Data objectListFilterFor(CosemResourceDescriptor param) {
        Data filter = new Data();
        Data.SubSeqOf_structure filterStruct = new Data.SubSeqOf_structure();

        filterStruct.add(new Data());
        filterStruct.add(new Data());

        filterStruct.get(0).setlong_unsigned(new Unsigned16(param.getClassId()));
        filterStruct.get(1).setoctet_string(new AxdrOctetString(param.getInstanceId().bytes()));

        filter.setstructure(filterStruct);
        return filter;
    }

    Map<ObisCode, SnObjectInfo> getLatestObjectInfoMapping() throws IOException {
        synchronized (this.snObjectMapping) {
            initializeLnMap();
            return new HashMap<>(this.snObjectMapping);
        }
    }

    private void initializeLnMap() throws IOException {
        synchronized (snObjectMapping) {
            if (mapIsInitialized) {
                return;
            }

            ReadRequest request = new ReadRequest();
            Variable_Access_Specification getObjectList = new Variable_Access_Specification();
            getObjectList.setvariable_name(ASSOCIATION_OBJECT_LIST);
            request.add(getObjectList);

            COSEMpdu pdu = new COSEMpdu();
            pdu.setreadRequest(request);
            send(pdu);

            ReadResponse response = send(pdu);

            ReadResponse.SubChoice.Choices accessResultCode = response.get(0).getChoiceIndex();

            if (accessResultCode == ReadResponse.SubChoice.Choices.DATA_ACCESS_ERROR) {
                throw new FatalJDlmsException(ExceptionId.CONNECTION_ESTABLISH_ERROR, Fault.SYSTEM,
                        "Could not access the mapping list.");
            }

            List<Data> resultData = response.get(0).data.array.list();
            for (Data object : resultData) {
                SubSeqOf_structure objectStructur = object.structure;
                int baseName = (int) objectStructur.get(0).long_integer.getValue();
                int classId = (int) objectStructur.get(1).long_unsigned.getValue();
                int version = (int) objectStructur.get(2).unsigned.getValue();

                SnObjectInfo value = new SnObjectInfo(baseName, classId, version);

                byte[] instancIdBytes = objectStructur.get(3).octet_string.getValue();
                ObisCode instanceId = new ObisCode(instancIdBytes);

                this.snObjectMapping.put(instanceId, value);
            }

            this.mapIsInitialized = true;
        }
    }

    @Override
    protected MethodResult hlsAuthentication(byte[] processedChallenge) throws IOException {
        DataObject param = DataObject.newOctetStringData(processedChallenge);

        WellKnownInstanceIds.SECURITY_SETUP_ID.length();
        MethodParameter authenticate = new MethodParameter(AssociationSnMethod.REPLY_TO_HLS_AUTHENTICATION,
                CURRENT_ASSOCIATION_ID, param);
        return action(false, authenticate);
    }

    @Override
    protected void validateReferencingMethod() throws IOException {
        // If the Conformance bit string does not contain
        // read nor write -> this smart meter cannot
        // communicate with SN referencing.
        if (!(negotiatedFeatures().contains(READ) || negotiatedFeatures().contains(WRITE))) {
            close();
            throw new IOException("Wrong referencing method. Remote smart meter can't use SN referencing");
        }
    }

    @Override
    protected ContextId getContextId() {
        EncryptionMechanism encryptionMechanism = connectionSettings().securitySuite().getEncryptionMechanism();
        if (encryptionMechanism != EncryptionMechanism.NONE) {
            return ContextId.SHORT_NAME_REFERENCING_WITH_CIPHERING;
        }
        else {
            return ContextId.SHORT_NAME_REFERENCING_NO_CIPHERING;
        }
    }

}
