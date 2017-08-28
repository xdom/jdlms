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
package org.openmuc.jdlms.interfaceclass.method;

import java.util.HashMap;
import java.util.Map;

import org.openmuc.jdlms.interfaceclass.InterfaceClass;

public class MethodDirectory {

    private static final Map<InterfaceClass, Class<? extends MethodClass>> classes;

    static {
        classes = new HashMap<>();
        classes.put(RegisterMethod.INTERFACE_CLASS, RegisterMethod.class);
        classes.put(ExtendedRegisterMethod.INTERFACE_CLASS, ExtendedRegisterMethod.class);
        classes.put(DemandRegisterMethod.INTERFACE_CLASS, DemandRegisterMethod.class);
        classes.put(RegisterActivationMethod.INTERFACE_CLASS, RegisterActivationMethod.class);
        classes.put(ProfileGenericMethod.INTERFACE_CLASS, ProfileGenericMethod.class);
        classes.put(ClockMethod.INTERFACE_CLASS, ClockMethod.class);
        classes.put(ScriptTableMethod.INTERFACE_CLASS, ScriptTableMethod.class);
        classes.put(ScheduleMethod.INTERFACE_CLASS, ScheduleMethod.class);
        classes.put(SpecialDaysTableMethod.INTERFACE_CLASS, SpecialDaysTableMethod.class);
        classes.put(ActivityCalendarMethod.INTERFACE_CLASS, ActivityCalendarMethod.class);
        classes.put(AssociationLnMethod.INTERFACE_CLASS, AssociationLnMethod.class);
        classes.put(AssociationSnMethod.INTERFACE_CLASS, AssociationSnMethod.class);
        classes.put(SapAssignmentMethod.INTERFACE_CLASS, SapAssignmentMethod.class);
        classes.put(ImageTransferMethod.INTERFACE_CLASS, ImageTransferMethod.class);
        classes.put(RegisterTableMethod.INTERFACE_CLASS, RegisterTableMethod.class);
        classes.put(SecuritySetupMethod.INTERFACE_CLASS, SecuritySetupMethod.class);
        classes.put(DisconnectControlMethod.INTERFACE_CLASS, DisconnectControlMethod.class);
        classes.put(MBusClientMethod.INTERFACE_CLASS, MBusClientMethod.class);

        // Protocol related interface classes
        classes.put(Ipv4SetupMethod.INTERFACE_CLASS, Ipv4SetupMethod.class);
    }

    public static MethodClass methodClassFor(InterfaceClass interfaceClass, int methodId)
            throws MethodNotFoundException {
        if (interfaceClass == InterfaceClass.UNKNOWN) {
            throw new MethodNotFoundException("Interfaceclass is UNKNOWN");
        }
        Class<? extends MethodClass> methodClassClass = classes.get(interfaceClass);

        for (MethodClass methodClass : methodClassClass.getEnumConstants()) {
            if (methodClass.getMethodId() == methodId) {
                return methodClass;
            }
        }

        throw new MethodNotFoundException(
                String.format("Method with ID %d not found in intefaceclass %s. ", methodId, interfaceClass.name()));
    }

    public static class MethodNotFoundException extends Exception {
        public MethodNotFoundException(String message) {
            super(message);
        }
    }
}
