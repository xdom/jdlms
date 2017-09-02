package org.openmuc.jdlms.sessionlayer.hdlc;

import org.openmuc.jdlms.settings.client.HdlcSettings;

/**
 * @author Dominik Matta
 */
public interface HdlcConnectionFactory {
    HdlcConnection getHdlcConnection(HdlcSettings settings,
                                     HdlcConnection.Listener listener);
}
