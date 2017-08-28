package org.openmuc.jdlms;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openmuc.jasn1.ber.BerByteArrayOutputStream;
import org.openmuc.jdlms.SecuritySuite.EncryptionMechanism;
import org.openmuc.jdlms.SecuritySuite.SecurityPolicy;
import org.openmuc.jdlms.SecuritySuite.SecuritySuiteBuilder;
import org.openmuc.jdlms.internal.APdu;
import org.openmuc.jdlms.internal.asn1.axdr.types.AxdrBoolean;
import org.openmuc.jdlms.internal.asn1.axdr.types.AxdrOctetString;
import org.openmuc.jdlms.internal.asn1.cosem.COSEMpdu;
import org.openmuc.jdlms.internal.asn1.cosem.Conformance;
import org.openmuc.jdlms.internal.asn1.cosem.Cosem_Attribute_Descriptor;
import org.openmuc.jdlms.internal.asn1.cosem.Get_Request_Normal;
import org.openmuc.jdlms.internal.asn1.cosem.InitiateRequest;
import org.openmuc.jdlms.internal.asn1.cosem.Unsigned16;
import org.openmuc.jdlms.internal.asn1.cosem.Unsigned8;
import org.openmuc.jdlms.internal.security.GcmModule;

public class CipheringTest {

    private static final byte[] SYSTEM_TITLE = HexConverter.fromShortHexString("4D4D4D0000BC614E");
    private static SecuritySuiteBuilder securitySuiteBuilder;
    final byte[] dedicatedKey = HexConverter.fromShortHexString("00112233445566778899AABBCCDDEEFF");

    @BeforeClass
    public static void init() {
        final byte[] globalCipherKey = HexConverter.fromShortHexString("000102030405060708090A0B0C0D0E0F");
        final byte[] authenticationKey = HexConverter.fromShortHexString("D0D1D2D3D4D5D6D7D8D9DADBDCDDDEDF");
        securitySuiteBuilder = SecuritySuite.builder()
                .setGlobalUnicastEncryptionKey(globalCipherKey)
                .setAuthenticationKey(authenticationKey)
                .setEncryptionMechanism(EncryptionMechanism.AES_GMC_128)
                .setAuthenticationMechanism(AuthenticationMechanism.HLS5_GMAC);
    }

    @Test
    public void test_authenticated_and_enrcypted_apdu() throws Exception {
        byte[] data = HexConverter.fromShortHexString("C0010000080000010000FF0200");
        assertEquals(13, data.length);

        APdu aPdu = APdu.decode(data, RawMessageData.builder());

        COSEMpdu cosemPdu = aPdu.getCosemPdu();
        assertEquals(COSEMpdu.Choices.GET_REQUEST, cosemPdu.getChoiceIndex());
        Get_Request_Normal normalReq = cosemPdu.get_request.get_request_normal;
        Cosem_Attribute_Descriptor descriptor = normalReq.cosem_attribute_descriptor;
        assertEquals(8, descriptor.class_id.getValue());
        assertEquals(2, descriptor.attribute_id.getValue());

        SecuritySuite securitySuite = securitySuiteBuilder.setSecurityPolicy(SecurityPolicy.AUTHENTICATED_AND_ENCRYPTED)
                .build();

        byte[] encryptedApdu = GcmModule.processPlain(data, 0, data.length, SYSTEM_TITLE, 0x1234567, securitySuite,
                (byte) 0xC8);

        byte[] expecteds = HexConverter
                .fromShortHexString("C81E3001234567411312FF935A47566827C467BC7D825C3BE4A77C3FCC056B6B");
        assertArrayEquals(expecteds, encryptedApdu);
    }

    @Test
    public void test_enrcypted_apdu() throws Exception {
        byte[] data = HexConverter.fromShortHexString("C0010000080000010000FF0200");

        SecuritySuite securitySuite = securitySuiteBuilder.setSecurityPolicy(SecurityPolicy.ENCRYPTED).build();

        byte[] encryptedApdu = GcmModule.processPlain(data, 0, data.length, SYSTEM_TITLE, 0x1234567, securitySuite,
                (byte) 0xC8);

        byte[] expecteds = HexConverter.fromShortHexString("C8122001234567411312FF935A47566827C467BC");
        assertArrayEquals(expecteds, encryptedApdu);
    }

    @Test
    public void testCipheringExampleFromStandard()
            throws IOException, IllegalStateException, InvalidCipherTextException {

        // create xDLMS plain text byte array

        byte[] initRequestXDlmsPduBytes = getInitRequestXDlmsPduBytes();

        byte[] ciphered = GcmModule.processPlain(initRequestXDlmsPduBytes, 0, initRequestXDlmsPduBytes.length,
                SYSTEM_TITLE, 0x1234567, securitySuiteBuilder.build(), (byte) 33);

        assertArrayEquals(getCipherdFromStandard(), ciphered);

    }

    @Test
    public void testDecryptionExampleFromStandard() throws IOException {

        // String cipheredFromStandardString = "3001234567"
        // + "801302FF8A7874133D414CED25B42534D28DB0047720606B175BD52211BE68" + "41DB204D39EE6FDB8E356855";

        byte[] cipheredFromStandard = HexConverter.fromShortHexString("3001234567"
                + "801302FF8A7874133D414CED25B42534D28DB0047720606B175BD52211BE68" + "41DB204D39EE6FDB8E356855");
        byte[] initRequestXDlmsPduBytes = getInitRequestXDlmsPduBytes();

        byte[] deciphered = GcmModule.decrypt(cipheredFromStandard, SYSTEM_TITLE, securitySuiteBuilder.build());
        assertArrayEquals(initRequestXDlmsPduBytes, deciphered);

    }

    private byte[] getInitRequestXDlmsPduBytes() throws IOException {

        byte[] conformanceBlock = HexConverter.fromShortHexString("007E1F");
        int clientMaxReceivePduSize = 1200;

        COSEMpdu initRequestXDlmsPdu = new COSEMpdu();
        initRequestXDlmsPdu.setinitiateRequest(
                new InitiateRequest(new AxdrOctetString(dedicatedKey), new AxdrBoolean(true), null, new Unsigned8(6),
                        new Conformance(conformanceBlock, 24), new Unsigned16(clientMaxReceivePduSize)));

        BerByteArrayOutputStream abaos = new BerByteArrayOutputStream(1000);
        initRequestXDlmsPdu.encode(abaos);

        return abaos.getArray();
    }

    private byte[] getCipherdFromStandard() {

        String cipheredFromStandardString = "21303001234567"
                + "801302FF8A7874133D414CED25B42534D28DB0047720606B175BD52211BE68" + "41DB204D39EE6FDB8E356855";
        byte[] cipheredFromStandard = HexConverter.fromShortHexString(cipheredFromStandardString);
        return cipheredFromStandard;
    }

    @Test
    public void testKaifaMessages() throws IOException {

        ByteArrayInputStream bais = new ByteArrayInputStream(
                HexConverter.fromShortHexString("c301c1000f0000280000ff0101091110012345677d52391c407e3a45210f000e"));
        COSEMpdu actionRequestPdu = new COSEMpdu();
        actionRequestPdu.decode(bais);
        // System.out.println("action request pdu from kaifa meter: " + actionRequestPdu);

        bais = new ByteArrayInputStream(
                HexConverter.fromShortHexString("c701c10001000911101a9190bc0a587217bcd2bcd4fddb7143"));
        COSEMpdu actionResponsePdu = new COSEMpdu();
        actionResponsePdu.decode(bais);
        // System.out.println("action response pdu from kaifa meter: " + actionResponsePdu);

    }
}
