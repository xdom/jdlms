package org.openmuc.jdlms;

import static org.junit.Assert.assertArrayEquals;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;

@RunWith(JUnitParamsRunner.class)
public class LogicalDeviceTest {
    public Object testAddRestrictionsParam() {
        Object[] p1 = { 0, SecuritySuite.builder().build() };
        Object[] p2 = { 1, null };
        return new Object[] { p1, p2 };
    }

    @Test(expected = IllegalArgumentException.class)
    @Parameters(method = "testAddRestrictionsParam")
    public void testAddRestrictions(int clientId, SecuritySuite securitySuite) throws Exception {

        LogicalDevice ld = mock(LogicalDevice.class);
        when(ld.addRestriction(Matchers.anyInt(), Matchers.any(SecuritySuite.class))).thenCallRealMethod();

        ld.addRestriction(clientId, securitySuite);
    }

    public Object testLdConstructorParam() {
        Object[] p1 = { 1, new String(new byte[17]).replaceAll("\0", "a"), "ISE", 9 };
        Object[] p2 = { 0, new String(new byte[16]).replaceAll("\0", "a"), "ISE", 9 };
        Object[] p3 = { 1, new String(new byte[16]).replaceAll("\0", "a"), "I", 9 };
        Object[] p4 = { 1, new String(new byte[16]).replaceAll("\0", "a"), "ISEW", 9 };
        return new Object[] { p1, p2, p3, p4 };
    }

    @Test(expected = IllegalArgumentException.class)
    @Parameters(method = "testLdConstructorParam")
    public void testLdConstructor(int logicalDeviceId, String logicalDeviceName, String manufacturerId, long deviceId)
            throws Exception {
        new LogicalDevice(logicalDeviceId, logicalDeviceName, manufacturerId, deviceId);
    }

    public Object testSetMasterKeyParam() {
        Object[] p1 = { new byte[100] };
        Object[] p2 = { new byte[128] };
        Object[] p3 = { new byte[15] };
        Object[] p4 = { new byte[17] };
        return new Object[] { p1, p2, p3, p4 };
    }

    @Test(expected = IllegalArgumentException.class)
    @Parameters(method = "testSetMasterKeyParam")
    public void testSetMasterKey(byte[] masterKey) {
        LogicalDevice ld = setupMKeyLd();

        ld.setMasterKey(masterKey);
    }

    @Test
    public void testSetMasterKeySuccess() throws Exception {
        LogicalDevice ld = setupMKeyLd();
        byte[] generateAES128Key = SecurityUtils.generateAES128Key();

        ld.setMasterKey(generateAES128Key.clone());

        assertArrayEquals(generateAES128Key, ld.getMasterKey());
    }

    private LogicalDevice setupMKeyLd() {
        LogicalDevice ld = mock(LogicalDevice.class);
        when(ld.setMasterKey(Matchers.any(byte[].class))).thenCallRealMethod();
        when(ld.getMasterKey()).thenCallRealMethod();
        return ld;
    }
}
