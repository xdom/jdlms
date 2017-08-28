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
package org.openmuc.jdlms.interfaceclass.attribute;

import org.openmuc.jdlms.interfaceclass.InterfaceClass;

public enum IecLocalPortSetupAttribute implements AttributeClass {
    LOGICAL_NAME(1),
    DEFAULT_MODE(2),
    DEFAULT_BAUD(3),
    PROP_BAUD(4),
    RESPONSE_TIME(5),
    DEVICE_ADDR(6),
    PASS_P1(7),
    PASS_P2(8),
    PASS_W5(9);

    static final InterfaceClass INTERFACE_CLASS = InterfaceClass.IEC_LOCAL_PORT_SETUP;
    private int attributeId;

    private IecLocalPortSetupAttribute(int attributeId) {
        this.attributeId = attributeId;
    }

    @Override
    public int attributeId() {
        return attributeId;
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
