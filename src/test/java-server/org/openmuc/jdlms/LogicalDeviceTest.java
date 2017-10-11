/**
 * Copyright 2012-17 Fraunhofer ISE
 *
 * This file is part of jDLMS.
 * For more information visit http://www.openmuc.org
 *
 * jDLMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jDLMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with jDLMS.  If not, see <http://www.gnu.org/licenses/>.
 */
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
