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

import java.util.HashMap;
import java.util.Map;

import org.openmuc.jdlms.interfaceclass.InterfaceClass;

public class AttributeDirectory {

    private static final Map<InterfaceClass, Class<? extends AttributeClass>> classes;

    static {
        classes = new HashMap<>();
        classes.put(DataAttribute.INTERFACE_CLASS, DataAttribute.class);
        classes.put(RegisterAttribute.INTERFACE_CLASS, RegisterAttribute.class);
        classes.put(ExtendedRegisterAttribute.INTERFACE_CLASS, ExtendedRegisterAttribute.class);
        classes.put(DemandRegisterAttribute.INTERFACE_CLASS, DemandRegisterAttribute.class);
        classes.put(RegisterActivationAttribute.INTERFACE_CLASS, RegisterActivationAttribute.class);
        classes.put(ProfileGenericAttribute.INTERFACE_CLASS, ProfileGenericAttribute.class);
        classes.put(ClockAttribute.INTERFACE_CLASS, ClockAttribute.class);
        classes.put(ScriptTableAttribute.INTERFACE_CLASS, ScriptTableAttribute.class);
        classes.put(ScheduleAttribute.INTERFACE_CLASS, ScheduleAttribute.class);
        classes.put(SpecialDaysTableAttribute.INTERFACE_CLASS, SpecialDaysTableAttribute.class);
        classes.put(ActivityCalendarAttribute.INTERFACE_CLASS, ActivityCalendarAttribute.class);
        classes.put(AssociationLnAttribute.INTERFACE_CLASS, AssociationLnAttribute.class);
        classes.put(AssociationSnAttribute.INTERFACE_CLASS, AssociationSnAttribute.class);
        classes.put(SapAssignmentAttribute.INTERFACE_CLASS, SapAssignmentAttribute.class);
        classes.put(ImageTransferAttribute.INTERFACE_CLASS, ImageTransferAttribute.class);
        classes.put(RegisterMonitorAttribute.INTERFACE_CLASS, RegisterMonitorAttribute.class);
        classes.put(UtilityTablesAttribute.INTERFACE_CLASS, UtilityTablesAttribute.class);
        classes.put(SingleActionScheduleAttribute.INTERFACE_CLASS, SingleActionScheduleAttribute.class);
        classes.put(RegisterTableAttribute.INTERFACE_CLASS, RegisterTableAttribute.class);
        classes.put(StatusMappingAttribute.INTERFACE_CLASS, StatusMappingAttribute.class);
        classes.put(DisconnectControlAttribute.INTERFACE_CLASS, DisconnectControlAttribute.class);
        classes.put(MbusClientAttribute.INTERFACE_CLASS, MbusClientAttribute.class);

        // Protocol related interface classes
        classes.put(IecLocalPortSetupAttribute.INTERFACE_CLASS, IecLocalPortSetupAttribute.class);
        classes.put(ModemConfigurationAttribute.INTERFACE_CLASS, ModemConfigurationAttribute.class);
        classes.put(AutoAnswerAttribute.INTERFACE_CLASS, AutoAnswerAttribute.class);
        classes.put(AutoConnectAttribute.INTERFACE_CLASS, AutoConnectAttribute.class);
        classes.put(IecHdlcSetupClassAttribute.INTERFACE_CLASS, IecHdlcSetupClassAttribute.class);
        classes.put(IecTwistedPairAttribute.INTERFACE_CLASS, IecTwistedPairAttribute.class);
        classes.put(TcpUdpSetupAttribute.INTERFACE_CLASS, TcpUdpSetupAttribute.class);
        classes.put(Ipv4SetupAttribute.INTERFACE_CLASS, Ipv4SetupAttribute.class);
        classes.put(EthernetSetupAttribute.INTERFACE_CLASS, EthernetSetupAttribute.class);
        classes.put(PppSetupAttribute.INTERFACE_CLASS, PppSetupAttribute.class);
        classes.put(GprsModemSetupAttribute.INTERFACE_CLASS, GprsModemSetupAttribute.class);
        classes.put(SmtpSetupAttribute.INTERFACE_CLASS, SmtpSetupAttribute.class);
        classes.put(SecuritySetupAttribute.INTERFACE_CLASS, SecuritySetupAttribute.class);

    }

    public static AttributeClass attributeClassFor(InterfaceClass interfaceClass, int attributeId)
            throws AttributeNotFoundException {
        if (interfaceClass == InterfaceClass.UNKNOWN) {
            throw new AttributeNotFoundException("Interfaceclass is UNKNOWN");
        }

        Class<? extends AttributeClass> attributeClassClass = classes.get(interfaceClass);

        for (AttributeClass attributeClass : attributeClassClass.getEnumConstants()) {
            if (attributeClass.attributeId() == attributeId) {
                return attributeClass;
            }
        }

        throw new AttributeNotFoundException(String.format("Attribute with ID %d not found in intefaceclass %s. ",
                attributeId, interfaceClass.name()));
    }

    public static class AttributeNotFoundException extends Exception {
        public AttributeNotFoundException(String message) {
            super(message);
        }
    }
}
