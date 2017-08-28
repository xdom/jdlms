package org.openmuc.jdlms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.openmuc.jdlms.ConformanceSetting.ACTION;
import static org.openmuc.jdlms.ConformanceSetting.BLOCK_TRANSFER_WITH_GET_OR_READ;
import static org.openmuc.jdlms.ConformanceSetting.EVENT_NOTIFICATION;
import static org.openmuc.jdlms.ConformanceSetting.GET;
import static org.openmuc.jdlms.ConformanceSetting.PRIORITY_MGMT_SUPPORTED;
import static org.openmuc.jdlms.ConformanceSetting.SELECTIVE_ACCESS;
import static org.openmuc.jdlms.ConformanceSetting.SET;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.openmuc.jdlms.internal.ConformanceSettingConverter;
import org.openmuc.jdlms.internal.asn1.cosem.Conformance;

public class LnConformanceSettingTest {

    // see 62056-5-3 © IEC:2013 p 127
    @Test
    public void test1() throws Exception {
        Conformance conformance = ConformanceSettingConverter.conformanceFor(PRIORITY_MGMT_SUPPORTED,
                BLOCK_TRANSFER_WITH_GET_OR_READ, GET, SET, SELECTIVE_ACCESS, EVENT_NOTIFICATION, ACTION);
        byte[] value = conformance.value;
        int bits = (value[2] & 0xFF) | value[1] << 8;

        assertEquals(0x00501F, bits);
    }

    @Test
    public void testSymmetry() throws Exception {
        List<ConformanceSetting> settings = Arrays.asList(GET, SET);

        Set<ConformanceSetting> conformanceSettingRes = ConformanceSettingConverter
                .conformanceSettingFor(ConformanceSettingConverter.conformanceFor(settings));

        for (ConformanceSetting setting : settings) {
            assertTrue(conformanceSettingRes.contains(setting));
        }
    }

}
