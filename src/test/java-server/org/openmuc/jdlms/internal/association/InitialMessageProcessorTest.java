package org.openmuc.jdlms.internal.association;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.openmuc.jdlms.AuthenticationMechanism.HLS5_GMAC;
import static org.openmuc.jdlms.AuthenticationMechanism.LOW;
import static org.openmuc.jdlms.AuthenticationMechanism.NONE;
import static org.openmuc.jdlms.ConformanceSetting.ACTION;
import static org.openmuc.jdlms.ConformanceSetting.GET;
import static org.openmuc.jdlms.ConformanceSetting.SET;
import static org.openmuc.jdlms.internal.ConformanceSettingConverter.conformanceSettingFor;
import static org.openmuc.jdlms.internal.ContextId.LOGICAL_NAME_REFERENCING_NO_CIPHERING;
import static org.openmuc.jdlms.internal.ObjectIdentifier.applicationContextNameFrom;
import static org.openmuc.jdlms.internal.ObjectIdentifier.mechanismNameFrom;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.spy;
import static org.powermock.api.mockito.PowerMockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmuc.jasn1.ber.types.BerOctetString;
import org.openmuc.jdlms.ConformanceSetting;
import org.openmuc.jdlms.LogicalDevice;
import org.openmuc.jdlms.RawMessageData.RawMessageDataBuilder;
import org.openmuc.jdlms.SecuritySuite;
import org.openmuc.jdlms.internal.APdu;
import org.openmuc.jdlms.internal.AssociationResult;
import org.openmuc.jdlms.internal.DataDirectoryImpl.CosemLogicalDevice;
import org.openmuc.jdlms.internal.ServerConnectionData;
import org.openmuc.jdlms.internal.asn1.cosem.COSEMpdu;
import org.openmuc.jdlms.internal.asn1.cosem.InitiateRequest;
import org.openmuc.jdlms.internal.asn1.cosem.InitiateResponse;
import org.openmuc.jdlms.internal.asn1.cosem.Unsigned16;
import org.openmuc.jdlms.internal.asn1.iso.acse.AARQApdu;
import org.openmuc.jdlms.internal.asn1.iso.acse.ACSEApdu;
import org.openmuc.jdlms.internal.asn1.iso.acse.AuthenticationValue;
import org.openmuc.jdlms.internal.asn1.iso.acse.MechanismName;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(APdu.class)
public class InitialMessageProcessorTest {

    @Test(expected = AssociatRequestException.class)
    public void test_authmechanism_mismatch() throws Exception {

        SecuritySuite secSuite = mock(SecuritySuite.class);
        when(secSuite.getAuthenticationMechanism()).thenReturn(HLS5_GMAC);

        ServerConnectionData cd = mock(ServerConnectionData.class);
        cd.clientId = 16;

        Map<Integer, SecuritySuite> restrictions = new HashMap<>();
        restrictions.put(cd.clientId, secSuite);

        LogicalDevice ld = mock(LogicalDevice.class);
        when(ld.getRestrictions()).thenReturn(restrictions);

        CosemLogicalDevice cLd = mock(CosemLogicalDevice.class);
        when(cLd.getLogicalDevice()).thenReturn(ld);

        mockApduDecodeMethods(null, null);
        InitiateMessageProcessor processor = spy(new InitiateMessageProcessor(cd, cLd));
        processor.processInitialMessage(null);
    }

    @Test()
    public void test_authmechanism_none() throws Exception {

        SecuritySuite secSuite = mock(SecuritySuite.class);
        when(secSuite.getAuthenticationMechanism()).thenReturn(NONE);

        ServerConnectionData cd = mock(ServerConnectionData.class);
        cd.clientId = 16;

        Map<Integer, SecuritySuite> restrictions = new HashMap<>();
        restrictions.put(cd.clientId, secSuite);

        LogicalDevice ld = mock(LogicalDevice.class);
        when(ld.getRestrictions()).thenReturn(restrictions);

        CosemLogicalDevice cLd = mock(CosemLogicalDevice.class);
        when(cLd.getLogicalDevice()).thenReturn(ld);

        mockApduDecodeMethods(null, null);
        InitiateMessageProcessor processor = spy(new InitiateMessageProcessor(cd, cLd));
        APdu res = processor.processInitialMessage(null);
        assertEquals(AssociationResult.ACCEPTED.getValue(), res.getAcseAPdu().getAare().getResult().longValue());
    }

    @Test(expected = AssociatRequestException.class)
    public void test_wrong_client_Id() throws Exception {

        SecuritySuite secSuite = mock(SecuritySuite.class);
        when(secSuite.getAuthenticationMechanism()).thenReturn(NONE);

        ServerConnectionData cd = mock(ServerConnectionData.class);
        cd.clientId = 16;

        Map<Integer, SecuritySuite> restrictions = new HashMap<>();
        restrictions.put(17, secSuite);

        LogicalDevice ld = mock(LogicalDevice.class);
        when(ld.getRestrictions()).thenReturn(restrictions);

        CosemLogicalDevice cLd = mock(CosemLogicalDevice.class);
        when(cLd.getLogicalDevice()).thenReturn(ld);

        mockApduDecodeMethods(null, null);
        InitiateMessageProcessor processor = spy(new InitiateMessageProcessor(cd, cLd));
        processor.processInitialMessage(null);
    }

    @Test
    public void test_sec_suite_low() throws Exception {
        byte[] password = "HelloWold".getBytes();

        SecuritySuite secSuite = mock(SecuritySuite.class);
        when(secSuite.getAuthenticationMechanism()).thenReturn(LOW);
        when(secSuite.getPassword()).thenReturn(password);

        ServerConnectionData cd = mock(ServerConnectionData.class);
        cd.clientId = 16;

        Map<Integer, SecuritySuite> restrictions = new HashMap<>();
        restrictions.put(cd.clientId, secSuite);

        LogicalDevice ld = mock(LogicalDevice.class);
        Set<ConformanceSetting> conformance = new HashSet<>(Arrays.asList(GET, SET, ACTION));
        when(ld.getConformance()).thenReturn(conformance);
        when(ld.getRestrictions()).thenReturn(restrictions);

        CosemLogicalDevice cLd = mock(CosemLogicalDevice.class);
        when(cLd.getLogicalDevice()).thenReturn(ld);

        mockApduDecodeMethods(mechanismNameFrom(LOW), password);
        InitiateMessageProcessor processor = spy(new InitiateMessageProcessor(cd, cLd));

        APdu result = processor.processInitialMessage(null);

        ACSEApdu acseAPdu = result.getAcseAPdu();
        COSEMpdu cosemPdu = result.getCosemPdu();
        assertNotNull(acseAPdu.getAare());

        assertEquals(AssociationResult.ACCEPTED.getValue(), acseAPdu.getAare().getResult().intValue());
        assertEquals(COSEMpdu.Choices.INITIATERESPONSE, cosemPdu.getChoiceIndex());
        InitiateResponse initiateResponse = cosemPdu.initiateResponse;
        assertEquals(conformance, conformanceSettingFor(initiateResponse.negotiated_conformance));
        assertEquals(6L, initiateResponse.negotiated_dlms_version_number.getValue());
    }

    @Test(expected = AssociatRequestException.class)
    public void test_sec_suite_low_wrong_pw() throws Exception {
        byte[] password = "HelloWold".getBytes();

        SecuritySuite secSuite = mock(SecuritySuite.class);
        when(secSuite.getAuthenticationMechanism()).thenReturn(LOW);
        when(secSuite.getPassword()).thenReturn(password);

        ServerConnectionData cd = mock(ServerConnectionData.class);
        cd.clientId = 16;

        Map<Integer, SecuritySuite> restrictions = new HashMap<>();
        restrictions.put(cd.clientId, secSuite);

        LogicalDevice ld = mock(LogicalDevice.class);
        Set<ConformanceSetting> conformance = new HashSet<>(Arrays.asList(GET, SET, ACTION));
        when(ld.getConformance()).thenReturn(conformance);
        when(ld.getRestrictions()).thenReturn(restrictions);

        CosemLogicalDevice cLd = mock(CosemLogicalDevice.class);
        when(cLd.getLogicalDevice()).thenReturn(ld);

        mockApduDecodeMethods(mechanismNameFrom(LOW), "guessedPW".getBytes());
        InitiateMessageProcessor processor = spy(new InitiateMessageProcessor(cd, cLd));

        processor.processInitialMessage(null);
    }

    private void mockApduDecodeMethods(MechanismName mechanismName, byte[] password) throws IOException {
        ACSEApdu acseAPdu = new ACSEApdu();
        AARQApdu aarq = new AARQApdu();
        aarq.setApplicationContextName(applicationContextNameFrom(LOGICAL_NAME_REFERENCING_NO_CIPHERING));
        aarq.setMechanismName(mechanismName);
        AuthenticationValue authenticationValue = new AuthenticationValue();
        authenticationValue.setCharstring(new BerOctetString(password));
        aarq.setCallingAuthenticationValue(authenticationValue);
        acseAPdu.setAarq(aarq);

        COSEMpdu cosemPdu = new COSEMpdu();
        InitiateRequest initReq = new InitiateRequest();
        initReq.client_max_receive_pdu_size = new Unsigned16(0);
        cosemPdu.setinitiateRequest(initReq);
        APdu resultApdu = new APdu(acseAPdu, cosemPdu);
        mockStatic(APdu.class);

        when(APdu.decode(any(byte[].class), any(RawMessageDataBuilder.class))).thenReturn(resultApdu);

        when(APdu.decode(any(byte[].class), any(byte[].class), anyInt(), any(SecuritySuite.class),
                any(RawMessageDataBuilder.class))).thenReturn(resultApdu);
    }

}
