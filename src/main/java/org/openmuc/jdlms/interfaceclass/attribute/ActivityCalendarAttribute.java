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

public enum ActivityCalendarAttribute implements AttributeClass {
    LOGICAL_NAME(1),
    CALENDAR_NAME_ACTIVE(2),
    SEASON_PROFILE_ACTIVE(3),
    WEEK_PROFILE_TABLE_ACTIVE(4),
    DAY_PROFILE_TABLE_ACTIVE(5),
    CALENDAR_NAME_PASSIVE(6),
    SEASON_PROFILE_PASSIVE(7),
    WEEK_PROFILE_TABLE_PASSIVE(8),
    DAY_PROFILE_TABLE_PASSIVE(9),
    ACTIVATE_PASSIVE_CALENDAR_TIME(10);

    static final InterfaceClass INTERFACE_CLASS = InterfaceClass.ACTIVITY_CALENDAR;

    private int id;

    private ActivityCalendarAttribute(int id) {
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
