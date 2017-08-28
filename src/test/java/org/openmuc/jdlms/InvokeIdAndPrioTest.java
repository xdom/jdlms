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
package org.openmuc.jdlms;

import static org.junit.Assert.assertEquals;
import static org.mockito.internal.util.reflection.Whitebox.setInternalState;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.openmuc.jdlms.internal.asn1.cosem.Invoke_Id_And_Priority;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import junitparams.naming.TestCaseName;

@RunWith(JUnitParamsRunner.class)
public class InvokeIdAndPrioTest {

    @Test
    @Parameters({ "false, 0x81, true", "true, 0xC1, true", "true, 0x41, false" })
    @TestCaseName("invokeIdAndPriorityFor(prioHigh = {2}, confirmedMode = {0}) = {1}, invokeId = 1")
    public void testInvoke_Id_And_Priority(boolean confirmedModeEnabled, String expectedRes, boolean prio) {
        DlmsConnection connection = mock(DlmsConnection.class);

        when(connection.invokeIdAndPriorityFor(Matchers.anyBoolean())).thenCallRealMethod();
        when(connection.confirmedModeEnabled()).thenReturn(confirmedModeEnabled);

        setInternalState(connection, "invokeId", 1);

        Invoke_Id_And_Priority idAndPriority = connection.invokeIdAndPriorityFor(prio);

        byte expectedResByte = (byte) Integer.parseInt(expectedRes.substring(2), 16);
        assertEquals(expectedResByte, idAndPriority.getValue()[0]);
    }
}
