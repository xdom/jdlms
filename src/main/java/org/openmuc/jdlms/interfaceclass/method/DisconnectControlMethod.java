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
package org.openmuc.jdlms.interfaceclass.method;

import org.openmuc.jdlms.interfaceclass.InterfaceClass;

public enum DisconnectControlMethod implements MethodClass {
    REMOTE_DISCONNECT(1, true),
    REMOTE_RECONNECT(2, true);

    static final InterfaceClass INTERFACE_CLASS = InterfaceClass.DISCONNECT_CONTROL;

    private final int id;
    private final boolean mandatory;

    private DisconnectControlMethod(int id, boolean mandatory) {
        this.id = id;
        this.mandatory = mandatory;
    }

    @Override
    public int getMethodId() {
        return id;
    }

    @Override
    public InterfaceClass getInterfaceClass() {
        return INTERFACE_CLASS;
    }

    @Override
    public boolean isMandatory() {
        return mandatory;
    }

    @Override
    public String getMethodName() {
        return name();
    }

}
