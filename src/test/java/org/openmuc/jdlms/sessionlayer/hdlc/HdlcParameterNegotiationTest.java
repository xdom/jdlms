/*
 * Copyright 2012-17 Fraunhofer ISE
 *
 * This file is part of jDLMS. For more information visit http://www.openmuc.org
 *
 * jDLMS is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * jDLMS is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with jDLMS. If not, see
 * <http://www.gnu.org/licenses/>.
 *
 */
package org.openmuc.jdlms.sessionlayer.hdlc;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openmuc.jdlms.sessionlayer.hdlc.HdlcParameters;

public class HdlcParameterNegotiationTest {

    @Test
    public void tesEncodeDecodetSymmetry() throws Exception {
        final int transmitInformationLength = HdlcParameters.MIN_INFORMATION_LENGTH + 2;
        final int transmitWindowSize = HdlcParameters.MIN_WINDOW_SIZE + 3;
        HdlcParameters parameterNegotiation = new HdlcParameters(transmitInformationLength, transmitWindowSize);

        byte[] encodedParameterNegotiation = parameterNegotiation.encode();

        HdlcParameters parameterNegotiation2 = HdlcParameters.decode(encodedParameterNegotiation);

        assertEquals(transmitInformationLength, parameterNegotiation2.getTransmitInformationLength());
        assertEquals(transmitWindowSize, parameterNegotiation2.getTransmitWindowSize());
    }

    @Test
    public void testSymmetry() throws Exception {
        HdlcParameters par = new HdlcParameters(HdlcParameters.MAX_INFORMATION_LENGTH, HdlcParameters.MIN_WINDOW_SIZE);
        HdlcParameters parameters = HdlcParameters.decode(par.encode());

        assertEquals(par.getReceiveInformationLength(), parameters.getReceiveInformationLength());
        assertEquals(par.getTransmitInformationLength(), parameters.getTransmitInformationLength());

        assertEquals(par.getReceiveWindowSize(), parameters.getReceiveWindowSize());
        assertEquals(par.getTransmitWindowSize(), parameters.getTransmitWindowSize());

    }
}
