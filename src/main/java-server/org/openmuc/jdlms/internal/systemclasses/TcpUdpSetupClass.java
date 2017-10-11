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
package org.openmuc.jdlms.internal.systemclasses;

import org.openmuc.jdlms.CosemAttribute;
import org.openmuc.jdlms.CosemClass;
import org.openmuc.jdlms.CosemInterfaceObject;
import org.openmuc.jdlms.ObisCode;
import org.openmuc.jdlms.datatypes.DataObject;

@CosemClass(id = 41, version = 0)
public class TcpUdpSetupClass extends CosemInterfaceObject {

    @CosemAttribute(id = 2)
    private DataObject dlReference;

    @CosemAttribute(id = 3)
    private DataObject ipAddress;

    @CosemAttribute(id = 4)
    private DataObject multicastIpAddress;

    @CosemAttribute(id = 5)
    private DataObject subnetMask;

    @CosemAttribute(id = 6)
    private DataObject gatewayIpAddress;

    public TcpUdpSetupClass(ObisCode instanceId) {
        super(instanceId);
    }

}
