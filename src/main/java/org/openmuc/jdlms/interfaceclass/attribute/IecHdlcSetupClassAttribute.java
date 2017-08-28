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

public enum IecHdlcSetupClassAttribute implements AttributeClass {
    LOGICAL_NAME(1),
    COMM_SPEED(2),
    WINDOW_SIZE_TRANSMIT(3),
    WINDOW_SIZE_RECEIVE(4),
    MAX_INFO_FIELD_LENGTH_TRANSMIT(5),
    MAX_INFO_FIELD_LENGTH_RECEIVE(6),
    INTER_OCTET_TIME_OUT(7),
    INACTIVITY_TIME_OUT(8),
    DEVICE_ADDRESS(9),;

    static final InterfaceClass INTERFACE_CLASS = InterfaceClass.IEC_HDLC_SETUP_CLASS;
    private int attributeId;

    private IecHdlcSetupClassAttribute(int attributeId) {
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
