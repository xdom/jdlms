package org.openmuc.jdlms.internal.association;

import org.openmuc.jasn1.ber.types.BerBitString;
import org.openmuc.jasn1.ber.types.BerInteger;
import org.openmuc.jasn1.ber.types.BerOctetString;
import org.openmuc.jdlms.internal.APdu;
import org.openmuc.jdlms.internal.AssociateSourceDiagnostic.AcseServiceProvider;
import org.openmuc.jdlms.internal.AssociateSourceDiagnostic.AcseServiceUser;
import org.openmuc.jdlms.internal.AssociationResult;
import org.openmuc.jdlms.internal.ContextId;
import org.openmuc.jdlms.internal.ObjectIdentifier;
import org.openmuc.jdlms.internal.asn1.cosem.COSEMpdu;
import org.openmuc.jdlms.internal.asn1.cosem.Conformance;
import org.openmuc.jdlms.internal.asn1.cosem.InitiateResponse;
import org.openmuc.jdlms.internal.asn1.cosem.Integer16;
import org.openmuc.jdlms.internal.asn1.cosem.Unsigned16;
import org.openmuc.jdlms.internal.asn1.cosem.Unsigned8;
import org.openmuc.jdlms.internal.asn1.iso.acse.AAREApdu;
import org.openmuc.jdlms.internal.asn1.iso.acse.ACSEApdu;
import org.openmuc.jdlms.internal.asn1.iso.acse.APTitle;
import org.openmuc.jdlms.internal.asn1.iso.acse.APTitleForm2;
import org.openmuc.jdlms.internal.asn1.iso.acse.AssociateResult;
import org.openmuc.jdlms.internal.asn1.iso.acse.AssociateSourceDiagnostic;
import org.openmuc.jdlms.internal.asn1.iso.acse.AuthenticationValue;

class InitiateResponseBuilder {

    private Conformance conformanceSetting;
    private ContextId contextId;
    private AssociateResult result;
    private AssociateSourceDiagnostic associateSourceDiagnostic;
    private APTitle respondingAPTitle;
    private AuthenticationValue serverToClientChallenge;

    public InitiateResponseBuilder(Conformance conformanceSetting) {
        this.conformanceSetting = conformanceSetting;

        this.contextId = ContextId.LOGICAL_NAME_REFERENCING_WITH_CIPHERING;

        this.result = new AssociateResult(AssociationResult.ACCEPTED.getValue());

        this.associateSourceDiagnostic = new AssociateSourceDiagnostic();
        associateSourceDiagnostic.setAcseServiceUser(new BerInteger(AcseServiceUser.NULL.getCode()));

        this.respondingAPTitle = null;
    }

    public InitiateResponseBuilder setConformanceSetting(Conformance conformanceSetting) {
        this.conformanceSetting = conformanceSetting;
        return this;
    }

    public InitiateResponseBuilder setResult(AssociationResult result) {
        this.result = new AssociateResult(result.getValue());
        return this;
    }

    public InitiateResponseBuilder setSystemTitle(byte[] respondingAPTitle) {
        this.respondingAPTitle = new APTitle();
        this.respondingAPTitle.setApTitleForm2(new APTitleForm2(respondingAPTitle));
        return this;
    }

    public InitiateResponseBuilder setAuthenticationValue(byte[] serverToClientChallenge) {
        this.serverToClientChallenge = new AuthenticationValue();
        this.serverToClientChallenge.setCharstring(new BerOctetString(serverToClientChallenge));
        return this;
    }

    public InitiateResponseBuilder setContextId(ContextId contextId) {
        this.contextId = contextId;
        return this;
    }

    public InitiateResponseBuilder setAssociateSourceDiagnostic(AcseServiceUser acseServiceUser) {
        return setAssociateSourceDiagnostic(acseServiceUser, null);
    }

    public InitiateResponseBuilder setAssociateSourceDiagnostic(AcseServiceProvider acseServiceProvider) {
        return setAssociateSourceDiagnostic(null, acseServiceProvider);
    }

    private InitiateResponseBuilder setAssociateSourceDiagnostic(AcseServiceUser acseServiceUser,
            AcseServiceProvider acseServiceProvider) {
        BerInteger serviceUser = null;
        BerInteger serviceProvider = null;
        if (acseServiceUser != null) {
            serviceUser = new BerInteger(acseServiceUser.getCode());
        }
        if (acseServiceProvider != null) {
            serviceProvider = new BerInteger(acseServiceProvider.getCode());
        }

        this.associateSourceDiagnostic = new AssociateSourceDiagnostic();
        associateSourceDiagnostic.setAcseServiceUser(serviceUser);
        associateSourceDiagnostic.setAcseServiceProvider(serviceProvider);
        return this;
    }

    public APdu build() {

        COSEMpdu xDlmsInitiateResponsePdu = new COSEMpdu();

        InitiateResponse initiateResponse = new InitiateResponse(null, new Unsigned8(6), this.conformanceSetting,
                new Unsigned16(0xffff), new Integer16(0x0007));
        xDlmsInitiateResponsePdu.setinitiateResponse(initiateResponse);

        AAREApdu aare = new AAREApdu();
        aare.setProtocolVersion(new BerBitString(new byte[] { (byte) 0x80 }, 1));
        aare.setApplicationContextName(ObjectIdentifier.applicationContextNameFrom(this.contextId));
        aare.setResult(this.result);
        aare.setResultSourceDiagnostic(this.associateSourceDiagnostic);

        aare.setRespondingAPTitle(this.respondingAPTitle);

        aare.setRespondingAuthenticationValue(this.serverToClientChallenge);

        ACSEApdu aarqAcseAPdu = new ACSEApdu();
        aarqAcseAPdu.setAare(aare);

        return new APdu(aarqAcseAPdu, xDlmsInitiateResponsePdu);
    }

}
