package org.openmuc.jdlms.internal.security;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RandomSequenceGeneratorTest {

    @Test
    public void testNextRand() throws Exception {
        int challengeLenth = 10;
        byte[] data = RandomSequenceGenerator.generateNewChallenge(challengeLenth);
        assertEquals(challengeLenth, data.length);
    }

}
