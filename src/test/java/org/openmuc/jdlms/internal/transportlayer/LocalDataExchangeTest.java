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
package org.openmuc.jdlms.internal.transportlayer;

import static org.junit.Assert.assertEquals;
import static org.powermock.reflect.Whitebox.invokeMethod;

import java.util.Arrays;
import java.util.Collection;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.openmuc.jdlms.FatalJDlmsException;
import org.openmuc.jdlms.JDlmsException.Fault;
import org.openmuc.jdlms.transportlayer.client.Iec21Layer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

@PowerMockRunnerDelegate(Parameterized.class)
@RunWith(PowerMockRunner.class)
@PrepareForTest(Iec21Layer.class)
public class LocalDataExchangeTest {

    private static final String METHOD_NAME = "baudRateFor";

    private static Iec21Layer dataExchangeClient;

    private final char option;

    private final int expectedBaudRate;

    @BeforeClass
    public static void setupTest() throws Exception {
        dataExchangeClient = PowerMockito.mock(Iec21Layer.class);
        // doCallRealMethod().when(dataExchangeClient, METHOD_NAME, Matchers.anyChar());
    }

    @Parameters(name = "baud rate char {0}; expected value {1}")
    public static Collection<Object[]> data() {
        Object[] o1 = { (char) 0x30, 300 };
        Object[] o2 = { '1', 600 };
        Object[] o3 = { '2', 1200 };
        Object[] o4 = { (char) 0x33, 2400 };
        return Arrays.asList(new Object[][] { o1, o2, o3, o4 });
    }

    public LocalDataExchangeTest(char option, int expectedBaudRate) {
        this.option = option;
        this.expectedBaudRate = expectedBaudRate;
    }

    /*
     * Testing conditions of IEC 62056-21 6.3.14 13c
     */
    @Test
    public void testBaudRateTransformation0() throws Exception {
        int baudRate = invokeMethodWith(this.option);
        assertEquals(this.expectedBaudRate, baudRate);
    }

    @Test(expected = FatalJDlmsException.class)
    public void testBaudRateTransformationError2() throws Exception {
        char option = 0xFF;

        try {
            invokeMethodWith(option);
        } catch (FatalJDlmsException e) {
            assertEquals(Fault.SYSTEM, e.getAssumedFault());
            throw e;
        }
    }

    private int invokeMethodWith(char option) throws Exception {
        return invokeMethod(dataExchangeClient, METHOD_NAME, option);
    }
}
