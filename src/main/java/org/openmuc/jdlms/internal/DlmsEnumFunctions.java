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
package org.openmuc.jdlms.internal;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.openmuc.jdlms.datatypes.DlmsEnumeration;
import org.openmuc.jdlms.internal.asn1.axdr.types.AxdrEnum;
import org.openmuc.jdlms.internal.asn1.axdr.types.AxdrInteger;

public class DlmsEnumFunctions {
    public static <T extends Enum<T> & DlmsEnumeration> T enumValueFrom(long code, Class<T> enumClass) {
        return enumValueFrom(new AxdrInteger(code), enumClass);
    }

    public static <T extends DlmsEnumeration> AxdrEnum enumToAxdrEnum(T dlmsEnum) {
        return new AxdrEnum(dlmsEnum.getCode());
    }

    public static <T extends Enum<T> & DlmsEnumeration, M extends AxdrInteger> T enumValueFrom(M code,
            Class<T> enumClass) {
        T[] enumConstants = enumClass.getEnumConstants();

        long maskedCode = code.getValue() & 0xFFFFFFFFFFFFFFFFL;
        for (T constant : enumConstants) {
            if (constant.getCode() == maskedCode) {
                return constant;
            }
        }

        // TODO
        String message = MessageFormat.format("No constant with code {0} in {1}.", maskedCode,
                enumClass.getSimpleName());
        throw new IllegalArgumentException(message);
    }

    public static <T extends Enum<T> & DlmsEnumeration> Map<Long, T> generateEnumMap(Class<T> enumClass) {
        T[] enumConstants = enumClass.getEnumConstants();

        HashMap<Long, T> map = new HashMap<>(enumConstants.length);

        for (T constant : enumConstants) {
            map.put(constant.getCode(), constant);
        }

        return map;
    }

    public static <T extends Enum<T> & DlmsEnumeration> T constantFor(Map<Long, T> mapping, Long code, T defaultValue) {
        T constant = mapping.get(code);

        if (constant != null) {
            return constant;
        }
        else {
            return defaultValue;
        }
    }

    private DlmsEnumFunctions() {
    }
}
