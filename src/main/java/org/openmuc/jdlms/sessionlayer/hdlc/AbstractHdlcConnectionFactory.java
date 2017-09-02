package org.openmuc.jdlms.sessionlayer.hdlc;

import org.openmuc.jdlms.settings.client.HdlcSettings;

/**
 * @author Dominik Matta
 */
public abstract class AbstractHdlcConnectionFactory implements HdlcConnectionFactory {
    @Override
    public HdlcConnection getHdlcConnection(HdlcSettings settings,
                                            HdlcConnection.Listener listener) {
        HdlcConnection hdlcConnection = createHdlcConnection(settings);
        hdlcConnection.registerNewListener(settings.addressPair(), listener);
        return hdlcConnection;
    }

    protected abstract HdlcConnection createHdlcConnection(HdlcSettings settings);
}
