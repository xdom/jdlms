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

import static org.openmuc.jdlms.JDlmsException.ExceptionId.CONNECTION_ESTABLISH_ERROR;
import static org.openmuc.jdlms.JDlmsException.Fault.SYSTEM;
import static org.openmuc.jdlms.internal.ConformanceSettingConverter.conformanceFor;
import static org.openmuc.jdlms.internal.ObjectIdentifier.applicationContextNameFrom;
import static org.openmuc.jdlms.internal.ObjectIdentifier.mechanismNameFrom;
import static org.openmuc.jdlms.internal.security.RandomSequenceGenerator.generateNewChallenge;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.openmuc.jasn1.ber.types.BerOctetString;
import org.openmuc.jdlms.JDlmsException.ExceptionId;
import org.openmuc.jdlms.JDlmsException.Fault;
import org.openmuc.jdlms.RawMessageData.MessageSource;
import org.openmuc.jdlms.RawMessageData.RawMessageDataBuilder;
import org.openmuc.jdlms.SecuritySuite.EncryptionMechanism;
import org.openmuc.jdlms.datatypes.DataObject.Type;
import org.openmuc.jdlms.internal.APdu;
import org.openmuc.jdlms.internal.ConformanceSettingConverter;
import org.openmuc.jdlms.internal.ContextId;
import org.openmuc.jdlms.internal.PduHelper;
import org.openmuc.jdlms.internal.ReleaseReqReason;
import org.openmuc.jdlms.internal.asn1.axdr.AxdrType;
import org.openmuc.jdlms.internal.asn1.axdr.types.AxdrBoolean;
import org.openmuc.jdlms.internal.asn1.axdr.types.AxdrOctetString;
import org.openmuc.jdlms.internal.asn1.cosem.COSEMpdu;
import org.openmuc.jdlms.internal.asn1.cosem.ConfirmedServiceError;
import org.openmuc.jdlms.internal.asn1.cosem.Conformance;
import org.openmuc.jdlms.internal.asn1.cosem.EXCEPTION_Response;
import org.openmuc.jdlms.internal.asn1.cosem.InitiateRequest;
import org.openmuc.jdlms.internal.asn1.cosem.InitiateResponse;
import org.openmuc.jdlms.internal.asn1.cosem.Integer8;
import org.openmuc.jdlms.internal.asn1.cosem.Invoke_Id_And_Priority;
import org.openmuc.jdlms.internal.asn1.cosem.Unsigned16;
import org.openmuc.jdlms.internal.asn1.cosem.Unsigned8;
import org.openmuc.jdlms.internal.asn1.iso.acse.AAREApdu;
import org.openmuc.jdlms.internal.asn1.iso.acse.AARQApdu;
import org.openmuc.jdlms.internal.asn1.iso.acse.ACSEApdu;
import org.openmuc.jdlms.internal.asn1.iso.acse.ACSERequirements;
import org.openmuc.jdlms.internal.asn1.iso.acse.APTitle;
import org.openmuc.jdlms.internal.asn1.iso.acse.APTitleForm2;
import org.openmuc.jdlms.internal.asn1.iso.acse.ApplicationContextName;
import org.openmuc.jdlms.internal.asn1.iso.acse.AssociationInformation;
import org.openmuc.jdlms.internal.asn1.iso.acse.AuthenticationValue;
import org.openmuc.jdlms.internal.asn1.iso.acse.MechanismName;
import org.openmuc.jdlms.internal.asn1.iso.acse.RLRQApdu;
import org.openmuc.jdlms.internal.asn1.iso.acse.ReleaseRequestReason;
import org.openmuc.jdlms.internal.security.HlsProcessorGmac;
import org.openmuc.jdlms.internal.security.HlsSecretProcessor;
import org.openmuc.jdlms.sessionlayer.client.SessionLayer;
import org.openmuc.jdlms.sessionlayer.client.SessionLayerListener;
import org.openmuc.jdlms.settings.client.ReferencingMethod;
import org.openmuc.jdlms.settings.client.Settings;

/**
 * Class used to interact with a DLMS/Cosem Server.
 * 
 * @see ConnectionBuilder
 */
public abstract class DlmsConnection implements AutoCloseable {

    private static final int HIGH_PRIO_FLAG = 0x80;
    private static final int CONFIRMED_MODE_FLAG = 0x40;

    private final SessionLayer sessionLayer;
    private final APduBlockingQueue incomingApduQeue;

    private Set<ConformanceSetting> negotiatedFeatures;
    private final Settings settings;

    private byte[] serverSystemTitle;
    private byte[] buffer;

    private int frameCounter;
    private int invokeId;
    private int maxSendPduSize;

    private final ResponseQueue cosemResponseQ;

    DlmsConnection(Settings settings, SessionLayer sessionLayer) {
        this.settings = settings;

        this.sessionLayer = sessionLayer;

        this.incomingApduQeue = new APduBlockingQueue();

        this.maxSendPduSize = 0xffff;
        this.buffer = new byte[this.maxSendPduSize];
        this.invokeId = 1;
        this.frameCounter = 1;

        this.cosemResponseQ = new ResponseQueue();
    }

    /**
     * Convenience method to call {@code #get(false, List)}
     * 
     * 
     * @param params
     *            args of specifiers which attributes to send (See {@link AttributeAddress})
     * @return List of results from the smart meter in the same order as the requests
     * 
     * @throws IOException
     *             if the connection breaks, while requesting.
     *             <p>
     *             May be of type {@link FatalJDlmsException} or {@link ResponseTimeoutException}
     *             </p>
     * 
     * @see #get(boolean, List)
     */
    public final List<GetResult> get(List<AttributeAddress> params) throws IOException {
        return get(false, params);
    }

    /**
     * Requests the remote smart meter to send the values of several attributes.
     * 
     * <p>
     * Convenience method to call {@code #get(false, AttributeAddress)}.
     * </p>
     * 
     * @param attributeAddress
     *            specifiers which attributes to send (See {@link AttributeAddress})
     * @return single result from the meter.
     * 
     * @throws IOException
     *             if the connection breaks, while requesting.
     *             <p>
     *             May be of type {@link FatalJDlmsException} or {@link ResponseTimeoutException}
     *             </p>
     * 
     * @see #get(boolean, AttributeAddress)
     */
    public final GetResult get(AttributeAddress attributeAddress) throws IOException {
        return get(false, attributeAddress);
    }

    /**
     * Requests the remote smart meter to send the values of several attributes.
     * 
     * 
     * @param priority
     *            if true: sends this request with high priority, if supported
     * @param attributeAddress
     *            specifiers which attributes to send (See {@link AttributeAddress})
     * 
     * @return single results from the smart meter in the same order as the requests
     * 
     * @throws IOException
     *             if the connection breaks, while requesting.
     *             <p>
     *             May be of type {@link FatalJDlmsException} or {@link ResponseTimeoutException}
     *             </p>
     * 
     * @see #get(boolean, List)
     */
    public final GetResult get(boolean priority, AttributeAddress attributeAddress) throws IOException {
        List<GetResult> result = get(priority, Arrays.asList(attributeAddress));
        return result.isEmpty() ? new GetResult(AccessResultCode.OTHER_REASON) : result.get(0);
    }

    /**
     * Requests the remote smart meter to send the values of one or several attributes
     * 
     * @param priority
     *            if true: sends this request with high priority, if supported
     * @param params
     *            args of specifiers which attributes to send (See {@link AttributeAddress})
     * @return List of results from the smart meter in the same order as the requests
     * 
     * @throws IOException
     *             if the connection breaks, while requesting.
     *             <p>
     *             May be of type {@link FatalJDlmsException} or {@link ResponseTimeoutException}
     *             </p>
     * 
     */
    public abstract List<GetResult> get(boolean priority, List<AttributeAddress> params) throws IOException;

    /**
     * Requests the remote smart meter to set one attribute to the committed value.
     * 
     * <p>
     * Convenience method to call {@code set(false, SetParameter...)}.
     * </p>
     * 
     * @param params
     *            args of specifier which attributes to set to which values (See {@link SetParameter})
     * @return List of results from the smart meter in the same order as the requests or null if confirmed has been set
     *         to false on creation of this object. A true value indicates that this particular value has been
     *         successfully set
     * @throws IOException
     *             if the connection breaks, while requesting.
     *             <p>
     *             May be of type {@link FatalJDlmsException} or {@link ResponseTimeoutException}
     *             </p>
     * 
     * 
     * @see #set(boolean, List)
     */
    public final List<AccessResultCode> set(List<SetParameter> params) throws IOException {
        return set(false, params);
    }

    /**
     * Requests the remote smart meter to set one or several attributes to the committed values
     * 
     * @param priority
     *            Sends this request with high priority, if supported
     * @param params
     *            Varargs of specifier which attributes to set to which values (See {@link SetParameter})
     * @return List of results from the smart meter in the same order as the requests or null if confirmed has been set
     *         to false on creation of this object. A true value indicates that this particular value has been
     *         successfully set
     * @throws IOException
     *             if the connection breaks, while requesting.
     *             <p>
     *             May be of type {@link FatalJDlmsException} or {@link ResponseTimeoutException}
     *             </p>
     * 
     */
    public abstract List<AccessResultCode> set(boolean priority, List<SetParameter> params) throws IOException;

    /**
     * Requests the remote smart meter to set one attributes to the committed values.
     * 
     * @param priority
     *            Sends this request with high priority, if supported
     * @param setParameter
     *            Varargs of specifier which attributes to set to which values (See {@link SetParameter})
     * @return results from the smart meter in the same order as the requests or null if confirmed has been set to false
     *         on creation of this object. A true value indicates that this particular value has been successfully set
     * @throws IOException
     *             if the connection breaks, while requesting.
     *             <p>
     *             May be of type {@link FatalJDlmsException} or {@link ResponseTimeoutException}
     *             </p>
     * 
     */
    public final AccessResultCode set(boolean priority, SetParameter setParameter) throws IOException {
        List<AccessResultCode> result = set(priority, Arrays.asList(setParameter));
        return result.isEmpty() ? AccessResultCode.OTHER_REASON : result.get(0);
    }

    /**
     * Requests the remote smart meter to set one or several attributes to the committed values.
     * 
     * <p>
     * Convenience method to call {@code set(false, SetParameter)}
     * </p>
     * 
     * @param setParameter
     *            attribute and values (see {@link SetParameter})
     * @return results from the smart meter in the same order as the requests or null if confirmed has been set to false
     *         on creation of this object. A true value indicates that this particular value has been successfully set
     * @throws IOException
     *             if the connection breaks, while requesting.
     *             <p>
     *             May be of type {@link FatalJDlmsException} or {@link ResponseTimeoutException}
     *             </p>
     * 
     */
    public final AccessResultCode set(SetParameter setParameter) throws IOException {
        return set(false, setParameter);
    }

    /**
     * Requests the remote smart meter to call one methods with or without committed parameters.
     * 
     * @param priority
     *            Sends this request with high priority, if supported
     * 
     * @param methodParameter
     *            method to be called and, if needed, what parameters to call (See {@link MethodParameter}
     * @return results from the smart meter in the same order as the requests or null if confirmed has been set to false
     *         on creation of this object.
     * 
     * @throws IOException
     *             if the connection breaks, while requesting.
     *             <p>
     *             May be of type {@link FatalJDlmsException} or {@link ResponseTimeoutException}
     *             </p>
     */
    public final MethodResult action(boolean priority, MethodParameter methodParameter) throws IOException {
        List<MethodResult> action = action(priority, Arrays.asList(methodParameter));
        return action.isEmpty() ? new MethodResult(MethodResultCode.OTHER_REASON) : action.get(0);
    }

    /**
     * 
     * Requests the remote smart meter to call one methods with or without committed parameters.
     * <p>
     * Convenience method to call {@code action(false, methodParameter)}
     * </p>
     * 
     * @param methodParameter
     *            specifier which method to be called and, if needed, what parameters to call (See
     *            {@link MethodParameter}
     * @return results from the smart meter in the same order as the requests or null if confirmed has been set to false
     *         on creation of this object.
     * 
     * @throws IOException
     *             if the connection breaks, while requesting.
     *             <p>
     *             May be of type {@link FatalJDlmsException} or {@link ResponseTimeoutException}
     *             </p>
     */
    public final MethodResult action(MethodParameter methodParameter) throws IOException {
        return action(false, methodParameter);
    }

    /**
     * 
     * Convenience method to call {@code action(false, params)}
     * 
     * @param params
     *            List of specifier which methods to be called and, if needed, what parameters to call (See
     *            {@link MethodParameter}
     * 
     * @return List of results from the smart meter in the same order as the requests or null if confirmed has been set
     *         to false on creation of this object
     * 
     * @throws IOException
     *             if the connection breaks, while requesting.
     *             <p>
     *             May be of type {@link FatalJDlmsException} or {@link ResponseTimeoutException}
     *             </p>
     */
    public final List<MethodResult> action(List<MethodParameter> params) throws IOException {
        return action(false, params);
    }

    /**
     * Requests the remote smart meter to call one or several methods with or without committed parameters
     * 
     * @param priority
     *            Sends this request with high priority, if supported
     * @param params
     *            List of specifier which methods to be called and, if needed, what parameters to call (See
     *            {@link MethodParameter}
     * @return List of results from the smart meter in the same order as the requests or null if confirmed has been set
     *         to false on creation of this object
     * @throws IOException
     *             if the connection breaks, while requesting.
     *             <p>
     *             May be of type {@link FatalJDlmsException} or {@link ResponseTimeoutException}
     *             </p>
     */
    public abstract List<MethodResult> action(boolean priority, List<MethodParameter> params) throws IOException;

    /**
     * Disconnects gracefully from the server.
     * 
     * @throws IOException
     *             if an I/O Exception occurs while closing
     */
    public synchronized void disconnect() throws IOException {
        try {
            ReleaseReqReason releaseReason = ReleaseReqReason.NORMAL;

            ReleaseRequestReason reason = new ReleaseRequestReason(releaseReason.getCode());

            AssociationInformation userInformation = null;
            RLRQApdu rlrq = new RLRQApdu();
            rlrq.setReason(reason);
            rlrq.setUserInformation(userInformation);

            ACSEApdu acseApdu = new ACSEApdu();
            acseApdu.setRlrq(rlrq);

            COSEMpdu cosemPdu = null;

            APdu aPduOut = new APdu(acseApdu, cosemPdu);
            RawMessageDataBuilder rawMessageBuilder = newRawMessageDataBuilder();
            int length = unencryptedEncode(aPduOut, rawMessageBuilder);

            this.incomingApduQeue.clear();

            int offset = buffer.length - length;
            this.sessionLayer.send(buffer, offset, length, rawMessageBuilder);

            try {
                APdu aPdu = this.incomingApduQeue.take();
                if (aPdu == null) {
                    throw new ResponseTimeoutException("Disconnect timed out.");
                }

                if (aPdu == null || aPdu.getAcseAPdu() == null) {
                    throw new FatalJDlmsException(ExceptionId.CONNECTION_DISCONNECT_ERROR, Fault.SYSTEM,
                            "Server did not answer on disconnect");
                }
            } catch (InterruptedException e) {
                // ignore, shouldn't occur
                // Restore interrupted state...
                Thread.currentThread().interrupt();
            }

        } finally {
            close();
        }
    }

    /**
     * Closes the connection.
     * 
     * @throws IOException
     *             if an I/O Exception occurs while closing
     */
    @Override
    public void close() throws IOException {
        this.sessionLayer.close();
    }

    /**
     * Change the global authentication key used by the client.
     * 
     * @param key
     *            the new key
     */
    public void changeClientGlobalAuthenticationKey(byte[] key) {
        settings.updateAuthenticationKey(key);
    }

    /**
     * Change the global encryption used by the client.
     * 
     * @param key
     *            the new key
     */
    public void changeClientGlobalEncryptionKey(byte[] key) {
        settings.updateGlobalEncryptionKey(key);
    }

    void connect() throws IOException {

        this.sessionLayer.startListening(new SessionLayerListenerImpl());

        ContextId contextId = getContextId();

        HlsSecretProcessor hlsSecretProcessor = null;
        byte[] clientToServerChallenge = null;

        SecuritySuite securitySuite = settings.securitySuite();

        ApplicationContextName applicationContextName = applicationContextNameFrom(contextId);
        MechanismName mechanismName = mechanismNameFrom(settings.securitySuite().getAuthenticationMechanism());

        AARQApdu aarq = new AARQApdu();
        aarq.setApplicationContextName(applicationContextName);
        aarq.setMechanismName(mechanismName);

        AuthenticationMechanism authMechanism = securitySuite.getAuthenticationMechanism();
        switch (authMechanism) {
        case LOW:
            setupAarqAuthentication(aarq, securitySuite.getPassword());
        case NONE:
            break;

        case HLS5_GMAC:
            clientToServerChallenge = generateNewChallenge(settings.challengeLength());
            setupAarqAuthentication(aarq, clientToServerChallenge);
            hlsSecretProcessor = new HlsProcessorGmac();

            APTitle clientApTitle = new APTitle();
            clientApTitle.setApTitleForm2(new APTitleForm2(settings.systemTitle()));
            aarq.setCallingAPTitle(clientApTitle);
            break;
        default:
            // Should not happen
            String msg = MessageFormat.format("Authentication {0} mechanism not supported.", authMechanism);
            throw new IllegalArgumentException(msg);
        }

        ACSEApdu aarqAcseAPdu = new ACSEApdu();
        aarqAcseAPdu.setAarq(aarq);

        COSEMpdu xDlmsInitiateRequestPdu = new COSEMpdu();
        xDlmsInitiateRequestPdu.setinitiateRequest(newInitReq());

        APdu aarqAPdu = new APdu(aarqAcseAPdu, xDlmsInitiateRequestPdu);

        RawMessageDataBuilder rawMessageBuilder = newRawMessageDataBuilder();

        try {
            int length = encodeAPdu(aarqAPdu, rawMessageBuilder);
            this.sessionLayer.send(buffer, buffer.length - length, length, rawMessageBuilder);
        } catch (IOException e) {
            closeUnsafe();

            throw e;
        }

        processInitResponse(hlsSecretProcessor, clientToServerChallenge, waitForServerResponseAPdu());
    }

    Settings connectionSettings() {
        return this.settings;
    }

    abstract ContextId getContextId();

    boolean confirmedModeEnabled() {
        return true;
    }

    Set<ConformanceSetting> negotiatedFeatures() {
        return this.negotiatedFeatures;
    }

    /**
     * PDU size of zero implies, that the server does not indicate a limit.getResponseQueue
     * 
     * @return the maximum PDU size.
     */
    int maxSendPduSize() {
        return this.maxSendPduSize;
    }

    Invoke_Id_And_Priority invokeIdAndPriorityFor(boolean priority) {

        byte[] invokeIdAndPriorityBytes = new byte[] { (byte) (invokeId & 0xF) };
        if (confirmedModeEnabled()) {
            invokeIdAndPriorityBytes[0] |= CONFIRMED_MODE_FLAG;
        }
        if (priority) {
            invokeIdAndPriorityBytes[0] |= HIGH_PRIO_FLAG;
        }
        Invoke_Id_And_Priority result = new Invoke_Id_And_Priority(invokeIdAndPriorityBytes);

        invokeId = (invokeId + 1) % 16;
        return result;
    }

    @SuppressWarnings("unchecked")
    synchronized <T extends AxdrType> T send(COSEMpdu pdu) throws IOException {

        this.cosemResponseQ.clear();

        APdu aPdu = new APdu(null, pdu);
        RawMessageDataBuilder rawMessageBuilder = RawMessageData.builder().setMessageSource(MessageSource.CLIENT);

        int length = encodeAPdu(aPdu, rawMessageBuilder);

        int offset = buffer.length - length;
        this.sessionLayer.send(buffer, offset, length, rawMessageBuilder);

        COSEMpdu responsePdu = this.cosemResponseQ.poll();

        if (settings.referencingMethod() == ReferencingMethod.LOGICAL) {
            for (int i = 0; i < 3; i++) {
                int invokeId = PduHelper.invokeIdFrom(responsePdu);

                int prevId = (this.invokeId - 1) % 16;
                if (prevId < 0) {
                    prevId += 16;
                }
                if (invokeId == prevId) {
                    break;
                }

                responsePdu = this.cosemResponseQ.poll();
            }
        }

        switch (responsePdu.getChoiceIndex()) {
        case ACTION_RESPONSE:

            return (T) responsePdu.action_response;
        case GET_RESPONSE:
            return (T) responsePdu.get_response;
        case SET_RESPONSE:
            return (T) responsePdu.set_response;

        case READRESPONSE:
            return (T) responsePdu.readResponse;
        case WRITERESPONSE:
            return (T) responsePdu.writeResponse;

        default:
            throw new FatalJDlmsException(ExceptionId.ILLEGAL_RESPONSE, Fault.SYSTEM,
                    MessageFormat.format("The response type {0} was not expected.", responsePdu.getChoiceIndex()));
        }
    }

    abstract Set<ConformanceSetting> proposedConformance();

    abstract void validateReferencingMethod() throws IOException;

    abstract MethodResult hlsAuthentication(byte[] processedChallenge) throws IOException;

    abstract void processEventPdu(COSEMpdu pdu);

    private InitiateRequest newInitReq() {
        AxdrOctetString dedicatedKey = null;
        AxdrBoolean confirmedMode = new AxdrBoolean(true);
        Integer8 proposedQualityOfService = null;
        Unsigned8 propDlmsVersion = new Unsigned8(6);
        Conformance proposedConformance = conformanceFor(proposedConformance());
        Unsigned16 clientMaxReceivePduSize = new Unsigned16(0xFFFF);

        return new InitiateRequest(dedicatedKey, confirmedMode, proposedQualityOfService, propDlmsVersion,
                proposedConformance, clientMaxReceivePduSize);
    }

    private RawMessageDataBuilder newRawMessageDataBuilder() {
        if (settings.rawMessageListener() == null) {
            return null;
        }
        return RawMessageData.builder();
    }

    private int encodeAPdu(final APdu aPdu, final RawMessageDataBuilder rawMessageBuilder) throws IOException {
        final SecuritySuite securitySuite = settings.securitySuite();

        if (securitySuite.getEncryptionMechanism() != EncryptionMechanism.NONE) {
            return aPdu.encode(buffer, this.frameCounter++, settings.systemTitle(), securitySuite, rawMessageBuilder);
        }
        else {
            return unencryptedEncode(aPdu, rawMessageBuilder);
        }
    }

    private int unencryptedEncode(final APdu aPdu, final RawMessageDataBuilder rawMessageBuilder) throws IOException {
        return aPdu.encode(buffer, rawMessageBuilder);
    }

    private void processInitResponse(HlsSecretProcessor hlsSecretProcessor, byte[] clientToServerChallenge,
            APdu responseAPdu) throws IOException {

        validateConnectConfirm(responseAPdu);

        AAREApdu aare = responseAPdu.getAcseAPdu().getAare();

        if (settings.securitySuite().getAuthenticationMechanism().isHlsMechanism()) {
            this.serverSystemTitle = aare.getRespondingAPTitle().getApTitleForm2().value;
        }

        COSEMpdu xDlmsInitResponse = responseAPdu.getCosemPdu();

        InitiateResponse initiateResponse = xDlmsInitResponse.initiateResponse;

        this.maxSendPduSize = (int) initiateResponse.server_max_receive_pdu_size.getValue();

        if (this.maxSendPduSize == 0) {
            this.maxSendPduSize = 0xFFFF;
        }

        if (this.buffer.length < this.maxSendPduSize) {
            this.buffer = new byte[this.maxSendPduSize];
        }

        this.negotiatedFeatures = ConformanceSettingConverter
                .conformanceSettingFor(initiateResponse.negotiated_conformance);

        validateReferencingMethod();

        // Step 3 and 4 of HLS
        AuthenticationMechanism authenticationMechanism = settings.securitySuite().getAuthenticationMechanism();

        switch (authenticationMechanism) {
        case HLS5_GMAC:
            hls5Connect(hlsSecretProcessor, clientToServerChallenge, aare);

        case LOW:
        case NONE:
        default:
            break;
        }

    }

    private void hls5Connect(HlsSecretProcessor hlsSecretProcessor, byte[] clientToServerChallenge, AAREApdu aare)
            throws IOException {
        byte[] serverToClientChallenge = aare.getRespondingAuthenticationValue().getCharstring().value;

        SecuritySuite securitySuite = this.settings.securitySuite();
        byte[] clientSystemTitle = settings.systemTitle();

        byte[] processedChallenge = hlsSecretProcessor.process(serverToClientChallenge, securitySuite,
                clientSystemTitle, this.frameCounter);

        byte[] remoteChallenge = callHls5Auth(processedChallenge);

        int frameCounter = ByteBuffer.wrap(remoteChallenge, 1, 4).getInt();

        processedChallenge = hlsSecretProcessor.process(clientToServerChallenge, securitySuite, this.serverSystemTitle,
                frameCounter);

        if (!Arrays.equals(remoteChallenge, processedChallenge)) {
            String message = "Server could not authenticate itself. There may be a MIM.";
            throw new FatalJDlmsException(ExceptionId.AUTHENTICATION_ERROR, Fault.SYSTEM, message);
        }
    }

    private byte[] callHls5Auth(byte[] processedChallenge) throws IOException {
        MethodResult remoteResponse;
        try {
            remoteResponse = hlsAuthentication(processedChallenge);
        } catch (ResponseTimeoutException e) {
            throw new FatalJDlmsException(ExceptionId.CONNECTION_ESTABLISH_ERROR, Fault.SYSTEM,
                    "Server replied late in HLS exchange.", e);
        } catch (IOException e) {
            Fault assumedFault = Fault.USER;
            if (e instanceof JDlmsException) {
                assumedFault = ((JDlmsException) e).getAssumedFault();
            }
            throw new FatalJDlmsException(ExceptionId.AUTHENTICATION_ERROR, assumedFault,
                    "Exception during HLS authentication steps 3 and 4");
        }
        if (remoteResponse.getResultCode() != MethodResultCode.SUCCESS
                || remoteResponse.getResultData().getType() != Type.OCTET_STRING) {
            throw new FatalJDlmsException(ExceptionId.AUTHENTICATION_ERROR, Fault.USER,
                    "Failed to authenticate to server. HLS authentication step 4.");
        }
        return remoteResponse.getResultData().getValue();
    }

    private APdu waitForServerResponseAPdu() throws IOException {
        try {
            if (settings.responseTimeout() == 0) {
                return incomingApduQeue.take();
            }
            else {
                return incomingApduQeue.poll(settings.responseTimeout(), TimeUnit.MILLISECONDS);
            }
        } catch (InterruptedException e) {
            // Restore interrupted state...
            Thread.currentThread().interrupt();

            throw new IllegalStateException(e);
        }
    }

    private void validateConnectConfirm(APdu decodedResponsePdu) throws IOException {
        if (decodedResponsePdu == null) {
            String msg = "Timeout waiting for associate response message (AARE). No further information.";
            throw new FatalJDlmsException(CONNECTION_ESTABLISH_ERROR, SYSTEM, msg);
        }

        if (decodedResponsePdu.getAcseAPdu() == null || decodedResponsePdu.getAcseAPdu().getAare() == null) {
            String msg = "Did not receive expected associate response (AARE) message.";
            throw new FatalJDlmsException(CONNECTION_ESTABLISH_ERROR, SYSTEM, msg);
        }
    }

    private void closeUnsafe() {
        try {
            close();
        } catch (IOException e2) {
            // ignore.
        }
    }

    private static void setupAarqAuthentication(AARQApdu aarq, byte[] clientToServerChallenge) {
        aarq.setSenderAcseRequirements(new ACSERequirements(new byte[] { (byte) 0x80 }, 2));

        AuthenticationValue authenticationValue = new AuthenticationValue();
        authenticationValue.setCharstring(new BerOctetString(clientToServerChallenge));
        aarq.setCallingAuthenticationValue(authenticationValue);

    }

    private class SessionLayerListenerImpl implements SessionLayerListener {
        @Override
        public void dataReceived(byte[] data, RawMessageDataBuilder rawMessageBuilder) {
            APdu aPdu;
            try {
                aPdu = encodeData(data, rawMessageBuilder);

                COSEMpdu cosemPdu = aPdu.getCosemPdu();

                if (cosemPdu != null && checkForErrors(cosemPdu)) {
                    return;
                }

            } catch (IOException e) {
                errorOccurred(e);
                return;
            }

            RawMessageListener rawMessageListener = settings.rawMessageListener();
            if (rawMessageListener != null) {
                RawMessageData rawMessageData = rawMessageBuilder.setMessageSource(MessageSource.SERVER).build();
                rawMessageListener.messageCaptured(rawMessageData);
            }

            if (aPdu.getAcseAPdu() != null) {
                incomingApduQeue.put(aPdu);
            }
            else {
                COSEMpdu cosemPdu = aPdu.getCosemPdu();

                switch (cosemPdu.getChoiceIndex()) {
                // LN referencing
                case ACTION_RESPONSE:
                case GET_RESPONSE:
                case SET_RESPONSE:

                    // SN referencing
                case READRESPONSE:
                case WRITERESPONSE:
                    cosemResponseQ.put(cosemPdu);
                    break;

                case EVENT_NOTIFICATION_REQUEST:
                case INFORMATIONREPORTREQUEST:
                    processEventPdu(cosemPdu);

                default:
                    // else do ignore..
                    break;
                }
            }

        }

        private APdu encodeData(byte[] data, RawMessageDataBuilder rawMessageBuilder) throws IOException {
            SecuritySuite securitySuite = settings.securitySuite();

            APdu aPdu;
            if (securitySuite.getEncryptionMechanism() != EncryptionMechanism.NONE) {
                aPdu = APdu.decode(data, serverSystemTitle, frameCounter, securitySuite, rawMessageBuilder);
            }
            else {
                aPdu = APdu.decode(data, rawMessageBuilder);
            }
            return aPdu;
        }

        @Override
        public void connectionInterrupted(IOException e) {
            errorOccurred(e);
        }

        private boolean checkForErrors(COSEMpdu cosemPdu) {
            COSEMpdu.Choices choiceIndex = cosemPdu.getChoiceIndex();
            if (choiceIndex == COSEMpdu.Choices.EXCEPTION_RESPONSE) {
                EXCEPTION_Response exceptionResponse = cosemPdu.exception_response;
                errorOccurred(new IOException(exceptionResponse.toString()));
                return true;
            }
            else if (choiceIndex == COSEMpdu.Choices.CONFIRMEDSERVICEERROR) {
                ConfirmedServiceError confirmedServiceError = cosemPdu.confirmedServiceError;
                errorOccurred(new IOException(confirmedServiceError.toString()));
                return true;
            }
            else {
                return false;
            }
        }

        private void errorOccurred(IOException ex) {
            if (cosemResponseQ.beingPolled()) {
                cosemResponseQ.putError(ex);
            }
            else {
                incomingApduQeue.putError(ex);
            }

        }

    }

}
