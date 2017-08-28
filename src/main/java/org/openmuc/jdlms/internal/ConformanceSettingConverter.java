package org.openmuc.jdlms.internal;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.openmuc.jdlms.ConformanceSetting;
import org.openmuc.jdlms.internal.asn1.cosem.Conformance;

public class ConformanceSettingConverter {

    private static final int LENGTH = 3;
    private static final int NUM_BITS = 24;

    private static final ConformanceSetting[] VALUES = ConformanceSetting.values();

    public static Set<ConformanceSetting> conformanceSettingFor(Conformance conformance) {
        Set<ConformanceSetting> settings = new HashSet<>(VALUES.length);

        byte[] bytes = conformance.value;
        int bits = (0xff & bytes[2]) | (bytes[1] << 8) | (bytes[0] << 16);
        for (ConformanceSetting conformanceSetting : VALUES) {
            if (bitsIsSet(bits, conformanceSetting)) {
                settings.add(conformanceSetting);
            }
        }

        return Collections.unmodifiableSet(settings);
    }

    private static boolean bitsIsSet(int bits, ConformanceSetting conformanceSetting) {
        return (bits & (1 << (NUM_BITS - (1 + conformanceSetting.getIndex())))) != 0;
    }

    public static Conformance conformanceFor(ConformanceSetting... settings) {
        return conformanceFor(Arrays.asList(settings));
    }

    public static Conformance conformanceFor(Collection<ConformanceSetting> settings) {
        int bits = 0;

        for (ConformanceSetting conformanceSetting : settings) {
            bits |= 1 << (NUM_BITS - (1 + conformanceSetting.getIndex()));
        }

        byte[] bitString = new byte[LENGTH];
        bitString[0] = (byte) ((bits & 0xFF0000) >> 16);

        bitString[1] = (byte) ((bits & 0xFF00) >> 8);

        bitString[2] = (byte) (bits & 0xFF);

        return new Conformance(bitString, NUM_BITS);
    }

    /*
     * Don't let anyone instantiate this class.
     */
    private ConformanceSettingConverter() {
    }

}
