package org.openmuc.jdlms.internal.asn1.axdr.types;

public class AxdrVisibleString extends AxdrOctetString {

    public AxdrVisibleString() {
        super();
    }

    public AxdrVisibleString(byte[] octetString) {
        super(0, octetString);
    }
}
