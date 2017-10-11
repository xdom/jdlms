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
package org.openmuc.jdlms.interfaceclass.attribute;

import org.openmuc.jdlms.interfaceclass.InterfaceClass;

public enum MbusClientAttribute implements AttributeClass {
    LOGICAL_NAME(1),
    MBUS_PORT_REFERENCE(2),
    CAPTURE_DEFINITION(3),
    CAPTURE_PERIOD(4),
    PRIMARY_ADDRESS(5),
    IDENTIFICATION_NUMBER(6),
    MANUFACTURER_ID(7),
    VERSION(8),
    DEVICE_TYPE(9),
    ACCESS_NUMBER(10),
    STATUS(11),
    ALARM(12),
    CONFIGURATION(13),
    ENCRYPTION_KEY_STATUS(14);

    static final InterfaceClass INTERFACE_CLASS = InterfaceClass.MBUS_CLIENT;

    private MbusClientAttribute(int id) {
        this.id = id;
    }

    private int id;

    @Override
    public int attributeId() {
        return id;
    }

    @Override
    public String attributeName() {
        return name();
    }

    @Override
    public InterfaceClass interfaceClass() {
        return INTERFACE_CLASS;
    }

}
