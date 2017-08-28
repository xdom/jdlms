package org.openmuc.jdlms.internal.sessionlayer.client;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.openmuc.jdlms.sessionlayer.client.HdlcSequenceNumber;

public class HdlcSequenceNumberTest {

    @Test
    public void testIncrement() throws Exception {
        HdlcSequenceNumber number = new HdlcSequenceNumber();

        for (int i = 0; i < 8; i++) {
            assertEquals(i, number.getValue());
            assertEquals(i, number.increment());
        }
        assertEquals(0, number.getValue());

    }
}
