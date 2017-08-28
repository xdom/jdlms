/*
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
 *
 */
package org.openmuc.jdlms.sessionlayer.hdlc;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmuc.jdlms.HexConverter;
import org.openmuc.jdlms.sessionlayer.hdlc.HdlcAddress;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import junitparams.naming.TestCaseName;

@RunWith(JUnitParamsRunner.class)
public class HdlcAddressTest {

    /*
     * See EN 62056-46:2002 + A1:2007 p. 40 for further information
     */

    @Test
    @Parameters(method = "params1")
    @TestCaseName("Test if {0} encoded and decoded again equals itself")
    public void test_symmetry(HdlcAddress address) throws Exception {
        byte[] encodedAddress = address.encode();
        HdlcAddress decodedAddress = HdlcAddress.decode(encodedAddress, encodedAddress.length);

        assertEquals(address.getLogicalId(), decodedAddress.getLogicalId());
        assertEquals(address, decodedAddress);
    }

    public Object params1() {
        final Object param1 = new HdlcAddress(10);
        final Object param2 = new HdlcAddress(10, 12);
        return new Object[] { param1, param2 };
    }

    @Test
    @Parameters(method = "params2")
    @TestCaseName("Testing if {1} decodes to {0}")
    public void test_decoding_from_string_data(HdlcAddress expectedAddress, String hexData) throws Exception {
        byte[] data = HexConverter.fromShortHexString(hexData);
        int length = data.length;
        HdlcAddress address = HdlcAddress.decode(data, length);

        assertEquals(expectedAddress, address);
    }

    public Object params2() {
        final Object[] param1 = { new HdlcAddress(100), "C9" };
        final Object[] param2 = { new HdlcAddress(1, 17), "0223" };
        final Object[] param3 = { new HdlcAddress(145, 1), "02220003" };

        return new Object[][] { param1, param2, param3 };
    }

    @Test
    @Parameters({ "9", "30", "27", "77", "10" })
    public void test_decode(final int logicalDeviceAddress) throws Exception {

        byte[] bytes = { (byte) (logicalDeviceAddress << 1 | 1) };

        HdlcAddress decodedAddress = HdlcAddress.decode(bytes, bytes.length);

        assertEquals(logicalDeviceAddress, decodedAddress.getLogicalId());

    }
}
