package org.openmuc.jdlms.internal.asn1.axdr.types;

public class AxdrGeneralizedTime extends AxdrOctetString {

    public AxdrGeneralizedTime() {
        super();
    }

    public AxdrGeneralizedTime(byte[] octetString) {
        super(0, octetString);
    }
}
