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
package org.openmuc.jdlms.internal;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.text.MessageFormat;

import org.openmuc.jasn1.ber.BerByteArrayOutputStream;
import org.openmuc.jdlms.FatalJDlmsException;
import org.openmuc.jdlms.JDlmsException.ExceptionId;
import org.openmuc.jdlms.JDlmsException.Fault;
import org.openmuc.jdlms.RawMessageData;
import org.openmuc.jdlms.RawMessageData.Apdu;
import org.openmuc.jdlms.RawMessageData.CosemPdu;
import org.openmuc.jdlms.RawMessageData.RawMessageDataBuilder;
import org.openmuc.jdlms.SecuritySuite;
import org.openmuc.jdlms.internal.AssociateSourceDiagnostic.AcseServiceProvider;
import org.openmuc.jdlms.internal.AssociateSourceDiagnostic.AcseServiceUser;
import org.openmuc.jdlms.internal.asn1.axdr.AxdrLength;
import org.openmuc.jdlms.internal.asn1.cosem.COSEMpdu;
import org.openmuc.jdlms.internal.asn1.iso.acse.ACSEApdu;
import org.openmuc.jdlms.internal.asn1.iso.acse.AssociateSourceDiagnostic;
import org.openmuc.jdlms.internal.asn1.iso.acse.AssociationInformation;
import org.openmuc.jdlms.internal.association.AssociatRequestException;
import org.openmuc.jdlms.internal.security.GcmModule;

public class APdu {
    private static final SecuritySuite DEFAULT_SECURITY_SUITE = SecuritySuite.builder().build();

    private ACSEApdu acseAPdu;
    private COSEMpdu cosemPdu;

    private APdu() {
    }

    public APdu(ACSEApdu acseAPdu, COSEMpdu cosemPdu) {
        this.acseAPdu = acseAPdu;
        this.cosemPdu = cosemPdu;
    }

    public ACSEApdu getAcseAPdu() {
        return acseAPdu;
    }

    public COSEMpdu getCosemPdu() {
        return cosemPdu;
    }

    public static APdu decode(byte[] bytes, byte[] serverSystemTitle, int frameCounter, SecuritySuite securitySuite,
            RawMessageDataBuilder rawMessageBuilder) throws IOException {
        return decode(bytes, true, serverSystemTitle, frameCounter, securitySuite, rawMessageBuilder);
    }

    public static APdu decode(byte[] bytes, RawMessageDataBuilder rawMessageBuilder) throws IOException {
        return decode(bytes, false, null, 0, DEFAULT_SECURITY_SUITE, rawMessageBuilder);
    }

    public boolean isEncrypted() {
        switch (this.cosemPdu.getChoiceIndex()) {
        case GLO_ACTION_REQUEST:
        case GLO_ACTION_RESPONSE:
        case GLO_EVENT_NOTIFICATION_REQUEST:
        case GLO_GET_REQUEST:
        case GLO_GET_RESPONSE:
        case GLO_INITIATEREQUEST:
        case GLO_INITIATERESPONSE:
        case GLO_READREQUEST:
        case GLO_READRESPONSE:
        case GLO_SET_REQUEST:
        case GLO_SET_RESPONSE:
        case GLO_WRITEREQUEST:
        case GLO_WRITERESPONSE:
        case DED_ACTIONREQUEST:
        case DED_ACTION_RESPONSE:
        case DED_EVENT_NOTIFICATION_REQUEST:
        case DED_GET_REQUEST:
        case DED_GET_RESPONSE:
        case DED_SET_REQUEST:
        case DED_SET_RESPONSE:
            return true;

        case ACTION_REQUEST:
        case ACTION_RESPONSE:
        case CONFIRMEDSERVICEERROR:
        case EVENT_NOTIFICATION_REQUEST:
        case EXCEPTION_RESPONSE:
        case GET_REQUEST:
        case GET_RESPONSE:
        case INFORMATIONREPORTREQUEST:
        case INITIATEREQUEST:
        case INITIATERESPONSE:
        case READREQUEST:
        case READRESPONSE:
        case SET_REQUEST:
        case SET_RESPONSE:
        case UNCONFIRMEDWRITEREQUEST:
        case WRITEREQUEST:
        case WRITERESPONSE:
        case _ERR_NONE_SELECTED:
        default:
            return false;
        }
    }

    private static APdu decode(byte[] bytes, boolean encrypt, byte[] provServerSysT, int frameCounter,
            SecuritySuite securitySuite, RawMessageData.RawMessageDataBuilder rawMessageBuilder) throws IOException {
        DataInputStream is = new DataInputStream(new ByteArrayInputStream(bytes));

        APdu aPdu = new APdu();

        byte[] serverSysT = provServerSysT;

        final int tag = bytes[0] & 0xFF;
        if (tag >= 0x60 && tag <= 0x63) {
            aPdu.acseAPdu = new ACSEApdu();
            ACSEApdu acseAPdu = aPdu.acseAPdu;
            acseAPdu.decode(is, null);

            validateAssociateResult(aPdu);

            if (acseAPdu.getAarq() != null) {
                is = new DataInputStream(new ByteArrayInputStream(acseAPdu.getAarq().getUserInformation().value));
            }
            else if (acseAPdu.getAare() != null) {
                byte[] userInfo = acseAPdu.getAare().getUserInformation().value;

                /* Workaround for E650 LGZ bug */
                if (is.available() > 0) {
                    byte[] remaining = new byte[is.available()];
                    is.readFully(remaining);
                    userInfo = ByteBuffer.allocate(userInfo.length + remaining.length)
                            .put(userInfo)
                            .put(remaining)
                            .array();
                }
                is = new DataInputStream(new ByteArrayInputStream(userInfo));
            }

            if (encrypt) {
                if (acseAPdu.getAare() != null && acseAPdu.getAare().getRespondingAPTitle() != null) {
                    serverSysT = acseAPdu.getAare().getRespondingAPTitle().getApTitleForm2().value;
                }
                else if (acseAPdu.getAarq() != null) {
                    ContextId contextId = ObjectIdentifier
                            .applicationContextIdFrom(acseAPdu.getAarq().getApplicationContextName());

                    if (!contextId.isCiphered()) {
                        throw new AssociatRequestException(AcseServiceUser.APPLICATION_CONTEXT_NAME_NOT_SUPPORTED);
                    }

                    if (acseAPdu.getAarq().getCallingAPTitle() != null) {
                        serverSysT = acseAPdu.getAarq().getCallingAPTitle().getApTitleForm2().value;

                    }
                }
            }
            if (acseAPdu.getRlrq() != null || acseAPdu.getRlre() != null) {
                return aPdu;
            }

        }

        byte[] ciphertext = null;
        byte[] plaintext = null;

        InputStream cosemPduIs = is;
        if (encrypt) {

            is.read();

            AxdrLength axdrLength = new AxdrLength();
            axdrLength.decode(is);
            int encLength = axdrLength.getValue();

            ciphertext = new byte[encLength];
            is.readFully(ciphertext);

            plaintext = GcmModule.decrypt(ciphertext, serverSysT, securitySuite);

            cosemPduIs = new ByteArrayInputStream(plaintext);
        }

        int size = cosemPduIs.available();
        if (size > 0) {
            aPdu.cosemPdu = new COSEMpdu();
            aPdu.cosemPdu.decode(cosemPduIs);
        }

        setDataToBuilder(aPdu, rawMessageBuilder, ciphertext, plaintext);

        return aPdu;
    }

    private static void validateAssociateResult(APdu aPdu) throws FatalJDlmsException {
        ACSEApdu acseAPdu = aPdu.acseAPdu;
        if (acseAPdu.getAare() == null) {
            return;
        }

        AssociationResult associationResult = AssociationResult
                .associationResultFor(acseAPdu.getAare().getResult().value.longValue());

        if (associationResult == AssociationResult.ACCEPTED) {
            return;
        }

        AssociateSourceDiagnostic resultSourceDiagnostic = acseAPdu.getAare().getResultSourceDiagnostic();
        Fault faut;
        ExceptionId exceptionId;
        if (resultSourceDiagnostic.getAcseServiceUser() != null) {
            faut = Fault.USER;
            AcseServiceUser serviceUser = AcseServiceUser
                    .acseServiceUserFor(resultSourceDiagnostic.getAcseServiceUser().value.longValue());

            switch (serviceUser) {
            case APPLICATION_CONTEXT_NAME_NOT_SUPPORTED:
                exceptionId = ExceptionId.WRONG_REFERENCING_METHOD;
                break;

            case AUTHENTICATION_FAILURE:
            case AUTHENTICATION_MECHANISM_NAME_NOT_RECOGNISED:
            case AUTHENTICATION_MECHANISM_NAME_REQUIRED:
                exceptionId = ExceptionId.AUTHENTICATION_ERROR;
                break;// TODO: genauer

            case AUTHENTICATION_REQUIRED:
                exceptionId = ExceptionId.AUTHENTICATION_REQUIRED;
                break;

            default:
            case NO_REASON_GIVEN:
            case NULL:
                exceptionId = ExceptionId.UNKNOWN;
                break;
            }
        }
        else {
            faut = Fault.SYSTEM;
            AcseServiceProvider acseServiceProvider = AcseServiceProvider
                    .acseServiceProviderFor(resultSourceDiagnostic.getAcseServiceProvider().value.longValue());

            switch (acseServiceProvider) {
            case NO_COMMON_ACSE_VERSION:
            case NO_REASON_GIVEN:
            case NULL:
            default:
                exceptionId = ExceptionId.UNKNOWN;
                break;
            }
        }
        String message = MessageFormat.format(
                "Received an association response (AARE) with an error message. Result name {0}.", associationResult);
        throw new FatalJDlmsException(exceptionId, faut, message);
    }

    public int encode(byte[] buffer, int frameCounter, byte[] systemTitle, SecuritySuite securitySuite,
            RawMessageDataBuilder rawMessageBuilder) throws IOException {
        BerByteArrayOutputStream baos = new BerByteArrayOutputStream(buffer, buffer.length - 1);
        int numBytesEncoded = encodeCosemPdu(buffer, baos);

        // -- encrypting
        int origTag = buffer[buffer.length - numBytesEncoded] & 0xff;
        int newTag;

        if (origTag < 25) {
            newTag = origTag + 32;
        }
        else {
            newTag = origTag + 8;
        }
        byte[] ciphertext = GcmModule.processPlain(buffer, buffer.length - numBytesEncoded, numBytesEncoded,
                systemTitle, frameCounter, securitySuite, (byte) newTag);

        numBytesEncoded = ciphertext.length;

        System.arraycopy(ciphertext, 0, buffer, buffer.length - numBytesEncoded, ciphertext.length);
        baos = new BerByteArrayOutputStream(buffer, buffer.length - numBytesEncoded - 1);
        // -- encrypting

        numBytesEncoded = encodeAcsePdu(numBytesEncoded, baos);
        setDataToBuilder(this, rawMessageBuilder, ciphertext, null);
        return numBytesEncoded;
    }

    public int encode(byte[] buffer, RawMessageDataBuilder rawMessageBuilder) throws IOException {
        BerByteArrayOutputStream baos = new BerByteArrayOutputStream(buffer, buffer.length - 1);
        int numBytesEncoded = encodeCosemPdu(buffer, baos);

        numBytesEncoded = encodeAcsePdu(numBytesEncoded, baos);

        setDataToBuilder(this, rawMessageBuilder, null, null);

        return numBytesEncoded;
    }

    private int encodeCosemPdu(byte[] buffer, BerByteArrayOutputStream baos) throws IOException {
        return cosemPdu == null ? 0 : cosemPdu.encode(baos);
    }

    private static void setDataToBuilder(APdu apdu, RawMessageDataBuilder rawMessageBuilder, byte[] ciphertext,
            byte[] provPlaintext) {
        if (rawMessageBuilder == null) {
            return;
        }
        byte[] plaintext = provPlaintext;
        try {

            if (provPlaintext == null && apdu.cosemPdu != null) {
                BerByteArrayOutputStream axdrOStream = new BerByteArrayOutputStream(50, true);
                apdu.cosemPdu.encode(axdrOStream);
                plaintext = axdrOStream.getArray();
            }

            BerByteArrayOutputStream axdrOStream2 = new BerByteArrayOutputStream(50, true);

            byte[] acseAPduData = null;
            if (apdu.acseAPdu != null) {
                apdu.acseAPdu.encode(axdrOStream2);
                acseAPduData = axdrOStream2.getArray();
            }
            CosemPdu rawCosemPdu = null;
            if (apdu.cosemPdu != null) {
                rawCosemPdu = new CosemPdu(ciphertext, plaintext);
            }

            Apdu rawApdu = new Apdu(rawCosemPdu, acseAPduData);

            rawMessageBuilder.setApdu(rawApdu);
        } catch (IOException e) {
            // ignore, since this shoundn't occur
        }

    }

    private int encodeAcsePdu(int numBytesEncoded, BerByteArrayOutputStream baos) throws IOException {
        if (acseAPdu != null) {
            if (acseAPdu.getAarq() != null) {
                acseAPdu.getAarq().setUserInformation(new AssociationInformation(baos.getArray()));
            }
            else if (acseAPdu.getAare() != null) {
                acseAPdu.getAare().setUserInformation(new AssociationInformation(baos.getArray()));
            }

            baos.reset();
            numBytesEncoded = acseAPdu.encode(baos);
        }
        return numBytesEncoded;
    }

    @Override
    public String toString() {
        return new StringBuilder().append("ACSE PDU: ")
                .append(acseAPdu)
                .append(", ")
                .append("COSEM xDLMS PDU:")
                .append(cosemPdu)
                .toString();
    }

}
