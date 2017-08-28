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

public enum ClockAttribute implements AttributeClass {

    LOGICAL_NAME(1),
    TIME(2),
    TIME_ZONE(3),
    STATUS(4),
    DAYLIGHT_SAVINGS_BEGIN(5),
    DAYLIGHT_SAVINGS_END(6),
    DAYLIGHT_SAVINGS_DEVIATION(7),
    DAYLIGHT_SAVINGS_ENABLED(8),
    CLOCK_BASE(9);

    static final InterfaceClass INTERFACE_CLASS = InterfaceClass.CLOCK;

    private int id;

    private ClockAttribute(int id) {
        this.id = id;
    }

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
