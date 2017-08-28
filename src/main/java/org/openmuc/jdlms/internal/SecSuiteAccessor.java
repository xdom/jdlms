package org.openmuc.jdlms.internal;

public interface SecSuiteAccessor {
    void updateGlobalUnicastEncryptionKey(byte[] newKey);

    void updateAuthentciationKey(byte[] newKey);
}
