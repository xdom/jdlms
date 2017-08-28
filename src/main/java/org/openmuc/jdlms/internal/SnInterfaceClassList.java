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
package org.openmuc.jdlms.internal;

import java.util.HashMap;
import java.util.Map;

import org.openmuc.jdlms.MethodIdOffsetPair;
import org.openmuc.jdlms.SnObjectInfo;

/**
 * List of all supported COSEM interface classes
 */
public class SnInterfaceClassList {

    private static MethodIdOffsetPair newPair(int v1, int v2) {
        return new MethodIdOffsetPair(v1, v2);
    }

    private static Map<MethodIdOffsetPair, MethodIdOffsetPair> interfaceClassMap;

    public static MethodIdOffsetPair firstMethodPairFor(SnObjectInfo snObjectInfo) {
        MethodIdOffsetPair firstMethodIdOffsetPair = snObjectInfo.getFirstMethodIdOffsetPair();
        if (firstMethodIdOffsetPair != null) {
            return firstMethodIdOffsetPair;
        }

        MethodIdOffsetPair methodIdOffsetPair = interfaceClassMap
                .get(newPair(snObjectInfo.getClassId(), snObjectInfo.getVersion()));

        return methodIdOffsetPair;
    }

    static {
        interfaceClassMap = new HashMap<>();

        // Register class
        interfaceClassMap.put(newPair(3, 0), newPair(1, 0x28));

        // Extended register class
        interfaceClassMap.put(newPair(4, 0), newPair(1, 0x38));

        // Demand register class
        interfaceClassMap.put(newPair(5, 0), newPair(1, 0x48));

        // Register activation class
        interfaceClassMap.put(newPair(6, 0), newPair(1, 0x30));

        // Profile generic class
        interfaceClassMap.put(newPair(7, 1), newPair(1, 0x58));

        // Clock class
        interfaceClassMap.put(newPair(8, 0), newPair(1, 0x60));

        // Script table class
        interfaceClassMap.put(newPair(9, 0), newPair(1, 0x20));

        // Schedule class
        interfaceClassMap.put(newPair(10, 0), newPair(1, 0x20));

        // Special days table class
        interfaceClassMap.put(newPair(11, 0), newPair(1, 0x10));

        // Activity calendar class
        interfaceClassMap.put(newPair(20, 0), newPair(1, 0x50));

        // Association SN class
        interfaceClassMap.put(newPair(12, 0), newPair(1, 0x20));
        interfaceClassMap.put(newPair(12, 1), newPair(3, 0x30));
        interfaceClassMap.put(newPair(12, 2), newPair(3, 0x30));
        interfaceClassMap.put(newPair(12, 3), newPair(3, 0x30));

        // SAP assignment class
        interfaceClassMap.put(newPair(17, 0), newPair(1, 0x20));

        // image transfer class
        interfaceClassMap.put(newPair(18, 0), newPair(1, 0x40));

        // -------------------NO-METHODS--------------------

        // Data class

        // Register monitor class

        // Utilities table class id:26, v:0

        // Single action schedule class id:22, v:0

        // Status mapping class id:63, v:0

        // IEC local port setup class id:19, v:0,1

        // Modem configuration class id:27, v:0,1

        // Auto answer class id:28, v:0

        // PSTN auto dial class id:29, v:0

        // Auto connect class id:29, v:1

        // IEC HDLC setup class id:23, v:0,1

        // IEC twisted pair setup class id:24, v:0

        // TCP-UDP setup class id:41, v:0

        // / PPP setup class id:44, v:0

        // GPRS modem setup class id:45, v:0

        // SMTP setup class id:46, v:0

        // -------------------------------------------------

        // push setup id:40, v:0
        interfaceClassMap.put(newPair(40, 0), newPair(1, 0x38));

        // Register table class
        interfaceClassMap.put(newPair(61, 0), newPair(1, 0x28));

        // security setup
        interfaceClassMap.put(newPair(64, 0), newPair(1, 0x28));

        // parameter monitor
        interfaceClassMap.put(newPair(65, 0), newPair(1, 0x20));

        // sensor manager
        interfaceClassMap.put(newPair(67, 0), newPair(1, 0x80));

        // disconnect control
        interfaceClassMap.put(newPair(70, 0), newPair(1, 0x20));

        // m-bus client
        interfaceClassMap.put(newPair(72, 0), newPair(1, 0x60));

        // IPv4 setup class
        interfaceClassMap.put(newPair(42, 0), newPair(1, 0x60));

    }

    private SnInterfaceClassList() {
    }

}
