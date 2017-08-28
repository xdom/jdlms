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

public enum DemandRegisterAttribute implements AttributeClass {
    LOGICAL_NAME(1),
    CURRENT_AVERAGE_VALUE(2),
    LAST_AVERAGE_VALUE(3),
    SCALER_UNIT(4),
    STATUS(5),
    CAPTURE_TIME(6),
    START_TIME_CURRENT(7),
    PERIOD(8),
    NUMBER_OF_PERIODS(9);

    static final InterfaceClass INTERFACE_CLASS = InterfaceClass.DEMAND_REGISTER;

    private int id;

    private DemandRegisterAttribute(int id) {
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
