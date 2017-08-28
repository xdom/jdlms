package org.openmuc.jdlms.internal;

import org.openmuc.jdlms.SecuritySuite;
import org.openmuc.jdlms.sessionlayer.server.ServerSessionLayer;

/**
 * DTO -- Data Transfer Object
 */
public class ServerConnectionData {
    public byte[] processedServerToClientChallenge;

    public byte[] clientToServerChallenge;

    public int clientId;

    public byte[] clientSystemTitle;

    public int frameCounter;

    public boolean authenticated;

    public long clientMaxReceivePduSize;

    public SecuritySuite securitySuite;

    public final ServerSessionLayer sessionLayer;

    public final Long connectionId;

    public ServerConnectionData(ServerSessionLayer sessionLayer, Long connectionId) {
        this.sessionLayer = sessionLayer;
        this.connectionId = connectionId;
        this.authenticated = false;
        this.frameCounter = 1;
        this.securitySuite = SecuritySuite.builder().build();
    }

}
