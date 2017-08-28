package org.openmuc.jdlms.settings.client;

import org.openmuc.jdlms.sessionlayer.hdlc.HdlcAddressPair;

public interface HdlcSettings extends Settings {

    HdlcAddressPair addressPair();

    int hdlcMaxInformationLength();
}
