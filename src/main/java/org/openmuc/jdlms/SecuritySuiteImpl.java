package org.openmuc.jdlms;

import org.openmuc.jdlms.internal.SecSuiteAccessor;

class SecuritySuiteImpl extends SecuritySuite implements SecSuiteAccessor {

    public SecuritySuiteImpl(byte[] globalUnicastEncryptionKey, byte[] authenticationKey, byte[] password,
            EncryptionMechanism cryptographicAlgorithm, AuthenticationMechanism authenticationLevel,
            SecurityPolicy securityPolicy) {
        super(globalUnicastEncryptionKey, authenticationKey, password, cryptographicAlgorithm, authenticationLevel,
                securityPolicy);

    }

    @Override
    public void updateGlobalUnicastEncryptionKey(byte[] newKey) {
        super.globalUnicastEncryptionKey = newKey;
    }

    @Override
    public void updateAuthentciationKey(byte[] newKey) {
        super.authenticationKey = newKey;
    }

}
